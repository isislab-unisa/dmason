package it.isislab.dmason.experimentals.systemmanagement.console;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
				long numStep = new Long(params[8]);
				String connType=(params.length==10)?params[9]:"";
				int mode=DistributedField2D.UNIFORM_PARTITIONING_MODE;
				int connection = Integer.MIN_VALUE;

				switch(connType.toLowerCase()){
				case "activemq": connection =ConnectionType.pureActiveMQ; break;
				case "mpi": connection =ConnectionType.pureMPIParallel; break;
				default: connection =ConnectionType.pureActiveMQ; break;
				}


				ArrayList<String> allworkers = new ArrayList<String>();
				allworkers.addAll(ms.getInfoWorkers().keySet());

				if(allworkers.size() == 0){ c.printf("No worker available"); return null;}

				ArrayList<String> subWorkersList = new ArrayList<String>();


				int numCell = rows * columns;
				if(numCell < allworkers.size()){
					subWorkersList.addAll(allworkers.subList(0,numCell));
				}else{
					subWorkersList = allworkers;
				}

				if(!checkSlots(ms,subWorkersList,numCell)){ c.printf("No slots available");return null;}

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
				c.printf("Some errors were occurred");
				c.printf(e.getMessage());
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
				e.printStackTrace();
				return null;
			}
			return null;
		}
	}),
	NONUNIFORM(new Action(){
		public Object exec(Console c, String[] params, String stringPrompt, MasterServer ms) throws Exception {

			if(params==null){
				c.printf("too few arguments\n");
				c.printf("USAGE: uniform [arguments.....]");
				c.printf("       Arguments");
				c.printf("               simName     (String)  simulation name");
				c.printf("               jarFile     (String)  a jar pathname");
				c.printf("               cells       (Numeric) the # of cells for partitioning");
				c.printf("               aoi         (Numeric) the Area Of Interest ");
				c.printf("               width       (Numeric) the field width ");
				c.printf("               height      (Numeric) the field height ");
				c.printf("               #agent      (Numeric) the # of agents ");
				c.printf("               #step       (Numeric) the # of steps ");
				c.printf("               connection  (String)  the intraworker layer comunication  (activemq or mpi)");
				return null;
			}

			if(params.length < 8 || params.length>9){
				if(params.length>10)
					c.printf("too many arguments\n");
				else
					c.printf("too few arguments\n");

				c.printf("USAGE: uniform [arguments.....]");
				c.printf("       Arguments");
				c.printf("               simName     (String)  simulation name");
				c.printf("               jarFile     (String)  a jar pathname");
				c.printf("               cells       (Numeric) the # of cells for partitioning");
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
				int cells = Integer.parseInt(params[2]);
				int aoi = Integer.parseInt(params[3]);
				int width = Integer.parseInt(params[4]);
				int height = Integer.parseInt(params[5]);
				int numAgent = Integer.parseInt(params[6]);
				long numStep = new Long(params[7]);
				String connType=(params.length==9)?params[8]:"";
				int mode=DistributedField2D.NON_UNIFORM_PARTITIONING_MODE;
				int connection = Integer.MIN_VALUE;

				switch(connType.toLowerCase()){
				case "activemq": connection =ConnectionType.pureActiveMQ; break;
				case "mpi": connection =ConnectionType.pureMPIParallel; break;
				default: connection =ConnectionType.pureActiveMQ; break;
				}


				ArrayList<String> allworkers = new ArrayList<String>();
				allworkers.addAll(ms.getInfoWorkers().keySet());

				if(allworkers.size() == 0){ c.printf("No worker available"); return null;}

				ArrayList<String> subWorkersList = new ArrayList<String>();

				if(cells < allworkers.size()){
					subWorkersList.addAll(allworkers.subList(0,cells));
				}else{
					subWorkersList = allworkers;
				}
				if(!checkSlots(ms,subWorkersList,cells)){ c.printf("No slots available");return null;}

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
				Simulation s = new Simulation(simName, simPath, internalSimPathJar+File.separator+jarSimName, cells, aoi, width, height, numAgent, numStep, mode, connection);

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
				c.printf("               cells         (Numeric) the # of cells for partitioning");
				c.printf("               aoi           (Numeric) the Area Of Interest ");
				c.printf("               width         (Numeric) the field width ");
				c.printf("               height        (Numeric) the field height ");
				c.printf("               #agent        (Numeric) the # of agents ");
				c.printf("               #step         (Numeric) the # of steps ");
				c.printf("               [connection]  (String optional)  the intraworker layer comunication  (activemq or mpi). Default value: activemq");
				e.printStackTrace();
				return null;
			}
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

	private static boolean checkSlots(MasterServer ms,ArrayList<String> subList,int numcell ){
		try {
			JSONParser parser = new JSONParser();
			JSONObject w;
			int x=0;
			for(String k : subList){
				String s = ms.getInfoWorkers().get(k);
				System.out.println(s);
				w = (JSONObject)parser.parse(s);
				x+=(Long)w.get("slots");
			}
			return(x>=numcell);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

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

