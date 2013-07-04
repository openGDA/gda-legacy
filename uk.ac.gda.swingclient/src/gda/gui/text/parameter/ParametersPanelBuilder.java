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

package gda.gui.text.parameter;

import gda.gui.text.Formatter.SimpleFormatter;
import gda.gui.text.validator.ConfirmerMessage;
import gda.gui.text.validator.TextFieldValidator;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a panel containing a grid of text fields to allow the user to view and change the value of a variable See
 * ParameterPanelBuilderTest.java for an example. Also see the constructor for more details
 */
public class ParametersPanelBuilder extends JPanel implements PropertyChangeListener {
	
	private static final String VALUE_PROPERTY_CHANGE_NAME = "value";

	private static final Logger logger = LoggerFactory.getLogger(ParametersPanelBuilder.class);
	
	/**
	 * An empty spacer.
	 */
	public static final String NO_SPACER = "";
	
	/**
	 * Class to allow users of the ParameterBuilder to respond to user actions that change the value of a field. The
	 * only public method is setNewValue
	 */
	public static final String ParameterPropertyName = "Parameters";

	/**
	 * 
	 */
	public static final String ValuePropertyName = "Value";

	/**
	 * ParameterField Class
	 */
	public class ParameterField {
		// these are all to be package private
		Limited limited;

		JLabel label;

		JFormattedTextField field;

		Object oldValue;

		//changed the field text only without causes the limited to be updated
		void setValueFromMonitor(Object newValue) {
			setValue(newValue, true);
		}

		//changed the field and causes the limited to be updated
		void setValue(Object newValue) {
			setValue(newValue, false);
		}

		private void setValue(Object newValue, boolean preventChangeToLimited) {
			if (oldValue.equals(newValue)) {
				return;
			}
			if( preventChangeToLimited)
				oldValue = newValue;
			if (!field.hasFocus()) {
				field.setValue(newValue);
			}
		}
		
		javax.swing.JFormattedTextField.AbstractFormatter format;

		JLabel units;

		Color initialForegroundColor;

		/**
		 * @return limit id
		 */
		public long getLimitId() {
			return limited.id;
		}

		boolean resetAfterListeners=false;
		
		
		ParameterField() {
		}

		/**
		 * Attempts to set the limited field to a new value This may fail if the value is outside the limit in which
		 * case an option box is displayed.
		 * 
		 * @param newValue
		 */
		public void setNewValue(Object newValue) {
			limited = Limited.setVal(limited, newValue);
		}

		/**
		 * @return true if the field is reset to previous value after vetoable change listeners
		 */
		public boolean isResetAfterListeners() {
			return resetAfterListeners;
		}

		/**
		 * @param resetAfterListeners
		 */
		public void setResetAfterListeners(boolean resetAfterListeners) {
			this.resetAfterListeners = resetAfterListeners;
		}
		void setToolTip(){
			try{
				if(limited.fullTooltip == null){
					StringBuffer toolTip = new StringBuffer();
					if(limited.tooltip != null)
						toolTip.append(limited.tooltip);
					if (limited.editable && (limited.val instanceof Number)) {
						toolTip.append(". Range = ").append(format.valueToString(limited.min)).append(" - ").append(
								format.valueToString(limited.max));
					}
					if (!limited.units.isEmpty()) {
						toolTip.append(" ").append(limited.units);
					}
					if (limited.editable && limited.displayedMnemonic != 0) {
						toolTip.append(". (ALT-").append(limited.displayedMnemonic).append(')');
					}
					field.setToolTipText(toolTip.toString());
				} else {
					field.setToolTipText(limited.fullTooltip);
				}
			} catch( Exception e){
				logger.error(e.getMessage(),e);
			}
		}
		
		void setLimits(Object min, Object max){
			if( limited.val.getClass().equals(min.getClass()) && limited.val.getClass().equals(max.getClass())){
				limited.min = min;
				limited.max = max;
				setToolTip();
				
			}
		}
		
		void setUnits(String units){
			limited.units = units;
			this.units.setText(units);
			setToolTip();
		}
					
	}

	protected String title="";

	private ArrayList<ParameterField> parameters;

	/**
	 * The object passed to the constructor. It is not used by ParametersPanelBuilder in any way
	 */
	public Object ContextData;

