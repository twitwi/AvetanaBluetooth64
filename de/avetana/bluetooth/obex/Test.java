/*
 * Created on 27.10.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.avetana.bluetooth.obex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;
import javax.obex.ServerRequestHandler;
import javax.obex.SessionNotifier;

/**
 * @author gmelin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Test {
	   public static void main (String args[]) throws Exception  {
		if (args.length == 4 && args[0].equals("send")) {
			send (new String[] { args[1], args[2], args[3] });
		} else if (args.length == 1 && args[0].equals("receive")) receive();
		else System.out.println ("Usage :\nsend <addr> <port> <filename>\nreceive");
}
		
public static void send(String args[]) throws Exception {
		ClientSession obcon = (ClientSession)Connector.open ("btgoep://" + args[0] + ":" + args[1] + ";authenticate=false;master=false;encrypt=false");
		HeaderSetImpl hs = (HeaderSetImpl) obcon.createHeaderSet();
		File f = new File (args[2]);
		hs.setHeader(HeaderSetImpl.NAME, args[2]);
		hs.setHeader(HeaderSetImpl.LENGTH, new Long(f.length()));
		obcon.connect(null);
		OperationImpl op = (OperationImpl) obcon.put(hs);
		OutputStream os = op.openOutputStream();
		FileInputStream fis = new FileInputStream (f);
		byte[] b = new byte[1000];
		int count;
		while ((count = fis.read (b)) >= 0) {
			os.write (b, 0, count);
		}
		System.out.println ("Finished writing");
		os.close();
		op.close();
		obcon.disconnect(null);
		obcon.close();
		
}

	static boolean finished = false;
public static void receive () throws Exception {
		final SessionNotifier sn = (SessionNotifier)Connector.open("btgoep://localhost:11223344556677881122334455667788;name=AvetanaObex;authenticate=false;master=false;encrypt=false");
		sn.acceptAndOpen(new ServerRequestHandler() {
			
			public int onConnect (HeaderSet request, HeaderSet response) {
				System.out.println ("RequestHandler got connect");
				return ResponseCodes.OBEX_HTTP_OK;
			}
			
			public int onPut (Operation op) {
				try {
					java.io.InputStream is = op.openInputStream();
				System.out.println ("Got data " + is + " dataAvailabla " + is.available());
				HeaderSet hs = op.getReceivedHeaders();
				System.out.println ("Name " + hs.getHeader(HeaderSet.NAME));
				System.out.println ("Length " + hs.getHeader(HeaderSet.LENGTH) + " " + op.getLength());
				System.out.println ("Type " + hs.getHeader(HeaderSet.TYPE) + " " + op.getType());
				File f = File.createTempFile("obex", ".tmp");
				FileOutputStream fos = new FileOutputStream (f);
				byte b[] = new byte[1000];
				int len;
				while (is.available() > 0 && (len = is.read(b)) > 0) {
					fos.write (b, 0, len);
				}
				fos.close();
				System.out.println ("Wrote data to " + f.getAbsolutePath());
				} catch (Exception e) { e.printStackTrace(); }
				return 0xa0;
			}
			
			public void onDisconnect (HeaderSet req, HeaderSet resp) {
				System.out.println ("Disconnecting");
				finished = true;
			}
		});
		
		while (!finished) {
			;
		}
		sn.close();
		System.exit(0);
		
}

}
