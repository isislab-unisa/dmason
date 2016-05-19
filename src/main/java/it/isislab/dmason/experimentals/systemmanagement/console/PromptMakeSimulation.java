package it.isislab.dmason.experimentals.systemmanagement.console;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

import it.isislab.dmason.experimentals.systemmanagement.Manager;
import it.isislab.dmason.experimentals.systemmanagement.master.MasterServer;
import it.isislab.dmason.experimentals.systemmanagement.utils.Simulation;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.util.connection.ConnectionType;

public enum PromptMakeSimulation implements Prompt {
	HELP(new Action(){
		public Object exec(Console c, String[] params, String stringPrompt, MasterServer ms) throws Exception {
			          
		c.printf(   "****************************** MAKE SIMULATION MODE ******************************");
		c.printf(   "*                                                                                *");
		c.printf(   "* COMMANDS                                                                       *");
		c.printf(   "*    uniform        | To make and submit a simulation with uniform partitioning  *");
		c.printf(   "*    nonuniform     | To make and submit a simulation with uniform partitioning  *");
		c.printf(   "*    exit           | To go back at previously section                           *");
		c.printf(   "*                                                                                *");
		c.printf(   "************************* Simulation Field Partitioning **************************");
		c.printf(
					"*     .................................    .................................     * \n"+
					"*     :               :               :    :               :       :       :     * \n"+
					"*     :               :               :    :...............:       :       :     * \n"+
					"*     :               :               :    :       :       :       :       :     * \n"+
					"*     :               :               :    :       :       :       :       :     * \n"+
					"*     :               :               :    :       :       :       :       :     * \n"+
					"*     :               :               :    :       :       :       :       :     * \n"+
					"*     :...............:...............:    :       :       :.......:.......:     * \n"+
					"*     :               :               :    :       :       :               :     * \n"+
					"*     :               :    uniform    :    :       :       :...............:     * \n"+
					"*     :               :  partitioning :    :       :       :               :     * \n"+
					"*     :               :               :    :.......:.......:  non uniform  :     * \n"+
					"*     :               :               :    :               :  partitioning :     * \n"+
					"*     :...............:...............:    :...............:...............:     *   ");
		c.printf(   "*                                                                                *   ");
		c.printf(   "**********************************************************************************\n");
			return null;
		}
	}),