	protected final VetoableChangeSupport vcs = new VetoableChangeSupport(this);

	protected java.util.List<Limited> limiteds = new ArrayList<Limited>();
	protected int columns = 0;
	protected String spacer= "";
	protected Border editableFieldBorder=BorderFactory.createBevelBorder(BevelBorder.LOWERED);
	protected Border noneditableFieldBorder=BorderFactory.createBevelBorder(BevelBorder.LOWERED);
	protected VetoableChangeListener propertyChangeListener;
	protected int txtColumns = 5;
	protected int vertSpace = 5;
	protected int minTextWidth = 80;
	protected int subHorzSpace = 10;
	protected int horzSpace = 10;	
	
	/**
	 * Default constructor - call configure to complete the layout
	 */
	public ParametersPanelBuilder(){
	}
/**
 * @return Title used in dialog boxes
 */
	public String getTitle() {
		return title;
	}


	/**
	 * @param title Title used in dialog boxes
	 */
	public void setTitle(String title) {
		if(configured)
			return;
		this.title = title;
	}


	/**
	 * @return  Any object that can be used by the caller to help processing PropertyChangeEvents.
	 */
	public Object getContextData() {
		return ContextData;
	}


	/**
	 * @param contextData Any object that can be used by the caller to help processing PropertyChangeEvents.
	 */
	public void setContextData(Object contextData) {
		ContextData = contextData;
	}



	/**
	 * @param limiteds A list of Limited objects to be displayed to the user
	 */
	public void setLimiteds(java.util.List<Limited> limiteds) {
		if(configured)
			return;
		this.limiteds = limiteds;
	}
	/**
	 * @param limited object to be displayed to the user
	 */
	public void addLimited( Limited limited){
		if(configured)
			return;
		this.limiteds.add(limited);
	}


	/**
	 * @return  number of columns in which the fields are to be shown
	 */
	public int getColumns() {
		return columns;
	}


	/**
	 * @param columns  number of columns in which the fields are to be shown
	 */
	public void setColumns(int columns) {
		if(configured)
			return;
		this.columns = columns;
	}


	/**
	 * @return A string to be displayed after the parameter name in the lable field
	 */
	public String getSpacer() {
		return spacer;
	}


	/**
	 * @param spacer A string to be displayed after the parameter name in the lable field
	 */
	public void setSpacer(String spacer) {
		if(configured)
			return;
		this.spacer = spacer;
	}


	/**
	 * @return Border used for editable fields
	 */
	public Border getEditableFieldBorder() {
		return editableFieldBorder;
	}


	/**
	 * @param editableFieldBorder Border used for editable fields
	 */
	public void setEditableFieldBorder(Border editableFieldBorder) {
		if(configured)
			return;
		this.editableFieldBorder = editableFieldBorder;
	}


	/**
	 * @return Border used for non-editable fields
	 */
	public Border getNoneditableFieldBorder() {
		return noneditableFieldBorder;
	}


	/**
	 * @param noneditableFieldBorder Border used for non-editable fields
	 */
	public void setNoneditableFieldBorder(Border noneditableFieldBorder) {
		if(configured)
			return;
		this.noneditableFieldBorder = noneditableFieldBorder;
	}

	/**
	 * @return  vertical space between items in pixels
	 */
	public int getVertSpace() {
		return vertSpace;
	}


	/**
	 * @param vertSpace vertical space between items in pixels
	 */
	public void setVertSpace(int vertSpace) {
		if(configured)
			return;
		this.vertSpace = vertSpace;
	}


	/**
	 * @return minimum width of each each text field in pixels
	 */
	public int getMinTextWidth() {
		return minTextWidth;
	}


	/**
	 * @param minTextWidth minimum width of each each text field in pixels
	 */
	public void setMinTextWidth(int minTextWidth) {
		if(configured)
			return;
		this.minTextWidth = minTextWidth;
	}


	/**
	 * @return  horizontal space between label fields and units  in pixels
	 */
	public int getSubHorzSpace() {
		return subHorzSpace;
	}


	/**
	 * @param subHorzSpace horizontal space between label fields and units  in pixels
	 */
	public void setSubHorzSpace(int subHorzSpace) {
		if(configured)
			return;
		this.subHorzSpace = subHorzSpace;
	}


