package it.isislab.dmason.util.connection.mpi;



public interface MPIMessageListener{
	
	public void onMessage(Object message) throws Exception;
}
