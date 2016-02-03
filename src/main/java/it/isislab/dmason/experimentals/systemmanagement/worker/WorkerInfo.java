package it.isislab.dmason.experimentals.systemmanagement.worker;

import java.io.Serializable;

//se vogliamo inviare l'oggetto
	class WorkerInfo implements Serializable{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String cpuLoad;
		private String availableheapmemory;
		private String busyheapmemory;
		private String numStep;
		private String mode;
		private String numberOfCells;
		
		
		
		public WorkerInfo() {}
		
		
		


		public void setCpuLoad(double x){this.cpuLoad=""+x;}
		public void setAvailableHeap(double x){this.availableheapmemory=""+x;}
		public void setBusyHeap(double x){this.busyheapmemory=""+x;}
		public String getCpuLoad(){return cpuLoad;}
		public String availableHeapMemory(){return availableheapmemory;}
		public String busyHeapMemory(){return busyheapmemory;}
	}