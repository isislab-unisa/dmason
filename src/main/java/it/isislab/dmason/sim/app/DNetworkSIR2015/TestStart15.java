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
package it.isislab.dmason.sim.app.DNetworkSIR2015;
/*
 * THIS CLASS HAS BEEN USED FOR TESTING PURPOSES IN THE BEGINNINGS,
 */
import it.isislab.dmason.annotation.AuthorAnnotation;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.continuous.DContinuousGrid2DFactory;
import it.isislab.dmason.tools.batch.data.GeneralParam;
import it.isislab.dmason.util.connection.ConnectionType;

import java.util.ArrayList;

import sim.display.Console;

@AuthorAnnotation(
		author = {"Ada Mancuso","Francesco Milone","Carmine Spagnuolo"},
		date = "7/3/2014"
		)
public class TestStart15 {

	private static boolean graphicsOn=false; //with or without graphics?
	private static int numSteps = 100; //only graphicsOn=false
	private static int rows = 1; //number of rows
	private static int columns = 4; //number of columns
	private static int MAX_DISTANCE=1; //max distance
	private static int NUM_AGENTS=0; //number of agents
	private static int WIDTH=0; //field width
	private static int HEIGHT=0; //field height
	private static String ip="127.0.0.1"; //ip of activemq
	private static String port="61616"; //port of activemq

	//don't modify this...
	private static int MODE = (rows==1 || columns==1)? DistributedField2D.HORIZONTAL_DISTRIBUTION_MODE : DistributedField2D.SQUARE_DISTRIBUTION_MODE; 

	public static void main(String[] args) 
	{		
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
				while(true)
									{
										System.out.println(i);
					ds.schedule.step(ds);
									i++;
								}
			}
		}

		ArrayList<worker> myWorker = new ArrayList<worker>();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {

				GeneralParam genParam = new GeneralParam(WIDTH, HEIGHT, MAX_DISTANCE, rows,columns,NUM_AGENTS, MODE,ConnectionType.pureActiveMQ); 
				genParam.setI(i);
				genParam.setJ(j);
				genParam.setIp(ip);
				genParam.setPort(port);
				if((i==0 && j==0))
				{
					VertexesWithUI15 sim =new VertexesWithUI15(genParam,"resources/app/DNetworkSIR2015/uk_network/uk.graph",
							"resources/app/DNetworkSIR2015/uk_network/4/Metis/uk.graph.part.4");
					((Console)sim.createController()).pressPause();
				}
				else
				{
					Vertexes15 sim = new Vertexes15(genParam,"resources/app/DNetworkSIR2015/uk_network/uk.graph",
							"resources/app/DNetworkSIR2015/uk_network/4/Metis/uk.graph.part.4");
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