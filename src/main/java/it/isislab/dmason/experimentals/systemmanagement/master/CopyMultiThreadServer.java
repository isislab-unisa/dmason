package it.isislab.dmason.experimentals.systemmanagement.master;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Class that simulates a copy server with tcp
 * 
 * @author Michele Carillo
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 *
 */
public class CopyMultiThreadServer implements Runnable {
	private Socket csocket=null;
	private String jarPathToSend;

	public CopyMultiThreadServer(Socket c, String jarPath) {
		this.csocket=c;
		this.jarPathToSend=jarPath;
	}


	@Override
	public void run() {

		BufferedOutputStream outToClient = null;
		try {

			outToClient = new BufferedOutputStream(csocket.getOutputStream());
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		if (outToClient != null) {
			
			File myFile = new File(jarPathToSend);
			myFile.setReadable(true);
			byte[] mybytearray = new byte[(int) myFile.length()];

			FileInputStream fis = null;

			try {
				fis = new FileInputStream(myFile);
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			}
			BufferedInputStream bis = new BufferedInputStream(fis);

			try {
				bis.read(mybytearray, 0, mybytearray.length);
				outToClient.write(mybytearray, 0, mybytearray.length);
				outToClient.flush();
				outToClient.close();
				csocket.close();

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
}

