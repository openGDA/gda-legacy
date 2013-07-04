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

import de.jreality.scene.SceneGraphComponent;


/**
 * Super class for all geometry based stack movements
 *
 */

public class GeometryMovement implements IMovement {

	private DataSetImages texFrame;
	private  LinkedList<Integer> stackBuffer;
	protected int stackPos[];
	protected int numSlides;
	private int numDataSets;
	protected SceneGraphComponent[] slides;

	
	/**
	 * GeometryMovement constructor
	 * @param texFrame reference back to the DataSetImages panel
	 * @param stackBuffer 
	 * @param stackPositions
	 * @param slides
	 * @param numberOfDataSets
	 */
	public GeometryMovement(DataSetImages texFrame,
		      				LinkedList<Integer> stackBuffer,
		      				int [] stackPositions,
		      				SceneGraphComponent[] slides,
		      				int numberOfDataSets)
	{
		this.texFrame = texFrame;
		this.stackBuffer = stackBuffer;
		this.stackPos = stackPositions;
		this.slides = slides;
		numSlides = slides.length;
		this.numDataSets = numberOfDataSets;
	}
	
	@Override
	public int moveBackward(int bufferPos) {
		if (bufferPos > 0)
		{
			bufferPos--;
			moveGeometryBackward();
			int nextTopFrame = stackBuffer.removeLast();
			stackBuffer.addFirst(nextTopFrame);
			texFrame.updateStack(nextTopFrame,bufferPos,false);
		}
		return bufferPos;
	}

	@Override
	public int moveForward(int bufferPos) {
		bufferPos++;
		moveGeometryForward();
		int previousTopFrame = stackBuffer.remove();
		stackBuffer.addLast(previousTopFrame);
		texFrame.updateStack(previousTopFrame,(numSlides-1+bufferPos)%numDataSets, true);
		return bufferPos;
	}

	protected void moveGeometryForward()
	{
	}
	
	protected void moveGeometryBackward()
	{
		
	}	
}

	