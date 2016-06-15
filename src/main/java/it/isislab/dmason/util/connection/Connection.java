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
import java.util.ArrayList;

/**
 * This interface abstracts a communication-layer using publish/subscribe paradigm.
 * Anyone can implement these methods using a preferred way to communicate.
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
public interface Connection {

	/**
	 * Estabilishes a connection with a provider located at given address.
	 * @param providerAddr Address of the provider.
	 * @return true if correct
	 * @throws Exception for exception
	 */
	public boolean setupConnection(Address providerAddr) throws Exception;


	/**
	 * Create an identifier to physical topic using the given string
	 *  
	 * @param topicName name of topic 
	 * @param numFields num of field
	 * @throws Exception for exception
	 * @return true if the topic is created
	 */
	public boolean createTopic(String topicName, int numFields) throws Exception;

	/**
	 * Write a Serializable object to a topic. Using the generic type
	 * Serializable  allows from type indipendency.
	 * @param object The Serializable object to publish.
	 * @param topicName The name of the topic where <code>obejct</code> will be published.
	 * @param key A string associated to the <code>object</code>.
	 * @throws Exception for exception
	 * @return true if the publish is completed
	 */
	public boolean publishToTopic(Serializable object, String topicName, String key) throws Exception;

	/**
	 * Subscribes the peer to a topic named as the given string.
	 * @param topicName Name of the topic to subscribe to.
	 * @throws Exception for exception
	 * @return true if the subscription is completed
	 */
	public boolean subscribeToTopic(String topicName) throws Exception;

	/**
	 * Allow client to to receive messages asynchronously.
	 * @param key A string associated to the object to receive.
	 * @return true if correct 
	 */
	public boolean asynchronousReceive(String key);

	/**
	 * Retrieves the list of active topics on the provider.
	 * @throws Exception for exception
	 * @return list of topic
	 */
	public ArrayList<String> getTopicList() throws Exception;

	/**
	 * Unsubscribe from a topic
	 * 
	 * @param topicName name of topic
	 * @throws Exception for exception
	 * @return true if correct
	 */
	public boolean unsubscribe(String topicName) throws Exception;

}
