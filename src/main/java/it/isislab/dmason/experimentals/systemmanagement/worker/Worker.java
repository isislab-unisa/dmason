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
package it.isislab.dmason.experimentals.systemmanagement.worker;

import it.isislab.dmason.experimentals.systemmanagement.utils.MyFileSystem;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.experimentals.util.management.JarClassLoader;
import it.isislab.dmason.experimentals.util.management.worker.PeerStatusInfo;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.MessageListener;
import it.isislab.dmason.util.connection.Address;
import it.isislab.dmason.util.connection.ConnectionType;
import it.isislab.dmason.util.connection.MyHashMap;
import it.isislab.dmason.util.connection.jms.activemq.ConnectionNFieldsWithActiveMQAPI;
import it.isislab.dmason.util.connection.jms.activemq.MyMessageListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.rmi.server.UID;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.hadoop.net.NetUtils;

import sim.engine.SimState;


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
	private static final String MASTER_TOPIC="MASTER";
	private static String prova="/home/miccar/Scrivania/";
	private static final String workerDirectory=prova+"worker";
	private static final String workerTemporary=workerDirectory+File.separator+"temporary";
	private static final String simulationsDirectories=workerDirectory+File.separator+"simulations";
	private String TOPIC_WORKER_ID="";
	private String TOPIC_WORKER_ID_MASTER="";



	private ConnectionNFieldsWithActiveMQAPI conn=null;

	/**
	 * Localhost connection for activemq tcp://127.0.0.1:61616
	 * non dovrebbe servire
	 */
	/*public Worker() {

		MyFileSystem.make(workerTemporary);
		MyFileSystem.make(simulationsDirectories);
		this.setIpActivemq(IP_ACTIVEMQ);
		this.setPortActivemq(PORT_ACTIVEMQ);
		this.conn=new ConnectionNFieldsWithActiveMQAPI();
		this.createConnection();
		this.subToInitialTopic(MASTER_TOPIC);	
		try {
		this.TOPIC_WORKER_ID=InetAddress.getLocalHost().getHostAddress()+"-"+new UID();} catch (UnknownHostException e) {e.printStackTrace();}
	}*/

	/**
	 * 
	 * @param ipMaster
	 * @param portMaster
	 * @param topicPrefix
	 */
	public Worker(String ipMaster,String portMaster) {
		MyFileSystem.make(workerTemporary);
		MyFileSystem.make(simulationsDirectories);
		this.IP_ACTIVEMQ=ipMaster;
		this.PORT_ACTIVEMQ=portMaster;
		this.conn=new ConnectionNFieldsWithActiveMQAPI();
		this.createConnection();
		this.startMasterComunication();
		try {
			String workerID="WORKER-"+InetAddress.getLocalHost().getHostAddress()+"-"+new UID();
			this.TOPIC_WORKER_ID=workerID;} catch (UnknownHostException e) {e.printStackTrace();}

	}



	protected void startMasterComunication() {
		final Worker worker=this;

		try {conn.subscribeToTopic(MASTER_TOPIC);} 
		catch (Exception e1) {e1.printStackTrace();}

		conn.asynchronousReceive(MASTER_TOPIC, new MyMessageListener() {

			@Override
			public void onMessage(Message msg) {

				Object o;
				try {
					o=parseMessage(msg);
					MyHashMap map=(MyHashMap) o;


					if(map.containsKey(worker.TOPIC_WORKER_ID)){
						TOPIC_WORKER_ID_MASTER=map.get(TOPIC_WORKER_ID).toString();
						listenerForMasterComunication();


					} 



					if (map.containsKey("esegui")){
						System.out.println("eseguo la sim");
						GeneralParam params=new GeneralParam(200, 200, 5,
								1, 2, 100,DistributedField2D.UNIFORM_PARTITIONING_MODE, ConnectionType.pureActiveMQ);
						DistributedState dis=worker.makeSimulation( params, "");
						//dis.start();
						dis.schedule.step(dis.getState());
						int i=0;
						while(i!=dis.columns)
						{


							System.out.println("endsim with prefixID"+dis.schedule.getSteps());


							dis.schedule.step(dis);
							i++;
						}






					}


				} catch (JMSException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});



	} 	


	private void listenerForMasterComunication(){
		System.out.println("Ricevuto ack iscrzione");
		//mi sottoscrivo e mi metto in ricezione sul tale topic per comuncazioni del master
		try{
			getConnection().subscribeToTopic(TOPIC_WORKER_ID_MASTER);
			//mi metto in attesa sul canale 
		}catch(Exception e){ e.printStackTrace();}


		getConnection().asynchronousReceive(TOPIC_WORKER_ID_MASTER, new MyMessageListener() {

			@Override
			public void onMessage(Message msg) {

				Object o;
				try {
					o=parseMessage(msg);
					MyHashMap map=(MyHashMap) o;

					if(map.containsKey("check")){
					WorkerInfo info=new WorkerInfo();
					   System.out.println("scrivo  su"+TOPIC_WORKER_ID);
                      getConnection().publishToTopic(info.toString(), TOPIC_WORKER_ID, "info");
                      System.out.println("invisto");
					}

					/*if(map.containsKey("jar"))
                    {
                    	Address add=(Address)map.get("jar");
                      	System.out.println("scarica da porta "+add.getPort());
                        downloadFile(Integer.parseInt(add.getPort()));
                       // worker.getConnection().publishToTopic(worker.TOPIC_WORKER_ID, "READY", "downloaded");
                    }*/





				} catch (JMSException e) {e.printStackTrace();}


			}
		});
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




	protected void downloadFile(int serverSocketPort){




		String localJarFilePath =this.getSimulationsDirectories()+File.separator+"1"+File.separator+"out.jar"; ;//simulationsDirectories+File.separator+TOPIC_WORKER_ID+"out.jar";//da scegliere 

		byte[] aByte = new byte[1];
		int bytesRead;
		Socket clientSocket = null;
		InputStream is = null;

		try {
			clientSocket = new Socket( this.IP_ACTIVEMQ , serverSocketPort );
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
		getConnection().publishToTopic(TOPIC_WORKER_ID, "READY", "downloaded");
	}


	protected DistributedState makeSimulation(GeneralParam params, String prefix)
	{

		this.TOPICPREFIX="";
		String path_jar_file=simulationsDirectories+File.separator+"1"+File.separator+"out.jar";
		System.out.println("execute"+path_jar_file);

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
			System.out.println("ds"+distributedState.getName());


			return (DistributedState) cload.getInstance(distributedState.getName(), params,prefix);
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;


		//		Constructor constr = distributedState.getConstructor(new Class[]{ params.getClass() });
		//		return (DistributedState) constr.newInstance(new Object[]{ params });

	}


	/**
	 * Reestituisce info al master
	 */
	public void info() 
	{
		WorkerResourceInfo info=new WorkerResourceInfo();
		info.getAvailableHeapGb();
		info.getBusyHeapGb();
		info.getCPULoad();

	}
	//method for topic 


	protected boolean createConnection(){
		Address address=new Address(this.getIpActivemq(), this.getPortActivemq());
		System.out.println("connection to server "+address);
		return conn.setupConnection(address);

	}




	protected void sendIdentifyTopic(){
		try{	
			conn.createTopic("READY", 1);
			conn.subscribeToTopic("READY");
			conn.publishToTopic(this.TOPIC_WORKER_ID,"READY" ,"signrequest");

			//topic per fornire info al master 
			conn.createTopic(this.TOPIC_WORKER_ID, 1);
			conn.subscribeToTopic(TOPIC_WORKER_ID);
		} catch(Exception e){e.printStackTrace();}
	}








	//getters and setters
	public String getIpActivemq() {return IP_ACTIVEMQ;}
	public void setIpActivemq(String iP) {IP_ACTIVEMQ = iP;}
	public String getPortActivemq() {return PORT_ACTIVEMQ;}
	public void setPortActivemq(String port) {PORT_ACTIVEMQ = port;}
	public String getTopicPrefix() {return TOPICPREFIX;}
	public void setTopicPrefix(String topicPrefix) {TOPICPREFIX = topicPrefix;}
	public ConnectionNFieldsWithActiveMQAPI getConnection() {return conn;}
	public String getSimulationsDirectories() {return simulationsDirectories;}



}
