/**
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
package javax.microedition.io;

import java.io.*;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import de.avetana.bluetooth.rfcomm.*;
import de.avetana.bluetooth.l2cap.*;
import javax.bluetooth.RemoteDevice;
import de.avetana.bluetooth.stack.*;
import de.avetana.bluetooth.connection.*;

/**
 * This class only supports the RFCOMM Protocol for the moment.
 * It will be soon extended in order to support RFCOMM and maybe OBEX.
 *
 * Remote (btspp://010203040506:1;master=false)
 * or
 * local (btspp://localhost:3B9FA89520078C303355AAA694238F07:1;name=Avetana Service;) URLs
 * are supported. The class JSR82URL verifies that the URL is a correct Bluetooth
 * connection URL, which matches the RFC 1808 specification.
 * (see http://www.w3.org/Addressing/rfc1808.txt for more information).
 *
 *
 *
 */
public class Connector {
    public static final int READ       = 0;
    public static final int WRITE      = 0;
    public static final int READ_WRITE = 0;

    static { try { BluetoothStack.getBluetoothStack(); } catch (Exception e) {}}

    public static Connection open(String url) throws IOException {

        try {
          JSR82URL myURL=new JSR82URL(url);
          if((myURL.isAuthenticated() || myURL.isEncrypted() || myURL.isAuthorized()) &&
             System.getProperty("os.name").toLowerCase().equals("linux") && myURL.getProtocol()!=JSR82URL.PROTOCOL_L2CAP)
                throw new IOException("The current implementation of Bluetooth under linux does not support secured connections");
          if(myURL.getBTAddress()==null) {
            if(myURL.getProtocol()!=JSR82URL.PROTOCOL_L2CAP) return new LocalConnectionNotifier(myURL);
            else return new L2CAPConnectionNotifierImpl(myURL);
          }
          else {
            BluetoothStack bluetooth  = BluetoothStack.getBluetoothStack();
            if(myURL.getProtocol() == JSR82URL.PROTOCOL_RFCOMM)
              return bluetooth.openRFCommConnection(myURL);
            else if(myURL.getProtocol() == JSR82URL.PROTOCOL_L2CAP)
              return bluetooth.openL2CAPConnection(myURL);
          }
        }
        catch (BluetoothStateException e) { throw new IOException("" + e); }
        catch (Exception e) { throw new IOException("" + e); }
        throw new IllegalArgumentException(url+" is not a valid Bluetooth connection URL");
    }

    /**
     * Create and open a Connection.
     * @param url The URL for the connection.
     * @param mode The access mode
     * @return A new Connection object.
     * @throws IllegalArgumentException If a parameter is invalid.
     * @throws ConnectionNotFoundException If the connection cannot be found.
     * @throws IOException If some other kind of I/O error occurs.
     * This call is equivilant to Connection.open(url).
     */
    public static Connection open(String url, int mode) throws IOException {
      throw new RuntimeException("Not implemented");
    }

    /**
     * Create and open a Connection
     * @param url The URL for the connection.
     * @param mode The access mode
     * @param timeouts A flag to indicate that the called wants timeout exceptions
     * @return A new Connection object.
     * @throws IllegalArgumentException If a parameter is invalid.
     * @throws ConnectionNotFoundException If the connection cannot be found.
     * @throws IOException If some other kind of I/O error occurs.
     * This call is equivilant to Connection.open(url).
     */
    public static Connection open(String url, int mode, boolean timeouts) throws IOException {
      throw new RuntimeException("Not implemented");
    }

    /**
     * Create and open a connection input stream. This method is not implemented and will throw a Runtime Exception.
     * @param url The URL for the connection.
     * @return A DataInputStream.
     * @throws IllegalArgumentException If a parameter is invalid.
     * @throws ConnectionNotFoundException If the connection cannot be found.
     * @throws IOException If some other kind of I/O error occurs.
     */
    public static DataInputStream openDataInputStream(String url) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Create and open a connection output stream. This method is not implemented and will throw a Runtime Exception.
     * @param url The URL for the connection.
     * @return A DataOutputStream.
     * @throws IllegalArgumentException If a parameter is invalid.
     * @throws ConnectionNotFoundException If the connection cannot be found.
     * @throws IOException If some other kind of I/O error occurs.
     */
    public static DataOutputStream openDataOutputStream(String url) { //TODO openDataOutputStream
        throw new RuntimeException("This implementation of javax.microedition.io.Connector only supports open(String url).");
    }

    /**
     * Create and open a connection input stream. This method is not implemented and will throw a Runtime Exception.
     * @param url The URL for the connection.
     * @return A InputStream.
     * @throws IllegalArgumentException If a parameter is invalid.
     * @throws ConnectionNotFoundException If the connection cannot be found.
     * @throws IOException If some other kind of I/O error occurs.
     */
    public static InputStream openInputStream(String url) { //TODO openInputStream
        throw new RuntimeException("This implementation of javax.microedition.io.Connector only supports open(String url).");
    }

    /**
     * Create and open a connection input stream. This method is not implemented and will throw a Runtime Exception.
     * @param url The URL for the connection.
     * @return A DataOutputStream.
     * @throws IllegalArgumentException If a parameter is invalid.
     * @throws ConnectionNotFoundException If the connection cannot be found.
     * @throws IOException If some other kind of I/O error occurs.
     */
    public static OutputStream openOutputStream(String url) { //TODO openOutputStream
        throw new RuntimeException("This implementation of javax.microedition.io.Connector only supports open(String url).");
    }
}

