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

package gda.oe.corba.impl;

import gda.factory.Findable;
import gda.factory.corba.util.EventService;
import gda.factory.corba.util.EventSubscriber;
import gda.factory.corba.util.NameFilter;
import gda.factory.corba.util.NetService;
import gda.factory.corba.util.RbacEnabledAdapter;
import gda.lockable.Locker;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.oe.MoveableException;
import gda.oe.MoveableStatus;
import gda.oe.OE;
import gda.oe.corba.CorbaLocker;
import gda.oe.corba.CorbaOE;
import gda.oe.corba.CorbaOEHelper;
import gda.oe.corba.CorbaQuantity;
import gda.oe.corba.CorbaUnit;
import gda.oe.dofs.corba.CorbaMoveableException;
import gda.oe.dofs.corba.CorbaMoveableStatus;
import gda.util.QuantityFactory;

import java.util.ArrayList;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the OE class
 */
public class OeAdapter implements OE, EventSubscriber, Findable, RbacEnabledAdapter {
	
	private CorbaOE corbaOE;

	private NetService netService;

	private String name;

	private ObservableComponent observableComponent = new ObservableComponent();

	/**
	 * Create client side interface to the CORBA package.
	 * 
	 * @param obj
	 *            the CORBA object
	 * @param name
	 *            the name of the object
	 * @param netService
	 *            the CORBA naming service
	 */
	public OeAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		corbaOE = CorbaOEHelper.narrow(obj);
		this.netService = netService;
		this.name = name;

