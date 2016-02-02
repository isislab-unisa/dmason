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


public class CopyMultiThreadServer implements Runnable {
	Socket csocket=null;

	public CopyMultiThreadServer(Socket c) {
		csocket=c;
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
			File myFile = new File("/home/miccar/Scrivania/flockers.jar");
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

				System.out.println("File sended");   

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	
	
	public static void main(String[] args) throws IOException {
		int port =1414;
		InetAddress address=InetAddress.getByName("127.0.0.1");

		ServerSocket welcomeSocket = new ServerSocket(port,1,address);    



		System.out.println("Listening");
		while (true) {
			Socket sock = welcomeSocket.accept();
			System.out.println("Connected");
			new Thread(new CopyMultiThreadServer(sock)).start();
		}
	}



	/*public static void main(String args[]) {




(new Thread() {

			@Override
			public void run() {
		        int port=1414;		
				ServerSocket welcomeSocket = null;
				Socket connectionSocket = null;
				BufferedOutputStream outToClient = null;
				InetAddress address=null;
				while (true) {
					try {
						address=InetAddress.getByName("127.0.0.1");
						System.out.println(port);

						welcomeSocket = new ServerSocket(port,1000,address);
					    port++;

						connectionSocket = welcomeSocket.accept();
						System.out.println("listening for a connection...");
						outToClient = new BufferedOutputStream(connectionSocket.getOutputStream());
					} catch (IOException ex) {
						ex.printStackTrace();
					}

					if (outToClient != null) {
						File myFile = new File("/home/miccar/Scrivania/flockers.jar");
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
							connectionSocket.close();

							System.out.println("File sended");   

						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
				}


			}
		}).start();
	 */
	/*String fileToSend = "/home/miccar/Scrivania/flockers.jar";
		ServerSocket welcomeSocket = null;
		Socket connectionSocket = null;
		BufferedOutputStream outToClient = null;
		InetAddress address=null;


		while (true) {




			try {
				address=InetAddress.getByName("127.0.0.1");
				welcomeSocket = new ServerSocket(3248,1000,address);    
				connectionSocket = welcomeSocket.accept();
				System.out.println("accettat");
				outToClient = new BufferedOutputStream(connectionSocket.getOutputStream());
			} catch (IOException ex) {
				// Do exception handling
			}
			if (outToClient != null) {
				File myFile = new File(fileToSend);
				System.out.println(myFile);
				myFile.setReadable(true);
				byte[] mybytearray = new byte[(int) myFile.length()];

				FileInputStream fis = null;

				try {
					fis = new FileInputStream(myFile);
				} catch (FileNotFoundException ex) {
					// Do exception handling
				}
				BufferedInputStream bis = new BufferedInputStream(fis);

				try {
					bis.read(mybytearray, 0, mybytearray.length);
					outToClient.write(mybytearray, 0, mybytearray.length);
					outToClient.flush();
					outToClient.close();
					connectionSocket.close();

					// File sent, exit the main method
					return;
				} catch (IOException ex) {
					// Do exception handling
				}
			}
		}*/

	
}

