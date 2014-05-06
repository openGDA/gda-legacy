/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

import java.awt.Color;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.text.Document;

public class BatonMessageViewer {
	
	public static void main(String[] args) {
		
		// Expect directory containing .log files to be supplied on the command line
		if (args.length != 1) {
			System.err.println("Usage: java BatonMessageViewer <message dir>");
				System.exit(1);
		}
		String messagesDirPath = args[0];
		File messagesDir = new File(messagesDirPath);
		
		// Find all .log files in the directory
		File[] messageFiles = messagesDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String filename = pathname.getName();
				return (filename.startsWith("message_") && filename.endsWith(".log"));
			}
		});
		
		JFrame frame = new JFrame();
		frame.setTitle("Baton message viewer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JTabbedPane pane = new JTabbedPane();
		frame.add(pane);
		
		for (int i=0; i<messageFiles.length; i++) {
			
			final File file = messageFiles[i];
			
			// Create scrollable text area for this file
			JTextArea textArea = new JTextArea();
			textArea.setLineWrap(true);
			textArea.setEditable(false);
			JScrollPane scrollPane = new JScrollPane(textArea);
			
			// Add tab for this file
			pane.addTab(file.getName(), scrollPane);
			
			// Try to load the document from the file
			Document doc;
			try {
				doc = readMessageFile(file);
				textArea.setDocument(doc);
			}
			
			catch (Exception e) {
				
				// Make the tab red for this file
				pane.setForegroundAt(i, Color.RED);
				
				// Show stack trace on this file's tab
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				pw.println("Could not read " + file + ":");
				pw.println();
				e.printStackTrace(pw);
				textArea.setText(sw.toString());
			}
		}
		
		frame.setSize(1200, 800);
		frame.setLocation(200, 200);
		frame.setVisible(true);
	}
	
	private static Document readMessageFile(File file) throws Exception {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
		Document doc = (Document) ois.readObject();
		ois.close();
		return doc;
	}

}
