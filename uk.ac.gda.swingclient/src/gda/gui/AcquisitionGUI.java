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

package gda.gui;

import gda.configuration.properties.LocalProperties;
import gda.data.metadata.icat.IcatProvider;
import gda.device.DeviceException;
import gda.factory.Finder;
import gda.icons.GdaIcons;
import gda.jython.JythonServerFacade;
import gda.jython.authenticator.Authenticator;
import gda.jython.authenticator.UserAuthentication;
import gda.jython.authoriser.AuthoriserProvider;
import gda.util.MultiScreenSupport;
import gda.util.ObjectServer;
import gda.util.SplashScreen;
import gda.util.Version;
import gda.util.exceptionUtils;
import gda.util.findableHashtable.FindableHashtable;
import gda.util.logging.LogbackUtils;
import gda.util.logging.LoggingUtils;

import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.net.SimpleSocketServer;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * A class to implement a data acquisition graphical user interface.
 */
public class AcquisitionGUI {
	private static MultiScreenSupport mss = new MultiScreenSupport();

	private static int screenIndex;

	private static final Logger logger = LoggerFactory.getLogger(AcquisitionGUI.class);

	/**
	 * This GUI's acquisition frame.
	 */
	private AcquisitionFrame acquisitionFrame;

	/**
	 * The singleton GUI instance.
	 */
	private static AcquisitionGUI instance;

	private static SplashScreen splashScreen;

	private static String[] args = new String[] {};

	private static boolean showSplash;

	private static ObjectServer objectServer;
	
	// null unless xml file given on command line
	private static String commandLineXmlFile;

	/**
	 * Default client-side log server port to use if a port is not specified using the {@code gda.client.logging.port}
	 * property.
	 */
	private static final int DEFAULT_CLIENT_SIDE_LOG_SERVICE_PORT = 6001;

	/**
	 * Configures the client-side logging system, and starts the log server.
	 * 
	 * @param defaultLogContext
	 *            the default {@link LoggerContext} to use if a client-side specific configuration is not specified
	 *            using the {@code gda.clientLogService.logging.xml} property
	 */
	private static void configureAndStartLogServer(LoggerContext defaultLogContext) {
		LoggerContext clientSideLogContext = defaultLogContext;
		int clientSideLogServicePort = LocalProperties.getInt("gda.client.logging.port",
				DEFAULT_CLIENT_SIDE_LOG_SERVICE_PORT);
		String clientLogService;
		/* allow different handling of messages received via ClientSideLogService */
		if ((clientLogService = LocalProperties.get("gda.clientLogService.logging.xml")) != null) {
			LoggerContext lc1 = new LoggerContext();
			JoranConfigurator configurator1 = new JoranConfigurator();
			lc1.reset();
			configurator1.setContext(lc1);
			try {
				configurator1.doConfigure(clientLogService);
			} catch (JoranException e) {
				exceptionUtils.logException(logger, "Error calling doConfigure for " + clientLogService, e);
			}
			clientSideLogContext = lc1;
		}
		
		if (clientSideLogServicePort != -1) {
			final SimpleSocketServer logServer = new SimpleSocketServer(clientSideLogContext, clientSideLogServicePort);
			logServer.start();
		}
	}