	/**
	 * @return horizontal space between columns
	 */
	public int getHorzSpace() {
		return horzSpace;
	}

	/**
	 * @param horzSpace horizontal space between columns
	 */
	public void setHorzSpace(int horzSpace) {
		if(configured)
			return;
		this.horzSpace = horzSpace;
	}

	/**
	 * @return  object whose propertyChange method is called with a PropertyChangeEvent object whenever
	 *            a valid change is made to a ParameterField. If null then a value change iParametersPanelBuilders handled by the object each
	 *            itself by calling ParameterField.setNewValue method.
	 */
	public VetoableChangeListener getPropertyChangeListener() {
		return propertyChangeListener;
	}
	
	/**
	 * @param propertyChangeListener object whose propertyChange method is called with a PropertyChangeEvent object whenever
	 *            a valid change is made to a ParameterField. If null then a value change iParametersPanelBuilders handled by the object each
	 *            itself by calling ParameterField.setNewValue method.
	 */
	public void setPropertyChangeListener(VetoableChangeListener propertyChangeListener) {
		if(configured)
			return;
		this.propertyChangeListener = propertyChangeListener;
	}

	/**
	 * @param limiteds
	 * @param txtColumns
	 * @param spacer
	 * @param title
	 * @param border
	 * @param editableFieldBorder
	 * @param noneditableFieldBorder
	 * @param columns
	 * @param minTextWidth
	 * @param subHorzSpace
	 * @param horzSpace
	 * @param vertSpace
	 * @param propertyChangeListener
	 * @param ContextData
	 */
	public ParametersPanelBuilder(java.util.List<Limited> limiteds,
			@SuppressWarnings("unused") int txtColumns, String spacer, String title, Border border, Border editableFieldBorder,
			Border noneditableFieldBorder, int columns, int minTextWidth, int subHorzSpace, int horzSpace,
			int vertSpace, VetoableChangeListener propertyChangeListener, Object ContextData) {
		setBorder(border);
		setTitle(title);
		setLimiteds(limiteds);
		setColumns(columns);
		setSpacer(spacer);
		if(editableFieldBorder != null){
			setEditableFieldBorder(editableFieldBorder);
		}
		if(noneditableFieldBorder != null){
			setNoneditableFieldBorder(noneditableFieldBorder);
		}
		this.ContextData = ContextData;
		setPropertyChangeListener(propertyChangeListener);
		setVertSpace(vertSpace);
		setMinTextWidth(minTextWidth);
		setSubHorzSpace(subHorzSpace);
		setHorzSpace(horzSpace);
		configure();
	}

	boolean configured=false;

	/**
	 *  Method to call to layout the items.
	 */
	public void configure(){
		
		parameters = new ArrayList<ParameterField>();

		setLayout(new GridBagLayout());
		createParameters();
		int iColumn = 0;
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = GridBagConstraints.RELATIVE;
		c.gridy = 0;
		Insets mainInsets = new Insets(0, 0, vertSpace, horzSpace);
		Insets subInsets = new Insets(0, 0, vertSpace, subHorzSpace);
		for (ParameterField p : parameters) {
			if (p != null) {
				c.insets = subInsets;
				c.fill = GridBagConstraints.BOTH;
				if (p.label.getAlignmentX() == Component.RIGHT_ALIGNMENT) {
					Box labelPanel = Box.createHorizontalBox();
					labelPanel.add(Box.createHorizontalGlue());
					labelPanel.add(p.label);
					labelPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
					add(labelPanel, c);
				} else {
					add(p.label, c);
				}
				Box subPanel = Box.createVerticalBox();
				subPanel.add(p.field);
				subPanel.add(Box.createHorizontalStrut(minTextWidth));
				add(subPanel, c);
				c.insets = mainInsets;
				add(p.units, c);
			}
			iColumn += 1;
			if (iColumn >= columns) {
				iColumn = 0;
				c.gridy += 1;
			}
		}
		this.vcs.addVetoableChangeListener(ParameterPropertyName, propertyChangeListener);
		configured = true;
	}

