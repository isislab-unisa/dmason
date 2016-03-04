package it.isislab.dmason.experimentals.systemmanagement.worker;

import java.io.Serializable;
import java.text.DecimalFormat;

//se vogliamo inviare l'oggetto
public class WorkerInfo implements Serializable{

	/**
	 * 
	 */

	private String cpuLoad;
	private int slots;
	private String availableheapmemory;
	private String busyheapmemory;
	private String ip="";
	private String workerID="";
	private String maxHeap="";
	private String portCopyLog="";

	/**
	 * @return the portCopyLog
	 */
	public String getPortCopyLog() {
		return portCopyLog;
	}

	/**
	 * @param portCopyLog the portCopyLog to set
	 */
	public void setPortCopyLog(String portCopyLog) {
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
		setCpuLoad(info.getCPULoad());
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

	public void setWorkerID(String idworker){this.workerID=idworker;}
	public void setNumSlots(int num){slots = num;};
	public int getNumSlots(){return slots;}
	public String getCpuLoad(){return cpuLoad;}
	public String getAvailableHeapMemory(){return availableheapmemory;}
	public String getBusyHeapMemory(){return busyheapmemory;}
	public String getIP(){return ip;}
	public String getWorkerID(){return workerID;}


	/**
	 * YOU MUST NOT CHANGE THIS JSON FORMAT
	 * THE FIRST PARAMETER SLOTS used in a master to set numslots
	 */
	public String toString() {
		return "{\"slots\":"+ slots+","+
				"\"cpuLoad\":" + cpuLoad +","+
				"\"availableheapmemory\":\""+availableheapmemory +"\","+ 
				"\"busyheapmemory\":\""+ busyheapmemory +"\","+
				"\"maxHeap\":\""+maxHeap+"\","+
				"\"ip\":\""+ ip +"\","+
				"\"portcopylog\":\""+ portCopyLog +"\","+
				"\"workerID\":\""+workerID+"\"}";
	}



	public static void main(String[] args) {
		WorkerInfo inf=new WorkerInfo();
		inf.setIP("123.0.0.0");
		inf.setWorkerID("1");
		inf.setNumSlots(11);
		inf.setPortCopyLog("1616");

		System.out.println(inf.toString());
		String [] split1=inf.toString().split(",");
		
		for(String x:split1){
			if(x.contains("ip")) System.out.println(x.split(":")[1]);
			if(x.contains("portcopylog"))System.out.println(x.split(":")[1]);


		}

	}


}

