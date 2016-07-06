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

package gda.gui.dv.panels.vispanels;

import gda.gui.dv.ImageData;
import gda.gui.dv.panels.IMainPlotVisualiser;
import gda.gui.dv.panels.MainPlot;
import gda.gui.dv.panels.VisPanel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle;

import org.eclipse.january.dataset.DoubleDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A basic panel which fits in the side panel and provides colour information for the image manipulator.
 */
public class ColourSelectorBase extends VisPanel implements IMainPlotVisualiser, ActionListener {
	private static final Logger logger = LoggerFactory.getLogger(ColourSelectorBase.class);

	private JToggleButton[] toggles = null;

	private JComboBox[] boxes = null;

	private JComboBox mainSelect = null;

	private ACastModule[] f = new ACastModule[3];

	private Vector<FunctionDef> functions = new Vector<FunctionDef>();

	private Vector<Selection> selections = new Vector<Selection>();

	private GridBagConstraints c = new GridBagConstraints();

	/**
	 * set to true if multiple changes should be make at the same time
	 */
	private boolean batchChanging = false;

	/**
	 * Basic constructor which initialises the GUI
	 * 
	 * @param main
	 *            The MainPlot to be associated with this panel
	 */
	public ColourSelectorBase(MainPlot main) {
		super(main);

		this.setName("Colour Selector Base");

		popuplateFunctionsAndSelections();

		this.setLayout(new GridBagLayout());

		// construct the string list

		String[] choiceStrings = getFunctionNames();

		boxes = new JComboBox[3];

		boxes[0] = new JComboBox(choiceStrings);
		boxes[1] = new JComboBox(choiceStrings);
		boxes[2] = new JComboBox(choiceStrings);

		toggles = new JToggleButton[3];

		toggles[0] = new JToggleButton("Invert Red");
		toggles[1] = new JToggleButton("Invert Green");
		toggles[2] = new JToggleButton("Invert Blue");

		boxes[0].addActionListener(this);
		boxes[1].addActionListener(this);
		boxes[2].addActionListener(this);

		toggles[0].addActionListener(this);
		toggles[1].addActionListener(this);
		toggles[2].addActionListener(this);

		// checks[0].setHorizontalTextPosition(SwingConstants.LEFT);
		// checks[1].setHorizontalTextPosition(SwingConstants.LEFT);
		// checks[2].setHorizontalTextPosition(SwingConstants.LEFT);

		String[] selectStrings = getSelectionNames();

		mainSelect = new JComboBox(selectStrings);

		// c.insets.set(5, 5, 5, 5);
		//
		// c.gridx = 0;
		// c.gridy = 0;
		// c.gridwidth = 2;

		mainSelect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// when this is selected, the selected tag need to be spread out to all the others
				batchChanging = true;
				for (JToggleButton t : toggles)
					t.setSelected(false);
				boxes[0].setSelectedIndex(selections.get(mainSelect.getSelectedIndex()).getRedPosition());
				boxes[1].setSelectedIndex(selections.get(mainSelect.getSelectedIndex()).getGreenPosition());
				batchChanging = false;
				boxes[2].setSelectedIndex(selections.get(mainSelect.getSelectedIndex()).getBluePosition());
			}

		});

		JPanel jPSelector = new JPanel();
		GroupLayout jPSLayout = new GroupLayout(jPSelector);
		jPSelector.setLayout(jPSLayout);
		jPSLayout.setHorizontalGroup(jPSLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
				jPSLayout.createSequentialGroup().addContainerGap().addGroup(
						jPSLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
								jPSLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addComponent(
										mainSelect, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE).addGroup(
										jPSLayout.createSequentialGroup().addComponent(boxes[0],
												GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE).addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE).addComponent(toggles[0]))).addGroup(
								jPSLayout.createSequentialGroup().addComponent(boxes[1], GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE).addComponent(toggles[1])).addGroup(
								GroupLayout.Alignment.TRAILING,
								jPSLayout.createSequentialGroup().addComponent(boxes[2], GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE).addComponent(toggles[2]))).addContainerGap()));
		jPSLayout.setVerticalGroup(jPSLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
				jPSLayout.createSequentialGroup().addContainerGap(52, Short.MAX_VALUE).addComponent(mainSelect,
						GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGap(8, 8,
						8).addGroup(
						jPSLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(boxes[0],
								GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(toggles[0])).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(
								jPSLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(boxes[1],
										GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE).addComponent(toggles[1])).addPreferredGap(
								LayoutStyle.ComponentPlacement.RELATED).addGroup(
								jPSLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(boxes[2],
										GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE).addComponent(toggles[2]))));
		this.add(jPSelector, c);

		// this.add(mainSelect, c);
		//
		// c.gridx = 0;
		// c.gridy = 1;
		// c.gridwidth = 1;
		//
		// this.add(boxes[0], c);
		// c.gridx = 1;
		// c.gridy = 1;
		// this.add(toggles[0], c);
		// c.gridx = 0;
		// c.gridy = 2;
		// this.add(boxes[1], c);
		// c.gridx = 1;
		// c.gridy = 2;
		// this.add(toggles[1], c);
		// c.gridx = 0;
		// c.gridy = 3;
		// this.add(boxes[2], c);
		// c.gridx = 1;
		// c.gridy = 3;
		// this.add(toggles[2], c);

		// force the starting scheme upon the selectors.
		batchChanging = true;
		boxes[0].setSelectedIndex(selections.get(mainSelect.getSelectedIndex()).getRedPosition());
		boxes[1].setSelectedIndex(selections.get(mainSelect.getSelectedIndex()).getGreenPosition());
		batchChanging = false;
		boxes[2].setSelectedIndex(selections.get(mainSelect.getSelectedIndex()).getBluePosition());

		for (int i = 0; i < 3; i++) {
			f[i] = functions.get(boxes[i].getSelectedIndex()).getFunc();
		}

	}

	protected void popuplateFunctionsAndSelections() {
		// fill the function hashmap
		addFunction("0", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return 0;
			}

		});
		addFunction("0.5", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return 0.5;
			}

		});
		addFunction("1", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return 1;
			}

		});
		addFunction("x", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return invalue;
			}

		});
		addFunction("x^2", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return invalue * invalue;
			}

		});
		addFunction("x^3", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return invalue * invalue * invalue;
			}

		});
		addFunction("x^4", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return invalue * invalue * invalue * invalue;
			}

		});
		addFunction("sqrt(x)", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return Math.sqrt(invalue);
			}

		});
		addFunction("sqrt(sqrt(x))", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return Math.sqrt(Math.sqrt(invalue));
			}

		});
		addFunction("sin(x*90)", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return Math.sin(invalue * Math.PI * 0.5);
			}

		});
		addFunction("cos(x*90)", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return Math.cos(invalue * Math.PI * 0.5);
			}

		});
		addFunction("|x-0.5|", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return Math.abs(invalue - 0.5);
			}

		});
		addFunction("(2x-1)^2", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return Math.pow(2.0 * invalue - 1, 2.0);
			}

		});
		addFunction("sin(x*180)", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return Math.sin(invalue * Math.PI);
			}

		});
		addFunction("|cos(x*180)|", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return Math.abs(Math.cos(invalue * Math.PI));
			}

		});
		addFunction("sin(x*360)", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return Math.sin(invalue * Math.PI * 2.0);
			}

		});
		addFunction("cos(x*360)", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return Math.cos(invalue * Math.PI * 2.0);
			}

		});
		addFunction("|sin(x*360)|", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return Math.abs(Math.sin(invalue * Math.PI * 2.0));
			}

		});
		addFunction("|cos(x*360)|", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return Math.abs(Math.cos(invalue * Math.PI * 2.0));
			}

		});
		addFunction("|sin(x*720)|", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return Math.abs(Math.sin(invalue * Math.PI * 4.0));
			}

		});
		addFunction("|cos(x*720)|", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return Math.abs(Math.cos(invalue * Math.PI * 4.0));
			}

		});
		addFunction("3x", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return invalue * 3.0;
			}

		});
		addFunction("3x-1", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return invalue * 3.0 - 1.0;
			}

		});
		addFunction("3x-2", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return invalue * 3.0 - 2.0;
			}

		});
		addFunction("|3x-1|", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return Math.abs(invalue * 3.0 - 1.0);
			}

		});
		addFunction("|3x-2|", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return Math.abs(invalue * 3.0 - 2.0);
			}

		});
		addFunction("(3x-1)/2", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return (invalue * 3.0 - 1.0) / 2.0;
			}

		});
		addFunction("(3x-2)/2", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return (invalue * 3.0 - 2.0) / 2.0;
			}

		});
		addFunction("|(3x-1)/2|", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return Math.abs((invalue * 3.0 - 1.0) / 2.0);
			}

		});
		addFunction("|(3x-2)/2|", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return Math.abs((invalue * 3.0 - 2.0) / 2.0);
			}

		});
		addFunction("x/0.32-0.78125", new ACastModule() {

			@Override
			public double cast(double invalue) {
				if (invalue <= 0.25)
					return 0;
				if (invalue >= 0.57)
					return 1;
				return (invalue / 0.32) - 0.78125;

			}

		});
		addFunction("2*x-0.84", new ACastModule() {

			@Override
			public double cast(double invalue) {
				if (invalue <= 0.42)
					return 0;
				if (invalue >= 0.92)
					return 1;
				return (2.0 * invalue) - 0.84;

			}

		});
		addFunction("4x;1;-2x+1.84;x/0.08-11.5", new ACastModule() {

			@Override
			public double cast(double invalue) {
				if (invalue <= 0.42) {
					return invalue * 4;
				} else if (invalue <= 0.92) {
					return (invalue * -2.0) + 1.84;
				} else {
					return (invalue / 0.08) - 11.5;
				}
			}

		});
		addFunction("|2x-0.5|", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return Math.abs((2.0 * invalue) - 0.5);
			}

		});
		addFunction("2x", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return (2.0 * invalue);
			}

		});
		addFunction("2x-0.5", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return (2.0 * invalue) - 0.5;
			}

		});
		addFunction("2x-1", new ACastModule() {

			@Override
			public double cast(double invalue) {
				return (2.0 * invalue) - 1.0;
			}

		});

		addSelection("Traditional pm3d (black-blue-red-yellow)", "sqrt(x)", "x^3", "sin(x*360)");
		addSelection("green-red-violet", "x", "|x-0.5|", "x^4");
		addSelection("Ocean (green-blue-white)", "3x-2", "|(3x-1)/2|", "x");
		addSelection("Hot (black-red-yellow-white)", "3x", "3x-1", "3x-2");
		addSelection("Colour printable on gray (black-blue-violet-yellow-white)", "x/0.32-0.78125", "2*x-0.84",
				"4x;1;-2x+1.84;x/0.08-11.5");
		addSelection("Rainbow (blue-green-yellow-red)", "|2x-0.5|", "sin(x*180)", "cos(x*90)");
		addSelection("AFM hot (black-red-yellow-white)", "2x", "2x-0.5", "2x-1");
		addSelection("Grayscale (black-white)", "x", "x", "x");
		addSelection("Low Skewed Grayscale (black-white)", "sqrt(x)", "sqrt(x)", "sqrt(x)");
		addSelection("High Skewed Grayscale (black-white)", "x^2", "x^2", "x^2");
	}

	protected final String[] getSelectionNames() {
		String[] result = new String[selections.size()];
		for (int i = 0; i < selections.size(); i++) {
			result[i] = selections.get(i).getName();
		}
		return result;
	}

	protected final void addSelection(String name, String red, String green, String blue) {

		int redpos = 0;
		int bluepos = 0;
		int greenpos = 0;

		String[] choiceStrings = getFunctionNames();
		for (int i = 0; i < choiceStrings.length; i++) {
			// check to see if any of the red green or blue parts are the right ones
			if (red.equals(choiceStrings[i])) {
				redpos = i;
			}
			if (green.equals(choiceStrings[i])) {
				greenpos = i;
			}
			if (blue.equals(choiceStrings[i])) {
				bluepos = i;
			}
		}

		Selection selection = new Selection(name, redpos, greenpos, bluepos);

		selections.add(selection);

	}

	protected ImageData colourCast(DoubleDataset raw, double max, double min) {

		int[] pix = new int[raw.getShape()[0] * raw.getShape()[1]];

		logger.debug("min and max are: " + min + " " + max);

		double scale = (0.9999999 / ((max - min) == 0 ? 1 : (max - min)));

		double[] buffer = raw.getData();
		if (pix.length != buffer.length)
			throw new IllegalArgumentException("colourCast - pix.length != buffer.length");

		boolean check0 = toggles[0].isSelected();
		boolean check1 = toggles[1].isSelected();
		boolean check2 = toggles[2].isSelected();
		for (int k = 0; k < pix.length; k++) {

			// double value = ((raw.get(k) - min) * scale);
			double value = (buffer[k] - min) * scale;
			if (value >= 1.0) {
				value = 1.0;
			}
			if (value <= 0.0) {
				value = 0.0;
			}

			if (check0) {
				pix[k] = ((byte) ((f[2].boundedCast(1.0 - value) * 255.0)) & 0xff);
			} else {
				pix[k] = ((byte) ((f[2].boundedCast(value) * 255)) & 0xff);
			}

			if (check1) {
				pix[k] |= ((byte) ((f[1].boundedCast(1.0 - value) * 255.0)) & 0xff) << 8;
			} else {
				pix[k] |= ((byte) ((f[1].boundedCast(value) * 255)) & 0xff) << 8;
			}

			if (check2) {
				pix[k] |= ((byte) ((f[0].boundedCast(1.0 - value) * 255.0)) & 0xff) << 16;
			} else {
				pix[k] |= ((byte) ((f[0].boundedCast(value) * 255)) & 0xff) << 16;
			}

		}

		return (new ImageData(raw.getShape()[1], raw.getShape()[0], pix));

	}

	/**
	 * The function that performs the histogram Drawing
	 * 
	 * @param raw
	 *            the raw data
	 * @return the new data in the appropriate form
	 */
	@Override
	public ImageData cast(DoubleDataset raw) {
		return colourCast(raw, raw.max().doubleValue(), raw.min().doubleValue());
	}

	/**
	 * Simply overloads any change on the panel to update the main panel
	 * 
	 * @param arg0
	 *            The action
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {

		if (!batchChanging) {
			for (int i = 0; i < 3; i++) {
				f[i] = functions.get(boxes[i].getSelectedIndex()).getFunc();
			}
			this.owner.setVisualiser(this);
		}
	}

	protected final void addFunction(String name, ACastModule function) {
		functions.add(new FunctionDef(name, function));
	}

	protected final String[] getFunctionNames() {
		String[] result = new String[functions.size()];
		for (int i = 0; i < functions.size(); i++) {
			result[i] = functions.get(i).getTag();
		}
		return result;
	}

	private interface ICastModule {
		/**
		 * Add your function for a double to double mapping here. Valid range is [0,1]
		 * 
		 * @param invalue
		 * @return outvalue
		 */
		public abstract double cast(double invalue);
	}

	/**
	 * Abstract class to implement the colour cast mapping
	 */
	public abstract class ACastModule implements ICastModule {

		/**
		 * This is a wrapper to ensure the result is within limits
		 * 
		 * @param value
		 * @return cast
		 */
		public final double boundedCast(double value) {

			double invalue = this.cast(value);

			if (invalue < 0.0)
				return 0.0;
			if (invalue > 1.0)
				return 1.0;
			return invalue;
		}
	}

	private class FunctionDef {

		String tag = null;
		ACastModule func = null;

		/**
		 * Constructor
		 * 
		 * @param name
		 * @param function
		 */
		public FunctionDef(String name, ACastModule function) {
			tag = name;
			func = function;
		}

		/**
		 * getter
		 * 
		 * @return the function
		 */
		public ACastModule getFunc() {
			return func;
		}

		/**
		 * Setter
		 * 
		 * @param func
		 */
		public void setFunc(ACastModule func) {
			this.func = func;
		}

		/**
		 * Getter
		 * 
		 * @return the name of the function
		 */
		public String getTag() {
			return tag;
		}

		/**
		 * Setter
		 * 
		 * @param tag
		 */
		public void setTag(String tag) {
			this.tag = tag;
		}

	}

	private class Selection {
		private String name = "";
		private int redPosition = 0;
		private int greenPosition = 0;
		private int bluePosition = 0;

		/**
		 * Constructor
		 * 
		 * @param inName
		 * @param red
		 * @param green
		 * @param blue
		 */
		public Selection(String inName, int red, int green, int blue) {
			name = inName;
			redPosition = red;
			greenPosition = green;
			bluePosition = blue;
		}

		/**
		 * Getter
		 * 
		 * @return value
		 */
		public int getBluePosition() {
			return bluePosition;
		}

		/**
		 * Setter
		 * 
		 * @param bluePosition
		 */
		public void setBluePosition(int bluePosition) {
			this.bluePosition = bluePosition;
		}

		/**
		 * Getter
		 * 
		 * @return value
		 */
		public int getGreenPosition() {
			return greenPosition;
		}

		/**
		 * Setter
		 * 
		 * @param greenPosition
		 */
		public void setGreenPosition(int greenPosition) {
			this.greenPosition = greenPosition;
		}

		/**
		 * Getter
		 * 
		 * @return value
		 */
		public String getName() {
			return name;
		}

		/**
		 * Setter
		 * 
		 * @param name
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * Getter
		 * 
		 * @return value
		 */
		public int getRedPosition() {
			return redPosition;
		}

		/**
		 * Setter
		 * 
		 * @param redPosition
		 */
		public void setRedPosition(int redPosition) {
			this.redPosition = redPosition;
		}
	}
}