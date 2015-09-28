/*
 * Copyright 2002 HM Research, Ltd. All rights reserved.
 */

package kr.or.ioi2002.RMIServer.agent;

/**
 * 
 * @author Sunglim Lee
 * @version 1.00, 11/01/03
 */

import java.net.ServerSocket;
import java.net.Socket;

import kr.or.ioi2002.RMIServer.Syslog;

public abstract class TCPListner extends Thread {
	private int iServerPort = 0;

	abstract protected void makeNewProduct(Socket socket);

	public TCPListner(int iServerPort) {
		this.iServerPort = iServerPort;

		setDaemon(true);
		start();
	}

	public void run() {
		while (true) {
			try {
				ServerSocket ss = new ServerSocket(iServerPort);
				while (true) {
					Socket socket = ss.accept();
					if (socket != null) {
					    makeNewProduct(socket);
					}
				}
			} catch (java.io.IOException e) {
				Syslog.log("TCP Listener Daemon ["
						+ Integer.toString(iServerPort)
						+ "]: IOException while listening, waiting 10sec..."
						+ e.toString());
				System.out.println("TCP Listener Daemon ["
						+ Integer.toString(iServerPort)
						+ "]: IOException while listening, waiting 10sec..."
						+ e.toString());
				try {
					sleep(10000);
				} catch (InterruptedException ex) {
					Syslog.log("TCP Listener Daemon ["
							+ Integer.toString(iServerPort)
							+ "]: InterrptedException"
							+ ex.toString());
				}

			}
		}
	}
}