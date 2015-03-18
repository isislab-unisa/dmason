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
package it.isislab.dmason.util.connection.jms;

import it.isislab.dmason.util.connection.Connection;
import it.isislab.dmason.util.connection.jms.activemq.MyMessageListener;

import java.util.HashMap;

import javax.jms.JMSException;

/**
 * This interface extends Connection for a purpose : while many programs have to only receive message and
 * callback the program with the result , others more complex applications could might perform different
 * operations depending on the type of message. So extending Connection with this interface developers
 * can set a different listener for any topic.
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
public interface ConnectionJMS extends Connection{
	
	/** Allow client to to receive in asynchronous way messages and to customize listeners for every topic. */
	public boolean asynchronousReceive(String arg0,MyMessageListener arg1);
	
	public void setTable(HashMap table);
	public void close() throws JMSException;
	
}
