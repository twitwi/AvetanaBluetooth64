/*
 * Created on 25.10.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.avetana.bluetooth.obex;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.obex.*;

/**
 * @author gmelin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class OperationImpl implements Operation {

	private CommandHandler con;
	private HeaderSet hs;
	private HeaderSet recHeaders;
	private OBEXOutputStream oos;
	private OBEXInputStream ois;
	private int respCode = 0;
	private long len = -1;
	private String type = null;
	private boolean closed = false;
	
	protected OperationImpl (CommandHandler con, HeaderSet hs) {
		this.con = con;
		this.hs = hs;
		oos = new OBEXOutputStream();
		ois = new OBEXInputStream();
	}
	/* (non-Javadoc)
	 * @see javax.obex.Operation#abort()
	 */
	public void abort() throws IOException {
		try {
			con.sendCommand(OBEXConnection.ABORT, new byte[] { 0x49, 0x00, 0x03 });
			con.receiveCommand();
		} catch (IOException e) {
			e.printStackTrace() ;
		}
	}

	/* (non-Javadoc)
	 * @see javax.obex.Operation#getReceivedHeaders()
	 */
	public HeaderSet getReceivedHeaders() throws IOException {
		if (recHeaders == null) recHeaders = con.createHeaderSet();
		return recHeaders;
	}

	/* (non-Javadoc)
	 * @see javax.obex.Operation#sendHeaders(javax.obex.HeaderSet)
	 */
	public void sendHeaders(HeaderSet headers) throws IOException {
		new Throwable().printStackTrace();
		this.hs = headers;
	}

	protected HeaderSet getHeadersToSend() {
		if (hs == null) hs = con.createHeaderSet();
		return hs;
	}
	/* (non-Javadoc)
	 * @see javax.obex.Operation#getResponseCode()
	 */
	public int getResponseCode() throws IOException {
		return respCode;
	}

	/* (non-Javadoc)
	 * @see javax.microedition.io.ContentConnection#getEncoding()
	 */
	public String getEncoding() {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.microedition.io.ContentConnection#getLength()
	 */
	public long getLength() {
		return len;
	}

	/* (non-Javadoc)
	 * @see javax.microedition.io.ContentConnection#getType()
	 */
	public String getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see javax.microedition.io.InputConnection#openDataInputStream()
	 */
	public DataInputStream openDataInputStream() throws IOException {
		return new DataInputStream (ois);
	}

	/* (non-Javadoc)
	 * @see javax.microedition.io.InputConnection#openInputStream()
	 */
	public InputStream openInputStream() throws IOException {
		return ois;
	}

	/* (non-Javadoc)
	 * @see javax.microedition.io.OutputConnection#openDataOutputStream()
	 */
	public DataOutputStream openDataOutputStream() throws IOException {
		return new DataOutputStream (oos);
	}

	/* (non-Javadoc)
	 * @see javax.microedition.io.OutputConnection#openOutputStream()
	 */
	public OutputStream openOutputStream() throws IOException {
		return oos;
	}

	/* (non-Javadoc)
	 * @see javax.microedition.io.Connection#close()
	 */
	public void close() {
		try {
			this.con.sendCommand(OBEXConnection.CLOSE, new byte[] { 0x49, 0x00, 0x03 });
			this.con.receiveCommand();
		} catch (IOException e) {
			e.printStackTrace() ;
		}
	}
	
	class OBEXOutputStream extends OutputStream {

		/* (non-Javadoc)
		 * @see java.io.OutputStream#write(int)
		 */
		public void write(int b) throws IOException {
			write (new byte[] { (byte)(b & 0xff) });
		}
		
		public synchronized void write (byte b[], int off, int len) throws IOException {
			
			byte[] hsba = hs != null ? OBEXConnection.hsToByteArray(hs) : new byte[0];
			
			while (len + 6 + hsba.length > con.getMTU()) {
				write (b, off, con.getMTU() - 6 - hsba.length);
				off += con.getMTU();
				len -= con.getMTU();
			}
			
			byte[] d;
			
			if (OperationImpl.this.hs == null) hs = con.createHeaderSet();
			d = new byte[len];
			System.arraycopy(b, off, d, 0, len);
			hs.setHeader(0x48, d);
			con.sendCommand (OBEXConnection.PUT, OBEXConnection.hsToByteArray(hs));
			hs = null;
			d = con.receiveCommand();
			if (d[0] != (byte)0x90) throw new IOException ("Error while sending PUT command");
			respCode = (int)d[0] & 0xff;
		}
		
	}
	
	protected void newData (HeaderSet header) {
		if (recHeaders == null) recHeaders = con.createHeaderSet();
		int[] hids = header.getHeaderList();	
		for (int i = 0;i < hids.length;i++) {
			if (hids[i] == 0x48 || hids[i] == 0x49) { ois.addData((byte[])header.getHeader(hids[i])); continue; }
			else if (hids[i] == HeaderSet.LENGTH) len = ((Long)header.getHeader(HeaderSet.LENGTH)).longValue();
			else if (hids[i] == HeaderSet.TYPE) type = (String)header.getHeader (HeaderSet.TYPE);
			recHeaders.setHeader (hids[i], header.getHeader(hids[i]));
			//System.out.println ("New data with header " + Integer.toHexString(hids[i]));
		}
	}

	 /**
	   * An own extension of the classical java InputStream class.
	   * @author Moritz Gmelin
	   */
	  protected class OBEXInputStream extends InputStream {

	    byte[] buffer = new byte[100];
	    int readPos = 0, writePos = 0;

	    public synchronized int available () {
	      return closed ? 0 : writePos - readPos;
	    }

	    public synchronized void addData(byte[] b) {
	      while (writePos + b.length > buffer.length) {
	        byte[] b2 = new byte[buffer.length * 2];
	        System.arraycopy(buffer, readPos, b2, 0, writePos - readPos);
	        buffer = b2;
	        writePos -= readPos;
	        readPos = 0;
	      }
	      System.arraycopy(b, 0, buffer, writePos, b.length);
	      writePos += b.length;
	      this.notify();
	    }

	    private synchronized void waitForData() throws IOException {
	      while (writePos <= readPos) {
	        if (closed == true) throw new IOException("Connection closed");
	        try {
	          this.wait(50);
	        }
	        catch (Exception e) {
	        }
	      }
	    }

	    public synchronized int read() throws IOException {
	      if (closed == true) throw new IOException("Connection closed");;
	      waitForData();
	      return (int)buffer[readPos++];
	    }

	    public synchronized int read (byte[] b, int off, int len) throws IOException {
	      waitForData();
	      if (closed == true) throw new IOException("Connection closed");;
	      int av = available();
	      int r = av > b.length - off ? b.length - off : av;
	      r = r > len ? len : r;
	      System.arraycopy(buffer, readPos, b, off, r);
	      readPos += r;
	      return r;
	    }

	    public void close() {
	      closed = true;
	    }

	    public void reset() {
	      readPos = writePos = 0;
	    }

	  }

}
