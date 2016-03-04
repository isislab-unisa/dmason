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

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.experimentals.systemmanagement.utils.DMasonFileSystem;
import it.isislab.dmason.experimentals.systemmanagement.utils.FindAvailablePort;
import it.isislab.dmason.experimentals.systemmanagement.utils.Simulation;
import it.isislab.dmason.experimentals.systemmanagement.utils.ZipDirectory;
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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.rmi.server.UID;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.jms.JMSException;
import javax.jms.Message;

import com.sun.swing.internal.plaf.synth.resources.synth;

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
	private static String dmasonDirectory=System.getProperty("user.dir")+File.separator+"dmason";
	private static  String workerDirectory;
	private static  String workerTemporary;
	private static  String simulationsDirectories;
	private String TOPIC_WORKER_ID="";
	private String TOPIC_WORKER_ID_MASTER="";
	private static String WORKER_IP="127.0.0.1";
	private int DEFAULT_COPY_SERVER_PORT=0;
	private HashMap< Integer, Simulation> simulationList;
	private SimpleDateFormat sdf=null;
	private ConnectionNFieldsWithActiveMQAPI conn=null;


	/**
	 * WORKER 
	 * @param ipMaster
	 * @param portMaster
	 * @param topicPrefix
	 */
	@SuppressWarnings("serial")
	public Worker(String ipMaster,String portMaster, int slots) {
		try {
			this.IP_ACTIVEMQ=ipMaster;
			this.PORT_ACTIVEMQ=portMaster;
			this.conn=new ConnectionNFieldsWithActiveMQAPI();
			WORKER_IP=getIP();
			this.createConnection();
			this.startMasterComunication();
			this.TOPIC_WORKER_ID="WORKER-"+WORKER_IP+"-"+new UID(); //my topic to master
			generateFolders(TOPIC_WORKER_ID); //generate folders for worker
			simulationList=new HashMap< /*idsim*/Integer, Simulation>();
			this.slotsNumber=slots;
			signRequestToMaster();


			getConnection().createTopic("SIMULATION_READY", 1);
			getConnection().subscribeToTopic("SIMULATION_READY");
			getConnection().asynchronousReceive("SIMULATION_READY", new MyMessageListener() {

				@Override
				public void onMessage(Message msg) {
					Object o;
					try {
						o=parseMessage(msg);
						MyHashMap map=(MyHashMap) o;
						if(map.containsKey("cellready")){
							int sim_id=(int) map.get("cellready");

							simulationList.get(sim_id).setReceived_cell_type(simulationList.get(sim_id).getReceived_cell_type()+1);
							if(simulationList.get(sim_id).getReceived_cell_type()==simulationList.get(sim_id).getNumCells())
							{
								runSimulation(sim_id);
							}
						} 


					} catch (JMSException e) {e.printStackTrace();}

				}
			});
		} catch (Exception e) {e.printStackTrace();}

		System.out.println("Worker started ...");
	}



	void playSimulationProcessByID(int id){
		try {
		if(simulationList.containsKey(id) && simulationList.get(id).getStatus().equals(Simulation.PAUSED))
		{  
			simulationList.get(id).setStatus(Simulation.STARTED);
			for(CellExecutor cexe:executorThread.get(id))
			{
				cexe.restartThread();
			}

			return;
		}

		GeneralParam params = null;
		String prefix=null;
		Simulation simulation=getSimulationList().get(id);

		List<CellType> cellstype=simulation.getCellTypeList();
		int aoi=simulation.getAoi();
		int height= simulation.getHeight();
		int width= simulation.getWidth();
		int cols=simulation.getColumns();
		int rows=simulation.getRows();
		int agents=simulation.getNumAgents();
		int mode=simulation.getMode();
		int p=simulation.getP();
		@SuppressWarnings("unused")
		int typeConn=simulation.getConnectionType();

		System.err.println("TODO MANAGE CONNECTION MPI");
		long step=simulation.getNumberStep();
		prefix= simulation.getTopicPrefix();
		
		params=(simulation.getMode()==DistributedField2D.UNIFORM_PARTITIONING_MODE)?
				new GeneralParam(width, height, aoi, rows, cols, agents, mode,step,ConnectionType.pureActiveMQ):
			new GeneralParam(width, height, aoi,p, agents, mode,step,ConnectionType.pureActiveMQ);
		params.setIp(IP_ACTIVEMQ);
		params.setPort(PORT_ACTIVEMQ);
		simulationList.put(simulation.getSimID(), simulation);
		executorThread.put(simulation.getSimID(),new ArrayList<CellExecutor>());
		for (CellType cellType : cellstype) {
			slotsNumber--;
			params.setI(cellType.pos_i);
			params.setJ(cellType.pos_j);
			FileOutputStream output = new FileOutputStream(simulation.getSimulationFolder()+File.separator+"out"+File.separator+cellType+".out");
			PrintStream printOut = new PrintStream(output);

			CellExecutor celle=(new CellExecutor(params, prefix,simulation.getSimName()+""+simulation.getSimID(), simulation.getJarName(),printOut,simulation.getSimID(),
					(cellstype.indexOf(cellType)==0?true:false)));

			executorThread.get(simulation.getSimID()).add(celle);
			celle.startSimulation();
			getConnection().publishToTopic(simulation.getSimID(),"SIMULATION_READY", "cellready");

		}

		getConnection().createTopic("SIMULATION_"+simulation.getSimID(), 1);
		getConnection().publishToTopic(simulation,"SIMULATION_"+simulation.getSimID(), "workerstatus");
		}catch(IOException e){
			e.printStackTrace();
		}
	}



	private synchronized void listenerForMasterComunication(){
		try{
			getConnection().subscribeToTopic(TOPIC_WORKER_ID_MASTER);
		}catch(Exception e){ e.printStackTrace();}


		getConnection().asynchronousReceive(TOPIC_WORKER_ID_MASTER, new MyMessageListener() {

			@Override
			public void onMessage(Message msg) {

				Object o;
				try {
					o=parseMessage(msg);
					final MyHashMap map=(MyHashMap) o;

					if(map.containsKey("check")){
						String info=getInfoWorker().toString();
						getConnection().publishToTopic(info, TOPIC_WORKER_ID,"info");
					}
					if(map.containsKey("newsim")){
						Simulation sim=(Simulation)map.get("newsim");
						createNewSimulationProcess(sim);

						new Thread(new Runnable() {

							@Override
							public void run() {

								String local=System.currentTimeMillis()+sim.getJarName();
								sim.setJarName(local);
								downloadFile(DEFAULT_COPY_SERVER_PORT, sim.getSimulationFolder()+File.separator+local);
							}
						}).start(); 

					}
					if (map.containsKey("start")){

						int id = (int)map.get("start");

						playSimulationProcessByID(id);


					}
					if (map.containsKey("stop")){

						int id = (int)map.get("stop");
						stopSimulation(id);
					}
					if (map.containsKey("pause")){

						int id = (int)map.get("pause");
						System.out.println("Command pause received for simulation "+id);
						pauseSimulation(id);
					}

					if(map.containsKey("logs")){
						int id=(int)map.get("logs");
						System.out.println("Received request for logs for simid "+id);
						getLogBySimID(id);
						
					}

				} catch (JMSException e) {e.printStackTrace();} 


			}
		});
	}


	private HashMap<Integer,ArrayList<CellExecutor>> executorThread=new HashMap<Integer,ArrayList<CellExecutor>>();

	private synchronized void runSimulation(int sim_id){
		Simulation s=simulationList.get(sim_id);
		s.setStartTime(System.currentTimeMillis());
		s.setStep(0);
		for(CellExecutor cexe:executorThread.get(sim_id))
		{
			cexe.start();
		}
		s.setStatus(Simulation.STARTED);
		s.setStartTime(System.currentTimeMillis());
		s.setStep(0);
		getConnection().publishToTopic(s,"SIMULATION_"+s.getSimID(), "workerstatus");
		System.out.println("Simulation "+sim_id+" started, with "+executorThread.get(sim_id).size()+".");


	}
	/**
	 * 
	 * @param sim_id
	 */
	private synchronized void stopSimulation(int sim_id)
	{
		Simulation s=simulationList.get(sim_id);
		s.setEndTime(System.currentTimeMillis());
		for(CellExecutor cexe:executorThread.get(sim_id))
		{
			cexe.stopThread();
		}
		//simulationList.remove(sim_id);
		s.setStatus(Simulation.FINISHED);
		s.setEndTime(System.currentTimeMillis());
		getConnection().publishToTopic(s,"SIMULATION_"+s.getSimID(), "workerstatus");
		System.out.println("Simulation "+sim_id+" stopped, with "+executorThread.get(sim_id).size());

	}
	private synchronized void pauseSimulation(int sim_id)
	{
		Simulation s=simulationList.get(sim_id);
		for(CellExecutor cexe:executorThread.get(sim_id))
		{
			cexe.pauseThread();
		}
		s.setStatus(Simulation.PAUSED);
		s.setEndTime(System.currentTimeMillis());
		getConnection().publishToTopic(s,"SIMULATION_"+s.getSimID(), "workerstatus");
		System.out.println("Simulation "+sim_id+" paused, with "+executorThread.get(sim_id).size());



	}
	class CellExecutor extends Thread{

		public GeneralParam params;
		public String prefix;
		public String folder_sim;
		public String jar_name;
		private int sim_id;
		public boolean run=true;
		public boolean pause=false;
		public boolean masterCell=false;
		@SuppressWarnings("rawtypes")
		private DistributedState dis;

		final Lock lock = new ReentrantLock();
		final Condition isPause  = lock.newCondition(); 

		public CellExecutor(GeneralParam params, String prefix, String folder_name,String jar_name, PrintStream out,int sim_id,boolean master_cell) {
			super();
			this.params = params;
			this.prefix=prefix;
			this.folder_sim=folder_name;
			this.jar_name=jar_name;
			dis=makeSimulation( params, prefix, getSimulationsDirectories()+File.separator+folder_sim+File.separator+jar_name);
			dis.setOutputStream(out);
			this.sim_id=sim_id;
			this.masterCell=master_cell;
		}

		public void startSimulation(){
			dis.start();

		}
		@Override
		public  void run() {
			System.out.println("Start cell for "+params.getMaxStep());
			int i=0;
			Simulation s=simulationList.get(sim_id);

			while(i!=params.getMaxStep() && run)
			{   
				if(i%500==0){System.out.println("STEP NUMBER "+dis.schedule.getSteps());}
				try{
					lock.lock();

					while(pause)
					{
						isPause.await();
					}

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					lock.unlock();
				}
				if(masterCell)
				{
					s.setStep(i);
					getConnection().publishToTopic(s,"SIMULATION_"+s.getSimID(), "workerstatus");
				}
				dis.schedule.step(dis);
				i++;
			}
			this.stopThread();
		}
		public synchronized void stopThread()
		{
			run=false;
			slotsNumber++;
		}
		public void restartThread() {
			try{
				lock.lock();

				pause=false;
				isPause.signalAll();

			}finally{
				lock.unlock();
			}
		}
		public void pauseThread() {
			try{
				lock.lock();

				pause=true;
				isPause.signalAll();

			}finally{
				lock.unlock();
			}
		}
	}

	private synchronized void createNewSimulationProcess(Simulation sim){

		String path=this.createSimulationDirectoryByID(sim.getSimName()+""+sim.getSimID());
		sim.setSimulationFolder(path);
		getSimulationList().put(sim.getSimID(),sim);
		this.getConnection().publishToTopic(TOPIC_WORKER_ID_MASTER, this.TOPIC_WORKER_ID, "simrcv");


	}
	private String createSimulationDirectoryByID(String  name){
		String path=simulationsDirectories+File.separator+name;
		DMasonFileSystem.make(path+File.separator+"out");
		return path;
	}

	protected void deleteSimulationDirectoryByID(String simID){
		String path=simulationsDirectories+File.separator+simID;
		File c=new File(path);
		DMasonFileSystem.delete(c);
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
				//System.out.println("jar copy finished...");
				clientSocket.close();

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}	

	}


	@SuppressWarnings({ "rawtypes", "deprecation" })
	protected DistributedState makeSimulation(GeneralParam params, String prefix,String pathJar)
	{
		String path_jar_file=pathJar;
		try{
			@SuppressWarnings("resource")
			JarFile jar=new JarFile(new File(path_jar_file));
			Enumeration e=jar.entries();
			File file  = new File(path_jar_file);
			URL url = file.toURL(); 
			URL[] urls = new URL[]{url};
			@SuppressWarnings("resource")
			ClassLoader cl = new URLClassLoader(urls);
			Class distributedState=null;
			while(e.hasMoreElements()){

				JarEntry je=(JarEntry)e.nextElement();
				String classPath = je.getName();
				if(!je.getName().contains(".class")) continue;

				String[] nameclass = classPath.split("/");
				nameclass[0]=((nameclass[nameclass.length-1]).split(".class"))[0];

				Class c=cl.loadClass(je.getName().replaceAll("/", ".").replaceAll(".class", ""));

				if(c.getSuperclass().equals(DistributedState.class))
					distributedState=c;

			}
			if(distributedState==null) return null;
			@SuppressWarnings("resource")
			JarClassLoader cload = new JarClassLoader(new URL("jar:file://"+path_jar_file+"!/"));
			cload.addToClassPath();
			return (DistributedState) cload.getInstance(distributedState.getName(), params,prefix);
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;

	}


	
	private int findAvailablePort(){
		int port=FindAvailablePort.getPortAvailable();
		return port;
	}
	/**
	 * 
	 */
	private WorkerInfo getInfoWorker() 
	{
		WorkerInfo info=new WorkerInfo();
		info.setIP(WORKER_IP);
		info.setWorkerID(this.TOPIC_WORKER_ID_MASTER);
		info.setNumSlots(this.getSlotsNumber());
		int port =findAvailablePort();
		info.setPortCopyLog(port);
		return info;

	}

	private boolean createConnection(){
		Address address=new Address(this.getIpActivemq(), this.getPortActivemq());
		return conn.setupConnection(address);

	}
	public void signRequestToMaster(){
		try{	
			conn.createTopic("READY", 1);
			conn.subscribeToTopic("READY");
			conn.publishToTopic(this.TOPIC_WORKER_ID,"READY" ,"signrequest");
			conn.createTopic(this.TOPIC_WORKER_ID, 1);
			conn.subscribeToTopic(TOPIC_WORKER_ID);
		} catch(Exception e){e.printStackTrace();}
	}



	public boolean getLogBySimID(int simID){
	   Simulation sim=getSimulationList().get(simID);
	   String folderToCopy=sim.getSimulationFolder()+File.separator+"out";
	   String zippone=sim.getSimulationFolder()+File.separator+"out"+File.separator+"zippone.zip";
	   System.out.println("Copy file from folder "+folderToCopy+" to "+zippone);
	   return ZipDirectory.createZipDirectory(zippone, folderToCopy);	   

	   }
	   
	   
	


	private void generateFolders(String wID) throws FileNotFoundException {
		sdf = new SimpleDateFormat(); 
		sdf.applyPattern("dd-MM-yy-HH_mm");
		String dataStr = sdf.format(new Date()); // data corrente (20 febbraio 2014)
		workerDirectory=dmasonDirectory+File.separator+"worker"+File.separator+wID+File.separator+dataStr;
		workerTemporary=workerDirectory+File.separator+"temporary";
		simulationsDirectories=workerDirectory+File.separator+"simulations";
		DMasonFileSystem.make(workerTemporary);
		DMasonFileSystem.make(simulationsDirectories);
		DMasonFileSystem.make(workerDirectory+File.separator+"err");
		FileOutputStream output = new FileOutputStream(workerDirectory+File.separator+"err"+File.separator+"worker"+TOPIC_WORKER_ID+".err");
		PrintStream printOut = new PrintStream(output);
		System.setErr(printOut);
	}



	/**
	 * Explores NetworkInterface and finds IP Address 
	 * @return ip of Worker
	 */
	private static String getIP() {

		try {     
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


	@SuppressWarnings("serial")
	private void startMasterComunication() {
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

					if(map.containsKey("port")){
						int port=(int) map.get("port");
						DEFAULT_COPY_SERVER_PORT=port;
					}

				} catch (JMSException e) {
					e.printStackTrace();
				}

			}
		});
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
	public synchronized Integer getSlotsNumber(){return slotsNumber;}
	public int setSlotsNumuber(int slots){return this.slotsNumber=slots;}
	public HashMap<Integer, Simulation> getSimulationList(){return simulationList;}
}
