package de.avetana.bluetooth.util;


import java.io.*;
import java.util.*;

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
public class LibLoader {

  public static void loadCommLib (String name) throws Exception {

    String libName = System.mapLibraryName(name);

    InputStream is = new LibLoader().getClass().getClassLoader().getResourceAsStream(libName);

    File fd = File.createTempFile("lib", "");
    String path = fd.getAbsolutePath();
    fd.delete();
    File f = new File(path);
    f.deleteOnExit();
    f.mkdirs();
    f = new File (f, libName);
    f.deleteOnExit();
    FileOutputStream fos = new FileOutputStream (f);

    byte[] b = new byte[1000];
    int len;
    while ((len = is.read(b)) >= 0) {
      fos.write(b, 0, len);
    }
    fos.close();
    System.load(f.getAbsolutePath());

  }

}
