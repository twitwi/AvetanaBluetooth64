package de.avetana.bluetooth.stack;

import de.avetana.bluetooth.rfcomm.*;
import de.avetana.bluetooth.connection.*;
import de.avetana.bluetooth.sdp.*;
import de.avetana.bluetooth.hci.HCIInquiryResult;
import javax.bluetooth.*;
import de.avetana.bluetooth.util.BTAddress;
import javax.microedition.io.*;
import java.util.Vector;

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
* <b>Description:</b><br>
* This class implements the de.avetana.bluetooth.stack.BluetoothStack class. <br>
* Please refer to this abstract class for the documentation.
*
* @see de.avetana.bluetooth.stack.BluetoothStack
* @author Julien Campana
*/
public class BluezStack extends BluetoothStack {

  private int m_bd; // bluetooth descriptor
  private int devID=-1; // bluetooth adapter
  private static RemoteServiceRecord myRecord;
  private static boolean fini=false;

  public BluezStack() throws Exception{
      m_bd = BlueZ.hciOpenDevice(0);
      devID = 0;
  }

  public BluezStack(int devID) throws Exception{
      m_bd = BlueZ.hciOpenDevice(devID);
      this.devID = devID;
  }

  public void setDeviceID(int dev) throws Exception{
    if (BlueZ.myFactory.getConnections().size()!=0) throw new Exception("You must close before all connections");
    if (BlueZ.myFactory.getNotifiers().size()!=0) throw new Exception("You must close before all connection notifiers");
    BlueZ.myFactory = new ConnectionFactory();
    if(devID > -1) {
      try {
        BlueZ.hciCloseDevice(devID);
      } catch(Exception ex) {}
    }
    BlueZ.hciOpenDevice(dev);
  }

  public String getRemoteName(String bd_addr) throws Exception {
    try {
      String addr="";
      if(bd_addr.length()==12) {
        addr=BTAddress.transform(bd_addr);
      } else addr=bd_addr;
      System.out.println("calling remotename with:"+addr+"  "+m_bd);
      return BlueZ.hciRemoteName(m_bd,addr);
    }catch(Exception ex) {ex.printStackTrace();return "null";}
  }

  public int authenticate(RemoteDevice dev) throws Exception {
    BTConnection bs=isConnected(dev);
    if(bs==null || bs.getConnectionID() == -1) throw new Exception("This remote device is not connected!");
    System.out.println("dev address="+dev.getBTAddress().toString());
    return BlueZ.authenticate(bs.getConnectionID(), dev.getBTAddress().toString());
  }

  public boolean getConnectionFlag(RemoteDevice dev, int pos) throws Exception {
    boolean b[]=getConnectionOptions(dev);
    if(b!=null) return b[pos];
    return false;
  }



  private boolean[] getConnectionOptions(RemoteDevice dev) throws Exception {
    BTConnection bs=isConnected(dev);
    if(bs==null || bs.getConnectionID() == -1) throw new Exception("This remote device is not connected!");
    return BlueZ.connectionOptions(bs.getConnectionID(), dev.getBTAddress().toString());
  }

  public int encrypt(Connection conn,RemoteDevice dev, boolean encrypt) throws Exception {
    if(conn==null || ((BTConnection)conn).getConnectionID() == -1) throw new Exception("This remote device is not connected!");
    System.out.println("dev address="+dev.getBTAddress().toString());
    return BlueZ.encrypt(((BTConnection)conn).getConnectionID(), dev.getBTAddress().toString(), encrypt);
  }

  public Vector Inquire() throws Exception{
    HCIInquiryResult di = BlueZ.hciInquiry(devID);
    return di.getDevices();
  }

  public BTConnection isConnected(RemoteDevice dev) {
    return BlueZ.myFactory.isConnected(dev);
  }

  public String getLocalDeviceAddress() throws java.lang.Exception {
    return BlueZ.hciDevBTAddress(devID).toStringSep(false);
  }
  public String getLocalDeviceName() throws java.lang.Exception {
    return BlueZ.hciLocalName(m_bd);
  }

  private void searchServices(final int[] attrSet, final short[] uuidSet, RemoteDevice btDev, final DiscoveryListener myListener) {
    String addr=btDev.bdAddrString;
    try {addr=BTAddress.transform(addr);}catch(Exception ex) {}
    final String addr2=addr;
    Runnable r=new Runnable() {
      public void run() {
        try {
          BlueZ.searchServices(addr2,uuidSet,attrSet,myListener);
        }catch(Exception ex) {ex.printStackTrace();}
      }
    };
    new Thread(r).start();
  }