	private void createParameters() {
		for (Limited l : limiteds) {
			if (l == null) {
				// add a space
				parameters.add(null);
				continue;
			}
			ParameterField p = new ParameterField();
			
			SimpleFormatter formatter = new SimpleFormatter(l.formatString, l.val.getClass());
			formatter.setOverwriteMode(false);
			p.format = formatter;
			
			p.field = new JFormattedTextField(p.format);
			p.oldValue = l.val;
			p.field.setValue(p.oldValue);
			p.field.setColumns(columns);
			p.field.setEditable(l.editable);
			p.field.setFocusable(l.editable);
			p.field.addPropertyChangeListener(this);
			p.field.addKeyListener(new ResetFieldActionListener(p));
			p.initialForegroundColor = p.field.getForeground();
			if (l.editable) {
				p.field.setBorder(editableFieldBorder );
			} else {
				p.field.setBorder(noneditableFieldBorder);
			}
			p.field.setFocusLostBehavior(l.getFocusLostBehaviour());

			StringBuffer label = new StringBuffer().append(l.label);
			if (spacer != null) {
				label.append(spacer);
			}
			p.label = new JLabel(label.toString());
			p.label.setLabelFor(p.field);
			if (l.displayedMnemonic != (char) 0) {
				p.label.setDisplayedMnemonic(l.displayedMnemonic);
			}
			p.label.setAlignmentX(l.alignmentX);
			p.units = new JLabel(l.units);
			p.limited = l;
			p.setToolTip();
			parameters.add(p);

		}
	}

	private ParameterField getParameterField(long limitedId) {
		if( configured){
			for (ParameterField p : parameters) {
				if (p != null && p.limited.id == limitedId) {
					return p;
				}
			}
		}
		return null;
	}

	/**
	 * Changes the field text but not the associated Limited
	 * @param limitedId
	 * @param value
	 */
	public void setParameterFromMonitor(long limitedId, double value) {
		ParameterField p = getParameterField(limitedId);
		if (p != null) {
			p.setValueFromMonitor(new Double(value));
		}
	}

	/**
	 * Changes the field text and fires validator and changed associated Limited
	 * @param limitedId
	 * @param value
	 */
	public void setParameter(long limitedId, double value) {
		ParameterField p = getParameterField(limitedId);
		if (p != null) {
			p.setValue(new Double(value));
		}
	}
	
	/**
	 * @param limitedId
	 * @param min
	 * @param max
	 */
	public void setLimits(long limitedId, Object min, Object max){
		ParameterField p = getParameterField(limitedId);
		if (p != null) {
			p.setLimits(min,max);
		}
		
	}

	/**
	 * @param limitedId
	 * @param units
	 */
	public void setUnits(long limitedId, String units){
		ParameterField p = getParameterField(limitedId);
		if (p != null) {
			p.setUnits(units);
		}
		
	}
	/**
	 * Changes the displayed text but not the underlying limited value
	 * @param limitedId
	 * @param value
	 */
	public void setParameterFromMonitor(long limitedId, Object value) {
		ParameterField p = getParameterField(limitedId);
		if (p != null) {
			p.setValueFromMonitor(value);
		}
	}

	/**
	 * Changes the displayed text and fires validator and changes underlying limited value
	 * @param limitedId
	 * @param value
	 */
	public void setParameter(long limitedId, Object value) {
		ParameterField p = getParameterField(limitedId);
		if (p != null) {
			p.setValue(value);
		}
	}
	
	/**
	 * @param limitedId - identifies the filed
	 * @param value - true if the changes entered by user are to be reset to previous value after listeners
	 * @throws IllegalStateException 
	 */
	public void setResetAfterListeners(long limitedId, boolean value) throws IllegalStateException {
		if(!configured)
			throw new IllegalStateException("setResetAfterListeners msut be called after configure");
		ParameterField p = getParameterField(limitedId);
		if (p != null) {
			p.setResetAfterListeners(value);
		}
	}
	
	
	/**
	 * @param limitedId
	 * @param fg
	 */
	public void setForeground(long limitedId, Color fg) {
		ParameterField p = getParameterField(limitedId);
		if (p != null) {
			p.field.setForeground(fg);
		}
	}

	/**
	 * @param limitedId
	 * @return Color foreground
	 */
	public Color getForeground(long limitedId) {
		ParameterField p = getParameterField(limitedId);
		if (p != null) {
			return p.field.getForeground();
		}
		return null;
	}

