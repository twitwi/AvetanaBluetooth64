
package de.avetana.bluetooth.util;

import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.util.prefs.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import de.avetana.bluetooth.sdp.*;
import de.avetana.bluetooth.stack.*;
import javax.bluetooth.*;

/**
 * <b>COPYRIGHT:</b><br> (c) Copyright 2004 Avetana GmbH ALL RIGHTS RESERVED. <br><br>
 *
 * This file is part of the Avetana bluetooth API for Linux.<br><br>
 *
 * The Avetana bluetooth API for Linux is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version. <br><br>
 *
 * The Avetana bluetooth API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.<br><br>
 *
 * The development of the Avetana bluetooth API is based on the work of
 * Christian Lorenz (see the Javabluetooth Stack at http://www.javabluetooth.org) for some classes,
 * on the work of the jbluez team (see http://jbluez.sourceforge.net/) and
 * on the work of the bluez team (see the BlueZ linux Stack at http://www.bluez.org) for the C code.
 * Classes, part of classes, C functions or part of C functions programmed by these teams and/or persons
 * are explicitly mentioned.<br><br><br><br>
 *
 *
 * <b>Description:</b><br> Dialog that finds every bluetooth device with Services matching a search kriteria. The search kriteria (an Array of UUIDs)
 * is entered by the programmer as a parameter of the ServiceFinder constructor. If this parameterr is null, the ServiceFinder
 * class will then look for every Services accepting RFCOMM connections.
 *
 * @author Julien Campana
 */

public class ServiceFinder extends JDialog  implements ActionListener{

  ServiceFinderPane myPane;
  JButton macScan, cancel;

  /**
   * Constructs an new Service Finder object
   * @param owner The Frame this dialogs belongs to
   * @param a_search The search kriteria (an array of UUIDs)
   * @throws HeadlessException
   * @throws Exception
   */
  public ServiceFinder(Frame owner, UUID[] a_search) throws HeadlessException, Exception {
    super(owner, "Service Finder",true);
    try {
      jbInit();
      setSize(640, 480);
      setLocationRelativeTo(null);
    }catch(Exception ex) {
      JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      ex.printStackTrace();
      setVisible(false);
    }
  }

  /**
   * Returns the description of the service currently selected
   * @return The description of the service currently selected
   */
  public ServiceDescriptor getSelectedService() {
    return myPane.selected;
  }

  /**
   * Draws the dialog
   * @throws Exception
   */
  public void jbInit() throws Exception {
    macScan = new JButton ("Mac-Inquiry");
    cancel = new JButton("Close");
    myPane=new ServiceFinderPane(this, "lastSrSearch", null);
    JPanel command=myPane.getCommandPanel();
    if (System.getProperty("os.name").equalsIgnoreCase("mac os x")) command.add(macScan);
    command.add(cancel);
    Container c=this.getContentPane();
    c.setLayout(new BorderLayout());
    c.add(myPane, BorderLayout.CENTER);
    cancel.addActionListener(this);
    macScan.addActionListener(this);
    myPane.m_select.addActionListener(this);
  }

  /**
   * Reaction of user's actions
   * @param e The actionEvent describing the action
   */
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == cancel) {
      myPane.selected=null;
      setVisible(false);
    }
    else if (e.getSource() == macScan) {
      BlueZ.macScan();
    }
    else if (e.getSource() == myPane.m_select)
      if(myPane.selected != null) setVisible(false);
  }


  public static void main(String[] args) throws HeadlessException, Exception {
    BluezStack stack=new BluezStack();
    BluetoothStack.init(stack);

    ServiceFinder ServiceFinder1 = new ServiceFinder(new JFrame(), null);
    ServiceFinder1.setVisible(true);
  }
}

