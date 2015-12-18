package it.isislab.dmason.sim.app.DNetworkSIR;


import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.tools.batch.data.GeneralParam;
import it.isislab.dmason.util.connection.ConnectionType;
import java.io.File;
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
public class MPIAMQVertexes15 {
	/**
	 * The main method
	 * @param args 
	 * @throws MPIException
	 * @throws IOException
	 * @author carminespagnuolo
	 */
	public static void main(String[] args) throws MPIException, IOException {
		/**
		 * Worker parameters
		 * 0 WIDTH, field width 
		 * 1 HEIGHT, field height 
		 * 2 MAX_DISTANCE, AOI 
		 * 3 ROW, row of the distribution
		 * 4 COL, column of the distribution
		 * 5 NUM_AGENTS, total agents
		 * 6 STEP_NUMBER, max steps 
		 * 7 LOG ON (values y(es) or n(o))
		 * (all are the same for D-MASON AMQ)
		 **/
		/*Here you can set the logger level for debugging purpose*/
		boolean logOn=true;
		//	log.setLevel((args[7].equalsIgnoreCase("n"))?Level.OFF:Level.ALL);
		LogManager.getRootLogger().setLevel(org.apache.log4j.Level.OFF);

		/**/
		//MAX_D
		//NUM PROC
		//STEP
		//PATH FILE
		//IP
		if(args.length<4)
		{
			System.out.println("Error in parameters!");
			System.exit(-1);
		}
		MPI.Init(args);
		int NUM_STEP=Integer.parseInt(args[2]);
		Vertexes15 state=null;
		
		GeneralParam genParam = new GeneralParam(
				/*width*/1,
				/*height*/Integer.parseInt(args[1]),
				/*maxDistance*/Integer.parseInt(args[0]),
				/*rows*/1,
				/*columns*/Integer.parseInt(args[1]),
				/*numAgents*/0,
				/*mode*/DistributedField2D.UNIFORM_PARTITIONING_MODE,
				ConnectionType.pureActiveMQ);

		/**
		 This works when the number of processes = row * col, 
		 only for DSparseGrid2DFactory.SQUARE_DISTRIBUTION_MODE
		 **/
//		if(MPI.COMM_WORLD.getRank()==0)
//		{
			genParam.setI(0);
			genParam.setJ(MPI.COMM_WORLD.getRank());
			System.out.println("Start node 0-"+MPI.COMM_WORLD.getRank());
//		}else{
//			genParam.setI((int)(MPI.COMM_WORLD.getRank()%Integer.parseInt(args[3])));
//			genParam.setJ((int)(MPI.COMM_WORLD.getRank()/Integer.parseInt(args[4])));
//		}
		/*
		 * Fake args; because IP and PORT is not needed in MPI version
		 * */
			genParam.setIp(args[4]);
			genParam.setPort("61616");
//		boolean gui=false;
		
//		if(MPI.COMM_WORLD.getRank()==0)
//		{
//			simgui =new DFlockersWithUI(genParam);
//			
//			gui=true;
//		}
//		else
		{
			state=new Vertexes15(genParam,args[3],"");
		}
		if(MPI.COMM_WORLD.getRank()==0)
		System.out.println("After init...");
		MPI.COMM_WORLD.barrier();
//		if(!gui)
//		{
			/*Debug: you can omit this code (1) */
			if(logOn)
				if(MPI.COMM_WORLD.getRank()==0)
				{
//					printParams(args, agent_number);
					System.out.println("Prepare simulation.");
				}
			/*End (1)*/
			state.start();

			MPI.COMM_WORLD.barrier();
			/*Debug: you can omit this code (2) */
			long start_time=0;
			String file=null;
			FileOutputStream out=null;
			PrintStream print=null;
			if(logOn){	
				if(MPI.COMM_WORLD.getRank()==0)
				{
					file="MPI_NUM_PROC:"
							+args[1]+"-FILE_"+args[3].split("/")[args[3].split("/").length - 1]+"-STEP_"+args[2]+".txt";
					start_time=System.currentTimeMillis();
					out=new FileOutputStream(new File(file));
					print=new PrintStream(out);
					
					System.out.println("Worker 0 FILE:"+file+" simulation started..");
					System.out.println("STEP:");
				}
			}
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

			}
			/*Debug: you can omit this code (4) */
//			if(MPI.COMM_WORLD.getRank()==0 && logOn)
//			{
//				print.println(System.currentTimeMillis()-start_time);
//				System.out.println("end Simulation");
//				
//				SuperGraphStats sg = Partitioner.generateSuperGraphStat(state.part);
//				
//				print.print("peso: " + sg.getWeigth() + "\n#edges: " + sg.getNoe());
//				
//				try{
//					print.close();
//					out.close();
//				}catch (Exception e) {
//					System.out.println("Error file writing: "+file);
//
//				}
//			}
			/*End (4)*/
			MPI.Finalize();
			System.exit(0);
//		}else ((Console)simgui.createController()).pressPause();


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
