package de.avetana.bluetooth.sdp;

import javax.bluetooth.*;
import java.io.IOException;
import java.util.*;
import de.avetana.bluetooth.util.*;
import de.avetana.bluetooth.stack.BlueZ;
import de.avetana.bluetooth.connection.*;
import java.io.*;

/**
 * The class used to manage local service records.
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
 * This class is used to manage the local service records. As requested by the JSR82 Specification, the class
 * is an instance of javax.bluetooth.ServiceRecord. However, some methods like getRemoteDevice() throw a RuntimeExcepion
 * because there is logically no RemoteDevice for a local Service Record. The most used method is certainly the
 * static method createSerialSvcRecord, which creates with few parameters a complete local Service record. <br>
 * Other methods like getSDPRecordXML() were implemented in order to fullfill OS specific needs.
 *
 * @author Julien Campana
 */

public class LocalServiceRecord extends SDPServiceRecord {


  private short m_type;
  private UUID localUUID;

  /**
   * Default constructor: creates a new LocalServiceRecord object
   */
  public LocalServiceRecord() {
    super();
  }

  /**
   * Creates a new LocalServiceRecord and sets its record handle
   * @param recordHandle The record handle of the service record
   */
  public LocalServiceRecord(long recordHandle) {
    super(recordHandle);
  }

  /**
   * Returns the protocol supported by this local service record. In the case of a service record
   * supporting more than one protocol (for example L2CAP and RFCOMM), the protocol code identifying
   * the highest level protocol is returned (with the previous example, this would be JSR82URL.PROTOCOL_RFCOMM).
   * @return The protocol code.
   */
  public short getProtocol() {return m_type;}

  /**
   * Creates a new local Service Record with desired options
   * @param svcID The UUID of the local service record
   * @param name The name of the local service record
   * @param channel The Channel/PSM value
   * @param protocol The protocol code identifying the highest level protocol
   * @return The new local service record defined.
   */
  public static LocalServiceRecord createSerialSvcRecord(UUID svcID,
                                                         String name,
                                                         int channel,
                                                         short protocol) {
    LocalServiceRecord rec=new LocalServiceRecord();
    rec.localUUID = svcID;
    rec.m_type=protocol;
    DataElement serviceClassIDList = new DataElement(DataElement.DATSEQ);
    if(protocol==JSR82URL.PROTOCOL_RFCOMM) serviceClassIDList.addElement(new DataElement(DataElement.UUID, new UUID(SDPConstants.UUID_SERIAL_PORT)));
    rec.m_attributes.put(new Integer(SDPConstants.ATTR_SERVICE_CLASS_ID_LIST), serviceClassIDList);
    DataElement protocolDescriptorList = new DataElement(DataElement.DATSEQ);
    DataElement l2capDescriptor = new DataElement(DataElement.DATSEQ);
    l2capDescriptor.addElement(new DataElement(DataElement.UUID, new UUID(SDPConstants.UUID_L2CAP)));
    protocolDescriptorList.addElement(l2capDescriptor);
    DataElement rfcommDescriptor = new DataElement(DataElement.DATSEQ);
    rfcommDescriptor.addElement(new DataElement(DataElement.UUID, new UUID(SDPConstants.UUID_RFCOMM)));
    rfcommDescriptor.addElement(new DataElement(DataElement.U_INT_1, channel));
    // If the user want to create an RFComm or an Obex service, the rfcomm protocol descriptor has to be added
    if(protocol==JSR82URL.PROTOCOL_RFCOMM || protocol==JSR82URL.PROTOCOL_OBEX)
      protocolDescriptorList.addElement(rfcommDescriptor);
    else l2capDescriptor.addElement(new DataElement(DataElement.U_INT_2, channel));

    DataElement obexDescriptor = new DataElement(DataElement.DATSEQ);
    obexDescriptor.addElement(new DataElement(DataElement.UUID, new UUID(SDPConstants.UUID_OBEX)));

    //Add if requested the obex protocol descriptor
    if(protocol==JSR82URL.PROTOCOL_OBEX)
      protocolDescriptorList.addElement(obexDescriptor);

    rec.m_attributes.put(new Integer(SDPConstants.ATTR_PROTO_DESC_LIST), protocolDescriptorList);
    DataElement browseClassIDList = new DataElement(DataElement.DATSEQ);
    UUID browseClassUUID = new UUID(SDPConstants.UUID_PUBLICBROWSE_GROUP);
    browseClassIDList.addElement(new DataElement(DataElement.UUID, browseClassUUID));
    rec.m_attributes.put(new Integer(5), browseClassIDList);
    DataElement languageBaseAttributeIDList = new DataElement(DataElement.DATSEQ);
    languageBaseAttributeIDList.addElement(new DataElement(DataElement.U_INT_2, 25966));
    languageBaseAttributeIDList.addElement(new DataElement(DataElement.U_INT_2, 106));
    languageBaseAttributeIDList.addElement(new DataElement(DataElement.U_INT_2, 256));
    rec.m_attributes.put(new Integer(6), languageBaseAttributeIDList);
    DataElement profileDescriptorList = new DataElement(DataElement.DATSEQ);
    DataElement profileDescriptor = new DataElement(DataElement.DATSEQ);
    profileDescriptor.addElement(new DataElement(DataElement.UUID, new UUID(SDPConstants.UUID_SERIAL_PORT)));
    profileDescriptor.addElement(new DataElement(DataElement.U_INT_2, 256));
    profileDescriptorList.addElement(profileDescriptor);
    rec.m_attributes.put(new Integer(9), profileDescriptorList);
    rec.m_attributes.put(new Integer(256), new DataElement(DataElement.STRING, name));
    return rec;
  }

