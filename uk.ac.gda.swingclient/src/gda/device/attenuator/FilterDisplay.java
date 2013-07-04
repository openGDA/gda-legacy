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

package gda.device.attenuator;
import gda.device.Attenuator;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 */
public class FilterDisplay extends JPanel {
	private JLabel lblName;
	private JLabel lblCalculated;
	private JLabel lblActual;
	private Attenuator attenuator;
	private int position;
	
	/**
	 * 
	 */
	public FilterDisplay(){
		initGUI();
	}

	
	private void initGUI() {
		try {
			{
				lblName = new JLabel();
				this.add(lblName);
				lblName.setText("Name");
			}
			{
				lblCalculated = new JLabel();
				this.add(lblCalculated);
				lblCalculated.setText("Calc");
			}
			{
				lblActual = new JLabel();
				this.add(lblActual);
				lblActual.setText("Actual");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Must be called by the Swing thread.
	 * 
	 * @param value
	 */
	public void setActual(boolean value){
		if (value){
			lblActual.setText("IN");
		} else {
			lblActual.setText("OUT");
		}
	}

	/**
	 * Must be called by the Swing thread.
	 * 
	 * @param value
	 */
	public void setCalculated(boolean value){
		if (value){
			lblCalculated.setText("IN");
		} else {
			lblCalculated.setText("OUT");
		}
	}
	
	/**
	 * Must be called by the Swing thread.
	 * 
	 * @param name
	 */
	public void setFilterName(String name){
		lblName.setText(name);
	}

	/**
	 * @param attenuator The attenuator this is looking at
	 */
	public void setAttenuator(Attenuator attenuator) {
		this.attenuator = attenuator;
	}

	/**
	 * @return Returns the attenuator.
	 */
	public Attenuator getAttenuator() {
		return attenuator;
	}

	/**
	 * @param position The position of this filter in the array of filters
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 * @return Returns the position.
	 */
	public int getPosition() {
		return position;
	}

}
