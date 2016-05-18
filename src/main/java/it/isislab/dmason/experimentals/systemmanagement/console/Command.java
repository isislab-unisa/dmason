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

public enum Command implements Prompt{


	LICENSE(new Action()
	{
		@Override
		public Object exec(Console c, String[] params,String stringPrompt,MasterServer ms)
		{
			c.printf("DMASON is a tool utility powerd by ISISLab, 2011.\n");
			return null;

		}
	}),
	HELP(new Action()
	{
		@Override
		public Object exec(Console c, String[] params,String stringPrompt,MasterServer ms)
		{
			c.printf(""
					+ "██████╗ ███╗   ███╗ █████╗ ███████╗ ██████╗ ███╗   ██╗ \n"
					+ "██╔══██╗████╗ ████║██╔══██╗██╔════╝██╔═══██╗████╗  ██║ \n"
					+ "██║  ██║██╔████╔██║███████║███████╗██║   ██║██╔██╗ ██║ \n"
					+ "██║  ██║██║╚██╔╝██║██╔══██║╚════██║██║   ██║██║╚██╗██║ \n"
					+ "██████╔╝██║ ╚═╝ ██║██║  ██║███████║╚██████╔╝██║ ╚████║ \n"
					+ "╚═════╝ ╚═╝     ╚═╝╚═╝  ╚═╝╚══════╝ ╚═════╝ ╚═╝  ╚═══╝ \n"
					+"\n");

			c.printf("***************************************************************************************************\n*");
			c.printf("*    help                 |print commands list.                                                   *\n*");
			c.printf("*    workers              |avaiable workers list.                                                 *\n*");
			c.printf("*    start                |exec the simulation corresponding to the given id.                     *\n*");
			c.printf("*    stop                 |stop the simulation corresponding to the given id.                     *\n*");
			c.printf("*    pause                |stop the simulation corresponding to the given id.                     *\n*");
			c.printf("*    createsimulation     |create new simulation execution.                                       *\n*");
			c.printf("*    getsimulations       |print all simulations created by the user.                             *\n*");
			c.printf("*    getsimulation        |print status of the simulation corresponding to the given id.          *\n*");
			c.printf("*    getlog               |download the results of the simulation corresponding to the given id.  *\n*");
			c.printf("*    kill                 |kill the simulation corresponding to the given id.                     *\n*");
			c.printf("***************************************************************************************************\n");
			return null;
		}
	}),
	WORKERS(new Action()
	{
		private String workerPrintFormatter(String prefix, Object s, String postfix, String fillerChar,int len){

			String str = new String(prefix+" "+s);
			int l = str.length();
			if(l<len){
				for(int i=l; i<(len-postfix.length()-1); i++){
					str+=fillerChar;
				}
			}		
			str+=postfix;
			return str;
		}
		@Override
		public Object exec(Console c, String[] params,String stringPrompt, MasterServer ms)
		{

			/**
			 * Simulation s = getSimulationDatabyId(session,  username, simID);
			 */
			if(params != null && params.length > 0 )
			{
				c.printf("Too many arguments!\n");

			}
			String message = "{\"workers\":[";

			int startMessageSize = message.length();

			for(String s : ms.getInfoWorkers().values())
				message+=s+",";

			if(message.length() > startMessageSize)
				message=message.substring(0, message.length()-1)+"]}";
			else
				message="";
			
			if(message.length()==0)
				return null;

			try{
				JSONParser parser = new JSONParser();
				JSONObject j_workers = (JSONObject)parser.parse(message);
				JSONArray worker = (JSONArray)j_workers.get("workers");
				if(worker !=null){

					for(int i=0; i<worker.size(); i++){
						JSONObject o = (JSONObject)worker.get(i);
						//System.out.println(("************************\n").length()); ==25
						c.printf("************************");
						c.printf(workerPrintFormatter("*   ID",o.get("workerID"),"*"," ",25));
						c.printf(workerPrintFormatter("*   CPU",o.get("cpuLoad"),"*"," ",25));
						c.printf(workerPrintFormatter("*   RAM",o.get("maxHeap"),"*"," ",25));
						c.printf(workerPrintFormatter("*     Used",o.get("availableheapmemory"),"*"," ",25));
						c.printf(workerPrintFormatter("*   Slots",o.get("slots"),"*"," ",25));
						c.printf(workerPrintFormatter("*   IP",o.get("ip"),"*"," ",25));
						c.printf("************************");
						//{"workers":[{"slots":4,"cpuLoad":0.0,"availableheapmemory":"110,28","maxHeap":"1765,5","ip":"192.168.122.1","workerID":"-1169498471"f6l5a4v3i2o1

					}
				}else{
					c.printf("No workers available");
				}
			}catch(ParseException e){
				c.printf("Some errors were occurred \n");
				e.printStackTrace();
			}
			return null;
		}
	}),
	CREATESIMULATION(new Action(){

		@Override
		public Object exec(Console c, String[] params, String stringPrompt, MasterServer ms) throws Exception {
			if(params==null){
				c.printf("too few arguments\n");
				c.printf("USAGE: createsimulation simName jarFile rows columns aoi width height #agent #step [partitioning(uniform | nonuniform)] [connection (activemq | mpi)]\n");
				return null;
			}
			
			if(params.length < 9 || params.length>11){
				if(params.length>11)
					c.printf("too many arguments\n");
				else
					c.printf("too few arguments\n");
				
				c.printf("USAGE: createsimulation simName jarFile rows columns aoi width height #agent #step [partitioning(uniform | nonuniform)] [connection (activemq | mpi)]\n");
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
				String partitioning="";
				String connType="";
				int mode=Integer.MIN_VALUE;
				int connection = Integer.MIN_VALUE;

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
				}

				switch(partitioning.toLowerCase()){
					case "uniform": mode =DistributedField2D.UNIFORM_PARTITIONING_MODE; break;
					case "nonuniform": mode =DistributedField2D.NON_UNIFORM_PARTITIONING_MODE; break;
					default: mode =DistributedField2D.UNIFORM_PARTITIONING_MODE; break;
				}

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
				
				switch(mode){
				case DistributedField2D.UNIFORM_PARTITIONING_MODE:
					int numCell = rows * columns;
					if(numCell < allworkers.size()){
						subWorkersList.addAll(allworkers.subList(0,numCell));
					}else{
						subWorkersList = allworkers;
					}
					break;
				/*case DistributedField2D.NON_UNIFORM_PARTITIONING_MODE:
					if(cells < topics.length){
						for (int i = 0; i < cells; i++) {
							topicList.add(topics[i]);
						}
					}else{
						for(String x: topics) 
							topicList.add(x);
					}
						
					break;*/
			}


				Simulation s = new Simulation(simName, simPath,internalSimPathJar+File.separator+jarSimName,rows, columns, aoi, width, height, numAgent, numStep, mode, connection);
				
				
				
				s.setTopicList(subWorkersList);
				s.setNumWorkers(subWorkersList.size());
				s.setSimID(simId);
				s.setTopicPrefix(simName+"-"+simId);


				ms.submitSimulation(s );
				
			}catch(Exception e){
				c.printf("Some errors were occurred\n");
				c.printf(e.getMessage()+"\n");
				c.printf("USAGE: createsimulation simName jarFile rows columns aoi width height #agent #step [partitioning(uniform | nonuniform)] [connection (activemq | mpi)]\n");
				return null;
			}
			return null;
		}

	})

	//	,
	//
	//	START(new Action()
	//	{
	//		@Override
	//		public Object exec(Console c, String[] params,String stringPrompt)
	//		{
	//			/**
	//			 * Simulation s = getSimulationDatabyId(session,  username, simID);
	//			 */
	//			if(params == null || params.length < 1 )
	//			{
	//				c.printf("usage of start command : start ID [ID:intger ID of simulation execution environment]\n");
	//				return null;
	//			}else{
	//				int simID = Integer.parseInt(params[0])-1;
	//				Simulations sims = SofManager.getSimulationsData(SOFShellClient.session);
	//				if(sims == null){
	//					c.printf("No such simulation");
	//					return null;
	//				}
	//				Simulation sim = sims.getSimulations().get(simID);
	//				//sim = ScudManager.getSimulationDatabyId(SCUDShellClient.session,  SCUDShellClient.session.getUsername(), simID);
	//				SofManager.runAsynchronousSimulation(SOFShellClient.session,sim);
	//				//ScudManager.runSimulation(SCUDShellClient.session, SCUDShellClient.session.getUsername(), simID, s.getLoop());	
	//				return null;
	//			}
	//		}
	//	}),
	//
	//	CREATESIMULATION(new Action()
	//	{
	//		@Override
	//		public Object exec(Console c, String[] params,String stringPrompt)
	//		{
	//
	//			String parsedParams[] = SofManager.parseParamenters(params,4);
	//
	//			if(parsedParams.length == 6)
	//			{
	//				
	//				try {
	//					SofManager.checkParamMakeSimulationFolder(parsedParams);
	//					
	//				} catch (ParameterException e1) {
	//					// TODO Auto-generated catch block
	//					c.printf(e1.getMessage());
	//					return null;
	//				}
	//				try {
	//
	//
	//					SofManager.makeSimulationFolder(
	//							
	//							SOFShellClient.session,
	//							parsedParams[0],//MODEL TYPE MASON - NETLOGO -GENERIC
	//							parsedParams[1],//SIM NAME
	//							parsedParams[2],//INPUT.XML PATH 
	//							parsedParams[3],//OUTPUT.XML PATH 
	//							parsedParams[4],//DESCRIPTION SIM
	//							parsedParams[5],//SIMULATION EXEC PATH
	//							""); //interpretergenericpath
	//					return null;
	//				} catch (Exception e) {
	//					e.printStackTrace();
	//					c.printf("Error in making execution environment!\n");
	//					return null;
	//				}
	//			}
	//			else if(parsedParams.length == 7){
	//				try {
	//					
	//					SofManager.checkParamMakeSimulationFolder(parsedParams);
	//				} catch (ParameterException e1) {
	//					// TODO Auto-generated catch block
	//					c.printf(e1.getMessage());
	//					return null;
	//				}
	//				try {
	//
	//
	//					SofManager.makeSimulationFolder(
	//							SOFShellClient.session,
	//							parsedParams[0],//MODEL TYPE MASON - NETLOGO -GENERIC
	//							parsedParams[1],//SIM NAME
	//							parsedParams[2],//INPUT.XML PATH 
	//							parsedParams[3],//OUTPUT.XML PATH 
	//							parsedParams[4],//DESCRIPTION SIM
	//							parsedParams[5],//SIMULATION EXEC PATH
	//							parsedParams[6]); //intepretergenericpath
	//					return null;
	//				} catch (Exception e) {
	//					e.printStackTrace();
	//					c.printf("Error in making execution environment!\n");
	//					return null;
	//				}
	//			}
	//			
	//			else{
	//				c.printf("Error "+(parsedParams.length<6?"few":"much more")+" parameters.:\n");
	//				c.printf("usage: MODEL[MASON-NETLOGO-GENERIC] SIM-NAME[String]INPUT.xml[String absolutely]"
	//						+ " Output.xml[String absolutely path] DESCRIPTION-SIM[String] SIMULATION-EXECUTABLE-MODEL[String absolutely path]\n");
	//				return null;
	//			}
	//		}
	//	}),
	//	GETSIMULATIONS(new Action()
	//	{
	//		@Override
	//		public Object exec(Console c, String[] params,String stringPrompt)
	//		{	
	//			try {
	//
	//				Simulations listSim  = SofManager.getSimulationsData(SOFShellClient.session);
	//				if(listSim == null){
	//					c.printf("No such simulation");
	//					return null;
	//				}
	//
	//				for(Simulation s: listSim.getSimulations())
	//					c.printf(s+"\n");
	//				return null;
	//			} catch (NumberFormatException e) {
	//				// TODO Auto-generated catch block
	//				e.printStackTrace();
	//				return null;
	//			}
	//		}
	//	}),
	//	GETSIMULATION(new Action()
	//	{
	//		@Override
	//		public Object exec(Console c, String[] params,String stringPrompt)
	//		{
	//			if(params == null || params.length < 1 )
	//			{
	//				c.printf("few parameters!\n Usage: getsimuation #IDSIM");
	//				return null;
	//			}else{
	//				int simID = Integer.parseInt(params[0])-1;
	//				Simulations listSim  = SofManager.getSimulationsData(SOFShellClient.session);
	//				if(listSim == null){
	//					c.printf("No such simulation");
	//					return null;
	//				}
	//				Simulation sim = listSim.getSimulations().get(simID);
	//				try {
	//					if(!HadoopFileSystemManager.ifExists(SOFShellClient.session,SofManager.fs.getHdfsUserPathSimulationByID(sim.getId())))
	//						c.printf("Simulation SIM"+simID+" not exists\n");
	//
	//					//sim = ScudManager.getSimulationDatabyId(SCUDShellClient.session,SCUDShellClient.session.getUsername(),simID);
	//					c.printf(sim+"\n");
	//					return null;
	//				} catch (NumberFormatException e) {
	//					// TODO Auto-generated catch block
	//					e.printStackTrace();
	//					return null;
	//				} catch (JSchException e) {
	//					c.printf("Error during resource creating\n"+e.getMessage());
	//					e.printStackTrace();
	//					return null;
	//				} catch (IOException e) {
	//					c.printf("Error during resource creating\n"+e.getMessage());
	//					e.printStackTrace();
	//					return null;
	//				}
	//
	//			}
	//		}
	//	}),
	//
	//	GETRESULT(new Action()
	//	{
	//		@Override
	//		public Object exec(Console c, String[] params,String stringPrompt)
	//		{
	//			if(params == null)
	//			{
	//				c.printf("Error few parameters!\n Usage: getresult simID [destinationDirPath]");
	//				return null;
	//			}else{
	//				Simulations sims = SofManager.getSimulationsData(SOFShellClient.session);
	//				if(sims == null){
	//					c.printf("No such simulation");
	//					return null;
	//				}
	//				int simID = Integer.parseInt(params[0])-1;
	//				Simulation sim = null;
	//				try{
	//					sim = sims.getSimulations().get(simID);
	//				}catch(IndexOutOfBoundsException e){
	//					c.printf("No such simulation");
	//					return null;
	//				}
	//				//if no path is specified, saves in current directory
	//				String path = (params.length < 2)? System.getProperty("user.dir"):params[1];
	//				c.printf("Simulation will download in: "+path);
	//				SofManager.downloadSimulation(SOFShellClient.session,sim.getId(),path);
	//				return null;
	//			}
	//		}
	//	}),
	//	LIST(new Action()
	//	{
	//		@Override
	//		public Object exec(Console c, String[] params,String stringPrompt)
	//		{
	//			Simulations sims = SofManager.getSimulationsData(SOFShellClient.session);
	//			if(sims == null){
	//				c.printf("No such simulation");
	//				return null;
	//			}
	//			for(int i=1; i<=sims.getSimulations().size(); i++){
	//				int simID= i-1;
	//				Simulation s = sims.getSimulations().get(simID);
	//				c.printf("*****************************************************************************************************************\n");
	//				c.printf("sim-id: "+i+" name: "+s.getName()+" state: "+s.getState()+" time: "+s.getCreationTime()+" hdfsId: "+s.getId()+" *\n");
	//				c.printf("*****************************************************************************************************************\n");
	//			}
	//			return null;
	//		}
	//	}),
	//	KILL(new Action()
	//	{
	//		@Override
	//		public Object exec(Console c, String[] params,String stringPrompt)
	//		{
	//			if(params == null || params.length < 1 )
	//			{
	//				c.printf("Error few parameters!\n Usage: kill simID");
	//				return null;
	//			}else{
	//				int simID = Integer.parseInt(params[0])-1;
	//
	//				Simulations listSim  = SofManager.getSimulationsData(SOFShellClient.session);
	//				if(listSim == null){
	//					c.printf("No such simulation");
	//					return null;
	//				}
	//				if(listSim.getSimulations().size()-1<simID){
	//					c.printf("Simulation not exists");
	//					return null;
	//				}
	//
	//				Simulation sim = listSim.getSimulations().get(simID);
	//				String cmd="pkill -f \""+sim.getProcessName()+"\"";
	//
	//				String bash = "if "+cmd+" ; then echo 0; else echo -1; fi";
	//
	//				try {
	//					HadoopFileSystemManager.exec(SOFShellClient.session,bash);
	//				} catch (Exception e ) {
	//					// TODO Auto-generated catch block
	//					e.printStackTrace();
	//					return null;
	//				}
	//				
	//				c.printf("Process killed");
	//				SofManager.setSimulationStatus(SOFShellClient.session,sim,Simulation.KILLED);
	//				return null;
	//			}
	//		}
	//	}),
	//	STOP(new Action()
	//	{
	//		@Override
	//		public Object exec(Console c, String[] params,String stringPrompt)
	//		{
	//			if(params == null || params.length < 1 )
	//			{
	//				c.printf("Error few parameters!\n Usage: stop simID");
	//				return null;
	//			}else{
	//				int simID = Integer.parseInt(params[0])-1;
	//
	//				Simulations listSim  = SofManager.getSimulationsData(SOFShellClient.session);
	//				if(listSim == null){
	//					c.printf("No such simulation");
	//					return null;
	//				}
	//				if(listSim.getSimulations().size()-1<simID){
	//					c.printf("Simulation not exists");
	//					return null;
	//				}
	//
	//				Simulation sim = listSim.getSimulations().get(simID);
	//				if(sim.getState().equals(Simulation.STOPPED)){
	//					c.printf("Already stopped!\n");
	//					return null;
	//				}
	//				sim = listSim.getSimulations().get(simID);
	//				if(sim.getState().equals(Simulation.RUNNING)){
	//				   Message stop = new Message();
	//				   stop.setId(SofManager.getMexID());
	//				   stop.setMessage(Message.STOP_MESSAGE);
	//				   SofManager.sendMessage(SOFShellClient.session, sim, stop);
	//				   return null;
	//				}
	//				return null;
	//			}
	//		}
	//	})

	;


	private Action action;

	private Command(Action a)
	{
		this.action = a;
	}

	public Object exec(final Console c, final String[] params,String stringPrompt, MasterServer ms, final PromptListener l)
	{
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


	private static Object execCommand(final Console console, Class enumClass, String stringPrompt,MasterServer ms) throws IOException{
		String commandLine = null;
		Scanner scanner = null;
		Object toReturn = null;
		final Enum helpmsg = Enum.valueOf(enumClass, "help".toUpperCase());
		((Prompt)helpmsg).exec(console,null,stringPrompt,ms, new PromptListener()
		{
			@Override
			public void exception(Exception e)
			{
				console.printf(Manager.COMMAND_ERROR, helpmsg, e.getMessage());
			}
		});


		while (true)
		{
			commandLine = console.readLine(stringPrompt+" "+Manager.TIME_FORMAT+" >>>", new Date());
			scanner = new Scanner(commandLine);

			if (scanner.hasNext())
			{
				final String commandName = scanner.next().toUpperCase();
				if(commandName.equalsIgnoreCase("exit")){
					return toReturn;
				}
				try
				{
					final Enum cmd = Enum.valueOf(enumClass, commandName);
					String param= scanner.hasNext() ? scanner.nextLine() : null;
					if(param !=null && param.charAt(0)== ' ')
						param=param.substring(1,param.length());
					String[] params = param!=null?param.split(" "):null;

					toReturn = ((Prompt)cmd).exec(console,params, stringPrompt, ms ,new PromptListener()
					{
						@Override
						public void exception(Exception e)
						{
							console.printf(Manager.COMMAND_ERROR, cmd, e.getMessage());
						}
					});
				}
				catch (IllegalArgumentException e)
				{
					console.printf(Manager.UNKNOWN_COMMAND, commandName);
				}
			}

			scanner.close();
		}
	}

}