  /**
   * Transforms the service record object into an XML element and returns the string representation
   * of this XML element. Useful for the Mac implementation
   * @return The XML-based string representation of the service record
   */
  public String getSDPRecordXML() {
    try {
      File f = File.createTempFile ("serviceRecord", ".xml");
      f.deleteOnExit();

      PElement plist = new PElement ("plist");
      plist.setAttribute("version", "0.9");
      PElement dict = new PElement ("dict");
      plist.addChild(dict);
      dict.addChild(new PElement ("key", "0000 - ServiceRecordHandle*"));
      dict.addChild(new PElement ("integer", "65540"));
      dict.addChild(new PElement ("key", "0001 - ServiceClassIDList"));
      dict.addChild(new PElement ("array")).addChild(new PElement ("data", new String(Base64.encode(localUUID.toByteArray()))));

    if(getProtocol()==JSR82URL.PROTOCOL_RFCOMM || getProtocol()==JSR82URL.PROTOCOL_OBEX || getProtocol()==JSR82URL.PROTOCOL_L2CAP) {
      dict.addChild(new PElement ("key", "0004 - ProtocolDescriptorList"));
      PElement pdla = dict.addChild(new PElement ("array"));

      PElement pd1aa = pdla.addChild (new PElement ("array"));
      pd1aa.addChild(new PElement ("data", "AQA="));

      DataElement de = getChannelNumberElement();

      int channelNumber = de != null ? (int)de.getLong() : 1;
      if (getProtocol() == JSR82URL.PROTOCOL_RFCOMM ||getProtocol() == JSR82URL.PROTOCOL_OBEX) {
        PElement pdla1 = pdla.addChild (new PElement ("array"));
        pdla1.addChild(new PElement ("data", "AAM="));
        pdla1.addChild(newDataElement (1, 1, channelNumber));
      }
      else {
        pd1aa.addChild(newDataElement (2, 1, channelNumber));
      }

    }

      dict.addChild(new PElement ("key", "0005 - BrowseGroupList*"));
      dict.addChild(new PElement ("array")).addChild(new PElement ("data", "EAI="));

//      dict.addChild(new PElement ("key", "0006 - LanguageBaseAttributeIDList*"));

      dict.addChild(new PElement ("key", "0009 - BluetoothProfileDescriptorList"));
      PElement bpda = dict.addChild(new PElement ("array")).addChild(new PElement ("array"));
      bpda.addChild(new PElement ("data", new String (Base64.encode(localUUID.toByteArray()))));
      bpda.addChild(newDataElement (2, 1, 256));

      dict.addChild(new PElement ("key", "0100 - ServiceName*"));
      dict.addChild(new PElement ("string", ((String)this.getAttributeValue(256).getValue())));

//      dict.addChild(new PElement ("key", "0303 - Supported Formats List"));

      FileOutputStream fos = new FileOutputStream (f);
      plist.writeXML(fos);

      return f.getAbsolutePath();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Changes the channel number of an RFCOMM local service record
   * @param newChannel The new Channel number
   */
  public void updateChannelNumber(int newChannel) {

    DataElement parent = this.getChannelNumberElementParent();
    DataElement channel = this.getChannelNumberElement();
    if (parent == null) return;
    if (channel != null) parent.removeElement(channel);
    parent.addElement(new DataElement(getProtocol() == JSR82URL.PROTOCOL_RFCOMM ? DataElement.U_INT_1 : DataElement.U_INT_2, newChannel));

  }

  /**
   * Gets the channel/PSM number of the local service record
   * @return The channel/PSM number of the local service record
   */
  public DataElement getChannelNumberElement() {
    DataElement protocolDescriptorListElement = (DataElement)m_attributes.get(new Integer(4));
    if (protocolDescriptorListElement == null)
      throw new IllegalArgumentException("Protocol Descriptor is missing. You should maybe populate this Service Record with attrId=0x0004");
    Enumeration protocolDescriptorList = (Enumeration)protocolDescriptorListElement.getValue();
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
            if ( lg == 0x0003 || lg == 0x100) {
              if (protocolParameterList.hasMoreElements()) {
                DataElement protocolPSMElement = (DataElement)protocolParameterList.nextElement();
                if (protocolPSMElement != null) {
                   if((lg == 0x3 && protocolPSMElement.getDataType() == DataElement.U_INT_1) ||
                      (lg==0x100 && protocolPSMElement.getDataType() == DataElement.U_INT_2))
                      return protocolPSMElement;
                  }
              }
            }
            else if(lg == 0x0008) System.out.println("OBEX FOUND!!!");
            else continue;
          }
        }
      }
    }
    return null;
  }

  private DataElement getChannelNumberElementParent() {
    DataElement protocolDescriptorListElement = (DataElement)m_attributes.get(new Integer(4));
    if (protocolDescriptorListElement == null)
      throw new IllegalArgumentException("Protocol Descriptor is missing. You should maybe populate this Service Record with attrId=0x0004");
    Enumeration protocolDescriptorList = (Enumeration)protocolDescriptorListElement.getValue();
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
            if ( lg == 0x0003 || lg == 0x0100) { // is L2CAP
              if (protocolParameterList.hasMoreElements()) {
                DataElement protocolPSMElement = (DataElement)protocolParameterList.nextElement();
//                System.out.println(protocolPSMElement);
                if (protocolPSMElement != null) {
                  if((lg == 0x3 && protocolPSMElement.getDataType() == DataElement.U_INT_1) ||
                     (lg==0x100 && protocolPSMElement.getDataType() == DataElement.U_INT_2))
                      return protocolDescriptorElement;
                  }
              }
            }
            else if(lg == 0x0008) System.out.println("OBEX FOUND!!!");
            else continue;
          }
        }
      }
    }
    return null;
  }

  private PElement newDataElement (int size, int type, int value) {
    PElement dict = new PElement ("dict");
    dict.addChild(new PElement ("key", "DataElementSize"));
    dict.addChild(new PElement ("integer", "" + size));
    dict.addChild(new PElement ("key", "DataElementType"));
    dict.addChild(new PElement ("integer", "" + type));
    dict.addChild(new PElement ("key", "DataElementValue"));
    dict.addChild(new PElement ("integer", "" + value));
    return dict;
  }

  private PElement newDataElement (int type, byte value[]) {
      PElement dict = new PElement ("dict");
      dict.addChild(new PElement ("key", "DataElementType"));
      dict.addChild(new PElement ("integer", "" + type));
      dict.addChild(new PElement ("key", "DataElementValue"));
      dict.addChild(new PElement ("data", "" + Base64.encode(value)));
      return dict;
  }

  /**
   * Method not available for a local service record.<br>
   * This method always returns null. (This returned value is the one requested by the JSR82 specification
   * version 1.0)
   * @return <code>null</code>
   */
  public RemoteDevice getHostDevice() {
    return null;
  }

  /**
   * Method not available for a local service record. Throws a new exception.
   * @return nothing - Throws an Exception
   */
  public boolean populateRecord(int[] attrIDs) throws IOException {
    throw new RuntimeException("This is a local Service Record: the record can not be populated!");
  }

  /**
   * Method not available for a local service record. Throws a new exception.
   * @return nothing - Throws an Exception
   */
  public String getConnectionURL(int requiredSecurity, boolean mustBeMaster) {
    throw new RuntimeException("This is a local Service Record: no connection URL is available!");
  }

  /**
   * Method not yet supported. Throws a new exception.
   * @return  nothing - Throws an Exception
   */
  public void setDeviceServiceClasses(int classes) {
    throw new java.lang.UnsupportedOperationException("Method setDeviceServiceClasses() not yet implemented.");
  }


  public static void main (String[] args) throws Exception {
    LocalServiceRecord lsr = LocalServiceRecord.createSerialSvcRecord(new UUID (new byte[] { 0x0D, (byte)0xAD, 0x43, 0x65, 0x5D, (byte)0xF1, 0x11, (byte)0xD6, (byte)0x9F, 0x6E, 0x00, 0x03, (byte)0x93, 0x53, (byte)0xE8, 0x58 }), "AvetanaTest", 5, SDPConstants.UUID_SERIAL_PORT);
    lsr.getSDPRecordXML();
  }
}