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
import it.isislab.dmason.experimentals.systemmanagement.utils.loader.DMasonClassLoader;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.experimentals.util.visualization.globalviewer.RemoteSnap;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.TraceableField;
import it.isislab.dmason.util.connection.Address;
import it.isislab.dmason.util.connection.MyHashMap;
import it.isislab.dmason.util.connection.jms.activemq.ConnectionNFieldsWithActiveMQAPI;
import it.isislab.dmason.util.connection.jms.activemq.MyMessageListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Worker class for DMason System Management
 * 
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 *
 */
public class Worker implements Observer {
	private String IP_ACTIVEMQ = ""; /* ip activemq */
	private String PORT_ACTIVEMQ = ""; /* port activemq */
	private int PORT_COPY_LOG; /* port for local socket server */
	private int slotsNumber = 0; // number of available slots(cells of a field in dmason)
	private static int slotsNumberBackup = 0; // a copy backup of slots number value for reconnection 
	private static final String MANAGEMENT = "DMASON-MANAGEMENT";
	private static String dmasonDirectory = System.getProperty("user.dir") + File.separator + "dmason";
	private static String workerDirectory; // worker main directory
	private static String simulationsDirectories; // list of simulations' folder
	private String TOPIC_WORKER_ID = ""; // worker topic, worker writes in this topic (publish) for all communication           
	private static String WORKER_IP = "127.0.0.1"; // local ip of node 
	private int DEFAULT_COPY_SERVER_PORT = 1414; /* default port of master node for copy log file */
	private HashMap<Integer, Simulation> simulationList; // List of simulation executed on this worker
	private SimpleDateFormat sdf = null;
	private ConnectionNFieldsWithActiveMQAPI conn = null;
	private FindAvailablePort availableport;
	private boolean MASTER_ACK = false;
	final Lock lock = new ReentrantLock();
	final Condition waitMaster = lock.newCondition();
	final Lock lockconnection = new ReentrantLock();
	final Condition waitconnection = lockconnection.newCondition();
    private AtomicBoolean publishInfo = new AtomicBoolean(false);

	// Socket for log services
	protected Socket sock = null;
	protected ServerSocket welcomeSocket;

	/// INTERNAL LOGGER FOR DEBUG 
	private static final Logger LOGGER = Logger.getLogger(Worker.class.getName()); //show constructor to enable Logger

	/**
	 * Worker Constructor
	 * @param ipMaster   ip ActivemQ
	 * @param portMaster port ActivemQ
	 * @param slots number of cells that node can execute
	 */
	public Worker(String ipMaster, String portMaster, int slots) {
		try {
            System.out.println("Starting worker " + InetAddress.getLocalHost().getHostName());
			// comment following line to enable Logger 
			LOGGER.setUseParentHandlers(false);
			LOGGER.info("LOGGER ENABLE");

			//
            publishInfo.set(true);
			this.IP_ACTIVEMQ = ipMaster;
			this.PORT_ACTIVEMQ = portMaster;
			this.conn = new ConnectionNFieldsWithActiveMQAPI();
			WORKER_IP = getIP(); //find local ip

			this.TOPIC_WORKER_ID = "WORKER-" + WORKER_IP + "-" + new UID(); 
			/************************************************
			 * @author miccar
			 * character ":" causes folder creation error on operating system Windows like  
			 * YOU MUST NOT DELETE FOLLOWING LINE OF CODE 
			 */
			TOPIC_WORKER_ID = TOPIC_WORKER_ID.replace(":", "");
			/****************************************************/

			generateFolders(TOPIC_WORKER_ID); // generate folders for worker
			this.TOPIC_WORKER_ID = "" + TOPIC_WORKER_ID.hashCode(); // my topic
			System.out.println("ID: " + this.TOPIC_WORKER_ID + ", " + slots + " slots");

			simulationList = new HashMap</*idsim*/Integer, Simulation>();
			this.slotsNumber = slots;
			slotsNumberBackup = slots; // for reconnection
			availableport = new FindAvailablePort(1000, 3000); // find an available port on a fixed range <x,y> on nodes-> for Socket node -send ->master 
			this.PORT_COPY_LOG = availableport.getPortAvailable(); // socket communication with master (server side, used for logs)
			welcomeSocket = new ServerSocket(PORT_COPY_LOG, 1000, InetAddress.getByName(WORKER_IP)); // create server for socket communication 
			getConnection().addObserver(this); // observer on connection status

			connectToMessageBroker();
		} catch (Exception e) {e.printStackTrace();}
	}

