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
 * <b>Description: </b><br>A JPanel performing an HCI inquiry and storing the result of this inquire in a
 * a JList swing component.
 * </b><br>
 */

public class DeviceFinderPane extends JPanel implements ActionListener, DiscoveryListener{

  private JList m_deviceList;
  private DiscoveryAgent m_agent;
  private int maxServiceSearch=1;
  private int nbOfServiceSearch=0;
  private static volatile ProgressDialog m_dialog;
  private Window m_owner;
  private Hashtable nameCache = new Hashtable();
  private Vector m_remote=new Vector();
  public JButton m_refresh, m_name;
  private JPanel m_commandPanel;
  private DefaultListModel myListModel;

  public DeviceFinderPane(Window owner) throws Exception{
    super ();
    m_owner=owner;
    myListModel = new DefaultListModel() {
      public int getSize() { return m_remote.size(); }
      public Object getElementAt(int index) {
        System.out.println("getinng");
        if(index == -1 || m_remote.size() <= index) return null;
        RemoteDevice dev=(RemoteDevice)m_remote.elementAt(index);
        return dev.toStringWithName();
      }
      public Object elementAt(int index) {
        System.out.println("elAt");
        return getElementAt(index);
      }
    };

    m_deviceList=new JList(myListModel);
    initStack();
    setLayout(new BorderLayout());
    JScrollPane myPane=new JScrollPane(m_deviceList);
    add(myPane, BorderLayout.CENTER);

    m_commandPanel = new JPanel ();

    m_refresh = new JButton ("Refresh");
    m_name= new JButton ("Get names");
    m_name.setEnabled(false);
    m_commandPanel.add(m_refresh);
    m_commandPanel.add(m_name);
 //   if (System.getProperty("os.name").equalsIgnoreCase("mac os x")) butPan.add(macScan);
 //   c.add(butPan, BorderLayout.SOUTH);
    m_refresh.addActionListener(this);
    m_name.addActionListener(this);
    add(m_commandPanel, BorderLayout.SOUTH);
  }

  public JPanel getCommandPanel() {
    return m_commandPanel;
  }

  public void initStack() throws Exception{
    LocalDevice local=LocalDevice.getLocalDevice();
    m_agent=local.getDiscoveryAgent();
  }

  public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
    String addr=null, name=null;
    System.out.println("new deviceDiscovered!!!!"+btDevice.bdAddrString);
    try {
      addr=btDevice.bdAddrString;
    }
    catch(Exception ex) {ex.printStackTrace();}
    if(addr!=null) {
      nameCache.put(addr, (name==null?"Not Found":name));
      RemoteDevice rem=new RemoteDevice(addr);
      rem.friendlyName=name;
      m_remote.add(rem);
      inform("Device: "+name+" found!!");
    }
  }

  public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
  }

  public void inform(String message) {
    if(m_dialog!=null) m_dialog.setString(message);
    else System.out.println("m_dialog est null!!!");
  }

  public void showDialog() {
    Runnable r=new Runnable() {
      public void run() {
        if(m_owner instanceof Dialog)
          m_dialog=new ProgressDialog((Dialog)m_owner);
        else
          m_dialog=new ProgressDialog((Frame)m_owner);
        m_dialog.setVisible(true);
      }
    };
    new Thread(r).start();
  }

  public void doInquiry() {
    showDialog();
    nameCache=new Hashtable();
    m_remote=new Vector();
    try {
      m_agent.startInquiry(DiscoveryAgent.GIAC, this);
    }
    catch(Exception ex) {ex.printStackTrace();}
  }

  public void actionPerformed (ActionEvent e) {
    if (e.getSource() == m_refresh) doInquiry();
    else if (e.getSource() == m_name) {
      Cursor cur=this.getCursor();
      this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
      for(int i=0;i<m_remote.size();i++) {
        try {
          RemoteDevice dev=(RemoteDevice)m_remote.elementAt(i);
          dev.getFriendlyName(true);
          }catch(Exception ex) {ex.printStackTrace();}
      }
      myListModel.removeAllElements();
      for(int i=0;i<m_remote.size();i++) {
        System.out.println("i="+i);
        myListModel.addElement(m_remote.elementAt(i));
      }
      this.setCursor(cur);
      m_deviceList=new JList(myListModel);
    }
  }

  public void inquiryCompleted(int discType) {
    m_deviceList.setListData(m_remote);
    m_dialog.setVisible(false);
  }

  public void serviceSearchCompleted(int transID, int respCode) {
  }
  }