	UNIFORM(new Action(){
		public Object exec(Console c, String[] params, String stringPrompt, MasterServer ms) throws Exception {

			if(params==null){
				c.printf("too few arguments\n");
				c.printf("USAGE: uniform [arguments.....]");
				c.printf("       Arguments");
				c.printf("               simName     (String)  simulation name");
				c.printf("               jarFile     (String)  a jar pathname");
				c.printf("               rows        (Numeric) the # of rows for partitioning");
				c.printf("               columns     (Numeric) the # of columns for partitioning");
				c.printf("               aoi         (Numeric) the Area Of Interest ");
				c.printf("               width       (Numeric) the field width ");
				c.printf("               height      (Numeric) the field height ");
				c.printf("               #agent      (Numeric) the # of agents ");
				c.printf("               #step       (Numeric) the # of steps ");
				c.printf("               connection  (String)  the intraworker layer comunication  (activemq or mpi)");
				return null;
			}

			if(params.length < 9 || params.length>10){
				if(params.length>10)
					c.printf("too many arguments\n");
				else
					c.printf("too few arguments\n");

				c.printf("USAGE: uniform [arguments.....]");
				c.printf("       Arguments");
				c.printf("               simName     (String)  simulation name");
				c.printf("               jarFile     (String)  a jar pathname");
				c.printf("               rows        (Numeric) the # of rows for partitioning");
				c.printf("               columns     (Numeric) the # of columns for partitioning");
				c.printf("               aoi         (Numeric) the Area Of Interest ");
				c.printf("               width       (Numeric) the field width ");
				c.printf("               height      (Numeric) the field height ");
				c.printf("               #agent      (Numeric) the # of agents ");
				c.printf("               #step       (Numeric) the # of steps ");
				c.printf("               connection  (String)  the intraworker layer comunication  (activemq or mpi)");
				return null;
			}
			try{
				String simName = params[0];
				String pathSimJar = params[1];
				int rows = Integer.parseInt(params[2]);
				int columns = Integer.parseInt(params[3]);
				int aoi = Integer.parseInt(params[4]);
				int width = Integer.parseInt(params[5]);
				int height = Integer.parseInt(params[6]);
				int numAgent = Integer.parseInt(params[7]);
				int numStep = Integer.parseInt(params[8]);
				String connType=(params.length==10)?params[9]:"";
				int mode=DistributedField2D.UNIFORM_PARTITIONING_MODE;
				int connection = Integer.MIN_VALUE;

				/*
				switch(params.length){
				case 10: String xParam=params[9];
				switch(xParam){
				case "uniform": partitioning=xParam; break;
				case "nonuniform": partitioning=xParam; break;
				case "activemq": connType=xParam; break;
				case "mpi": connType=xParam; break;
				}
				break; 
				case 11: partitioning = params[9]; connType=params[10]; break;
				}*/

				/*switch(partitioning.toLowerCase()){
				case "uniform": mode =DistributedField2D.UNIFORM_PARTITIONING_MODE; break;
				case "nonuniform": mode =DistributedField2D.NON_UNIFORM_PARTITIONING_MODE; break;
				default: mode =DistributedField2D.UNIFORM_PARTITIONING_MODE; break;
				}*/

				switch(connType.toLowerCase()){
					case "activemq": connection =ConnectionType.pureActiveMQ; break;
					case "mpi": connection =ConnectionType.pureMPIParallel; break;
					default: connection =ConnectionType.pureActiveMQ; break;
				}


				ArrayList<String> allworkers = new ArrayList<String>();
				allworkers.addAll(ms.getInfoWorkers().keySet());

				if(allworkers.size() == 0){ c.printf("No worker available"); return null;}

				ArrayList<String> subWorkersList = new ArrayList<String>();


				File f = new File(pathSimJar);
				if(!f.exists()){
					c.printf("File: "+pathSimJar+" doesn't exist");
					return null;
				}

				int simId=ms.getKeySim().incrementAndGet();
				String simPath=ms.getSimulationsDirectories()+File.separator+simName+simId;
				ms.createSimulationDirectoryByID(simName, simId);

				String jarSimName=f.getName();
				String internalSimPathJar=simPath+File.separator+"jar";

				FileUtils.copyFileToDirectory(f, new File(internalSimPathJar));

				
				int numCell = rows * columns;
				if(numCell < allworkers.size()){
					subWorkersList.addAll(allworkers.subList(0,numCell));
				}else{
					subWorkersList = allworkers;
				}
				/*
				switch(mode){
				case DistributedField2D.UNIFORM_PARTITIONING_MODE:
					int numCell = rows * columns;
					if(numCell < allworkers.size()){
						subWorkersList.addAll(allworkers.subList(0,numCell));
					}else{
						subWorkersList = allworkers;
					}
					break;
					case DistributedField2D.NON_UNIFORM_PARTITIONING_MODE:
					if(cells < topics.length){
						for (int i = 0; i < cells; i++) {
							topicList.add(topics[i]);
						}
					}else{
						for(String x: topics) 
							topicList.add(x);
					}

					break;
				}*/


				Simulation s = new Simulation(simName, simPath,internalSimPathJar+File.separator+jarSimName,rows, columns, aoi, width, height, numAgent, numStep, mode, connection);

				s.setTopicList(subWorkersList);
				s.setNumWorkers(subWorkersList.size());
				s.setSimID(simId);
				s.setTopicPrefix(simName+"-"+simId);

				if(ms.submitSimulation(s)){
					c.printf("Simulation has been created with ID "+s.getSimID());
				}else{
					c.printf("Several error while simulation is creating");
					return null;
				}

			}catch(Exception e){
				c.printf("Some errors were occurred\n");
				c.printf(e.getMessage()+"\n");
				c.printf("USAGE: uniform [arguments.....]");
				c.printf("       Arguments");
				c.printf("               simName       (String)  simulation name");
				c.printf("               jarFile       (String)  a jar pathname");
				c.printf("               rows          (Numeric) the # of rows for partitioning");
				c.printf("               columns       (Numeric) the # of columns for partitioning");
				c.printf("               aoi           (Numeric) the Area Of Interest ");
				c.printf("               width         (Numeric) the field width ");
				c.printf("               height        (Numeric) the field height ");
				c.printf("               #agent        (Numeric) the # of agents ");
				c.printf("               #step         (Numeric) the # of steps ");
				c.printf("               [connection]  (String optional)  the intraworker layer comunication  (activemq or mpi). Default value: activemq");
				return null;
			}
			return null;
		}
	}),
	NONUNIFORM(new Action(){
		public Object exec(Console c, String[] params, String stringPrompt, MasterServer ms) throws Exception {
			return null;
		}
	}),
	
	EXIT(new Action(){
		public Object exec(Console c, String[] params, String stringPrompt, MasterServer ms) throws Exception {
			return null;
		}
	});
	private Action action=null;
	private PromptMakeSimulation(Action a) { action = a;}



	@Override
	public Object exec(Console c, String[] params, String stringPrompt, MasterServer ms, PromptListener l) {
		try
		{
			return action.exec(c, params,stringPrompt,ms);
		}
		catch (Exception e)
		{
			l.exception(e);
			return null;
		}
	}

}

