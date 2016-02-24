package it.isislab.dmason.experimentals.systemmanagement.utils;

import java.io.Serializable;
import java.util.ArrayList;

import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;

public class Simulation implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String simName;
	private int simID;
	private String simulationFolder;
	private int rows;
	private int columns;
	private int aoi;
	private int width;
	private int height;
	private int numAgents;
	private int mode;
	private int connectionType;
    private int numCells;
    private String execFileName;



	private GeneralParam parameters;
	private ArrayList<String> topicList;
	
	
	


	public Simulation() {}


	/**
	 * 
	 * Wrapper of a sim
	 * 
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
	public Simulation(String simName, String simulationFolder,String execSimNAme,
			String rows, String columns, String aoi, String width,
			String height, String numAgent, int mode, int connection) {
		this.simName = simName;
		this.simulationFolder = simulationFolder;
		this.rows = Integer.parseInt(rows);
		this.columns = Integer.parseInt(columns);
		this.aoi = Integer.parseInt(aoi);
		this.width = Integer.parseInt(width);
		this.height = Integer.parseInt(height);
		this.numAgents = Integer.parseInt(numAgent);
		this.mode = mode;
		this.numCells= getRows()*getColumns();
		this.connectionType=connection;
		this.topicList=new ArrayList<>();
		this.parameters=new GeneralParam(this.width,this.height,this.aoi,this.rows,this.columns,numAgents,this.mode,connectionType);
	    this.execFileName=execSimNAme;
	}


	public String getJarName() {
		return execFileName;
	}


	public void setJarName(String simName) {
		this.execFileName = simName;
	}
	
	
	public String getSimName() {
		return simName;
	}


	public void setSimName(String simName) {
		this.simName = simName;
	}


	public int getSimID() {
		return simID;
	}


	public void setSimID(int simID) {
		this.simID = simID;
	}


	public String getSimulationFolder() {
		return simulationFolder;
	}


	public void setSimulationFolder(String simulationFolder) {
		this.simulationFolder = simulationFolder;
	}


	public int getRows() {
		return rows;
	}


	public void setRows(int rows) {
		this.rows = rows;
	}


	public int getColumns() {
		return columns;
	}


	public void setColumns(int columns) {
		this.columns = columns;
	}


	public int getAoi() {
		return aoi;
	}


	public void setAoi(int aoi) {
		this.aoi = aoi;
	}


	public int getWidth() {
		return width;
	}


	public void setWidth(int width) {
		this.width = width;
	}


	public int getHeight() {
		return height;
	}


	public void setHeight(int height) {
		this.height = height;
	}


	public int getNumAgents() {
		return numAgents;
	}


	public void setNumAgents(int numAgents) {
		this.numAgents = numAgents;
	}


	public int getMode() {
		return mode;
	}


	public void setMode(int mode) {
		this.mode = mode;
	}


	public int getConnectionType() {
		return connectionType;
	}


	public void setConnectionType(int connectionType) {
		this.connectionType = connectionType;}


	public GeneralParam getParameters() {return parameters;
	}


	public void setParameters(GeneralParam parameters) {this.parameters = parameters;}

	
	public ArrayList<String> getTopicList() {
		return topicList;
	}


	public void setTopicList(ArrayList<String> topicList) {
		this.topicList = topicList;
	}
	
	public int getNumCells() {
		return numCells;
	}


	public void setNumCells(int numCells) {
		this.numCells = numCells;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Simulation [simName=" + simName + ", simID=" + simID
				+ ", simulationFolder=" + simulationFolder + ", rows=" + rows
				+ ", columns=" + columns + ", aoi=" + aoi + ", width=" + width
				+ ", height=" + height + ", numAgents=" + numAgents + ", mode="
				+ mode + ", connectionType=" + connectionType + ", numCells="
				+ numCells + ", parameters=" + parameters + ", topicList="
				+ topicList + "]";
	}
	
		
}
