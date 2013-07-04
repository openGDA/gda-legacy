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

package gda.gui;

import static org.junit.Assert.assertFalse;
import gda.configuration.properties.LocalProperties;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.GdaMetadata;
import gda.data.metadata.IcatMetadataEntry;
import gda.data.metadata.IcatProviderTest;
import gda.data.metadata.MetadataEntry;
import gda.data.metadata.StoredMetadataEntry;
import gda.data.metadata.icat.Icat;
import gda.data.metadata.icat.IcatProvider;
import gda.jython.authenticator.UserAuthentication;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.SwingUtilities;

import org.junit.Before;
import org.junit.Test;

/**
 * This unit test should not form part of the unit test suite as it is for testing the GUI
 */
public class ChooseVisitIDFramePluginTest {

	private GdaMetadata metadata;

	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		metadata = (GdaMetadata) GDAMetadataProvider.getInstance();
		LocalProperties.set(Icat.URL_PROP,IcatProviderTest.class.getResource("testicat.xml").getFile());
		LocalProperties.set(Icat.SHIFT_TOL_PROP,"1440");
		LocalProperties.set(Icat.ICAT_TYPE_PROP,gda.data.metadata.icat.XMLIcat.class.getName());
		MetadataEntry visit = new IcatMetadataEntry("visit", "lower(visit_id)visit_id:investigation:id");
		visit.configure();
		metadata.addMetadataEntry(visit);
		MetadataEntry prop = new IcatMetadataEntry("proposal", "INV_NUMBER:investigation:id");
		prop.configure();
		metadata.addMetadataEntry(prop);
		MetadataEntry title = new IcatMetadataEntry("title", "TITLE:investigation:id");
		title.configure();
		metadata.addMetadataEntry(title);
		MetadataEntry inst = new StoredMetadataEntry("instrument", "i24");
		inst.configure();
		metadata.addMetadataEntry(inst);
		MetadataEntry fedid = new StoredMetadataEntry("federalid", "mzp47");
		fedid.configure();
		metadata.addMetadataEntry(fedid);
		MetadataEntry user = new StoredMetadataEntry("userid", "mzp47");
		user.configure();
		metadata.addMetadataEntry(user);

		System.setProperty("user.name", "mzp47");
		UserAuthentication.setToUseOSAuthentication();
	}

	/**
	 * @throws Exception 
	 * 
	 */
	@Test
	public void testChooseVisit() throws Exception {
		String test = "";
		try {
			// set "now" to be 11:00 16-02-10 where there is a duplicate in the test file
			DateFormat formatter = new SimpleDateFormat("HH:mm dd-MM-yy");
			Date date = formatter.parse("11:00 16-02-10");
			IcatProvider.getInstance().setOperatingDate(date);
			test = IcatProvider.getInstance().getMyInformation("visitID", "def456", null);
		} catch (Exception e1) {
		}

		if (test.contains(",")) {
			// show a popup to resolve the issue
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						ChooseVisitIDFrame.getInstance().setVisible(true);
					}
				});
			} catch (InterruptedException e) {
			} catch (InvocationTargetException e) {
			}

			while (ChooseVisitIDFrame.getInstance().isVisible()) {
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
				}
			}

			// this would be done by the BatonManager whenever the baton changes hands
			IcatProvider.getInstance().setMyVisit(ChooseVisitIDFrame.getChosenVisitID());

			// test that we have a single value in the icat information which matches the selected value
			String visitValue = metadata.getMetadataValue("visit");
			assertFalse(visitValue.contains(","));
			System.out.println(visitValue);

		}
	}

}
