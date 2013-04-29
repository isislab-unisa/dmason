package dmason.util.SystemManagement;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;

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
		com.sun.management.OperatingSystemMXBean sunBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		info.setMemory(sunBean.getTotalPhysicalMemorySize());
		info.setOS(os.getName()+" v"+os.getVersion());
		info.setHostname(InetAddress.getLocalHost().getHostName());
		info.setId(id);
		info.setStatus("Idle");
		return info;
	}

}
