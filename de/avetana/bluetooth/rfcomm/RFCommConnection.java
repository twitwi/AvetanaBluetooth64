package de.avetana.bluetooth.rfcomm;


import de.avetana.bluetooth.stack.BlueZ;
import de.avetana.bluetooth.connection.*;

/**
 * The class used to open new RFCOMM client-connections.
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
 * <b>Description: </b><br>
 * This class opens a new RFCOMM connection with a remote BT device.
 * @see de.avetana.bluetooth.connection.BluetoothStream
 */

public class RFCommConnection extends BluetoothStream {

  /**
   * Creates a new instance of RFCommConnection.
   * @param fid A number whcih uniquely identifies the Connection (under linux, this number is the file descriptor)
   * @param addr The address of the remote device.
   */
  protected RFCommConnection(int fid, String addr) {
    super(fid, addr);
  }

  /**
   * Creates a new RFCOMM connection with a remote BT device.
   * @param url The connection URL encapsulating all connection options
   * @return An instance of RFCommConnection with manages the Output and InputStream connections streams
   * @throws Exception
   */
  public static RFCommConnection createRFCommConnection (JSR82URL url) throws Exception{
    int fid = -1;

    fid=BlueZ.openRFComm (url);
    if(fid < 0) throw new Exception("Connection could not be created with remote device!");

    RFCommConnection conn =  null;
    conn=new RFCommConnection (fid, url.getBTAddress().toString());
    conn.startReading();
    return conn;
  }
}
