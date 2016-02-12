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
		private String ip="";
		private String workerID="";
		
		/*private String numStep;
		  private String mode;
		  private String numberOfCells;
		*/
		WorkerResourceInfo info=null;
		
		
		
		
		public WorkerInfo() {
			info=new WorkerResourceInfo();
			setAvailableHeap(info.getAvailableHeapMb());
			setBusyHeap(info.getBusyHeapMb());
			setCpuLoad(info.getCPULoad());
			
		}
		
		public void setIP(String ip){this.ip=ip;};
		public void setCpuLoad(double x){this.cpuLoad=""+x;}
		public void setAvailableHeap(double x){this.availableheapmemory=""+x;}
		public void setBusyHeap(double x){this.busyheapmemory=""+x;}
		public void setWorkerID(String idworker){this.workerID=idworker;}
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
			return "[{cpuLoad:" + cpuLoad +"},"+
					"{availableheapmemory:"+availableheapmemory +"},"+ 
					"{busyheapmemory:"+ busyheapmemory +"},"+ 
					"{ip:"+ ip +"}"+"{workerID:"+workerID+"}]";
		}		
	}
	
	