	/**
	 * Create a connection, and start communication with master
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private void connectToMessageBroker() throws UnknownHostException, IOException {
		System.out.println("Waiting for connection to Message Broker..");
		this.createConnection();

		System.out.println("Waiting master connection ..."); 
		this.startMasterComunication();

		System.out.println("connected.");
	}

	/**
	 * listener for connection status 
	 */
	@Override
	public void update(Observable obs, Object arg) {
		if (obs == conn){
			if (!getConnection().isConnected()) {
				this.publishInfo.set(false); //interrupt loop of topic publish 
				this.simulationList = new HashMap</*idsim*/Integer, Simulation>(); // reset hashmap of simulations
				this.slotsNumber = slotsNumberBackup; // reset default slot number
			}
			if (getConnection().isConnected()) {
				// on reconnection launch new istance of worker
				conn = null;					
				new Worker(IP_ACTIVEMQ,PORT_ACTIVEMQ, slotsNumber);
			}
		}
	}

	/**
	 * 
	 * Class for sending information to master 
	 * on topic 
	 *
	 */
	class MasterLostChecker extends Thread {
		public MasterLostChecker() {
		}
		@Override
		public void run() {
			while (publishInfo.get()) {
				try {
					Thread.sleep(new Random().nextInt(3) * 1000);
					if (getConnection().getTopicList().contains(MANAGEMENT)) {
						getConnection().publishToTopic(getInfoWorker().toString(), MANAGEMENT, "WORKER");
					}
				} catch (Exception e) {e.printStackTrace();}
			}
		}
	}

