package de.avetana.bluetooth.sdp;


import javax.bluetooth.*;
import java.io.IOException;
import java.util.*;
import de.avetana.bluetooth.util.BTAddress;
import de.avetana.bluetooth.stack.BlueZ;

/**
 * The class used to manage remote service records.
 *
 * <br><br><b>COPYRIGHT:</b><br> (c) Copyright 2004 Avetana GmbH ALL RIGHTS RESERVED. <br><br>
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
 * <dd>This class is used to manage the remote service records. As requested by the JSR82 Specification, the class
 * is an instance of javax.bluetooth.ServiceRecord. <br>
 * <dd>A remote service search will always return RemoteServiceRecord objects. Indeed, the common use of a JSR82 implementation
 * is to first make a service search, then to select the desired service, to retrieve te connection URL and finally to connect with the remote device.
 * That's why the method getConnectionURL(..) of this class is one of the most used.
 *
 *
 * @author Julien Campana
 */
public class RemoteServiceRecord extends SDPServiceRecord {

  /**
   * The remote device this service belongs to
   */
  private RemoteDevice m_remote;
  /* if no uuid is specified, the service search method will use
  the uuid 0x1002 (PublicBrowseGroup, see therefore the sdp assigned numbers
  at http://www.bluetooth.org)
  */
  private final byte[][] m_uuid= new byte[][] { {0x10, 0x02} };

  /**
   * The internal DiscoveryListener used to re-populate the service record
   */
  private InternListener m_internListener;

  /**
   * Default constructor: creates a RemoteServiceRecord object, which extends SDPServiceRecord
   * @see de.avetana.bluetooth.sdp.SDPServiceRecord
   */
  public RemoteServiceRecord() {
    super();
  }

  /**
   * Creates a RemoteServiceRecord object and sets the remote device this service belongs to.
   * @param badr The BT address of the remote device
   */
  public RemoteServiceRecord(String badr) {
    this();
    m_remote=new RemoteDevice(badr);
  }

  /**
   * Returns the remote device this service belongs to
   * @return The remote device this service belongs to
   */
  public RemoteDevice getHostDevice() {
    return m_remote;
  }

  /**
   * JSR82 Specification: <br>
   * Retrieves the values by contacting the remote Bluetooth device
   * for a set of service attribute IDs of a service that is available
   * on a Bluetooth device.  (This involves going over the air and
   * contacting the remote device for the attribute values.)  The system
   * might impose a limit on the number of service attribute ID
   * values one can request at a time.  Applications can obtain the value of this limit as a String by calling
   * <code>LocalDevice.getProperty("bluetooth.sd.attr.retrievable.max")</code>.
   * The method is blocking and will return when the results of the request are available.  Attribute IDs whose
   * values could be obtained are added to this service record. If
   * there exist attribute IDs for which values are retrieved this
   * will cause the old values to be overwritten. If the remote
   * device cannot be reached, an <code>IOException</code> will be thrown.
   * @param attrIDs the list of service attributes IDs whose value
   * are to be retrieved; the number of attributes cannot exceed the
   * property <code>bluetooth.sd.attr.retrievable.max</code>; the
   * attributes in the request must be legal, i.e. their values are
   * in the range of [0, 2<sup>16</sup>-1]. The input attribute IDs
   * can include attribute IDs from the default attribute set too.
   * @return <code>true</code> if the request was successful in retrieving values for some or all of the attribute IDs;
   * <code>false</code> if it was unsuccessful in retrieving any values
   * @exception IOException if the local device is unable to connect
   * to the remote Bluetooth device that was the source of this <code>ServiceRecord</code>; if this
   * <code>ServiceRecord</code> was deleted from the SDDB of the remote device
   * @exception IllegalArgumentException if the size of <code>attrIDs</code> exceeds the system specified limit as
   * defined by <code>bluetooth.sd.attr.retrievable.max</code>; if the
   * <code>attrIDs</code> array length is zero; if any of their values are not in the range of [0, 2<sup>16</sup>-1]; if
   * <code>attrIDs</code> has duplicate values
   * @exception NullPointerException if <code>attrIDs</code> is <code>null</code>
   * @exception RuntimeException if this <code>ServiceRecord</code>
   * describes a service on the local device rather than a service on a remote device
     */
  public boolean populateRecord(int[] attr) throws java.io.IOException {
    final int[] attrs=attr;
    m_internListener=new InternListener();
    Runnable r=new Runnable() {
      public void run() {
        String addr=m_remote.bdAddrString;
        try {addr=BTAddress.transform(addr);}catch(Exception ex) {}
        try {
          BlueZ.searchServices(addr,m_uuid,attrs,m_internListener);
        }catch(Exception ex) {
           m_internListener.setResponse(0);
        }
      }
    };
    new Thread(r).start();
    while(m_internListener.resp==-1) {
      try {
        Thread.sleep(100);
      }catch(Exception ex) {return false;}
    }
    return (m_internListener.resp==1);
  }

