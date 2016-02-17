package it.isislab.dmason.experimentals.systemmanagement.utils;

import java.io.Serializable;
import java.util.ArrayList;

import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;

public class Simulation implements Serializable{

	
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
	public Simulation(String simName, String simulationFolder,
			String rows, String columns, String aoi, String width,
			String height, String numAgent, String mode, String connection) {
		this.simName = simName;
		this.simulationFolder = simulationFolder;
		this.rows = Integer.parseInt(rows);
		this.columns = Integer.parseInt(columns);
		this.aoi = Integer.parseInt(aoi);
		this.width = Integer.parseInt(width);
		this.height = Integer.parseInt(height);
		this.numAgents = Integer.parseInt(numAgent);
		this.mode = Integer.parseInt(mode);
		this.numCells= getRows()*getColumns();
		this.connectionType=Integer.parseInt(connection);
		this.topicList=new ArrayList<>();
		this.parameters=new GeneralParam(this.width,this.height,this.aoi,this.rows,this.columns,numAgents,this.mode,connectionType);
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
	
		
}
