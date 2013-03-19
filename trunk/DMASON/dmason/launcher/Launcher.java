package dmason.launcher;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import dmason.launcher.ui.Wizard;

/**
 * 
 * @author Luca Vicidomini
 *
 */
public class Launcher
{
	private static final int MIN_WORKERS = 1;
	private static final int SUGGESTED_WORKERS = 2;
	private static final int MAX_WORKERS = 8;
	
	private static final Class<?> WRAPPER_CLASS = dmason.wrapper.activemq.launcher.ActiveMQWrapper.class;
	private static final Class<?> WORKER_CLASS = dmason.util.SystemManagement.StartWorkerWithGui.class;
	private static final Class<?> MASTER_CLASS = dmason.util.SystemManagement.JMasterUI.class;
	private static final Class<?> INSPECTOR_CLASS = dmason.util.visualization.LauncherViewer.class;
	
	Process wrapperProcess = null;
	ArrayList<Process> workerProcesses = new ArrayList<Process>();
	Process masterProcess = null;
	Process inspectorProcess = null;
	
	HashMap<String, Class<?>> classList;
	int numCores;
	
	public Launcher()
	{
		classList = new LinkedHashMap<String, Class<?>>();
		classList.put("server", WRAPPER_CLASS);
		classList.put("worker", WORKER_CLASS);
		classList.put("master", MASTER_CLASS);
		classList.put("inspector", INSPECTOR_CLASS);
		
		numCores = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
	}
	
	public void easyMode()
	{
		/* Launch wrapper */
		launchWrapper();
		
		/* Launch workers */
		int nWorkers = getSuggestedWorkers();
		
		System.out.println(numCores + " CPU/Cores detected. D-Mason chose to launch " + nWorkers + " worker threads.");
		
		for (int i = 0; i < nWorkers; i++)
		{
			launchWorker();
		}
		
		/* Launch master */
		launchMaster();
	}
	
	public static void main(String[] args)
	{
		Launcher launcher = new Launcher();
		
		Runtime.getRuntime().addShutdownHook(launcher.new ShutdownListener());
		
		Wizard wizard = new Wizard(launcher);
		wizard.setVisible(true);
	}
	
	public static void main2(String[] args)
	{
		Launcher launcher = new Launcher();
		
		String what = "";
				
		if (args.length >= 1)
		{
			what = args[0].toLowerCase();
		}
		
		// args2 will contain args entries except the first one
		String[] args2 = new String[args.length - 1];
		System.arraycopy(args, 1, args2, 0, args2.length);
		
		boolean result = launcher.launch(what, args2);
		
		if (!result)
		{
			String executableName = new java.io.File(Launcher.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName() + ".jar";
			
			System.out.println("Please specify the component you want to launch:");

			for (String component : launcher.classList.keySet())
			{
				System.out.println("  java -jar " + executableName + " " + component);
			}
			
			System.out.println();
			System.out.println();
			System.out.println("Or choose the easy mode if you want to try D-Mason on a single machine:");
			System.out.println("  java -jar " + executableName + " easymode");
			
			System.out.println();
			System.out.println();
			System.out.println("Visit http://dmason.org for more information.");
			
			System.exit(1);
		}
		
		
	}
	
	public boolean launch(String component, String[] args)
	{
		Class<?> executableClass = null;
		executableClass = classList.get(component);
		
		if (component.equals("easymode"))
		{
			easyMode();
			return true;
		}
		else if (executableClass != null)
		{
			try {			
				// Call main method of selected component
				Forker forker = new Forker(executableClass);
				forker.launch();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public void launchWrapper()
	{
		Forker wrapperFork = new Forker(WRAPPER_CLASS);
		wrapperProcess = wrapperFork.launch();
	}
	
	public void launchWorker()
	{
		Forker workerFork = new Forker(WORKER_CLASS);
		workerFork.add("127.0.0.1");
		workerFork.add("61616");
		workerFork.add("autoconnect");
		workerProcesses.add( workerFork.launch() );
	}
	
	public void launchMaster()
	{
		Forker masterFork = new Forker(MASTER_CLASS);
		masterFork.add("127.0.0.1");
		masterFork.add("autoconnect");
		masterProcess = masterFork.launch();
	}
	
	public void launchInspector()
	{
		Forker workerFork = new Forker(INSPECTOR_CLASS);
		workerFork.add("127.0.0.1");
		workerFork.add("61616");
		workerFork.add("autoconnect");
		inspectorProcess = workerFork.launch();
	}
	
	public int getNumCores()
	{
		return numCores;
	}
	
	public int getMinWorkers()
	{
		return MIN_WORKERS;
	}
	
	public int getMaxWorkers()
	{
		return MAX_WORKERS;
	}
	
	public int getSuggestedWorkers()
	{
		/*
		 * SUGGESTED_WORKERS is the ideal number of workers we would like to
		 * run. We compare SUGGESTED_WORKERS with the number of available cores
		 * and return the minimum between SUGGESTER_WORKERS and (NumCores - 1),
		 * but also we ensure to return at least "1".  
		 */
		int suggestion = getNumCores() > 2
			? getNumCores() - 1
			: 1;

		return suggestion < SUGGESTED_WORKERS
			? suggestion
			: SUGGESTED_WORKERS;
	}
	
	/**
	 * Executes the run() method when the user closes Launcher.
	 * @author Luca Vicidomini
	 *
	 */
	class ShutdownListener extends Thread {		
		@Override
		public void run()
		{
			super.run();
			
			if (wrapperProcess != null)
				wrapperProcess.destroy();
		}
	}

	public void stopAllProcesses()
	{
		/* Please note: wrapperProcvess is always destroyed when Launcher exits. */
		
		for (Process workerProcess : workerProcesses)
			workerProcess.destroy();
		
		if (masterProcess != null)
			masterProcess.destroy();
		
		if (inspectorProcess != null)
			inspectorProcess.destroy();
		
	}
}

		
