package it.isislab.dmason.sim.app.SIR;



import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.util.connection.ConnectionType;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import mpi.MPI;
import mpi.MPIException;

import org.apache.log4j.LogManager;

/**
 * An example of MPI Worker
 * @author carminespagnuolo
 *
 */
public class MPIWorker {

	private static int numSteps = 10000; 
	private static int rows = 1; //number of rows
	private static int columns = 2; //number of columns
	private static int AOI=10; //max distance
	private static int NUM_AGENTS=1000; //number of agents
	private static int WIDTH=400; //field width
	private static int HEIGHT=400; //field height
	private static String ip="127.0.0.1"; //ip of activemq
	private static String port="61616"; //port of activemq

	private static int LP;
	/**
	 * The main method
	 * @param args 
	 * @throws MPIException
	 * @throws IOException
	 * @author carminespagnuolo
	 */
	public static void main(String[] args) {
		
		try{
			/**
			 * Worker parameters
			 * 0 AOI
			 * 1 W
			 * 2 H
			 * 3 IP
			 * 4 AGENTS
			 * 5 STEP
			 * 6 R
			 * 7 C
			 * 8 TYPE 0 DB, 1 REFLECTION, 2 MH-LOOKUP, 3 MH-STATIC
			 **/
			/*Here you can set the logger level for debugging purpose*/
			boolean logOn=true;
			if(args.length<5)
			{
				System.out.println("Error in parameters!");
				System.exit(-1);
			}

			AOI=Integer.parseInt(args[0]);
			WIDTH=Integer.parseInt(args[1]);
			HEIGHT=Integer.parseInt(args[2]);
			ip=args[3];
			NUM_AGENTS=Integer.parseInt(args[4]);
			numSteps=Integer.parseInt(args[5]);
			rows=Integer.parseInt(args[6]);
			columns=Integer.parseInt(args[7]);
			int type=Integer.parseInt(args[8]);
			LogManager.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
			/**/

			MPI.Init(args);
		
			int NUM_STEP=numSteps;
			GeneralParam genParam=null;
			
				genParam = new GeneralParam(
						/*width*/WIDTH,
						/*height*/HEIGHT,
						/*maxDistance*/AOI,
						/*rows*/rows,
						/*columns*/columns,
						/*numAgents*/NUM_AGENTS,
						/*mode*/DistributedField2D.UNIFORM_PARTITIONING_MODE,
						ConnectionType.pureActiveMQ);



				/**
		 This works when the number of processes = row * col, 
		 only for DSparseGrid2DFactory.SQUARE_DISTRIBUTION_MODE
				 **/
				if(MPI.COMM_WORLD.getRank()==0)
				{
					genParam.setI(0);
					genParam.setJ(0);
				}else{
					genParam.setI((int)(MPI.COMM_WORLD.getRank()%rows));
					genParam.setJ((int)(MPI.COMM_WORLD.getRank()/columns));
				}
				/*
				 * Fake args; because IP and PORT is not needed in MPI version
				 * */
				genParam.setIp(ip);
				genParam.setPort("61616");

				DistributedState state = null;
				switch (type) {
				case 0:
					state = new it.isislab.dmason.sim.app.SIRDoubleBuffering.DPeople(genParam,"test1");
					break;
				case 1:
					state = new it.isislab.dmason.sim.app.SIRStateReflection.DPeople(genParam,"test2");
					break;
				case 2:
					state = new it.isislab.dmason.sim.app.SIRStateWithLookup.DPeople(genParam,"test3");
					break;
				case 3:
					state = new it.isislab.dmason.sim.app.SIRState.DPeople(genParam,"test4");
					break;

				default:
					break;
				}



				MPI.COMM_WORLD.barrier();


				/*Debug: you can omit this code (1) */
				if(logOn)
					if(MPI.COMM_WORLD.getRank()==0)
					{
						printParams(args, 0);
						System.out.println("Prepare simulation.");
					}
				/*End (1)*/
				state.start();

				long end_time=0;

				MPI.COMM_WORLD.barrier();

				/*Debug: you can omit this code (2) */
				long start_time=0;
				String file=null;
				FileOutputStream out=null;
				PrintStream print=null;

				/*End (2)*/
				//MPI.COMM_WORLD.barrier();

				int STEP=NUM_STEP;
				while(NUM_STEP!=0)                                                
				{                                                                 
					/*Debug: you can omit this code (3) */
					if(MPI.COMM_WORLD.getRank()==0 && logOn)
					{
						System.out.print(STEP-NUM_STEP+" . ");
					}
					/*End (3)*/
					state.schedule.step(state);
					NUM_STEP--;


					if(logOn && NUM_STEP == (STEP-1)){	

						if(MPI.COMM_WORLD.getRank()==0)
						{

							file="SIR-SIM-TIME-"+type+"-"+STEP+"-"+NUM_STEP+"-"+NUM_AGENTS+"-"+WIDTH+"-"+HEIGHT+"-"+AOI+"-"+rows+"-"+columns+".txt";
							start_time=System.currentTimeMillis();
							out=new FileOutputStream(file);
							print=new PrintStream(out);

							System.out.println("Worker 0 FILE:"+file+" simulation started..");
							System.out.println("STEP:");
						}
					}
					if(logOn && NUM_STEP == 0){	
						end_time=(System.currentTimeMillis()-start_time);

					}

				}

				/*Debug: you can omit this code (4) */
				if(MPI.COMM_WORLD.getRank()==0 && logOn)
				{

					print.print(type+";"+STEP+";"+NUM_STEP+";"+NUM_AGENTS+";"+WIDTH+";"+HEIGHT+";"+AOI+";"+rows+";"+columns+";"+end_time+"\n");
					System.out.println("end Simulation");
					try{
						print.close();
						out.close();
					}catch (Exception e) {
						System.out.println("Error file writing: "+file);

					}
				}
				/*End (4)*/
				MPI.Finalize();
				System.exit(0);
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/*Debug: you can omit this code (6) */
	private static void printParams(String[] args, int agent_number)
			throws MPIException {
		final Logger log=Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		if(MPI.COMM_WORLD.getRank()==0 && log.isLoggable(Level.ALL))
		{
			log.info("Simulation Parameters:");
			log.info("	Width: "+args[0]);
			log.info("	Height: "+args[1]);
			log.info("	Max Distance: "+args[2]);
			log.info("	Row: "+args[3]);
			log.info("	Col: "+args[4]);
			log.info("	Tot Agent: "+args[5]);
			log.info("	Num Steps: "+args[6]);
			log.info("	Cell Agents: "+agent_number);
		}
	}
	/*End (6)*/

}