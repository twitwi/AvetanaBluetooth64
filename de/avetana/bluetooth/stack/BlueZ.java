/*
*  (c) Copyright 2004 Avetana GmbH ALL RIGHTS RESERVED.
*
* This file is part of the Avetana bluetooth API for Linux.
*
* The Avetana bluetooth API for Linux is free software; you can redistribute it
* and/or modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2 of
* the License, or (at your option) any later version.
*
* The Avetana bluetooth API is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* The development of the Avetana bluetooth API is based on the work of
* Christian Lorenz (see the Javabluetooth Stack at http://www.javabluetooth.org) for some classes,
* on the work of the jbluez team (see http://jbluez.sourceforge.net/) and
* on the work of the bluez team (see the BlueZ linux Stack at http://www.bluez.org) for the C code.
* Classes, part of classes, C functions or part of C functions programmed by these teams and/or persons
* are explicitly mentioned.
*
* @author Julien Campana
*/

package de.avetana.bluetooth.stack;


import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.ServiceRecord;
import javax.swing.JOptionPane;

import de.avetana.bluetooth.connection.ConnectionFactory;
import de.avetana.bluetooth.connection.ConnectionNotifier;
import de.avetana.bluetooth.connection.JSR82URL;
import de.avetana.bluetooth.hci.HCIInquiryResult;
import de.avetana.bluetooth.l2cap.L2CAPConnParam;
import de.avetana.bluetooth.sdp.LocalServiceRecord;
import de.avetana.bluetooth.util.BTAddress;
import de.avetana.bluetooth.util.LibLoader;

/**
 * This class provides the methods to access the underlying BlueZ functions.
 * All native methods defined in this class are implemented via the
 * Java Native Interface (JNI) in C/C++. See the bluez.cpp file and associated
 * comments for full details.
 *
 * All methods are static. This way of defining a common access to the
 * stack avoids the user and the developper to repeatedly create a new instance of the
 * BlueZ class.
 *
 * @author Edward Kay, ed.kay@appliancestudio.com / Julien Campana
 * @version 1.1
 */

public class BlueZ
{
	// Loads the library containing the native code implementation.
	// It is usually called "libjbluez.so" under UNIX/Linux, but is loaded
	// with the "jbluez" string, since this is how JNI implements platform
	// independence.
	static {try {LibLoader.loadCommLib("jbluez"); } catch (Exception e ) { e.printStackTrace(); System.exit(0);} }
        public static ConnectionFactory myFactory=new ConnectionFactory();

        public static int m_transactionId = 0;


	/**
	 * Opens the HCI device.
	 *
	 * @param hciDevID The local HCI device ID (see the hciconfig tool provided
	 *     by BlueZ for further information).
	 * @exception BlueZException Unable to open the HCI device.
	 * @return A device descriptor (often named <code>dd</code>) for the HCI
	 *     device.
	 */
	public synchronized static native int hciOpenDevice(int hciDevID, BlueZ ref) throws BlueZException;

	/* HCI Close Device */
	/**
	 * Close the HCI device.
	 *
	 * @param dd The HCI device descriptor (as returned from
	 *     <code>hciOpenDevice</code>)
	 */
	public synchronized static native void hciCloseDevice(int dd);

	/* HCI Inquiry */
	/**
	 * Performs an HCI inquiry to discover remote Bluetooth devices.
	 *
	 * See HCI_Inquiry in the Bluetooth Specification for further details of
	 * the various arguments.
	 *
	 * @exception BlueZException If the inquiry failed.
	 * @param hciDevID The local HCI device ID (see the hciconfig tool provided
	 *     by BlueZ for further information).
	 * @param len Maximum amount of time before inquiry is halted. Time = len
	 *      x 1.28 secs.
	 * @param max_num_rsp Maximum number of responses allowed before inquiry
	 *     is halted. For an unlimited number, set to 0.
	 * @param flags Additional flags. See BlueZ documentation and source code
	 *      for details.
	 * @return An InquiryInfo object containing the results of the inquiry.
	 * @see #hciInquiry(int hciDevID)
	 */
	public static native HCIInquiryResult hciInquiry(int hciDevID, int len, int max_num_rsp, long flags, DiscoveryAgent agent) throws BlueZException;
	/**
	 * Performs an HCI inquiry to discover remote Bluetooth devices. This is the
	 * same as <code>hciInquiry(int hciDevID, int len, int max_num_rsp, long
	 * flags)</code>, except that the <code>len</code>, <code>max_num_rsp</code>
	 * and <code>flags</code> fields are preset to 'default' values. These
	 * values are 8, 10 and 0, respectively.
	 *
	 * @exception BlueZException If the inquiry failed.
	 * @param hciDevID The local HCI device ID (see the hciconfig tool provided
	 *     by BlueZ for further information)
	 * @return An InquiryInfo object containing the results of the inquiry.
	 */
	public static HCIInquiryResult hciInquiry(int hciDevID, DiscoveryAgent agent) throws BlueZException
	{
            return hciInquiry(hciDevID, 8, 10, 0, agent);
        }