	/**
	 * Wait for master connection, and save Socket port node <-send- master
	 */
	@SuppressWarnings("serial")
	private void startMasterComunication() {
		try {
			getConnection().createTopic(MANAGEMENT, 1);
			getConnection().subscribeToTopic(MANAGEMENT);			
			getConnection().createTopic(TOPIC_WORKER_ID,1); 
			getConnection().subscribeToTopic(TOPIC_WORKER_ID);
			listenerForMasterComunication();
		} 
		catch (Exception e1) {e1.printStackTrace();}

		new MasterLostChecker().start();

		getConnection().asynchronousReceive(MANAGEMENT, new MyMessageListener() {
			@Override
			public void onMessage(Message msg) {
				Object o;
				try {
					o = parseMessage(msg);
					MyHashMap map = (MyHashMap) o;
					if (map.containsKey("WORKER-ID-" + TOPIC_WORKER_ID)){
						///
						try {
							lock.lock();
							if (!MASTER_ACK)
							{
								MASTER_ACK = true;
//								System.out.println("Master connected...");
								DEFAULT_COPY_SERVER_PORT = (int)map.get("WORKER-ID-" + TOPIC_WORKER_ID);
								waitMaster.signalAll();
							}
						} finally {
							lock.unlock();
						}
					}
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Return information of: 
	 * - hw resources(cpu, ram)
	 * - port of node for server socket
	 * - topic that identify this node
	 * - topic IP
	 * - slots still available for this node
	 */
	private WorkerInfo getInfoWorker() {
		WorkerInfo info = new WorkerInfo();
		info.setIP(WORKER_IP);
		info.setWorkerID(this.TOPIC_WORKER_ID);
		info.setNumSlots(this.getSlotsNumber());
		info.setPortCopyLog(PORT_COPY_LOG);
		return info;
	}
    
	/**
	 * Create connection with ActivemQ server
	 *
	 */
	private boolean createConnection() {
		Address address = new Address(this.getIpActivemq(), this.getPortActivemq());
		return getConnection().setupConnection(address);
	}

	/**
	 * Subscribe to master topic for communication [master->worker]
	 * Request list from master
	 */
	@SuppressWarnings("serial")
	private synchronized void listenerForMasterComunication() {
		getConnection().asynchronousReceive(TOPIC_WORKER_ID, new MyMessageListener() {
			@Override
			public void onMessage(Message msg) {
				Object o;
				try {
					o = parseMessage(msg);
					final MyHashMap map = (MyHashMap) o;

					if (map.containsKey("shutdown")){
						shutdownWorker();
					} else if (map.containsKey("startViewer")) {
						int id = (int)map.get("startViewer");
						
						startProcessImagesForSim(id);
					} else if (map.containsKey("stopViewer")) {
						int id = (int)map.get("stopViewer");
								
						stopProcessImagesForSim(id);
					}

					// request of a storage a new simulation
					else if (map.containsKey("newsim")) {
						Simulation sim = (Simulation)map.get("newsim");
						createNewSimulationProcess(sim);
//						System.out.println("open stream port " + DEFAULT_COPY_SERVER_PORT);
						downloadFile(sim, DEFAULT_COPY_SERVER_PORT);
						List<CellType> cellstype = sim.getCellTypeList();
						for (CellType cellType: cellstype) {
							slotsNumber--;
						}

					// request to start a simulation
					} else if (map.containsKey("start")) {
						int id = (int)map.get("start");

						if ((getSimulationList().get(id).getStatus() ) != Simulation.FINISHED && 
									(getSimulationList().get(id).getStatus() )!= Simulation.STARTED)
							playSimulationProcessByID(id);

					// request to stop a simulation
					} else if (map.containsKey("stop")) {
						int id = (int)map.get("stop");
						if ((getSimulationList().get(id).getStatus()) != Simulation.FINISHED)
							stopSimulation(id);
					// request to pause a simulation
					} else if (map.containsKey("pause")) {
						int id = (int)map.get("pause");
						LOGGER.info("Command pause received for simulation " + id);
						if ((getSimulationList().get(id).getStatus()!= Simulation.FINISHED) &&
								(getSimulationList().get(id).getStatus()!= Simulation.PAUSED))
							pauseSimulation(id);
					// log request of a simulation
					} else if (map.containsKey("logreq")) {
						int id = (int)map.get("logreq");
						LOGGER.info("Received request for logs for simid " + id);
						String pre_status = getSimulationList().get(id).getStatus();
						if ((pre_status.equals(Simulation.STARTED))) { 
							pauseSimulation(id); 
							getLogBySimIDProcess(id,pre_status,"log");
						} else { // PAUSED
							LOGGER.info("invoke getlog con " + pre_status);
							getLogBySimIDProcess(id,pre_status,"log");
						}
					// request to remove a simulation
					} else if (map.containsKey("simrm")) {
						int id = (int)map.get("simrm");
						LOGGER.info("Command remove received for simulation " + id);
						deleteSimulationProcessByID(id);
						LOGGER.info("simulation " + id + " removed!"); // TODO evaluate removal of this line
					}
				} catch (JMSException e) {e.printStackTrace();} 
			}
		});
	}
	
	private synchronized void startProcessImagesForSim(int id) {
//		System.out.println("ricevo richiesta immagine " + id);
		for (CellExecutor cexe: executorThread.get(id)) {
			((TraceableField) cexe.dis.getField()).trace("-GRAPHICS");
		}
	}

	private synchronized void stopProcessImagesForSim(int id) {
		for (CellExecutor cexe: executorThread.get(id)) {
			((TraceableField) cexe.dis.getField()).untrace("-GRAPHICS");
		}
	}

	private HashMap<Integer, ArrayList<CellExecutor>> executorThread = new HashMap<Integer, ArrayList<CellExecutor>>();

	/**
	 * Start Thread for simulation running
	 * @param sim_id id of simulation to run
	 */
	private synchronized void runSimulation(int sim_id) {
		getSimulationList().get(sim_id).setStartTime(System.currentTimeMillis());
		getSimulationList().get(sim_id).setStep(0);
		for (CellExecutor cexe: executorThread.get(sim_id)) {
			cexe.start();
		}
		getSimulationList().get(sim_id).setStatus(Simulation.STARTED);
		getSimulationList().get(sim_id).setStartTime(System.currentTimeMillis());
		getSimulationList().get(sim_id).setStep(0);
		getConnection().publishToTopic(getSimulationList().get(sim_id), "SIMULATION_" + sim_id, "workerstatus");
	}

	/**
	 * Start a simulation for first time, or a paused simulation -> when receive "start"
	 * start a simulation by id 
	 * @param id ID of simulation to play
	 */
	private	void playSimulationProcessByID(int id) {
		try {
			if (getSimulationList().containsKey(id) && getSimulationList().get(id).getStatus().equals(Simulation.PAUSED)) {  
				getSimulationList().get(id).setStatus(Simulation.STARTED);
				for (CellExecutor cexe:executorThread.get(id)) {
					cexe.restartThread();
				}

				return;
			}

			GeneralParam params = null;
			Simulation simulation = getSimulationList().get(id);

			List<CellType> cellstype = simulation.getCellTypeList();
			int aoi = simulation.getAoi();
			int height = simulation.getHeight();
			int width = simulation.getWidth();
			int cols = simulation.getColumns();
			int rows = simulation.getRows();
			int agents = simulation.getNumAgents();
			int mode = simulation.getMode();
			int p = simulation.getP();
			int typeConn = simulation.getConnectionType();
			long step = simulation.getNumberStep();

			params = (simulation.getMode() == DistributedField2D.UNIFORM_PARTITIONING_MODE) ?
					new GeneralParam(width, height, aoi, rows, cols, agents, mode, step, typeConn /*ConnectionType.pureActiveMQ*/) :
						new GeneralParam(width, height, aoi, p, agents, mode, step, typeConn /*ConnectionType.pureActiveMQ*/);
			params.setIp(IP_ACTIVEMQ);
			params.setPort(PORT_ACTIVEMQ);
			getSimulationList().put(simulation.getSimID(), simulation);
			executorThread.put(simulation.getSimID(), new ArrayList<CellExecutor>());

			for (CellType cellType: cellstype) {
				params.setI(cellType.pos_i);
				params.setJ(cellType.pos_j);
				FileOutputStream output = new FileOutputStream(simulation.getSimulationFolder() + File.separator + "out" + File.separator + cellType + ".out");
				PrintStream printOut = new PrintStream(output);
				CellExecutor celle = (new CellExecutor(params, printOut, simulation.getSimID(),
						(cellstype.indexOf(cellType) == 0 ? true : false)));

				executorThread.get(simulation.getSimID()).add(celle);
				celle.startSimulation();
				getConnection().publishToTopic(simulation.getSimID(), "SIMULATION_READY" + simulation.getSimID(), "cellready");

				if (celle.masterCell) {
					getConnection().createTopic(simulation.getTopicPrefix() + "GRAPHICS", 1);
					try {
						getConnection().subscribeToTopic(simulation.getTopicPrefix() + "GRAPHICS");
						getConnection().asynchronousReceive(simulation.getTopicPrefix() + "GRAPHICS", new MyMessageListener() {
							private static final long serialVersionUID = 1L;

							@Override
							public void onMessage(Message msg) {
								Object o;
								try {
									o = parseMessage(msg);
									MyHashMap map = (MyHashMap) o;

									for (Object obj: map.values()) {
										RemoteSnap rs = (RemoteSnap)obj;
										if (getSimulationList().get(id).getSnapshots().get(rs.step) == null)
//											imgs = new ArrayList<>();
											getSimulationList().get(id).getSnapshots().put(rs.step, new ArrayList<>());

										getSimulationList().get(id).getSnapshots().get(rs.step).add(rs);
									}
								} catch (JMSException e) {
									e.printStackTrace();
								}
							}
						});
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					getConnection().createTopic(simulation.getTopicPrefix() + "GRAPHICS", 1);
				}
			}

			getConnection().createTopic("SIMULATION_" + simulation.getSimID(), 1);
			getConnection().publishToTopic(simulation, "SIMULATION_" + simulation.getSimID(), "workerstatus");
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Start stop process for a sim -> when receive "stop" 
	 * @param sim_id id of simulation to stop 
	 */
	private synchronized void stopSimulation(int sim_id) {
		for(CellExecutor cexe:executorThread.get(sim_id)) {
			if (cexe.masterCell) { 
				getSimulationList().get(sim_id).setStatus(Simulation.STOPPED);	
				getSimulationList().get(sim_id).setEndTime(System.currentTimeMillis());
				getConnection().publishToTopic(getSimulationList().get(sim_id), "SIMULATION_" + sim_id, "workerstatus");
			}
			cexe.stopThread();
		}

		// start process to create a log file for this simulation
		String pre_status = getSimulationList().get(sim_id).getStatus();
		getLogBySimIDProcess(sim_id, pre_status, "history");
	}

	/**
	 * Pause simulation process
	 * @param sim_id id of simulation to pause
	 */
	private synchronized void pauseSimulation(int sim_id) {
		for (CellExecutor cexe:executorThread.get(sim_id)) {
			cexe.pauseThread();
			((TraceableField)cexe.dis.getField()).untrace("-GRAPHICS");
		}
		getSimulationList().get(sim_id).setStatus(Simulation.PAUSED);
		getSimulationList().get(sim_id).setEndTime(System.currentTimeMillis());
		getConnection().publishToTopic(getSimulationList().get(sim_id), "SIMULATION_" + sim_id, "workerstatus");
	}

	/*****************CELLEXECUTOR CLASS******************************/
	/**
	 * Class for start, stop, pause simulation with Thread
	 *
	 */
	class CellExecutor extends Thread {
		public GeneralParam params;
		public String prefix;
		public String folder_sim;
		public String jar_pathname;
		private int sim_id;
		public boolean run = true;
		public boolean pause = false;
		public boolean masterCell = false;
		@SuppressWarnings("rawtypes")
		private DistributedState dis;

		final Lock lock = new ReentrantLock();
		final Condition isPause = lock.newCondition(); 

		public CellExecutor(GeneralParam params, PrintStream out, int sim_id, boolean master_cell) {
			super();
			this.params = params;
			this.sim_id = sim_id;
			Simulation sim = getSimulationList().get(sim_id);
			this.prefix = sim.getTopicPrefix(); // prefix;
			this.folder_sim = sim.getSimulationFolder(); // folder_name;
			this.jar_pathname = sim.getJarPath(); // jar_path;
			dis = makeSimulationWithNewLoader(params, prefix, jar_pathname);
			dis.setOutputStream(out);
			this.masterCell = master_cell;
		}

		public void startSimulation() {
			dis.start();
		}

		@Override
		public void run() {
			LOGGER.info("Start cell for " + params.getMaxStep());
			int i = 0;

			while (i != params.getMaxStep() && run) {   
				if (i%500 == 0) {LOGGER.info("STEP NUMBER " + dis.schedule.getSteps() + " for simid" + sim_id );} // testing
				try {
					lock.lock();

					while (pause) {
						isPause.await();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					lock.unlock();
				}
				if (masterCell) {   
					getSimulationList().get(sim_id).setStep(i);

					getConnection().publishToTopic(getSimulationList().get(sim_id), "SIMULATION_" + sim_id, "workerstatus");
				}
				dis.schedule.step(dis);
				i++;
			}

			// simulation finished             
			if ((i == params.getMaxStep()) && masterCell) {
				getSimulationList().get(sim_id).setEndTime(System.currentTimeMillis());
				getSimulationList().get(sim_id).setStatus(Simulation.FINISHED);
				getConnection().publishToTopic(getSimulationList().get(sim_id), "SIMULATION_" + sim_id, "workerstatus");

				// process to create log file
				String pre_status = getSimulationList().get(sim_id).getStatus();
				getLogBySimIDProcess(sim_id, pre_status, "history");
			}
		}

		public synchronized void stopThread() {
			run = false;
		}

		public void restartThread() {
			try {
				lock.lock();

				pause = false;
				isPause.signalAll();

			} finally {
				lock.unlock();
			}
		}

		public void pauseThread() {
			try {
				lock.lock();

				pause = true;
				isPause.signalAll();

			} finally {
				lock.unlock();
			}
		}
	}

	/*****************CELLEXECUTOR CLASS******************************/

	/**
	 * Create a new sim execution process    
	 * @param sim the simulation object
	 */
	@SuppressWarnings("serial")
	private synchronized void createNewSimulationProcess(Simulation sim) {
		String path = this.createSimulationDirectoryByID(sim.getSimName() + "" + sim.getSimID());
		sim.setSimulationFolder(path);
		getSimulationList().put(sim.getSimID(), sim);
		String createTopicSimReady = "SIMULATION_READY" + sim.getSimID();
		getConnection().createTopic(createTopicSimReady, 1);

//		getConnection().createTopic(sim.getTopicPrefix() + "GRAPHICS", 1);

		try {
			getConnection().subscribeToTopic(createTopicSimReady);
		} catch (Exception e1) {e1.printStackTrace();}

		getConnection().asynchronousReceive(createTopicSimReady, new MyMessageListener() {
			public void onMessage(Message msg) {
				Object o;
				try {
					o = parseMessage(msg);
					MyHashMap map = (MyHashMap) o;
					if (map.containsKey("cellready")){
						int sim_id = (int) map.get("cellready");

						getSimulationList().get(sim_id).setReceived_cell_type(getSimulationList().get(sim_id).getReceived_cell_type() + 1);
						if (getSimulationList().get(sim_id).getReceived_cell_type()==getSimulationList().get(sim_id).getNumCells()) {
							runSimulation(sim_id);
						}
					} 
				} catch (JMSException e) {e.printStackTrace();}
			}
		});	
	}

	/**
	 * Create a folder for a Simulation
	 * @param name name of folder 
	 * @return path of created folder
	 */
	private String createSimulationDirectoryByID(String  name) {
		String path = simulationsDirectories + File.separator + name;
		DMasonFileSystem.make(path + File.separator + "out");
		return path;
	}

	/**
	 * Start process for remove a simulation
	 * @param simID id of Simulation
	 */
	public synchronized void deleteSimulationProcessByID(int simID) {
		Simulation s = getSimulationList().get(simID); 
		String folder = s.getSimulationFolder();
		DMasonFileSystem.delete(new File(folder));
		List<CellType> cellstype = s.getCellTypeList();
		for (CellType cellType: cellstype) {
			slotsNumber++;
		}
		getSimulationList().remove(simID);
	}

	/**
	 * Download with Socket the jar of a sim from master 
	 * @param sim
	 * @param serverSocketPort
	 */
	private synchronized void downloadFile(Simulation sim, int serverSocketPort) {
		String jarName = System.currentTimeMillis() + ".jar";

		String localJarFilePath = sim.getSimulationFolder() + File.separator + jarName;
		sim.setJarPath(localJarFilePath);
		Socket clientSocket;

		try {
			clientSocket = new Socket(this.IP_ACTIVEMQ, serverSocketPort);
			Thread tr = null;
			System.out.println("copy in " + localJarFilePath);
			tr = new Thread(new ClientSocketCopy(clientSocket, localJarFilePath));
			tr.start();
			tr.join();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		this.getConnection().publishToTopic(sim.getSimID(), TOPIC_WORKER_ID, "simrcv");
	}

//	/**
//	 * Get Distributed state instance of simulation from jar file
//	 * @param params
//	 * @param prefix
//	 * @param pathJar
//	 * @return
//	 */
//	@Deprecated
//	private DistributedState makeSimulation(GeneralParam params, String prefix, String pathJar)
//	{
//		String path_jar_file = pathJar;
//		try {
//			JarFile jar = new JarFile(new File(path_jar_file));
//			Enumeration e = jar.entries();
//			File file = new File(path_jar_file);
//			URL url = file.toURL(); 
//			URL[] urls = new URL[]{url};
//			ClassLoader cl = new URLClassLoader(urls);
//			Class distributedState = null;
//
//			while (e.hasMoreElements()) {
//				JarEntry je = (JarEntry)e.nextElement();
//				if (!je.getName().contains(".class")) continue;
//
//				Class c = cl.loadClass(je.getName().replaceAll("/", ".").replaceAll(".class", ""));
//
//				if (c.getSuperclass().equals(DistributedState.class))
//					distributedState = c;
//			}
//
//			if (distributedState == null) return null;
//			//JarClassLoader cload = new JarClassLoader(new URL("jar:file://" + path_jar_file + "!/"));
//			JarClassLoader cload = new JarClassLoader(new File(path_jar_file).toURI().toURL());
//			cload.addToClassPath();
//			return (DistributedState) cload.getInstance(distributedState.getName(), params, prefix);
//		} catch (Exception e){
//			e.printStackTrace();
//		}
//
//		return null;
//	}

	/**
	 * Get Distributed state instance of simulation from jar file and 
	 * avoid dynamically classloading problem  of the same path from multiple jars
	 * @param params
	 * @param prefix
	 * @param pathJar
	 */
	private DistributedState<?> makeSimulationWithNewLoader(GeneralParam params, String prefix, String pathJar) {
		String path_jar_file = pathJar;

		try {
			JarFile jar = new JarFile(new File(path_jar_file));
			Enumeration<JarEntry> e = jar.entries();
			File file = new File(path_jar_file);
			String u = file.toURI().toURL().toString(); 
			URL url = new URL(u);
			URL[] urls = new URL[]{url};

			URLClassLoader aUrlCL = new URLClassLoader(urls, new DMasonClassLoader());
			Thread.currentThread().setContextClassLoader(aUrlCL);
			Class<?> distributedState = null;

			while (e.hasMoreElements()) {
				JarEntry je = (JarEntry)e.nextElement();
				if (!je.getName().contains(".class")) continue;

				Class<?> c = aUrlCL.loadClass(je.getName().replaceAll("/", ".").replaceAll(".class", ""));

				if (c.getSuperclass().equals(DistributedState.class)) {
					distributedState = c;
				}
			}
			jar.close();

			if (distributedState == null) return null;

			Class<?> urlClass = aUrlCL.getClass(); //URLClassLoader.class;////

			Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
			method.setAccessible(true);
			method.invoke(aUrlCL, new Object[]{url});;

			Class<?> simClass = aUrlCL.loadClass(distributedState.getName());
			Constructor<?> constr = simClass.getConstructor(new Class[]{GeneralParam.class, String.class});
			Object obj = constr.newInstance(new Object[]{params, prefix});

			return (DistributedState<?>) obj;
		} catch (Exception e){
			e.printStackTrace();
		} catch (Throwable e1) {
			e1.printStackTrace();
		}

		return null; // TODO does this have to return null?
	}

	/**
	 * Create log for a Simulation 
	 * @param simID the id of simulation
	 * @param status log(a request of log) | history(a request of a finished simulation)
	 * @param type simple log or log for  history 
	 */
	public synchronized void getLogBySimIDProcess(int simID, String status, String type) {
		Simulation sim = getSimulationList().get(simID);
		String folderToCopy = sim.getSimulationFolder() + File.separator + "out";
		String fileToSend = sim.getSimulationFolder() + File.separator + "out" + File.separator + this.TOPIC_WORKER_ID + ".zip";

		if (type.equals("log")) {
			if (getLogBySimID(folderToCopy, fileToSend)) {
				System.out.println("Zip File created to " + fileToSend);			
				if (status.equals(Simulation.STARTED))
					playSimulationProcessByID(simID);

				if (startServiceCopyForLog(fileToSend, simID, "logready"))
					DMasonFileSystem.delete(new File(fileToSend));
			}
		} else { // type.equals("history")
			if (getLogBySimID(folderToCopy, fileToSend)) {
				if (startServiceCopyForLog(fileToSend, simID, "loghistory")) {
					System.out.println("Zip File created to " + fileToSend);
					DMasonFileSystem.delete(new File(fileToSend));
				}
			}
		}
	}

	/**
	 * Start Server with socket to send log
	 * @param zipFile
	 * @param id
	 * @param type logready || loghistory
	 * @return
	 */
	private boolean startServiceCopyForLog(String zipFile, int id, String type) {
		LOGGER.info("open stream copy of " + zipFile + " on port " + welcomeSocket.getLocalPort());
		getConnection().publishToTopic(id, TOPIC_WORKER_ID, type);
		try {
			Thread t = null;

			LOGGER.info("accept block");
			sock = welcomeSocket.accept();
			LOGGER.info("exit accept ");

			t = new Thread(new ServerSocketCopy(sock, zipFile));
			t.start();
			t.join();
			sock.close();
		} catch (UnknownHostException e) {e.printStackTrace();} catch (IOException e) {
			if (welcomeSocket != null && !welcomeSocket.isClosed()) {
				try {welcomeSocket.close();}
				catch (IOException exx){exx.printStackTrace(System.err); return false;}
			}
		} catch (InterruptedException e) {e.printStackTrace();}

		return true;
	}

	private boolean getLogBySimID(String folderToCopy, String zipPath) {
//		System.out.println("Copy file from folder " + folderToCopy + " to " + zipPath);
		return ZipDirectory.createZipDirectory(zipPath, folderToCopy);	   
	}

	private void shutdownWorker() {
		publishInfo.set(false);

		Thread v = new Thread(new Runnable() {
			@Override
			public void run() {
				getConnection().publishToTopic("", TOPIC_WORKER_ID, "disconnect");				
			}
		});
		v.start();
		try {
			v.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			System.out.println("Shutdown worker " + InetAddress.getLocalHost().getHostName() + "(" + this.TOPIC_WORKER_ID + ")");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	/**
	 * Create all folder for worker environment 
	 * @param wID
	 * @throws FileNotFoundException
	 */
	private void generateFolders(String wID) throws FileNotFoundException {
		sdf = new SimpleDateFormat(); 
		sdf.applyPattern("dd-MM-yy-HH_mm");
		String dataStr = sdf.format(new Date());
		workerDirectory = dmasonDirectory + File.separator + "worker" + File.separator + wID + File.separator + dataStr;
		simulationsDirectories = workerDirectory + File.separator + "simulations";
		DMasonFileSystem.make(simulationsDirectories);
		DMasonFileSystem.make(workerDirectory + File.separator + "err");
		FileOutputStream output = new FileOutputStream(workerDirectory + File.separator + "err" + File.separator + "worker" + TOPIC_WORKER_ID + ".err");
		PrintStream printOut = new PrintStream(output);
		System.setErr(printOut);
	}

	/**
	 * Explores NetworkInterface and finds IP Address 
	 * @return ip of Worker
	 */
	private static String getIP() {
		try {     
			String c = InetAddress.getByName("localhost").getHostAddress();
			Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
			for (; n.hasMoreElements(); ) {
				NetworkInterface e = n.nextElement();
				Enumeration<InetAddress> a = e.getInetAddresses();
				for (; a.hasMoreElements(); ) {
					InetAddress addr = a.nextElement();
					String p = addr.getHostAddress();
					if (p.contains(".") && p.compareTo(c) != 0)
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

	// getters and setters
	public String getIpActivemq() {return IP_ACTIVEMQ;}
	public void setIpActivemq(String ip) {IP_ACTIVEMQ = ip;}
	public String getPortActivemq() {return PORT_ACTIVEMQ;}
	public void setPortActivemq(String port) {PORT_ACTIVEMQ = port;}
	public synchronized ConnectionNFieldsWithActiveMQAPI getConnection() {return conn;}
	public String getSimulationsDirectories() {return simulationsDirectories;}
	public synchronized Integer getSlotsNumber() {return slotsNumber;}
	public synchronized int setSlotsNumuber(int slots) {return this.slotsNumber = slots;}
	public HashMap<Integer, Simulation> getSimulationList() {return simulationList;}
}
