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

package gda.gui.oemove;

import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.oe.OE;

import java.util.ArrayList;

import javax.swing.JComponent;

/**
 * A class responsible for the display of arrows representing movement of DOF's.
 */
public class DOFImageView implements Viewable, IObservable {
	private ObservableComponent observableComponent = new ObservableComponent();

	private String name;

	private int xImagePosition = 0;

	private int yImagePosition = 0;

	private String arrowGifName;

	private String labelPosition;

	private int labelSize;

	private DOFImageComponent dofImageComponent;

	private DOFControlPanel dofControlPanel;

	private boolean editable = false;

	private JComponent positionDisplayComponent;

	private OE oe;

	private ArrayList<String> modeList = new ArrayList<String>();

	private ArrayList<String> speedList = new ArrayList<String>();

	private double defaultInputValue = 0.0;

	/**
	 * 
	 */
	public DOFImageView() {
	}

	/**
	 * @return Returns the labelPosition.
	 */
	public String getLabelPosition() {
		return labelPosition;
	}

	/**
	 * @param labelPosition
	 *            The labelPosition to set.
	 */
	public void setLabelPosition(String labelPosition) {
		this.labelPosition = labelPosition;
	}

	/**
	 * @return Returns the labelSize.
	 */
	public int getLabelSize() {
		return labelSize;
	}

	/**
	 * @param labelSize
	 *            The labelSize to set.
	 */
	public void setLabelSize(int labelSize) {
		this.labelSize = labelSize;
	}

	/**
	 * @return Returns the name.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the oeGifName.
	 */
	public String getArrowGifName() {
		return arrowGifName;
	}

	/**
	 * @param arrowGifName
	 *            The arrowGifName to set.
	 */
	public void setArrowGifName(String arrowGifName) {
		this.arrowGifName = arrowGifName;
	}

	/**
	 * @return Returns the xImagePosition.
	 */
	public int getXImagePosition() {
		return xImagePosition;
	}

	/**
	 * @param imagePosition
	 *            The xImagePosition to set.
	 */
	public void setXImagePosition(int imagePosition) {
		xImagePosition = imagePosition;
	}

	/**
	 * @return Returns the yImagePosition.
	 */
	public int getYImagePosition() {
		return yImagePosition;
	}

	/**
	 * @param imagePosition
	 *            The yImagePosition to set.
	 */
	public void setYImagePosition(int imagePosition) {
		yImagePosition = imagePosition;
	}

	/**
	 * Set the order of modes (used by castor)
	 * 
	 * @param modeList
	 */
	public void setModeNameList(ArrayList<String> modeList) {
		this.modeList = modeList;
	}

	/**
	 * Get the mode names (used by castor)
	 * 
	 * @return an ArrayList of mode names
	 */
	public ArrayList<String> getModeNameList() {
		return modeList;
	}

	/**
	 * Set the order of speeds (used by castor)
	 * 
	 * @param speedList
	 */
	public void setSpeedNameList(ArrayList<String> speedList) {
		this.speedList = speedList;
	}

	/**
	 * Get the speed names (used by castor)
	 * 
	 * @return an ArrayList of speed names
	 */
	public ArrayList<String> getSpeedNameList() {
		return speedList;
	}

	/**
	 * @return Returns the defaultInputValue.
	 */
	public double getDefaultInputValue() {
		return defaultInputValue;
	}

	/**
	 * @param defaultInputValue
	 *            The defaultInputValue to set.
	 */
	public void setDefaultInputValue(double defaultInputValue) {
		this.defaultInputValue = defaultInputValue;
	}

	@Override
	public ArrayList<Viewable> getViewableList() {
		// Do nothing for leaf implementation of composite pattern
		return null;
	}

	@Override
	public void addViewable(Viewable viewable) {
		// Do nothing for leaf implementation of composite pattern
	}

	@Override
	public JComponent getDisplayComponent() {
		dofImageComponent = new DOFImageComponent(this);
		dofImageComponent.setToolTipText(name);
		return dofImageComponent;
	}

	/**
	 * Add a display label to with current postion to the image.
	 */
	public void displayLabel() {
		if (dofImageComponent != null && labelPosition != null && !labelPosition.equals("None") && oe != null) {
			positionDisplayComponent = (JComponent) DOFDisplayComponentFactory.createPositionDisplay(oe, name,
					labelSize, false);
			dofImageComponent.addDisplay(labelPosition, positionDisplayComponent);
		}
	}

	/**
	 * Construct the control panel and return the instance of it.
	 * 
	 * @param oe
	 * @param dofName
	 * @return the control panel component
	 */
	public JComponent getControlPanel(OE oe, String dofName) {
		dofControlPanel = new DOFControlPanel(oe, dofName);
		return dofControlPanel;
	}

	@Override
	public void setParent(Viewable parent) {
		// not used in this class
	}

	/**
	 * Set the dof that is selected
	 */
	public void setSelectedDOF() {
		dofImageComponent.doSelect();
	}

	/**
	 * Set whether this component can be moved (editted)
	 * 
	 * @param editable
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	/**
	 * @return true if editable
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * Associate an OE with this view
	 * 
	 * @param oe
	 */
	public void setOE(OE oe) {
		this.oe = oe;
	}

	/**
	 * Notify observers of this class
	 */
	public void notifyIObservers() {
		observableComponent.notifyIObservers(this, null);
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}
}
