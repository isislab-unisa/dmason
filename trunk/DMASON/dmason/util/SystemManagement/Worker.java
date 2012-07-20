package dmason.util.SystemManagement;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.jms.JMSException;

import sim.display.Console;
import sim.display.GUIState;
import dmason.util.SystemManagement.StartUpData;
import dmason.sim.engine.DistributedMultiSchedule;
import dmason.sim.engine.DistributedState;
import dmason.sim.field.MessageListener;
import dmason.sim.field.TraceableField;
import dmason.util.connection.Address;
import dmason.util.connection.Connection;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;

public class Worker
{
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
	
	public Worker(StartUpData data, Connection con)
	{
		super();
		this.data = data;
		connection = (ConnectionNFieldsWithActiveMQAPI)con;
		bootstrap();
	}
	
	public void bootstrap()
	{
		state = this.makeState(data.getDef(), data.getParam(), new String[]{});
		step = data.isStep();
		gui = data.isGraphic();
		address = connection.getAdress();
		
		// If this worker must publish to the "step" topic
		if (step)
		{
			connection.createTopic("step", 1);
		}
				
		if (!gui)
		{
			state.start();
		}
		
		
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
							if(step)
							{
								connection.publishToTopic(state.schedule.getSteps()-1,"step","step");
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
	 * @param args_sim Parameters to be passed to simulation.
	 * @param args_mason Parameters to be passed to MASON engine.
	 * @return
	 */
	public DistributedState makeState(Class simClass, Object[] args_sim, String[] args_mason)
	{
		try
		{
			//System.out.println("Class name: "+simClass.getName());
			
			Constructor constr = simClass.getConstructor(new Class[]{ args_sim.getClass() });
			Object obj = constr.newInstance(new Object[]{ args_sim });
			
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
				return (DistributedState)obj.getClass().getField("state").get(obj); 
			 }
		} catch (Exception e) {
			throw new RuntimeException("Exception occurred while trying to construct the simulation " + simClass + "\n" + e);			
		}
	}
	
	public ArrayList<MessageListener> getListeners()
	{
		return ((DistributedState)state).getLocalListener();
	}
	
	public void setTable(HashMap<String, MessageListener> table)
	{
		this.table = table;
		state.getField().setTable(this.table);
	}
}