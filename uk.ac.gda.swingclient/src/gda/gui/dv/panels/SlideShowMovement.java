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
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;

/**
 * Movement in stack handler for SlideShow mode
 */
public class SlideShowMovement implements IMovement {

	private DataSetImages texFrame;
	private LinkedList<Integer> stackBuffer;
	private int stackPos[];
	private Appearance[] apps;
	private IPlayBack playback;
	private int numSlides;
	private int numberOfDataSets;
	private SceneGraphComponent[] slides;

	/**
	 * Constructor for SlideShowMovement
	 * 
	 * @param texFrame
	 *            DataSetImages this Movement is attached to
	 * @param stackBuffer
	 * @param stackPositions
	 * @param apps
	 * @param slides
	 * @param playback
	 * @param numberOfDataSets
	 */
	public SlideShowMovement(DataSetImages texFrame, LinkedList<Integer> stackBuffer, int[] stackPositions,
			Appearance[] apps, SceneGraphComponent[] slides, IPlayBack playback, int numberOfDataSets) {
		this.texFrame = texFrame;
		this.stackBuffer = stackBuffer;
		this.stackPos = stackPositions;
		this.apps = apps;
		this.playback = playback;
		this.slides = slides;
		numSlides = slides.length;
		this.numberOfDataSets = numberOfDataSets;
	}

	@Override
	public int moveBackward(int bufferPos) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int moveForward(int bufferPos) {
		int numOfFrames = (playback.getFrameNr() / 100);

		if (numOfFrames > 2 && numberOfDataSets > (numSlides + bufferPos))

		{
			bufferPos++;
			for (int i = 0; i < numSlides; i++) {
				stackPos[i]--;
				if (stackPos[i] < 0)
					stackPos[i] = numSlides - 1;
				MatrixBuilder.euclidean().translate(0, 0, -stackPos[i] * DataSetImages.GAPINSTACK).assignTo(slides[i]);
			}
			int previousTopFrame = stackBuffer.remove();
			stackBuffer.addLast(previousTopFrame);
			apps[previousTopFrame].setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.TRANSPARENCY,
					0.0);
			texFrame.updateStack(previousTopFrame, (numSlides - 1 + bufferPos), true);
		}
		return bufferPos;
	}

}
