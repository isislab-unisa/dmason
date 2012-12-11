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

package dmason.batch.data;

import java.io.Serializable;

public class GeneralParam implements Serializable
{
	public int Width;
	public int Height;
	public int Rows;
	public int Columns;
	public int MaxDistance;
	//public int NumRegions;
	public int NumAgents;
	public int Mode;
	public long MaxStep;
	
	public String ip;
	public String port;
	public int i;
	public int j;
	
	public boolean isBatch;
	
	public GeneralParam(int width, int height, int maxDistance,
			int rows, int columns, int numAgents, int mode) {
		super();
		this.Width = width;
		this.Height = height;
		this.MaxDistance = maxDistance;
		//this.NumRegions = numRegions;
		this.Rows = rows;
		this.Columns = columns;
		this.NumAgents = numAgents;
		this.Mode = mode;
		this.isBatch = false;
	}
	
	public GeneralParam(int width, int height, int maxDistance,
			int rows, int columns, int numAgents, int mode, long MaxStep) {
		super();
		this.Width = width;
		this.Height = height;
		this.MaxDistance = maxDistance;
		//this.NumRegions = numRegions;
		this.Rows = rows;
		this.Columns = columns;
		this.NumAgents = numAgents;
		this.Mode = mode;
		this.MaxStep = MaxStep;
		this.isBatch = true;
	}

	public GeneralParam() {
		// TODO Auto-generated constructor stub
	}

	
	public int getRows() {
		return Rows;
	}

	public void setRows(int rows) {
		Rows = rows;
	}

	public int getColumns() {
		return Columns;
	}

	public void setColumns(int columns) {
		Columns = columns;
	}

	public int getWidth() {
		return Width;
	}

	public void setWidth(int width) {
		this.Width = width;
	}

	public int getHeight() {
		return Height;
	}

	public void setHeight(int height) {
		this.Height = height;
	}

	public int getMaxDistance() {
		return MaxDistance;
	}

	public void setMaxDistance(int maxDistance) {
		this.MaxDistance = maxDistance;
	}

	/*public int getNumRegions() {
		return NumRegions;
	}

	public void setNumRegions(int numRegions) {
		this.NumRegions = numRegions;
	}*/

	public int getNumAgents() {
		return NumAgents;
	}

	public void setNumAgents(int numAgents) {
		this.NumAgents = numAgents;
	}

	public int getMode() {
		return Mode;
	}

	public void setMode(int mode) {
		this.Mode = mode;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public int getI() {
		return i;
	}

	public void setI(int i) {
		this.i = i;
	}

	public int getJ() {
		return j;
	}

	public void setJ(int j) {
		this.j = j;
	}

	
	
	public long getMaxStep() {
		return MaxStep;
	}

	public void setMaxStep(long maxStep) {
		MaxStep = maxStep;
	}

	@Override
	public String toString() {
		return "GeneralParam [Width=" + Width + ", Height=" + Height
				+ ", Rows=" + Rows + ", Columns=" + Columns + ", MaxDistance="
				+ MaxDistance + ", NumAgents=" + NumAgents + ", Mode=" + Mode
				+ ", MaxStep=" + MaxStep + ", ip=" + ip + ", port=" + port
				+ ", i=" + i + ", j=" + j + ", isBatch=" + isBatch + "]";
	}

	
	
	
	

}
