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

import it.isislab.dmason.experimentals.util.visualization.globalviewer.RemoteSnap;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField2D;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import edu.cmu.graphchi.apps.recommendations.CircleOfTrustSalsa;

/**
 * A class to create a Simulation in DMason
 * 
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 *
 */
public class Simulation implements Serializable{

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
	private String topicPrefix;
	private int P;
	private ArrayList<String> topicList;
	private int numWorkers;
	private List<CellType> cellTypeList;
	private int received_cell_type;
	private long startTime=Long.MIN_VALUE;
	private long endTime=Long.MIN_VALUE;
	private long simTime=Long.MIN_VALUE;
	private long numStep;//step of simulation 
	private long step=0;//current step
	public static final String CREATED="CREATED";
	public static final String STARTED="STARTED";
	public static final String FINISHED="FINISHED";
	public static final String PAUSED="PAUSED";
	public static final String STOPPED="STOPPED";
	private String simulationStatus=CREATED; //play,pause,stop
	private ConcurrentHashMap<Long, ArrayList<RemoteSnap>> snapshots;

	public Simulation() {}

	/**
	 * 
	 * Simulation Object constructor fon Uniform division
	 * 
	 * @param simName name of simulation 
	 * @param simulationFolder simulation path for files
	 * @param rows number of rows of distributed field
	 * @param columns number of columns of distributed field
	 * @param execSimNAme pathname of executable file
	 * @param aoi area of interest 
	 * @param width width of field
	 * @param height height of field 
	 * @param numAgent number of agents
	 * @param stepsnumber number of steps
	 * @param connection type of connection 
	 * @param mode  if 0 uniform, else if 1 non-uniform
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
		snapshots = new ConcurrentHashMap<>(100);

	}


	/**
	 * 
	 * Simulation Object constructor for Non-Uniform division
	 * 
	 * @param simName name of simulation 
	 * @param simulationFolder simulation path for files
	 * @param execSimNAme pathname of executable file
	 * @param p number of division of distributed field
	 * @param aoi area of interest 
	 * @param width width of field
	 * @param height height of field 
	 * @param numAgent number of agents 
	 * @param mode  if 0 uniform, else if 1 non-uniform
	 * @param stepsnumber number of steps
	 * @param connection type of connection
	 * 
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
		snapshots = new ConcurrentHashMap<>(100);

	}


	/**
	 * Return number of workers
	 * @return number of workers 
	 */
	public int getNumWorkers() {
		return numWorkers;
	}
	/**
	 * Set number of workers
	 * @param numWorkers number of workers 
	 */
	public void setNumWorkers(int numWorkers) {
		this.numWorkers = numWorkers;
	}
	

	/**
	 * Return status of Simulation
	 * @return the status of a simulation
	 */
	public String getStatus() {
		return simulationStatus;
	}
	/**
	 * Set status of a simulation
	 * @param status  status of simulation
	 */
	public void setStatus(String status)
	{
		this.simulationStatus=status;
	}

	/**
	 * Set end time of simulation
	 * 
	 * @param endTime endTime to set
	 */
	public void setEndTime(long endTime) {
		this.endTime = endTime;
		this.simTime = endTime - startTime;
	}

	/**
	 * Get number of steps of a simulation
	 * 
	 * @return step simulation number
	 */
	public long getNumStep() {
		return numStep;
	}

	/**
	 * Set number of step for a simulation
	 * @param numStep number of step
	 */
	public void setNumStep(long numStep) {
		this.numStep = numStep;
	}

	/**
	 * Return the time of start of simulation
	 * @return the start time of simulation
	 */
	public long getStartTime() {
		
		
		return startTime;
	}

	/**
	 * Return end time of simulation in ms
	 * @return end 
	 */
	public long getEndTime() {
		return endTime;
	}
	
	
	/**
	 * Return the duration of a simulation in ms
	 * @return duration of simulation
	 */
	public long getSimTime() {
		return simTime;
	}
	
