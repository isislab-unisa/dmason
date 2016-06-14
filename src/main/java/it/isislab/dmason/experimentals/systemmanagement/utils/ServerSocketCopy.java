/**
 * Copyright 2016 Universita' degli Studi di Salerno


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package it.isislab.dmason.experimentals.systemmanagement.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;

/**
 * Class to copy file with Socket tcp connection
 * 
 * @author Michele Carillo
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 *
 */
public class ServerSocketCopy implements Runnable {
	private Socket csocket=null;
	private String pathResourceToSend;

	/**
	 * Constructor
	 * 
	 * @param c Socket object
	 * @param srcpath path of resources to send
	 */
	public ServerSocketCopy(Socket c, String srcpath) {
		this.csocket=c;
		this.pathResourceToSend=srcpath;
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
			
			File myFile = new File(pathResourceToSend);
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
            System.out.println("end copy "+pathResourceToSend);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
}

