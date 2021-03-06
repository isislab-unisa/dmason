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
package it.isislab.dmason.experimentals.systemmanagement.worker;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * A class is a container of node 
 * 
 *  resources info
 * 
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 *
 */
public class WorkerInfo implements Serializable{

	private String cpuLoad;
	private int slots;
	private String availableheapmemory;
	private String busyheapmemory;
	private String ip="";
	private String workerID="";
	private String maxHeap="";
	private int portCopyLog;

	/**
	 * @return the portCopyLog
	 */
	public int getPortCopyLog() {
		return portCopyLog;
	}

	/**
	 * @param portCopyLog the portCopyLog to set
	 */
	public void setPortCopyLog(int portCopyLog) {
		this.portCopyLog = portCopyLog;
	}


	/*private String numStep;
		  private String mode;
		  private String numberOfCells;
	 */
	WorkerResourceInfo info=null;

	public WorkerInfo() {
		info=new WorkerResourceInfo();
		setNumSlots(info.getNumCores());
		setAvailableHeap(info.getAvailableHeapMb());
		setBusyHeap(info.getBusyHeapMb());
		setCpuLoad(WorkerResourceInfo.getCPULoad());
		setMaxHeap(info.getMaxHeapMb());
	}

	private void setMaxHeap(double maxHeapMb) {

		DecimalFormat format=new DecimalFormat("#.##");
		String dx=format.format(maxHeapMb);
		maxHeap =dx;
	}


	public void setIP(String ip){this.ip=ip;};

	public void setCpuLoad(double x){this.cpuLoad=""+x;}

	public void setAvailableHeap(double x){

		DecimalFormat format=new DecimalFormat("#.##");
		String dx=format.format(x);
		this.availableheapmemory=dx;
	}
	public void setBusyHeap(double x){

		DecimalFormat format=new DecimalFormat("#.##");
		String dx=format.format(x);
		this.busyheapmemory=dx;
	}

	/**
	 * Set id of Worker 
	 * @param idworker of node
	 */
	public void setWorkerID(String idworker){this.workerID=idworker;}
	public void setNumSlots(int num){slots = num;};
	public int getNumSlots(){return slots;}
	
	public String getCpuLoad(){return cpuLoad;}
	public String getAvailableHeapMemory(){return availableheapmemory;}
	public String getBusyHeapMemory(){return busyheapmemory;}

	/**
	 * Return ip of node
	 * 
	 * @return ip of node
	 */
	public String getIP(){return ip;}
	/**
	 * Return the Worker ID
	 * @return id of Worker 
	 */
	public String getWorkerID(){return workerID;}


	
	 // YOU MUST NOT CHANGE THIS JSON FORMAT
	 // THE FIRST PARAMETER SLOTS used in a master to set numslots
	 
	public String toString() {
		return "{\"slots\":"+ slots+","+
				"\"cpuLoad\":" + cpuLoad +","+
				"\"availableheapmemory\":\""+availableheapmemory +"\","+ 
				"\"busyheapmemory\":\""+ busyheapmemory +"\","+
				"\"maxHeap\":\""+maxHeap+"\","+
				"\"ip\":\""+ ip +"\","+
				"\"portcopylog\":"+ portCopyLog +","+
				"\"workerID\":\""+workerID+"\"}";
	}


}