	/* HCI Device Bluetooth Address */
	/**
	 * Gets the Bluetooth device address for a specified local HCI device.
	 *
	 * @exception BlueZException If unable to get the Bluetooth device address.
	 * @param hciDevID The local HCI device ID (see the hciconfig tool provided
	 *     by BlueZ for further information)
	 * @return A BTAddress object representing the Bluetooth device address.
	 */
	public synchronized static native BTAddress hciDevBTAddress(int hciDevID) throws BlueZException;

	/* HCI Device ID */
	/**
	 * Gets the device ID for a specified local HCI device.
	 *
	 * @exception BlueZException If unable to get the device ID.
	 * @param bdaddr Bluetooth address String in the form
	 *     <code>"00:12:34:56:78:9A"</code>.
	 * @return The device ID for the local device.
	 */
	public synchronized static native int hciDeviceID(String bdaddr) throws BlueZException;
	/**
	 * Gets the device ID for a specified local HCI device.
	 *
	 * @exception BlueZException If unable to get the device ID.
	 * @param bdaddr Bluetooth address as a BTAddress object.
	 * @return The device ID for the local device.
	 */
	public synchronized static int hciDeviceID(BTAddress bdaddr) throws BlueZException
	{	return hciDeviceID(bdaddr.toString());	}

	/* HCI Local Name */
	/**
	 * Gets the name of a local device. The device must be opened using
	 * <code>hciOpenDevice</code> before calling this method.
	 *
	 * @exception BlueZException If unable to get the local device name.
	 * @param dd HCI device descriptor.
	 * @param timeOut Timeout, in milliseconds.
	 * @return A String containing the name of the specified local device.
	 */
	public synchronized static native String hciLocalName(int dd, int timeOut) throws BlueZException;
	/**
	 * Gets the name of a local device. The device must be opened using
	 * <code>hciOpenDevice</code> before calling this method. This is the same
	 * as <code>hciLocalName(int dd, int timeOut)</code> with the
	 * <code>timeOut</code> argument set to 10000 (i.e. 10 seconds).
	 *
	 * @param dd HCI device descriptor.
	 * @exception BlueZException If unable to get the local device name.
	 * @return A String containing the name of the specified local device.
	 */
	public synchronized static String hciLocalName(int dd) throws BlueZException
	{	return hciLocalName(dd, 10000);	}

