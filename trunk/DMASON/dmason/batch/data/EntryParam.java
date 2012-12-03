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



public class EntryParam<V, B> implements Serializable{

	public enum ParamType {
	    SIMULATION, GENERAL
	}
	
	private String paramName;
	private Object paramValue;
	private ParamType type;

	public EntryParam() {
	}

	

	public EntryParam(String paramName, Object paramValue, ParamType type) {
		super();
		this.paramName = paramName;
		this.paramValue = paramValue;
		this.type = type;
	}



	public ParamType getType() {
		return type;
	}



	public void setType(ParamType type) {
		this.type = type;
	}



	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public Object getParamValue() {
		return paramValue;
	}

	public void setParamValue(Object paramValue) {
		this.paramValue = paramValue;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "<" + paramName + "," + paramValue + "," + type + ">";
	}

}