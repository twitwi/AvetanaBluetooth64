/*
 * Created on 28.10.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.avetana.bluetooth.test;

import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.*;

/**
 * @author gmelin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class InqTest implements DiscoveryListener {

	/* (non-Javadoc)
	 * @see javax.bluetooth.DiscoveryListener#deviceDiscovered(javax.bluetooth.RemoteDevice, javax.bluetooth.DeviceClass)
	 */
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.bluetooth.DiscoveryListener#servicesDiscovered(int, javax.bluetooth.ServiceRecord[])
	 */
	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		// TODO Auto-generated method stub
		System.out.println (servRecord.length + " services discovered ");
		for (int i = 0;i < servRecord.length;i++) {
			try {
				System.out.println ("Record " + (i + 1));
				System.out.println ("" + servRecord[i]);
				} catch (Exception e) { e.printStackTrace(); }
 		}

	}

	/* (non-Javadoc)
	 * @see javax.bluetooth.DiscoveryListener#serviceSearchCompleted(int, int)
	 */
	public void serviceSearchCompleted(int transID, int respCode) {
		// TODO Auto-generated method stub
		System.out.println ("Service search completed");

	}

	/* (non-Javadoc)
	 * @see javax.bluetooth.DiscoveryListener#inquiryCompleted(int)
	 */
	public void inquiryCompleted(int discType) {
		// TODO Auto-generated method stub

	}

	public InqTest (String addr, String uuid) throws Exception {
		UUID uuids[];
		if (uuid == null) uuids = new UUID[] { };
		else uuids = new UUID[] { new UUID (uuid, false) };
		LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(new int[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x100, 0x303 }, uuids, new RemoteDevice (addr), this);
	}
	
	public static void main(String[] args) throws Exception {
		new InqTest(args[0], args.length == 1 ? null : args[1]);
	}
}
