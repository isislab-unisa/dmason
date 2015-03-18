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

package it.isislab.dmason.test.sim.field;

import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.test.util.connection.VirtualMessageListener;
import it.isislab.dmason.util.connection.jms.ConnectionJMS;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A Thread than instances a listener.
 * It's necessary to not block main thread.
 * @param <E> the type of coordinates
 * @param <F> the type of locations
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
public class FakeUpdaterThreadForListener extends Thread implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected ConnectionJMS con;
	CellType type;
	protected ArrayList<DistributedField2D> fields;
	protected ArrayList<VirtualMessageListener> listeners;
	public String topic;


	public FakeUpdaterThreadForListener(ConnectionJMS con,String topic,ArrayList<DistributedField2D> fields, ArrayList<VirtualMessageListener> listeners) {

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
			VirtualMessageListener m = new VirtualMessageListener(fields, topic);
			listeners.add(m);
			con.asynchronousReceive(topic,m);
		} catch (Exception e) { e.printStackTrace(); }
	}
}