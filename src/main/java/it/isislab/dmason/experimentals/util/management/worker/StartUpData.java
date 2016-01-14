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

import it.isislab.dmason.experimentals.tools.batch.data.EntryParam;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.util.connection.Address;

import java.io.Serializable;
import java.util.List;
/**
* @author Michele Carillo
* @author Ada Mancuso
* @author Dario Mazzeo
* @author Francesco Milone
* @author Francesco Raia
* @author Flavio Serrapica
* @author Carmine Spagnuolo
**/
public class StartUpData implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * The simulation class.
	 */
	private Class def;
	
	/**
	 * Parameters to pass to simulation's constructor.
	 */
	//private Object[] param;
	private GeneralParam param;
	private List<EntryParam<String, Object>> simParam;
	
	/**
	 * True if this worker must create and run the Reducer
	 */
	public boolean reducer = false;
	
	/**
	 * <code>true</code> if GUI must be shown on workers.
	 */
	public boolean graphic;
	
	/**
	 * <code>true</code> if this worker must publish the current step to the
	 * "step" topic, in order for the central console to read it.
	 */
	public boolean step = false;
	public int pos_x;
	public int pos_y;
	public boolean local;
	
	/**
	 * Name of the simulation jar to download
	 */
	private String jarName;
	
	/**
	 * Address of FTP Server
	 */
	private Address FTPAddress;

	private String uploadDir;

	/**
	 * Stores an univoque identifier for a batch run. It will be empty
	 * if the simulation is not part of a batch.
	 */
	private String topicPrefix = "";
	
	
	
	public String getTopicPrefix() {
		return topicPrefix;
	}

	public void setTopicPrefix(String topicPrefix) {
		this.topicPrefix = topicPrefix;
	}

	public String getUploadDir() {
		return uploadDir;
	}

	public void setUploadDir(String uploadDir) {
		this.uploadDir = uploadDir;
	}

	public Address getFTPAddress() {
		return FTPAddress;
	}

	public void setFTPAddress(Address fTPAddress) {
		FTPAddress = fTPAddress;
	}

	public StartUpData(Class def, GeneralParam param, boolean graphic,boolean local) {
		super();
		this.def = def;
		this.param = param;
		this.graphic = graphic;
		this.local = local;
	}
	
	public StartUpData() { }

	public Class getDef() {
		return def;
	}

	public void setDef(Class def) {
		this.def = def;
	}

	public GeneralParam getParam() {
		return param;
	}

	public void setParam(GeneralParam param) {
		this.param = param;
	}
	
	
	
	public List<EntryParam<String, Object>> getSimParam() {
		return simParam;
	}

	public void setSimParam(List<EntryParam<String, Object>> simParam) {
		this.simParam = simParam;
	}

	public boolean isStep() {
		return step;
	}

	public void setStep(boolean step) {
		this.step = step;
	}

	public void setPos_x(int pos_x){
		this.pos_x = pos_x;
	}

	public int getPos_x() {
		return pos_x;
	}
	
	public void setPos_y(int pos_y){
		this.pos_y = pos_y;
	}
	
	public int getPos_y() {
		return pos_y;
	}
	
	public void setLocal(boolean flag){
		this.local = flag;
	}
	
	public boolean getLocal(){
		return this.local;
	}

	public boolean isGraphic() {
		return graphic;
	}

	public void setGraphic(boolean graphic) {
		this.graphic = graphic;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		super.clone();
		//Object[] obj = new Object[param.length];
		StartUpData s = new StartUpData();
		s.setDef(def);
		s.setStep(step);
		s.setGraphic(graphic);
		s.setLocal(local);
		s.setPos_x(pos_x);
		s.setPos_y(pos_y);
		//s.setParam(obj);
		return s;
	}

	public void setJarName(String selSim) { jarName = selSim; }

	public String getJarName() { return jarName; }

	@Override
	public String toString() {
		return "StartUpData [def=" + def + ", param=" + param + ", simParam="
				+ simParam + ", graphic=" + graphic + ", step=" + step
				+ ", pos_x=" + pos_x + ", pos_y=" + pos_y + ", local=" + local
				+ ", jarName=" + jarName + ", FTPAddress=" + FTPAddress
				+ ", uploadDir=" + uploadDir + ", topicPrefix=" + topicPrefix
				+ "]";
	}

	
	
}