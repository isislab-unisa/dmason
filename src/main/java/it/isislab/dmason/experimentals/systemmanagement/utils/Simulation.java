package it.isislab.dmason.experimentals.systemmanagement.utils;

public class Simulation {

	
	private String simName;
	private String simID;
	private String simPath;

	public Simulation(String name, String id, String mainPath) {
		simName=name;
		simID=id;
		simPath=mainPath;
	}
	
	
	
	//getter and setter
	protected String getSimName() {return simName;}
	protected void setSimName(String simName) {this.simName = simName;}
	protected String getSimID() {return simID;}
	protected void setSimID(String simID) {this.simID = simID;}
	protected String getSimPath() {return simPath;}
	protected void setSimPath(String simPath) {this.simPath = simPath;}
	
	
	
}
