package de.avetana.bluetooth.util;


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
 * <b>Description: </b><br>The C-functions are "stored" in a library (.so under linux, .dll under Windows), which is
 * part of the Jar archive of the AvetanaBluetooth project. This class extracts the librairy from the jar
 * archive, copies it as a temporary system file and uses this temporary system file to perform function-calls.
 *
 */

import java.io.*;

public class LibLoader {

	public static void loadCommLib (String name) throws Exception {

	    String libName = name;
	    String sysName = System.getProperty("os.name");
	    
	    if (sysName.toLowerCase().indexOf("windows") != -1 && sysName.toLowerCase().indexOf("ce") != -1) libName = libName + "CE.dll";
	    else if (sysName.toLowerCase().indexOf("windows") != -1) libName = libName + ".dll";
	    else if (sysName.toLowerCase().indexOf("linux") != -1) libName = "lib" + libName + ".so";
	    else if (sysName.toLowerCase().indexOf("mac os x") != -1) libName = "lib" + libName + ".jnilib";
	    else throw new Exception ("Unsupported operating system" + sysName);
	    
	    InputStream is = null;
	    
	    try {
	    		Class cl = new LibLoader().getClass();
	    		ClassLoader clo = cl.getClassLoader();
	    		if (clo == null) {
	    			is = ClassLoader.getSystemResourceAsStream(libName);
	    		}else is = clo.getResourceAsStream(libName);
	 
	    } catch (Exception e) {
	    		e.printStackTrace();
	    		throw new Exception ("Native Library " + libName + " is not a ressource !");
	    }

		if (is == null) throw new Exception ("Native Library " + libName + " not in CLASSPATH !");

	    File fd = null;
	    while (fd == null) {
	      try {
	      	do {
	      		int count = (int)(100000f * Math.random());
	      		fd = new File (System.getProperty("java.io.tmpdir") + File.separator + count);
	      	} while (fd.exists());
	      } catch (Exception e) { e.printStackTrace(); System.err.println ("Writing of temp lib-file failed " + fd.getAbsolutePath()); }
	    }
	    String path = fd.getAbsolutePath();
	    fd.delete();
	    final File f = new File(path);
	    f.mkdirs();
	    final File f2 = new File (f, libName);
	    FileOutputStream fos = new FileOutputStream (f2);

	    byte[] b = new byte[1000];
	    int len;
	    while ((len = is.read(b)) >= 0) {
	      fos.write(b, 0, len);
	    }
	    fos.close();
	    System.load(f2.getAbsolutePath());

	    Runnable r = new Runnable() {
	    		public void run() {
	    			f2.delete();
	    			f.delete();
	    		}
	    };
	    Runtime.getRuntime().addShutdownHook(new Thread (r));
	  }

/*  public static void loadCommLib (String name) throws Exception {

    String libName = "jbluez.dll";
    
    try {
    		Class cl = new LibLoader().getClass();
    		ClassLoader clo = cl.getClassLoader();
    			System.load(System.getProperty("user.dir") + File.separatorChar + libName);
    			return;
    } catch (Exception e) {
    		e.printStackTrace();
    		throw new Exception ("Native Library " + libName + " is not a ressource !");
    }

 
  }*/

}