	/**
	 * @param limitedId
	 * @param enabled
	 */
	public void setEnabled(long limitedId, boolean enabled) {
		ParameterField p = getParameterField(limitedId);
		if (p != null) {
			p.field.setEnabled(enabled);
		}
	}

	/**
	 * @param limitedId
	 * @param editable
	 */
	public void setEditable(long limitedId, boolean editable) {
		ParameterField p = getParameterField(limitedId);
		if (p != null) {
			//need to change underlying value of limited  - see setParameterConnectedState
			p.limited.editable = editable; 
			p.field.setEditable(editable);
		}
	}

	/**
	 * @param limitedId
	 * @param cursor
	 */
	public void setCursor(long limitedId, Cursor cursor) {
		ParameterField p = getParameterField(limitedId);
		if (p != null) {
			p.field.setCursor(cursor);
		}
	}

	/**
	 * @param limitedId
	 * @return cursor
	 */
	public Cursor getCursor(long limitedId) {
		ParameterField p = getParameterField(limitedId);
		if (p != null) {
			return p.field.getCursor();
		}
		return null;
	}

	/**
	 * @param limitedId
	 * @return limited value
	 */
	public Object getLimitedValue(long limitedId) {
		ParameterField p = getParameterField(limitedId);
		if (p != null) {
			return p.limited.val;
		}
		return null;
	}

	/**
	 * @param limitedId
	 * @param connected
	 */
	public void setParameterConnectedState(long limitedId, boolean connected) {
		ParameterField p = getParameterField(limitedId);
		if (p != null) {
			p.field.setEditable(p.limited.editable && connected);
			p.field.setForeground(connected ? p.initialForegroundColor : Color.GRAY);
		}
	}

	/**
	 * @return limiteds
	 */
	public java.util.List<Limited> getParameters() {
		ArrayList<Limited> limiteds = new ArrayList<Limited>();
		for (ParameterField p : parameters) {
			limiteds.add(p == null ? null : p.limited);
		}
		return limiteds;
	}

	/**
	 * @param LimitedId
	 * @param listener
	 */
	public void addVetoableChangeListener(int LimitedId, VetoableChangeListener listener) {
		addVetoableChangeListener(ValuePropertyName + Integer.toString(LimitedId), listener);
	}

	/**
	 * @param Property
	 * @param listener
	 */
	public void addVetoableChangeListener(String Property, VetoableChangeListener listener) {
		this.vcs.addVetoableChangeListener(Property, listener);
	}

	/**
	 * @param Property
	 * @param listener
	 */
	public void removeVetoableChangeListener(String Property, VetoableChangeListener listener) {
		this.vcs.removeVetoableChangeListener(Property, listener);
	}

	/**
	 * Contains the ParameterPanelBuilder that created the event and the ParameterField that the user has tried to
	 * change
	 */
	public class ParameterChangeEventSource {

		/**
		 * 
		 */
		public final ParametersPanelBuilder parametersPanelBuilder;

		/**
		 * 
		 */
		public final ParameterField parameterField;

		/**
		 * @param parametersPanelBuilder
		 *            The <code>ParametersPanelBuilder</code> that created the event
		 * @param parameterField
		 *            The <code>ParameterField</code> that the user has tried to change
		 */
		public ParameterChangeEventSource(ParametersPanelBuilder parametersPanelBuilder, ParameterField parameterField) {
			this.parametersPanelBuilder = parametersPanelBuilder;
			this.parameterField = parameterField;
		}
	}

	private boolean loseFocusAfterValueChange;
	
	public void setLoseFocusAfterValueChange(boolean loseFocusAfterValueChange) {
		this.loseFocusAfterValueChange = loseFocusAfterValueChange;
	}
	
	private static void loseFocus() {
		final KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		focusManager.clearGlobalFocusOwner();
	}
	
