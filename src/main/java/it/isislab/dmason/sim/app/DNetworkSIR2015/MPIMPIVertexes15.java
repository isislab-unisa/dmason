package it.isislab.dmason.sim.app.DNetworkSIR2015;


import it.isislab.dmason.sim.field.continuous.DContinuous2DFactory;
import it.isislab.dmason.sim.field.network.partitioning.Partitioner;
import it.isislab.dmason.sim.field.network.partitioning.SuperGraphStats;
import it.isislab.dmason.tools.batch.data.GeneralParam;
import it.isislab.dmason.util.connection.ConnectionType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
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
public class MPIMPIVertexes15 {
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
		 * 0 COL, column of the distribution
		 * 1 STEP_NUMBER, max steps 
		 * 2 GRAPH-PATH
		 * 3 GRAPH-PARTS-PATH 
		 **/
		/*Here you can set the logger level for debugging purpose*/
		boolean logOn=true;
		LogManager.getRootLogger().setLevel(org.apache.log4j.Level.OFF);

		if(args.length<4)
		{
			System.out.println("Error in parameters!");
			System.exit(-1);
		}
		
		Integer col=Integer.parseInt(args[0]);
		Integer step=Integer.parseInt(args[1]);
		String path_graph=args[2];
		String path_parts_graph=args[3];
		
		MPI.Init(args);

		Vertexes15 state=null;
		
		GeneralParam genParam = new GeneralParam(
				/*width*/1,
				/*height*/1,
				/*maxDistance*/1,
				/*rows*/1,
				/*columns*/col,
				/*numAgents*/0,
				/*mode*/DContinuous2DFactory.HORIZONTAL_DISTRIBUTION_MODE,
				ConnectionType.pureMPIParallel);

		/**
		 This works when the number of processes = row * col, 
		 only for DSparseGrid2DFactory.SQUARE_DISTRIBUTION_MODE
		 **/
		genParam.setI(0);
		genParam.setJ(MPI.COMM_WORLD.getRank());
		if(MPI.COMM_WORLD.getRank()==0)
			System.out.println("Start node 0-"+MPI.COMM_WORLD.getRank());

			genParam.setIp("127.0.0.1");
			genParam.setPort("61616");

			state=new Vertexes15(genParam,path_graph,path_parts_graph);

		if(MPI.COMM_WORLD.getRank()==0) System.out.println("After init...");
		MPI.COMM_WORLD.barrier();

			if(logOn)
				if(MPI.COMM_WORLD.getRank()==0)
				{

					System.out.println("Prepare simulation.");
				}
			/*End (1)*/
			state.start();
			String[] graph_name=path_graph.split("/");
			String[] algo_name=path_parts_graph.split("/");
			MPI.COMM_WORLD.barrier();
			/*Debug: you can omit this code (2) */
			long start_time=0;
			String file=null;
			FileOutputStream out=null;
//			PrintStream print=null;
			PrintWriter print = null;
			String log_data="";
			if(logOn){	
				if(MPI.COMM_WORLD.getRank()==0)
				{
				
//					file="MPI_k-"+col+"-graph-"+graph_name[graph_name.length-1]+"numstep-"+step;
					file="MPI-Results.csv";
					start_time=System.currentTimeMillis();
					
					print=new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
					
					System.out.println("Worker 0 FILE:"+file+" simulation started..");
					System.out.println("STEP:");
				}
			}
			if(MPI.COMM_WORLD.getRank()==0)
			{
				log_data+=algo_name[algo_name.length-2]+";"+graph_name[graph_name.length-1]+";"+col+";"+step;
			}
			/*End (2)*/
			//MPI.COMM_WORLD.barrier();

			int STEP=step;
			while(step!=0)
			{
				/*Debug: you can omit this code (3) */
				if(MPI.COMM_WORLD.getRank()==0 && logOn)
				{
					System.out.print(STEP-step+" . ");
				}
				/*End (3)*/
				state.schedule.step(state);
				step--;

			}
			/*Debug: you can omit this code (4) */
			if(MPI.COMM_WORLD.getRank()==0)
			{
				log_data+=";"+(System.currentTimeMillis()-start_time);
				print.println(log_data);
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
//		}else ((Console)simgui.createController()).pressPause();


	}
	

}