  public void searchServices(final int[] attrSet, UUID[] uuidSet, RemoteDevice btDev, final DiscoveryListener myListener) {
    short[] uuid16Set;
    if(uuidSet==null || uuidSet.length==0) uuid16Set=new short[]{0x1002};
    else {
      uuid16Set=new short[uuidSet.length];
      for(int i=0;i<uuid16Set.length;i++) {
        try {uuid16Set[i]=Short.decode("0x"+uuidSet[i].to32bitsString()).shortValue();}catch (Exception ex) {throw new IllegalArgumentException("UUID must be 16 bits length!!!!!");}
      }
      for(int i=0;i<uuid16Set.length;i++) {
        System.out.println("Searching for 0x"+Integer.toHexString(uuid16Set[i]));
      }
    }
    searchServices(attrSet,uuid16Set,btDev,myListener);
  }

  public int getClassOfDevice() throws java.lang.Exception {
    return BlueZ.getDeviceClass(m_bd);
  }

  public void closeDevice() throws Exception {
    BlueZ.hciCloseDevice(m_bd);
  }

  public int getDiscoverableMode() throws Exception {
    return BlueZ.getAccessMode(m_bd);
  }

  public int setDiscoverableMode(int mode) throws Exception {
    return BlueZ.setAccessMode(m_bd, mode);
  }

  public Connection openRFCommConnection(JSR82URL url) throws Exception {
    return de.avetana.bluetooth.rfcomm.RFCommConnection.createRFCommConnection(url);
  }

  public Connection openL2CAPConnection(JSR82URL url) throws Exception {
    return de.avetana.bluetooth.l2cap.L2CAPConnectionImpl.createL2CAPConnection(url);
  }

  public void cancelServiceSearch(int transID) {

  }

  public int updateService(byte[] b, long recordHandle) throws Exception{
    return BlueZ.updateService(b, b.length, recordHandle);
  }

  public static final void main(String[] args) throws Exception {
       BluezStack stack=new BluezStack();
       BluetoothStack.init(stack);
       LocalDevice local=LocalDevice.getLocalDevice();
       DiscoveryAgent agent=local.getDiscoveryAgent();
       RemoteDevice dev=new RemoteDevice("00A096057490");
       System.out.println("BlueZStack was called for device chessBelt!!!");
       DiscoveryListener myListener=new DiscoveryListener() {
         public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
           try {
             System.out.println("Device "+btDevice.getFriendlyName(false)+"  "+btDevice.getBluetoothAddress()+"  discovered");
           }catch(Exception ex) {ex.printStackTrace();}
         }

         public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
           for(int i=0;i<servRecord.length;i++) {
             try {
               System.out.println("TransID="+transID+" new Service discovered for device: "+servRecord[i].getHostDevice().bdAddrString);
               System.out.println(servRecord[i]+"\n\n\n");
               System.out.println("Connection URL="+servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT,false));
             }catch(Exception ex) {ex.printStackTrace();}
           }
         }
         public void inquiryCompleted(int discType) {}
         public void serviceSearchCompleted(int transID, int respCode) {
           fini=true;
           System.out.println("ServiceSearchcompleted="+(respCode==DiscoveryListener.SERVICE_SEARCH_COMPLETED));
         }

       };
//       agent.searchServices(new int[]{0,3,4,6,9,0x100}, new UUID[]{new UUID("0003",true)},dev, myListener);
//       while(!fini) {Thread.sleep(1000);}
       RFCommConnection connection=(RFCommConnection)Connector.open("btspp://00A096057490:1;authentificate=false;encrypt=false;master=false");
       System.out.println("Waiting 4 seconds before sending bytes!");
       Thread.sleep(4000);
       System.out.println("Sending first bytes");
       connection.getOutputStream().write(new byte[] { (byte)0xfc, 0x13, 0x09, 0x04, 0x30, 0x30, 0x30, 0x30, (byte)0xff, (byte)0xff, (byte)0xfd });
       System.out.println("First bytes ok.\nSending second byte array");
       connection.getOutputStream().write(new byte[] { (byte)0xfc, 0x17, 0x09, 0x02, 0x01, (byte)0xff, (byte)0xff, (byte)0xfd });
       System.out.println("Second bytes ok.\nSending third byte array");
       connection.getOutputStream().write(new byte[] { (byte)0xfc, 0x19, 0x09, 0x05, 0x01, (byte)0xff, (byte)0xff, (byte)0xfd });
       System.out.println("Third bytes ok.\nWaiting for bytes to be received...");
       //System.exit(0);
       //   agent.startInquiry(DiscoveryAgent.GIAC, myListener);
    /*
       if(myRecord!=null) {
         boolean b=myRecord.populateRecord(new int[]{4});
         System.out.println("b="+b);
         System.out.println(myRecord);
       } else {
         System.out.println("myRecord est null");
       }*/
  }
}
