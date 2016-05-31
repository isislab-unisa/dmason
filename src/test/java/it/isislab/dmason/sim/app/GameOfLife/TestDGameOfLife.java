package it.isislab.dmason.sim.app.GameOfLife;

import java.util.ArrayList;

import it.isislab.dmason.experimentals.tools.batch.data.EntryParam;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.util.connection.ConnectionType;
import sim.display.Console;

public class TestDGameOfLife {
	private static boolean graphicsOn=true; //with or without graphics?
	private static int numSteps = 100; //only graphicsOn=false
	private static int rows = 2; //number of rows
	private static int columns = 1; //number of columns
	private static int AOI=1; //max distance
	private static int NUM_AGENTS=20; //number of agents
	private static int WIDTH=200; //field width
	private static int HEIGHT=200; //field height
	private static int CONNECTION_TYPE=ConnectionType.pureActiveMQ;
	private static String ip="127.0.0.1"; //ip of activemq
	private static String port="61616"; //port of activemq
	private static String topicPrefix="life"; //unique string to identify topics for this simulation
	
	 
	private static int MODE = DistributedField2D.UNIFORM_PARTITIONING_MODE;
	
	
	public static void main(String[] args) 
	{	System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES","*");	
		class worker extends Thread
		{
			private DistributedState ds;
			public worker(DistributedState ds) {
				this.ds=ds;
				ds.start();
			}
			@Override
			public void run() {
				int i=0;
				while(i!=numSteps)
				{
					//System.out.println(i);
					ds.schedule.step(ds);
					i++;
				}
			}
		}

		ArrayList<worker> myWorker = new ArrayList<worker>();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				
				GeneralParam genParam = new GeneralParam(WIDTH, HEIGHT, AOI, rows,columns,NUM_AGENTS, MODE, CONNECTION_TYPE); 
				genParam.setI(i);
				genParam.setJ(j);
				genParam.setIp(ip);
				genParam.setPort(port);
				ArrayList<EntryParam<String, Object>> simParams=new ArrayList<EntryParam<String, Object>>();
				if(graphicsOn  || i==0 && j==0)
				{
					DGameOfLifeWithUI sim =new DGameOfLifeWithUI(genParam,simParams,topicPrefix);
					((Console)sim.createController()).pressPause();
				}
				else
				{
					DGameOfLife sim = new DGameOfLife(genParam,simParams,topicPrefix); 
					worker a = new worker(sim);
					myWorker.add(a);
				}
			}
		}
		if(!graphicsOn)
			for (worker w : myWorker) {
				w.start();
			}
	}
}