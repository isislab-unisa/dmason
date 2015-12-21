package it.isislab.dmason.sim.app.DFlockersState;

import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.grid.sparse.DSparseGrid2DFactory;
import it.isislab.dmason.util.connection.ConnectionType;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import mpi.MPI;
import mpi.MPIException;

import org.apache.log4j.LogManager;

import sim.display.Console;
import sim.display.GUIState;
/**
 * This class is only for testing GUI, you can run on your local machine with MPI
 * @author carminespagnuolo
 *
 */
public class MPIGUIWorker {

	public static void main(String[] args) throws MPIException, IOException {
		/*
		 * Args
		 * 0 WIDTH
		 * 1 HEIGHT
		 * 2 MAX_DISTANCE
		 * 3 ROW
		 * 4 COL
		 * 5 NUM_AGENTS
		 */
		/*Debug*/

		Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.OFF);
		LogManager.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		/**/
		if(args.length<5)
		{
			System.out.println("Error in parameters!");
			System.exit(-1);
		}
		MPI.Init(new String[]{});
		
		GUIState state=null;

		int agent_number=Integer.parseInt(args[5])/(Integer.parseInt(args[3])*Integer.parseInt(args[4]));

		GeneralParam genParam = new GeneralParam(
				/*width*/Integer.parseInt(args[0]),
				/*height*/Integer.parseInt(args[1]),
				/*maxDistance*/Integer.parseInt(args[2]),
				/*rows*/Integer.parseInt(args[3]),
				/*columns*/Integer.parseInt(args[4]),
				/*numAgents*/agent_number,
				/*mode*/DistributedField2D.UNIFORM_PARTITIONING_MODE,
				ConnectionType.pureMPIParallel);

		printParams(args, agent_number);

		/*Tmp solution*/
		if(MPI.COMM_WORLD.getRank()==0)
		{
			genParam.setI(0);
			genParam.setJ(0);
		}else{
			genParam.setI((int)(MPI.COMM_WORLD.getRank()%Integer.parseInt(args[3])));
			genParam.setJ((int)(MPI.COMM_WORLD.getRank()/Integer.parseInt(args[4])));
		}
		/**/
		/*
		 * Fake args; because ip and port is not needed in MPI version
		 * */
		genParam.setIp("");
		genParam.setPort("");

		state=new DFlockersWithUI(genParam);
		MPI.COMM_WORLD.barrier();
		((Console)state.createController()).pressPause();
		


	}

	private static void printParams(String[] args, int agent_number)
			throws MPIException {
		if(MPI.COMM_WORLD.getRank()==0)
		{
			System.out.println("Simulation Parameters:");
			System.out.println("	Width: "+args[0]);
			System.out.println("	Height: "+args[1]);
			System.out.println("	Max Distance: "+args[2]);
			System.out.println("	Row: "+args[3]);
			System.out.println("	Col: "+args[4]);
			System.out.println("	Tot Agent: "+args[5]);
			System.out.println("	Cell Agents: "+agent_number);
		}
	}

}
