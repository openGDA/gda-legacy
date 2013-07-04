/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.oe.util;

import gda.configuration.properties.LocalProperties;
import gda.factory.Configurable;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.factory.Localizable;
import gda.jscience.physics.units.NonSIext;
import gda.lockable.LockableComponent;
import gda.oe.Moveable;
import gda.oe.MoveableStatus;
import gda.oe.OE;
import gda.oe.OEBase;
import gda.oe.dofs.DOF;
import gda.oe.dofs.PolarizationValue;
import gda.oe.positioners.UndulatorPhasePositioner;

import java.util.ArrayList;

import org.jscience.physics.quantities.Angle;
import org.jscience.physics.quantities.Energy;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.NonSI;
import org.jscience.physics.units.SI;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mediates undulator moves. Movements of the DOFs for Energy, Harmonic and Polarization need cooperative moves of the
 * underlying Gap and either the MutualPhase or the OpposingPhase.
 */
public class UndulatorMoveCalculator implements Configurable, Findable, Localizable {
	private static final Logger logger = LoggerFactory.getLogger(UndulatorMoveCalculator.class);

	/**
	 * Move Class
	 */
	public class Move {

		private Quantity[] positions;

		/**
		 * @param gap
		 * @param phase
		 */
		public Move(double gap, double phase) {
			positions = new Quantity[2];
			positions[0] = Quantity.valueOf(gap, SI.MILLI(SI.METER));
			positions[1] = Quantity.valueOf(phase, SI.MILLI(SI.METER));
		}

		/**
		 * @return positions
		 */
		public Quantity[] getPositions() {
			return positions;
		}

	}

	private Energy currentEnergy = null;

	private int currentHarmonic = -1;

	private Angle currentPolarization = null;

	private ArrayList<String> lookupTableList = new ArrayList<String>();

	private LookUpTable[] lookuptable = null;

	private ArrayList<String> moveableNameList = new ArrayList<String>();

	private ArrayList<Move> moves;

	private DOFRouteChecker mutualPhaseGapPowerMap = null;

	private DOFRouteChecker opposingPhaseGapPowerMap = null;

	private boolean configured = false;

	private Moveable[] moveables;

	private String name;

	private boolean local = false;

	private final int GAP = 0;

	private final int MUTUALPHASE = 1;

	private final int OPPOSINGPHASE = 2;

	private String oeName;

	private OE oe;

	private Moveable[] toBeMoved = null;

	private Quantity requestedHarmonic = Quantity.valueOf(1.0, Unit.ONE);

	private Angle requestedPolarization = Quantity.valueOf(360, NonSIext.DEG_ANGLE);

	private Energy requestedEnergy;

	private ArrayList<UndulatorMoveCalculatorWatcher> watchers = new ArrayList<UndulatorMoveCalculatorWatcher>();

	private Quantity[] positionsAfterLastMove = null;

	/**
	 * Constructor
	 */
	public UndulatorMoveCalculator() {
	}

	@Override
	public void configure() {
		oe = (OE) Finder.getInstance().find(oeName);
		if (!configured) {
			int nosOfMoveables = moveableNameList.size();
			moveables = new Moveable[nosOfMoveables];

			ArrayList<Moveable> dofList = ((OEBase) oe).getMoveableList();
			for (int i = 0; i < nosOfMoveables; i++) {
				for (int j = 0; j < dofList.size(); j++) {
					if ((dofList.get(j)).getName().equals(moveableNameList.get(i))) {
						moveables[i] = dofList.get(j);
						break;
					}
				}
			}

			// Find out properties folder
			String propertiesFolder = LocalProperties.get("gda.oe.undulatorLookupDir","${gda.config}");
			// Fill array of lookuptables (lookupTableList will have been
			// set
			// automatically from being in the XML)
			lookuptable = new LookUpTable[lookupTableList.size()];
			for (int i = 0; i < lookupTableList.size(); i++) {
				lookuptable[i] = new LookUpTable(propertiesFolder + "/" + lookupTableList.get(i));
			}

			// FIXME: the DOFRouteCheckers are created in the XML file but
			// only their forbiddenZones are set there. They should
			// also get their DOFs from the XML file and indeed should
			// construct their forbiddenZones and movements from data
			// found in a file.

			mutualPhaseGapPowerMap.setXMover((DOF) moveables[MUTUALPHASE]);
			mutualPhaseGapPowerMap.setYMover((DOF) moveables[GAP]);
			opposingPhaseGapPowerMap.setXMover((DOF) moveables[OPPOSINGPHASE]);
			opposingPhaseGapPowerMap.setYMover((DOF) moveables[GAP]);
			configured = true;

			// The starting positions are a problem. Currently Energy is
			// 0.0,
			// Harmonic is arbitrarily 1 and Polarization is arbitrarily
			// LCP.
			// To get movemements to happen consistently from this point it
			// is best to move Gap and MutualPhase to positions
			// corresponding
			// to some Energy at LCP. Then move Energy to that energy and
			// from
			// then everything should be alright. This is obviously
			// unsatisfactory.
			// positionValid = false;
			guessHarmonic();
			guessPolarization();
			guessEnergy();
		}
	}

