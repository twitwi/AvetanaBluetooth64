/*
 * Created on 27.10.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.avetana.bluetooth.obex;

import java.io.*;

import javax.microedition.io.*;
import javax.obex.*;

/**
 * @author gmelin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SessionNotifierImpl implements SessionNotifier, CommandHandler {

	private StreamConnectionNotifier locConNot;
	private ServerRequestHandler myHandler;
	private StreamConnection streamCon;
	private InputStream is;
	private OutputStream os;
	private int mtu = 0x2000;
	private Operation m_putOperation = null;
	
	public SessionNotifierImpl (StreamConnectionNotifier locConNot) {
		this.locConNot = locConNot;
		myHandler = null;
	}
	/* (non-Javadoc)
	 * @see javax.obex.SessionNotifier#acceptAndOpen(javax.obex.ServerRequestHandler)
	 */
	public Connection acceptAndOpen(ServerRequestHandler handler)
			throws IOException {
		this.myHandler = handler;
		streamCon = locConNot.acceptAndOpen();
		is = streamCon.openInputStream();
		os = streamCon.openOutputStream();
		startWaiting();
		return this;
	}
	
	public HeaderSet createHeaderSet() {
		if (myHandler != null) return myHandler.createHeaderSet();
		else return new HeaderSetImpl();
	}
	
	private void startWaiting () {
		Runnable r = new Runnable() {
			public void run() {
				try {
					while (true) {
						byte[] data = receiveCommand ();
						switch ((int)(data[0] & 0xff)) {
							case 0x80: {
								mtu = 0xffff & ((0xff & data[5]) << 8 | (0xff & data[6]));
								HeaderSet request = OBEXConnection.parseHeaders(data, 7);
								HeaderSet response = myHandler.createHeaderSet();
								int ret = myHandler.onConnect( request, response);
								byte[] rhead = OBEXConnection.hsToByteArray(response);
								byte retdata[] = new byte[7 + rhead.length];
								retdata[0] = (byte)ret;
								retdata[1] = (byte)((retdata.length >> 8) & 0xff);
								retdata[2] = (byte)((retdata.length >> 0) & 0xff);
								retdata[3] = 0x10;
								retdata[4] = 0x00;
								retdata[5] = 0x20;
								retdata[6] = 0x00;
								System.arraycopy(rhead, 0, retdata, 7, rhead.length);
								os.write(retdata);
								break;
							}
							case 0x81: {
								HeaderSet request = OBEXConnection.parseHeaders(data, 3);
								HeaderSet response = myHandler.createHeaderSet();
								myHandler.onDisconnect( request, response);
								byte[] rhead = OBEXConnection.hsToByteArray(response);
								byte retdata[] = new byte[3 + rhead.length];
								retdata[0] = (byte)0xa0;
								retdata[1] = (byte)((retdata.length >> 8) & 0xff);
								retdata[2] = (byte)((retdata.length >> 0) & 0xff);
								System.arraycopy(rhead, 0, retdata, 3, rhead.length);
								os.write(retdata);
								//System.out.println ("OBEX Disconnected");
								break;
							}
							case 0x02:
							case 0x82: {
								HeaderSet request = OBEXConnection.parseHeaders(data, 3);
								HeaderSet response = myHandler.createHeaderSet();
								if (m_putOperation == null) m_putOperation = new OperationImpl (SessionNotifierImpl.this, response);
								((OperationImpl)m_putOperation).newData (request);
								int ret = 0x90;
								if (data[0] == (byte)0x82) {
									ret = myHandler.onPut(m_putOperation);
									response = ((OperationImpl)m_putOperation).getHeadersToSend();
								}
								
								byte[] rhead = OBEXConnection.hsToByteArray(response);
								byte retdata[] = new byte[3 + rhead.length];
								retdata[0] = (byte)ret;
								retdata[1] = (byte)((retdata.length >> 8) & 0xff);
								retdata[2] = (byte)((retdata.length >> 0) & 0xff);
								System.arraycopy(rhead, 0, retdata, 3, rhead.length);
								os.write(retdata);
								os.flush();
								//System.out.println ("OBEX PUT ret " + ret + " got " + (int)(data[0] & 0xff) + " len " + retdata.length);
								break;
							}
							default:
								System.out.println ("Received unidentifier command ! " + (int)(data[0] & 0xff));

						}
						
					}
				} catch (Exception e) {
					e.printStackTrace();
					streamCon.close();
					locConNot.close();
				}
			}
		};
		new Thread (r).start();
	}

	/* (non-Javadoc)
	 * @see javax.obex.SessionNotifier#acceptAndOpen(javax.obex.ServerRequestHandler, javax.obex.Authenticator)
	 */
	public Connection acceptAndOpen(ServerRequestHandler handler,
			Authenticator auth) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.microedition.io.Connection#close()
	 */
	public void close() {
		// TODO Auto-generated method stub

	}

	public byte[] receiveCommand () throws IOException {
		byte start[] = new byte[3];
		int read = 0;
		while (read < 3) {
			//System.out.println ("Waiting for command " + read);
			read += Math.max(0, is.read(start, read, 3 - read));
		}
		//System.out.println ("Got " + read + " bytes");
		int toRead = 0xffff & (((start[1] & 0xff) << 8) | (start[2] & 0xff));
		byte[] data = new byte[toRead];
		System.arraycopy (start, 0, data, 0, 3);
		while (read < toRead) 			read += Math.max(0, is.read (data, read, toRead - read));
		/*System.out.print ("Data received ");
		for (int i = 0;i < data.length;i++)
			System.out.print (" " + Integer.toHexString(0xff & data[i] ));
		System.out.println();*/
		return data;
	}

	public void sendCommand (int commId, byte[] data) throws IOException {
		int len = 3 + data.length;
		
		os.write (new byte[] { (byte)commId, (byte)((len >> 8) & 0xff), (byte)(len & 0xff) });
		os.write (data);
		//System.out.print ("Sending command ");
		 //System.out.print (" " + Integer.toHexString(commId & 0xff));	
		 //System.out.print (" " + Integer.toHexString((byte)((len >> 8) & 0xff)));	
		 //System.out.print (" " + Integer.toHexString( (byte)(len & 0xff)));	
		//for (int i = 0;i < data.length ;i++) System.out.print (" " + Integer.toHexString(data[i] & 0xff));
		//System.out.println();
		os.flush();
	}
	
	public int getMTU() {
		return mtu;
	}


}
