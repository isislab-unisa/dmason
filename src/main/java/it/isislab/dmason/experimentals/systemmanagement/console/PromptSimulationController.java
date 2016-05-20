package it.isislab.dmason.experimentals.systemmanagement.console;

import java.util.Iterator;

import org.apache.commons.math.ode.IntegratorException;

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
			c.printf("*    log                  |show the path where find the simulation log corresponding to the given id.  *");
			c.printf("********************************************************************************************************");
			return null;
		}
		
	}),
	START(new Action(){

		@Override
		public Object exec(Console c, String[] params, String stringPrompt, MasterServer ms) throws Exception {
		
			return null;
		}
		
	}),STOP(new Action(){

		@Override
		public Object exec(Console c, String[] params, String stringPrompt, MasterServer ms) throws Exception {
		
			return null;
		}
		
	}),PAUSE(new Action(){

		@Override
		public Object exec(Console c, String[] params, String stringPrompt, MasterServer ms) throws Exception {
		
			return null;
		}
		
	}),LIST(new Action(){
		
		private String simPrintFormatter(String prefix, Object s, String postfix, String fillerChar,int len){

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
		public Object exec(Console c, String[] params, String stringPrompt, MasterServer ms) throws Exception {
			Iterator i = ms.getSimulationsList().values().iterator();
			int x=0;
			Simulation s1=null,s2=null; 
			String toPrint="";
			while(i.hasNext()){
				s1= (Simulation)i.next();
				x++;
				if(i.hasNext()){
					s2 = (Simulation)i.next();
					
					toPrint+=String.format("*************************       *************************\n"+
										   "*  ID %-18d*       *  ID %-18d*\n"+
										   "*                       *       *                       *\n"+
										   "*  Status %-14s*       *  Status %-14s*\n"+
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

		@Override
		public Object exec(Console c, String[] params, String stringPrompt, MasterServer ms) throws Exception {
		
			return null;
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
