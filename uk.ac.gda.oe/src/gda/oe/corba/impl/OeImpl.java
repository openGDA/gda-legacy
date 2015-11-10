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

import gda.factory.corba.util.EventDispatcher;
import gda.factory.corba.util.EventService;
import gda.lockable.Locker;
import gda.observable.IObserver;
import gda.oe.MoveableException;
import gda.oe.MoveableStatus;
import gda.oe.OE;
import gda.oe.OEBase;
import gda.oe.corba.CorbaLocker;
import gda.oe.corba.CorbaOEPOA;
import gda.oe.corba.CorbaQuantity;
import gda.oe.corba.CorbaUnit;
import gda.oe.dofs.corba.CorbaMoveableException;
import gda.oe.dofs.corba.CorbaMoveableStatus;
import gda.oe.dofs.corba.CorbaMoveableStatusValue;
import gda.util.QuantityFactory;

import java.io.Serializable;
import java.util.ArrayList;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;

/**
 * A server side implementation for a distributed OE class
 */
public class OeImpl extends CorbaOEPOA implements IObserver {
	
	//
	// Private reference to implementation object
	//
	private OE oe;

	//
	// Private reference to POA
	//
	private org.omg.PortableServer.POA poa;

	private EventDispatcher dispatcher;

	private String name;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param oe
	 *            the OE implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public OeImpl(OE oe, org.omg.PortableServer.POA poa) {
		this.oe = oe;
		this.poa = poa;
		name = ((OEBase) oe).getName();

		dispatcher = EventService.getInstance().getEventDispatcher();
		oe.addIObserver(this); //FIXME: potential race condition
	}

