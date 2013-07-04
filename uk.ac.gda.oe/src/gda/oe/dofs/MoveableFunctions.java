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

/**
 * MoveableFunctions Class
 */
public class MoveableFunctions {

	private String moveableName;

	private String index = null;

	private String function = null;

	private boolean functionAvailable = false;

	/**
	 * @return function
	 */
	public String getFunction() {
		return function;
	}

	/**
	 * @param function
	 */
	public void setFunction(String function) {
		this.function = function;
	}

	/**
	 * @return index
	 */
	public String getIndex() {
		return index;
	}

	/**
	 * @param index
	 */
	public void setIndex(String index) {
		this.index = index;
	}

	/**
	 * @return moveableName
	 */
	public String getMoveableName() {
		return moveableName;
	}

	/**
	 * @param moveableName
	 */
	public void setMoveableName(String moveableName) {
		this.moveableName = moveableName;
	}

	/**
	 * @return boolean
	 */
	public boolean isFunctionAvailable() {
		return functionAvailable;
	}
}