	/* HCI Remote Name */
	/**
	 * Gets the name of a remote device, as specified by its Bluetooth device
	 * address. The local device must be opened using <code>hciOpenDevice</code>
	 * before calling this method.
	 *
	 * @exception BlueZException If unable to get the remote device name.
	 * @param dd HCI device descriptor.
	 * @param bdaddr Bluetooth address String in the form
	 *     <code>"00:12:34:56:78:9A"</code>.
	 * @param timeOut Timeout, in milliseconds.
	 * @return A String containing the name of the specified remote device.
	 */
	public synchronized static native String hciRemoteName(int dd, String bdaddr, int timeOut) throws BlueZException;
	/**
	 * Gets the name of a remote device, as specified by its Bluetooth device
	 * address. The local device must be opened using <code>hciOpenDevice</code>
	 * before calling this method. This is the same as
	 * <code>hciRemoteName(int dd, String bdaddr, int timeOut)</code> with the
	 * <code>timeOut</code> argument set to 10000 (i.e. 10 seconds).
	 *
	 * @param dd HCI device descriptor.
	 * @param bdaddr Bluetooth address String in the form
	 *     <code>"00:12:34:56:78:9A"</code>.
	 * @exception BlueZException If unable to get the remote device name.
	 * @return A String containing the name of the specified remote device.
	 */
	public synchronized static String hciRemoteName(int dd, String bdaddr) throws BlueZException
	{	return hciRemoteName(dd, bdaddr, 10000);	}
	/**
	 * Gets the name of a remote device, as specified by its Bluetooth device
	 * address. The local device must be opened using <code>hciOpenDevice</code>
	 * before calling this method.
	 *
	 * @param dd HCI device descriptor.
	 * @param bdaddr Bluetooth address as a BTAddress object.
	 * @param timeOut Timeout, in milliseconds.
	 * @exception BlueZException If unable to get the remote device name.
	 * @return A String containing the name of the specified remote device.
	 */
	public static String hciRemoteName(int dd, BTAddress bdaddr, int timeOut) throws BlueZException
	{	return hciRemoteName(dd, bdaddr.toString(), timeOut);	}
	/**
	 * Gets the name of a remote device, as specified by its Bluetooth device
	 * address. The local device must be opened using <code>hciOpenDevice</code>
	 * before calling this method. This is the same as
	 * <code>hciRemoteName(int dd, BTAddress bdaddr, int timeOut)</code> with
	 * the <code>timeOut</code> argument set to 10000 (i.e. 10 seconds).
	 *
	 * @param dd HCI device descriptor.
	 * @param bdaddr Bluetooth address as a BTAddress object.
	 * @exception BlueZException If unable to get the remote device name.
	 * @return A String containing the name of the specified remote device.
	 */
	public static String hciRemoteName(int dd, BTAddress bdaddr) throws BlueZException
	{	return hciRemoteName(dd, bdaddr.toString(), 25000);	}

        // Call of the native method openRFCommNative
        private synchronized static native int openRFCommNative (String addr, int channel,  boolean master, boolean auth, boolean encrypt);

        // Call of the native method openL2CAPNative
        private synchronized static native L2CAPConnParam openL2CAPNative (String addr, int psm, boolean master, boolean auth, boolean encrypt,            int receiveMTU, int transmitMTU);

        /**
         * Opens an L2CAP connection with a remote BT device.
         * @param url The JSR82URL object describing the remote BT device (BT address and PSM),
         * the desired security options (master, authenticate, encrypt) and the desired connection options (imtu, omtu)
         * @return An L2CAPConnParam object encapsulating the integer, which uniquely identifies the connection.
         * @throws BlueZException If an error occured in the C part of the Code
         * @throws Exception If the URL is not a valid L2CAP URL.
         */
        public static L2CAPConnParam openL2CAP (JSR82URL url) throws BlueZException, Exception{
          if(url.getBTAddress()==null) throw new Exception("This is not a valid remote L2CAP connection url!");
          int psm=(url.getAttrNumber()==null)?10:url.getAttrNumber().intValue();
          int receiveMTU=-1, transmitMTU=-1;
          try {receiveMTU=Integer.parseInt((String)url.getParameter("receivemtu"));}catch(Exception ex) {}
          try {transmitMTU=Integer.parseInt((String)url.getParameter("transmitmtu"));}catch(Exception ex) {}
          return openL2CAPNative (url.getBTAddress().toString(),
                                   psm,
                                   url.isLocalMaster(),
                                   url.isAuthenticated(),
                                   url.isEncrypted(),
                                   receiveMTU,
                                   transmitMTU);
        }

        /**
         * Opens an RFCOMM connection with a remote BT device.
         * @param url The JSR82URL object describing the remote BT device (BT address and channel number),
         * the desired security options (master, authenticate, encrypt).
         * @return The integer, which uniquely identifies the connection.
         * @throws BlueZException If an error occured in the C part of the Code
         * @throws Exception If the URL is not a valid L2CAP URL.
         */
        public static int openRFComm (JSR82URL url) throws BlueZException, Exception{
          if(url.getBTAddress()==null) throw new Exception("This is not a valid remote RFComm connection url!");
          int channel=(url.getAttrNumber()==null)?1:url.getAttrNumber().intValue();
          return openRFCommNative (url.getBTAddress().toString(),
                                   channel,
                                   url.isLocalMaster(),
                                   url.isAuthenticated(),
                                   url.isEncrypted());
        }

        /**
         * Closes an existing connection.
         * @param fid The integer, which uniquely identifies the connection (the file descriptor under linux).
         */
        public static native void closeConnection (int fid) ;

