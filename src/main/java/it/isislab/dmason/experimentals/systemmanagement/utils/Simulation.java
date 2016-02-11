package it.isislab.dmason.experimentals.systemmanagement.utils;

import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;

public class Simulation {

	
	private String simName;
	private String simID;
	private String simPath;
    private GeneralParam parameters;
	
   
    /**
     * Wrapper for simulation 
     * @param name
     * @param id
     * @param mainPath
     * @param params
     */
	public Simulation(String name, String id, String mainPath,GeneralParam params) {
		simName=name;
		simID=id;
		simPath=mainPath;
		parameters=params;
		
	}
	

	
	//getter and setter
	protected String getSimName() {return simName;}
	protected void setSimName(String simName) {this.simName = simName;}
	protected String getSimID() {return simID;}
	protected void setSimID(String simID) {this.simID = simID;}
	protected String getSimPath() {return simPath;}
	protected void setSimPath(String simPath) {this.simPath = simPath;}
	protected GeneralParam getSimParameters() {return parameters;}
	protected void setSimParams(GeneralParam params) {this.parameters = params;}
	
	
}
