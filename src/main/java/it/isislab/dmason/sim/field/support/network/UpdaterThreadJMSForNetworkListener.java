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

package it.isislab.dmason.sim.field.support.network;

import it.isislab.dmason.annotation.AuthorAnnotation;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.network.DNetwork;
import it.isislab.dmason.util.connection.jms.ConnectionJMS;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A Thread than instances a listener.
 * It's necessary to not block main thread.
 */
@AuthorAnnotation(
		author = {"Ada Mancuso","Francesco Milone","Carmine Spagnuolo"},
		date = "6/3/2014"
		)
public class UpdaterThreadJMSForNetworkListener extends Thread implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected ConnectionJMS con;
	CellType type;
	protected ArrayList<DNetwork> fields;
	protected ArrayList<DNetworkJMSMessageListener> listeners;
	public String topic;


	public UpdaterThreadJMSForNetworkListener(ConnectionJMS con,String topic,ArrayList<DNetwork> fields, ArrayList<DNetworkJMSMessageListener> listeners) {

		this.con=con;
		this.fields=fields;
		this.topic=topic;
		this.listeners = listeners;
	
	}

	@Override
	public void run()
	{
		try 
		{
			DNetworkJMSMessageListener m = new DNetworkJMSMessageListener(fields, topic);
			listeners.add(m);
			con.asynchronousReceive(topic,m);
		} catch (Exception e) { e.printStackTrace(); }
	}
}