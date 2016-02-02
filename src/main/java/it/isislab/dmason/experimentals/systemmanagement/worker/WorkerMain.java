package it.isislab.dmason.experimentals.systemmanagement.worker;

import java.net.InetAddress;
import java.rmi.server.UID;
import java.util.UUID;

import it.isislab.dmason.util.connection.Address;




public class WorkerMain {



	public static void main(String[] args) throws Exception {
		
		System.out.println(InetAddress.getLocalHost().getHostAddress()+"-"+new UID());

		String ip =args[0];
		String port=args[1];
		Worker worker=new Worker(ip, port);
		worker.sendIdentifyTopic();
		
	/*	worker.getConnection().createTopic("READY", 1);
		worker.getConnection().subscribeToTopic("READY");
		worker.getConnection().publishToTopic(new Address("127.0.0.1", "1000"), "READY", "ciao2");
		*/
		//worker.createConnection();
		//GeneralParam p=new GeneralParam(400, 400, 10, 2, 2, 2222, DistributedField2D.UNIFORM_PARTITIONING_MODE, 2, ConnectionType.pureActiveMQ);
		
			//DistributedState d=worker.makeSimulation("/home/miccar/Scrivania/flocksim2.jar",p,"");
			//d.start();
		
	}

}