	/**
	 * If authentication is required, opens the authentication dialog, waits for it to close and shuts down the
	 * application if no credentials have been provided or if the credentials are invalid.
	 */
	private static void doAuthentication() {
		// If authentication is required show the window (in the EventQueue)
		// then after it has appeared wait (in the main thread) for the user
		// to make it disappear. If no information was supplied then exit.
		// Note that invokeAndWait is used because we want the window before
		// continuing. This is now done BEFORE the SplashScreen because
		// otherwise the SplashScreen is not visible while the GUI is being
		// made (which was its whole reason for existing).
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						AuthenticationFrame.getInstance().setVisible(true);
					}
				});

				while (AuthenticationFrame.getInstance().isVisible()) {
					Thread.sleep(250);
				}
			} catch (InterruptedException e) {
				// just carry on as success of login will be tested after this method
			} catch (InvocationTargetException e) {
				// just carry on as success of login will be tested after this method
			}
		
	}

	private static void testAuthentication() {
		try {
			// test if info given and user was authenticated
			if (!UserAuthentication.isAuthInfoSupplied()) {
				System.exit(0);
			}
			if (!UserAuthentication.isAuthenticated()) {
				System.out.println("Authorisation failure: incorrect username or password. Client cannot start.");
				logger.warn("Authorisation failed for user: " + UserAuthentication.getUsername());
				System.exit(0);
			}
		} catch (Exception e) {
			System.out.println("Exception during authorisation: " + e.getMessage());
			System.exit(0);
		}
	}

	/**
	 * Use the metadata/icat system to identify the visit ID this client will collect data under. If this system is not
	 * in place then this function returns without effect, but if the system is in place and no valid ID can be
	 * identified then the client closes.
	 * <p>
	 * For more details see the ChooseVisitIDFrame javadoc.
	 * @throws Exception 
	 * @throws HeadlessException 
	 */
	private static void chooseVisitID() throws HeadlessException, Exception {

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					final ChooseVisitIDFrame frame = ChooseVisitIDFrame.getInstance();
					frame.setLocation(200, 200);
					frame.setVisible(true);
				}
			});
			while (ChooseVisitIDFrame.getInstance().isVisible()) {
				Thread.sleep(250);
			}
		} catch (InterruptedException e) {
			// just move on...
		} catch (InvocationTargetException e) {
			// just move on...
		}

		// test if any visit chosen or if there is any valid visit at all, else exit the process
		if (IcatProvider.getInstance().icatInUse() && ChooseVisitIDFrame.getChosenVisitID() == null) {
			String message = "No valid visitID for "
					+ UserAuthentication.getUsername()
					+ " (or user not a member of staff), so the data directory cannot be determined. Client will not continue.";
			System.out.println(message);
			logger.error(message);
			JFrame tempFrame = new JFrame();
			JOptionPane.showMessageDialog(tempFrame, message, "No valid visit", JOptionPane.ERROR_MESSAGE);

			System.exit(0);
		}
	}



	/**
	 * Tests for the condition that the user has a valid visit ID but there is another user of another group already
	 * running on the beamline. If so then run a reduced GUI only showing the BatonPanel so they may talk to the current
	 * user and see when they release the baton.
	 * 
	 * @return true if the reduced GUI will be run
	 * @throws Exception 
	 */
	private static boolean testIfShouldRunReducedGUI() throws Exception {

		if (!LocalProperties.isAccessControlEnabled() || !IcatProvider.getInstance().icatInUse()) {
			return false;
		}

		boolean runReducedGUI = false;

		try {
			runReducedGUI = !AuthoriserProvider.getAuthoriser().isLocalStaff(UserAuthentication.getUsername());
		} catch (ClassNotFoundException e1) {
			runReducedGUI = false;
		}

		// return true if not beamline staff and the baton is held by someone on a different visit
		if (runReducedGUI
				&& JythonServerFacade.getInstance().isBatonHeld()
				&& !(JythonServerFacade.getInstance().getBatonHolder().getVisitID().equals(ChooseVisitIDFrame
						.getChosenVisitID()))) {
			return true;
		}
		return false;
	}

	/**
	 * Graphical User Interface application main program
	 * 
	 * @param args
	 *            user options
	 */
	public static void main(String[] args) {

		LoggingUtils.setLogDirectory();
		LogbackUtils.configureLoggingForClientProcess();

		if (LocalProperties.get("gda.screen.primary") != null) {
			// screen index for the primary screen starting from 0.
			screenIndex = LocalProperties.getInt("gda.screen.primary", 0);
			mss.setPrimaryScreen(screenIndex);
		}

		configureAndStartLogServer(LogbackUtils.getLoggerContext());

		authenticateUser();

		// If necessary create and show the SplashScreen (in the EventQueue).
		// As with AuthenticationFrame we use invokeAndWait because we want
		// this done before we continue.
		// Because of the internal mechanisms of the EventDispatchThread it
		// is possible that no event will be dispatched to cause SplashScreen
		// to paint until the main gui is painted. I (PCS) have gone for the
		// possibly dubious option of making a SplashScreen.paintImmediately
		// (see
		// the comments in there too). It is also possible to more or less
		// guarantee a paint by putting a sleep in this thread (which allows
		// the EventDispatchThread time to schedule a repainting).
		showSplash = !checkArgs(args, "-nosplash", false);
		if (showSplash) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						splashScreen = new SplashScreen();
						splashScreen.showSplash();
						splashScreen.paintImmediately();
					}
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}

		// Build objectServer here as it creates the remote factories which are required to find the
		// metadata object used to work out which visit ID this client will collect data as.
		
		parseArgs(args);
		try {
			objectServer = ObjectServer.createClientImpl(commandLineXmlFile);
		} catch (Throwable e) {
			logger.error("Exception while starting gui", e);
			System.exit(1);
		}

		if (showSplash) {
			splashScreen.hideSplash();
		}


		try {
			chooseVisitID();
		} catch (Exception e1) {
			logger.error("Error starting gui", e1);
			System.exit(1);
		}
		checkMetadata(args);

		// Start the GUI
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					getInstance().showGui(testIfShouldRunReducedGUI());
				} catch (Throwable e) {
					logger.error("Error starting gui", e);
					System.exit(1);
				}
			}
		});
	}

	private static void authenticateUser() {
		if (LocalProperties.get(Authenticator.AUTHENTICATORCLASS_PROPERTY, null) != null) {
			doAuthentication();
			testAuthentication();
		} else {
			UserAuthentication.setToUseOSAuthentication();
		}
	}

	/**
	 * Returns the singleton instance of the acquisition GUI, creating it if it does not already exist.
	 * 
	 * @return the singleton GUI instance
	 */
	public synchronized static AcquisitionGUI getInstance() {
		if (instance == null) {
			instance = new AcquisitionGUI();
		}
		return instance;
	}

	private static void checkMetadata(String[] args) {
		boolean b;
		Finder finder = Finder.getInstance();
		gda.util.findableHashtable.Hashtable hashtable = (gda.util.findableHashtable.Hashtable) finder
				.find("GDAHashtable");

		try {
			if (hashtable != null) {
				b = LocalProperties.check("gda.data.nexusMetadata", false);
				b = checkArgs(args, "-nexusMetadata", b);
				hashtable.putBoolean(FindableHashtable.NEXUS_METADATA, b);

				b = LocalProperties.check("gda.data.srbStore", false);
				b = checkArgs(args, "-srbStore", b);
				hashtable.putBoolean(FindableHashtable.SRB_STORE, b);
			}
		} catch (DeviceException e) {
			System.out.println("Device Exception " + e.getMessage());
		}
	}
	
	private static void parseArgs(String[] args) {
		setArgs(args);
		int argno = 0;
		int argc = args.length;
		while (argno < argc) {
			if (args[argno].equals("-f") && (argno + 1 < argc)) {
				commandLineXmlFile = args[++argno];
			}
			argno++;
		}
	}
	
	/**
	 *  Name of property used to give a title bar suffix e.g. beamline tel no.
	 */
	static final String GDA_TITLEBAR_PREFIX = "gda.gui.titlebar.prefix";
	/**
	 * @return title prefix - e.g. beamline tel xxx GDA version 
	 */
	public static String getTitlePrefix(){
		return LocalProperties.get(GDA_TITLEBAR_PREFIX,"") + " GDA - " + Version.getRelease() + 
			" - logged in as "	+ UserAuthentication.getUsername();
	}
	/**
	 * Builds and displays the GDA GUI.
	 * 
	 * @param batonPanelOnly
	 *            - set to true if the user should only see a reduced GUI to communicate with the current user about
	 *            baton exchange. This option is for beamlines expecting remote users.
	 */
	public void showGui(boolean batonPanelOnly) {
		try {
			
			if(batonPanelOnly){
				JBatonDialog.displayBatonPanel(null, objectServer);
				batonPanelOnly = false;
			}
			String guiTitle = getTitlePrefix();
			// Message.setDebugLevel(args);
			acquisitionFrame = new AcquisitionFrame(guiTitle, objectServer);
			acquisitionFrame.configure(batonPanelOnly);
			acquisitionFrame.setPosition();
			acquisitionFrame.setSize();
			acquisitionFrame.setIconImage(GdaIcons.getWindowIcon());
			acquisitionFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			acquisitionFrame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent ev) {
					acquisitionFrame.confirmExit();
				}
			});

			acquisitionFrame.setVisible(true);
			// we MUST set the dividerlocation AFTER calling setVisible.
			if (LocalProperties.get("gda.frame.DividerLocation") != null) {
				acquisitionFrame.setDividerLocation(LocalProperties.getDouble("gda.frame.DividerLocation", 0.75D));
			}
		} catch (InternalError e) {
			String message = e.getMessage();
			if (message.indexOf("X11") > 0 & message.indexOf("DISPLAY") > 0) {
				logger.debug("\nGDA AcquisitionGUI cannot display!\n\n"
						+ "Set the DISPLAY environment variable to point to the "
						+ "monitor you are using. e.g. setenv DISPLAY hostname:0.0");
			} else {
				logger.debug(e.getStackTrace().toString());
			}
			System.exit(1);
		}
	}

	private static boolean checkArgs(String[] args, String arg, boolean defaultValue) {
		boolean argFound = defaultValue;

		for (String s : args) {
			if (s.equals(arg)) {
				argFound = true;
				break;
			}
		}

		return argFound;
	}

	/**
	 * Sets the arguments used by this GUI.
	 * 
	 * @param arg
	 *            the arguments
	 */
	public static void setArgs(String[] arg) {
		args = arg;
	}

	/**
	 * @return String[] args
	 */
	public String[] getArgs() {
		return args;
	}

	/**
	 * Returns the acquisition frame in this GUI.
	 * 
	 * @return the acquisition frame
	 */
	public AcquisitionFrame getAcquisitionFrame() {
		return acquisitionFrame;
	}

	/**
	 * @return the gui frame
	 */
	public static JFrame getFrame() {
		return getInstance().getAcquisitionFrame();
	}
}