	/**
	 * This function responds to PropertyChangeEvent with name equal to 'value' source set to the <code>field</code>
	 * field of a one of the ParameterField object created in the constructor For the ParameterField specified by the
	 * event the function checks the <code>double</code> extracted from associated JFormattedTextField object against
	 * the limits for the field. If the new value is valid it then either changes the ParameterField value directly or
	 * passes the newvalue onto the propertyChangeListener supplied in the constructor with the source of the new
	 * PropertyChangeEvent set to a ParameterChangeEventSource, Otherwise the ParameterField identified by the source is
	 * {@inheritDoc}
	 * 
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		Object source = e.getSource();
		if (e.getPropertyName() != VALUE_PROPERTY_CHANGE_NAME) {
			return;
		}

		for (ParameterField p : parameters) {
			if (p != null && source == p.field) {
				// String newValueString = p.field.getText();
				Object newValObject = p.field.getValue();
				if (!( newValObject instanceof Number || newValObject instanceof String)) {
					// this is valid if the field is displaying a string
					return;
				}
				Object newValue = p.field.getValue();
				if (p.oldValue.equals(newValue)) {
					return;
				}
				boolean isValid = false;
				if( p.limited.validator != null){
					String msg = p.limited.validator.isValid(newValue);
					isValid = msg == null;
					if(!isValid){
						JOptionPane.showMessageDialog(this, msg, title,
								JOptionPane.WARNING_MESSAGE);
					}
				} else {
					if (newValue instanceof Double) {
						TextFieldValidator<Double> textFieldValidator = new TextFieldValidator<Double>();
						isValid = (textFieldValidator.isValid(this, (Double) newValue, (Double) p.limited.min,
								(Double) p.limited.max, title));
					} else if (newValue instanceof Integer) {
						TextFieldValidator<Integer> textFieldValidator = new TextFieldValidator<Integer>();
						isValid = (textFieldValidator.isValid(this, (Integer) newValue, (Integer) p.limited.min,
								(Integer) p.limited.max, title));
					} else {
						isValid = true;
					}
				}
				if (isValid) {
					if (p.limited.confirmer != null) {
						ConfirmerMessage confirmMessage = null;
						try {
							confirmMessage = p.limited.confirmer.getMessage(newValue);
						} catch (Exception ex) {
							logger.error("Unable to get user confirmation message", ex);
							confirmMessage = new ConfirmerMessage("Confirm value change", "Are you sure?");
						}
						final int answer = JOptionPane.showConfirmDialog(this, confirmMessage.getMessage(), confirmMessage.getTitle(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						if (answer != JOptionPane.YES_OPTION) {
							isValid = false;
						}
					}
				}
				
				if (isValid) {
					if (loseFocusAfterValueChange) {
						loseFocus();
					}
					try {
						if (this.vcs.hasListeners(ParameterPropertyName)) {
							PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(
									new ParameterChangeEventSource(this, p), ParameterPropertyName, p.limited.val,
									newValue);
							vcs.fireVetoableChange(propertyChangeEvent);
						}
						
						String valueName = ValuePropertyName + Integer.toString(p.limited.id);
						if (this.vcs.hasListeners(valueName)) {
							PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(
									new ParameterChangeEventSource(this, p), valueName, e.getOldValue(), newValue);
							vcs.fireVetoableChange(propertyChangeEvent);
						}
						
						p.setNewValue(newValue);
						
						if(!p.resetAfterListeners)
							p.oldValue = newValue; 
					} catch (PropertyVetoException _e) {
						logger.error(_e.getMessage());
						p.field.setValue(p.oldValue);
					}
				} else {
					p.field.setValue(p.oldValue);
				}
				if(p.resetAfterListeners)
					p.field.setValue(p.oldValue);
				break;
			}
		}
	}

//	@Override
//	public void setEnabled(boolean enabled) {
//		super.setEnabled(enabled);
//		for (ParameterField p : parameters) {
//			if (p != null) {
//				p.field.setEnabled(enabled);
//			}
//		}
//	}

}
class ResetFieldActionListener implements KeyListener {

	ParametersPanelBuilder.ParameterField p;
	
	ResetFieldActionListener (ParametersPanelBuilder.ParameterField p)
	{
		this.p = p;
	}
	@Override
	public void keyPressed(KeyEvent e) {
		if( e.getKeyCode() == KeyEvent.VK_ESCAPE)
			p.field.setValue(p.oldValue);
	}
	@Override
	public void keyReleased(KeyEvent e) {
	}
	@Override
	public void keyTyped(KeyEvent e) {
	}
}