	/**
	 * This calculateMoveables calculates the positions for a new energy, harmonic and polarization.
	 * 
	 * @return an array of Quantities, one for each Moveable
	 */
	private Quantity[] calculateMoveables() {
		int requestedHarmonicIndex = requestedHarmonic.intValue();
		double[] values = lookuptable[requestedHarmonicIndex - 1].calculateValues(requestedEnergy.to(
				NonSI.ELECTRON_VOLT).getAmount(), requestedPolarization);

		// The toBeMoved array is set to contain whichever of the two DOFs are
		// needed for this move.
		toBeMoved = new Moveable[2];
		for (int k = 0; k < 2; k++)
			for (int j = 0; j < moveables.length; j++)
				if (moveables[j].getName().endsWith(
						lookuptable[requestedHarmonicIndex - 1].getDofNames(requestedPolarization)[k]))
					toBeMoved[k] = moveables[j];

		Quantity[] rtrn = new Quantity[values.length];

		logger.debug(" UndulatorMoveCalculator required energy was: " + requestedEnergy.to(NonSI.ELECTRON_VOLT));
		logger.debug(" UndulatorMoveCalculator required harmonic was: " + requestedHarmonic);
		logger.debug(" UndulatorMoveCalculator required polarization was: " + requestedPolarization);
		for (int i = 0; i < values.length; i++) {
			rtrn[i] = Quantity.valueOf(values[i], SI.MILLI(SI.METER));
		}

		return rtrn;
	}

	/**
	 * This is called by a DOFCommand object during the setting up and locking phase of the move.
	 * 
	 * @return the success or otherwise of the locking and checking
	 */
	public int checkMoveMoveables() {
		// Need to override the DOF checkMoveMoveables method because the
		// route checker may want to split the move into parts
		int check = MoveableStatus.MOVE_NOT_ALLOWED;

		// This method is called from the createAbsoluteMover method of one
		// of UndulatorEnergyDOF, UndulatorHarmonicDOF or
		// UndulatorPolarizationDOF.
		// Values will be set for requestedEnergy, requestedHarmonic and
		// requestedPolarization (these may be the same as the current values).
		Quantity[] moveablePositions = calculateMoveables();

		logger
				.debug("UndulatorMoveCalculator checkMoveMoveables calculateMoveables has returned: "
						+ moveablePositions);

		for (int j = 0; j < moveablePositions.length; j++)
			logger.debug("     " + moveablePositions[j]);

		// We expect the lookup tables to specify gap and either mutual phase
		// or opposing phase.
		// We expect two power maps one with x as mutualPhase and y as gap, one
		// with x as opposing phase and y as gap. Which one controls whether the
		// move is allowed depends on the polarization setting.
		// The method isMoveAllowed returns an array of moves which will get
		// to the required position.
		moves = isMoveAllowed(((Length) moveablePositions[0]).to(SI.MILLI(SI.METER)).getAmount(),
				((Length) moveablePositions[1]).to(SI.MILLI(SI.METER)).getAmount());

		if (moves == null) {
			check = MoveableStatus.MOVE_NOT_ALLOWED;
		} else {
			// More BFI, this checks that the moves are allowed by
			// soft limits and so on. In order to do this we have to
			// try to lock them with a specially created
			// LockableComponent. If the locking succeeds then we
			// have to unlock (if the move is not allowed then the
			// Moveable will not be locked).
			LockableComponent lc = new LockableComponent();
			int moveCheck = MoveableStatus.SUCCESS;
			for (int i = 0; i < moves.size(); i++) {
				Quantity[] positions = moves.get(i).getPositions();
				moveCheck = toBeMoved[0].checkMoveTo(positions[0], lc);
				if (moveCheck == MoveableStatus.SUCCESS)
					toBeMoved[0].unLock(lc);
				else
					break;
				moveCheck = toBeMoved[1].checkMoveTo(positions[1], lc);
				if (moveCheck == MoveableStatus.SUCCESS)
					toBeMoved[1].unLock(lc);
				else
					break;
			}
			check = moveCheck;
			if (check != MoveableStatus.SUCCESS)
				check = MoveableStatus.ERROR;
		}
		return check;

	}

