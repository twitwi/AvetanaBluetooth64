package de.avetana.bluetooth.sdp;

import javax.bluetooth.*;

import java.io.*;
import java.util.*;
import de.avetana.bluetooth.util.*;
import de.avetana.bluetooth.connection.*;

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
    rec.m_type=protocol;
    DataElement serviceClassIDList = new DataElement(DataElement.DATSEQ);
    DataElement scidProt = null;
    if ((svcID.toLong() & 0x1100l) == 0x1100l) ;
    else if(protocol==JSR82URL.PROTOCOL_RFCOMM) scidProt = new DataElement(DataElement.UUID, new UUID(SDPConstants.UUID_SERIAL_PORT));
    else if(protocol==JSR82URL.PROTOCOL_OBEX) scidProt = new DataElement(DataElement.UUID, new UUID(SDPConstants.UUID_OBEX_OBJECT_PUSH));
    if (scidProt != null) serviceClassIDList.addElement(scidProt);
    if (svcID != null && !svcID.equals (UUID.NULL_UUID) && (scidProt == null || !svcID.equals(scidProt.getValue())))  
    		serviceClassIDList.addElement(new DataElement (DataElement.UUID, svcID));
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
    if (protocol == JSR82URL.PROTOCOL_RFCOMM) {
    		profileDescriptor.addElement(new DataElement(DataElement.UUID, new UUID(SDPConstants.UUID_SERIAL_PORT)));
    		profileDescriptor.addElement(new DataElement(DataElement.U_INT_2, 256));
    } else if (protocol == JSR82URL.PROTOCOL_OBEX) {
		profileDescriptor.addElement(new DataElement(DataElement.UUID, new UUID(SDPConstants.UUID_OBEX_OBJECT_PUSH)));
		profileDescriptor.addElement(new DataElement(DataElement.U_INT_2, 256));
    }
    	profileDescriptorList.addElement(profileDescriptor);
    rec.m_attributes.put(new Integer(9), profileDescriptorList);
    rec.m_attributes.put(new Integer(256), new DataElement(DataElement.STRING, name));

    if(protocol==JSR82URL.PROTOCOL_OBEX) {
    		DataElement sfl = new DataElement (DataElement.DATSEQ);
    		sfl.addElement(new DataElement (DataElement.U_INT_1, 1));
    		sfl.addElement(new DataElement (DataElement.U_INT_1, 2));
    		sfl.addElement(new DataElement (DataElement.U_INT_1, 4));
    		sfl.addElement(new DataElement (DataElement.U_INT_1, 5));
    		sfl.addElement(new DataElement (DataElement.U_INT_1, 6));
    		sfl.addElement(new DataElement (DataElement.U_INT_1, 255));
    		rec.m_attributes.put(new Integer (SDPConstants.ATTR_SUPPORTED_FORMATS_LIST), sfl);
    		rec.m_attributes.put(new Integer (SDPConstants.ATTR_SERVICE_AVAILABILITY), new DataElement (DataElement.U_INT_1, 255));
    }
    
    return rec;
  }

  /**
   * Transforms the service record object into an XML element and returns the string representation
   * of this XML element. Useful for the Mac implementation
   * @return The XML-based string representation of the service record
   */
    
  public String getSDPRecordXML() {
    try {
      File f = null;
      do {
      	int counter = (int)(1000f * Math.random());
      	f = new File (System.getProperty("java.io.tmpdir") + File.separator + "serviceRecord" + counter + ".xml");
      } while (f.exists());

      PElement plist = new PElement ("plist");
      plist.setAttribute("version", "0.9");
      PElement dict = new PElement ("dict");
      plist.addChild(dict);
      dict.addChild(new PElement ("key", "0000 - ServiceRecordHandle*"));
      dict.addChild(new PElement ("integer", "65540"));
      for (int i = 1;i < 65535;i++) {
      	DataElement de = this.getAttributeValue(i);
      	if (de == null) continue;
      	PElement depe = makePElement (de);
      	if (depe == null) continue;
      	String attId = "" + Integer.toHexString(i);
      	while (attId.length() < 4) attId = "0" + attId;
        dict.addChild(new PElement ("key", attId));
      	dict.addChild (depe);
      }

      FileOutputStream fos = new FileOutputStream (f);
      plist.writeXML(fos);

      return f.getAbsolutePath();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  
  private PElement makePElement (DataElement e) {
  	PElement ret = null;
  	switch (e.getDataType()) {
  		case DataElement.STRING:
 		case DataElement.URL:
  			//try {
			//	return newDataElement (4, ((String)e.getValue()).getBytes("UTF-8"));
			//} catch (UnsupportedEncodingException e1) {
				return new PElement ("string", (String)e.getValue());
			//}
 		case DataElement.U_INT_1:
  			return newDataElement (1, 1, e.getLong());
  		case DataElement.U_INT_2:
  			return newDataElement (2, 1, e.getLong());
  		case DataElement.U_INT_4:
  			return newDataElement (4, 1, e.getLong());
  		case DataElement.INT_1:
  			return newDataElement (1, 2, e.getLong());
  		case DataElement.INT_2:
  			return newDataElement (2, 2, e.getLong());
  		case DataElement.INT_4:
  			return newDataElement (4, 2, e.getLong());
  		case DataElement.UUID:
  			return new PElement ("data", new String (Base64.encode (((UUID)e.getValue()).toByteArray())));
  		case DataElement.DATSEQ:
  			ret = new PElement ("array");
  			Enumeration v = (Enumeration)e.getValue();
  			while (v.hasMoreElements())
  				ret.addChild(makePElement ((DataElement)v.nextElement()));
  			
  			return ret;
  		default:
  			System.err.println ("Unhandeled DataElement type" + e.getDataType());
  	}
  	return ret;
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
    parent.addElement(new DataElement((getProtocol() == JSR82URL.PROTOCOL_RFCOMM || getProtocol() == JSR82URL.PROTOCOL_OBEX) ? DataElement.U_INT_1 : DataElement.U_INT_2, newChannel));

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
      //System.out.println ("Found protocolDescriptorElement " + protocolDescriptorElement);
      if (protocolDescriptorElement == null)
        throw new IllegalArgumentException("Protocol Descriptor is missing. You should maybe populate this Service Record with attrId=0x0004");
      Enumeration protocolParameterList = (Enumeration)protocolDescriptorElement.getValue();

      if (protocolParameterList.hasMoreElements()) {
        DataElement protocolDescriptor = (DataElement)protocolParameterList.nextElement();
        //System.out.println ("Found protocolDescriptor " + protocolDescriptor.getValue());

        if (protocolDescriptor != null) {
          if (protocolDescriptor.getDataType() == DataElement.UUID) {
            UUID protocolDescriptorUUID = (UUID)protocolDescriptor.getValue();
            long lg=protocolDescriptorUUID.toLong();
            //System.out.println ("Descriptor long Value " + lg);
            if ( lg == 0x0003 || lg == 0x100) {
              if (protocolParameterList.hasMoreElements()) {
                DataElement protocolPSMElement = (DataElement)protocolParameterList.nextElement();
                //System.out.println ("Found parameter " + protocolPSMElement.getDataType());
                
                if (protocolPSMElement != null) {
                   if((lg == 0x3 && protocolPSMElement.getDataType() == DataElement.U_INT_1) ||
                      (lg == 0x100 && protocolPSMElement.getDataType() == DataElement.U_INT_2))
                      return protocolPSMElement;
                   //else System.out.println ("protocollPSMElement has no matching type " + lg + " " + protocolPSMElement.getDataType());
                  }
                
              }
            }
            //else if(lg == 0x0008) System.err.println("OBEX FOUND!!!");
            else continue;
          }
        }
      }
    }
    //System.out.println ("No PSM Element found");
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
            //else if(lg == 0x0008) System.err.println("OBEX FOUND!!!");
            else continue;
          }
        }
      }
    }
    return null;
  }

  private PElement newDataElement (int size, int type, long value) {
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
      dict.addChild(new PElement ("data", new String (Base64.encode(value))));
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
  	String url = "";
  	if (getProtocol() == JSR82URL.PROTOCOL_L2CAP) url = "btl2cap://";
  	if (getProtocol() == JSR82URL.PROTOCOL_RFCOMM) url = "btspp://";
  	if (getProtocol() == JSR82URL.PROTOCOL_OBEX) url = "btgoep://";
  	try {
		url += LocalDevice.getLocalDevice().getBluetoothAddress() + ":";
	} catch (BluetoothStateException e) {
		e.printStackTrace();
	}
  	url += getChannelNumberElement().getLong();
  	if (mustBeMaster) url += ";master=true";
  	return url;
  }

  /**
   * Method not yet supported. Throws a new exception.
   * @return  nothing - Throws an Exception
   */
  public void setDeviceServiceClasses(int classes) {
    throw new java.lang.UnsupportedOperationException("Method setDeviceServiceClasses() not yet implemented.");
  }

  /**
   * Returns the ServiceClassID of this service
   * @return serviceClassID UUID
   */

  public UUID getServiceClassID() {
    UUID u[] = getServiceClassIDList();
    return u.length == 0 ? null : u[u.length - 1];
  }
  
  public UUID[] getServiceClassIDList() {
  		Enumeration enum = (Enumeration)getAttributeValue(SDPConstants.ATTR_SERVICE_CLASS_ID_LIST).getValue();
  		Vector v = new Vector ();
  		while (enum.hasMoreElements()) v.add(((DataElement)enum.nextElement()).getValue());
  		UUID ret[] = new UUID[v.size()];
  		v.toArray(ret);
  		return ret;
  }

}