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

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ParamDistributionNormal")
public class ParamDistributionNormal extends ParamDistribution 
{

	private String mean;
	private String stdDev;
	
	public ParamDistributionNormal(String name, String type, int runs,
			String mean, String stdDev, int numOfVal) {
		super(name, type, runs, "normal", numOfVal);
		// TODO Auto-generated constructor stub
		this.mean = mean;
		this.stdDev = stdDev;
	}

	public String getMean() {
		return mean;
	}

	public void setMean(String mean) {
		this.mean = mean;
	}

	public String getStdDev() {
		return stdDev;
	}

	public void setStdDev(String stdDev) {
		this.stdDev = stdDev;
	}

	@Override
	public String toString() {
		return super.toString()+ " Normal [mean=" + mean + ", stdDev=" + stdDev
				+ "]";
	}

	
}
