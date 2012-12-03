/**
 * Copyright 2012 UniversitÓ degli Studi di Salerno
 

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.jms.JMSException;

import org.apache.log4j.Logger;

import sim.display.Console;
import sim.display.GUIState;
import dmason.batch.data.EntryParam;
import dmason.batch.data.GeneralParam;
import dmason.sim.engine.DistributedMultiSchedule;
import dmason.sim.engine.DistributedState;
import dmason.sim.field.MessageListener;
import dmason.sim.field.TraceableField;
import dmason.util.connection.Address;
import dmason.util.connection.Connection;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;

public class Worker extends Observable
{
	//used for FTP
	private static final String DOWNLOADED_JAR_PATH = "TMP";
	private static final String SIMULATION_DIR = "simulation";
	private static final String TEST_PARAM_NAME = "Logs/workers/params.conf";
	private static String SEPARATOR;
	
	private Address address;
	private final ReentrantLock lock = new ReentrantLock();
	private final Condition block1 = lock.newCondition();
	private DistributedState state;
	private boolean gui;
	private boolean flag;
	private boolean step;
	private ConnectionNFieldsWithActiveMQAPI connection;
	private boolean run = true;
	private StartUpData data;
	private HashMap<String,MessageListener> table;
	private Console console;
	private volatile boolean resetted = false;
	
	private boolean isFirst = true;
	private boolean blocked = false;
	
	private long totSteps = 0;
	private Logger logger;
	private String stepTopic;
	
	
	public Worker(StartUpData data, Connection con)
	{
		super();
		this.data = data;
		connection = (ConnectionNFieldsWithActiveMQAPI)con;
		
		logger = Logger.getLogger(Worker.class.getCanonicalName());
		
		bootstrap();
		
		
	}
	
	public void bootstrap()
	{
		step = data.isStep();
		gui = data.isGraphic();
		address = connection.getAddress();
		
		System.out.println("Params :"+ data.getParam());
	
		totSteps = data.getParam().getMaxStep();
		
		
		System.out.println("Tot steps: "+totSteps);
		setSeparator();
		
		
		state = this.makeState(data.getDef(), data.getParam());
		
		
		// If this worker must publish to the "step" topic
		if (step)
		{
			if(data.getParam().isBatch)
				stepTopic = data.getTopicPrefix()+"step";
			else
				stepTopic = "step";
			
			connection.createTopic(stepTopic, 1);
			
			
			/*FileOutputStream paramFile;
			try {
				paramFile = new FileOutputStream(TEST_PARAM_NAME);
				PrintStream writer = new PrintStream(paramFile);
				writer.println(data.getParam()+"\n");
				if(data.getParam().isBatch)
					writer.println(data.getSimParam()+"\n");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			
			logger.debug(data.getParam()+"\n");
			if(data.getParam().isBatch)
				logger.debug(data.getSimParam()+"\n");
		    
			
		}
				
		if (!gui)
		{
			state.start();
		}
		
		
	}
	
	

	private static void setSeparator() 
	{
		OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		
		if(os.getName().contains("Windows"))
			SEPARATOR = "\\";
		if(os.getName().contains("Linux") || os.getName().contains("OS X"))
			SEPARATOR = "/";
	}
	
	
	public boolean isGUI()
	{
		return gui;
	}
	public Console getConsole()
	{
		return console;
	}
	
	//add
	public void kill()
	{
		if (gui)
		{   
			console.pressStop();	
			//console.dispose();
			//console.doClose();
			
			try {
				((DistributedState)state).closeConnection();
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		else
		{
			
			lock.lock();
			{
				resetted = true;
				run = false;
				flag = true;
				
			}
			lock.unlock();
			
			if(blocked)
				signal();
			
		}
		
		
		
	}
	
	public void oneStep()
	{
		if (gui)
		{
			console.pressPlay();
		}
		else
		{
			state.schedule.step(state);
		}
		if (step)
		{
			try
			{
				if (gui)
					connection.publishToTopic(console.simulation.state.schedule.getSteps(), "step","step");
				else
					connection.publishToTopic(state.schedule.getSteps()-1,"step","step"); 
						
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
	}
		 
	public void signal()
	{
		if (gui)
		{
			console.pressPause();
		}
		else
		{
			lock.lock();
			{
				flag = false;	
				block1.signal();
			}
			lock.unlock();
			
		}
	}
	
	public void await()
	{
		if (gui)
		{
			console.pressPause();
		}
		else
		{
			lock.lock();
			{
				flag = true;
			}
			lock.unlock();
			
			
		}
	}

	public synchronized void stop_play()
	{
		if (gui)
		{   
			//System.out.println("Called method stop_play");
			console.pressStop();	
			//console.dispose();
			//console.doClose();
			
			//System.out.println("Terminated method stop_play");
		}
		else
		{
			run = false;
			flag = true;
		}
	}

	public void _start() {
		if(!gui)
		{
			if(run)
				try {	
					while(!resetted)
					{
						while(!flag)
						{
							//System.out.println("step");
							if(step)
							{
								connection.publishToTopic(state.schedule.getSteps()-1,stepTopic,"step");
								
							}

							if(totSteps != 0)
							{
								if(state.schedule.getSteps()-1 == totSteps)
								{
									//System.out.println("step: "+(state.schedule.getSteps()-1));
									
									stop_play();
									// notify to PeerDeamonListener
									setChanged();
									notifyObservers();
									
									
								}
							}
							
							
							state.schedule.step(state);
							//worker.setStep(state.schedule.getSteps());
							
						}

						if(!resetted)
						{
							
							blocked  = true;
							lock.lock();
								block1.await();
							lock.unlock();
						
							blocked = false;

						}

					}
					closeConnection();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		else
			console.pressPause();
		
	}

	
	private void closeConnection() throws JMSException
	{
		((DistributedState)state).closeConnection();
	}

	
	/**
	 * Starts/stop simulation's parameter tracing
	 * @param param The parameter to start/stop tracing.
	 * @param trace true if we want to start tracing, false if we want to stop tracing.
	 */
	public void trace(String param, boolean trace)
	{
		TraceableField tf = (TraceableField)this.state.getField();
		if (trace)
			tf.trace(param);
		else
			tf.untrace(param);
	}

	/**
	 * Instantiate the simulation object.
	 * @param simClass The simulation class to instantiate.
	 * @param args_gen Parameters to be passed to simulation.
	 * @param args_mason Parameters to be passed to MASON engine.
	 * @return
	 */
	public DistributedState makeState(Class simClass, GeneralParam args_gen)
	{
		Object obj = null;
		
		if(simClass != null) //hardcoded simulation
		{
			Constructor constr;
			try {
				
				/*if(data.getSimParam() != null) //batch test simulation
				{
					constr = simClass.getConstructor(new Class[]{ args_sim.getClass(),List.class});
					obj = constr.newInstance(new Object[]{ args_sim ,data.getSimParam()});
				}*/
				//else //costruttore nel caso di sim non batch
				//{
					constr = simClass.getConstructor(new Class[]{ args_gen.getClass() });
					obj = constr.newInstance(new Object[]{ args_gen });
					
				//}
				
				//List<EntryParam<String, Object>> list = Arrays.asList(new EntryParam<String, Object>("width", 150), new EntryParam<String, Object>("height",500), new EntryParam<String, Object>("numFlockers",30), new EntryParam<String, Object>("cohesion",10.0), new EntryParam<String, Object>("avoidance",5.0), new EntryParam<String, Object>("randomness",2.0), new EntryParam<String, Object>("consistency",13.0), new EntryParam<String, Object>("momentum",11.0), new EntryParam<String, Object>("deadFlockerProbability",0.5), new EntryParam<String, Object>("neighborhood",40.0));
				
				/*simClass.getConstructors();
				for (Constructor c : simClass.getConstructors()) {
					System.out.println(c.getName()+" "+(c.getParameterTypes().length == 2 ? c.getParameterTypes()[1] : c.getParameterTypes()[0]));
				}*/
				
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		else
		{
			try
			{
				
				URL url = Updater.getSimulationJar(data);
				
			    obj = getSimulationInstance(args_gen, url,gui);
				
				
			} catch (Exception e) {
				throw new RuntimeException("Exception occurred while trying to construct the simulation " + simClass + "\n" + e);			
			}
		}
		
		
		if(obj instanceof DistributedState)
		{
			
			// The instantiated class is the proper simulation class
			return (DistributedState)obj;
		}
		else
		{
			
			// The instantiated class is a GUIState
			GUIState gui = (GUIState)obj;
			console = (Console)gui.createController();
			console.setVisible(true);
			console.pressPause();
			// Read as "get <state> variable from object <obj>"
			try {
				return (DistributedState)obj.getClass().getField("state").get(obj);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		 }
	
		return null;
	}

	private Object getSimulationInstance(GeneralParam args_gen, URL url, boolean isGui)
			throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException, ClassNotFoundException,
			InstantiationException 
			{
		JarClassLoader cl = new JarClassLoader(url);

		cl.addToClassPath();

		// String name = "dmason.sim.app.DAntsForage.DAntsForage";
		String name = null;
		try {
			name = cl.getMainClassName();
		} catch (IOException e) {
			System.err.println("I/O error while loading JAR file:");
			e.printStackTrace();
			System.exit(1);
		}
		if (name == null) {
			System.out.println("Specified jar file does not contain a 'Main-Class'" +
					" manifest attribute");
		}

		if(isGui)
		{

			name += "WithUI";
		}
		if(data.getParam().isBatch) //batch test simulation
		{
			
			return cl.getInstance(name, args_gen, data.getSimParam(), data.getTopicPrefix());
		}
		else
			return cl.getInstance(name, args_gen);
	
		
		
	}
	
	public ArrayList<MessageListener> getListeners()
	{
		return ((DistributedState)state).getLocalListener();
	}
	
	public void setTable(HashMap<String, MessageListener> table)
	{
		this.table = table;
		if(state.getField() == null)
			System.out.println("O_O");
		state.getField().setTable(this.table);
	}
}