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
package it.isislab.dmason.util.connection;

import java.io.Serializable;

/** Wrapper for network configuration parameters . It provides ip address and port number but 
 * the developer is free to extend the class with additional informations.
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

public class Address implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String IPaddress;
	private String port;
	
	/**
	 * Constructor of Address
	 * @param iPaddress the ip of address
	 * @param port thr port of address
	 */
	public Address(String iPaddress, String port) {
		super();
		IPaddress = iPaddress;
		this.port = port;
	}

	/** Return a String containing IPaddress
	 * 
	 * @return an ip address
	 */
	public String getIPaddress() {
		return IPaddress;
	}
	
	/**
	 * Set, given a value, IPAddress.
	 * @param iPaddress ip to set
	 */
	public void setIPaddress(String iPaddress) {
		IPaddress = iPaddress;
	}
	
	/**Return a String containing port number
	 * 
	 * @return the port number
	 */
	public String getPort() {
		return port;
	}

	/** Set, given a value, port number. 
	 * 
	 * @param port the port to set
	 */
	public void setPort(String port) {
		this.port = port;
	}

	/** Rewriting of Object toString() method. */
	@Override
	public String toString() {
		return "Address [IPaddress=" + IPaddress + ", port=" + port + "]";
	}
	
	
	
	

}
