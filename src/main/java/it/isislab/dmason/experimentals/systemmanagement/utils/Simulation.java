package it.isislab.dmason.experimentals.systemmanagement.utils;

import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;

public class Simulation {

	
	private String simName;
	private String simID;
	private String simulationFolder;
	
	private int rows;
	private int columns;
	private int aoi;
	private int width;
	private int height;
	private int numAgents;
	private int mode;
	private int connectionType;
	private GeneralParam parameters;
	
	
	public Simulation() {}


	/**
	 * @param simName
	 * @param simID
	 * @param simulationFolder
	 * @param rows
	 * @param columns
	 * @param aoi
	 * @param width
	 * @param height
	 * @param numAgent
	 * @param mode
	 * @param parameters
	 */
	public Simulation(String simName, String simID, String simulationFolder,
			String rows, String columns, String aoi, String width,
			String height, String numAgent, String mode, String connection) {
		this.simName = simName;
		this.simID = simID;
		this.simulationFolder = simulationFolder;
		this.rows = Integer.parseInt(rows);
		this.columns = Integer.parseInt(columns);
		this.aoi = Integer.parseInt(aoi);
		this.width = Integer.parseInt(width);
		this.height = Integer.parseInt(height);
		this.numAgents = Integer.parseInt(numAgent);
		this.mode = Integer.parseInt(mode);
		this.connectionType=Integer.parseInt(connection);
		this.parameters=new GeneralParam(this.width,this.height,this.aoi,this.rows,this.columns,numAgents,this.mode,connectionType);
	}
	
	
	
	
	
    
   
	
}
