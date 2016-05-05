/**
 * Copyright 2015 Universita' degli Studi di Salerno


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
package it.isislab.dmason.sim.app.SIRState;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
/*
 * THIS CLASS HAS BEEN USED FOR TESTING PURPOSES IN THE BEGINNINGS,
 */
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.util.connection.ConnectionType;

import java.util.ArrayList;

import sim.display.Console;

/**
 * 
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 *
 */
public class TestStart {

	private static boolean graphicsOn=false; //with or without graphics?
	private static int numSteps = 10000; 
	private static int rows = 1; //number of rows
	private static int columns = 2; //number of columns
	private static int AOI=10; //max distance
	private static int NUM_AGENTS=1000; //number of agents
	private static int WIDTH=400; //field width
	private static int HEIGHT=400; //field height
	private static String ip="127.0.0.1"; //ip of activemq
	private static String port="61616"; //port of activemq
	
	//don't modify this...
	private static int MODE = DistributedField2D.UNIFORM_PARTITIONING_MODE; 
	
	public static void main(String[] args) 
	{		System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES","*");
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
				
				GeneralParam genParam = new GeneralParam(WIDTH, HEIGHT, AOI, rows,columns,NUM_AGENTS, MODE,ConnectionType.pureActiveMQ); 
				genParam.setI(i);
				genParam.setJ(j);
				genParam.setIp(ip);
				genParam.setPort(port);
				if(graphicsOn || i==0 && j==0)
				{
					DPeopleWithGui sim =new DPeopleWithGui(genParam);
					((Console)sim.createController()).pressPause();
				}
				else
				{
					DPeople sim = new DPeople(genParam); 
					worker a = new worker(sim);
					myWorker.add(a);
				}
			}
		}
		//if(!graphicsOn)
			for (worker w : myWorker) {
				w.start();
			}
	}
}