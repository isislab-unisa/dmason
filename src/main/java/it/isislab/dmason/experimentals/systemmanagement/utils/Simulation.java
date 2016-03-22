/**
 * Copyright 2016 Universita' degli Studi di Salerno


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package it.isislab.dmason.experimentals.systemmanagement.utils;

import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField2D;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 *
 */
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
	private long numStep;
	private int connectionType;
	private int numCells;
	private String execFileName;
	private String topicPrefix;
	private int P;
	private ArrayList<String> topicList;
	private int numWorkers;
	public int getNumWorkers() {
		return numWorkers;
	}
	public void setNumWorkers(int numWorkers) {
		this.numWorkers = numWorkers;
	}


	private List<CellType> cellTypeList;
	private int received_cell_type;
	private long startTime=Long.MIN_VALUE;
	private long endTime=Long.MIN_VALUE;
	private long step=0;
	public static final String CREATED="CREATED";
	public static final String STARTED="STARTED";
	public static final String FINISHED="FINISHED";
	public static final String PAUSED="PAUSED";
	private String simulationStatus=CREATED; //play,pause,stop





	public String getStatus() {
		return simulationStatus;
	}
	public void setStatus(String status)
	{
		this.simulationStatus=status;
	}

	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	/**
	 * @return the numStep
	 */
	public long getNumStep() {
		return numStep;
	}

	/**
	 * @param numStep the numStep to set
	 */
	public void setNumStep(long numStep) {
		this.numStep = numStep;
	}

	/**
	 * @return the startTime
	 */
	public long getStartTime() {
		
		
		return startTime;
	}

	/**
	 * @return the endTime
	 */
	public long getEndTime() {
		return endTime;
	}
	
	public String getStartTimeAsDate(){
		Date date=new Date(this.getStartTime());
		SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
		String dateText = df2.format(date);
		return dateText;
	}

	public String getEndTimeAsDate(){
		Date date=new Date(this.getEndTime());
		SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
		String dateText = df2.format(date);
		return dateText;
	}
	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the step
	 */
	public long getStep() {
		return step;
	}

	/**
	 * @param step the step to set
	 */
	public void setStep(long step) {
		this.step = step;
	}

	/**
	 * @return the received_cell_type
	 */
	public int getReceived_cell_type() {
		return received_cell_type;
	}

	/**
	 * @param received_cell_type the received_cell_type to set
	 */
	public void setReceived_cell_type(int received_cell_type) {
		this.received_cell_type = received_cell_type;
	}

	/**
	 * 
	 */
	public Simulation() {}

	/**
	 * 
	 * Simulation for ActivemQ Connection
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
	 * @param mode  if 0 uniform, else if 1 non-uniform
	 * @param parameters
	 */
	public Simulation(String simName, String simulationFolder,String execSimNAme,
			int rows, int columns, int aoi, int width,
			int height, int numAgent, long stepsnumber, int mode, int connection) {
		this.simName = simName;
		this.simulationFolder = simulationFolder;
		this.rows = rows;
		this.columns = columns;
		this.aoi = aoi;
		this.width = width;
		this.height = height;
		this.numAgents =numAgent;
		this.numCells= getRows()*getColumns();
		this.connectionType=connection;
		this.topicList=new ArrayList<>();
		this.mode = mode;		
		this.numStep=stepsnumber;
		this.execFileName=execSimNAme;
		this.cellTypeList=new ArrayList<CellType>();

	}


	/**
	 * Simulation  for NON UNIFORM Connection
	 * @param simName
	 * @param simulationFolder
	 * @param execSimNAme
	 * @param p
	 * @param aoi
	 * @param width
	 * @param height
	 * @param numAgent
	 * @param stepsnumber
	 * @param mode
	 * @param connection
	 */
	public Simulation(String simName, String simulationFolder,String execSimNAme,
			int p, int aoi, int width,
			int height, int numAgent, long stepsnumber, int mode, int connection) {
		this.simName = simName;
		this.simulationFolder = simulationFolder;
		this.aoi = aoi;
		this.width =width;
		this.height = height;
		this.numAgents = numAgent;
		this.connectionType=connection;
		this.topicList=new ArrayList<>();
		this.mode = mode;		
		this.numStep=stepsnumber;
		this.execFileName=execSimNAme;
		this.cellTypeList=new ArrayList<CellType>();
		this.P=p;
		this.numCells= P;


	}


	/**
	 * @return the p
	 */
	public int getP() {
		return P;
	}


	/**
	 * @param p the p to set
	 */
	public void setP(int p) {
		P = p;
	}


	public List<CellType> getCellTypeList() {
		return cellTypeList;
	}


	public long getNumberStep(){ 
		return numStep;
	}

	public void setNumberStep(long step){ 
		numStep=step;
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


	public void setTopicPrefix(String pref){
		topicPrefix=pref;
	}

	public String getTopicPrefix(){
		return topicPrefix;
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

	private String getModeForToString(int i){
		if(i==0) return "uniform";
		else return "non-uniform";
	}


	public void setListCellType(List<CellType> list) {
		cellTypeList=list;

	}


	/**
	 * toString method for Simulation in json format
	 *
	 */
	public String toString() {
		if(mode==DistributedField2D.UNIFORM_PARTITIONING_MODE)
			return "{\"name\":\"" + simName + "\","
			+ " \"id\":\"" + simID+ "\","
			+ " \"simulationFolder\":\"" + simulationFolder + "\","
			+ " \"rows\":\"" + rows+ "\","
			+ " \"columns\":\"" + columns + "\","
			+ "\"aoi\":\"" + aoi + "\","
			+ " \"width\":\"" + width+ "\","
			+ " \"height\":\"" + height + "\","
			+ "\"numAgents\":\"" + numAgents + "\","
			+ "\"partitioning\":\""+ getModeForToString(this.mode) + "\","
			+ " \"connectionType\":\"" + connectionType + "\","
			+ " \"num_cell\":\""+ numCells + "\","
			+ " \"num_worker\":\""+ topicList.size() + "\","
			+ " \"start\":\""+this.getStartTimeAsDate()+"\","
			+ " \"step\":\""+step+"\","
			+ " \"status\":\""+simulationStatus+"\"}";
		else // NON UNIFORM
			return "{\"name\":\"" + simName + "\","
					+ "\"id\":\"" + simID+ "\","
					+ "\"simulationFolder\":\"" + simulationFolder + "\","
					+ "\"aoi\":\"" + aoi + "\","
					+ "\"width\":\"" + width+ "\","
					+ "\"height\":\"" + height + "\","
					+ "\"numAgents\":\"" + numAgents + "\","
					+ "\"partitioning\":\""+ getModeForToString(this.mode) + "\","
					+ "\"connectionType\":\"" + connectionType + "\","
					+ "\"cells\":\""+ P + "\","
					+ "\"num_worker\":\""+ topicList.size() + "\","
					+ "\"start\":\""+this.getStartTimeAsDate()+"\","
					+ "\"step\":\""+step+"\","
					+ "\"status\":\""+simulationStatus+"\"}";

	}

	

}
