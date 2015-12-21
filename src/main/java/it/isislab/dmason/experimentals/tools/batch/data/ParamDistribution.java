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

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class ParamDistribution extends Param 
{

	@XStreamAsAttribute
	private String distributionName;
	@XStreamAsAttribute
	private int numberOfValues;
	
	public ParamDistribution(String name, String type, int runs, String distName,int numOfVal) 
	{
		super(name, type, runs, "distribution");
		// TODO Auto-generated constructor stub
		this.distributionName = distName;
		this.numberOfValues = numOfVal;
	}

	public String getDistributionName() {
		return distributionName;
	}

	public void setDistributionName(String distributionName) {
		this.distributionName = distributionName;
	}
	
	public int getNumberOfValues() {
		return numberOfValues;
	}

	public void setNumberOfValues(int numberOfValues) {
		this.numberOfValues = numberOfValues;
	}

	@Override
	public String toString() {
		return super.toString() + " #Values: "+numberOfValues;
	}
	
	

}
