package de.avetana.bluetooth.util;

import javax.bluetooth.*;
import de.avetana.bluetooth.stack.*;
import de.avetana.bluetooth.rfcomm.*;
import de.avetana.bluetooth.l2cap.*;
import de.avetana.bluetooth.sdp.*;
import de.avetana.bluetooth.connection.*;
import javax.microedition.io.*;
import java.io.*;
import javax.swing.JOptionPane;

/**
 * <b>COPYRIGHT:</b><br> (c) Copyright 2004 Avetana GmbH ALL RIGHTS RESERVED. <br><br>
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
 * A test class used for the development of AvetanaBluetooth. Do not use it.
 *
 * @author Julien Campana
 */
public class Test {

  protected final String campino="00046181138A";
  protected final String bolero="000D9305170E";
  protected final String avetana_web="000461811202";
  BufferedReader input = new BufferedReader (new InputStreamReader(System.in));

  protected String myTest="btspp://localhost:3B9FA89520078C303355AAA694238F07:1;name=Avetana Service;authenticate=true;encrypt=true";
  protected String remoteTest="btspp://"+campino+":1";
  protected String remoteSiemens="btspp://0001E304A323:1";

  L2CAPConnection streamCon;
  ConnectionNotifier notify;

  private class DL2CAPThread extends Thread {
    private boolean running;
    private int received;

    public DL2CAPThread() {
      super();
    }

    public void run() {
      running = true;
      received = 0;
      int u=0;
      try {
        while (running && u++<10) {
          byte b[] = new byte[1000];
          int a = ((L2CAPConnection)streamCon).receive(b);
          received += a;
        }
      } catch (Exception e) {e.printStackTrace(); revokeService(); }
    }

    public void stopReading() {
      running = false;
    }
   }

  public void revokeService()  {
    if (notify != null) { notify.close(); notify = null; }
  }

  public Test(String argv[]) throws Exception{
    JSR82URL url=new JSR82URL("btl2cap://localhost:0dad43655df111d69f6e00039353e765;name=L2CAPTest");
    url.setParameter("encryt", new Boolean(false));
    url.setParameter("authenticate", new Boolean(false));
    url.setParameter("master", new Boolean(false));
    notify = (ConnectionNotifier)Connector.open(url.toString());
    streamCon = ((L2CAPConnectionNotifierImpl)notify).acceptAndOpen();
    DL2CAPThread l2capThread = new DL2CAPThread();
    l2capThread.start();
/*
    String inputString = new String();
    StreamConnectionNotifier con=null;
    try {
      BluezStack stack=new BluezStack();
      BluetoothStack.init(stack);
      BluetoothStack myStack=BluetoothStack.getBluetoothStack();
      System.out.println("Press a key to connect!");
      inputString = input.readLine();
      L2CAPConnectionImpl connection=(L2CAPConnectionImpl)Connector.open("btl2cap://"+avetana_web+":10");
      RemoteDevice dev=connection.getRemoteDevice();
      System.out.println("Press a key to authenticate the remote device!");
      //connection.send(new byte[]{(byte)0xfc,0x13,0x24,0x56});
      int v=0;
      while(true && ++v<11) {

        byte b[]=new byte[1000];
        if(connection.ready()) connection.receive(b);
        else continue;

        int i=0;
        System.out.println("Java output (new version):");
        while(i++<b.length-1) System.out.print(Integer.toHexString(new Byte(b[i]).shortValue())+" ");
        System.out.println("\n");
        try {this.wait(200);}catch(Exception ex) {}
      }
      System.out.println("Press a key to close the connection!");
      inputString = input.readLine();

      connection.close();

    }catch(Exception ex) {
            ex.printStackTrace();
          if(con!=null) con.close();}
    System.exit(0);*/
  }
  public static void main(String[] args) throws Exception{
    Test test1 = new Test(args);
  }

}

