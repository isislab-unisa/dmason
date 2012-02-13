package dmason.util.SystemManagement;

import java.io.Serializable;

public class PeerStatusInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	private String oS="";
	private String arch="";;
	private int num_core;
	private String address;
	
	public PeerStatusInfo() {
		super();
	}
	
	public PeerStatusInfo(String oS, String arch, int num_core, String address) {
		super();
		this.oS = oS;
		this.arch = arch;
		this.num_core = num_core;
		this.address = address;
	}

	public String getoS() {
		return oS;
	}

	public void setoS(String oS) {
		this.oS = oS;
	}

	public String getArch() {
		return arch;
	}

	public void setArch(String arch) {
		this.arch = arch;
	}

	public int getNum_core() {
		return num_core;
	}

	public void setNum_core(int num_core) {
		this.num_core = num_core;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String toString(){
		return "HOST = "+address+"\n"+"--- diagnostic network info ---"+"\n"+"Operating System = "+oS+"\n"+
		"Architecture = "+arch+"\n"+"Number of available processors = "+num_core+"\n";
	}
	
	
	
	
	
	
	
	
	
	

}