        /**
         * Reads bytes from an existing connection (RFCOMM, L2CAP or another type of connection)
         * @param fid The integer, which uniquely identifies the connection.
         * @param b The byte array used to store the read bytes
         * @param len The length of b
         * @return THe number of bytes read of -1 if an error occured
         * @throws BlueZException
         */
        public synchronized static native int readBytes (int fid, byte[] b, int len) throws BlueZException;

        /**
         * Writes byte to an existing connection
         * @param fid The integer, which uniquely identifies the connection.
         * @param b The byte array storing the bytes to be written
         * @param len The length of b
         * @param off The offset into b at which to start
         */
        public synchronized static  native void writeBytes (int fid, byte b[], int off, int len);

        /**
         * Lists all SDP services, which match a desired list of UUIDs. Only the attributes contained in attrIds will
         * populate the returned service records.
         * @param bdaddr_jstr The address of the remote BT device
         * @param uuid The list of UUIDs the service record must contain.
         * @param attrIds The list of Attributes which will populate the Service record.
         * @throws BlueZException
         */
        public static synchronized native void listService (String bdaddr_jstr, byte[][] uuid, int[] attrIds, int transID) throws BlueZException;

        /**
         * Stores a new Service record in the BCC.
         * @param service The service record
         * @return a positive integer is the process succeeds.
         * @throws BlueZException
         */
        public synchronized static native int createService(LocalServiceRecord service) throws BlueZException;

        /**
         * Updates an existing service record (the old <service record must be already stored in the BCC.)
         * @param newService The byte representation of this new service record
         * @param length The length of this byte representation
         * @param recordHandle The record handle of the old service record
         * @return a positive integer is the process succeeds.
         * @throws BlueZException
         */
        public synchronized static native int updateService(ServiceRecord service, long recordHandle) throws BlueZException;

        /**
         * Registers the service record identified by the variable "serviceHandle" and listens for an incoming RFCOMM Connection
         * @param serviceHandle The integer, which uniquely identifies the service record
         * @param channel The channel number (Incoming connections linked with a channel number
         * @param master Is the local device master for this connection?
         * @param auth Must the remote device be authenticated during the establishment of the connection
         * @param encrypt Must the ACL linkbe encrpyted during the establishment of the connection
         * @return a positive integer is the process succeeds.
         * @throws BlueZException
         */
        public static native int registerService(int serviceHandle, int channel, boolean master, boolean auth, boolean encrypt) throws BlueZException;

        /**
         * Registers the service record identified by the variable "serviceHandle" and listens for an incoming L2CAP Connection
         * @param serviceHandle The integer, which uniquely identifies the service record
         * @param channel The channel number (Incoming connections linked with a channel number
         * @param master Is the local device master for this connection?
         * @param auth Must the remote device be authenticated during the establishment of the connection
         * @param encrypt Must the ACL linkbe encrpyted during the establishment of the connection
         * @param omtu Set the size of the Output MTU
         * @param imtu Set the size of the Input MTU
         * @return a positive integer is the process succeeds.
         * @throws BlueZException
         */
        public static native int registerL2CAPService(int serviceHandle,
                                                      int channel,
                                                      boolean master,
                                                      boolean auth,
                                                      boolean encrypt,
                                                      int omtu,
                                                      int imtu) throws BlueZException;

        /**
         * Deletes the service record identified by the record handle given as parameter.
         * @param recordHandle
         * @throws BlueZException
         */
        public synchronized static native void disposeLocalRecord(long recordHandle) throws BlueZException;

        /**
         * Gets the access mode of the local device
         * @param device The integer which identifies the local device
         * @return The access mode if this device
         * @throws BlueZException
         */
        public synchronized static native int getAccessMode(int device) throws BlueZException;

        /**
         * Sets the access mode of the local device
         * @param device The integer which identifies the local device
         * @param mode The new access mode
         * @return
         * @throws BlueZException
         */
        public synchronized static native int setAccessMode(int device, int mode) throws BlueZException;

        /**
         * Gets the device class of the local device number "dev_id"
         * @param dev_id The number which identifies the local device
         * @return
         * @throws BlueZException
         */
        public synchronized static native int getDeviceClass(int dev_id) throws BlueZException;

        /**
         * Is master/slave switch allowed?
         * @return <code>true</code> If master/slave switch is allowed<br>
         *         <code>false</code> Otherwise
         * @throws BlueZException
         */
        public synchronized static native boolean isMasterSwitchAllowed() throws BlueZException;

