/*
 * Created on 25.10.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.avetana.bluetooth.obex;

import de.avetana.bluetooth.rfcomm.*;

import javax.bluetooth.RemoteDevice;
import javax.obex.*;
import java.io.*;

/**
 * @author gmelin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class OBEXConnection implements ClientSession, CommandHandler {

	protected static final int CONNECT = 0x80;
	protected static final int DISCONNECT = 0x81;
	protected static final int CLOSE = 0x82;
	protected static final int PUT = 0x02;
	protected static final int GET = 0x03;
	protected static final int SETPATH = 0x85;
	protected static final int SESSION = 0x87;
	protected static final int ABORT = 0xFF;
	protected int mtu = 0x2000;
	
	private RFCommConnection con;
	private long conID;
	private InputStream is;
	private OutputStream os;
	
	public OBEXConnection (RFCommConnection con)  throws IOException {
		this.con = con;
		os = con.openOutputStream();
		is = con.openInputStream();
	}
	
	/* (non-Javadoc)
	 * @see javax.microedition.io.Connection#close()
	 */
	public void close() {
		con.close();
	}
	
	public RemoteDevice getRemoteDevice() {
		return con.getRemoteDevice();
	}
	
	public javax.obex.HeaderSet createHeaderSet() {
		return new de.avetana.bluetooth.obex.HeaderSetImpl();
	}

	/* (non-Javadoc)
	 * @see javax.obex.ClientSession#setAuthenticator(javax.obex.Authenticator)
	 */
	public void setAuthenticator(Authenticator auth) {
		
	}

	/* (non-Javadoc)
	 * @see javax.obex.ClientSession#setConnectionID(long)
	 */
	public void setConnectionID(long id) {
		this.conID = id;
	}

	/* (non-Javadoc)
	 * @see javax.obex.ClientSession#getConnectionID()
	 */
	public long getConnectionID() {
		return conID;
	}

	/* (non-Javadoc)
	 * @see javax.obex.ClientSession#connect(javax.obex.HeaderSet)
	 */
	public javax.obex.HeaderSet connect(javax.obex.HeaderSet headers) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bos.write(0x10);
		bos.write (0);
		bos.write (new byte[] { 0x20, 0x00 });
		bos.write (hsToByteArray (headers));
		sendCommand (CONNECT, bos.toByteArray());
		byte[] b = receiveCommand();
		if (b[0] != (byte)0xa0) throw new IOException ("Connection not accepted");
		mtu = 0xffff & ((0xff & b[5]) << 8 | (0xff & b[6]));
		return parseHeaders (b, 7);
	}

	/* (non-Javadoc)
	 * @see javax.obex.ClientSession#disconnect(javax.obex.HeaderSet)
	 */
	public javax.obex.HeaderSet disconnect(javax.obex.HeaderSet headers) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bos.write (hsToByteArray (headers));
		sendCommand (DISCONNECT, bos.toByteArray());
		byte[] b = receiveCommand();
		if (b[0] != (byte)0xa0) throw new IOException ("Disconnection error");
		return parseHeaders (b, 3);

	}

	/* (non-Javadoc)
	 * @see javax.obex.ClientSession#setPath(javax.obex.HeaderSet, boolean, boolean)
	 */
	public javax.obex.HeaderSet setPath(javax.obex.HeaderSet headers, boolean backup, boolean create) throws IOException {
			byte b1[] = OBEXConnection.hsToByteArray(headers);
			byte b2[] = new byte[b1.length + 2];
			b2[0] = b2[1] = 0;
			if (backup) b2[0] |= 1;
			if (!create) b2[0] |= 2;
			System.arraycopy(b1, 0, b2, 2, b1.length);
			sendCommand (OBEXConnection.SETPATH, b2);
			byte[] resp = receiveCommand();
			HeaderSet hs = OBEXConnection.parseHeaders(resp, 3);
			return hs;
	}

	/* (non-Javadoc)
	 * @see javax.obex.ClientSession#delete(javax.obex.HeaderSet)
	 */
	public javax.obex.HeaderSet delete(javax.obex.HeaderSet headers) throws IOException {
		byte b1[] = OBEXConnection.hsToByteArray(headers);
		sendCommand (OBEXConnection.PUT, b1);
		byte[] resp = receiveCommand();
		HeaderSet hs = OBEXConnection.parseHeaders(resp, 3);
		return hs;
	}

	/* (non-Javadoc)
	 * @see javax.obex.ClientSession#get(javax.obex.HeaderSet)
	 */
	public Operation get(javax.obex.HeaderSet headers) throws IOException {
		return new OperationImpl (this, headers, OBEXConnection.GET);
	}

	/* (non-Javadoc)
	 * @see javax.obex.ClientSession#put(javax.obex.HeaderSet)
	 */
	public Operation put(javax.obex.HeaderSet headers) throws IOException {
		return new OperationImpl (this, headers, OBEXConnection.PUT);
	}
		
	public void sendCommand (int commId, byte[] data) throws IOException {
		int len = 3 + data.length;
		byte d2[] = new byte[len];
		d2[0] = (byte)commId;
		d2[1] =  (byte)((len >> 8) & 0xff);
		d2[2] = (byte)(len & 0xff);
		System.arraycopy (data, 0, d2, 3, data.length);
		os.write (d2);
		/*System.out.print ("Sending command ");
		 System.out.print (" " + Integer.toHexString(commId & 0xff));	
		 System.out.print (" " + Integer.toHexString((byte)((len >> 8) & 0xff)));	
		 System.out.print (" " + Integer.toHexString( (byte)(len & 0xff)));	
		for (int i = 0;i < data.length ;i++) System.out.print (" " + Integer.toHexString(data[i] & 0xff));
		System.out.println();*/
		os.flush();
	}
	
	public byte[] receiveCommand () throws IOException {
		byte start[] = new byte[3];
		int read = 0;
		while (read < 3) read += Math.max(0, is.read(start, read, 3 - read));
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
	
	static private void writeLen (OutputStream os, long v)  {
		byte[] b = new byte[4];
		b[0] = (byte) ((v >> 24) & 0xff);
		b[1] = (byte) ((v >> 16) & 0xff);
		b[2] = (byte) ((v >> 8) & 0xff);
		b[3] = (byte) (v & 0xff);
		try { os.write (b); } catch (Exception e) { e.printStackTrace();  }
	}
	
	static private long parseLong (byte data[], int offset) {
		long v = 0;
		for (int i = 0;i < 4;i++) {
			v = v << 8;
			v |= (int)(data[offset++] & 0xff);
		}
		return v;
	}
	
	static protected byte[] hsToByteArray (javax.obex.HeaderSet hs) {
		if (hs == null) return new byte[0];
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int[] hids = hs.getHeaderList();
		for (int i = 0;i < hids.length;i++) {
			Object header = hs.getHeader(hids[i]);
			try {
				bos.write (hids[i]);
				switch (hids[i]) {
				case HeaderSetImpl.COUNT:
				case HeaderSetImpl.LENGTH:	
					writeLen (bos, ((Long)header).longValue());
					break;
				case HeaderSetImpl.NAME:
				case HeaderSetImpl.DESCRIPTION:
					String nameObj = (String)header;
					writeShortLen (bos, 3 + 2 * (nameObj.length() + 1));
					bos.write (nameObj.getBytes ("UTF-16BE"));
					bos.write (new byte[] { 0, 0});
					break;
				case HeaderSetImpl.TYPE:
					String typeObj = (String)header;
					writeShortLen (bos, 3 + (typeObj.length() + 1));
					bos.write (typeObj.getBytes ("iso-8859-1"));
					bos.write (new byte[] { 0 });
					break;
				default:
					byte http[] = (byte[])header;
					writeShortLen (bos, 3 + http.length);
					bos.write (http);
					break;
				}
			} catch (IOException e) { e.printStackTrace(); }
		}
		return bos.toByteArray();
	}

	static protected HeaderSetImpl parseHeaders (byte[] data, int offset) {
		HeaderSetImpl hs = (HeaderSetImpl)new de.avetana.bluetooth.obex.HeaderSetImpl();

		while (offset < data.length) {
			int id = (int)data[offset] & 0xff;
			int len = (int)(0xffff & (((0xff & data[offset + 1]) << 8) | (data[offset + 2] & 0xff)));
			switch (id) {
			case HeaderSetImpl.COUNT:
			case HeaderSetImpl.LENGTH:	
				hs.setHeader(id, new Long(parseLong (data, offset + 1)));
				len = 5;
				break;
			case HeaderSetImpl.NAME:
			case HeaderSetImpl.DESCRIPTION:
				try {
					String nameObj = new String (data, offset + 3, len - 2 - 3, "UTF-16BE");
					hs.setHeader(id, nameObj);
					break;
				} catch (Exception e) { System.err.println(len + " " + offset + " " + data.length); e.printStackTrace(); }
				break;
			case HeaderSetImpl.TYPE:
				try {
					String typeObj = new String (data, offset + 3, len - 1 - 3, "iso-8859-1");
					hs.setHeader(id, typeObj);
					break;
				} catch (Exception e) { e.printStackTrace(); }
				break;
			default:
				if (len <= 3) break;
				byte http[] =new byte[len - 3];
				System.arraycopy (data, offset + 3, http, 0, len - 3);
				hs.setHeader (id, http);
				break;
			}
			offset += len;
		} 
		return hs;
	}
	
	/**
	 * @param bos
	 * @param length
	 */
	static private void writeShortLen(ByteArrayOutputStream bos, int v) {
		byte[] b = new byte[2];
		b[0] = (byte) ((v >> 8) & 0xff);
		b[1] = (byte) (v & 0xff);
		try { bos.write (b); } catch (Exception e) { e.printStackTrace();  }		
	}
	
	public int getMTU() {
		return mtu;
	}

}
