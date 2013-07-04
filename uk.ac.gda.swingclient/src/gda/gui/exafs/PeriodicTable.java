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

package gda.gui.exafs;

import gda.observable.IObservableJPanel;
import gda.observable.IObserver;
import gda.util.exafs.Element;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General Periodic Table class. Manages a set of ElementRenderers according to a PTLayout.
 */
public class PeriodicTable extends IObservableJPanel implements IObserver {
	
	private static final Logger logger = LoggerFactory.getLogger(PeriodicTable.class);
	
	/**
	 * Constructor
	 */
	public PeriodicTable() {
		GridBagConstraints gbc = new GridBagConstraints();
		ArrayList<ElementRenderer> elementRenderers = new ArrayList<ElementRenderer>();
		PTLayout ptLayout;
		Dimension dimension;
		int maxWidthOrHeight = 0;

		setLayout(new GridBagLayout());

		/* This fill value makes all the Renderers expand to */
		/* fill their cells. */
		gbc.fill = GridBagConstraints.BOTH;

		/* These values will make the cells expand when the */
		/* enclosing component expands */

		gbc.weightx = 0.5;
		gbc.weighty = 0.5;

		/* The ptLayout supplies gridX and gridY values for */
		/* each element */

		ptLayout = createPTLayout();

		/* Because the GridBagLayout seems to provide no way */
		/* to specify that all the cells should be the same */
		/* size we have to have two loops and a temporary */
		/* ArrayList<ElementRenderer> of ElementRenderers. */

		/* The first loop creates an ElementRenderer for each */
		/* element and adds it to the ArrayList. It also */
		/* calculates the maximum width and height. */
		for (Element element : Element.getAllElements()) {
			if (ptLayout.includeElement(element)) {
				ElementRenderer elementRenderer = createElementRenderer(element);
				elementRenderers.add(elementRenderer);
				dimension = elementRenderer.getPreferredSize();

				/* Since the ElementRenderers are going to be */
				/* made square we only need one maximum dimension. */

				if (dimension.width > maxWidthOrHeight)
					maxWidthOrHeight = dimension.width;
				if (dimension.height > maxWidthOrHeight)
					maxWidthOrHeight = dimension.height;
			}
		}

		Dimension maxDimension = new Dimension(maxWidthOrHeight, maxWidthOrHeight);

		/* The second loop runs through the ElementRenderers */
		/* setting the minimumSize of each to the size of the */
		/* largest, getting its position in the GridBag from */
		/* ptLayout and adding it to the PeriodicTable. */
		/* The Periodic Table also becomes an IObserver of */
		/* each elementRenderer. */
		for (ElementRenderer elementRenderer : elementRenderers) {
			/* The GridBagLayout will take notice of this if */
			/* it is allowed itself to be its preferred size */
			elementRenderer.setPreferredSize(maxDimension);

			Element element = elementRenderer.getElement();

			gbc.gridy = ptLayout.getGridY(element);
			gbc.gridx = ptLayout.getGridX(element);
			add(elementRenderer, gbc);

			elementRenderer.addIObserver(this);
		}

	}

	/**
	 * FactoryMethod to create a PTLayout. Subclasses can override this to provide their own PTLayouts (which is why it
	 * is protected rather than private). This one creates a StandardPTLayout.
	 * 
	 * @return the periodic table layout
	 */
	protected PTLayout createPTLayout() {
		return new StandardPTLayout();
	}

	/**
	 * FactoryMethod to create an ElementRenderer. Subclasses can override this to provide their own ElementRenderers
	 * (which is why it is protected rather than private).
	 * 
	 * @param element
	 *            the Element for which an ElementRenderer is to be supplied
	 * @return the ElementRenderer
	 */
	protected ElementRenderer createElementRenderer(Element element) {
		return new DefaultElementRenderer(element);
	}

	/**
	 * Called by notifyIObservers of the ElementRenderers.
	 * 
	 * @param iObservable
	 *            the IObservable calling the method
	 * @param argument
	 *            the argument specified
	 */
	@Override
	public void update(Object iObservable, Object argument) {
		logger.debug("PeriodicTable update: " + iObservable + " " + argument);

		/* Pass on the message to IObservers of this */
		notifyIObservers(this, argument);
	}
}
