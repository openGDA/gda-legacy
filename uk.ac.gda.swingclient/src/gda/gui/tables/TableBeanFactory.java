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

package gda.gui.tables;


import java.io.StringReader;
import java.io.StringWriter;

import org.xml.sax.InputSource;

import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 *
 */
public class TableBeanFactory {
	/**
	 * @param xml
	 * @return ExptTableBean
	 * @throws Exception
	 */
	public static TableRowBean getFromXML(String xml) throws Exception
	{
		return (TableRowBean)XMLHelpers.createFromXML(null, TableRowBean.class, null, new InputSource( new StringReader(xml)));
	}
	
	/**
	 * @param bean
	 * @return String
	 * @throws Exception
	 */
	public static String toXml(TableRowBean bean) throws Exception{
		StringWriter s = new StringWriter();
		XMLHelpers.writeToXML(null, bean, s);
		return s.toString();
	}		

	/**
	 * @param xml
	 * @return ExptTableBean
	 * @throws Exception
	 */
	public static TableRowBeans getListFromXML(String xml) throws Exception
	{
		return (TableRowBeans) XMLHelpers.createFromXML(null, TableRowBeans.class , null, new InputSource( new StringReader(xml)));
	}

	/**
	 * @param beans
	 * @return String
	 * @throws Exception
	 */
	public static String toXml(TableRowBeans beans) throws Exception{
		StringWriter s = new StringWriter();
		XMLHelpers.writeToXML(null, beans, s);
		return s.toString();
	}		

	
	/**
	 * @param filename
	 * @return ExptTableBeans
	 * @throws Exception
	 */
	public static TableRowBeans getListFromFile(String filename) throws Exception
	{
		return (TableRowBeans) XMLHelpers.createFromXML(null, TableRowBeans.class , null, filename);
	}

	/**
	 * @param beans 
	 * @param filename
	 * @throws Exception
	 */
	public static void toFile(TableRowBeans beans, String filename) throws Exception{
		XMLHelpers.writeToXML(null, beans, filename);
	}		
	
}
