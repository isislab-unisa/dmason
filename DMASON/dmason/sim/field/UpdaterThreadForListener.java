/**
 * Copyright 2012 Università degli Studi di Salerno


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

package dmason.sim.field;

import java.io.Serializable;
import java.util.ArrayList;
import dmason.util.connection.ConnectionWithJMS;

/**
 * A Thread than instances a listener.
 * It's necessary to not block main thread.
 * @param <E> the type of coordinates
 * @param <F> the type of locations
 */
public class UpdaterThreadForListener extends Thread implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ConnectionWithJMS con;
	CellType type;
	ArrayList<DistributedField> fields;
	ArrayList<MessageListener> listeners;
	public String topic;


	public UpdaterThreadForListener(ConnectionWithJMS con,String topic,ArrayList<DistributedField> fields, ArrayList<MessageListener> listeners) {

		this.con=con;
		this.fields=fields;
		this.topic=topic;
		this.listeners = listeners;
	
	}

	public void run()
	{
		try 
		{
			//System.out.println("TOPIC"+topic);
			MessageListener m = new MessageListener(fields, topic);
			listeners.add(m);
			con.asynchronousReceive(topic,m);
		} catch (Exception e) { e.printStackTrace(); }
	}
}