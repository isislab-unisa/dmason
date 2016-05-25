package it.isislab.dmason.experimentals.systemmanagement.console;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.math.ode.IntegratorException;
import org.json.simple.JSONObject;

import it.isislab.dmason.experimentals.systemmanagement.master.MasterServer;
import it.isislab.dmason.experimentals.systemmanagement.utils.Simulation;

public enum PromptSimulationController implements Prompt {
	HELP(new Action(){

		@Override
		public Object exec(Console c, String[] params, String stringPrompt, MasterServer ms) throws Exception {
			c.printf("********************************************************************************************************");
			c.printf("*    help                 |print commands list.                                                        *");
			c.printf("*    start                |exec the simulation corresponding to the given id.                          *");
			c.printf("*    stop                 |stop the simulation corresponding to the given id.                          *");
			c.printf("*    pause                |pause the simulation corresponding to the given id.                         *");
			c.printf("*    list                 |list the existing simulations                                               *");
			c.printf("*    logs                 |show the path where find the simulation log corresponding to the given id.  *");
			c.printf("********************************************************************************************************");
			return null;
		}

	}),
	START(new Action(){
		
		@Override
		public Object exec(Console c, String[] params, String stringPrompt, MasterServer ms) throws Exception {

			if(params!=null && params.length>0 && params.length<2){
				int simID;
				try{
				 simID= Integer.parseInt(params[0]);
				}catch(NumberFormatException e){
					c.printf("An Error was occurred! \n"
							+"   Invalid simID\n");
					c.printf("Operation aborted!");
					return null;
				}
				if(ms.getSimulationsList().containsKey(simID)){
					ms.start(simID);
					c.printf("Simulation started");
				}
				else{
					c.printf("No simulation found");
				}
				
				return null;
			}else{
				if(params==null) c.printf("Too few arguments");
				else c.printf("Too many arguments");
				c.printf("Usage: \n"
						+"      start <simID>\n");
				return null;
			}
		}

	}),STOP(new Action(){

		@Override
		public Object exec(Console c, String[] params, String stringPrompt, MasterServer ms) throws Exception {

			if(params!=null && params.length>0 && params.length<2){
				int simID;
				try{
				 simID= Integer.parseInt(params[0]);
				}catch(NumberFormatException e){
					c.printf("An Error was occurred! \n"
							+"   Invalid simID\n");
					c.printf("Operation aborted!");
					return null;
				}
				if(ms.getSimulationsList().containsKey(simID)){
					ms.start(simID);
					c.printf("Simulation started");
				}
				else{
					c.printf("No simulation found");
				}
				
				return null;
			}else{
				if(params==null) c.printf("Too few arguments");
				else c.printf("Too many arguments");
				c.printf("Usage: \n"
						+"      stop <simID>\n");
				return null;
			}
		}

	}),PAUSE(new Action(){

		@Override
		public Object exec(Console c, String[] params, String stringPrompt, MasterServer ms) throws Exception {

			if(params!=null && params.length>0 && params.length<2){
				int simID;
				try{
				 simID= Integer.parseInt(params[0]);
				}catch(NumberFormatException e){
					c.printf("An Error was occurred! \n"
							+"   Invalid simID\n");
					c.printf("Operation aborted!");
					return null;
				}
				if(ms.getSimulationsList().containsKey(simID)){
					ms.start(simID);
					c.printf("Simulation started");
				}
				else{
					c.printf("No simulation found");
				}
				
				return null;
			}else{
				if(params==null) c.printf("Too few arguments");
				else c.printf("Too many arguments");
				c.printf("Usage: \n"
						+"      pause <simID>\n");
				return null;
			}
		}

	}),LIST(new Action(){

		@Override
		public Object exec(Console c, String[] params, String stringPrompt, MasterServer ms) throws Exception {
			Iterator i = ms.getSimulationsList().values().iterator();
			Simulation s1=null,s2=null; 
			String toPrint="";
			while(i.hasNext()){
				s1= (Simulation)i.next();
				if(i.hasNext()){
					s2 = (Simulation)i.next();

					toPrint+=String.format("*************************       *************************\n"+
							"*  ID %-18d*       *  ID %-18d*\n"+
							"*                       *       *                       *\n"+
							"*  Status %-14s*       *  Status %-14s*\n"+
							"*                       *       *                       *\n"+
							"*  Step(s)  %-12d*       *  Step(s)  %-12d*\n"+
							"*                       *       *                       *\n"+
							"*  Step(s)  %-12d*       *  Step(s)  %-12d*\n"+
							"*                       *       *                       *\n"+
							"*                       *       *                       *\n"+
							"*************************       *************************\n",
							s1.getSimID(),s2.getSimID(),s1.getStatus(),s2.getStatus(),s1.getStep(),s2.getStep());
				}else
					toPrint+=String.format("*************************\n"+
							"*  ID %d                *\n"+
							"*                       *\n"+
							"*  Status %s            *\n"+
							"*                       *\n"+
							"*  Step(s)  %d          *\n"+
							"*                       *\n"+
							"*                       *\n"+
							"*************************\n",
							s1.getSimID(),s1.getStatus(),s1.getStep());
			}
			c.printf(toPrint);
			return null;
		}

	}),LOGS(new Action(){
		
		private String getSimLog(MasterServer ms, int simID){
			String hitory_pathName = ms.getMasterHistory();
			File f_history = new File(hitory_pathName);
			if(!f_history.exists() || !f_history.isDirectory()){
				return null;
			}
			File[] cur_dir_files = null;
			Properties prop = null;
			InputStream in = null;
			for(File f: f_history.listFiles()){
				if(!f.isDirectory()) continue;
				cur_dir_files = f.listFiles(new FileFilter() {
					
					@Override
					public boolean accept(File pathname) {
						
						return pathname.getName().endsWith(".history");
					}
				});
				prop = new Properties();
				try {
					in = new FileInputStream(cur_dir_files[0]);
					prop.load(in);
					String id=prop.getProperty("simID");
					String path=null;
					if(simID == Integer.parseInt(id))
						path=prop.getProperty("simLogZipFile");
					in.close();
					if(path!=null) return path;
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
			}
			return null;
		}

		@Override
		public Object exec(Console c, String[] params, String stringPrompt, MasterServer ms) throws Exception {

			if(params!=null && params.length>0 && params.length<2){
				int simID;
				try{
				 simID= Integer.parseInt(params[0]);
				}catch(NumberFormatException e){
					c.printf("An Error was occurred! \n"
							+"   Invalid simID\n");
					c.printf("Operation aborted!");
					return null;
				}
				if(ms.getSimulationsList().containsKey(simID)){
					String logsPathName = ms.logRequestForSimulationByID(simID,"logreq");
					c.printf("Simulation log folder:\n"
							+"     "+ logsPathName);
				}
				else{
					c.printf("No running simulation found!\n Checking in History.....");
					String result = getSimLog(ms,simID);
					if(result==null)
						c.printf("No simulation found!");
					else
						c.printf("Simulation log folder:\n"
								+"     "+ result);
				}
				
				return null;
			}else{
				if(params==null) c.printf("Too few arguments");
				else c.printf("Too many arguments");
				c.printf("Usage: \n"
						+"      logs <simID>\n");
				return null;
			}
		}

	});


	private Action action;
	private PromptSimulationController(Action a){ action =a;}

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
