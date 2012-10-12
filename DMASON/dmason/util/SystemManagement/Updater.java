/**
 * Copyright 2012 Università degli Studi di Salerno
 

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

package dmason.util.SystemManagement;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.swing.Timer;

import org.apache.commons.io.FilenameUtils;

import dmason.util.connection.Address;
import dmason.util.exception.NoDigestFoundException;


/**
 * This code was edited or generated using CloudGarden's Jigloo
 * SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation,
 * company or business for any purpose whatever) then you
 * should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details.
 * Use of Jigloo implies acceptance of these licensing terms.
 * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
 * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
 * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */

/** This class is used for deploy simulation/update JAR to the worker
 * 
 * @author Mario Fiore Vitale
 *
 */
public class Updater
{

	private static final String DOWNLOADED_JAR_PATH = "TEMP";
	private static final String UPDATE_DIR = "update";
	private static final String SIMULATION_DIR = "simulation";
	private static String SEPARATOR;

	private static String FTPIP;
	private static String jarName;
	private static String FTPPORT;


	public static void updateWithGUI(Address FTPaddress, String name, String myTopic, Address address) 
	{
		FTPIP = FTPaddress.getIPaddress();
		FTPPORT = FTPaddress.getPort();
		jarName = name;

		setSeparator();

		downloadJar(jarName,"upd");

		//DOWNLOADED_JAR_PATH+SEPARATOR+
		File f = new File(jarName);

		try {
			ArrayList<String> command = new ArrayList<String>();

			command.add("java");
			command.add("-jar");
			command.add(f.getAbsolutePath());
			command.add(myTopic);
			command.add(address.getIPaddress());
			command.add(address.getPort());


			ProcessBuilder builder = new ProcessBuilder(command);	
			Process process = builder.start();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Timer timer = new Timer(4000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);

			}
		});

		timer.start();

	}

	public static void updateNoGUI(Address ftpAddress, String name, String myTopic,
			Address address) {

		FTPIP = ftpAddress.getIPaddress();
		FTPPORT = ftpAddress.getPort();
		jarName = name;

		setSeparator();

		downloadJar(jarName,"upd");

		//DOWNLOADED_JAR_PATH+SEPARATOR+
		File f = new File(jarName);

		try {
			ArrayList<String> command = new ArrayList<String>();

			command.add("java");
			command.add("-jar");
			command.add(f.getAbsolutePath());
			command.add(address.getIPaddress());
			command.add(address.getPort());
			command.add(myTopic);


			ProcessBuilder builder = new ProcessBuilder(command);	
			Process process = builder.start();	

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Timer timer = new Timer(4000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);

			}
		});

		timer.start();
	}

	public static URL getSimulationJar(StartUpData data)
	{
		setSeparator();

		FTPIP = data.getFTPAddress().getIPaddress();
		FTPPORT = data.getFTPAddress().getPort();

		downloadJar(data.getJarName(),"sim");

		File f = new File(DOWNLOADED_JAR_PATH+SEPARATOR+data.getJarName());
		
		//System.out.println("file:"+File.separator+f.getAbsolutePath());
		URL url = null;
		try {
			OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();

			if(os.getName().contains("Linux") || os.getName().contains("OSX"))
				url = new URL("file:"+File.separator+File.separator+f.getAbsolutePath());
			else
				url = new URL("file:"+File.separator+f.getAbsolutePath());

		} catch (MalformedURLException e) {
			System.out.println("Invalid URL: " );
		}
		return url;

	}

	private static void setSeparator() {
		OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();

		if(os.getName().contains("Windows"))
			SEPARATOR = "\\";
		if(os.getName().contains("Linux") || os.getName().contains("OSX"))
			SEPARATOR = "/";
	}

	private static void downloadJar(String jarName,String jarType) 
	{

		FTPClient client = connect();

		login(client);

		File downDir = new File(DOWNLOADED_JAR_PATH);
		// if the directory does not exist, create it
		if (!downDir.exists())
		{

			boolean result = downDir.mkdir();  
			if(result){    
				System.out.println("DIR created");  
			}

		}

		try {

			if(jarType.equals("upd"))
				client.changeDirectory(UPDATE_DIR);
			if(jarType.equals("sim"))
			{
				client.changeDirectory(SIMULATION_DIR);

				File simFile = new File(DOWNLOADED_JAR_PATH+SEPARATOR+jarName);
				
				String digestFile = FilenameUtils.removeExtension(jarName)+".hash";
			
				client.download(digestFile, new java.io.File(DOWNLOADED_JAR_PATH+SEPARATOR+digestFile));
				
				if(simFile.exists()) //if the worker has the jar
				{
					
					Digester dg = new Digester(DigestAlgorithm.MD5);
						
					String DownloadedDigest = dg.loadFromPropFile(DOWNLOADED_JAR_PATH+SEPARATOR+digestFile);
					
					dg = new Digester(DigestAlgorithm.MD5);

					try {
						InputStream in = new FileInputStream(DOWNLOADED_JAR_PATH+SEPARATOR+jarName);

						String digest = dg.getDigest(in);
						
						if(DownloadedDigest.equals(digest))
							return;

					} catch (IOException ex) {
						ex.printStackTrace();
					}
					
				}
			}
			
			if(jarType.equals("upd"))
				client.download(jarName, new java.io.File(jarName));
			if(jarType.equals("sim"))
				client.download(jarName, new java.io.File(DOWNLOADED_JAR_PATH+SEPARATOR+jarName));
			
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FTPIllegalReplyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FTPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FTPDataTransferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FTPAbortedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void login(FTPClient client) 
	{
		try {
			client.login("anonymous", "");
		} catch (IllegalStateException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		} catch (FTPIllegalReplyException e) {

			e.printStackTrace();
		} catch (FTPException e) {

			e.printStackTrace();
		}
	}
	private static FTPClient connect() 
	{
		FTPClient client = new FTPClient();

		try {
			client.connect(FTPIP,Integer.parseInt(FTPPORT));
		} catch (IllegalStateException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		} catch (FTPIllegalReplyException e) {

			e.printStackTrace();
		} catch (FTPException e) {

			e.printStackTrace();
		}
		return client;
	}
	
	
	// Used for update log file
	public static void uploadLog(UpdateData up) 
	{
		String hostname = "";
		Date date;
		String jvmName = "";
		String balanceLog = "Balance";
		String workerLog = "Worker";

		FTPIP = up.getFTPAddress().getIPaddress();
		FTPPORT = up.getFTPAddress().getPort();

		FTPClient client = connect();

		login(client);

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
		date = new Date();
		dateFormat.format(date);

		try {
			InetAddress addr = InetAddress.getLocalHost();

			// Get IP Address
			//byte[] ipAddr = addr.getAddress();

			// Get hostname
			hostname = addr.getHostName();
		} catch (UnknownHostException e) {
		}

		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();

		//
		// Get name representing the running Java virtual machine.
		// It returns something like 6460@AURORA. Where the value
		// before the @ symbol is the PID.
		//
		jvmName = bean.getName();

		boolean isDirExists = false;

		try {
			client.createDirectory(dateFormat.format(date)+"@"+hostname);
		} catch (IllegalStateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FTPIllegalReplyException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FTPException e1) {
			isDirExists = true;
		}

		try {

			client.changeDirectory(dateFormat.format(date)+"@"+hostname);

			File blog = new File(balanceLog+jvmName+".log");
			if(blog.exists())
				client.upload(blog);
			else
				System.out.println("File not found");

			File wlog = new File(workerLog+jvmName+".log");
			if(wlog.exists())
				client.upload(wlog);
			else
				System.out.println("File not found");
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FTPIllegalReplyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FTPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FTPDataTransferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FTPAbortedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}



}
