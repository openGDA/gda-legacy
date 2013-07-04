/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import gda.factory.FactoryException;
import gda.gui.AcquisitionPanel;

import java.awt.FlowLayout;

import javax.swing.JPanel;

/**
 * Demo / testing program for gda.gui.beans objects. Needs to run on a beamline with a running GDA. Ensure jvm flags
 * used are the same as for a regular GDA Client program. 
 */
public class BeanPanel extends AcquisitionPanel {
	
	private String[] dofNames = new String[0];
	private String[] oeNames = new String[0];
	private String[] monitorNames = new String[0];
	private String[] pvNames = new String[0];
	private String[] pvScannableNames = new String[0];
	private String[] scannableMotionUnitsNames = new String[0];
	private String[] enumPositionerNames = new String[0];
	private String[] epicsPneumaticNames = new String[0];
	private String[] scannableNames = new String[0];
	
	
//	private DOFBean[] dofBeans = new DOFBean[0];
	private MonitorBean[] monitorBeans = new MonitorBean[0];
	private PVBean[] pvBeans = new PVBean[0];
	private PVScannableBean[] pvScannableBeans = new PVScannableBean[0];
	private ScannableMotionUnitsBean[] scannableMotionUnitsBeans = new ScannableMotionUnitsBean[0];
	private EnumPositionerComboBean[] enumPositionerBeans = new EnumPositionerComboBean[0];
	private EpicsPneumaticComboBean[] epicsPneumaticBeans = new EpicsPneumaticComboBean[0];
	
	private ScannableBean[] scannableBeans = new ScannableBean[0];
	/**
	 * Null constructor for Castor.
	 */
	public BeanPanel(){
	}
	
	@Override
	public void configure() throws FactoryException{
		super.configure();
		JPanel panel = new JPanel(new FlowLayout());
		
		
		//build the array of dofbeans based on the arrays of dof and oe names
//		if (dofNames.length == oeNames.length){
//			this.dofBeans = new DOFBean[dofNames.length];
//			for (int i = 0; i < dofNames.length; i++){
//				dofBeans[i] = new DOFBean();
//				dofBeans[i].setOeName(oeNames[i]);				
//				dofBeans[i].setDofName(dofNames[i]);
//			}
//		}
		
		//loop through all arrays of beans in turn and configure them...
//		for(DOFBean bean : dofBeans){
//			bean.startDisplay();
//			panel.add(bean);
//		}
		for(MonitorBean bean : monitorBeans){
			bean.startDisplay();
			panel.add(bean);
		}
		for(PVBean bean : pvBeans){
			bean.startDisplay();
			panel.add(bean);
		}
		for(PVScannableBean bean : pvScannableBeans){
			bean.startDisplay();
			panel.add(bean);
		}
		for(ScannableMotionUnitsBean bean : scannableMotionUnitsBeans){
			bean.startDisplay();
			panel.add(bean);
		}
		for(EnumPositionerComboBean bean : enumPositionerBeans){
			bean.configure();
			panel.add(bean);
		}
		for(EpicsPneumaticComboBean bean : epicsPneumaticBeans){
			bean.configure();
			panel.add(bean);
		}
		for(ScannableBean bean : scannableBeans){
			bean.startDisplay();
			panel.add(bean);
		}
		
		//then add all of them to the GUI
		this.add(panel);
	}


	
	/**
	 * @return Returns the oeNames.
	 */
	public String[] getOeNames() {
		return oeNames;
	}

	/**
	 * @param oeNames The oeNames to set.
	 */
	public void setOeNames(String[] oeNames) {
		this.oeNames = oeNames;
	}

	/**
	 * @return Returns the dofNames.
	 */
	public String[] getDofNames() {
		return dofNames;
	}

	/**
	 * @param dofNames The dofNames to set.
	 */
	public void setDofNames(String[] dofNames) {
		this.dofNames = dofNames;
	}

	/**
	 * @return Returns the monitorNames.
	 */
	public String[] getMonitorNames() {
		return monitorNames;
	}

	/**
	 * @param monitorNames The monitorNames to set.
	 */
	public void setMonitorNames(String[] monitorNames) {
		this.monitorNames = monitorNames;
		
		this.monitorBeans = new MonitorBean[monitorNames.length];
		for (int i = 0; i < monitorNames.length; i++){
			monitorBeans[i] = new MonitorBean();
			monitorBeans[i].setMonitorName(monitorNames[i]);
		}
	}