	/**
	 * Returns the current position
	 * 
	 * @return the current energy
	 */
	public Quantity getPosition() {
		// Returns arbitrarily the currentEnergy
		return currentEnergy;
	}

	/**
	 * Returns an ArrayList of Moves which will get the Undulator to the specified gap and phase.
	 * 
	 * @param gap
	 *            the gap to move to
	 * @param phase
	 *            the phase to move to
	 * @return an ArrayList of Moves, null if the final position is not allowed
	 */
	private ArrayList<Move> isMoveAllowed(double gap, double phase) {
		ArrayList<Move> rtrn = null;
		double[][] moves = null;

		// Pass the move to the relevant power map for checking. This will
		// return null if the move is not allowed or an two dimensional
		// array of moves.

		if (toBeMoved[1].getName().equals("UndulatorMutualPhase"))
			moves = mutualPhaseGapPowerMap.isAllowedMove(phase, gap);
		else
			moves = opposingPhaseGapPowerMap.isAllowedMove(phase, gap);

		// Each row (first dimension) in the moves array will be a position
		// of phase (mutual or opposing) and gap to move to (in that
		// order because it will be easier for power to be specified
		// with phase as x and gap as y). From each row we create a Move
		// (with the necessary swap over of values) and add it to an
		// ArrayList<Move> of Moves to be returned.

		if (moves != null) {
			rtrn = new ArrayList<Move>();
			for (int i = 0; i < moves.length; i++) {
				rtrn.add(new Move(moves[i][1], moves[i][0]));
			}
		}
		return rtrn;
	}

	/**
	 * Sets the new values of currentEnergy, currentHarmonic and currentPolarization at the end of a move. Whichever DOF
	 * started the move will call this method when it is over.
	 */
	public void moveDone() {

		if (!currentEnergy.equals(requestedEnergy)) {
			currentEnergy = requestedEnergy;
			logger.debug("UndulatorMoveCalculator currentEnergy now " + currentEnergy);
		}
		if (currentHarmonic != requestedHarmonic.intValue()) {
			currentHarmonic = requestedHarmonic.intValue();
			logger.debug("UndulatorMoveCalculator currentHarmonic now " + currentHarmonic);
		}
		if (!currentPolarization.equals(requestedPolarization)) {
			currentPolarization = requestedPolarization;
			logger.debug("UndulatorMoveCalculator currentPolarization now "
					+ PolarizationValue.doubleToString(currentPolarization.getAmount()));
		}

		// FIXME: turn currentHarmonic into a Quantity so it can be used here
		for (UndulatorMoveCalculatorWatcher w : watchers)
			w.inform(currentEnergy, requestedHarmonic, currentPolarization, true);

		positionsAfterLastMove = new Quantity[moveables.length];
		for (int i = 0; i < moveables.length; i++)
			positionsAfterLastMove[i] = moveables[i].getPosition();
	}

