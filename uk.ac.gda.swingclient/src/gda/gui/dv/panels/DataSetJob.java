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

import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import gda.gui.dv.ImageData;
import de.jreality.scene.Appearance;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;

/**
 * 
 *
 */

public class DataSetJob implements IJob {

	private Appearance ap;
	private Texture2D texture;
	private final DoubleDataset currentData;
	private final IMainPlotVisualiser visualiser;

	/**
	 * Constructor for a DataSetJob
	 * 
	 * @param app
	 *            appearance node
	 * @param tex
	 *            the result Texture
	 * @param data
	 *            the DataSet object that should be transformed into a Texture
	 * @param vis
	 *            visualiser to do the colour casting
	 */
	public DataSetJob(Appearance app, Texture2D tex, final DoubleDataset data, final IMainPlotVisualiser vis) {
		this.ap = app;
		this.texture = tex;
		this.currentData = data;
		this.visualiser = vis;

	}

	@Override
	public void executeJob() {
		ImageData colourTable = visualiser.cast(currentData);
		int width = colourTable.getW();
		int height = colourTable.getH();
		byte[] imageRGBAdata = new byte[width * height * 4];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int RGBAvalue = colourTable.get(x, y);
				byte red = (byte) ((RGBAvalue >> 16) & 0xff);
				byte green = (byte) ((RGBAvalue >> 8) & 0xff);
				byte blue = (byte) ((RGBAvalue) & 0xff);
				int index = x + y * width * 4;
				imageRGBAdata[index] = red;
				imageRGBAdata[index+1] = green;
				imageRGBAdata[index+2] = blue;
				imageRGBAdata[index+3] = ~0;
			}
		}
		de.jreality.shader.ImageData texImg = new de.jreality.shader.ImageData(imageRGBAdata, width, height);
		texture = TextureUtility.createTexture(ap, POLYGON_SHADER, texImg);
		texture.setRepeatS(Texture2D.GL_CLAMP_TO_EDGE);
		texture.setRepeatT(Texture2D.GL_CLAMP_TO_EDGE);
		texture.setMagFilter(Texture2D.GL_LINEAR_MIPMAP_LINEAR);
		texture.setMinFilter(Texture2D.GL_LINEAR_MIPMAP_LINEAR);
		texture.setMipmapMode(true);
		texImg = null;
		imageRGBAdata = null;
		System.gc();
	}
}
