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

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("paramRange")
public class ParamRange extends Param 
{
	private String start;
	private String end;
	private String increment;
	
	public ParamRange(String name, String type, int runs, String start,String end, String increment) 
	{
		super(name, type, runs, "range");
		// TODO Auto-generated constructor stub
		
		this.start = start;
		this.end = end;
		this.increment = increment;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}

	public String getIncrement() {
		return increment;
	}

	public void setIncrement(String increment) {
		this.increment = increment;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString()+" start: "+start+" end: "+end+" increment: "+increment;
	}
	
	
	
	
	
	

}