	public String getSimTimeAsDate() {
		
		String diff = "";
		Date start=new Date(this.getStartTime());
		SimpleDateFormat df = new SimpleDateFormat("dHH:mm:ss");
		Date end=new Date(this.getEndTime());
		SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
		
        long timeDiff = Math.abs(end.getTime() - start.getTime());
        
        diff = String.format("%d : %d : %d", TimeUnit.MILLISECONDS.toHours(timeDiff),
                TimeUnit.MILLISECONDS.toMinutes(timeDiff) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeDiff)),
                TimeUnit.MILLISECONDS.toSeconds(timeDiff) - TimeUnit.MILLISECONDS.toMinutes(timeDiff) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeDiff)));
        return diff;
	}
	
	/**
	 * Return the date of start of Simulation 
	 * 
	 * @return date of the beginning of the simulation  
	 */
	public String getStartTimeAsDate(){
		Date date=new Date(this.getStartTime());
		SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
		String dateText = df2.format(date);
		return dateText;
	}

	/**
	 * Return the date of end of Simulation 
	 * 
	 * @return date of the end of the simulation  
	 */
	public String getEndTimeAsDate(){
		Date date=new Date(this.getEndTime());
		SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
		String dateText = df2.format(date);
		return dateText;
	}
	/**
	 * 
	 * 
	 * @param startTime the startTime to set
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	/**
	 * return current step of simulation
	 * @return the step
	 */
	public long getStep() {
		return step;
	}

	/**
	 * set current step of simulation
	 * @param  step number of steps
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
	 * @return the p process 
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

    /**
     * Return the list of cells assigned on the node
     * @return the list of cells assigned on the node
     */
	public List<CellType> getCellTypeList() {
		return cellTypeList;
	}


	/**
	 * Return the number of the step of the simulation
	 * @return the number of the step of the simulation
	 */
	public long getNumberStep(){ 
		return numStep;
	}

	public void setNumberStep(long step){ 
		numStep=step;
	}
	public String getJarPath() {
		return execFileName;
	}


	public void setJarPath(String simName) {
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


	/**
	 * Return type of connection
	 * @return type of connection
	 */
	public int getConnectionType() {
		return connectionType;
	}

    /**
     * Set type of connection
     * @param connectionType type of connection
     */
	public void setConnectionType(int connectionType) {
		this.connectionType = connectionType;}

	/**
	 * Return the list of topic for the simulation
	 * 
	 * @return list of topic for the simulation
	 */
	public ArrayList<String> getTopicList() {
		return topicList;
	}

    /**
     * Set list of nodes(identified with topics) that executing this simulation
     * @param topicList list of topic for this simulation 
     */
	public void setTopicList(ArrayList<String> topicList) {
		this.topicList = topicList;
	}

	public int getNumCells() {
		return numCells;
	}

    /**
     * Return the number of cells in which the field is partitioned
     * 
     * @param numCells number of cells in which the field is partitioned
     */
	public void setNumCells(int numCells) {
		this.numCells = numCells;
	}

	/**
	 * Return type of field division: uniform, non-uniform
	 * @param i mode identifier
	 * @return mode of division
	 */
	private String getModeForToString(int i){
		if(i==0) return "uniform";
		else return "non-uniform";
	}

    /**
     * Return list of cells of field to execute on a node
     * @param list of cells to execute
     */
	public void setListCellType(List<CellType> list) {
		cellTypeList=list;

	}
	
	public ConcurrentHashMap<Long, ArrayList<RemoteSnap>> getSnapshots(){
		
		return snapshots;
	}
	
	public void setSnapshots(ConcurrentHashMap<Long, ArrayList<RemoteSnap>> snapshots) {
		this.snapshots = snapshots;
	}

	/**
	 * toString method for Simulation in json format
	 *
	 */
	@Override
	public String toString() {
		if(mode==DistributedField2D.UNIFORM_PARTITIONING_MODE) //UNIFORM
			return "{\"name\":\"" + simName + "\","
			+ " \"id\":\"" + simID+ "\","
		//	+ " \"simulationFolder\":\"" + simulationFolder + "\","
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
			//		+ "\"simulationFolder\":\"" + simulationFolder + "\","
					+ "\"aoi\":\"" + aoi + "\","
					+ "\"width\":\"" + width+ "\","
					+ "\"height\":\"" + height + "\","
					+ "\"numAgents\":\"" + numAgents + "\","
					+ "\"partitioning\":\""+ getModeForToString(this.mode) + "\","
					+ "\"connectionType\":\"" + connectionType + "\","
					+ "\"num_cell\":\""+ P + "\","
					+ "\"num_worker\":\""+ topicList.size() + "\","
					+ "\"start\":\""+this.getStartTimeAsDate()+"\","
					+ "\"step\":\""+step+"\","
					+ "\"status\":\""+simulationStatus+"\"}";

	}


	

}
