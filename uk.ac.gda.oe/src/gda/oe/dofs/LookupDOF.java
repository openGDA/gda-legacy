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

package gda.oe.dofs;

import gda.configuration.properties.LocalProperties;
import gda.factory.FactoryException;
import gda.function.InterpolationFunction;
import gda.gui.oemove.DOFType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.nfunk.jep.JEP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LookupDOF Class
 */
public class LookupDOF extends DOF implements Serializable {
	private static final Logger logger = LoggerFactory.getLogger(LookupDOF.class);

	private HashMap<String, Object[]> lookupValues;

	/**
	 * 
	 */
	public static final int LOOKUP = 1;

	/**
	 * 
	 */
	public static final int FUNCTION = 2;

	/**
	 * 
	 */
	public static final int DEFAULT = 3;

	private int mode = LOOKUP;

	private String lookupIndices = "ALL";

	private String functionIndices = "ALL";

	private String defaultIndices = "ALL";

	private int[] lookupMoveables;

	private int[] defaultMoveables;

	private InterpolationFunction[] interpolate;

	private HashMap<String, MoveableFunctions> moveableFunctions = new HashMap<String, MoveableFunctions>();

	/**
	 * Constructor.
	 */
	public LookupDOF() {
		dofType = DOFType.LookupDOF;
	}

	@Override
	public void configure() throws FactoryException {

		super.configure();
		switch (getMode()) {
		case LOOKUP:
			loadLookupTable();
			break;
		// not used in current implemention
		case FUNCTION:
			loadFunction();
			break;
		default:
			// donothing
			break;
		}

		updatePosition();
		setPositionValid(true);
		updateStatus();

	}

	private void loadFunction() {
		// not used
	}

