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
	private Operation m_getOperation = null;
	private Authenticator auth;
	private boolean authorized = false;
	
	public SessionNotifierImpl (StreamConnectionNotifier locConNot) {
		this.locConNot = locConNot;
		myHandler = null;
	}
	
	/* (non-Javadoc)
	 * 	 * @see javax.obex.SessionNotifier#acceptAndOpen(javax.obex.ServerRequestHandler)
	 */
	public synchronized  Connection acceptAndOpen(ServerRequestHandler handler)
			throws IOException {
		return this.acceptAndOpen(handler,null);
	}
	
	public synchronized  Connection acceptAndOpen(ServerRequestHandler handler, Authenticator auth)
		throws IOException {
		this.auth = auth;
		this.myHandler = handler;
		if (streamCon != null) throw new IOException ("Connection already connected");
		if (locConNot == null) throw new IOException ("ConnectionNotifier is null ! maybe it was closed previousely..");
		streamCon = locConNot.acceptAndOpen();
		startWaiting();
		return this;
	}
	
	public StreamConnectionNotifier getConnectionNotifier() {
		return (StreamConnectionNotifier)locConNot;
	}
	
	public HeaderSet createHeaderSet() {
		if (myHandler != null) return myHandler.createHeaderSet();
		else return new HeaderSetImpl();
	}
	
	private void startWaiting () {
		Runnable r = new Runnable() {
			public void run() {
					try {
					is = streamCon.openInputStream();
					os = streamCon.openOutputStream();
					while (streamCon != null) {
						byte[] data = null;
						try {
							data = receiveCommand ();
						} catch (Exception e) {
							closeStreamCon();
							continue;
						}
						//System.out.println ("Received command ! " + Integer.toHexString((int)(data[0] & 0xff)));

						switch ((int)(data[0] & 0xff)) {
							case 0x80: {
								mtu = 0xffff & ((0xff & data[5]) << 8 | (0xff & data[6]));
								HeaderSet request = OBEXConnection.parseHeaders(data, 7);
								HeaderSet response = myHandler.createHeaderSet();
								boolean success = handleAuthResponse(request);
								handleAuthChallenge(request, response);
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
								os.flush();
								break;
							}
							case 0x81: {
								HeaderSet request = OBEXConnection.parseHeaders(data, 3);
								HeaderSet response = myHandler.createHeaderSet();
								synchronized (SessionNotifierImpl.this) {
									myHandler.onDisconnect( request, response);
									byte[] rhead = OBEXConnection.hsToByteArray(response);
									byte retdata[] = new byte[3 + rhead.length];
									retdata[0] = (byte)0xa0;
									retdata[1] = (byte)((retdata.length >> 8) & 0xff);
									retdata[2] = (byte)((retdata.length >> 0) & 0xff);
									System.arraycopy(rhead, 0, retdata, 3, rhead.length);
									os.write(retdata);
									os.flush();
									closeStreamCon();
								}
//								is.close(); is = null;
//								os.close(); os = null;
//								streamCon.close(); streamCon = null;
//								System.out.println ("OBEX Disconnected");
								break;
							}
							case 0x02:
							case 0x82: {
								HeaderSet request = OBEXConnection.parseHeaders(data, 3);
								HeaderSet response = myHandler.createHeaderSet();
								handleAuthResponse(request);
								handleAuthChallenge(request, response);
								if (m_putOperation == null) m_putOperation = new OperationImpl (SessionNotifierImpl.this, request, OBEXConnection.PUT);
								((OperationImpl)m_putOperation).newData (request);
								int ret = 0x90;
								if (data[0] == (byte)0x82) {
									ret = myHandler.onPut(m_putOperation);
									response = ((OperationImpl)m_putOperation).getHeadersToSend();
								}
								response.setHeader(0x49, null);
								sendCommand (ret, OBEXConnection.hsToByteArray(response));
								//System.out.println ("OBEX PUT ret " + ret + " got " + (int)(data[0] & 0xff) + " len " + retdata.length);
								break;
							}
							case 0x83:	{
								HeaderSet request = OBEXConnection.parseHeaders(data, 3);
								HeaderSet response = null;
								
								int ret = 0xa0;
								if (m_getOperation == null) {
									m_getOperation = new OperationImpl (SessionNotifierImpl.this, request, OBEXConnection.GET);
									response = ((OperationImpl)m_getOperation).getHeadersToSend();
									handleAuthResponse(request);
									boolean success = handleAuthResponse(request);
									handleAuthChallenge(request, response);
									ret = myHandler.onGet(m_getOperation);
								} else 	response = ((OperationImpl)m_getOperation).getHeadersToSend();

								
								
								if (ret == 0x90 || ret == 0xa0) {
									InputStream is = m_getOperation.openInputStream();
									int respLen = OBEXConnection.hsToByteArray(response).length;
									byte d2[] = new byte[Math.min(is.available(), mtu - respLen - 3)];
									is.read(d2);
									if (is.available() == 0) {
										response.setHeader(0x49, d2);
										ret = 0xa0;
									} else {
										response.setHeader(0x48, d2);
										ret = 0x90;
									}
									sendCommand (ret, OBEXConnection.hsToByteArray(response));
									if (ret == 0xa0) m_getOperation = null;
								}
								break;
							}
							case 0x85: {
								HeaderSet request = OBEXConnection.parseHeaders(data, 5);
								HeaderSet response = myHandler.createHeaderSet();
								boolean success = handleAuthResponse(request);
								handleAuthChallenge(request, response);
								int ret = myHandler.onSetPath( request, response, (data[3] & 1) == 1, (data[3] & 2) == 2);
								byte[] rhead = OBEXConnection.hsToByteArray(response);
								byte retdata[] = new byte[3 + rhead.length];
								retdata[0] = (byte)ret;
								retdata[1] = (byte)((retdata.length >> 8) & 0xff);
								retdata[2] = (byte)((retdata.length >> 0) & 0xff);
								System.arraycopy(rhead, 0, retdata, 3, rhead.length);
								os.write(retdata);
								os.flush();
							}
							default:
								System.out.println ("Received unidentifier command ! " + (int)(data[0] & 0xff));

						}
						
					}
				} catch (Exception e) {
					e.printStackTrace();
					closeStreamCon();
				}
				
			}
		};
		new Thread (r).start();
	}
	
	/**
	 * @param request
	 * @param response
	 */
	protected void handleAuthChallenge(HeaderSet request, HeaderSet response) {
		byte authChallenge[] = (byte[])request.getHeader(HeaderSetImpl.AUTH_CHALLENGE);
		if (authChallenge != null && auth != null) {
			byte[] resp = HeaderSetImpl.createAuthResponse(authChallenge, auth);
			response.setHeader(HeaderSetImpl.AUTH_RESPONSE, resp);
		}
	}

	/**
	 * @param request
	 * @return
	 */
	protected boolean handleAuthResponse(HeaderSet request) {
		byte authResp[] = (byte[])request.getHeader(HeaderSetImpl.AUTH_RESPONSE);
		
		if (auth == null) return true;
		else if (authResp == null) return false;

		byte[] nonce = null;
		
		int offset = 18;
		
		byte[] digest = new byte[16];
		System.arraycopy(authResp, 2, digest, 0, 16);
		
		byte[] user = null;
		if (authResp[18] == (byte)0x01) {
			user = new byte[(byte)authResp[19]];
			System.arraycopy (authResp, 20, user, 0, user.length);
			offset += 2 + user.length;
		}
		byte[] passwd = auth.onAuthenticationResponse(user);
		
		if (authResp.length > offset && authResp[offset] == (byte)0x02) {
			nonce = new byte[16];
			System.arraycopy(authResp, offset + 2, nonce, 0, 16);
		}
		
		String check = "", digestS ="";
		try {
			check = new String (nonce, 0, 16, "iso-8859-1") + ":" + new String (passwd, 0, passwd.length, "iso-8859-1");
			MD5 md5 = new MD5();
			md5.update(check.toCharArray(), check.length());
			md5.md5final();
			byte checkB[] = md5.toByteArray();
			digestS = new String (digest, "iso-8859-1");
			check = new String (checkB, "iso-8859-1");
			if (!check.equals(digestS)) this.myHandler.onAuthenticationFailure(user);
			else authorized = true;
		} catch (UnsupportedEncodingException e) {e.printStackTrace();}
		return authorized;
	}

	/**
	 * closes only the currently open connection if there is one. Does not close the Connection notifier
	 * itself. So after closeStreamCon() is called, one can call acceptAndOpen() to offer a new service. 
	 *
	 */
	
	private void closeStreamCon() {
		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {}
			is = null;
		}
		if (os != null) {
			try {
				os.close();
			} catch (IOException e) {}
			os = null;
		}
		if (streamCon != null) {
			try {
				streamCon.close();
			} catch (Exception e) {}
			streamCon = null;
		}

	}

	/**
	 * First calls streamConClose and then closes the ConnectionNotifier.
	 * acceptAndOpen will fail after close has been called.
	 * 
	 */
	public void close() {
		closeStreamCon();
		if (locConNot != null) try { locConNot.close(); } catch (IOException e) {}
		locConNot = null;
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

	/*public RemoteDevice getRemoteDevice() {
		return locConNot.getRemoteDevice();
	}*/

	public Authenticator getAuthenticator() {
		return auth;
	}
}
