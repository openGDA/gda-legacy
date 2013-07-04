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

package gda.util;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;

import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;

/**
 * Provides a JTextField derivative which accepts the input of a decimal number and performs validation on the input as
 * it happens. A minimum and/or maximum value may be set for the field to ensure that the value of the content number is
 * always within a certain range.
 * <p>
 * The contents of this field are validated when number entry is deemed to have finished. The point at which this occurs
 * depends upon the setting of update mode.
 * <p>
 * DecimalField components are linked to two separate <b>DecimalDocument</b> instances. The first is the <i>active</i>
 * document, and is visible to the user as the contents of the DecimalField component. This document is changed
 * instantly as the user types. The second document is the <i>buffer</i> document, and is updated only when the user
 * has completely input the number they are typing (or by an explicit call to <b>updateBufferDocument</b>). This
 * document exists so that other objects can use a DocumentListener to get update messages on DecimelField components
 * when the number content changes, without having to recieve updates after each keypress.
 * 
 * @see DecimalDocument
 */
public class DecimalField extends JTextField implements MouseListener, FocusListener {
	/**
	 * 
	 */
	public final static boolean MINIMUM_OBSERVED = true;

	/**
	 * 
	 */
	public final static boolean MAXIMUM_OBSERVED = true;

	/**
	 * 
	 */
	public final static boolean NO_MINIMUM = false;

	/**
	 * 
	 */
	public final static boolean NO_MAXIMUM = false;

	/**
	 * This constant indicates no validation and updating of the contents of this DecimalField unless explicit calls are
	 * made to makeInRange() and updateBufferDocument()
	 */
	public final static int NO_UPDATE = 0;

	/**
	 * This constant indicates that the field contents will be validated by bounds-checking (according to min and max
	 * values being used) and the buffer document updated each time this component loses the input focus. This could be
	 * a mouse click on another component, a tab key press or a requestFocus() call from another component.
	 */
	public final static int UPDATE_ON_LOSEFOCUS = 1;

	/**
	 * This constant signifies that the field contents will be validated by bounds-checking and the buffer document will
	 * be updated each time the mouse pointer leaves this component. This mode implies that the mouse pointer should be
	 * positioned over this component whilst input takes place.
	 */
	public final static int UPDATE_ON_MOUSEEXIT = 2;

	// instance variables
	private double min_value = Double.NEGATIVE_INFINITY;

	private double max_value = Double.POSITIVE_INFINITY;

	// flags for whether we do bounds checking or not
	private boolean min_value_observed = NO_MINIMUM;

	private boolean max_value_observed = NO_MAXIMUM;

	private int auto_update = UPDATE_ON_LOSEFOCUS;

	private DecimalDocument buffer_document;

	private DecimalDocument active_document;

	private DecimalFormat _dFormat = new DecimalFormat();

	// constructors

	/**
	 * Constructor.
	 */
	public DecimalField() {
		this(0.0, 6);
		_dFormat.setGroupingUsed(false);
	}

	/**
	 * Constructor.
	 * 
	 * @param columns
	 * @param maxInt
	 * @param maxFract
	 */
	public DecimalField(int columns, int maxInt, int maxFract) {
		this(0.0, columns);
		setMaximumIntegerDigits(maxInt);
		setMaximumFractionDigits(maxFract);
		_dFormat.setGroupingUsed(false);
	}

	/**
	 * Constructor.
	 * 
	 * @param value
	 * @param columns
	 */
	public DecimalField(double value, int columns) {
		super(columns);
		buffer_document = new DecimalDocument();
		active_document = new DecimalDocument();
		setDocument(active_document);
		addFocusListener(this);
		addMouseListener(this);
		setValue(value);
		_dFormat.setGroupingUsed(false);
		setCaret(new TestCaret());
	}

	/**
	 * Constructor.
	 * 
	 * @param value
	 * @param columns
	 * @param update
	 */
	public DecimalField(double value, int columns, int update) {
		this(value, columns);
		auto_update = update;
		_dFormat.setGroupingUsed(false);
	}

	/**
	 * Constructor.
	 * 
	 * @param value
	 * @param columns
	 * @param min
	 * @param max
	 */
	public DecimalField(double value, int columns, double min, double max) {
		this(value, columns);
		setLimits(min, max);
		_dFormat.setGroupingUsed(false);
	}

	/**
	 * Constructor.
	 * 
	 * @param value
	 * @param columns
	 * @param min
	 * @param max
	 * @param update
	 */
	public DecimalField(double value, int columns, double min, double max, int update) {
		this(value, columns, min, max);
		auto_update = update;
		_dFormat.setGroupingUsed(false);
	}

	/**
	 * Sets the maximum number of digits before the decimal point
	 * 
	 * @param digits
	 *            The Maximum number of digits before the decimal point
	 */
	public void setMaximumIntegerDigits(int digits) {
		_dFormat.setMaximumIntegerDigits(digits);
	}

