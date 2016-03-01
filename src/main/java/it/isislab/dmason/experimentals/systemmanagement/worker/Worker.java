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
import it.isislab.dmason.experimentals.systemmanagement.utils.Simulation;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.experimentals.util.management.JarClassLoader;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField2D;
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
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.pig.parser.AliasMasker.load_clause_return;
import org.jets3t.service.multithread.GetObjectHeadsEvent;

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
	private int slotsNumber=0;
	private static final String MASTER_TOPIC="MASTER";
	private static String prova=System.getProperty("user.home")+File.separator+"Scrivania"+File.separator;
	private static final String workerDirectory=prova+"worker";
	private static final String workerTemporary=workerDirectory+File.separator+"temporary";
	private static final String simulationsDirectories=workerDirectory+File.separator+"simulations";
	private String TOPIC_WORKER_ID="";
	private String TOPIC_WORKER_ID_MASTER="";
	private static String WORKER_IP="127.0.0.1";
	private int DEFAULT_COPY_SERVER_PORT=0;

	private HashMap< Integer, Simulation> simulationList;

	//private LinkedList<Simulation> simulationList;



	private ConnectionNFieldsWithActiveMQAPI conn=null;


	/**
	 * 
	 * @param ipMaster
	 * @param portMaster
	 * @param topicPrefix
	 */
	public Worker(String ipMaster,String portMaster, int slots) {

		MyFileSystem.make(workerTemporary);
		MyFileSystem.make(simulationsDirectories);

		this.IP_ACTIVEMQ=ipMaster;
		this.PORT_ACTIVEMQ=portMaster;
		this.conn=new ConnectionNFieldsWithActiveMQAPI();
		WORKER_IP=getIP();
		this.createConnection();
		this.startMasterComunication();
		this.TOPIC_WORKER_ID="WORKER-"+WORKER_IP+"-"+new UID(); //my topic to master
		simulationList=new HashMap< Integer, Simulation>();
		this.slotsNumber=slots;
	}





	/**
	 * Explores NetworkInterface and finds IP Address 
	 * @return ip of Worker
	 */
	private static String getIP() {

		try {
			//String c=InetAddress.getLocalHost().getHostAddress();//doesn't work on windows            
			String c=InetAddress.getByName("localhost").getHostAddress();
			Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
			for (; n.hasMoreElements();)
			{
				NetworkInterface e = n.nextElement();
				Enumeration<InetAddress> a = e.getInetAddresses();
				for (; a.hasMoreElements();){
					InetAddress addr = a.nextElement();
					String p=addr.getHostAddress();
					if(p.contains(".") && p.compareTo(c)!=0)
						return p;
				}
			}
		} 
		catch (SocketException e1) {
			e1.printStackTrace();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		return WORKER_IP;
	} 

	//mi metto in ricezione sul master
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

					//se + diretta a me il master mi invia il topic univoco comunicazione master-me
					if(map.containsKey(worker.TOPIC_WORKER_ID)){
						TOPIC_WORKER_ID_MASTER=map.get(TOPIC_WORKER_ID).toString();
						listenerForMasterComunication();
					} 

					if(map.containsKey("port")){
						int port=(int) map.get("port");

						DEFAULT_COPY_SERVER_PORT=port;
					}



				} catch (JMSException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});



	} 	

	//mi mettto in ascolto sui messaggi che il master mi invia(1-1)
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
					final	MyHashMap map=(MyHashMap) o;

					//					if(map.containsKey("jar"))
					//					{
					//
					//						
					//						new Thread(new Runnable() {
					//
					//							@Override
					//							public void run() {
					//								int port=(int) map.get("jar");
					//								System.out.println("scarica da porta "+port);
					//								String local=System.currentTimeMillis()+"out.jar";
					//								simulation.setJarName(local);
					//								downloadFile(port, simulation.getSimulationFolder()+File.separator+local);
					//
					//								System.out.println("invio downloaded al master");
					//							}
					//						}).start(); 
					//						getConnection().publishToTopic(TOPIC_WORKER_ID,TOPIC_WORKER_ID, "downloaded");
					//					}



					if(map.containsKey("check")){
						String info=getInfoWorker();

						getConnection().publishToTopic(info, TOPIC_WORKER_ID, "info");
					}

					if(map.containsKey("newsim")){
						System.out.println("ho ricevuto la simulazione");
						Simulation sim=(Simulation)map.get("newsim");
						
						
						System.out.println("stampo sim"+sim.toString());
						System.out.println("stampo cellstype "+sim.getCellTypeList());
						
						createNewSimulationProcess(sim);

						new Thread(new Runnable() {

							@Override
							public void run() {


								String local=System.currentTimeMillis()+sim.getJarName();
								sim.setJarName(local);

								downloadFile(DEFAULT_COPY_SERVER_PORT, sim.getSimulationFolder()+File.separator+local);

								System.out.println("invio downloaded al master");
							}
						}).start(); 
						getConnection().publishToTopic(TOPIC_WORKER_ID,TOPIC_WORKER_ID, "downloaded");

					}



					if (map.containsKey("start")){

						System.out.println("Ho ricev start command per sim id "+map.get("start"));


						int id = (int)map.get("start");

						GeneralParam params = null;
						String prefix=null;
						System.out.println("worker simulazin "+getSimulationList().size());
						
						Simulation simulation=getSimulationList().get(id);
                        List<CellType> cellstype=simulation.getCellTypeList();
						
						
						int aoi=simulation.getAoi();
						int height= simulation.getHeight();
						int width= simulation.getWidth();
						int cols=simulation.getColumns();
						int rows=simulation.getRows();
						int agents=simulation.getNumAgents();
						int mode=simulation.getMode();
						int typeConn=simulation.getConnectionType();
						long step=simulation.getNumberStep();
						prefix= simulation.getTopicPrefix();
						System.out.println(width+" "+height+" "+aoi+" "+rows+" "+cols+" "+agents+" "
								+ ""+mode+"| val="+" "+ step+" "  +DistributedField2D.UNIFORM_PARTITIONING_MODE+" "+ConnectionType.pureActiveMQ);


						params=new GeneralParam(width, height, aoi, rows, cols, agents, mode,step,ConnectionType.pureActiveMQ); 	
						params.setIp(IP_ACTIVEMQ);
						params.setPort(PORT_ACTIVEMQ);
						
						
						for (CellType cellType : cellstype) {
							
							params.setI(cellType.pos_i);
							params.setJ(cellType.pos_j);
							CellExecutor celle=(new CellExecutor(params, prefix, simulation.getSimName(), simulation.getJarName()));
							executorThread.add(celle);
							celle.startSimulation();
							System.out.println("Created cell "+cellType);
							
						}
						for(CellExecutor cexe:executorThread)
						{
							cexe.start();
							System.out.println("Start cell "+cexe);
						}
						
					}


				} catch (JMSException e) {e.printStackTrace();}


			}
		});
	}
	private ArrayList<CellExecutor> executorThread=new ArrayList<CellExecutor>();
	class CellExecutor extends Thread{
		
		public GeneralParam params;
		public String prefix;
		public String sim_name;
		public String jar_name;
		public boolean run=true;
		private DistributedState dis;

		public CellExecutor(GeneralParam params, String prefix, String sim_name,String jar_name) {
			super();
			this.params = params;
			this.prefix=prefix;
			this.sim_name=sim_name;
			this.jar_name=jar_name;
			dis=makeSimulation( params, prefix,getSimulationsDirectories()+File.separator+sim_name+File.separator+jar_name);
			
		}
		public void startSimulation(){
			dis.start();
		}
		@Override
		public  void run() {
			System.out.println("Start cell for "+params.getMaxStep());
			int i=0;
			while(i!=params.getMaxStep() && run)
			{
				System.out.println("endsim with prefixID "+dis.schedule.getSteps());
				dis.schedule.step(dis);
				i++;
			}
		}
		public synchronized void stopThread()
		{
			run=false;
		}
	}
	//create folder for the sim
	private void createNewSimulationProcess(Simulation sim){
		this.createSimulationDirectoryByID(sim.getSimName());
		sim.setSimulationFolder(simulationsDirectories+File.separator+sim.getSimName());
		getSimulationList().put(sim.getSimID(),sim);
		System.out.println("sto per pubblicare al master");
		this.getConnection().publishToTopic(TOPIC_WORKER_ID_MASTER, this.TOPIC_WORKER_ID, "simrcv");


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




	protected void downloadFile(int serverSocketPort,String localJarFilePath){ 

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
		//getConnection().publishToTopic(TOPIC_WORKER_ID, "READY", "downloaded");//togli
	}


	protected DistributedState makeSimulation(GeneralParam params, String prefix,String pathJar)
	{
		String path_jar_file=pathJar;
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
			System.out.println(""+distributedState.getName());


			return (DistributedState) cload.getInstance(distributedState.getName(), params,prefix);
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;


		//		Constructor constr = distributedState.getConstructor(new Class[]{ params.getClass() });
		//		return (DistributedState) constr.newInstance(new Object[]{ params });

	}


	/**
	 * Reestituisce info al master del worker
	 */
	private String getInfoWorker() 
	{
		WorkerInfo info=new WorkerInfo();
		info.setIP(WORKER_IP);
		info.setWorkerID(this.TOPIC_WORKER_ID_MASTER);
		info.setNumSlots(this.getSlotsNumber());
		return info.toString();

	}
	//method for topic 


	protected boolean createConnection(){
		Address address=new Address(this.getIpActivemq(), this.getPortActivemq());
		System.out.println("connection to server "+address);
		return conn.setupConnection(address);

	}



	//invio richiesta di iscrizione e comunico il topic sul quale
	//invierò informazioni
	public void signRequestToMaster(){
		try{	
			conn.createTopic("READY", 1);
			conn.subscribeToTopic("READY");
			conn.publishToTopic(this.TOPIC_WORKER_ID,"READY" ,"signrequest");

			//topic per fornire info al master 
			conn.createTopic(this.TOPIC_WORKER_ID, 1);
			conn.subscribeToTopic(TOPIC_WORKER_ID);
		} catch(Exception e){e.printStackTrace();}
	}



	public void getLogBySimID(int simID){
		Simulation sim=getSimulationList().get(simID);
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
	public int getSlotsNumber(){return slotsNumber;}
	public int setSlotsNumuber(int slots){return this.slotsNumber=slots;}
	public HashMap<Integer, Simulation> getSimulationList(){return simulationList;}



}
