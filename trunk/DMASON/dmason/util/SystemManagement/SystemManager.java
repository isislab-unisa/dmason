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
		info.setArch(os.getArch());
		info.setAddress(InetAddress.getLocalHost().getHostAddress());
		info.setNum_core(os.getAvailableProcessors());
		com.sun.management.OperatingSystemMXBean sunBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		info.setMemory(sunBean.getTotalPhysicalMemorySize());
		info.setoS(os.getName()+" v"+os.getVersion());
		info.setHostName(id);
		return info;
	}

}