	/**
	 * Get the implementation object
	 * 
	 * @return the OE implementation object
	 */
	public OE _delegate() {
		return oe;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param oe
	 *            set the OE implementation object
	 */
	public void _delegate(OE oe) {
		this.oe = oe;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public void update(java.lang.Object o, java.lang.Object arg) {
		dispatcher.publish(name, arg);
	}

	@Override
	public String getDOFType(String dofName) throws CorbaMoveableException {
		try {
			return oe.getDOFType(dofName);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public String[] getDOFNames() {
		return oe.getDOFNames();
	}

	@Override
	public void moveBy(String dofname, CorbaQuantity increment) throws CorbaMoveableException {
		try {
			oe.moveBy(dofname, Quantity.valueOf(increment.valueString));
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public void moveTo(String dofname, CorbaQuantity position) throws CorbaMoveableException {
		try {
			oe.moveTo(dofname, Quantity.valueOf(position.valueString));
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public int moveLockedTo(String dofname, CorbaQuantity position, int lockId) throws CorbaMoveableException {
		try {
			return oe.moveLockedTo(dofname, Quantity.valueOf(position.valueString), lockId);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public void moveContinuously(String dofname, int direction) throws CorbaMoveableException {
		try {
			oe.moveContinuously(dofname, direction);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public void stop(String dofname) throws CorbaMoveableException {
		try {
			oe.stop(dofname);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public void stop2() throws CorbaMoveableException {
		try {
			oe.stop();
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public boolean isMoving() throws CorbaMoveableException {
		try {
			return oe.isMoving();
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public boolean isMoving2(String dofname) throws CorbaMoveableException {
		try {
			return oe.isMoving(dofname);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public void setPosition(String dofname, CorbaQuantity position) throws CorbaMoveableException {
		try {
			oe.setPosition(dofname, Quantity.valueOf(position.valueString));
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public CorbaQuantity getPosition(String dofname) throws CorbaMoveableException {
		try {
			return new CorbaQuantity(oe.getPosition(dofname).toString());
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public CorbaQuantity getPosition2(String dofname, CorbaUnit units) throws CorbaMoveableException {
		try {
			Unit<? extends Quantity> unit = QuantityFactory.createUnitFromString(units.unitString);
			return new CorbaQuantity(oe.getPosition(dofname, unit).toString());
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public String formatPosition(String dofname, double position) throws CorbaMoveableException {
		try {
			return oe.formatPosition(dofname, position);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public boolean isPositionValid(String dofname) throws CorbaMoveableException {
		try {
			return oe.isPositionValid(dofname);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public boolean isSpeedLevelSettable(String dofname) throws CorbaMoveableException {
		try {
			return oe.isSpeedLevelSettable(dofname);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public CorbaUnit[] getAcceptableUnits(String dofname) throws CorbaMoveableException {
		try {
			ArrayList<Unit<? extends Quantity>> units = oe.getAcceptableUnits(dofname);
			CorbaUnit[] ou = new CorbaUnit[units.size()];
			for (int i = 0; i < units.size(); i++)
				ou[i] = new CorbaUnit(units.get(i).toString());

			return ou;
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public void setReportingUnits(String dofname, CorbaUnit units) throws CorbaMoveableException {
		try {
			Unit<? extends Quantity> unit = QuantityFactory.createUnitFromString(units.unitString);
			oe.setReportingUnits(dofname, unit);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public CorbaUnit getReportingUnits(String dofname) throws CorbaMoveableException {
		try {
			return new CorbaUnit(oe.getReportingUnits(dofname).toString());
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public int getProtectionLevel(String dofname) throws CorbaMoveableException {
		try {
			return oe.getProtectionLevel(dofname);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public CorbaMoveableStatus getStatus(String dofname) throws CorbaMoveableException {
		try {
			return dofStatusConvert(oe.getStatus(dofname));
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public void setSpeed(String dofname, CorbaQuantity speed) throws CorbaMoveableException {
		try {
			oe.setSpeed(dofname, Quantity.valueOf(speed.valueString));
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public CorbaQuantity getSpeed(String dofname) throws CorbaMoveableException {
		try {
			return new CorbaQuantity(oe.getSpeed(dofname).toString());
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public void setSpeedLevel(String dofname, int speedLevel) throws CorbaMoveableException {
		try {
			oe.setSpeedLevel(dofname, speedLevel);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public void home(String dofname) throws CorbaMoveableException {
		try {
			oe.home(dofname);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public CorbaQuantity getHomeOffset(String dofname) throws CorbaMoveableException {
		try {
			return new CorbaQuantity(oe.getHomeOffset(dofname).toString());
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public CorbaQuantity getPositionOffset(String dofname) throws CorbaMoveableException {
		try {
			return new CorbaQuantity(oe.getPositionOffset(dofname).toString());
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public void setHomeOffset(String dofname, CorbaQuantity offset) throws CorbaMoveableException {
		try {
			oe.setHomeOffset(dofname, Quantity.valueOf(offset.valueString));
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public void setPositionOffset(String dofname, CorbaQuantity offset) throws CorbaMoveableException {
		try {
			oe.setPositionOffset(dofname, Quantity.valueOf(offset.valueString));
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public void refresh(String dofname) throws CorbaMoveableException {
		try {
			oe.refresh(dofname);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public int lock(String dofname) throws CorbaMoveableException {
		try {
			return oe.lock(dofname);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public int lock2(String dofname, CorbaLocker locker) throws CorbaMoveableException {
		try {
			return oe.lock(dofname, corbaLockerConvert(locker));
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public void unlock(String dofname, int lockId) throws CorbaMoveableException {
		try {
			oe.unlock(dofname, lockId);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public int moveCheck(String dofname, CorbaQuantity position) throws CorbaMoveableException {
		try {
			return oe.moveCheck(dofname, Quantity.valueOf(position.valueString));
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public boolean isScannable(String dofname) throws CorbaMoveableException {
		try {
			return oe.isScannable(dofname);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	private Locker corbaLockerConvert(CorbaLocker locker) {
		return new Locker(locker.lockId);
	}

	private CorbaMoveableStatus dofStatusConvert(MoveableStatus ms) {
		CorbaQuantity oq = null;
		Quantity q = ms.getPosition();
		if (q != null)
			oq = new CorbaQuantity(q.toString());
		else
			oq = new CorbaQuantity("");

		return new CorbaMoveableStatus(CorbaMoveableStatusValue.from_int(ms.value()), ms.getMoveableName(), oq,
				ms.id(), ms.getMessage());
	}

	@Override
	public CorbaQuantity getSoftLimitLower(String dofname) throws CorbaMoveableException {
		try {
			return new CorbaQuantity(oe.getSoftLimitLower(dofname).toString());
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public CorbaQuantity getSoftLimitUpper(String dofname) throws CorbaMoveableException {
		try {
			return new CorbaQuantity(oe.getSoftLimitUpper(dofname).toString());
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public void setSpeed2(String moveableName, CorbaQuantity start, CorbaQuantity end, CorbaQuantity time)
			throws CorbaMoveableException {

		try {
			oe.setSpeed(moveableName, Quantity.valueOf(start.valueString), Quantity.valueOf(end.valueString), Quantity
					.valueOf(time.valueString));
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public void pushSpeed(String moveableName) throws CorbaMoveableException {
		try {
			oe.pushSpeed(moveableName);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public void popSpeed(String moveableName) throws CorbaMoveableException {
		try {
			oe.popSpeed(moveableName);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public String[] getMoveableNames(String moveableName) throws CorbaMoveableException {

		try {
			return oe.getMoveableNames(moveableName);

		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}

	}

	@Override
	public org.omg.CORBA.Any getDeviceAttribute(String dofname, String name) throws CorbaMoveableException {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		try {
			java.lang.Object value = oe.getDeviceAttribute(dofname, name);
			any.insert_Value((Serializable) value);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}

		return any;
	}

	@Override
	public void setDeviceAttribute(String dofname, String name, org.omg.CORBA.Object value)
			throws CorbaMoveableException {
		try {
			oe.setDeviceAttribute(dofname, name, value);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}
	
	
	/**
	 * @param dofname
	 * @return getter for the DocString
	 * @throws CorbaMoveableException
	 */
	@Override
	public String getDocString(String dofname) throws CorbaMoveableException {
		try {
			return oe.getDocString(dofname);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}
	
	
	/**
	 * @param dofname
	 * @param docString
	 * @throws CorbaMoveableException
	 */
	@Override
	public void setDocString(String dofname, String docString) throws CorbaMoveableException {
		try {
			oe.setDocString(dofname, docString);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public double[] getLowerGdaLimits(String arg0)
			throws CorbaMoveableException {
		try {
			return oe.getLowerGdaLimits(arg0);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public double[] getTolerance(String arg0) throws CorbaMoveableException {
		try {
			return oe.getTolerance(arg0);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public double[] getUpperGdaLimits(String arg0)
			throws CorbaMoveableException {
		try {
			return oe.getUpperGdaLimits(arg0);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public void setLowerGdaLimits(String arg0, double[] arg1)
			throws CorbaMoveableException {
		try {
			oe.setLowerGdaLimits(arg0,arg1);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public void setLowerGdaLimits2(String arg0, double arg1)
			throws CorbaMoveableException {
		try {
			oe.setLowerGdaLimits(arg0,arg1);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public void setTolerance(String arg0, double[] arg1)
			throws CorbaMoveableException {
		try {
			oe.setTolerance(arg0,arg1);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public void setTolerance2(String arg0, double arg1)
			throws CorbaMoveableException {
		try {
			oe.setTolerance(arg0,arg1);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public void setUpperGdaLimits(String arg0, double[] arg1)
			throws CorbaMoveableException {
		try {
			oe.setUpperGdaLimits(arg0,arg1);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}

	@Override
	public void setUpperGdaLimits2(String arg0, double arg1)
			throws CorbaMoveableException {
		try {
			oe.setUpperGdaLimits(arg0,arg1);
		} catch (MoveableException me) {
			throw new CorbaMoveableException(dofStatusConvert(me.getMoveableStatus()), me.getMessage());
		}
	}
	
	
}