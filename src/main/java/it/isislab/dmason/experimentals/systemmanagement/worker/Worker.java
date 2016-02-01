/**
 * Copyright 2012 Universita' degli Studi di Salerno


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
package it.isislab.dmason.experimentals.systemmanagement.worker;

import it.isislab.dmason.experimentals.systemmanagement.master.MyFileSystem;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.experimentals.util.management.JarClassLoader;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.util.connection.Address;
import it.isislab.dmason.util.connection.jms.activemq.ConnectionNFieldsWithActiveMQAPI;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.net.ftp.FTPClient;


/**
 * 
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 *
 */
public class Worker {

	private String IP_ACTIVEMQ="";
	private String PORT_ACTIVEMQ="";
	private String TOPICPREFIX="";
	private String PORT_COPY_SERVER="";
	private static String prova="/home/miccar/Scrivania/";
	private static final String workerDirectory=prova+"worker";
	private static final String workerTemporary=workerDirectory+File.separator+"temporary";
	private static final String simulationsDirectories=workerDirectory+File.separator+"simulations";

	private ConnectionNFieldsWithActiveMQAPI conn=null;

	public Worker() {}

	/**
	 * 
	 * @param ipMaster
	 * @param portMaster
	 * @param topicPrefix
	 */
	public Worker(String ipMaster,String portMaster,String topicPrefix) {
		this.IP_ACTIVEMQ=ipMaster;
		this.PORT_ACTIVEMQ=portMaster;
		this.TOPICPREFIX=topicPrefix;
		this.conn=new ConnectionNFieldsWithActiveMQAPI();
		MyFileSystem.make(workerTemporary);
		MyFileSystem.make(simulationsDirectories);
	}




	protected void createSimulationDirectoryByID(String simID){
		String path=simulationsDirectories+File.separator+simID+File.separator+"runs";
		MyFileSystem.make(path);
	}

	protected void deleteSimulationDirectoryByID(String simID){
		String path=simulationsDirectories+File.separator+simID;
		File c=new File(path);
		MyFileSystem.delete(c);
	}


	protected void getSytemWorkerInfo(){

	}


	/**
	 * 
	 * @param path_jar_file
	 * @param params
	 * @param prefix
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */




	protected void downloadFile(String server, String port){

		//mi servono ip e port del serversocket
		System.out.println("setta");
		String serverSocketIP = "127.0.0.1";//da togliere
		int serverSocketPort = 1414;//da togliere
		String localJarFilePath = "/home/miccar/Scrivania/out.jar";//da scegliere 


		this.TOPICPREFIX=""+localJarFilePath.hashCode();


		byte[] aByte = new byte[1];
		int bytesRead;
		Socket clientSocket = null;
		InputStream is = null;

		try {
			clientSocket = new Socket( serverSocketIP , serverSocketPort );
			is = clientSocket.getInputStream();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		if (is != null) {

			FileOutputStream fos = null;
			BufferedOutputStream bos = null;
			try {
				System.out.println("Creating file...");

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

				System.out.println("Writing on file...");
				do {
					baos.write(aByte);
					bytesRead = is.read(aByte);
				} while (bytesRead != -1);

				bos.write(baos.toByteArray());
				bos.flush();
				bos.close();
				System.out.println("End writing...");
				clientSocket.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}	
	}


	protected DistributedState makeSimulation(String path_jar_file, GeneralParam params, String prefix)
	{


		try{
			JarFile jar=new JarFile(new File(path_jar_file));
			Enumeration e=jar.entries();
			File file  = new File(path_jar_file);
			URL url = file.toURL(); 
			URL[] urls = new URL[]{url};
			ClassLoader cl = new URLClassLoader(urls);
			Class distributedState=null;

			while(e.hasMoreElements()){

				JarEntry je=(JarEntry)e.nextElement();
				String classPath = je.getName();
				if(!je.getName().contains(".class")) continue;

				String[] nameclass = classPath.split("/");
				nameclass[0]=((nameclass[nameclass.length-1]).split(".class"))[0];

				byte[] classBytes = new byte[(int) je.getSize()];
				InputStream input = jar.getInputStream(je);
				BufferedInputStream readInput=new BufferedInputStream(input);

				Class c=cl.loadClass(je.getName().replaceAll("/", ".").replaceAll(".class", ""));

				if(c.getSuperclass().equals(DistributedState.class))
					distributedState=c;

			}
			if(distributedState==null) return null;
			JarClassLoader cload = new JarClassLoader(new URL("jar:file://"+path_jar_file+"!/"));

			cload.addToClassPath();
			return (DistributedState) cload.getInstance(distributedState.getName(), params,prefix);
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;


		//		Constructor constr = distributedState.getConstructor(new Class[]{ params.getClass() });
		//		return (DistributedState) constr.newInstance(new Object[]{ params });

	}




	protected boolean createConnection(){
		Address address=new Address(this.getIpActivemq(), this.getPortActivemq());
		System.out.println("connection to server "+address);
		return conn.setupConnection(address);

	}


	protected void subToInitialTopic(String initTopic){
		try {
			conn.subscribeToTopic(initTopic);
			conn.asynchronousReceive("iniz");
			for (String x : conn.getTopicList()) {
				System.out.println(x);	
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 	



	//getters and setters
	public String getIpActivemq() {return IP_ACTIVEMQ;}
	public void setIpActivemq(String iP) {IP_ACTIVEMQ = iP;}
	public String getPortActivemq() {return PORT_ACTIVEMQ;}
	public void setPortActivemq(String port) {PORT_ACTIVEMQ = port;}
	public String getTopicPrefix() {return TOPICPREFIX;}
	public void setTopicPrefix(String topicPrefix) {TOPICPREFIX = topicPrefix;}



}
