package it.isislab.dmason.experimentals.systemmanagement.master;

import java.util.HashMap;

public interface MultiServerInterface {

	
	
	public void checkAllConnectedWorkers();
	public void sumbit();
	
	public void start(int idSimulation);
	public void stop(int idSimulation);
	public void pause(int idSimulation);
	
	public HashMap<String, String> getInfoWorkers();
	
}
