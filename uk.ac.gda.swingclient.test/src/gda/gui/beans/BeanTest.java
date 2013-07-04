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

package gda.gui.beans;

import gda.device.DeviceException;
import gda.device.enumpositioner.DummyEnumPositioner;
import gda.device.scannable.DummyUnitsScannable;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.junit.Ignore;

/**
 *
 */
@Ignore("Not a JUnit test class")
public class BeanTest {

	/**
	 * 
	 */
	public BeanTest() {
	}
	
	private static void createAndShowGUI() throws DeviceException {

		DummyEnumPositioner enumPos1 = new DummyEnumPositioner();
		{
			enumPos1.setName("dummykjslfjlskjflsjkfl");
			enumPos1.addPosition("bad");
			enumPos1.addPosition("poor");
			enumPos1.addPosition("good");
			enumPos1.moveTo("good");
		}
		DummyUnitsScannable scanUnits1 = new DummyUnitsScannable("test", -10.3, "mm", "micron");

		JFrame frame = new JFrame("BeanTest");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));		
		for(int i=0; i< 10; i++)
		{
			SimpleBinaryBean bean = new SimpleBinaryBean();
			bean.setSimpleBinary(enumPos1);
			bean.setLabel(enumPos1.getName());
			bean.configure();
			panel.add(bean);
		}
		ScannableMotionUnitsBean bean1 = new ScannableMotionUnitsBean();
		bean1.setScannable(scanUnits1);
		bean1.setDisplayFormat("%5.1f");
		bean1.configure();
		panel.add(bean1);
		frame.add(panel);
		frame.pack();
		
		frame.setVisible(true);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					createAndShowGUI();
				}  catch (DeviceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

}
