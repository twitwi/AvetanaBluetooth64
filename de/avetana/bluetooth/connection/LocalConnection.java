package de.avetana.bluetooth.connection;

/**
 * The class used to manage RFCOMM or RFCOM-based client connection.
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
 * This class extends the class BluetoothStream and implements, as requested by the JSR82 specification
 * the interface StreamConnection. Typically, this class will be used to manage RFCOMM or RFCOMM-based
 * connections
 *
 * @see de.avetana.bluetooth.connection.BluetoothStream
 * @author Julien Campana
 */

public class LocalConnection extends BluetoothStream {

  private JSR82URL m_connectionURL;

  /**
   * Creates a new instance of LocalConnection.
   * @param fid The connection ID
   */
  public LocalConnection(int fid) {
    super(fid);
  }

  /**
   * Change or set the connection URL
   * @param a_url The new connection URL
   */
  public void setConnectionURL(JSR82URL a_url) {
    m_connectionURL=a_url;
  }

}