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
* The developpement of the Avetana bluetooth API is based on the work of
* Christian Lorenz (see the Javabluetooth Stack at http://www.javabluetooth.org) for some classes,
* on the work of the jbluez team (see http://jbluez.sourceforge.net/) and
* on the work of the bluez team (see the BlueZ linux Stack at http://www.bluez.org) for the C code.
* Classes, part of classes, C functions or part of C functions programmed by these teams and/or persons
* are explicitly mentioned.
*
* @author Julien Campana
*/

package de.avetana.bluetooth.connection;

import javax.bluetooth.*;


public class ConnectionDescriptor {

  public JSR82URL m_connectionURL;
  public int m_fid;
  public RemoteDevice m_remote;
  public short m_connectionType;

  public final static short CLIENT_CONNECTION=0x0;
  public final static short SERVER_CONNECTION=0x1;

  public ConnectionDescriptor(JSR82URL connectionURL, short type) {
    m_connectionURL=connectionURL;
    m_connectionType=type;

  }

  public ConnectionDescriptor(JSR82URL connectionURL, short type, int fid) {
    this(connectionURL,type);
    m_fid=fid;
  }
}