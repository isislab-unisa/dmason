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

package it.isislab.dmason.util.connection.mpi;

import it.isislab.dmason.annotation.AuthorAnnotation;

import java.io.Serializable;

/**
 * A Thread than instances a listener.
 * It's necessary to not block main thread.
 */
  //* @param <E> the type of coordinates
  // * @param <F> the type of locations
@AuthorAnnotation(
		author = {"Ada Mancuso","Francesco Milone","Carmine Spagnuolo"},
		date = "6/3/2014"
		)
public class UpdaterThreadForMPINetworkListener extends Thread implements Serializable
{
	/**
	 * 
	 */
//	private static final long serialVersionUID = 1L;
//	protected ConnectionMPI con;
//	CellType type;
//	protected ArrayList<DNetwork> fields;
//	protected ArrayList<DNetworkMPIMessageListener> listeners;
//	public String topic;
//
//
//	public UpdaterThreadForMPINetworkListener(ConnectionMPI con,String topic,ArrayList<DNetwork> fields, ArrayList<DNetworkMPIMessageListener> listeners) {
//
//		this.con=con;
//		this.fields=fields;
//		this.topic=topic;
//		this.listeners = listeners;
//	
//	}
//
//	@Override
//	public void run()
//	{
//		try 
//		{
//			DNetworkMPIMessageListener m = new DNetworkMPIMessageListener(fields, topic);
//			listeners.add(m);
//			if(con.asynchronousReceive(topic,m)==false)
//				throw new Exception("Error in saving listener foro topic "+topic);
//		} catch (Exception e) { e.printStackTrace(); }
//	}
}