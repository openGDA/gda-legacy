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

import java.util.HashMap;

/**
 *
 */
public class TableRowBean 
{
	/**
	 * 
	 */
	public HashMap<String,String> values = new HashMap<String,String>();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TableRowBean other = (TableRowBean) obj;
		if (values == null) {
			if (other.values != null) {
				return false;
			}
		} else if( values.size() != other.values.size())
			return false;
		else {
			for(String s : values.keySet()){
				if( !other.values.containsKey(s))
					return false;
				if( !values.get(s).equals(other.values.get(s)))
					return false;
			}
		}
		return true;
	}
	
//	public String selectorId, folder, prefix, sampleId, sample, sample_protein, sample_code, phistart, step, phidelta,
//			numimages, imagetime, resolution, sd_dist, wavelength, energy, run_number, first_image_number, is_snap,
//			passes, comment, transmission, beamsize_horz, beamsize_vert;
//


}
