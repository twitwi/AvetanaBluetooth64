/*
 * Created on 22.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.avetana.bluetooth.test;

import java.io.*;

import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.obex.*;



/**
 * @author gmelin
 *
 * 
 */
public class OBEXSendTest implements DiscoveryListener {

	private boolean finishedSearching = false;
	private ServiceRecord rec = null;
	
	public OBEXSendTest(String bta, String file) throws Exception {
        LocalDevice myLocalDevice = LocalDevice.getLocalDevice();
        DiscoveryAgent myDiscoveryAgent = myLocalDevice.getDiscoveryAgent();
        
        File sendFile = new File (file);
        
        finishedSearching = false;
        
        myDiscoveryAgent.searchServices(null, new UUID[] { new UUID (0x1105) }, new RemoteDevice (bta), this);
        
        while (!finishedSearching) {
        		synchronized (this) {
        			wait (100);
        		}
        }
        
        if (rec == null) throw new Exception ("No OBEX_OBJECT_PUSH Service found on device " + bta);
        
        ClientSession conn = (ClientSession) Connector 
              .open(rec.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false));
        HeaderSet header = conn.createHeaderSet();
        HeaderSet response = conn.connect(header);
        System.out.println("connected");
        header = conn.createHeaderSet();
        header.setHeader(HeaderSet.NAME, sendFile.getName());
        
        String type = null;
        
        if (file.endsWith(".jpg")) type = "image/jpeg";
        else if (file.endsWith(".gif")) type = "image/gif";
        else if (file.endsWith(".jar")) type = "application/x-java-archive";
        else if (file.endsWith(".vcf")) type = "text/x-vcard";
        else if (file.endsWith(".midi") || file.endsWith(".mid")) type = "audio/x-midi";
        
        if (type != null) header.setHeader(HeaderSet.TYPE, type);
        header.setHeader(HeaderSet.LENGTH, new Long (sendFile.length()));

        Operation op = conn.put(header);

        OutputStream os = op.openOutputStream();
        InputStream is = new FileInputStream (sendFile);
        
        byte[] b = new byte[400];
        int r;
        while ((r = is.read(b)) > 0) {
        		os.write(b, 0, r);
        }
        is.close();
        os.close();
        op.close();
        
        conn.disconnect(conn.createHeaderSet());
        
        System.exit(0);
        
	}
	
	public static void main(String[] args) throws Exception {
		new OBEXSendTest (args[0], args[1]);
	}

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
		rec = servRecord[0];
	}

	/* (non-Javadoc)
	 * @see javax.bluetooth.DiscoveryListener#serviceSearchCompleted(int, int)
	 */
	public void serviceSearchCompleted(int transID, int respCode) {
		finishedSearching = true;
		
	}

	/* (non-Javadoc)
	 * @see javax.bluetooth.DiscoveryListener#inquiryCompleted(int)
	 */
	public void inquiryCompleted(int discType) {
		// TODO Auto-generated method stub
		
	}
}
