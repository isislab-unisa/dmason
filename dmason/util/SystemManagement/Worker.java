package dmason.util.SystemManagement;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import sim.display.Console;
import sim.display.GUIState;
import dmason.util.SystemManagement.StartUpData;
import dmason.sim.engine.DistributedState;
import dmason.sim.field.MessageListener;
import dmason.util.connection.Address;
import dmason.util.connection.Connection;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;

public class Worker{

	private Address address;
	private final ReentrantLock lock=new ReentrantLock();
	private final Condition block1=lock.newCondition();
	private DistributedState state;
	private boolean gui;
	private boolean flag;
	private boolean step;
	private ConnectionNFieldsWithActiveMQAPI connection;
	private boolean RUN=true;
	private StartUpData data;
	private HashMap<String,MessageListener> table;
	private Console console;
	
	public Worker(StartUpData data,Connection con){
		super();
		this.data = data;
		connection = (ConnectionNFieldsWithActiveMQAPI)con;
		bootstrap();
	}
	
	public void bootstrap() {
			state= this.makeState(data.getDef(),data.getParam(),new String[]{});
			step = data.isStep();
			gui = data.isGraphic();
			address = connection.getAdress();
			if(step)
			{
				try{
					connection.createTopic("step",1);
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			if(!gui)
				state.start();
	}
	
	public void oneStep(){
		if(gui)
			console.pressPlay();
		else
		{
				state.schedule.step(state);
		 }
		if(step){
			try{
				if(gui)
					connection.publishToTopic(console.simulation.state.schedule.getSteps(), "step","step");
				else
					connection.publishToTopic(state.schedule.getSteps()-1,"step","step");
			}catch (Exception e) {
				e.printStackTrace();
			}
		   }	
		 }
		 
	public void signal(){
		if(gui)
			console.pressPause();
		else
		{
			lock.lock();
				flag=false;	
				block1.signal();
			lock.unlock();
		}
	}
	
	public void await(){
		if(gui)
			console.pressPause();
		else
		{
			lock.lock();

				flag=true;

			lock.unlock();
		}
	}

	public synchronized void stop_play(){
		if(gui)
		{
			console.pressStop();
			console.dispose();
		}
		else
		{
			RUN=false;
			flag = true;
			}
	}

	public void _start() {
		if(!gui)
		{
			if(RUN)
				try {	
					while(true)
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
					
									lock.lock();
										block1.await();
									lock.unlock();
							}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		else
			console.pressPause();
			}

	public DistributedState makeState( Class c, Object[] args_sim, String[] args_mason)
	{
		try {

			Constructor cc=c.getConstructor(new Class[]{args_sim.getClass()});
			 Object obj=cc.newInstance(new Object[]{args_sim});
			 
			 if(obj instanceof DistributedState)
			 {
				 return (DistributedState)obj;
			 }
			 else
			 {
				 GUIState gui=(GUIState)obj;
				 console=(Console)gui.createController();
				 console.setVisible(true);
				 console.pressPause();
				 return (DistributedState)obj.getClass().getField("state").get(obj);
				 
			 }
		} catch (Exception e) {
			// TODO Auto-generated catch block

			throw new RuntimeException("Exception occurred while trying to construct the simulation " + c + "\n" + e);
			
		}
	}
	
	public ArrayList<MessageListener> getListeners()
	{
		return ((DistributedState)state).getLocalListener();
	}
	
	public void setTable(HashMap<String,MessageListener> table)
	{
		this.table = table;
		state.getField().setTable(this.table);
	}
}