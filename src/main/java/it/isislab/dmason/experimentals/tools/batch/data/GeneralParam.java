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

package it.isislab.dmason.experimentals.tools.batch.data;

		import java.io.Serializable;
		import java.util.HashMap;

public class GeneralParam implements Serializable
{
	private int Width;
	private int Height;
	private int Lenght;
	private int Rows;
	private int Columns;
	private int Lenghts;
	private boolean is3D = true;
	private int Aoi;
	private int NumAgents;
	private int Mode;
	private long MaxStep;
	private String ip;
	private String port;
	private int i;
	private int j;
	private int z;
	private HashMap<String, Object> model_params;
	private boolean isBatch;
	private int connectionType;


	private int P;


	public GeneralParam(int width, int height, int aoi,
						int P, int numAgents, int mode, long maxStep , int connectionType) {
		super();
		this.Width = width;
		this.Height = height;
		this.Aoi = aoi;
		this.P=P;
		this.NumAgents = numAgents;
		this.Mode = mode;
		this.isBatch = false;
		this.connectionType=connectionType;
		this.MaxStep=maxStep;
	}

	public GeneralParam(int width, int height,int length, int aoi,
						int P, int numAgents, int mode, long maxStep , int connectionType, boolean is3D) {
		super();
		this.Width = width;
		this.Height = height;
		this.Lenght = length;
		this.Aoi = aoi;
		this.P=P;
		this.NumAgents = numAgents;
		this.Mode = mode;
		this.isBatch = false;
		this.connectionType=connectionType;
		this.MaxStep=maxStep;
		this.is3D=is3D;
	}


	public GeneralParam(int width, int height, int aoi,
						int P, int numAgents, int mode, int connectionType) {
		super();
		this.Width = width;
		this.Height = height;
		this.Aoi = aoi;
		this.P=P;
		this.NumAgents = numAgents;
		this.Mode = mode;
		this.isBatch = false;
		this.connectionType=connectionType;
	}

	public GeneralParam(int width, int height,int length, int aoi,
						int P, int numAgents, int mode, int connectionType, boolean is3D) {
		super();
		this.Width = width;
		this.Height = height;
		this.Lenght=length;
		this.Aoi = aoi;
		this.P=P;
		this.NumAgents = numAgents;
		this.Mode = mode;
		this.isBatch = false;
		this.connectionType=connectionType;
		this.is3D=is3D;
	}

	public int getP() {
		return P;
	}

	public void setP(int p) {
		P = p;
	}

	public GeneralParam(int width, int height, int aoi,
						int rows, int columns, int numAgents, int mode, int connectionType) {
		super();
		this.Width = width;
		this.Height = height;
		this.Aoi = aoi;
		this.P=rows*columns;
		this.Rows = rows;
		this.Columns = columns;
		this.Lenghts=0;
		this.NumAgents = numAgents;
		this.Mode = mode;
		this.isBatch = false;
		this.connectionType=connectionType;
	}

	public GeneralParam(int width, int height,int length, int aoi,
						int rows, int columns, int lenghts, int numAgents, int mode, int connectionType, boolean is3D) {
		super();
		this.Width = width;
		this.Height = height;
		this.Lenght = length;
		this.Aoi = aoi;
		this.P=rows*columns*lenghts;
		this.Rows = rows;
		this.Columns = columns;
		this.Lenghts=lenghts;
		this.NumAgents = numAgents;
		this.Mode = mode;
		this.isBatch = false;
		this.connectionType=connectionType;
		this.is3D=is3D;
	}

	public GeneralParam(int width, int height, int aoi,
						int rows, int columns, int numAgents, int mode,long MaxStep, int connectionType) {
		super();
		this.Width = width;
		this.Height = height;
		this.Aoi = aoi;
		this.P=rows*columns;
		this.Rows = rows;
		this.Columns = columns;
		this.NumAgents = numAgents;
		this.Mode = mode;
		this.MaxStep = MaxStep;
		this.isBatch = true;
		this.connectionType=connectionType;
	}

	public GeneralParam(int width, int height, int lenght, int aoi,
						int rows, int columns, int lenghts, int numAgents, int mode,long MaxStep, int connectionType, boolean is3D) {
		super();
		this.Width = width;
		this.Height = height;
		this.Lenght=lenght;
		this.Aoi = aoi;
		this.P=rows*columns*lenghts;
		this.Rows = rows;
		this.Columns = columns;
		this.Lenghts = lenghts;
		this.NumAgents = numAgents;
		this.Mode = mode;
		this.MaxStep = MaxStep;
		this.isBatch = true;
		this.connectionType=connectionType;
		this.is3D=is3D;
	}

	public GeneralParam(int width, int height, int aoi,
						int rows, int columns, int numAgents, int mode,
						HashMap<String, Object> model_params,long MaxStep ,int connectionType) {
		super();
		this.Width = width;
		this.Height = height;
		this.Aoi = aoi;
		this.P=rows*columns;
		this.Rows = rows;
		this.Columns = columns;
		this.NumAgents = numAgents;
		this.Mode = mode;
		this.isBatch = true;
		this.MaxStep=MaxStep;
		this.model_params=model_params;
		this.connectionType=connectionType;
	}

	public GeneralParam(int width, int height, int lenght, int aoi,
						int rows, int columns, int lengths, int numAgents, int mode,
						HashMap<String, Object> model_params,long MaxStep ,int connectionType, boolean is3D) {
		super();
		this.Width = width;
		this.Height = height;
		this.Lenght=lenght;
		this.Aoi = aoi;
		this.P=rows*columns*lengths;
		this.Rows = rows;
		this.Columns = columns;
		this.Lenghts = lengths;
		this.NumAgents = numAgents;
		this.Mode = mode;
		this.isBatch = true;
		this.MaxStep=MaxStep;
		this.model_params=model_params;
		this.connectionType=connectionType;
		this.is3D=is3D;
	}

	public HashMap<String, Object> getModel_params() {
		return model_params;
	}

	public void setModel_params(HashMap<String, Object> model_params) {
		this.model_params = model_params;
	}

	public boolean isBatch() {
		return isBatch;
	}

	public void setBatch(boolean isBatch) {
		this.isBatch = isBatch;
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

	public int getAoi() {
		return Aoi;
	}

	public void setAoi(int aoi) {
		this.Aoi = aoi;
	}

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

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}

	public int getConnectionType()
	{
		return connectionType;
	}

	public long getMaxStep() {
		return MaxStep;
	}

	public void setMaxStep(long maxStep) {
		MaxStep = maxStep;
	}
	public int getLenght() {
		return Lenght;
	}

	public void setLenght(int lenght) {
		Lenght = lenght;
	}

	public int getLenghts() {
		return Lenghts;
	}

	public void setLenghts(int lenghts) {
		Lenghts = lenghts;
	}

	public boolean getIs3D() {
		return is3D;
	}

	public void setIs3D(boolean is3d) {
		is3D = is3d;
	}
	@Override
	public String toString() {
		return "GeneralParam [Width=" + Width + ", Height=" + Height
				+ ", Lenght=" + Lenght + ", Rows=" + Rows + ", Columns=" + Columns + ", Lenghts="+ Lenghts+
				", AOI=" + Aoi + ", P="+ P + ", NumAgents=" + NumAgents + ", Mode=" + Mode
				+ ", MaxStep=" + MaxStep + ", ip=" + ip + ", port=" + port
				+ ", i=" + i + ", j=" + j +", z="+z+ ", isBatch=" + isBatch +", is3D="+is3D+ "]";
	}



}

