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

import gda.factory.Configurable;
import gda.factory.Findable;
import gda.oe.dofs.DOF;
import gda.util.QuantityFactory;

import java.awt.geom.Rectangle2D.Double;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a map of the allowed and forbidden positions of a pair of DOFs. The information about areas to
 * avoid would probably eventually be specified in a file but this initial implementation gets a forbidden zone from the
 * XML file. It keeps a record of the current position on the map by observing the DOFs. This initial implementation
 * provides a method which approves or disapproves of a move by checking whether it intersects or ends in the forbidden
 * zone (which is just a rectangle at the moment). NB Currently there is only one forbiddenZone but it should be easy to
 * extend to a list of zones.
 */
public class DOFRouteChecker implements Configurable, Findable {
	private static final Logger logger = LoggerFactory.getLogger(DOFRouteChecker.class);

	private String name;

	private ForbiddenZone forbiddenZone;

	private DOF xMover;

	private DOF yMover;

	private double currentX;

	private double currentY;

	/**
	 * Constructor
	 */
	public DOFRouteChecker() {
	}

	@Override
	public void configure() {
	}

	/**
	 * Specifies the DOF whose position corresponds to the x value in the map
	 * 
	 * @param xMover
	 *            the DOF
	 */
	public void setXMover(DOF xMover) {
		this.xMover = xMover;
		currentX = xMover.getPosition().to(QuantityFactory.createUnitFromString("mm")).getAmount();
	}

	/**
	 * Returns the DOF whose position corresponds to the x value in the map
	 * 
	 * @return the x DOF
	 */
	public DOF getXMover() {
		return xMover;
	}

	/**
	 * Specifies the DOF whose position corresponds to the y value in the map
	 * 
	 * @param yMover
	 *            the DOF
	 */
	public void setYMover(DOF yMover) {
		this.yMover = yMover;
		currentY = yMover.getPosition().to(QuantityFactory.createUnitFromString("mm")).getAmount();
	}

	/**
	 * Returns the DOF whose position corresponds to the x value in the map
	 * 
	 * @return the x DOF
	 */
	public DOF getYMover() {
		return yMover;
	}

	/**
	 * Sets the forbiddenZone
	 * 
	 * @param forbiddenZone
	 *            String specifying forbiddenZone
	 */
	public void setForbiddenZone(String forbiddenZone) {
		this.forbiddenZone = new ForbiddenZone(forbiddenZone);
	}

	/**
	 * Returns the forbiddenZone (as a String)
	 * 
	 * @return String representing the forbiddenZone
	 */
	public String getForbiddenZone() {
		if (forbiddenZone != null) {
			return forbiddenZone.toString();
		}
		return null;
	}

	/**
	 * NB this inner class extends java.awt.Rectangle2D.Double so we can use the methods of Rectangle2D to determine
	 * intersection and containment. The ForbiddenZone could be a more general close polygon and work in the same way.
	 */
	public class ForbiddenZone extends Double {
		/**
		 * This class only exists because the normal rectangle specification specifying one corner and a width and
		 * height is not the most useful for this type of application.
		 * 
		 * @param minX
		 *            the minimum X value of the zone
		 * @param maxX
		 *            the maximum X value of the zone
		 * @param minY
		 *            the minimum Y value of the zone
		 * @param maxY
		 *            the maximum Y value of the zone
		 */
		public ForbiddenZone(double minX, double maxX, double minY, double maxY) {
			super(minX, minY, maxX - minX, maxY - minY);
		}

		/**
		 * Constructs a ForbiddenZone from a String
		 * 
		 * @param stringRepresentation
		 *            the String
		 */
		public ForbiddenZone(String stringRepresentation) {
			logger.debug("Constructing ForbiddenZone from string " + stringRepresentation);
			// The String must be of the form "minX, maxX, minY, maxY"
			StringTokenizer strtok = new StringTokenizer(stringRepresentation, ",");
			double minX = java.lang.Double.valueOf(strtok.nextToken()).doubleValue();
			double maxX = java.lang.Double.valueOf(strtok.nextToken()).doubleValue();
			double minY = java.lang.Double.valueOf(strtok.nextToken()).doubleValue();
			double maxY = java.lang.Double.valueOf(strtok.nextToken()).doubleValue();
			setRect(minX, minY, maxX - minX, maxY - minY);
		}