	/**
	 * @param newgPPM
	 */
	public void setMutualPhaseGapPowerMap(DOFRouteChecker newgPPM) {
		mutualPhaseGapPowerMap = newgPPM;
	}

	/**
	 * @return mutualPhaseGapPowerMap
	 */
	public DOFRouteChecker getMutualPhaseGapPowerMap() {
		return mutualPhaseGapPowerMap;
	}

	/**
	 * @param newgPPM
	 */
	public void setOpposingPhaseGapPowerMap(DOFRouteChecker newgPPM) {
		opposingPhaseGapPowerMap = newgPPM;
	}

	/**
	 * @return opposingPhaseGapPowerMap
	 */
	public DOFRouteChecker getOpposingPhaseGapPowerMap() {
		return opposingPhaseGapPowerMap;
	}

	/**
	 * @param lookupTableName
	 */
	public void addLookupTableName(String lookupTableName) {
		logger.debug("Adding lookupTableName " + lookupTableName + " to list");
		lookupTableList.add(lookupTableName);
	}
	
	/**
	 * Sets the lookup table names for this calculator.
	 * 
	 * @param lookupTableNames the lookup table names
	 */
	public void setLookupTableNames(ArrayList<String> lookupTableNames) {
		setLookupTableList(lookupTableNames);
	}

	/**
	 * @return Returns the lookupTableList.
	 */
	public ArrayList<String> getLookupTableNames() {
		return lookupTableList;
	}

