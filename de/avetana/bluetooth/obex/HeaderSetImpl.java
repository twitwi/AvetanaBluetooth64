/*
 * Created on 25.10.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.avetana.bluetooth.obex;

import java.io.IOException;
import java.util.*;

/**
 * @author gmelin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class HeaderSetImpl implements javax.obex.HeaderSet {

	private Hashtable headers;
	
	public HeaderSetImpl() {
		headers = new Hashtable();
	}
	
	/* (non-Javadoc)
	 * @see javax.obex.HeaderSet#setHeader(int, java.lang.Object)
	 */
	public void setHeader(int headerID, Object headerValue) {
		headers.put( new Integer (headerID), headerValue);
	}

	/* (non-Javadoc)
	 * @see javax.obex.HeaderSet#getHeader(int)
	 */
	public Object getHeader(int headerID) {
		return headers.get( new Integer (headerID));
	}

	/* (non-Javadoc)
	 * @see javax.obex.HeaderSet#getHeaderList()
	 */
	public int[] getHeaderList() {
		Integer vi[] = new Integer[headers.size()];
		int v[] = new int[vi.length];
		headers.keySet().toArray(vi);
		boolean has48 = headers.containsKey(new Integer (0x48));
		int j = 0;
		for (int i = 0;i < v.length;i++) if (vi[i].intValue() != 0x48) v[j++] = vi[i].intValue();
		if (has48) v[j++] = 0x48;
		return v;
	}

	/* (non-Javadoc)
	 * @see javax.obex.HeaderSet#createAuthenticationChallenge(java.lang.String, boolean, boolean)
	 */
	public void createAuthenticationChallenge(String realm, boolean userID,
			boolean access) {

	}

	/* (non-Javadoc)
	 * @see javax.obex.HeaderSet#getResponseCode()
	 */
	public int getResponseCode() throws IOException {
		return 0;
	}

}
