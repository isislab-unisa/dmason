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

@XStreamAlias("ParamDistributionUniform")
public class ParamDistributionUniform extends ParamDistribution 
{

	private String a,b;
	
	public ParamDistributionUniform(String name, String type, int runs, String a, String b,int numOfVal) {
		super(name, type, runs,"uniform",numOfVal);
		// TODO Auto-generated constructor stub
		this.a = a;
		this.b = b;
	}

	public String getA() {
		return a;
	}

	public void setA(String a) {
		this.a = a;
	}

	public String getB() {
		return b;
	}

	public void setB(String b) {
		this.b = b;
	}

	@Override
	public String toString() {
		return super.toString()+ " Uniform [a=" + a + ", b=" + b + "]";
	}
	
	

}
