package de.avetana.bluetooth.sdp;

/**
 * A time out Exception used by the DiscoveryListener.
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
 * This exception occurs when the timeout defined by the programmer is over. <br>
 * BlueZ as well as other stacks is not endless waiting for the answer of the remote or the local device
 * but it does not return any special error code, identifying a timeout error. That's why this class is only
 * used with the Java part of the implementation. However, it would be a great idea to extend its use as soon as
 * the C-librairies better identifies the different error code. </p>
 * @author Julien Campana
 * @version 1.0
 */
public class TimeOutException extends Exception {

  /**
   * Default constructor
   */
  public TimeOutException() {
  }

  /**
   * Creates a new TimeOutException and sets the exception message
   * @param s The message of this exception.
   *
   */
  public TimeOutException(String s) {
    super(s);
  }

}