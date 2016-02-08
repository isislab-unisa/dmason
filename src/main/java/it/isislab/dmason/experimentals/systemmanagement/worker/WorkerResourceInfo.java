package it.isislab.dmason.experimentals.systemmanagement.worker;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.text.DecimalFormat;

public class WorkerResourceInfo {

	/**
	 * 
	 */
	
	private OperatingSystemMXBean  os=null;
	private MemoryMXBean memory=null;
	private MemoryUsage available=null;
	private MemoryUsage busy=null;
	private double cpuLoad=0.0;
	private static final double byte_giga=1073741824;
	private static final double byte_mega=1048576;
	

	
	public WorkerResourceInfo() {
		os = ManagementFactory.getOperatingSystemMXBean();
		memory = ManagementFactory.getMemoryMXBean();
	}


	public double getAvailableHeapGb(){	this.setAvailableHeapMemory();
	double toReturn=available.getMax()/byte_giga; 
	DecimalFormat format=new DecimalFormat("#.##");
	String dx=format.format(toReturn);
	toReturn=Double.valueOf(dx);
	return toReturn;		/*getConnection().asynchronousReceive(topicWorker, new MyMessageListener() {

	@Override
	public void onMessage(Message msg) {
	 try {
		Object o;
		o=parseMessage(msg);
		MyHashMap map=(MyHashMap) o;

	} catch (JMSException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	}
});*/
	}

	public double getAvailableHeapMb(){this.setAvailableHeapMemory();
	
	double toReturn=available.getMax()/byte_mega; 
	DecimalFormat format=new DecimalFormat("#,##");
	String dx=format.format(toReturn);
	toReturn=Double.valueOf(dx);
	return toReturn;
	}

	public double getBusyHeapGb(){this.setUsedHeapMemory();
	double toReturn=busy.getUsed()/byte_giga; 
	DecimalFormat format=new DecimalFormat("#,##");
	String dx=format.format(toReturn);
	toReturn=Double.valueOf(dx);
	return toReturn;
	
	
	}
	public double getBusyHeapMb(){this.setUsedHeapMemory();
	double toReturn=busy.getUsed()/byte_mega; 
	DecimalFormat format=new DecimalFormat("#,##");
	String dx=format.format(toReturn);
	toReturn=Double.valueOf(dx);
	return toReturn;
	}

	
	
	private void setCpuLoad(){cpuLoad=os.getSystemLoadAverage() ;}
	private void setAvailableHeapMemory(){available=memory.getNonHeapMemoryUsage();}
	private void setUsedHeapMemory(){busy= memory.getHeapMemoryUsage();}
	public double getCPULoad(){this.setCpuLoad();return cpuLoad;}

	
	
	
	/*
	public static void main(String[] args) throws MalformedObjectNameException, InstanceNotFoundException, NullPointerException, ReflectionException {
		
		double byte_giga=1073741824;
		double byte_mega=1048576;
		
		Runtime runtime=Runtime.getRuntime();
		NumberFormat format= NumberFormat.getInstance();
		
		
		long maxMemory = runtime.maxMemory(); //il massimoo della memoria che la vm tenta di usare
		long allocatedMemory = runtime.totalMemory();//la memoria disponibile per i processi attuali e futuri per la vm
		long freeMemory = runtime.freeMemory();//la memoria dipsonibili per i futuri oggetti allocati 
		long used =allocatedMemory-freeMemory;		
	
		
		MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
		MemoryUsage m= memory.getHeapMemoryUsage();
		//System.out.println(   Double.parseDouble(""+used)/byte_giga );
		//System.out.println(   Double.parseDouble(""+m.getUsed())/byte_giga );
		
		
		//MemoryUsage m1=memory.getNonHeapMemoryUsage();
		
		
		System.out.println(Double.parseDouble(""+m.getMax())/byte_giga);
		
		
		//System.out.println(Double.parseDouble(""+freeMemory)/byte_giga);
		
	   System.out.println(Double.parseDouble(""+allocatedMemory)/byte_giga);
		//System.out.println(Double.parseDouble(""+maxMemory)/byte_giga);//m.getmax
		//System.out.println( Double.parseDouble(""+   ((freeMemory + (maxMemory - allocatedMemory)) )/ byte_giga)   );
		

		
		
		//cpuLOAD
		//OperatingSystemMXBean  os = ManagementFactory.getOperatingSystemMXBean();
		//System.out.println("load cpu "+os.getSystemLoadAverage() );
		
		//memory available on vm 
		
		//memory busy on vm
		
		
	}
	*/
}
