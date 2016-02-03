/**
 * Copyright 2012 Universita' degli Studi di Salerno


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
package it.isislab.dmason.experimentals.util.management.worker;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.text.NumberFormat;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
/**
* @author Michele Carillo
* @author Ada Mancuso
* @author Dario Mazzeo
* @author Francesco Milone
* @author Francesco Raia
* @author Flavio Serrapica
* @author Carmine Spagnuolo
*/
public class SystemManager {
	
	MemoryMXBean memory;
	OperatingSystemMXBean os;
	String id;
	
	
	public SystemManager(String id) {
		memory = ManagementFactory.getMemoryMXBean();
		os = ManagementFactory.getOperatingSystemMXBean();
		this.id = id;
	}
	
	public PeerStatusInfo generate() throws Exception{
		PeerStatusInfo info = new PeerStatusInfo();
		info.setArchitecture(os.getArch());
		info.setAddress(InetAddress.getLocalHost().getHostAddress());
		info.setNumCores(os.getAvailableProcessors());
		OperatingSystemMXBean sunBean = ManagementFactory.getOperatingSystemMXBean();
		//info.setMemory(sunBean.getTotalPhysicalMemorySize());
		info.setOS(os.getName()+" v"+os.getVersion());
		info.setHostname(InetAddress.getLocalHost().getHostName());
		info.setId(id);
		info.setStatus("Idle");
		return info;
	}

	
	

	
	
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
