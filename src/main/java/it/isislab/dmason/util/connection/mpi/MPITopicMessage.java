package it.isislab.dmason.util.connection.mpi;



import java.io.Serializable;

public class MPITopicMessage implements Serializable {
	public String topic;
	public Serializable message;
	public MPITopicMessage(String topic,Serializable message) {
		this.topic=topic;
		this.message=message;
	}
}
