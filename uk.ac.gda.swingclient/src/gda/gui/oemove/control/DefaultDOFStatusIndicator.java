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

package gda.gui.oemove.control;

import gda.gui.oemove.DOFStatusIndicator;
import gda.oe.MoveableException;
import gda.oe.MoveableStatus;
import gda.oe.OE;

import java.awt.Color;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the default implementation of DOFStatusIndicator - the set of gear wheels with overwritten LIMIT or ERROR as
 * appropriate
 */
public class DefaultDOFStatusIndicator extends JLabel implements DOFStatusIndicator {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultDOFStatusIndicator.class);

	private String dofName;
	private ImageIcon idle;
	private ImageIcon moving;
	private ImageIcon limit;
	private ImageIcon error;

	/**
	 * @param oe
	 * @param dofName
	 */
	public DefaultDOFStatusIndicator(OE oe, String dofName) {
		idle = new ImageIcon(getResource("Icons/idle.gif"));
		moving = new ImageIcon(getResource("Icons/moving.gif"));
		limit = new ImageIcon(getResource("Icons/limit.gif"));
		error = new ImageIcon(getResource("Icons/error.gif"));

		int status = MoveableStatus.READY;
		try {
			if (oe != null) {
				MoveableStatus ms = oe.getStatus(dofName);
				status = ms.value();
			}
		} catch (MoveableException dex) {
			logger.error("DOFStatusIndicator, error getting " + dofName + " status");
		}

		this.dofName = dofName;
		if (oe != null)
			oe.addIObserver(this); //FIXME: potential race condition

		displayStatusIcon(status);

		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Status", TitledBorder.CENTER,
				TitledBorder.TOP, null, Color.black));

	}

	/*
	 * This method returns the URL of the specified file. It should be used when the required file is at the same
	 * directory hierachy level as this class such that the file would have to be specified with ../somedirectory/file.
	 * This is only required when addressing resource in this way in a jar file. It assumes the presence of an interface
	 * class in one directroy level down.
	 */
	private URL getResource(String file) {
		URL url = null;
		Class<?>[] classes = getClass().getInterfaces();
		for (Class<?> cls : classes) {
			url = cls.getResource(file);
			if (url != null) {
				break;
			}
		}
		return url;
	}

	@Override
	public void update(Object iObservable, Object arg) {
		if (arg instanceof MoveableStatus) {
			MoveableStatus ds = ((MoveableStatus) arg);
			if (ds.getMoveableName().equals(dofName)) {
				displayStatusIcon(ds.value());
			}
		}
	}

	private void displayStatusIcon(int status) {
		if (status == MoveableStatus.BUSY && moving != null) {
			setIcon(moving);
		} else if (status == MoveableStatus.ERROR && error != null) {
			setIcon(error);
		} else if ((status == MoveableStatus.UPPERLIMIT || status == MoveableStatus.LOWERLIMIT || status == MoveableStatus.AWAY_FROM_LIMIT)
				&& limit != null) {
			setIcon(limit);
		} else if (idle != null) {
			setIcon(idle);
		}
	}
}