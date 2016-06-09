/**
 * Copyright 2016 Universita' degli Studi di Salerno


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
package it.isislab.dmason.annotation;

public class ValueAnnotation 
{
	private String domain;
	private String suggestedValue;
	
	public ValueAnnotation(String name, String value) {
		super();
		this.domain = name;
		this.suggestedValue = value;
	}
	
	public String getDomain() {
		return domain;
	}
	public void setDomain(String name) {
		this.domain = name;
	}
	
	public String getSuggestedValue() {
		return suggestedValue;
	}
	public void setSuggestedValue(String value) {
		this.suggestedValue = value;
	}
	
}
