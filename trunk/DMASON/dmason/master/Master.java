package dmason.master;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import dmason.util.SystemManagement.DigestAlgorithm;
import dmason.util.SystemManagement.Digester;
import dmason.util.SystemManagement.MasterDaemonStarter;
import dmason.util.SystemManagement.PeerStatusInfo;
import dmason.util.connection.ActiveMQManager;
import dmason.util.connection.Address;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;
import dmason.util.exception.NoDigestFoundException;

/**
 * The Master component of D-Mason. This class represents the 'Controller'
 * part of D-Mason Master's MVC architecture.
 * 
 * @author unascribed
 * @author Luca Vicidomini
 * @author Fabio Fulgido
 * @author Mario Fiore Vitale
 *
 */
public class Master extends Observable implements MasterListener 
{
	public final String ftpDir = "ftp-home";
	public final String simulationDir = "simulation";
	public final String updateDir = "update";
	private final String separator = System.getProperty("file.separator");
	
	ActiveMQManager activeMqManager = null;
	
	private String rmiHostname = null;
	private int rmiPort = 61617;
	
	private Address activeMqAddress = null;
	private String activeMqHostname = null;
	private String activeMqPort = null;
	
	private ConnectionNFieldsWithActiveMQAPI connection = null;
	private boolean isConnected = false;

	/**
	 * Daemon for ActiveMQ communication. Starts a new simulation.
	 */
	private MasterDaemonStarter masterDaemon = null;
	
	/**
	 * List of known workers.
	 */
	private WorkerInfoList workersInfo = new WorkerInfoList();

	/**
	 * Connects to ActiveMQ server.
	 * 
	 * @param address Address (IP or hostname) of ActiveMQ host
	 * @param port Port where ActiveMQ is listening
	 * @return true if connection was successful, false otherwise
	 */
	public boolean connect(String address, String port)
	{
		try
		{
			if (activeMqManager == null)
			{
				rmiHostname = address; 
				activeMqManager = new ActiveMQManager(rmiHostname, rmiPort);
				checkActiveMqManagerStatus();
				//setEnableActiveMQControl(true);
			}
			
			activeMqHostname = address;			    
			activeMqPort = port;  
			activeMqAddress = new Address(activeMqHostname, activeMqPort);
			
			connection = new ConnectionNFieldsWithActiveMQAPI();
			isConnected = connection.setupConnection(activeMqAddress);	
			
			masterDaemon = new MasterDaemonStarter(connection, this);
	
			if (!masterDaemon.connectToServer())
			{
				return false;
			}
	
			refreshWorkerList();
			//dont = true;
			
			return isConnected;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void workerInfo(PeerStatusInfo info)
	{
		workersInfo.put(info.getId(), info);
		this.setChanged();
		this.notifyObservers(workersInfo);
	}
	
	@Override
	public void incrementUpdatedWorker()
	{
		// TODO Auto-generated method stub
		
	}
	
	public void refreshWorkerList()
	{
		ArrayList<String> peers;
		try {
			workersInfo.invalidate();
			this.setChanged();
			this.notifyObservers(workersInfo);
			
			peers = masterDaemon.getTopicList();
			//workersInfo.clear();
			for (String p : peers)
			{
				masterDaemon.info(p);
			}
		} catch (Exception e) {
			// TODO Handle
			e.printStackTrace();
		}

	}
	
	private void checkActiveMqManagerStatus()
	{
		if (activeMqManager.isUnknow())
		{
			// TODO Handle
			log("ActiveMQ Wrapper NOT found at address " + rmiHostname + ":" + rmiPort + ", can't monitor ActiveMQ.");
			System.out.println("UNCATCHED BEHAVIOUR IN MASTER.JAVA isUnknow");
			//lblStatusIcon.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/status-unknow.png")));
		}
		else if (activeMqManager.isStarted())
		{
			// TODO Handle
			log("ActiveMQ Wrapper found at address " + rmiHostname + ":" + rmiPort + ". ActiveMQ is running.");
			System.out.println("UNCATCHED BEHAVIOUR IN MASTER.JAVA isStarted");
			//lblStatusIcon.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/status-up.png")));
		}
		else
		{
			// TODO Handle
			log("ActiveMQ Wrapper found at address " + rmiHostname + ":" + rmiPort + ". ActiveMQ is stopped.");
			System.out.println("UNCATCHED BEHAVIOUR IN MASTER.JAVA notStarted");
			//lblStatusIcon.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/status-down.png")));
		}
	}
	
	public boolean deployWorker(File updateFile)
	{
		File dest = new File(ftpDir+separator+updateDir+separator+updateFile.getName());
		
		try {
			FileUtils.copyFile(updateFile, dest);
			
			Digester dg = new Digester(DigestAlgorithm.MD5);
			
			InputStream in = new FileInputStream(dest);
			String curWorkerDigest = dg.getDigest(in);
			String workerJarName = updateFile.getName();
			
			String fileName = FilenameUtils.removeExtension(updateFile.getName());
			dg.storeToPropFile(ftpDir+separator+updateDir+separator+fileName+".hash");
			
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (NoDigestFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public void log(String message)
	{
		System.out.println(message);
	}
	
	public class WorkerInfoList extends HashMap<String, PeerStatusInfo>
	{
		private static final long serialVersionUID = 1735283314589915208L;
		
		public void invalidate()
		{
			for (Entry<String, PeerStatusInfo> entry : this.entrySet())
			{
				entry.getValue().setStatus("Not responding");
			}
		}
	}

}
