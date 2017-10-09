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

package gda.gui;

import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.jython.INamedScanDataPointObserver;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;

import com.jidesoft.docking.DockContext;

/**
 * A class to provide a uniform interface for all panels that will be configured into the tabbed pane environment of
 * {@link gda.gui.AcquisitionFrame}
 */
public class AcquisitionPanel extends JPanel implements Findable, Configurable, INamedScanDataPointObserver {

	private transient int tabIndex;
	private static HashMap<String, Integer> initModes = new HashMap<String, Integer>();
	private static HashMap<Integer, String> initModesReverse = new HashMap<Integer, String>();
	/**
	 * 
	 */
	public static String StateFrameDocked = "STATE_FRAMEDOCKED";
	/**
	 * 
	 */
	public static String StateAutoHide = "STATE_AUTOHIDE";
	/**
	 * 
	 */
	public static String StateAutoHideShowing = "STATE_AUTOHIDE_SHOWING";
	/**
	 * 
	 */
	public static String StateFloating = "STATE_FLOATING";
	/**
	 * 
	 */
	public static String StateHidden = "STATE_HIDDEN";

	static {
		initModes.put(StateFrameDocked, DockContext.STATE_FRAMEDOCKED);
		initModes.put(StateAutoHide, DockContext.STATE_AUTOHIDE);
		initModes.put(StateAutoHideShowing, DockContext.STATE_AUTOHIDE_SHOWING);
		initModes.put(StateFloating, DockContext.STATE_FLOATING);
		initModes.put(StateHidden, DockContext.STATE_HIDDEN);
		Set<Map.Entry<String, Integer>> modes = initModes.entrySet();
		for (Map.Entry<String, Integer> mode : modes) {
			initModesReverse.put(mode.getValue(), mode.getKey());
		}
	}
	private int initModeAsInt = DockContext.STATE_FRAMEDOCKED;

	/**
	 * 
	 */
	public static String ALWAYS = "ALWAYS";
	/**
	 * 
	 */
	public static String AS_NEEDED = "AS_NEEDED";
	/**
	 * 
	 */
	public static String NEVER = "NEVER";

	private static HashMap<String, Integer> horizontalScrollBarPolicyModes = new HashMap<String, Integer>();
	private static HashMap<Integer, String> horizontalScrollBarPolicyModesReverse = new HashMap<Integer, String>();
	static {
		horizontalScrollBarPolicyModes.put(ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		horizontalScrollBarPolicyModes.put(AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		horizontalScrollBarPolicyModes.put(NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		Set<Map.Entry<String, Integer>> modes = horizontalScrollBarPolicyModes.entrySet();
		for (Map.Entry<String, Integer> mode : modes) {
			horizontalScrollBarPolicyModesReverse.put(mode.getValue(), mode.getKey());
		}
	}
	private int horizontalScrollBarPolicyAsInt = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;

	private static HashMap<String, Integer> verticalScrollBarPolicyModes = new HashMap<String, Integer>();
	private static HashMap<Integer, String> verticalScrollBarPolicyModesReverse = new HashMap<Integer, String>();

	static {
		verticalScrollBarPolicyModes.put(ALWAYS, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		verticalScrollBarPolicyModes.put(AS_NEEDED, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		verticalScrollBarPolicyModes.put(NEVER, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		Set<Map.Entry<String, Integer>> modes = verticalScrollBarPolicyModes.entrySet();
		for (Map.Entry<String, Integer> mode : modes) {
			verticalScrollBarPolicyModesReverse.put(mode.getValue(), mode.getKey());
		}
	}
	private int verticalScrollBarPolicyAsInt = ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;

	/**
	 * @return Returns the tabIndex.
	 */
	public int getTabIndex() {
		return tabIndex;
	}

	/**
	 * @param tabIndex
	 *            The tabIndex to set.
	 */
	public void setTabIndex(int tabIndex) {
		this.tabIndex = tabIndex;
	}

	/**
	 * @param initModeString
	 */
	public void setInitMode(String initModeString) {
		if (!initModes.containsKey(initModeString)) {
			throw new IllegalArgumentException("AcquisitionPanel.setInitMode - invalid initModeString:"
					+ initModeString);
		}
		this.initModeAsInt = initModes.get(initModeString);
	}

	/**
	 * @return String initmode
	 */
	public String getInitMode() {
		return initModesReverse.get(initModeAsInt);
	}

	/**
	 * @return int initmode
	 */
	public int getInitModeAsInt() {
		return initModeAsInt;
	}

	/**
	 * @param verticalScrollBarPolicyString
	 */
	public void setVerticalScrollBarPolicy(String verticalScrollBarPolicyString) {
		if (!verticalScrollBarPolicyModes.containsKey(verticalScrollBarPolicyString)) {
			throw new IllegalArgumentException(
					"AcquisitionPanel.setverticalScrollBarPolicy - invalid verticalScrollBarPolicyString:"
							+ verticalScrollBarPolicyString);
		}
		this.verticalScrollBarPolicyAsInt = verticalScrollBarPolicyModes.get(verticalScrollBarPolicyString);
	}

	/**
	 * @return Vertical Scroll Bar Policy as String
	 */
	public String getVerticalScrollBarPolicy() {
		return verticalScrollBarPolicyModesReverse.get(verticalScrollBarPolicyAsInt);
	}

	/**
	 * @return Vertical Scroll Bar Policy as int
	 */
	public int getVerticalScrollBarPolicyAsInt() {
		return verticalScrollBarPolicyAsInt;
	}

	/**
	 * @param horizontalScrollBarPolicyString
	 */
	public void setHorizontalScrollBarPolicy(String horizontalScrollBarPolicyString) {
		if (!horizontalScrollBarPolicyModes.containsKey(horizontalScrollBarPolicyString)) {
			throw new IllegalArgumentException(
					"AcquisitionPanel.sethorizontalScrollBarPolicy - invalid horizontalScrollBarPolicyString:"
							+ horizontalScrollBarPolicyString);
		}
		this.horizontalScrollBarPolicyAsInt = horizontalScrollBarPolicyModes.get(horizontalScrollBarPolicyString);
	}

	/**
	 * @return String
	 */
	public String getHorizontalScrollBarPolicy() {
		return horizontalScrollBarPolicyModesReverse.get(horizontalScrollBarPolicyAsInt);
	}

	/**
	 * @return String
	 */
	public int getHorizontalScrollBarPolicyAsInt() {
		return horizontalScrollBarPolicyAsInt;
	}

	protected String label;
	
	
	public String getLabel() {
		return label != null ? label : getName();
	}

	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Subclasses to override this method.
	 * 
	 * @see gda.factory.Configurable#configure()
	 */
	@Override
	public void configure() throws FactoryException {
	}

	/**
	 * Default implementation does nothing. Please implement if the
	 * panel should be updated with ScanDataPoint events.
	 * {@inheritDoc}
	 *
	 * @see gda.observable.IObserver#update(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void update(Object theObserved, Object changeCode) {
		
	}
}
