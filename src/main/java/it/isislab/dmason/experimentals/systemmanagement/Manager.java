package it.isislab.dmason.experimentals.systemmanagement;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.experimentals.systemmanagement.worker.Worker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class Manager {
	private static final String dmason="DMASON-3.1.jar";
	@Option(name="-m", aliases = { "--mode" },usage="master or worker")
	private String mode = "worker";

	@Option(name="-ip", aliases = { "--ipjms" },usage="ip address of JMS broker (default is localhost)")
	private String ip = "127.0.0.1";

	@Option(name="-p", aliases = { "--portjms" },usage="port of the JMS broker (default is 61616)")
	private String port = "61616";

	@Option(name="-ns", aliases = { "--numberofslots" },usage="number of simulation slot for this worker (defaults is 1)")
	private int ns = 1;

	@Option(name="-h", aliases = { "--hostslist" },usage="start worker on list og hosts (-h host1 host2 host3))")
	private boolean hosts = false;

	@Argument
	private List<String> arguments = new ArrayList<String>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {

			new Manager().doMain(args);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DMasonException e) {
			// TODO Auto-generated catch block
			System.err.println("Problem in this installation of DMASON.");
		}
	}

	class WorkerThread extends Thread{

		ChannelExec channel;
		Session session;
		BufferedReader in;
		boolean running = true;
		String host;
		public WorkerThread(String host,int nslot) throws IOException, JSchException {
			this.host=host;
			JSch jsch=new JSch();
			session=jsch.getSession(System.getProperty("user.name"), host, 22);
			jsch.addIdentity(System.getProperty("user.home")+File.separator+".ssh"+File.separator+"id_rsa");
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			channel=(ChannelExec) session.openChannel("exec");
		    in=new BufferedReader(new InputStreamReader(channel.getInputStream()));
		    //System.out.println("./"+dmason+" -m worker -ip "+ip+" -port "+port+" -ns "+ns+";");
		 
			channel.setCommand("java -jar "+dmason+" -m worker -ip "+ip+" -port "+port+" -ns "+nslot+";");
			
		}
		@Override
		public void run() {
			
			try {
				channel.connect();
				System.out.println("Connected to remote machine.");
				String msg=null;
				while(running && (msg=in.readLine())!=null){
				  System.out.println(msg);
				}

				channel.disconnect();
				session.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public void stopThread() throws Exception{
			running=false;
			System.out.println("Stopping worker "+host+".");
			
			ChannelExec channel2=(ChannelExec) session.openChannel("exec");
			channel2.setCommand("killall java");
			channel2.connect();
			channel2.disconnect();
			channel.disconnect();
			session.disconnect();
			in.close();
		}
	}
	private List<WorkerThread> workers=new ArrayList<WorkerThread>();
	private boolean waitThread=true;
	final Lock lock = new ReentrantLock();
	final Condition workersWork  = lock.newCondition();
	
	private void signalWorkers()
	{
		lock.lock();
		try {
			waitThread=false;
			for(WorkerThread w: workers) w.stopThread();
			
			workersWork.signalAll();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

	public void doMain(String[] args) throws IOException, DMasonException {
		CmdLineParser parser = new CmdLineParser(this);
		try {

			parser.parseArgument(args);
			
			if(mode.equals("master"))
			{
				System.out.println("Starting DMASON in Master mode ...");
				startMaster();
			}
			else if(mode.equals("worker"))
			{	
				if(hosts)
				{
					System.out.println("Starting workers...");
					if(arguments.isEmpty())
						throw new CmdLineException("Please specify the list of hosts, where start the workes.");
					
					WorkerThread w;

					//for(String host : arguments)
					for (int i = 0; i < arguments.size()-1; i+=2)	
					{
						String host=arguments.get(i);
						int ns=Integer.parseInt(arguments.get(i+1));
						System.out.println("Start worker on "+host+" with nslots "+ns);
						w=new WorkerThread(host,ns);
						workers.add(w);
						w.start();
					}

					try {

						Signal.handle(new Signal("TERM"), new SignalHandler() {

							@Override
							public void handle(Signal arg0) {
								// Signal handler method for CTRL-C and simple kill command.
								System.out.println("Kill jobs ...");
								signalWorkers();

							}
						});
						Signal.handle(new Signal("INT"), new SignalHandler() {

							@Override
							public void handle(Signal arg0) {
								// Signal handler method for CTRL-C and simple kill command.
								System.out.println("Kill jobs ...");
								signalWorkers();

							}
						});
						Signal.handle(new Signal("HUP"), new SignalHandler() {

							@Override
							public void handle(Signal arg0) {
								// Signal handler method for CTRL-C and simple kill command.
								System.out.println("Kill jobs ...");
								signalWorkers();

							}
						});
					}
					catch (final IllegalArgumentException e) {
						e.printStackTrace();
					}

					lock.lock();
					try {
						while(waitThread)
							workersWork.await();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						lock.unlock();
					}

				}else{
					System.out.println("Starting DMASON in Worker mode ...");
					System.out.println("\tJMS broker IP: "+ip);
					System.out.println("\tJMS broker PORT: "+port);
					System.out.println("\tNumber of simulations slots: "+ns);
					startWorker();
				}

			}else
			{
				throw new CmdLineException("No correct mode given, master or worker is allowed.");
			}


		} catch( CmdLineException e ) {
			// if there's a problem in the command line,
			// you'll get this exception. this will report
			// an error message.
			System.err.println(e.getMessage());
			System.err.println("java -jar dmason.jar [options...]");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			System.err.println("  Example: java -jar dmason.jar --mode master");

			return;
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public void startWorker(){
		Worker worker=new Worker(ip, port,ns);
		worker.signRequestToMaster();

	}
	public void startMaster() throws DMasonException
	{

		File dataresources=new File("resources");
		if(!dataresources.exists() || !dataresources.isDirectory())
			throw new DMasonException("Problems in resources check your data.");
		// 1. Creating the server on port 8080
		Server server = new Server(8080);
		ServletContextHandler handler =new ServletContextHandler(server,"resources/systemmanagement/master");	
		server.setHandler(handler);	

		// 2. Creating the WebAppContext for the created content
		WebAppContext ctx = new WebAppContext();
		ctx.setResourceBase("resources/systemmanagement/master");
		//ctx.setContextPath("/master");
		ctx.setContextPath("/");
		//3. Including the JSTL jars for the webapp.
		ctx.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",".*/[^/]*jstl.*\\.jar$");

		//4. Enabling the Annotation based configuration
		org.eclipse.jetty.webapp.Configuration.ClassList classlist = org.eclipse.jetty.webapp.Configuration.ClassList.setServerDefault(server);
		classlist.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration", "org.eclipse.jetty.plus.webapp.EnvConfiguration", "org.eclipse.jetty.plus.webapp.PlusConfiguration");
		classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration", "org.eclipse.jetty.annotations.AnnotationConfiguration");

		//	ctx.addServlet("it.isislab.dmason.experimentals.systemmanagement.master.web.utils.GetConnectedWorkersServlet", "/getWorkers");
		//		ctx.addServlet(new ServletHolder(new GetConnectedWorkersServlet(master)),"/getWorkers");//
		//      ctx.addServlet(new ServletHolder(new CreateSimulationFolderServlet(master)), "/createSim"); 
		//      ctx.addBean(master);

		//5. Setting the handler and starting the Server
		server.setHandler(ctx);
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
