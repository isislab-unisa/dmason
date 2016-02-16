package it.isislab.dmason.experimentals.systemmanagement.worker;





public class WorkerMain {



	public static void main(String[] args) throws Exception {
		
		String ip =args[0];
		String port=args[1];
		
		
		Worker worker=new Worker(ip, port);
		worker.signRequestToMaster();


	}

}