	/**
	 * Sets the maximum number of digits after the decimal point
	 * 
	 * @param digits
	 *            The maximum number of digits after the decimal point
	 */
	public void setMaximumFractionDigits(int digits) {
		_dFormat.setMaximumFractionDigits(digits);
	}

	/**
	 * Returns the double value represented by the contents of this DecimalField.
	 * 
	 * @return The double value.
	 */
	public double getValue() {
		return active_document.getValue();
	}

	/**
	 * sets the contents of this DecimalField to the specified double value. The contents of the buffer document will be
	 * updated by this call.
	 * 
	 * @param value
	 *            The new value
	 */
	public void setValue(double value) {
		setText(_dFormat.format(value));
		updateBufferDocument();
	}

	/**
	 * Sets the minimum value that may be represented by this fields contents. Calls to this method automatically set
	 * the MINIMUM_OBSERVED flag true.
	 * 
	 * @param value
	 *            The new minimum value for this component.
	 * @see #MINIMUM_OBSERVED
	 * @see #NO_MINIMUM
	 */
	public void setMinValue(double value) {
		min_value = value;
		min_value_observed = true;
	}

	/**
	 * Sets the maximum value that may be represented by this fields contents. Calls to this method automatically set
	 * the MAXIMUM_OBSERVED flag true.
	 * 
	 * @param value
	 *            The new maximum value for this component.
	 * @see #MAXIMUM_OBSERVED
	 * @see #NO_MAXIMUM
	 */
	public void setMaxValue(double value) {
		max_value = value;
		max_value_observed = true;
	}

	/**
	 * Sets upper and lower bounds for the contents of this DecimalField. Both MINIMUM_OBSERVED and MAXIMUM_OBSERVED are
	 * enabled by this call.
	 * 
	 * @param min
	 *            The new minimum value
	 * @param max
	 *            The new maximum value
	 */
	public void setLimits(double min, double max) {
		setMinValue(min);
		setMaxValue(max);
	}

	/**
	 * Sets the contents of the buffer document to reflect the contents of the active (visible) document. The buffer
	 * document exists so that other objects may listen on this component and register a change in document contents
	 * only at important events, rather than recieving update information as the user types.
	 */
	public void updateBufferDocument() {
		try {
			buffer_document.remove(0, buffer_document.getLength());
			buffer_document.insertString(0, new Double(getValue()).toString(), null);
		} catch (BadLocationException ex) {
		}
	}

	/**
	 * Sets the value within the min/max values for this component. This method is normally called automatically when
	 * one of the automatic update modes is set.
	 */
	public void makeInRange() {
		if (isTooHigh()) {
			setValue(max_value);
		} else if (isTooLow()) {
			setValue(min_value);
		} else {
			setValue(getValue());
		}
	}

	/**
	 * Returns true if the value represented by this components contents exceeds the permitted maximum value.
	 * 
	 * @return true if the value represented by this components contents exceeds the permitted maximum value
	 */
	protected boolean isTooHigh() {
		return (max_value_observed && getValue() > max_value);
	}

	/**
	 * Returns true if the value represented by this components contents is less than the permitted minimum value.
	 * 
	 * @return true if the value represented by this components contents is less than the permitted minimum value
	 */
	protected boolean isTooLow() {
		return (min_value_observed && getValue() < min_value);
	}

	// MouseListener

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (auto_update == UPDATE_ON_MOUSEEXIT) {
			makeInRange();
			updateBufferDocument();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// requestFocus();
	}

	@Override
	public void focusGained(FocusEvent e) {
	}

	@Override
	public void focusLost(FocusEvent e) {
		if (auto_update == UPDATE_ON_LOSEFOCUS) {
			makeInRange();
			updateBufferDocument();
		}
	}

	/**
	 * @return DecimalFormat
	 */
	public DecimalFormat getDecimalFormat() {
		return _dFormat;
	}

	/**
	 * A kludge klass of the highest order to get out of bug #528 part two. This class is set as the Caret of the
	 * DecimalField in order to override the default clicking behaviour.
	 */
	private class TestCaret extends DefaultCaret {
		@Override
		public void mouseClicked(MouseEvent me) {
			MouseEvent mine;

			// By default a double-click does the SelectWordAction of
			// DefaultToolkit
			// and a triple-click does the SelectLineAction. We fool the
			// DecimalField
			// here into thinking that a double-click is a triple-click. See
			// bug
			// #528
			// notes for more information. This might be dangerous though it
			// seems
			// to
			// work.
			if (me.getClickCount() == 2) {
				// Construct a new event in which clickCount is set to 3
				mine = new MouseEvent(me.getComponent(), me.getID(), 0, me.getModifiers(), me.getX(), me.getY(), 3,
						false, me.getButton());
			} else {
				// Use the incoming event.
				mine = me;
			}

			// Pass the event on to DefaultCaret to deal with.
			super.mouseClicked(mine);
		}
	}
}