  /**
   * JSR82 Specification:<br>
   * Returns a String including optional parameters that can be used by a client to connect to the service described by this
   * <code>ServiceRecord</code>.  The return value can be used as the
   * first argument to <code>Connector.open()</code>. In the case of a
   * Serial Port service record, this string might look like
   * "btspp://0050CD00321B:3;authenticate=true;encrypt=false;master=true", where "0050CD00321B" is the Bluetooth
   * address of the device that provided this <code>ServiceRecord</code>, "3" is the RFCOMM
   * server channel mentioned in this <code>ServiceRecord</code>, and
   * there are three optional parameters related to security and master/slave roles. <P>
   * If this method is called on a <code>ServiceRecord</code> returned
   * from <code>LocalDevice.getRecord()</code>, it will return the
   * connection string that a remote device will use to connect to this service.<br>
   * AvetanaBluetooth:<br>
   * The implementation of getConnectionURL() only supports RFCOMM and L2CAP.
   * @see #NOAUTHENTICATE_NOENCRYPT
   * @see #AUTHENTICATE_NOENCRYPT
   * @see #AUTHENTICATE_ENCRYPT
   * @param requiredSecurity determines whether authentication or encryption are required for a connection
   * @param mustBeMaster <code>true</code> indicates that this device
   * must play the role of master in connections to this service;
   * <code>false</code> indicates that the local device is willing to be either the master or the slave
   * @return a string that can be used to connect to the service or <code>null</code> if the ProtocolDescriptorList in this
   * ServiceRecord is not formatted according to the Bluetooth specification
   * @exception IllegalArgumentException if <code>requiredSecurity</code> is not one of the constants
   * <code>NOAUTHENTICATE_NOENCRYPT</code>, <code>AUTHENTICATE_NOENCRYPT</code>, or <code>AUTHENTICATE_ENCRYPT</code>
     */
  public String getConnectionURL(int requiredSecurity, boolean mustBeMaster) throws IllegalArgumentException {
    if (m_remote == null) throw new IllegalArgumentException("No remote device found");
    String url = "";
    DataElement protocolDescriptorListElement = (DataElement)m_attributes.get(new Integer(4));
    if (protocolDescriptorListElement == null)
      throw new IllegalArgumentException("Protocol Descriptor is missing. You should maybe populate this Service Record with attrId=0x0004");
    Enumeration protocolDescriptorList = (Enumeration)protocolDescriptorListElement.getValue();
    long l2capPSM=-1;
    long rfcommChannel=-1;
    boolean isObex = false;
    while (protocolDescriptorList.hasMoreElements()) {
      DataElement protocolDescriptorElement = (DataElement)protocolDescriptorList.nextElement();
      if (protocolDescriptorElement == null)
        throw new IllegalArgumentException("Protocol Descriptor is missing. You should maybe populate this Service Record with attrId=0x0004");
      Enumeration protocolParameterList = (Enumeration)protocolDescriptorElement.getValue();
      if (protocolParameterList.hasMoreElements()) {
        DataElement protocolDescriptor = (DataElement)protocolParameterList.nextElement();
        if (protocolDescriptor != null) {
          if (protocolDescriptor.getDataType() == DataElement.UUID) {
            UUID protocolDescriptorUUID = (UUID)protocolDescriptor.getValue();
            long lg=protocolDescriptorUUID.toLong();
            if(lg == 0x100 || lg == 0x3) { // l2cap or rfcomm
              if (protocolParameterList.hasMoreElements()) {
                DataElement protocolPSMElement = (DataElement)protocolParameterList.nextElement();
                if (protocolPSMElement != null) {
                  if (lg==0x3)// && protocolPSMElement.getDataType() == DataElement.U_INT_1)
                    rfcommChannel = protocolPSMElement.getLong();
                  else if (lg==0x100)// && protocolPSMElement.getDataType() == DataElement.U_INT_2)
                    l2capPSM = protocolPSMElement.getLong();
                }
              }
            }
            else if(lg == 0x0008) isObex = true;
            else continue;
          }
        }
      }
    }
    if (isObex && rfcommChannel != -1) url += "btgoep://"+m_remote.getBluetoothAddress()+":"+rfcommChannel;
    else if (rfcommChannel!=-1) url+="btspp://"+m_remote.getBluetoothAddress()+":"+rfcommChannel;
    else if (l2capPSM!=-1) url+="btl2cap://"+m_remote.getBluetoothAddress()+":"+l2capPSM;
    if(url.equals("")) return null;
    url+=";";

    if (requiredSecurity == ServiceRecord.AUTHENTICATE_ENCRYPT) url += "authenticate=true;encrypt=true;";
    else if (requiredSecurity == ServiceRecord.AUTHENTICATE_NOENCRYPT) url += "authenticate=true;encrypt=false;";
    if (requiredSecurity == ServiceRecord.NOAUTHENTICATE_NOENCRYPT) url += "authenticate=false;encrypt=false;";
    url += "master=" + mustBeMaster;
    return url;
  }

  /**
   * This method is not yet supported by the implementation.
   * @param parm1
   */
  public void setDeviceServiceClasses(int parm1) {
    throw new java.lang.UnsupportedOperationException("Method setDeviceServiceClasses() not yet implemented.");
  }

  private class InternListener implements DiscoveryListener {

    private int resp=-1;

    public InternListener() {}

    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {}
    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
      for(int i=0;i<servRecord.length;i++) {
        RemoteServiceRecord myRec=(RemoteServiceRecord)servRecord[i];
        if(myRec.getRecordHandle()==RemoteServiceRecord.this.m_recordHandle) {
          int[] attr=myRec.getAttributeIDs();
          for(int u=0;u<attr.length;u++) {
            DataElement dat=myRec.getAttributeValue(attr[u]);
            if(dat!=null) setAttributeValue(attr[u],dat);
          }
        }
      }
    }
    public void serviceSearchCompleted(int transID, int respCode) {
      switch(respCode) {
        case DiscoveryListener.SERVICE_SEARCH_COMPLETED:
          resp=1;
          break;
        default:
          resp=0;
          break;
      }
    }

    public void setResponse(int i) {resp=i;}

    public void inquiryCompleted(int discType) {}

  }

}