        /**
         * Returns the maximum number of connected devices allowed by the stack
         * This number may be greater than 7 if the implementation handles parked connections.
         *  The string will be in Base 10 digits.
         * @return The maximum number of connected devices allowed by the stack
         * @throws BlueZException
         */
        public synchronized static native int getMaxConnectedDevices() throws BlueZException;

        /**
         * Inquiry scanning allowed during connection?
         * @return <code>true</code> If the inquiry scanning is allowed during a connection<br>
         *         <code>false</code> Otherwise
         * @throws BlueZException
         */
        public synchronized static native boolean inquiryScanAndConAllowed() throws BlueZException;

        /**
         * Is Inquiry allowed during a connection?
         * @return <code>true</code> If the inquiry is allowed during a connection<br>
         *         <code>false</code> Otherwise
         * @throws BlueZException
         */
        public synchronized static native boolean inquiryAndConAllowed() throws BlueZException;

        /**
         * Page scanning allowed during connection?
         * @return <code>true</code> If page scanning during a connection is allowed <br>
         *         <code>false</code> Otherwise
         * @throws BlueZException
         */
        public synchronized static native boolean pageScanAndConAllowed() throws BlueZException;

        /**
         * Is paging allowed during a connection?
         * In other words, can a connection be established to one device if
         * it is already connected to another device.
         * @return <code>true</code> If paging during a connection is allowed <br>
         *         <code>false</code> Otherwise
         * @throws BlueZException
         */
        public static native boolean pageAndConnAllowed() throws BlueZException;

        /**
         * Authenticates the remote device (an connection between the local and the remote device MUST exists)
         * @param handle The number, which uniquely identifies the connection
         * @param deviceAddr The BT address of the remote device
         * @return
         * @throws BlueZException
         */
        public synchronized static native int authenticate(int handle, String deviceAddr) throws BlueZException;

        /**
         * Turns on/off the encryption of an ACL link
         * @param handle The number, which uniquely identifies the connection
         * @param deviceAddr The BT address of the remote device
         * @param enable If true, turn on the encryption of the ACL link
         * @return
         * @throws BlueZException
         */
        public synchronized static native int encrypt(int handle, String deviceAddr, boolean enable) throws BlueZException;

        /**
         * Retrieves the connection options
         * @param handle The number, which uniquely identifies the connection
         * @param deviceAddr The BT address of the remote device
         * @return
         * @throws BlueZException
         */
        public synchronized static native boolean[] connectionOptions(int handle, String deviceAddr) throws BlueZException;


        /**
         * Popup used to enter the PIN code (used by encrypted connections)
         * @return The PIN code entered by the user
         */
        public synchronized static String showPinRequest() {
          String input = JOptionPane.showInputDialog(null,"Please enter Bluetooth PIN Code","Bluetooth PIN",
              JOptionPane.QUESTION_MESSAGE);
          if(input.length() > 16) input=input.substring(0,15);
          return input;
        }

        // Debug method
        public synchronized static void debugPrintStr(String str)  {
          System.out.println("Function debugPrintStr called!!!!");
          System.out.println("\nJAVA: print string="+str);
        }

        /**
         * Starts a service search.
         * @param bdaddr_jstr The BT address of the remote device
         * @param uuid The list of UUIDs the services must match
         * @param attrIds The list of attributes the services should contain
         * @param listener The discovery listener, which handles the callback methods.
         * @throws BlueZException
         */
        public static void searchServices(String bdaddr_jstr, byte[][] uuid, int[] attrIds, DiscoveryListener listener) throws BlueZException{
           m_transactionId++;
           myFactory.addListener(m_transactionId, listener);
           listService(bdaddr_jstr, uuid, attrIds, m_transactionId);
        }

