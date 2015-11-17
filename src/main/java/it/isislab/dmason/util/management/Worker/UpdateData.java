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

package it.isislab.dmason.util.management.Worker;

import it.isislab.dmason.util.connection.Address;

import java.io.Serializable;

/**
 * This class wraps information to send to worker for jar deploy
 * @author marvit
 *
 */
public class UpdateData implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6723446025965349035L;

	/**
	 * Name of the simulation jar to download
	 */
	private String jarName;
	
	/**
	 * Address of FTP Server
	 */
	private Address FTPAddress;

	public UpdateData(String jarName, Address fTPAddress) {
		super();
		this.jarName = jarName;
		FTPAddress = fTPAddress;
	}

	public String getJarName() {
		return jarName;
	}

	public void setJarName(String jarName) {
		this.jarName = jarName;
	}

	public Address getFTPAddress() {
		return FTPAddress;
	}

	public void setFTPAddress(Address fTPAddress) {
		FTPAddress = fTPAddress;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		super.clone();
		
		return new UpdateData(jarName, FTPAddress);
	}

}
