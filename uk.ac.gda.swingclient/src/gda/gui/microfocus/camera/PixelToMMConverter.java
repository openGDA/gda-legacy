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

package gda.gui.microfocus.camera;

import java.io.IOException;

import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.gui.Tidyable;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.util.persistence.LocalParameters;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.nfunk.jep.JEP;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 *
 */
public class PixelToMMConverter implements Findable, Configurable, Tidyable, IObservable {	

	/**
	 * @param name 
	 * @param expression 
	 * @param pixelReference
	 * @param mmPerPixel
	 * @param mmOffset
	 */
	public PixelToMMConverter(String name, String expression, int pixelReference, double mmPerPixel,
			double mmOffset) {
		this();
		this.name = name;
		this.expression = expression;
		this.pixelReference = pixelReference;
		this.mmPerPixel = mmPerPixel;
		this.mmOffset = mmOffset;
	}

	/**
	 * @return expression
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * @param expression
	 */
	public void setExpression(String expression) {
		this.expression = expression;
	}

	int pixelReference;
	/**
	 * @return pixelReference
	 */
	public int getPixelReference() {
		return pixelReference;
	}

	/**
	 * @param pixelReference
	 */
	public void setPixelReference(int pixelReference) {
		this.pixelReference = pixelReference;
		logger.info("The pixel reference is " + pixelReference);
	}

	/**
	 * @return mmPerPixel
	 */
	public double getMmPerPixel() {
		return mmPerPixel;
	}

	/**
	 * @param mmPerPixel
	 */
	public void setMmPerPixel(double mmPerPixel) {
		this.mmPerPixel = mmPerPixel;
		logger.info("The mm per Pixel is " + mmPerPixel);
	}

	/**
	 * @return mmOffset
	 */ 
	public double getMmOffset() {
		return mmOffset;
	}

	/**
	 * @param mmOffset
	 */
	public void setMmOffset(double mmOffset) {
		this.mmOffset = mmOffset;
		logger.info("The mm offset is " + mmOffset);
	}

	private double mmPerPixel;
	private String mmPerPixelElement = "PixelToMMConverter.mmPerPixel";
	private double mmOffset;
	private String mmOffsetElement = "PixelToMMConverter.mmOffset";
	private String pixelReferenceElement = "PixelToMMConverter.pixelReference";
	private String nameElement = "PixelToMMConverter.name";
	private String name;
	private String expression = "(PIXELREF - PIXEL) * MMPERPIXEL + MMOFFSET";
	private String expressionElement = "PixelToMMConverter.expression";
	private JEP jep;
	private ObservableComponent observableComponent = new ObservableComponent();
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	private Logger logger = LoggerFactory.getLogger(PixelToMMConverter.class);
	private FileConfiguration config;
	
	/**
	 * 
	 */
	public PixelToMMConverter() {
		jep = new JEP();
		jep.addStandardConstants();
		jep.addStandardFunctions();
		jep.setImplicitMul(true);
	}

	/**
	 * @param pixel 
	 * @return double[]
	 */
	public double getConversion(int pixel) {
		jep.addVariable("PIXELREF", this.pixelReference);
		jep.addVariable("PIXEL", pixel);
		jep.addVariable("MMOFFSET",this.mmOffset);
		jep.addVariable("MMPERPIXEL", this.mmPerPixel);
		Node n1=null;
		double mmPosition=0.0;
		try {
			n1 = jep.parse(this.expression);
			mmPosition= (Double)jep.evaluate(n1);
		} catch (ParseException e) {
			logger.error("Error converting pixel to mm value", e);
		}
		logger.info("The mm position for "+pixel+ " is" + mmPosition  );
		return mmPosition;
	}

	@Override
	public void configure() throws FactoryException {
		try {
			config = LocalParameters.getXMLConfiguration(name);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.load();
		
	}
	
	/**
	 * Loads this converter's values.
	 */
	public void load() {
		this.expression = ((String) config.getProperty(expressionElement));
		this.pixelReference =Integer.parseInt((String) config.getProperty(pixelReferenceElement));
		this.mmPerPixel = Double.parseDouble((String)config.getProperty(mmPerPixelElement));
		this.mmOffset = Double.parseDouble((String)config.getProperty(mmOffsetElement));
		logger.info("Loaded values " + "EX= " + this.expression + "PR =" +pixelReference + " MMPixel="+mmPerPixel +" MMOffset="+mmOffset);
	}
	

	/**
	 * Reloads this converter's values.
	 */
	public void reload()
	{
		config.reload();
		this.load();
	}

	@Override
	public void tidyup() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param x
	 * @param x2
	 */
	public void notifyListeners(int x, int x2) {
		int minX = x< x2 ? x : x2;
		int maxX = x> x2 ? x :x2;
		double[]mmX = {getConversion(minX), getConversion(maxX)};
		observableComponent.notifyIObservers(this.name, mmX);
		
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

	/**
	 *  Save the parameters to a file
	 */
	public void save() {
		try {
			
			config.setProperty(nameElement, name);
			config.setProperty(expressionElement, expression);
			config.setProperty(pixelReferenceElement, this.pixelReference);
			config.setProperty(mmPerPixelElement, this.mmPerPixel);
			config.setProperty(mmOffsetElement, this.mmOffset);
			config.save();
			
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


}
