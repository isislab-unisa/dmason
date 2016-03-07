package it.isislab.dmason.experimentals.systemmanagement.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ClientSocketCopy implements Runnable {

	private Socket cSocket=null;
	private String localJarPath;
	

	public ClientSocketCopy(Socket clientSocket, String jarPath) {
		cSocket=clientSocket;
	   localJarPath=jarPath;
	}
	
	
	@Override
	public void run() {
	
		byte[] aByte = new byte[1];
		int bytesRead;
		//Socket clientSocket = null;
		InputStream is = null;
		try {
			//clientSocket = new Socket( IP_ACTIVEMQ , serverSocketPort );
			is = cSocket.getInputStream();


		} catch (IOException ex) {
			ex.printStackTrace();
		} 
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		if (is != null) {

			FileOutputStream fos = null;
			BufferedOutputStream bos = null;
			try {

				File v=new File(localJarPath);
				if(v.exists()){
					v.delete();
					v=new File(localJarPath);
				} 
				v.setWritable(true);
				v.setExecutable(true);
				fos = new FileOutputStream( v );
				bos = new BufferedOutputStream(fos);
				bytesRead = is.read(aByte, 0, aByte.length);
				do {
					baos.write(aByte);
					bytesRead = is.read(aByte);
				} while (bytesRead != -1);

				bos.write(baos.toByteArray());
				bos.flush();
				bos.close();
				cSocket.close();

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	
/*static void copyFile(String IP_ACTIVEMQ, int serverSocketPort,String localJarFilePath){
	byte[] aByte = new byte[1];
	int bytesRead;
	//Socket clientSocket = null;
	InputStream is = null;
	try {
		//clientSocket = new Socket( IP_ACTIVEMQ , serverSocketPort );
		is = clientSocket.getInputStream();


	} catch (IOException ex) {
		ex.printStackTrace();
	} 

	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	if (is != null) {

		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		try {

			File v=new File(localJarFilePath);
			if(v.exists()){
				v.delete();
				v=new File(localJarFilePath);
			} 
			v.setWritable(true);
			v.setExecutable(true);
			fos = new FileOutputStream( v );
			bos = new BufferedOutputStream(fos);
			bytesRead = is.read(aByte, 0, aByte.length);
			do {
				baos.write(aByte);
				bytesRead = is.read(aByte);
			} while (bytesRead != -1);

			bos.write(baos.toByteArray());
			bos.flush();
			bos.close();
			clientSocket.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}*/
	
	
	/*public static void main(String[] args) {


		String serverSocketIP = "127.0.0.1";
		int serverSocketPort = 1414;
		String fileOutput = "/home/miccar/Scrivania/"+System.currentTimeMillis()+ ".jar";
		byte[] aByte = new byte[1];
		int bytesRead;

		Socket clientSocket = null;
		InputStream is = null;

		try {
			while(clientSocket==null){
				System.out.println("stampo");
				clientSocket = new Socket( serverSocketIP , serverSocketPort );
				
				System.out.println(clientSocket.isBound());
			is = clientSocket.getInputStream();
			
			}
			
		} catch (IOException ex) {
			// Do exception handling
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		while(true){

		if (is != null) {

			FileOutputStream fos = null;
			BufferedOutputStream bos = null;
			try {
				System.out.println("Creating file");

				File v=new File(fileOutput);
				if(v.exists()){
					v.delete();
					v=new File(fileOutput);
				} 
				v.setWritable(true);
				v.setExecutable(true);
				fos = new FileOutputStream( v );
				bos = new BufferedOutputStream(fos);
				bytesRead = is.read(aByte, 0, aByte.length);

				System.out.println("Writing on file");
				do {
					baos.write(aByte);
					bytesRead = is.read(aByte);
				} while (bytesRead != -1);

				bos.write(baos.toByteArray());
				bos.flush();
				bos.close();
				System.out.println("End writing");
				clientSocket.close();
				return;
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}}
	}
*/

}
