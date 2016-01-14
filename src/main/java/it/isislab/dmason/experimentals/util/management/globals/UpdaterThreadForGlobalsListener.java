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

package it.isislab.dmason.experimentals.util.management.globals;

import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.MessageListener;
import it.isislab.dmason.sim.field.UpdaterThreadForListener;
import it.isislab.dmason.util.connection.jms.ConnectionJMS;

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
 */
public class UpdaterThreadForGlobalsListener extends UpdaterThreadForListener
{
	/**
	 * 
	 */

	public UpdaterThreadForGlobalsListener(ConnectionJMS con,String topic,ArrayList<DistributedField2D> fields, ArrayList<MessageListener> listeners) {

		super( con, topic, fields,  listeners);

	}

	@Override
	public void run()
	{
		try 
		{
			MessageListenerGlobals m = new MessageListenerGlobals(fields, topic);
			listeners.add(m);
			con.asynchronousReceive(topic,m);
		} catch (Exception e) { e.printStackTrace(); }
	}
}