package de.avetana.bluetooth.test;

import javax.swing.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.bluetooth.*;
import javax.microedition.io.*;

public class MultiReceiveTest extends JFrame implements ActionListener {

	public class ConnectionHandler extends JPanel implements ActionListener, Runnable {

		private Connection sCon;
		private JLabel receivedLab = new JLabel ("0");
		private JButton sendBut = new JButton ("Send");
		private JButton closeBut = new JButton ("Close");
		private boolean closed = false;
		private int receiveCount = 0;
		
		public ConnectionHandler(Connection con) {
			this.sCon = con;
			add (receivedLab);
			add (sendBut);
			add (closeBut);
			sendBut.addActionListener (ConnectionHandler.this);
			closeBut.addActionListener (ConnectionHandler.this);
			new Thread (this).start();
			
		}

		public void run() {
			byte[] b = new byte[100];
			while (!closed) {
				try {
					if (protocol.equals ("btspp")) receiveCount += ((StreamConnection)sCon).openInputStream().read(b);
					else if (protocol.equals ("btl2cap")) receiveCount += ((L2CAPConnection)sCon).receive(b);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					closed = true;
					
				}
				receivedLab.setText("" + receiveCount);
			}
		}
		
		public void actionPerformed (ActionEvent e) {
			if (e.getSource() == sendBut) {
				try {
					if (protocol.equals ("btspp")) ((StreamConnection)sCon).openOutputStream ().write (new byte[100]);
					else if (protocol.equals ("btl2cap")) ((L2CAPConnection)sCon).send(new byte[100]);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} else if (e.getSource() == closeBut) {
				try {
					if (!closed) sCon.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				closed = true;
				conPan.remove(ConnectionHandler.this);
			}
		}

	}

	JToggleButton acceptBut = new JToggleButton ("AcceptAndOpen");
	JTextField statusField;
	Vector connections = new Vector();
	JPanel conPan = new JPanel(new GridLayout(0, 1));
	Connection conNot = null;
	private String protocol;
	
	public MultiReceiveTest(String protocol) {
		Container cont = getContentPane();
		cont.setLayout(new BorderLayout());
		cont.add(acceptBut, BorderLayout.NORTH);
		cont.add (conPan, BorderLayout.CENTER);
		acceptBut.addActionListener(this);
		pack();
		this.protocol = protocol;
	}
	
	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		new MultiReceiveTest(args.length == 1 ? args[0] : "btspp").setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == acceptBut && acceptBut.isSelected()) {
				Runnable r = new Runnable() {

					public void run() {
					try {
							conNot = (Connection)Connector.open(protocol + "://localhost:00112233445566778899aabbccddeeff;name=MultiTest;authenticate=false;encrypt=false");

							while (true) {
						
								if (protocol.equals("btspp")) {
									StreamConnection sCon = ((StreamConnectionNotifier)conNot).acceptAndOpen();
									conPan.add(new ConnectionHandler (sCon));
								}
								else if (protocol.equals("btl2cap")) {
									L2CAPConnection sCon = ((L2CAPConnectionNotifier) conNot).acceptAndOpen();
									conPan.add(new ConnectionHandler (sCon));
								}
							} 
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						
					}
					}
				};
				new Thread (r).start();
				
		} else if (e.getSource() == acceptBut && !acceptBut.isSelected()) {
			try {
				conNot.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
	}

}