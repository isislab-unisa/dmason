package it.isislab.dmason.experimentals.systemmanagement.worker;




public class WorkerMain {



	public static void main(String[] args) {

		System.out.println(args[0]);
		String ip =args[0];
		String port=args[1];
		Worker worker=new Worker(ip, port);
		
		//worker.createConnection();
		//GeneralParam p=new GeneralParam(400, 400, 10, 2, 2, 2222, DistributedField2D.UNIFORM_PARTITIONING_MODE, 2, ConnectionType.pureActiveMQ);
		
			//DistributedState d=worker.makeSimulation("/home/miccar/Scrivania/flocksim2.jar",p,"");
			//d.start();
		
	}

}
