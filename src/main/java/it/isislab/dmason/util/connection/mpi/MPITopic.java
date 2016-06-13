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



import it.isislab.dmason.util.connection.MyHashMap;

import java.io.Serializable;
import java.util.TreeSet;

/**
 * @author Michele Carillo
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 *
 */
public class MPITopic implements Serializable {
	
	private String topic;
	private TreeSet<Integer> publisher;
	private TreeSet<Integer> group;
	private MyHashMap message;
	//private Intracomm comm=null;
	
//	public Intracomm getComm() {
//		return comm;
//	}
//
//	public void setComm(Intracomm comm) {
//		this.comm = comm;
//	}
	private int[] group_array;
	public void setGroupArray(int[] group_to_array) {
		this.group_array=group_to_array;
		
	}
	public int[] getGroupArray() {
		return this.group_array;
	}
	public MPITopic(String topic_name) {
		publisher = new TreeSet<Integer>();
		group = new TreeSet<Integer>();
		topic=topic_name;
		message=null;
	}

	public TreeSet<Integer> getGroup() {
		return group;
	}

	public void setGroup(TreeSet<Integer> group) {
		this.group = group;
	}

	public String getTopic() {
		return topic;
	}
	public String toString()
	{
		return "TopicName ["+topic+"]"+" Group ["+group+"]"+" Publisher["+publisher+"]";
	}

	public void setMessage(MyHashMap myHashMap) {
		// TODO Auto-generated method stub
		this.message=myHashMap;
	}

	public MyHashMap getMessage() {
		return message;
	}
	
	public void addPublisher(int pub)
	{
		publisher.add(pub);
	}
	
	public TreeSet<Integer> getPublisher()
	{
		return publisher;
	}

	
}
