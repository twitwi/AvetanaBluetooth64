/*
 *  PasswordAuthentication.java
 *
 *  $Revision: 1.1 $ $Date: 2004/10/28 06:19:01 $ 
 * 
 *  (c) Copyright 2001, 2002 Motorola, Inc.  ALL RIGHTS RESERVED.
 */
package javax.obex;

/**
 * This class holds user name and password combinations.
 *
 * @version 1.0 February 11, 2002
 */
public class PasswordAuthentication {

    /**
     * Creates a new <code>PasswordAuthentication</code> with the user name
     * and password provided.
     *
     * @param userName the user name to include; this may be <code>null</code>
     *
     * @param password the password to include in the response
     *
     * @exception NullPointerException if <code>password</code> is
     * <code>null</code>
     */
    public PasswordAuthentication(byte[] userName, byte[] password) {
throw new RuntimeException("Not Implemented! Used to compile Code");
    }

    /**
     * Retrieves the user name that was specified in the constructor.
     * The user name may be <code>null</code>.
     *
     * @return the user name
     */
    public byte[] getUserName() {
throw new RuntimeException("Not Implemented! Used to compile Code");
    }

    /**
     * Retrieves the password.
     *
     * @return the password
     */
    public byte[] getPassword() {
throw new RuntimeException("Not Implemented! Used to compile Code");
    }
}
