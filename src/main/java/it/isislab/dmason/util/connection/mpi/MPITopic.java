package it.isislab.dmason.util.connection.mpi;



import it.isislab.dmason.util.connection.MyHashMap;

import java.io.Serializable;
import java.util.TreeSet;


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
