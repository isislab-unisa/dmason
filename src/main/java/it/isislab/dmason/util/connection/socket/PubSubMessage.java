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
package it.isislab.dmason.util.connection.socket;

import java.io.Serializable;

/** 
 * Wrapper for a message delivered using Socket implementation of Publish/Subscribe paradigm.
 * The message is three-parts divided :
 * part1 = command like 'publish' and 'subscribe'
 * part2 = topic's name
 * part3 = if we are publishing this field contains the body of the message,an Object, otherwise it's set null.
 *
 *
 * 
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 *
 */
public class PubSubMessage implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String part1;
	private String part2;
	private Object part3;
	
	/** Costructor using fields. */
	public PubSubMessage(String part1, String part2, Object part3) {
		super();
		this.part1 = part1;
		this.part2 = part2;
		this.part3 = part3;
	}

	/** Return the String identifying the command */
	public String getPart1() {
		return part1;
	}
	
	/** Set the value of part1 */
	public void setPart1(String part1) {
		this.part1 = part1;
	}

	/** Return the topic's name String */
	public String getPart2() {
		return part2;
	}

	/** Set the value of part2 */
	public void setPart2(String part2) {
		this.part2 = part2;
	}

	/** Return the value of part3 */
	public Object getPart3() {
		return part3;
	}

	/** Set the value of part3 */
	public void setPart3(Object part3) {
		this.part3 = part3;
	}
}
