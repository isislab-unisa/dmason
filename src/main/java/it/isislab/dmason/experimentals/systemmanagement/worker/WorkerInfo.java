package it.isislab.dmason.experimentals.systemmanagement.worker;

import java.io.Serializable;
import java.text.DecimalFormat;

//se vogliamo inviare l'oggetto
public class WorkerInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String cpuLoad;
	private int slots;
	private String availableheapmemory;
	private String busyheapmemory;
	private String ip="";
	private String workerID="";
	private String maxHeap="";

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


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{\"slots\":"+ slots+","+
				"\"cpuLoad\":" + cpuLoad +","+
				"\"availableheapmemory\":\""+availableheapmemory +"\","+ 
				"\"busyheapmemory\":\""+ busyheapmemory +"\","+
				"\"maxHeap\":\""+maxHeap+"\","+
				"\"ip\":\""+ ip +"\","+"\"workerID\":\""+workerID+"\"}";
	}		
}