	/**
	 * @param lookupTableList
	 *            The lookupTableList to set.
	 */
	public void setLookupTableList(ArrayList<String> lookupTableList) {
		this.lookupTableList = lookupTableList;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return Moveable[] toBeMoved
	 */
	public Moveable[] getToBeMoved() {
		return toBeMoved;
	}

	/**
	 * @return moves list
	 */
	public ArrayList<Move> getMoves() {
		return moves;
	}

	/**
	 * @param moveableName
	 */
	public void addMoveableName(String moveableName) {
		logger.debug("Adding moveableName " + moveableName + " to list");
		moveableNameList.add(moveableName);
	}

	/**
	 * @return moveable name list
	 */
	public ArrayList<String> getMoveableNames() {
		return moveableNameList;
	}

	/**
	 * @param moveableNameList
	 *            The moveableNameList to set.
	 */
	public void setMoveableNameList(ArrayList<String> moveableNameList) {
		this.moveableNameList = moveableNameList;
	}
	
	/**
	 * Sets the moveable names for this calculator.
	 * 
	 * @param moveableNames the moveable names
	 */
	public void setMoveableNames(ArrayList<String> moveableNames) {
		setMoveableNameList(moveableNames);
	}

	@Override
	public void setLocal(boolean local) {
		this.local = local;
	}

	@Override
	public boolean isLocal() {
		return local;
	}

	/**
	 * @return Returns the oeName.
	 */
	public String getOeName() {
		return oeName;
	}

	/**
	 * @param oeName
	 *            The oeName to set.
	 */
	public void setOeName(String oeName) {
		this.oeName = oeName;
	}

	/**
	 * Guesses an energy from the current position of the GAP
	 */
	private void guessEnergy() {
		// The guess is based on the values of requestedHarmonic and
		// requestedPolarization so
		// guessEnergy must always come after guessHarmonic and
		// guessPolarization
		double[] values = lookuptable[requestedHarmonic.intValue() - 1].reverseCalculateValues(moveables[GAP]
				.getPosition().to(SI.MILLI(SI.METER)).getAmount(), requestedPolarization);
		currentEnergy = Quantity.valueOf(values[0], NonSI.ELECTRON_VOLT);
		requestedEnergy = currentEnergy;
	}

	/**
	 * Guesses current Polarization from the MUTUALPHASE position
	 */
	private void guessPolarization() {

		double value;
		double position = moveables[MUTUALPHASE].getPosition().to(SI.MILLI(SI.METER)).getAmount();

		if (Math.abs(position) < UndulatorPhasePositioner.ZERO_PHASE_TOLERANCE) {
			value = PolarizationValue.stringToDouble("Horizontal").doubleValue();
		} else if (position < -UndulatorPhasePositioner.ZERO_PHASE_TOLERANCE && position > -28.0) {
			value = PolarizationValue.stringToDouble("LeftCircular").doubleValue();
		} else if (position > UndulatorPhasePositioner.ZERO_PHASE_TOLERANCE && position < 28.0) {
			value = PolarizationValue.stringToDouble("RightCircular").doubleValue();
		} else {
			value = 90.0;
		}

		logger.debug("UndulatorMoveCalculator has guessed polarization is: " + PolarizationValue.doubleToString(value));
		// For startup purposes the requestedPolarization should be set to the
		// guessed value.
		requestedPolarization = Quantity.valueOf(value, NonSIext.DEG_ANGLE);

		currentPolarization = requestedPolarization;
	}

	/**
	 * Guesses a value for Harmonic.
	 */
	private void guessHarmonic() {
		// We always guess 1 for this
		requestedHarmonic = Quantity.valueOf(1.0, Unit.ONE);
		currentHarmonic = requestedHarmonic.intValue();
	}

	/**
	 * Returns the current energy. NB This method exists only so that UndulatorEnergyDOF can have access to the starting
	 * guess. Do not use it for any other purpose.
	 * 
	 * @return the current energy
	 */
	public Energy getEnergy() {
		if (!configured) {
			configure();
		}
		return currentEnergy;
	}

	/**
	 * Returns the current harmonic. NB This method exists only so that UndulatorHarmonicDOF can have access to the
	 * starting guess. Do not use it for any other purpose.
	 * 
	 * @return the current harmonic
	 */
	public Quantity getHarmonic() {
		if (!configured) {
			configure();
		}

		return Quantity.valueOf(currentHarmonic, Unit.ONE);
	}

	/**
	 * Returns the current polarization. NB This method exists only so that UndulatorPolarizationDOF can have access to
	 * the starting guess. Do not use it for any other purpose.
	 * 
	 * @return the current polarization
	 */
	public Angle getPolarization() {
		if (!configured) {
			configure();
		}
		return currentPolarization;
	}

	/**
	 * Refreshes the positions of the moveables
	 */

	public void refresh() {
		// If there has already been a move AND the gap and phase positions
		// do not correspond to that move then guess the values again and mark
		// the
		// position as invalid. (Because this indicates that the undulator has
		// moved by means other than OEMove.)
		boolean positionValid = true;

		if (positionsAfterLastMove != null) {
			for (int i = 0; i < moveables.length; i++) {
				if (!positionsAfterLastMove[i].equals(moveables[i].getPosition())) {
					positionValid = false;
					break;
				}
			}
		}

		if (positionValid == false) {
			logger.error("Undulator has moved !!!");
			// Guess again
			guessHarmonic();
			guessPolarization();
			guessEnergy();

			// Inform all the watchers (the DOFs in fact) of the new
			// positions.
			for (UndulatorMoveCalculatorWatcher w : watchers)
				w.inform(currentEnergy, requestedHarmonic, currentPolarization, false);
		}
	}

	/**
	 * Sets the harmonic value for the next move.
	 * 
	 * @param requestedHarmonic
	 *            the requested value for harmonic
	 */
	public void setRequestedHarmonic(Quantity requestedHarmonic) {
		this.requestedHarmonic = requestedHarmonic;
	}

	/**
	 * Sets the polarization value for the next move.
	 * 
	 * @param requestedPolarization
	 *            the requested value for polarization
	 */
	public void setRequestedPolarization(Angle requestedPolarization) {
		this.requestedPolarization = requestedPolarization;
	}

	/**
	 * Adds an UndulatorMoveCalculatorWatcher to the list.
	 * 
	 * @param umcw
	 *            the UndulatorMoveCalculatorWatcher to add
	 */
	public void addWatcher(UndulatorMoveCalculatorWatcher umcw) {
		watchers.add(umcw);
	}

	/**
	 * Sets the Energy for the next move.
	 * 
	 * @param requestedEnergy
	 *            the requested Energy
	 */
	public void setRequestedEnergy(Energy requestedEnergy) {
		this.requestedEnergy = requestedEnergy;
	}
}