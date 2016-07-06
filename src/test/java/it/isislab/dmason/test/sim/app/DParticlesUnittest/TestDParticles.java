/**
 * Copyright 2012 Universita' degli Studi di Salerno


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
package it.isislab.dmason.test.sim.app.DParticlesUnittest;
/*
 * THIS CLASS HAS BEEN USED FOR TESTING PURPOSES IN THE BEGINNINGS,
 */
import static org.junit.Assert.assertEquals;
import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.support.field2D.EntryAgent;
import it.isislab.dmason.test.sim.app.DAntsForageUnitTest.DRemoteAnt;
import it.isislab.dmason.test.support.AgentComparator;
import it.isislab.dmason.util.connection.ConnectionType;
import sim.util.Int2D;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

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

	

	private static int numSteps; //only graphicsOn=false
	private static int rows; //number of rows
	private static int columns; //number of columns
	private static int MAX_DISTANCE; //max distance
	private static int NUM_AGENTS; //number of agents
	private static int WIDTH; //field width
	private static int HEIGHT; //field height
	private static String ip; //ip of activemq
	private static String port; //port of activemq

	private static int MODE = DistributedField2D.UNIFORM_PARTITIONING_MODE;
	private static final String PREFIX="particletest";// simulation unique topic identifier

	ArrayList<DParticle> initial_agents = new ArrayList<DParticle>();
	ArrayList<DParticle> end_agents = new ArrayList<DParticle>();
	
	class worker extends Thread
	{

		private DParticles ds;
		public worker(DParticles ds) {
			this.ds=ds;
			ds.start();


		}
		@Override
		public void run() {
			int i=0;
			while(i!=numSteps)
			{
				//	System.out.println(i);
				ds.schedule.step(ds);
				i++;
             if(i==1) 
				synchronized (initial_agents) {
					for(EntryAgent<Int2D> d:ds.particles.myfield.values())
					{
						DParticle df=(DParticle) d.r;
						
						
						if(ds.particles.verifyPosition(df.getPos()))
							initial_agents.add(df);

					}

				}
             else if(i==numSteps-1)
            	 synchronized (end_agents) {
 					for(EntryAgent<Int2D> d:ds.particles.myfield.values())
 					{
 						DParticle df=(DParticle) d.r;
 	
 						if(ds.particles.verifyPosition(df.getPos()))
 							end_agents.add(df);

 					}
 				} 
			}
			
		}
	}
	
	@Before
	public void setUp(){
        
		numSteps = 1000; //only graphicsOn=false  
		rows = 2; //number of rows                
		columns = 2; //number of columns          
		MAX_DISTANCE=1; //max distance            
		NUM_AGENTS=4000; //number of agents       
		WIDTH=300; //field width                  
		HEIGHT=300; //field height                
		ip="127.0.0.1"; //ip of activemq       
		port="61616"; //port of activemq       
	}
	
		
	private void startWorkers(){
		ArrayList<worker> myWorker = new ArrayList<worker>();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {

				GeneralParam genParam = new GeneralParam(WIDTH, HEIGHT, MAX_DISTANCE, rows,columns,NUM_AGENTS, MODE,ConnectionType.fakeUnitTestJMS); 
				genParam.setI(i);
				genParam.setJ(j);
				genParam.setIp(ip);
				genParam.setPort(port);

				DParticles sim = new DParticles(genParam,PREFIX); 
				worker a = new worker(sim);
				myWorker.add(a);

			}
		}

		for (worker w : myWorker) {
			w.start();
		}
		for (worker w : myWorker) {
			try {
				w.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Test
	public void testSimulationReproducibility() throws DMasonException, InterruptedException {

		startWorkers();

		AgentComparator c=new AgentComparator();

		assertEquals(initial_agents.size(), end_agents.size());
		
		Collections.sort(end_agents,c);
		ArrayList<DRemoteAnt> firstExec = new ArrayList(end_agents);
		
		setUp();
		startWorkers();
		Collections.sort(end_agents,c);
		
		for(int i=0;i<end_agents.size();i++){
			assertEquals(firstExec.get(i).pos.x, end_agents.get(i).pos.x);
			assertEquals(firstExec.get(i).pos.y, end_agents.get(i).pos.y);
		}

	}
}