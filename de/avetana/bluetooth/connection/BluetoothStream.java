package de.avetana.bluetooth.connection;

import java.io.*;
import de.avetana.bluetooth.stack.BlueZ;
import javax.microedition.io.*;

/**
 * The top-class for the management of stream-oriented connections.
 *
 * <br><br><b>COPYRIGHT:</b><br> (c) Copyright 2004 Avetana GmbH ALL RIGHTS RESERVED. <br><br>
 *
 * This file is part of the Avetana bluetooth API for Linux.<br><br>
 *
 * The Avetana bluetooth API for Linux is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version. <br><br>
 *
 * The Avetana bluetooth API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.<br><br>
 *
 * The development of the Avetana bluetooth API is based on the work of
 * Christian Lorenz (see the Javabluetooth Stack at http://www.javabluetooth.org) for some classes,
 * on the work of the jbluez team (see http://jbluez.sourceforge.net/) and
 * on the work of the bluez team (see the BlueZ linux Stack at http://www.bluez.org) for the C code.
 * Classes, part of classes, C functions or part of C functions programmed by these teams and/or persons
 * are explicitly mentioned.<br><br><br><br>
 *
 *
 * <b>Description:</b><br>
 * This class allows the management of data streams between a remote and local device.
 * It provides an InputStream and an OutputStream for easily receiving and sending data from/to a remote device.
 * The InputStream stores all retrieved data into its buffer: no data is therefore lost. <br>
 *
 * @author Julien Campana
 */
public class BluetoothStream extends BTConnection implements StreamConnection {

  /**
   * The buffer used to store received data
   */
  protected byte[] buffer;

  /**
   * The own defined and implemented input stream.
   */
  protected MInputStream inStream = null;

  /**
   * The own defined and implemented output stream.
   */
  protected MOutputStream outStream;

  /**
   * If <code>true</code>, the connection was opened and the reading thread was started.
   */
  protected boolean isReading = false;

  /**
   * Constructs a new instance of BluetoothStream. Initializes the Input and OutputStream.
   * @param fid An integer, which uniquely identifies the connection.
   */
  protected BluetoothStream(int fid) {
    super(fid);
    outStream = new MOutputStream();
  }

  /**
   * Constructs a new instance of BluetoothStream. Initializes the InputStream, OutputStream.
   * @param fid An integer, which uniquely identifies the connection.
   * @param addr The address of the remote BT device
   */
  protected BluetoothStream(int fid, String addr) {
    super(fid, addr);
    outStream = new MOutputStream();
  }

  /**
   * Starts to read the data received from the remote device
   */
  protected void startReading() {
    inStream = new MInputStream();
    Runnable r = new Runnable() {
      public synchronized void run() {
        byte[] b = new byte[1000];
        isReading=true;
        while (!closed) {
          try {
            int data = BlueZ.readBytes(fid, b, b.length);
            if (data > 0) inStream.addData(b, data);
            else if (data == -1) inStream.close();
            else this.wait(50);
          } catch (Exception e) { e.printStackTrace(); closed = true; isReading = false; }
        }
      }
    };
    new Thread (r).start();
  }

  /**
   * Adds new data to the inputstream.<br>
   * This method allows the user to directly access the nested MInputStream class. This can be useful
   * if someone wants to offer a C-function which directly writes in the nested InputStream. But until then, this
   * method is not used by the AvetanaBluetooth implementation.
   * @param b A byte array representing the data.
   */
  public void newData (byte[] b) {
    inStream.addData(b, b.length);
  }

  /**
   * If the nested input stream was not opened before, opens it and starts reading. Returns the DataInputStream based
   * on this opened nested input stream.
   * @return The DataInputStream based on this opened nested input stream.
   * @throws java.io.IOException
   */
  public DataInputStream openDataInputStream() throws java.io.IOException {
    if(inStream == null) this.startReading();
    return new DataInputStream(inStream);
  }

  /**
   * Opens and returns the nested output stream.
   * @return The nested ouput stream
   * @throws java.io.IOException
   */
  public OutputStream openOutputStream() throws java.io.IOException {
    return outStream;
  }

  /**
   * Opens the nested output stream and returns the DataOutputStream based on it.
   * @return The nested OuputStream.
   * @throws java.io.IOException
   */
  public DataOutputStream openDataOutputStream() throws java.io.IOException {
    return new DataOutputStream(outStream);
  }

  /**
   * Returns the inputstream used by this connection
   * @return The inputstream used by this connection
   */
  public InputStream openInputStream() throws IOException  {
    if(inStream == null) this.startReading();
    return inStream;
  }

  /**
   * An own extension of the classical java InputStream class.
   * @author Moritz Gmelin
   */
  protected class MInputStream extends InputStream {

    byte[] buffer = new byte[100];
    private int readPos = 0, writePos = 0;

    public synchronized int available () {
      return Math.max (0, writePos - readPos);
    }

    public synchronized void addData(byte[] b, int len) {
      while (writePos + len > buffer.length) {
        byte[] b2 = new byte[buffer.length * 2];
        System.arraycopy(buffer, readPos, b2, 0, writePos - readPos);
        buffer = b2;
        writePos -= readPos;
        readPos = 0;
      }
      System.arraycopy(b, 0, buffer, writePos, len);
      writePos += len;
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
      waitForData();
      return (int)(buffer[readPos++] & 0xff);
    }

    public synchronized int read (byte[] b, int off, int len) throws IOException {
      waitForData();
      int av = available();
      int r = av > b.length - off ? b.length - off : av;
      r = r > len ? len : r;
      System.arraycopy(buffer, readPos, b, off, r);
      readPos += r;
      return r;
    }

    public synchronized void close() {
      closed = true;
    }

  }

  /**
   * An own extension of the classical java OutputStream class.
   * @author Moritz Gmelin
   */
  protected class MOutputStream extends OutputStream {
    public void write (int data) throws IOException {
      BlueZ.writeBytes (fid, new byte[] { (byte)data }, 0, 1);
    }

    public void write (byte[] b) throws IOException {
      BlueZ.writeBytes (fid, b, 0, b.length);
    }

    public void write (byte[] b, int off, int len) throws IOException {
      BlueZ.writeBytes (fid, b, off, len);
    }

  }

}