	/**
	 * @return Returns the pvNames.
	 */
	public String[] getPvNames() {
		return pvNames;
	}

	/**
	 * @param pvNames The pvNames to set.
	 */
	public void setPvNames(String[] pvNames) {
		this.pvNames = pvNames;
		
		this.pvBeans = new PVBean[pvNames.length];
		for (int i = 0; i < pvNames.length; i++){
			pvBeans[i] = new PVBean();
			pvBeans[i].setPv(pvNames[i]);
		}

	}

	/**
	 * @return Returns the pvScannableNames.
	 */
	public String[] getPvScannableNames() {
		return pvScannableNames;
	}

	/**
	 * @param pvScannableNames The pvScannableNames to set.
	 */
	public void setPvScannableNames(String[] pvScannableNames) {
		this.pvScannableNames = pvScannableNames;

		this.pvScannableBeans = new PVScannableBean[pvScannableNames.length];
		for (int i = 0; i < pvScannableNames.length; i++){
			pvScannableBeans[i] = new PVScannableBean();
			pvScannableBeans[i].setScannableName(pvScannableNames[i]);
		}

	}

	/**
	 * @return Returns the scannableMotionUnitsNames.
	 */
	public String[] getScannableMotionUnitsNames() {
		return scannableMotionUnitsNames;
	}

	/**
	 * @param scannableMotionUnitsNames The scannableMotionUnitsNames to set.
	 */
	public void setScannableMotionUnitsNames(String[] scannableMotionUnitsNames) {
		this.scannableMotionUnitsNames = scannableMotionUnitsNames;
		
		this.scannableMotionUnitsBeans = new ScannableMotionUnitsBean[scannableMotionUnitsNames.length];
		for (int i = 0; i < scannableMotionUnitsNames.length; i++){
			scannableMotionUnitsBeans[i] = new ScannableMotionUnitsBean();
			scannableMotionUnitsBeans[i].setScannableName(scannableMotionUnitsNames[i]);
		}
	}

	/**
	 * @return Returns the enumPositionerNames.
	 */
	public String[] getEnumPositionerNames() {
		return enumPositionerNames;
	}

	/**
	 * @param enumPositionerNames The enumPositionerNames to set.
	 */
	public void setEnumPositionerNames(String[] enumPositionerNames) {
		this.enumPositionerNames = enumPositionerNames;

		this.enumPositionerBeans = new EnumPositionerComboBean[enumPositionerNames.length];
		for (int i = 0; i < enumPositionerNames.length; i++){
			enumPositionerBeans[i] = new EnumPositionerComboBean();
			enumPositionerBeans[i].setEnumPositionerName(enumPositionerNames[i]);
		}
	}

	/**
	 * @return Returns the epicsPneumaticNames.
	 */
	public String[] getEpicsPneumaticNames() {
		return epicsPneumaticNames;
	}

	/**
	 * @param epicsPneumaticNames The epicsPneumaticNames to set.
	 */
	public void setEpicsPneumaticNames(String[] epicsPneumaticNames) {
		this.epicsPneumaticNames = epicsPneumaticNames;
		
		this.epicsPneumaticBeans = new EpicsPneumaticComboBean[epicsPneumaticNames.length];
		for (int i = 0; i < epicsPneumaticNames.length; i++){
			epicsPneumaticBeans[i] = new EpicsPneumaticComboBean();
			epicsPneumaticBeans[i].setEnumPositionerName(epicsPneumaticNames[i]);
		}
	}
	
	/**
	 * @return Returns the epicsPneumaticNames.
	 */
	public String[] getScannableNames() {
		return scannableNames;
	}

	/**
	 * Sets the scannable names.
	 * 
	 * @param scannableNames the scannable names
	 */
	public void setScannableNames(String[] scannableNames) {
		this.scannableNames = scannableNames;
		
		this.scannableBeans = new ScannableBean[scannableNames.length];
		for (int i = 0; i < scannableNames.length; i++){
			scannableBeans[i] = new ScannableBean();
			scannableBeans[i].setScannableName(scannableNames[i]);
		}
	}	
}