		EventService.getInstance().subscribe(this, new NameFilter(name, observableComponent));
	}
	
	@Override
	public org.omg.CORBA.Object getCorbaObject() {
		return corbaOE;
	}
	
	@Override
	public NetService getNetService() {
		return netService;
	}
	
	@Override
	public void inform(Object obj) {
		notifyIObservers(this, obj);
	}

	@Override
	public void setName(String name) {
		// see bugzilla bug 443
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDOFType(String dofName) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaOE.getDOFType(dofName);
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE),
				"Communication failure: retry failed");
	}

	@Override
	public String[] getDOFNames() {
		return corbaOE.getDOFNames();
	}

	@Override
	public void moveBy(String dofname, Quantity increment) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaOE.moveBy(dofname, new CorbaQuantity(increment.toString()));
				return;
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE),
				"Communication failure: retry failed");
	}

	@Override
	public void moveTo(String dofname, Quantity position) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaOE.moveTo(dofname, new CorbaQuantity(position.toString()));
				return;
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE, dofname),
				"Communication failure: retry failed");
	}

	@Override
	public int moveLockedTo(String dofname, Quantity position, int lockId) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaOE.moveLockedTo(dofname, new CorbaQuantity(position.toString()), lockId);
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE),
				"Communication failure: retry failed");
	}

	@Override
	public void moveContinuously(String dofname, int direction) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaOE.moveContinuously(dofname, direction);
				return;
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE, dofname),
				"Communication failure: retry failed");
	}

	@Override
	public void stop(String dofname) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaOE.stop(dofname);
				return;
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE),
				"Communication failure: retry failed");
	}

	@Override
	public void stop() throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaOE.stop2();
				return;
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE),
				"Communication failure: retry failed");
	}

	@Override
	public boolean isMoving() throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaOE.isMoving();
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE),
				"Communication failure: retry failed");
	}

	@Override
	public boolean isMoving(String dofname) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaOE.isMoving2(dofname);
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE),
				"Communication failure: retry failed");
	}

	@Override
	public void setPosition(String dofname, Quantity position) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaOE.setPosition(dofname, new CorbaQuantity(position.toString()));
				return;
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE, dofname),
				"Communication failure: retry failed");
	}

	@Override
	public Quantity getPosition(String dofname) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return Quantity.valueOf(corbaOE.getPosition(dofname).valueString);
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE, dofname),
				"Communication failure: retry failed");
	}

	@Override
	public Quantity getPosition(String dofname, Unit<? extends Quantity> units) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				String s = units.toString();
				CorbaQuantity cq = corbaOE.getPosition2(dofname, new CorbaUnit(s));
				Quantity q = Quantity.valueOf(cq.valueString);
				return q;
				// return Quantity.valueOf(corbaOE.getPosition2(
				// dofname, new CorbaUnit(units.toString())).valueString);
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE, dofname),
				"Communication failure: retry failed");
	}

	@Override
	public String formatPosition(String dofname, double position) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				String rtrn = corbaOE.formatPosition(dofname, position);
				return rtrn;
				// return corbaOE.formatPosition(dofname, position);
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE),
				"Communication failure: retry failed");
	}

	@Override
	public boolean isPositionValid(String dofname) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaOE.isPositionValid(dofname);
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE, dofname),
				"Communication failure: retry failed");
	}

	@Override
	public boolean isSpeedLevelSettable(String dofname) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaOE.isSpeedLevelSettable(dofname);
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE, dofname),
				"Communication failure: retry failed");
	}

	@Override
	public ArrayList<Unit<? extends Quantity>> getAcceptableUnits(String dofname) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				// FIXME this does not seem to work for µRad the UTF-8ness might get lost in
				// the CORBA transition (µ transforms to 1/4)
				CorbaUnit[] ua = corbaOE.getAcceptableUnits(dofname);
				ArrayList<Unit<? extends Quantity>> units = new ArrayList<Unit<? extends Quantity>>();
				for (i = 0; i < ua.length; i++) {
					units.add(QuantityFactory.createUnitFromString(ua[i].unitString));
				}

				return units;
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE, dofname),
				"Communication failure: retry failed");
	}

	@Override
	public void setReportingUnits(String dofname, Unit<? extends Quantity> units) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaOE.setReportingUnits(dofname, new CorbaUnit(units.toString()));
				return;
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE, dofname),
				"Communication failure: retry failed");
	}

	@Override
	public Unit<? extends Quantity> getReportingUnits(String dofname) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return QuantityFactory.createUnitFromString(corbaOE.getReportingUnits(dofname).unitString);
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE, dofname),
				"Communication failure: retry failed");
	}

	@Override
	public int getProtectionLevel(String dofname) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaOE.getProtectionLevel(dofname);
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE, dofname),
				"Communication failure: retry failed");
	}

	@Override
	public MoveableStatus getStatus(String dofname) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMoveableStatusConvert(corbaOE.getStatus(dofname));
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE, dofname),
				"Communication failure: retry failed");
	}

	@Override
	public void setSpeed(String dofname, Quantity speed) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaOE.setSpeed(dofname, new CorbaQuantity(speed.toString()));
				return;
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE, dofname),
				"Communication failure: retry failed");
	}

	@Override
	public Quantity getSpeed(String dofname) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return Quantity.valueOf(corbaOE.getSpeed(dofname).valueString);
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE, dofname),
				"Communication failure: retry failed");
	}

	@Override
	public void setSpeedLevel(String dofname, int speedLevel) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaOE.setSpeedLevel(dofname, speedLevel);
				return;
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE, dofname),
				"Communication failure: retry failed");
	}

	@Override
	public void home(String dofname) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaOE.home(dofname);
				return;
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE, dofname),
				"Communication failure: retry failed");
	}

	@Override
	public Quantity getHomeOffset(String dofname) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return Quantity.valueOf(corbaOE.getHomeOffset(dofname).valueString);

			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE),
				"Communication failure: retry failed");
	}

	@Override
	public void setPositionOffset(String dofname, Quantity offset) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaOE.setPositionOffset(dofname, new CorbaQuantity(offset.toString()));
				return;
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE, dofname),
				"Communication failure: retry failed");
	}

	@Override
	public Quantity getPositionOffset(String dofname) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return Quantity.valueOf(corbaOE.getPositionOffset(dofname).valueString);
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE),
				"Communication failure: retry failed");
	}

	@Override
	public void setHomeOffset(String dofname, Quantity offset) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaOE.setHomeOffset(dofname, new CorbaQuantity(offset.toString()));
				return;
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE, dofname),
				"Communication failure: retry failed");
	}

	@Override
	public int lock(String dofname) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaOE.lock(dofname);
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE, dofname),
				"Communication failure: retry failed");
	}

	@Override
	public int lock(String dofname, Locker locker) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaOE.lock2(dofname, lockerConvert(locker));
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE, dofname),
				"Communication failure: retry failed");
	}

	@Override
	public void unlock(String dofname, int lockId) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaOE.unlock(dofname, lockId);
				return;
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE, dofname),
				"Communication failure: retry failed");
	}

	@Override
	public int moveCheck(String dofname, Quantity position) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaOE.moveCheck(dofname, new CorbaQuantity(position.toString()));
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE, dofname),
				"Communication failure: retry failed");
	}

	@Override
	public boolean isScannable(String dofname) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaOE.isScannable(dofname);
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE, dofname),
				"Communication failure: retry failed");
	}

	@Override
	public void refresh(String dofname) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaOE.refresh(dofname);
				return;
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE, dofname),
				"Communication failure: retry failed");
	}

	private CorbaLocker lockerConvert(Locker locker) {
		return new CorbaLocker(locker.getId());
	}

	private MoveableStatus corbaMoveableStatusConvert(CorbaMoveableStatus ods) {
		String positionString = ods.position.valueString;

		if (positionString.equals(""))
			return new MoveableStatus(ods.value.value(), ods.moveableName, null, ods.id, ods.message);

		Quantity q = Quantity.valueOf(positionString);
		return new MoveableStatus(ods.value.value(), ods.moveableName, q, ods.id, ods.message);
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
	 * Notify all observers on the list of the requested change.
	 * 
	 * @param theObserved
	 *            the observed component
	 * @param changeCode
	 *            the data requested by the observer.
	 */
	public void notifyIObservers(java.lang.Object theObserved, java.lang.Object changeCode) {
		observableComponent.notifyIObservers(theObserved, changeCode);
	}

	@Override
	public Quantity getSoftLimitLower(String dofname) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return Quantity.valueOf(corbaOE.getSoftLimitLower(dofname).valueString);
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE),
				"Communication failure: retry failed");
	}

	@Override
	public Quantity getSoftLimitUpper(String dofname) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return Quantity.valueOf(corbaOE.getSoftLimitUpper(dofname).valueString);
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE),
				"Communication failure: retry failed");
	}

	@Override
	public void setSpeed(String dofname, Quantity start, Quantity end, Quantity time) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaOE.setSpeed2(dofname, new CorbaQuantity(start.toString()), new CorbaQuantity(end.toString()),
						new CorbaQuantity(time.toString()));
				return;
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
	}

	@Override
	public void pushSpeed(String dofname) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaOE.pushSpeed(dofname);
				return;
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
	}

	@Override
	public void popSpeed(String dofname) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaOE.popSpeed(dofname);
				return;
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
	}

	@Override
	public String[] getMoveableNames(String name) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaOE.getMoveableNames(name);
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE),
				"Communication failure: retry failed");

	}

	@Override
	public Object getDeviceAttribute(String dofname, String name) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				org.omg.CORBA.Any any = corbaOE.getDeviceAttribute(dofname, name);
				return any.extract_Value();
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE),
				"Communication failure: retry failed");
	}

	@Override
	public void setDeviceAttribute(String dofname, String name, Object value) throws MoveableException {
		try {
			corbaOE.setDeviceAttribute(dofname, name, (org.omg.CORBA.Object) value);
		} catch (COMM_FAILURE cf) {
			corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
		} catch (TRANSIENT ct) {
			corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
		} catch (CorbaMoveableException cme) {
			throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
		}
	}


	@Override
	public String getDocString(String dofname) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaOE.getDocString(dofname);
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE),
				"Communication failure: retry failed");
	}

	@Override
	public void setDocString(String dofname, String docString)
			throws MoveableException {
		try {
			corbaOE.setDocString(dofname, docString);
		} catch (COMM_FAILURE cf) {
			corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
		} catch (TRANSIENT ct) {
			corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
		} catch (CorbaMoveableException cme) {
			throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
		}
	}

	@Override
	public double[] getLowerGdaLimits(String dofname) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaOE.getLowerGdaLimits(dofname);
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE),
				"Communication failure: retry failed");
	}

	@Override
	public double[] getTolerance(String dofname) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaOE.getTolerance(dofname);
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE),
				"Communication failure: retry failed");
	}

	@Override
	public double[] getUpperGdaLimits(String dofname) throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaOE.getUpperGdaLimits(dofname);
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE),
				"Communication failure: retry failed");
	}

	@Override
	public void setLowerGdaLimits(String dofname, double[] lowerLim)
			throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaOE.setLowerGdaLimits(dofname,lowerLim);
				return;
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE),
				"Communication failure: retry failed");
	}

	@Override
	public void setLowerGdaLimits(String dofname, double lowerLim)
			throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaOE.setLowerGdaLimits2(dofname,lowerLim);
				return;
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE),
				"Communication failure: retry failed");

		
	}

	@Override
	public void setTolerance(String dofname, double[] tolerance)
			throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaOE.setTolerance(dofname,tolerance);
				return;
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE),
				"Communication failure: retry failed");
	}

	@Override
	public void setTolerance(String dofname, double tolerance)
			throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaOE.setTolerance2(dofname,tolerance);
				return;
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE),
				"Communication failure: retry failed");
	}

	@Override
	public void setUpperGdaLimits(String dofname, double[] upperLim)
			throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaOE.setUpperGdaLimits(dofname,upperLim);
				return;
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE),
				"Communication failure: retry failed");
	}

	@Override
	public void setUpperGdaLimits(String dofname, double upperLim)
			throws MoveableException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaOE.setUpperGdaLimits2(dofname,upperLim);
				return;
			} catch (COMM_FAILURE cf) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaOE = CorbaOEHelper.narrow(netService.reconnect(name));
			} catch (CorbaMoveableException cme) {
				throw new MoveableException(corbaMoveableStatusConvert(cme.status), cme.message);
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.COMMUNICATION_FAILURE),
				"Communication failure: retry failed");
	}
}