		/**
		 * Converts a ForbiddenZone into a String representation
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			double maxX = getMinX() + getWidth();
			double maxY = getMinY() + getHeight();
			return "" + getMinX() + "," + maxX + "," + getMinY() + "," + maxY;
		}
	}

	/**
	 * Calculates allowed moves to get to the specified final position. If the final position is in the forbidden zone
	 * returns null. If getting to the final position crosses the forbidden zone then returns a set of moves to go round
	 * it. If neither of these problems then returns a single move direct to the final position.
	 * 
	 * @param finalX
	 * @param finalY
	 * @return null if move ends in forbidden zone OR single move if move does not cross zone OR set of moves around the
	 *         forbidden zone NB The last option is currently disabled, moves across the forbidden zone are also
	 *         disallowed.
	 */
	public double[][] isAllowedMove(double finalX, double finalY) {
		double[][] moves = null;
		double x;
		double y;
		double w;
		double h;

		logger.debug("DOFRouteChecker forbiddenZone is: " + forbiddenZone);
		logger.debug("DOFRouteChecker isAllowedMove called for " + finalX + " " + finalY);

		if (forbiddenZone.contains(finalX, finalY)) {
			logger.debug("which is in the forbiddenZone");
			// moves will be null
		} else {
			currentX = xMover.getPosition().to(QuantityFactory.createUnitFromString("mm")).getAmount();
			currentY = yMover.getPosition().to(QuantityFactory.createUnitFromString("mm")).getAmount();
			// Need to construct a rectangle which represents the area of
			// the map
			// the move might cross. Need a rectangle not just a line
			// because we
			// cannot guarantee the relative speeds of the movements.

			logger.debug("DOFRouteChecker x,y final x,y" + currentX + " " + currentY + " " + finalX + " " + finalY + 3);
			// Intersection seems not to work if the width or height of the
			// moveRectangle is 0.0 so arbitrarily specifiy minimum of 0.1
			// for
			// these.
			x = Math.min(currentX, finalX);
			w = Math.abs(finalX - currentX);

			if (w == 0.0)
				w = 0.1;

			y = Math.min(currentY, finalY);
			h = Math.abs(finalY - currentY);
			if (h == 0.0)
				h = 0.1;

			Double moveRectangle = new Double(x, y, w, h);
			logger.debug("DOFRouteChecker moveRectangle is: " + moveRectangle);
			if (forbiddenZone.intersects(moveRectangle)) {
				logger.debug("which crosses the forbidden zone");
				/*
				 * Message.out("should make moves: to (" + currentX + "," + forbiddenZone.getMaxY() + ")");
				 * Message.out(" to (" + finalX + "," + forbiddenZone.getMaxY() + ")"); Message.out(" to (" + finalX +
				 * "," + finalY + ")"); Message.out("but temporarily the move is just forbidden");
				 */

				moves = new double[3][];
				moves[0] = new double[2];
				moves[1] = new double[2];
				moves[2] = new double[2];
				moves[0][0] = currentX;
				moves[0][1] = forbiddenZone.getMaxY();
				moves[1][0] = finalX;
				moves[1][1] = forbiddenZone.getMaxY();
				moves[2][0] = finalX;
				moves[2][1] = finalY;
			} else {
				// Construct a single move using the final values
				moves = new double[1][];
				moves[0] = new double[2];
				moves[0][0] = finalX;
				moves[0][1] = finalY;
			}
		}

		logger.debug("DOFRouteChecker isAllowedMove returning:");
		if (moves == null) {
			logger.debug("      null");
		} else {
			for (int i = 0; i < moves.length; i++) {
				logger.debug(" " + moves[i][0] + "," + moves[i][1]);
			}
		}

		return moves;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

}