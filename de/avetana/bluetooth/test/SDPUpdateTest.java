/*
 * Created on 01.11.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.avetana.bluetooth.test;

import javax.bluetooth.*;
import javax.microedition.io.*;
import java.io.*;

/**
 * @author gmelin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SDPUpdateTest {

	private StreamConnectionNotifier streamConNot;
	private boolean accepting = false;
	
	public SDPUpdateTest() throws Exception {
		System.out.println ("Registering Service");
		streamConNot = null;
		Runnable r = new Runnable() {
			public void run() {
				try {
					streamConNot = (StreamConnectionNotifier)Connector.open("btspp://localhost:" + new UUID (0x1101) + ";name=Testname");
					accepting = true;
					System.out.println ("acceptAndOpen...");
					streamConNot.acceptAndOpen();

				} catch (Exception e) { e.printStackTrace(); }
				accepting = false;

			}
		};
		new Thread(r).start();
		while (!accepting) { synchronized (this) { wait (1000); } }
		ServiceRecord srec = LocalDevice.getLocalDevice().getRecord(streamConNot);
		System.out.println ("Press a key to update service name... "  + srec);
		BufferedReader br = new BufferedReader (new InputStreamReader (System.in));
		br.readLine();
		srec.setAttributeValue(0x100, new DataElement (DataElement.STRING, "UpdatedName"));
		DataElement testEl = new DataElement (DataElement.DATSEQ);
		testEl.addElement(new DataElement (DataElement.U_INT_2, 0x1234));
		testEl.addElement(new DataElement (DataElement.UUID, new UUID (0x1234)));
		//DataElement testEl = new DataElement (DataElement.U_INT_2, 0x1234);
		//testEl.addElement();
		srec.setAttributeValue(0x102, testEl);		
		LocalDevice.getLocalDevice().updateRecord(srec);
		System.out.println ("Press a key to remove service");
		br.readLine();
		streamConNot.close();
		while (accepting) { synchronized (this) { wait (1000); } }
		System.exit(0);

	}
	
	public static void main(String[] args) throws Exception {
		new SDPUpdateTest();
	}
}
