package de.avetana.bluetooth.sdp;

import javax.bluetooth.*;
import java.util.*;

/**
 * The top-class implementing methods common with all instances of Service Record.
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
 * The Avetana Bluetooth implementation provides two classes, which aim to manage the service record, and separates therefore
 * local from remote service record.<br>
 * This separation is made in order to better implement the JSR82 specification. But
 * common methods of these two classes exist. The SDPServiceRecord class aims to implement these common methods.<br>
 *
 *
 * @author Julien Campana
 */

public abstract class SDPServiceRecord implements ServiceRecord{

  /**
   * The Hashtable containing the service attributes and their related DataElement
   */
  protected Hashtable m_attributes;

  /**
   * The handle of this service record
   */
  protected long m_recordHandle;

  /**
   * Creates a new SDP service record.<br>
   * Initializes the different class variables except the record handle.
   */
  public SDPServiceRecord() {
    m_attributes=new Hashtable();
  }

  /**
   * Creates a new SDP service record, initializes the different class variables and sets the record handle.
   * @param recordHandle
   */
  public SDPServiceRecord(long recordHandle) {
    this();
    m_recordHandle=recordHandle;
  }

  /**
   * Sets the record handle
   * @param recordHandle The value of the record handle
   */
  public void setRecordHandle(long recordHandle) {
    this.m_recordHandle = recordHandle;
  }

  /**
   * Returns the record handle.
   * @return The record handle.
   */
  public long getRecordHandle() {return this.m_recordHandle;}

  /**
   * Returns the DataElement corresponding to the given attribute
   * @param attrID The value of the attribute
   * @return <ul><li>The DataElement corresponding to the given attribute if it exists</li><li><code>null</code> - Otherwise</li>
   */
  public DataElement getAttributeValue(int attrID) {
    return (DataElement)m_attributes.get(new Integer(attrID));
  }

  public abstract RemoteDevice getHostDevice();


  public int[] getAttributeIDs() {
    int[] returnArray;

    if(m_attributes==null || m_attributes.keySet()==null) return new int[]{};
    Object keys[] = m_attributes.keySet().toArray();
    returnArray = new int[keys.length];
    for (int i = 0; i < returnArray.length; i++) { returnArray[i] = ((Integer)keys[i]).intValue(); }

    return returnArray;
  }

  /**
   *
   * @param attrID
   * @param attrDesc
   * @return
   */
  public boolean setAttributeValue(int attrID, DataElement attrDesc) {
  	if(attrID==0x0000) {
      m_recordHandle=attrDesc.getLong();
    }
    Integer i=new Integer(attrID);
    DataElement d=(DataElement)m_attributes.get(i);
    if(d==null) m_attributes.put(i,attrDesc);
    else {
      m_attributes.remove(d);
      m_attributes.put(i, attrDesc);
    }
    return true;
  }

  /**
   * Returns the String representation of a service record, which lists all attributes and their
   * corresponding DataElement
   * @return The String representation of a service record.
   */
  public String toString() {
    String retour="";
    Object[] obj=m_attributes.keySet().toArray();
    for(int i=0;i<obj.length;i++) {
      DataElement dat=(DataElement)m_attributes.get(obj[i]);
      retour+="ID="+obj[i]+" value="+dat+"\n";
    }
    return retour;
  }

  /**
   * The byte representation of this record.
   * @return The byte representation of this record.
   */
  public byte[] toByteArray() {
  DataElement resultAttributes = new DataElement(DataElement.DATSEQ);
  // Use of a Tree Set to sort the attribute IDs.
  // Some stacks do not store all keys if they are not sorted.
  TreeSet mySet=new TreeSet(m_attributes.keySet());
  Object[] keys=mySet.toArray();
  for(int i=0;i<keys.length;i++) {
      Integer key = (Integer)keys[i];
      DataElement value = (DataElement)m_attributes.get(key);
      resultAttributes.addElement(new DataElement(DataElement.U_INT_2, key.intValue()));
      resultAttributes.addElement(value);
  }
  return resultAttributes.toByteArray();
}

}
