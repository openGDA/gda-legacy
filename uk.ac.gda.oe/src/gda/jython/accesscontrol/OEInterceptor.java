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

package gda.jython.accesscontrol;

import gda.factory.corba.util.NetService;
import gda.jython.JythonServerFacade;
import gda.jython.JythonServer.JythonServerThread;
import gda.oe.OE;
import gda.oe.corba.impl.OeAdapter;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * Wrapper for OE objects so that method calls may be intercepted and tested for authorisation.
 */
public class OEInterceptor implements MethodInterceptor {
	private OE theObject;
	private ProtectedMethodComponent protectedMethods;

	/**
	 * Factory method to create an OEAdapter wrapped in a CGLIB proxy.
	 * 
	 * @param theOE
	 * @param theCorbaDevice
	 * @param name
	 * @param netService
	 * @return the wrapped object
	 */
	public static OE newOEAdapterInstance(OeAdapter theOE, org.omg.CORBA.Object theCorbaDevice, String name,
			NetService netService) {

		// create the object which will do the work when the proxied object is
		// called
		MethodInterceptor interceptor = new OEInterceptor(theOE);

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(theOE.getClass());
		enhancer.setCallback(interceptor);

		// As we are creating OEAdapters we know that they all have the same form of constructor:
		// org.omg.CORBA.Object obj, String name, NetService netService

		Object[] args = new Object[] { theCorbaDevice, name, netService };
		Class<?>[] parameterTypes = new Class<?>[] { org.omg.CORBA.Object.class, String.class, NetService.class };

		Object proxyObject = enhancer.create(parameterTypes, args);

		((Factory) proxyObject).setCallback(0, interceptor);

		return (OE) proxyObject;

	}

	/**
	 * Factory method to create an OE wrapped in a CGLIB proxy.
	 * 
	 * @param theOE
	 * @return the wrapped OE
	 */
	public static OE newOEInstance(OE theOE) {
		// create the object which will do the work when the proxied object is
		// called
		MethodInterceptor interceptor = new OEInterceptor(theOE);

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(theOE.getClass());
		enhancer.setCallback(interceptor);
		// create using the null constructor. We know that this is safe as the object could not be called by Castor
		// otherwise.
		Object proxyObject = enhancer.create();

		((Factory) proxyObject).setCallback(0, interceptor);

		return (OE) proxyObject;
	}

	/**
	 * Constructor
	 * 
	 * @param obj
	 */
	public OEInterceptor(OE obj) {
		theObject = obj;
		this.protectedMethods = new ProtectedMethodComponent(obj.getClass());
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		if (this.protectedMethods.isAnInvokeMethod(method)) {
			// most of time we are jython and are simply calling invoke...
			return this.protectedMethods.testForJythonInvocation(obj, method, args);
		}

		// if it is the equals method, then we want to call the interceptor's equals instead (for example when looking
		// in arrays of objects)
		if (method.getName().equals("equals") && method.getParameterTypes().length == 1
				&& method.getParameterTypes()[0] == java.lang.Object.class) {
			return this.equals(args[0]);
		}
		// __tojava__ is called by Jython code at various points e.g. pos scannable value
		if (method.getName().equals("__tojava__") && method.getParameterTypes().length == 1) {
			return obj;
		}

		// if get here then we are directly calling the objects methods

		// let's not be limited by protected modifier!
		method.setAccessible(true);
		// check if we are in a thread from the command server and the method is protected
		if (isReleventMethodCall(method, args)) {
			return callProtectedMethodInJythonServerThread(method, args);
		} else if (isReleventAdapterMethodCall(method, args)) {
			return callProtectedMethodUsingJSF(method, args);
		}
		// else simply call the method
		return InterceptorUtils.invokeMethod(method, theObject, args);
	}

	private boolean isReleventMethodCall(Method method, Object[] args) {
		return this.protectedMethods.isMethodProtected(method) && Thread.currentThread() instanceof JythonServerThread
				&& method.getParameterTypes()[0] == String.class && args[0] instanceof String;
	}

	private Object callProtectedMethodInJythonServerThread(Method method, Object[] args) throws Throwable {
		JythonServerThread currentThread = (JythonServerThread) Thread.currentThread();
		// identify the protection level of the given DOF
		// *** assume that the first arg of a protected method is the name of the dof ***
		String dofname = (String) args[0];

		if (currentThread.hasBeenAuthorised
				|| currentThread.authorisationLevel >= theObject.getProtectionLevel(dofname)) {
			return InterceptorUtils.invokeMethod(method, theObject, args);
		}
		if (currentThread.authorisationLevel == 0) {
			throw new AccessDeniedException(AccessDeniedException.NOBATON_EXCEPTION_MESSAGE);
		}
		throw new AccessDeniedException("You need a permission level of " + theObject.getProtectionLevel(dofname)
				+ " to perform this operation. Your current level is " + currentThread.authorisationLevel + ".");
	}

	private boolean isReleventAdapterMethodCall(Method method, Object[] args) {
		return this.protectedMethods.isMethodProtected(method) && theObject instanceof OeAdapter
				&& !(Thread.currentThread() instanceof JythonServerThread)
				&& method.getParameterTypes()[0] == String.class && args[0] instanceof String;
	}

	private Object callProtectedMethodUsingJSF(Method method, Object[] args) throws Throwable {
		String dofname = (String) args[0];
		if (JythonServerFacade.getInstance().getAuthorisationLevel() >= theObject.getProtectionLevel(dofname)) {
			return InterceptorUtils.invokeMethod(method, theObject, args);
		} else if (JythonServerFacade.getInstance().getAuthorisationLevel() == 0) {
			throw new AccessDeniedException(AccessDeniedException.NOBATON_EXCEPTION_MESSAGE);
		}
		throw new AccessDeniedException("You need a permission level of " + theObject.getProtectionLevel(dofname)
				+ " to perform this operation. Your current level is "
				+ JythonServerFacade.getInstance().getAuthorisationLevel() + ".");
	}

	@Override
	public boolean equals(Object obj) {
		if (theObject instanceof OeAdapter) {
			return this.theObject.equals(obj);
		}

		if (obj == null) {
			return false;
		}
		if (!(obj instanceof net.sf.cglib.proxy.Factory)) {
			return false;
		}

		net.sf.cglib.proxy.Factory other = (net.sf.cglib.proxy.Factory) obj;
		net.sf.cglib.proxy.Callback callback = other.getCallback(0);
		if (!(callback instanceof OEInterceptor)) {
			return false;
		}

		OEInterceptor otherInterceptor = (OEInterceptor) callback;
		return theObject.equals(otherInterceptor.theObject);
	}

	@Override
	public int hashCode() {
		// base on wrapped object
		return theObject.hashCode();
	}
}
