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

import gda.device.Monitor;
import gda.factory.Finder;
import gda.observable.IObserver;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;


/**
 * A dialog box that popups up for user attention when the machine mode leaves or enters 'User'.
 * 
 * To use you need to create an EpicsMonitor on the Server with PvName CS-CS-MSTAT-01:MODE
 * and define a java property gda.gui.beammodemonitor=<monitorname>
 * 
 * The current integration (configuration as Property) is not pretty, but this will change with 
 * the move to RCP anyway.
 */

public class BeamLostPopup implements IObserver {

	private class PopupWithSilencer extends JDialog implements ActionListener {
		
		  public PopupWithSilencer(String title, String message, boolean bad) {
		    super((Frame) null, title, false);
		    setLocationRelativeTo(parent);
		    
		    ImageIcon icon = bad ? 
					new ImageIcon(getClass().getResource("nobeam.png")) : 
						new ImageIcon(getClass().getResource("beam.png"));
			setIconImage(icon.getImage());
			
			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(new JLabel(icon), BorderLayout.LINE_START);
			
			JPanel rightPanel = new JPanel();
			rightPanel.setLayout(new GridLayout(2,1));
			
	    	JLabel messageLabel = new JLabel(message);
	    	messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    	messageLabel.setFont(messageLabel.getFont().deriveFont(messageLabel.getFont().getSize2D()+3.0f));
	    	rightPanel.add(messageLabel);
		    
	    	
		    JPanel buttonPane = new JPanel(new BorderLayout());
		    
		    JCheckBox check = new JCheckBox("<html>please continue to annoy me<br>with these messages</html>", isLive());
		    check.setHorizontalAlignment(SwingConstants.CENTER);
		    check.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					setLive(((AbstractButton) e.getSource()).isSelected());
				}
		    	
		    });
		    buttonPane.add(check, BorderLayout.CENTER);
		    JButton button = new JButton("OK"); 
		    buttonPane.add(button, BorderLayout.SOUTH); 
		    button.addActionListener(this);
		    rightPanel.add(buttonPane, BorderLayout.SOUTH);
		    
		    getContentPane().add(rightPanel);

		    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		    
		    pack(); 
		    setVisible(true);
		  }
		
		  @Override
		  public void actionPerformed(ActionEvent e) {
		    setVisible(false); 
		    dispose(); 
		  }
	}	  
	
	private Monitor beamModeMonitor;

	final int USERMODE = 4;
	final int MACHINE = 3;
	final int NOBEAM = 2;
	final int INJECTION = 1;
	final int SHUTDOWN = 0;
	
	Map<Integer, String> message = new HashMap<Integer, String>() {
	    { 
	        put(USERMODE, "Machine is back to user mode!");
	        put(MACHINE, "Machine Development started!");
	        put(NOBEAM, "No storged beam in the ring!");
	        put(INJECTION, "Injection is ongoing!");
	        put(SHUTDOWN, "Shutdown - no beam!");
	    }
	};

	private boolean beam = true;
	private boolean active = true;
	
	private PopupWithSilencer pd;

	private Component parent;
	
	private BeamLostPopup() {
		
	}
	
	/**
	 * @param parent component for placement
	 * @param beamModeMonitorName name of the EpicsMonitor on CS-CS-MSTAT-01:MODE
	 */
	public BeamLostPopup(Component parent, String beamModeMonitorName) {
		this();
		this.parent = parent;
		beamModeMonitor = (Monitor) Finder.getInstance().find(beamModeMonitorName);
		if (beamModeMonitor != null)
			beamModeMonitor.addIObserver(this); //FIXME: potential race condition
	}
	
	@Override
	public void update(Object theObserved, Object changeCode) {
		if (theObserved instanceof Monitor) {
			int newvalue = (Integer) changeCode;
			if (beam) {
				if (newvalue != USERMODE) {
					// beam lost
					beam = false;
					showPopupDialog("No User Beam", message.get(newvalue), true);
				} else {
					// we should not see this (oldvalue == newvalue)
				}
			} else {
				if (newvalue == USERMODE) {
					// beam regained
					beam = true;
					showPopupDialog("User Operation resumed", "Machine is back to user mode!", false);
				} else {
					// we do not care if mode switched between non-user values
				}
			}
			
		}
	}
	
	private void showPopupDialog(String title, String message, boolean bad) {
		if (!isLive()) return;
		
		if (pd != null) {
			try {
				pd.setVisible(false);
				pd.dispose();
			} catch(Exception ignored) {
				// 
			}
		}
		
		pd = new PopupWithSilencer(title, message, bad);
	}

	/**
	 * @return do you think the machine is in user mode?
	 */
	public boolean isBeam() {
		return beam;
	}

	/**
	 * (for testing)
	 * 
	 * @param beam 
	 */
	public void setBeam(boolean beam) {
		this.beam = beam;
	}

	/**
	 * @return state if we generate a popup on changes
	 */
	public boolean isLive() {
		if (beamModeMonitor == null) return false;
		return active;
	}

	/**
	 * activate/deactivate the popups
	 * 
	 * @param active
	 */
	public void setLive(boolean active) {
		this.active = active;
	}
}