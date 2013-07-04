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

package gda.images;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.filechooser.FileFilter;

/**
 * A filefilter which uses a given list of file extensions to filter on.
 */
public class ImageFileFilter extends FileFilter {

	private Hashtable<String, ImageFileFilter> filters = new Hashtable<String, ImageFileFilter>();

	private String description = null;

	/**
	 * Creates a file filter. If no filters are added, then all files are accepted.
	 * 
	 * @see #addExtension
	 */
	public ImageFileFilter() {
	}

	/**
	 * Return true if this file should be shown in the directory pane, false if it shouldn't. Files that begin with "."
	 * are ignored.
	 * 
	 * @param f
	 *            the file
	 * @return true if this file should be shown in the directory pane
	 * @see #getExtension
	 * @see FileFilter#accept
	 */
	@Override
	public boolean accept(File f) {
		if (f != null) {
			if (f.isDirectory()) {
				return true;
			}
			String extension = getExtension(f);
			if (extension != null && filters.get(getExtension(f)) != null) {
				return true;
			}
		}
		return false;
	}

	private String getExtension(File f) {
		if (f != null) {
			String filename = f.getName();
			int i = filename.lastIndexOf('.');
			if (i > 0 && i < filename.length() - 1) {
				return filename.substring(i + 1).toLowerCase();
			}
		}
		return null;
	}

	/**
	 * Adds a filetype "dot" extension to filter against. For example: the following code will create a filter that
	 * filters out all files except those that end in ".jpg" and ".tif": ExampleFileFilter filter = new
	 * ExampleFileFilter(); filter.addExtension("jpg"); filter.addExtension("tif"); Note that the "." before the
	 * extension is not needed and will be ignored.
	 * 
	 * @param extension
	 *            of file
	 */
	public void addExtension(String extension) {
		if (filters == null) {
			filters = new Hashtable<String, ImageFileFilter>(5);
		}
		filters.put(extension.toLowerCase(), this);
	}

	/**
	 * Returns the human readable description of this filter.
	 * 
	 * @return the full description of the filter
	 * @see #setDescription
	 * @see FileFilter#getDescription
	 */
	@Override
	public String getDescription() {
		if (description == null) {
			String output =  "(";
			// build the description from the extension list
			Enumeration<String> extensions = filters.keys();
			if (extensions != null) {
				output += "." + extensions.nextElement();
				while (extensions.hasMoreElements()) {
					output += ", " + extensions.nextElement();
				}
			}
			output += ")";
			return output;
		} 
		return description;
	}

	/**
	 * Sets the human readable description of this filter. For example: filter.setDescription("Gif and JPG Images");
	 * 
	 * @see #setDescription
	 * @param description
	 *            in human readable form
	 */
	public void setDescription(String description) {
		this.description = description;
	}

}