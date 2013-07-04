/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.gui.dv.panels;

import java.util.LinkedList;

import de.jreality.math.MatrixBuilder;
import de.jreality.scene.SceneGraphComponent;

/**
 * Card Stack Movement
 */

public class StackMovement extends GeometryMovement {

	/**
	 * @param texFrame
	 * @param stackBuffer
	 * @param stackPositions
	 * @param slides
	 * @param numberOfDataSets
	 */
	public StackMovement(DataSetImages texFrame, LinkedList<Integer> stackBuffer, int[] stackPositions,
			SceneGraphComponent[] slides, int numberOfDataSets) {
		super(texFrame, stackBuffer, stackPositions, slides, numberOfDataSets);
	}

	@Override
	protected void moveGeometryForward() {
		for (int i = 0; i < numSlides; i++) {
			stackPos[i]--;
			if (stackPos[i] < 0)
				stackPos[i] = numSlides - 1;
			MatrixBuilder.euclidean().rotateY(-0.25 * Math.PI).translate(-5.0, 0, -3.0 - stackPos[i] * 1.5).assignTo(
					slides[i]);
		}
	}

	@Override
	protected void moveGeometryBackward() {
		for (int i = 0; i < numSlides; i++) {
			stackPos[i] = (stackPos[i] + 1) % numSlides;
			MatrixBuilder.euclidean().rotateY(-0.25 * Math.PI).translate(-5.0, 0, -3.0 - stackPos[i] * 1.5).assignTo(
					slides[i]);
		}
	}
}
