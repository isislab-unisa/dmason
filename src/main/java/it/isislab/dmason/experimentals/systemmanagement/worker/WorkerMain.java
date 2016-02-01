package it.isislab.dmason.experimentals.systemmanagement.worker;

import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.util.connection.ConnectionType;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;



public class WorkerMain {



	public static void main(String[] args) {

		Worker worker=new Worker();
		//worker.createConnection();
		//GeneralParam p=new GeneralParam(400, 400, 10, 2, 2, 2222, DistributedField2D.UNIFORM_PARTITIONING_MODE, 2, ConnectionType.pureActiveMQ);
		
			//DistributedState d=worker.makeSimulation("/home/miccar/Scrivania/flocksim2.jar",p,"");
			//d.start();
		
	}

}
