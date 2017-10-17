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
package it.isislab.dmason.sim.app.GameOfLife;

import java.util.ArrayList;

import it.isislab.dmason.experimentals.systemmanagement.utils.activemq.ActiveMQStarter;
import it.isislab.dmason.experimentals.tools.batch.data.EntryParam;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.util.connection.ConnectionType;
import sim.display.Console;

/**
 * 
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 *
 */
public class TestDGameOfLife {
	private static boolean graphicsOn=false; //with or without graphics?
	private static int numSteps = 100; //only graphicsOn=false
	private static int rows = 4; //number of rows
	private static int columns = 8; //number of columns
	private static int AOI=1; //max distance
	private static int NUM_AGENTS=2; //number of agents
	private static int WIDTH=3200; //field width
	private static int HEIGHT=3200; //field height
	private static int CONNECTION_TYPE=ConnectionType.pureActiveMQ;
	private static String ip="127.0.0.1"; //ip of activemq
	private static String port="61616"; //port of activemq
	private static String topicPrefix="life"; //unique string to identify topics for this simulation
	
	 
	private static int MODE = DistributedField2D.UNIFORM_PARTITIONING_MODE;
	
	
	public static void main(String[] args) 
	{	ActiveMQStarter s=new ActiveMQStarter();
	    s.startActivemq();
		System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES","*");	
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
					DGameOfLife sim = new DGameOfLife(genParam,topicPrefix); 
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