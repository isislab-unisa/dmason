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
package it.isislab.dmason.sim.app.DParticles;
/*
 * THIS CLASS HAS BEEN USED FOR TESTING PURPOSES IN THE BEGINNINGS,
 */

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
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 *
 */
public class TestDParticles {

	private static int numSteps = 300000; //only graphicsOn=false
    private static int rows = 4; //number of rows
    private static int columns = 4; //number of columns
    private static int AOI=10; //max distance
    private static int NUM_AGENTS=500; //number of agents
    private static int WIDTH=1200; //field width
    private static int HEIGHT=1200; //field height
    private static String ip="127.0.0.1"; //ip of activemq
    private static String port="61616"; //port of activemq
    private static String topicPrefix="SIM-NAME"; //unique string to identify topics for this simulation 
    private static int MODE = DistributedField2D.UNIFORM_PARTITIONING_MODE;
   
    
    public static void main(String[] args) {
        
    	/***START EMBEDDED ACTIVEMQ ****/
    	ActiveMQStarter v=new ActiveMQStarter();
        v.startActivemq();
        /******************************/
        
        
        
    	System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES","*");
    	class worker extends Thread {
            		private DistributedState ds;
            		public worker(DistributedState ds) {
            			this.ds=ds;
            			ds.start();
            		}
            		public void run() {
            			int i=0;
            			while(i!=numSteps)
            			{
            				ds.schedule.step(ds);
            				i++;
            			}
            			System.exit(0);
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
        			ArrayList<EntryParam<String, Object>> simParams=new ArrayList<EntryParam<String, Object>>();
        			
        			if(i==0 && j==0)
    				{
    					DParticlesWithUI sim =new DParticlesWithUI(genParam,simParams,topicPrefix);
    					((Console)sim.createController()).pressPause();
    				}else {
    					DParticles sim = new DParticles(genParam,simParams,topicPrefix); 
            			worker a = new worker(sim);
            			myWorker.add(a);
    				}
        			
        			
        			
        		}
        	}
        		for (worker w : myWorker) {
        			w.start();
        		}
        	}
}