        /**
         * Registers a service and waits for incoming connections
         * @param a_notifier The connection notifier created by the user.
         * @throws Exception
         */
        public static void registerNotifier(ConnectionNotifier a_notifier) throws Exception{
          if(a_notifier.getConnectionURL()==null) throw new Exception("No connection URL previously defined!");
          myFactory.addNotifier(a_notifier);
          short proto=(a_notifier.getConnectionURL()==null?JSR82URL.PROTOCOL_RFCOMM:a_notifier.getConnectionURL().getProtocol());
          int defaultCh=(proto==JSR82URL.PROTOCOL_RFCOMM?1:10);
          int channel=(a_notifier.getConnectionURL().getAttrNumber()!=null?
                      a_notifier.getConnectionURL().getAttrNumber().intValue():defaultCh);
          //System.out.println ("Registered notifier at channel " + channel)
          if(proto==JSR82URL.PROTOCOL_L2CAP) {
            registerL2CAPService((int)a_notifier.getServiceHandle(),
                            channel,
                            a_notifier.getConnectionURL().isLocalMaster(),
                            a_notifier.getConnectionURL().isAuthenticated(),
                            a_notifier.getConnectionURL().isEncrypted(), -1,-1);
          } else {
            registerService((int)a_notifier.getServiceHandle(),
                            channel,
                            a_notifier.getConnectionURL().isLocalMaster(),
                            a_notifier.getConnectionURL().isAuthenticated(),
                            a_notifier.getConnectionURL().isEncrypted());
          }
        }

        /**
         * Remove the Notifier when creating a service hass failed
         */

        public static void removeNotifier(ConnectionNotifier a_notifier) {
          myFactory.removeNotifier(a_notifier);
        }

        /**
         * Method called by the C-Code in order to notify the establishment of a new connection.
         * This method is always called after the call of registerNotifier.
         * @param fid The number which uniquely identifies the connection
         * @param channel The channel or PSM number
         * @param protocol The protocol (see the class JSR82URL)
         * @param jaddr The BT address of the remote device
         * @return
         */
        public static boolean connectionEstablished(int fid, int channel, int protocol,String jaddr) {
          for(int i=0;i<myFactory.getNotifiers().size();i++) {
            try {
              ConnectionNotifier not=(ConnectionNotifier)myFactory.getNotifiers().elementAt(i);
              // Nur eine Verbindung pro Kanal!
              short proto=(not.getConnectionURL()==null?JSR82URL.PROTOCOL_RFCOMM:not.getConnectionURL().getProtocol());
              if (proto == JSR82URL.PROTOCOL_OBEX) proto = JSR82URL.PROTOCOL_RFCOMM;
              int defaultCh=(proto==JSR82URL.PROTOCOL_RFCOMM?1:10);
              int ch=(not.getConnectionURL()!=null && not.getConnectionURL().getAttrNumber()!=null)?
                      not.getConnectionURL().getAttrNumber().intValue():defaultCh;
              if(ch==channel && protocol==proto) {
                not.setConnectionID(fid);
                if(jaddr!=null) not.setRemoteDevice(jaddr);
                myFactory.removeNotifier(not);
                return true;
              }
            }catch(Exception ex) {ex.printStackTrace();}
          }
          System.err.println ("Notifier not found for " + fid + " " + channel + " in " + myFactory.getNotifiers().size());
          return false;
        }

        public static boolean cancelServiceSearch(int transID)  {
        	  DiscoveryListener dl = myFactory.getListener(transID);
        	  if (dl != null) dl.serviceSearchCompleted(transID, DiscoveryListener.SERVICE_SEARCH_TERMINATED);
          myFactory.removeListener(transID);
          return true;
        }

        /**
         * Callback method, which notifies the discovering a new service.
         * @param transID SDP transaction ID
         * @param rec The service record discovered
         */
        public static void addService(int transID, ServiceRecord rec) {
          DiscoveryListener myListener=myFactory.getListener(transID);
          if(myListener==null) {
            //System.out.println("ERROR - Listener not defined. Unable to add service " + transID);
            return;
          }
          myListener.servicesDiscovered(transID, new ServiceRecord[]{rec});
        }

        /**
         * Callback method which notifies the end of a service search.
         * @param transID SDP transaction code
         * @param respCode The responde code of the C-implementation
         * @param jBTAddr The BT address of the remote device
         */
        public static void serviceSearchComplete(int transID, int respCode) {
          DiscoveryListener myListener=myFactory.getListener(transID);
          if(myListener==null) {
            //System.out.println("ERROR - Listener not defined. Unable to interpret service search completed code");
            return;
          }
          myListener.serviceSearchCompleted(transID,respCode);
          myFactory.removeListener(transID);

        }

        public Class createClass (String name) {
          try {
            return Class.forName(name.replaceAll("/", "."));
          } catch (Exception e) {
            e.printStackTrace();
          }
          return null;
        }

        public static byte[] newByteArray (int size) {
        		return new byte[size];
        }
}