	@SuppressWarnings("unchecked")
	// method to load the lookup values from file to a HashMap, with the dof
	// name
	// as
	// key and an array of double values(read from lookup file) as value
	// The first column in the lookup file is the lookupdof and all the
	// other dof
	// are associated with the lookupdof through interpolation function
	private void loadLookupTable() {
		if (lookupValues == null)
			lookupValues = new HashMap<String, Object[]>();
		String lookupDir = LocalProperties.get("gda.lookup.directory");
		if (null == lookupDir)
			lookupDir = ".";
		try {
			String filename = lookupDir + File.separator + this.getName();
			// change to read the files using LookupFileReader
			logger.debug("the file name is " + filename);
			BufferedReader reader = new BufferedReader(new FileReader(filename));

			String line = reader.readLine();
			StringTokenizer stringTokens = new StringTokenizer(line, " ");
			String[] keys = new String[stringTokens.countTokens()];
			Vector<Double>[] values = new Vector[stringTokens.countTokens()];
			// now read the units line
			line = reader.readLine();
			StringTokenizer units = new StringTokenizer(line, " ");
			String[] unitsArray = new String[units.countTokens()];
			for (int i = 0; i < keys.length; i++) {
				keys[i] = stringTokens.nextToken();
				values[i] = new Vector<Double>();
				unitsArray[i] = units.nextToken();
			}
			// now read the units line

			while ((line = reader.readLine()) != null) {

				logger.debug("the line is  " + line);
				StringTokenizer tokens = new StringTokenizer(line, " ");
				int i = 0;
				while (tokens.hasMoreTokens()) {
					values[i].add(Double.parseDouble(tokens.nextToken()));
					i++;
				}

			}
			for (int j = 0; j < keys.length; j++) {
				lookupValues.put(keys[j], values[j].toArray());
			}
			reader.close();

			if (!lookupIndices.equals("ALL")) {
				StringTokenizer indTokens = new StringTokenizer(lookupIndices);
				lookupMoveables = new int[indTokens.countTokens()];
				for (int i = 0; i < lookupMoveables.length; i++) {
					lookupMoveables[i] = Integer.parseInt(indTokens.nextToken());
				}
			} else {
				lookupMoveables = new int[moveables.length];
				for (int i = 0; i < lookupMoveables.length; i++) {
					lookupMoveables[i] = i;
				}

			}
			interpolate = new InterpolationFunction[lookupMoveables.length];
			Object[] x = lookupValues.get(keys[0]);
			double xvalue[] = new double[x.length];
			for (int i = 0; i < xvalue.length; i++) {
				xvalue[i] = ((Double) x[i]).doubleValue();
			}
			for (int k = 0; k < interpolate.length; k++) {
				Object[] y = lookupValues.get(keys[k + 1]);
				double yvalue[] = new double[y.length];
				for (int i = 0; i < yvalue.length; i++) {
					yvalue[i] = ((Double) y[i]).doubleValue();
				}

				interpolate[k] = new InterpolationFunction(xvalue, yvalue, Unit.valueOf(unitsArray[0]), Unit
						.valueOf(unitsArray[k + 1]), 3);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void setDefaultAcceptableUnits() {
		defaultAcceptableUnits = moveables[0].getAcceptableUnits();

	}

	@Override
	protected void setValidAcceptableUnits() {
		validAcceptableUnits = new ArrayList<Unit<? extends Quantity>>();

	}

	@Override
	protected Quantity[] calculateMoveables(Quantity q) {

		logger.debug("inside lookupDODF calculate MOVEabels " + q);
		Quantity rtn[] = new Quantity[moveables.length];
		switch (getMode())
		// not used in current implemention
		{
		case LOOKUP:// not used in current implemention
		{
			double pos = q.getAmount();
			// search for the pos value in the lookup table
			// if pos is not available find the nearest min and max values
			// for
			// pos , then find the interpolation constant alpha = (pos - pos
			// min)/ (posmax - posmin)
			// to clalculate the position of moveables moveablepos =
			// moveableposmin + alpha *(moveableposmax - moveableposmin)

			logger.debug("the value is " + q + " " + pos);
			// find the functionIndex of the main Moveable
			String mainIndex = moveableFunctions.get(moveables[0].getName()).getIndex();

			for (int i = 0; i < moveables.length; i++) {
				if ((i + 1) == lookupMoveables[i]) {
					String name = moveables[i].getName();
					logger.debug("the name of moveable " + name);
					String func = getFuntionDesc(name);
					// System.out.println("the function description is " +
					// func);
					// for each of the dof first check if there is a
					// function
					// associated
					// with it, if it is use the function to calculation the
					// position
					// instead of the lookup file
					if (func != null) {
						String var = findVariable(func);
						int len = func.length();
						JEP myParser = new JEP();
						myParser.addStandardFunctions();
						myParser.addStandardConstants();
						double posi = Double.NaN;
						// if the variable name in the function description is
						// the
						// same as the
						// main dof index then use the moveto position to
						// calculate
						// function value, rather than
						// the current position.
						if (mainIndex.equals(var))
							posi = pos;
						else
							posi = getPositionfromIndex(var).getAmount();
						// logger.debug("after finding position of var "+
						// posi);
						myParser.addVariable(var, posi);
						myParser.parseExpression(func.substring(1, len - 1));
						// logger.debug("after parsing expression " +
						// func.substring(1, len - 1) );
						double result = myParser.getValue();
						// logger.debug("the result is " + result);
						rtn[i] = Quantity.valueOf(result, moveables[i].getAcceptableUnits().get(0));
						// logger.debug("the ret value is " + rtn[i]);
						continue;
					}
					// if there is no function associated with a dof then
					// use the
					// lookup
					// file and interpolation function to calculate position
					rtn[i] = interpolate[i].evaluate(q);
				} else
					rtn[i] = moveables[i].getPosition(); // Length.valueOf(moveables[i].getvalues[i],
				// ((DOF)moveables[i]).getReportingUnits());

				logger.debug("the looup index is  " + lookupMoveables[i] + " " + rtn[i]);

			}
			break;
		}
		case FUNCTION:// not used in current implemention
		{
			// do something relevant to function;'
			break;
		}
		default:// not used in current implemention
		{
			for (int i = 0; i < moveables.length; i++) {
				if (i == defaultMoveables[i])
					rtn[i] = q;
				else
					rtn[i] = moveables[i].getPosition();
			}
		}

		}

		return (rtn);
	}

	// method to find if a function index is present in the given function
	private String findVariable(String func) {
		char[] arr = func.toCharArray();
		for (int i = 0; i < arr.length; i++)
			if (arr[i] >= 'a' && arr[i] <= 'z')
				return String.valueOf(arr[i]);
		return null;
	}

	// return the function associated with a particular moveable name
	private String getFuntionDesc(String name) {
		if (moveableFunctions.containsKey(name)) {
			String desc = (moveableFunctions.get(name)).getFunction();
			return desc;
		}
		return null;
	}

	@Override
	protected Quantity checkTarget(Quantity newQuantity) {// needs to
		// change
		return ((DOF) moveables[0]).checkTarget(newQuantity);
	}

	@Override
	protected void updatePosition() {
		// needs to change
		Quantity q = moveables[0].getPosition();

		setCurrentQuantity(q);

	}

	@Override
	public Quantity getSoftLimitLower() {
		// needs to change
		// TODO Auto-generated method stub
		return moveables[0].getSoftLimitLower();
	}

	@Override
	public Quantity getSoftLimitUpper() {
		// needs to change
		// TODO Auto-generated method stub
		return moveables[0].getSoftLimitUpper();
	}

	/**
	 * @return mode
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * @param mode
	 */
	public void setMode(int mode) {
		this.mode = mode;
	}

	/**
	 * @return defaultIndices
	 */
	public String getDefaultIndices() {
		return defaultIndices;
	}

	/**
	 * @param defaultIndices
	 */
	public void setDefaultIndices(String defaultIndices) {
		this.defaultIndices = defaultIndices;
	}

	/**
	 * @return functionIndices
	 */
	public String getFunctionIndices() {
		return functionIndices;
	}

	/**
	 * @param functionIndices
	 */
	public void setFunctionIndices(String functionIndices) {
		this.functionIndices = functionIndices;
	}

	/**
	 * @return lookupIndices
	 */
	public String getLookupIndices() {
		return lookupIndices;
	}

	/**
	 * @param lookupIndices
	 */
	public void setLookupIndices(String lookupIndices) {
		this.lookupIndices = lookupIndices;
	}

	/**
	 * getter method for the functionIndex.
	 * 
	 * @return Returns a list of the function associated with each of the moveable.
	 */
	public ArrayList<String> getFunctionIndex() {
		if (moveableFunctions != null && !moveableFunctions.isEmpty()) {
			ArrayList<String> arr = new ArrayList<String>();
			Object[] c = moveableFunctions.values().toArray();
			for (int i = 0; i < c.length; i++)
				arr.add(((MoveableFunctions) c[i]).getIndex());
			return arr;
		}
		return null;
	}

	/**
	 * setter method function index. Adds MoveableFunction object to Hashmap for each function Index with the moveable
	 * name as the key.
	 * 
	 * @param functionIndex
	 */
	public void addFunctionIndex(String functionIndex) {
		int i = functionIndex.lastIndexOf(".");
		String temp = functionIndex.substring(0, i);
		int dofIndex = temp.lastIndexOf(".");
		String moveableName = temp.substring(dofIndex + 1);
		// logger.debug("ADDING FUNC INDEX FOR MOVEABLE NAME " + moveableName
		// );
		String index = functionIndex.substring(i + 1);
		// logger.debug("ADDING FUNC iNDEX FOR MOVEABLE NAME " + moveableName +
		// "
		// desc " + index);
		if (moveableFunctions.containsKey(moveableName)) {
			MoveableFunctions mvf = moveableFunctions.get(moveableName);
			mvf.setIndex(index);
			moveableFunctions.remove(moveableName);
			moveableFunctions.put(moveableName, mvf);
		} else {
			MoveableFunctions mvf = new MoveableFunctions();
			mvf.setMoveableName(moveableName);
			mvf.setIndex(index);
			moveableFunctions.put(moveableName, mvf);
		}
	}

	/**
	 * @return Returns a list of functions if any associated with each moveable
	 */
	public ArrayList<String> getFunctionDesc() {
		if (moveableFunctions != null && !moveableFunctions.isEmpty()) {
			ArrayList<String> arr = new ArrayList<String>();
			Object[] c = moveableFunctions.values().toArray();
			for (int i = 0; i < c.length; i++)
				arr.add(((MoveableFunctions) c[i]).getFunction());
			return arr;
		}
		return null;
	}

	/**
	 * setter method for function description for each moveable.
	 * 
	 * @param functionDesc
	 */
	public void addFunctionDesc(String functionDesc) {
		int i = functionDesc.indexOf("(");
		String temp = functionDesc.substring(0, i - 1);
		int dofIndex = temp.lastIndexOf(".");
		String moveableName = temp.substring(dofIndex + 1);
		// String moveableName = functionDesc.substring(0,i - 1);
		// logger.debug("ADDING FUNC DESC FOR MOVEABLE NAME " + moveableName );
		String desc = functionDesc.substring(i);
		// logger.debug("ADDING FUNC DESC FOR MOVEABLE NAME " + moveableName +
		// "
		// desc " + desc);
		if (moveableFunctions.containsKey(moveableName)) {
			MoveableFunctions mvf = moveableFunctions.get(moveableName);
			mvf.setMoveableName(moveableName);
			mvf.setFunction(desc);
			moveableFunctions.remove(moveableName);
			moveableFunctions.put(moveableName, mvf);
		} else {
			MoveableFunctions mvf = new MoveableFunctions();
			mvf.setMoveableName(moveableName);
			mvf.setFunction(desc);
			moveableFunctions.put(moveableName, mvf);
		}
	}

	private Quantity getPositionfromIndex(String Index) {
		if (moveableFunctions != null && !moveableFunctions.isEmpty()) {
			// ArrayList<String> arr = new ArrayList<String>();
			Object[] c = moveableFunctions.values().toArray();
			for (int i = 0; i < c.length; i++)
				if (((MoveableFunctions) c[i]).getIndex().equals(Index)) {
					String name = ((MoveableFunctions) c[i]).getMoveableName();
					for (int j = 0; j < moveables.length; j++)
						if (moveables[j].getName().equals(name))
							return moveables[j].getPosition();
				}
		}
		return null;
	}

}
