/**
 * Copyright 2012 Università degli Studi di Salerno
 

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

package dmason.util.SystemManagement;

import java.io.Serializable;

public class PeerStatusInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	private String oS="";
	private String arch="";;
	private int num_core;
	private String address;
	private String hostName;
	
	private String version;
	private String digest;
	private String topic;
	

	public PeerStatusInfo() {
		super();
	}
	
	public PeerStatusInfo(String oS, String arch, int num_core, String address,String hostName,String version) {
		super();
		this.hostName=hostName;
		this.oS = oS;
		this.arch = arch;
		this.num_core = num_core;
		this.address = address;
	}

	
	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getDigest() {
		return digest;
	}

	public void setDigest(String digest) {
		this.digest = digest;
	}
	public String getVersion()
	{
		return version;
	}
	public void setVersion(String vers)
	{
		this.version = vers;
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

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String toString(){
		return "HOST = "+address+"\n"+"--- diagnostic network info ---"+"\n"+"Operating System = "+oS+"\n"+
		"Architecture = "+arch+"\n"+"Number of available processors = "+num_core+"\n";
	}
}