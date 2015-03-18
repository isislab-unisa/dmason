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
package it.isislab.dmason.util.trigger;

import it.isislab.dmason.util.connection.MyHashMap;
import it.isislab.dmason.util.connection.jms.activemq.MyMessageListener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.JTextArea;

import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.log4j.Logger;
/**
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 */
public class TriggerListener extends MyMessageListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextArea area;
	private Logger logger;

	public TriggerListener(JTextArea textArea, org.apache.log4j.Logger logger2){
		area = textArea;
		this.logger = logger2;
	}
	
	@Override
	public void onMessage(Message arg0) {
		
		ActiveMQObjectMessage obj = (ActiveMQObjectMessage)arg0;
		try {
			MyHashMap mh = (MyHashMap)obj.getObject();
			if (mh.get("trigger")!=null){
				
				String message = (String) mh.get("trigger");
				area.append(message+"\n");
				logger.info(message);
			}
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Problems with casting....maybe.");
			e.printStackTrace();
		}

	}

}
