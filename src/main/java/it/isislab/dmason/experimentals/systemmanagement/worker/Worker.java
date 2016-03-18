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


import it.isislab.dmason.experimentals.systemmanagement.utils.ClientSocketCopy;
import it.isislab.dmason.experimentals.systemmanagement.utils.DMasonFileSystem;
import it.isislab.dmason.experimentals.systemmanagement.utils.FindAvailablePort;
import it.isislab.dmason.experimentals.systemmanagement.utils.ServerSocketCopy;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
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
	private int PORT_COPY_LOG;
	private int  slotsNumber=0;
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

	//Socket for log services
	protected Socket sock=null;
	protected ServerSocket welcomeSocket;


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
			this.PORT_COPY_LOG=findAvailablePort();
			welcomeSocket = new ServerSocket(PORT_COPY_LOG,1000,InetAddress.getByName(WORKER_IP));
			System.out.println("Starting worker ..."); 

		} catch (Exception e) {e.printStackTrace();}

		
	}



	/**
	 * start a simulation by id 
	 * @param id The id of simulation
	 */
	private	void playSimulationProcessByID(int id){
		try {
			if(getSimulationList().containsKey(id) && getSimulationList().get(id).getStatus().equals(Simulation.PAUSED))
			{  
				getSimulationList().get(id).setStatus(Simulation.STARTED);
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
					getSimulationList().put(simulation.getSimID(), simulation);
					executorThread.put(simulation.getSimID(),new ArrayList<CellExecutor>());
					for (CellType cellType : cellstype) {
						//slotsNumber--;
						params.setI(cellType.pos_i);
						params.setJ(cellType.pos_j);
						FileOutputStream output = new FileOutputStream(simulation.getSimulationFolder()+File.separator+"out"+File.separator+cellType+".out");
						PrintStream printOut = new PrintStream(output);

						CellExecutor celle=(new CellExecutor(params, prefix,simulation.getSimName()+""+simulation.getSimID(), simulation.getJarName(),printOut,simulation.getSimID(),
								(cellstype.indexOf(cellType)==0?true:false)));

						executorThread.get(simulation.getSimID()).add(celle);
						celle.startSimulation();
						getConnection().publishToTopic(simulation.getSimID(),"SIMULATION_READY"+simulation.getSimID(), "cellready");

					}

					getConnection().createTopic("SIMULATION_"+simulation.getSimID(), 1);
					getConnection().publishToTopic(simulation,"SIMULATION_"+simulation.getSimID(), "workerstatus");
		}catch(IOException e){
			e.printStackTrace();
		}
	}


	/**
	 * subscrive to topic of master for communication master->worker
	 */
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

					//request of resources info from master 
					if(map.containsKey("check")){
						String info=getInfoWorker().toString();
						getConnection().publishToTopic(info, TOPIC_WORKER_ID,"info");
					}
					// request of a storage a new simulation
					if(map.containsKey("newsim")){
						Simulation sim=(Simulation)map.get("newsim");
						createNewSimulationProcess(sim);
						downloadFile(sim,DEFAULT_COPY_SERVER_PORT);
						List<CellType> cellstype=sim.getCellTypeList();
						for (CellType cellType : cellstype) {
							slotsNumber--;
						}
						

					}
					// request to start a simulation
					if (map.containsKey("start")){
						int id = (int)map.get("start");
		                 
						if( (getSimulationList().get(id).getStatus() )!= Simulation.FINISHED && 
						    (getSimulationList().get(id).getStatus() )!= Simulation.STARTED)
							playSimulationProcessByID(id);

					}
					//request to stop a simulation
					if (map.containsKey("stop")){
						int id = (int)map.get("stop");
						if( (getSimulationList().get(id).getStatus()) != Simulation.FINISHED)
						stopSimulation(id);
					}
					//request to pause a simulation
					if (map.containsKey("pause")){
						int id = (int)map.get("pause");
						System.out.println("Command pause received for simulation "+id);
						if((getSimulationList().get(id).getStatus()!= Simulation.FINISHED) 
								&& (getSimulationList().get(id).getStatus()!= Simulation.PAUSED))
						pauseSimulation(id);
					}
					//log request of a simulation
					if(map.containsKey("logreq")){
						int id=(int)map.get("logreq");
						System.out.println("Received request for logs for simid "+id);
						String pre_status=getSimulationList().get(id).getStatus();
						if((pre_status.equals(Simulation.STARTED))){ 
							pauseSimulation(id); 
							getLogBySimIDProcess(id,pre_status,"log");
						}  
						else{ //PAUSED
							System.out.println("invoke getlog con "+pre_status);
							 getLogBySimIDProcess(id,pre_status,"log");
						}

					}
					//request to remove a simulation
					if (map.containsKey("simrm")){
						int id = (int)map.get("simrm");
						System.out.println("Command remove received for simulation "+id);
						deleteSimulationProcessByID(id);
					}


				} catch (JMSException e) {e.printStackTrace();} 


			}
		});
	}


	private HashMap<Integer,ArrayList<CellExecutor>> executorThread=new HashMap<Integer,ArrayList<CellExecutor>>();

	private synchronized void runSimulation(int sim_id){

		getSimulationList().get(sim_id).setStartTime(System.currentTimeMillis());
		getSimulationList().get(sim_id).setStep(0);
		for(CellExecutor cexe:executorThread.get(sim_id))
		{
			cexe.start();
		}
		getSimulationList().get(sim_id).setStatus(Simulation.STARTED);
		getSimulationList().get(sim_id).setStartTime(System.currentTimeMillis());
		getSimulationList().get(sim_id).setStep(0);
		getConnection().publishToTopic(getSimulationList().get(sim_id),"SIMULATION_"+sim_id, "workerstatus");


	}
	/**
	 * Stop simulation process
	 * @param sim_id
	 */
	private synchronized void stopSimulation(int sim_id)
	{
		getSimulationList().get(sim_id).setEndTime(System.currentTimeMillis());
		for(CellExecutor cexe:executorThread.get(sim_id))
		{
			if(cexe.masterCell){ 
				getSimulationList().get(sim_id).setStatus(Simulation.FINISHED);	
				getSimulationList().get(sim_id).setEndTime(System.currentTimeMillis());
				getConnection().publishToTopic(getSimulationList().get(sim_id),"SIMULATION_"+sim_id, "workerstatus");
			}
			cexe.stopThread();
		}
		getSimulationList().get(sim_id).setEndTime(System.currentTimeMillis());
		//System.out.println("Simulation "+sim_id+" stopped, with "+executorThread.get(sim_id).size());

		//start process to create a log file for this simulation
		String pre_status=getSimulationList().get(sim_id).getStatus();
		getLogBySimIDProcess(sim_id,pre_status,"history");

	}
	/**
	 * Pause simulation process
	 * @param sim_id
	 */
	private synchronized void pauseSimulation(int sim_id)
	{
		
		for(CellExecutor cexe:executorThread.get(sim_id))
		{
			cexe.pauseThread();
		}
		getSimulationList().get(sim_id).setStatus(Simulation.PAUSED);
		getSimulationList().get(sim_id).setEndTime(System.currentTimeMillis());
		getConnection().publishToTopic(getSimulationList().get(sim_id),"SIMULATION_"+sim_id, "workerstatus");
		//System.out.println("Simulation "+sim_id+" paused, with "+executorThread.get(sim_id).size());



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
			
            
			while(i!=params.getMaxStep() && run)
			{   
				if(i%500==0){System.out.println("STEP NUMBER "+dis.schedule.getSteps()+" for simid"+sim_id );}
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
					getSimulationList().get(sim_id).setStep(i);
					getConnection().publishToTopic(getSimulationList().get(sim_id),"SIMULATION_"+sim_id, "workerstatus");
				}
				dis.schedule.step(dis);
				i++;

			}

			if(i==params.getMaxStep() && masterCell){
				getSimulationList().get(sim_id).setEndTime(System.currentTimeMillis());
				getSimulationList().get(sim_id).setStatus(Simulation.FINISHED);
				getConnection().publishToTopic(getSimulationList().get(sim_id),"SIMULATION_"+sim_id, "workerstatus");
				//setSlotsNumuber(getSlotsNumber()+getSimulationList().get(sim_id).getCellTypeList().size());
				
				// process to create log file
				String pre_status=getSimulationList().get(sim_id).getStatus();
				getLogBySimIDProcess(sim_id,pre_status,"history");
			}



		}




		public synchronized void stopThread()
		{
			run=false;
			//slotsNumber++;
			
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
        
		
		//added dal costruttore
		String createTopicSimReady="SIMULATION_READY"+sim.getSimID();
		getConnection().createTopic(createTopicSimReady, 1);
	    try {
			getConnection().subscribeToTopic(createTopicSimReady);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    getConnection().asynchronousReceive(createTopicSimReady, new MyMessageListener() {
			
			@Override
			public void onMessage(Message msg) {
				Object o;
				try {
					o=parseMessage(msg);
					MyHashMap map=(MyHashMap) o;
					if(map.containsKey("cellready")){
						int sim_id=(int) map.get("cellready");
						//if(getSimulationList().containsKey(sim_id)) {

							getSimulationList().get(sim_id).setReceived_cell_type(getSimulationList().get(sim_id).getReceived_cell_type()+1);
							if(getSimulationList().get(sim_id).getReceived_cell_type()==getSimulationList().get(sim_id).getNumCells())
							{
								runSimulation(sim_id);
							}
						//}
					} 


				} catch (JMSException e) {e.printStackTrace();}
				
			}
		});
		

		
        

	}
	private String createSimulationDirectoryByID(String  name){
		String path=simulationsDirectories+File.separator+name;
		DMasonFileSystem.make(path+File.separator+"out");
		return path;
	}

	public  synchronized void deleteSimulationProcessByID(int simID){
		Simulation s =getSimulationList().get(simID); 
		String folder=s.getSimulationFolder();
		DMasonFileSystem.delete(new File(folder));
		List<CellType> cellstype=s.getCellTypeList();
		for (CellType cellType : cellstype) {
			slotsNumber++;
		}
		getSimulationList().remove(simID);
	}






	private synchronized void downloadFile(Simulation sim,int serverSocketPort){ 

		String local=System.currentTimeMillis()+sim.getJarName();
		sim.setJarName(local);
		String localJarFilePath=sim.getSimulationFolder()+File.separator+local;

		Socket clientSocket;
		try {
			clientSocket = new Socket( this.IP_ACTIVEMQ , serverSocketPort );
			Thread tr=null;
			tr=new Thread(new ClientSocketCopy(clientSocket, localJarFilePath));
			tr.start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}


	@SuppressWarnings({ "rawtypes", "deprecation" })
	private DistributedState makeSimulation(GeneralParam params, String prefix,String pathJar)
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
		info.setPortCopyLog(PORT_COPY_LOG);
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




	/**
	 * 
	 * @param simID
	 * @param status
	 * @param type simple log or log for  history 
	 */
	public synchronized void getLogBySimIDProcess(int simID,String status, String type){
		Simulation sim=getSimulationList().get(simID);
		String folderToCopy=sim.getSimulationFolder()+File.separator+"out";
		String fileToSend=sim.getSimulationFolder()+File.separator+"out"+File.separator+this.TOPIC_WORKER_ID_MASTER+".zip";

		if(type.equals("log")){
			if(getLogBySimID(folderToCopy,fileToSend)){
				System.out.println("File zip creato nella dir "+fileToSend);			
				if(status.equals(Simulation.STARTED))playSimulationProcessByID(simID);
				
				if(startServiceCopyForLog(fileToSend,simID,"logready"))
					DMasonFileSystem.delete(new File(fileToSend));
			}
		}

		else{ //history

			if(getLogBySimID(folderToCopy,fileToSend)){
				System.out.println("File zip creato nella dir "+fileToSend);			

				if(startServiceCopyForLog(fileToSend,simID,"loghistory")){
					System.out.println("File zip creato nella dir "+fileToSend);
					DMasonFileSystem.delete(new File(fileToSend));
				}	
			}
		}	
	} 





	/**
	 * 
	 * @param zipFile
	 * @param id
	 * @param type logready || loghistory
	 * @return
	 */
	private boolean startServiceCopyForLog(String zipFile,int id,String type){
		System.out.println("apro stream copia per "+zipFile+" su porta"+welcomeSocket.getLocalPort());
		getConnection().publishToTopic(id, this.TOPIC_WORKER_ID, type);
		try{
			Thread t=null;
			//System.out.println("mi metto in accept");
			sock = welcomeSocket.accept();
			//System.out.println("mi sblocco dalla accept");
			t=new Thread(new ServerSocketCopy(sock,zipFile));
			t.start();
			t.join();
			sock.close();

		}catch (UnknownHostException e) {e.printStackTrace();} catch (IOException e) {
			if (welcomeSocket != null && !welcomeSocket.isClosed()) {
				try {welcomeSocket.close();} 
				catch (IOException exx){exx.printStackTrace(System.err); return false;}
			}
		} catch (InterruptedException e) {e.printStackTrace();}


		return true;
	}



	private boolean getLogBySimID(String folderToCopy, String zipPath){
		System.out.println("Copy file from folder "+folderToCopy+" to "+zipPath);
		return ZipDirectory.createZipDirectory(zipPath, folderToCopy);	   

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
						System.out.println("Ack signup received ...");
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
	public synchronized int setSlotsNumuber(int slots){return this.slotsNumber=slots;}
	public HashMap<Integer, Simulation> getSimulationList(){return simulationList;}
}
