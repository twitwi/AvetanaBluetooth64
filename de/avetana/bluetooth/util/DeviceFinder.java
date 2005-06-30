/*
 * Created on 05.05.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.avetana.bluetooth.util;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.swing.*;


public class DeviceFinder extends JDialog implements ActionListener {
  	
  	JList jl = new JList();
  	JButton okBut = new JButton ("Select");
  	JButton cancelBut = new JButton ("Cancel");
  	JButton inqBut = new JButton ("Inquiring...");
  	RemoteDevice selectedDevice = null;
  	DefaultListModel lm = new DefaultListModel();
  	private boolean inquiryDone = false;
  	Vector remoteDevices;
  	DiscoveryListener discList;
  	
 	public DeviceFinder (Frame frame) {
 		this (frame, -1, -1);
 	}
 	
  	public DeviceFinder (Frame frame, final int min, final int maj) {
  		super (frame, true);
  		Container cont = getContentPane();
  		cont.setLayout(new BorderLayout());
  		cont.add (new JScrollPane (jl), BorderLayout.CENTER);
  		JPanel bp = new JPanel();
  		bp.add(okBut);
  		bp.add(cancelBut);
  		bp.add (inqBut);
  		cont.add(bp, BorderLayout.SOUTH);
  		pack();
  		
  		jl.setModel(lm);
  		
  		okBut.addActionListener(this);
  		cancelBut.addActionListener(this);
  		inqBut.addActionListener (this);
  		
  		discList = new DiscoveryListener() {
    		  public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
    		  	if ((maj != -1 && cod.getMajorDeviceClass() != maj) || (min != -1 && cod.getMinorDeviceClass() != min)) return;
    		  	try {
//    	 		  	System.out.println (btDevice.getFriendlyName(false) + " " + cod.getMajorDeviceClass() + " " + cod.getMinorDeviceClass());
    	 		 	lm.addElement(btDevice.getFriendlyName(false));
  				remoteDevices.add(btDevice);
  			} catch (IOException e) {
  				// TODO Auto-generated catch block
  				e.printStackTrace();
  			}
    		  }
    		  public void inquiryCompleted(int discType) {
    		    okBut.setEnabled (true);
    		    inqBut.setEnabled (true);
    		    inqBut.setText ("Inquiry");
    		  }

    		  public void serviceSearchCompleted(int transID, int respCode) {
    		  }

    		  public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
    		  }

    		};
 		doInquiry();
  		
 		super.setLocationRelativeTo(frame);
		
  		setVisible (true);
  	}
  	
  	public void actionPerformed (ActionEvent e) {
  		if (e.getSource() == okBut && jl.getSelectedIndex() != -1) {
  			selectedDevice = (RemoteDevice)remoteDevices.elementAt(jl.getSelectedIndex());
  			setVisible(false);
  		} else if (e.getSource() == cancelBut) {
  			if (inqBut.isEnabled() == false) { 
  			 try {
				LocalDevice.getLocalDevice().getDiscoveryAgent().cancelInquiry(discList);
			 } catch (BluetoothStateException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			 } return; }
  			
  			selectedDevice = null;
  			setVisible (false);
  		} else if (e.getSource() == inqBut) {
  			doInquiry();
  		}
  	}
  	
  	private void doInquiry() {
 
  		inquiryDone = false;
  		remoteDevices = new Vector();
  		lm.removeAllElements();
  		okBut.setEnabled (false);
  		inqBut.setText ("Inquiring...");
		inqBut.setEnabled (false);
  		
  		
 		Runnable r = new Runnable() {
  			public void run ()  {
  			  try {
				LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, discList);
  			  } catch (BluetoothStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 			}
  		};
  		
  		new Thread (r).start();
 
  	}
  	
  	public RemoteDevice getSelectedDevice() {
  		return selectedDevice;
  	}
  }
