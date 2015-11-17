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

package it.isislab.dmason.util.management.Worker;

import java.io.Serializable;
/**
* @author Michele Carillo
* @author Ada Mancuso
* @author Dario Mazzeo
* @author Francesco Milone
* @author Francesco Raia
* @author Flavio Serrapica
* @author Carmine Spagnuolo
**/
public class PeerStatusInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/** A mnemonic name users can assign to this worker */
	private String alias = "";
	
	/** Operative System running this worker */
	private String os="";
	
	/** CPU Architecture (i.e. 32bit, 64bit) */ 
	private String architecture = "";
	
	/** Total number of cores */
	private int numCores;
	
	/** Available system memory */
	private long memory;
	
	/** Name of simulation being ran (if any) */
	private String simulationName;
	
	/** Current simulation step (if running any) */
	private long steps;
	
	/** Status of the worker (running, idle, paused, ...) */
	private String status;
	
	/** IP address of the host running the worker */
	private String address;
	
	/** Hostname of the host running the worker */
	private String hostname;
	
	private String version;
	
	/** Worker's executable digest (identify a generic version of the worker) */
	private String digest;
	
	/** Topic name */
	private String topic;
	
	/** Unique distributed ID */
	private String id;
	

	public PeerStatusInfo() {
		super();
	}
	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getAlias()
	{
		return alias;
	}

	public void setAlias(String alias)
	{
		this.alias = alias;
	}
	
	public String getArchitecture() {
		return architecture;
	}

	public void setArchitecture(String arch) {
		this.architecture = arch;
	}
	
	public String getDigest() {
		return digest;
	}

	public void setDigest(String digest) {
		this.digest = digest;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}
	
	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}
	
	public long getMemory() {
		return memory;
	}

	public void setMemory(long memory) {
		this.memory = memory;
	}
	
	public int getNumCores() {
		return numCores;
	}

	public void setNumCores(int num_core) {
		this.numCores = num_core;
	}
	
	public String getOS() {
		return os;
	}

	public void setOS(String oS) {
		this.os = oS;
	}
	
	public String getSimulationName()
	{
		return simulationName;
	}

	public void setSimulationName(String simulationName)
	{
		this.simulationName = simulationName;
	}
	
	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}
	
	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}
	
	public String getVersion()
	{
		return version;
	}
	
	public void setVersion(String vers)
	{
		this.version = vers;
	}

	public String getOs()
	{
		return os;
	}

	public void setOs(String os)
	{
		this.os = os;
	}

	public long getSteps()
	{
		return steps;
	}

	public void setSteps(long steps)
	{
		this.steps = steps;
	}

	@Override
	public String toString()
	{
		return "PeerStatusInfo@" + Integer.toHexString(hashCode())
				+ " [address=" + address + ", hostname=" + hostname
				+ ", id=" + id + ", alias=" + alias + ", os=" + os
				+ ", architecture=" + architecture + ", numCores=" + numCores
				+ ", memory=" + memory + ", topic=" + topic + ", digest="
				+ digest + ", status=" + status + ", simulationName="
				+ simulationName + "]";
	}
}