package de.avetana.bluetooth.hci;

import java.util.*;
import javax.bluetooth.RemoteDevice;

/**
 * The class used to store the result of an HCI inquiry.
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
 * This class is used by the C-part of the implementation to store the result of an
 * HCI inquiry.
 *
 * @author Julien Campana
 */
public class HCIInquiryResult {
        /**
         * The number of devices that responded.
         */
        public byte num_responses;
        /**
         * The details of each device which responded to the Inquiry.
         */
        private Vector m_devices = new Vector();

        /**
         * Default constructor.
         */
        public HCIInquiryResult(){
        }

        /**
         * Creates a new HCIInquiryResult object and set the number of responses given by the stack.
         *
         * @param _num_responses The number of responses.
         */
        public HCIInquiryResult(byte num_responses) {
          System.out.println("Creating an HCIInquiryResult object!!!");
          this.num_responses = num_responses;
        }

        /**
         * Adds an instance of RemoteDevice object to the Vector of devices.
         *
         * @param dev An instance of RemoteDevice.
         */
        public void addDevice(RemoteDevice dev) {
          m_devices.addElement(dev);
        }

        /**
         * Returns the Vector representation of the RemoteDevice objects.
         *
         * @return Devices found.
         */
        public Vector getDevices() {
          return m_devices;
        }
}
