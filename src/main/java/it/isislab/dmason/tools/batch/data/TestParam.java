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
package it.isislab.dmason.tools.batch.data;

import java.util.List;

public class TestParam
{
	private GeneralParam genParams;
	private List<EntryParam<String, Object>> simParams;
	public TestParam(GeneralParam genParams,
			List<EntryParam<String, Object>> simParams) {
		super();
		this.genParams = genParams;
		this.simParams = simParams;
	}
	public TestParam() {
		// TODO Auto-generated constructor stub
	}
	public GeneralParam getGenParams() {
		return genParams;
	}
	public void setGenParams(GeneralParam genParams) {
		this.genParams = genParams;
	}
	public List<EntryParam<String, Object>> getSimParams() {
		return simParams;
	}
	public void setSimParams(List<EntryParam<String, Object>> simParams) {
		this.simParams = simParams;
	}
	
	

}
