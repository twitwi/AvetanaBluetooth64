/*
 * Created on 20.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.avetana.bluetooth.test;

import javax.microedition.io.*;
import javax.bluetooth.*;

import de.avetana.bluetooth.sdp.SDPConstants;
import java.io.*;

/**
 * @author gmelin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MultiAOTest {

	public MultiAOTest() throws Exception {
		StreamConnectionNotifier scnot = (StreamConnectionNotifier)Connector.open ("btspp://localhost:" + new UUID (SDPConstants.UUID_DIALUP_NETWORKING) + ";name=test");
		BufferedReader br = new BufferedReader (new InputStreamReader (System.in));
		while (true) {
			System.out.println ("Before acceptAndOpen");
			StreamConnection scon = scnot.acceptAndOpen();
			System.out.println ("acceptAndOpen done");
			InputStream is = scon.openInputStream();
			OutputStream os = scon.openOutputStream();
			System.out.println ("opening of streams done");
			br.readLine();
			is.close();
			os.close();
			System.out.println ("closing of streams done");
			br.readLine();
			scon.close();
			System.out.println ("closing of connection done");
			br.readLine();
		}
	}
	
	public static void main(String[] args) throws Exception {
		new MultiAOTest();
	}
}
