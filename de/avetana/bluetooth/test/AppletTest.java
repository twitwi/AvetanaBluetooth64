/*
 * Created on 01.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.avetana.bluetooth.test;
import java.applet.*;
import java.awt.*;
import java.io.IOException;

import javax.bluetooth.*;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import de.avetana.bluetooth.connection.ConnectionNotifier;

public class AppletTest extends Applet implements DiscoveryListener {

	    StringBuffer buffer;

	    public void init() {
	        buffer = new StringBuffer();
	        addItem("initializing... ");
	    }

	    public void start() {
	        addItem("starting... with inq");
	        try {
				LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, this);
			} catch (BluetoothStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }

	    public void stop() {
	        addItem("stopping... ");
	    }

	    public void destroy() {
	        addItem("preparing for unloading...");
	    }

	    void addItem(String newWord) {
	        System.out.println(newWord);
	        buffer.append(newWord);
	        repaint();
	    }

	    public void paint(Graphics g) {
	        //Draw a Rectangle around the applet's display area.
	        g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

	        //Draw the current string inside the rectangle.
	        g.drawString(buffer.toString(), 5, 15);
	    }

		/* (non-Javadoc)
		 * @see javax.bluetooth.DiscoveryListener#deviceDiscovered(javax.bluetooth.RemoteDevice, javax.bluetooth.DeviceClass)
		 */
		public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
			try {
				addItem ("Device found " + btDevice.getBluetoothAddress() + " " + btDevice.getFriendlyName(false));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		/* (non-Javadoc)
		 * @see javax.bluetooth.DiscoveryListener#servicesDiscovered(int, javax.bluetooth.ServiceRecord[])
		 */
		public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see javax.bluetooth.DiscoveryListener#serviceSearchCompleted(int, int)
		 */
		public void serviceSearchCompleted(int transID, int respCode) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see javax.bluetooth.DiscoveryListener#inquiryCompleted(int)
		 */
		public void inquiryCompleted(int discType) {
			addItem ("Discovery Finished now connecting");
			StreamConnection c = null;
			StreamConnectionNotifier conNot = null;
			try {
				conNot = (StreamConnectionNotifier)Connector.open ("btspp://localhost:00112233445566778899aabbccddeeff;name=appletTest;authenticate=false;encrypt=false");
				addItem ("Connected1 " + conNot);
				c = conNot.acceptAndOpen();
				addItem ("Connected2 " + c);
				c.openDataOutputStream().write("Hello world".getBytes());
				c.openDataOutputStream().flush();
				synchronized (this) { wait (1000); } 
				addItem ("Sent data " + c);
				c.openDataOutputStream().close();
				c.close();
				addItem ("Disconnected");
			} catch (Exception e) {
				addItem ("Error in connection " + e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
}


