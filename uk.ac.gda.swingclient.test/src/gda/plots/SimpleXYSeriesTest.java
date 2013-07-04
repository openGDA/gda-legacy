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

package gda.plots;

import java.io.IOException;
import java.util.Vector;

import org.junit.Test;


/**
 *
 */
public class SimpleXYSeriesTest {

	/**
	 * 
	 */
	@Test
	public void testArchive() {
		@SuppressWarnings("unused")
		double [] xVals1 = new double[1];
		@SuppressWarnings("unused")
		double [] yVals1 = new double[1];
		double [] xVals1000 = new double[1000];
		double [] yVals1000 = new double[1000];
		gda.configuration.properties.LocalProperties.set(SimpleXYSeries.GDA_PLOT_SIMPLEXYSERIES_ARCHIVE_THRESHOLD, "0");
		// 100 series with 1000 points gives 3.4MB archived and 6.4MB unarchived = 200,000 doubles and 100,000 XYDataItem, 16bytes for each
		// 100 series with 1 point gives 1.2MB archived and 1.2MB unarchived		, each SimpleXYSeries is 1KB
		// so it is worth archiving when data > 1KB ~50 points.
		Vector<SimpleXYSeries> series = new Vector<SimpleXYSeries>();
		for( int i =0; i< 100; i++){
			series.add(new SimpleXYSeries("Test",1, SimplePlot.LEFTYAXIS, xVals1000, yVals1000));
		}
		for(SimpleXYSeries sxys : series){
			try {
				sxys.archive();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		/* I have no idea what this is for - all it does is make the test hang forever
		while(true){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		*/
	}

}
