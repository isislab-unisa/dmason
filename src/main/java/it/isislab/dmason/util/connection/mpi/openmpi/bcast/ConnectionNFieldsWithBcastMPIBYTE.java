package it.isislab.dmason.util.connection.mpi.openmpi.bcast;


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

import it.isislab.dmason.util.connection.Address;
import it.isislab.dmason.util.connection.MyHashMap;
import it.isislab.dmason.util.connection.mpi.ConnectionMPI;
import it.isislab.dmason.util.connection.mpi.MPIMessageListener;
import it.isislab.dmason.util.connection.mpi.MPITopic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import mpi.Group;
import mpi.Intracomm;
import mpi.MPI;
import mpi.MPIException;


public class ConnectionNFieldsWithBcastMPIBYTE implements ConnectionMPI 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3803252417146440187L;
	private Logger log;
	private int MY_RANK;
	private HashMap<String, MyHashMap> contObj;
	private TreeMap<String,MPITopic> topics;
	private String[] ordered_topics;
	private int[] all_rank;
	private TreeMap<String, MPITopic> subscritted;
	private TreeMap<String,MPITopic> created;
	private HashMap<String, MPIMessageListener> listeners;
	private HashMap<String,Intracomm> communicators=new HashMap<String, Intracomm>();

	public ConnectionNFieldsWithBcastMPIBYTE() throws MPIException {
		contObj = new HashMap<String, MyHashMap>();
		topics = new TreeMap<String, MPITopic>();
		subscritted = new TreeMap<String, MPITopic>();
		created = new TreeMap<String, MPITopic>();
		listeners=new HashMap<String, MPIMessageListener>();
		log= Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

		MY_RANK=MPI.COMM_WORLD.getRank();
		all_rank = new int[MPI.COMM_WORLD.getSize()];
		for(int i=0; i<MPI.COMM_WORLD.getSize(); i++)
		{
			all_rank[i]=i;
		}
	}
	
	public void close() throws MPIException 
	{
		MPI.Finalize();
	}
	public boolean createTopic(String topicName, int numFields)
	{
		try
		{

			if(topics.get(topicName)==null)
			{
				MPITopic topic = new MPITopic(topicName);
				topic.getGroup().add(MY_RANK);
				topic.getPublisher().add(MY_RANK);

				topics.put(topicName,topic);

				contObj.put(topicName, new MyHashMap(numFields));
				created.put(topicName, topic);
				return true;
			}else{
				MPITopic topic = topics.get(topicName);

				topic.getGroup().add(MY_RANK);
				topic.getPublisher().add(MY_RANK);
				contObj.put(topicName, new MyHashMap(numFields));
				created.put(topicName, topic);

				return true;
			}
		} catch (Exception e) {
			System.err.println("Unable to create topic: " + topicName);
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean subscribeToTopic(String topicName) throws Exception{
		try
		{
			if(topics.get(topicName)==null)
			{
				MPITopic topic = new MPITopic(topicName);
				topic.getGroup().add(MY_RANK);
				topics.put(topicName,topic);
				subscritted.put(topicName, topic);
			}else topics.get(topicName).getGroup().add(MY_RANK);

			return true;
		} catch (Exception e) {
			System.err.println("Unable to subscribe to topic: " + topicName);
			e.printStackTrace();
			return false;
		}
	}



	@Override
	public synchronized boolean publishToTopic(Serializable object, String topicName, String key)
	{
		if (!topicName.equals("") || !(object == null))
		{
			MyHashMap mh = contObj.get(topicName);
			mh.put(key, (Object)object);
			contObj.put(topicName, mh);
			created.get(topicName).setMessage(contObj.get(topicName));

			boolean send_now=true;
			for (String t : created.keySet()) {
				if(created.get(t).getMessage()==null || !contObj.get(t).isFull())
				{
					send_now=false;
					break;
				}
			} 

			if(/* mh.isFull() && */ send_now)
			{				
				try{

					for(int TURN=0; TURN<topics.size(); TURN++)
					{
						MPITopic topicPicked=topics.get(ordered_topics[TURN]);

						if(topicPicked.getGroup().contains(MY_RANK))
						{

							for(int publisher : topicPicked.getPublisher())
							{
								Intracomm comm=communicators.get(topicPicked.getTopic());
								if(publisher==MPI.COMM_WORLD.getRank())
								{


									ByteArrayOutputStream output = new ByteArrayOutputStream();
									ObjectOutputStream stream = new ObjectOutputStream(output);
									stream.writeObject(contObj.get(topicName));
									output.close();
									byte[] bytes=output.toByteArray();
									int size[]={bytes.length};
//									topicPicked.getComm().bcast(size, 1, MPI.INT, topicPicked.getComm().getRank());	
//
//									topicPicked.getComm().bcast(bytes, bytes.length, MPI.BYTE, topicPicked.getComm().getRank());	
									
									comm.bcast(size, 1, MPI.INT, comm.getRank());	
									comm.bcast(bytes, bytes.length, MPI.BYTE, comm.getRank());	
								}
								else 
								{
									int ranks[]=new int[MPI.COMM_WORLD.getSize()];
//									ranks=mpi.Group.translateRanks(
//											MPI.COMM_WORLD.getGroup(),
//											all_rank,
//											topicPicked.getComm().getGroup());
									ranks=mpi.Group.translateRanks(
											MPI.COMM_WORLD.getGroup(),
											all_rank,
											comm.getGroup());

									int[] size_r=new int[1];
//									topicPicked.getComm().bcast(size_r,  1,MPI.INT,ranks[publisher]);
									comm.bcast(size_r,  1,MPI.INT,ranks[publisher]);

									byte[] bytes=new byte[size_r[0]];
//									topicPicked.getComm().bcast(bytes,  size_r[0],MPI.BYTE,ranks[publisher]);
									comm.bcast(bytes,  size_r[0],MPI.BYTE,ranks[publisher]);

									ByteArrayInputStream byte_stream=new ByteArrayInputStream(bytes);
									//byte_stream.setBuf(bytes);
									ObjectInputStream stream=new ObjectInputStream(byte_stream);
									listeners.get(topicPicked.getTopic()).onMessage(stream.readObject());
									stream.close();
									log.info("["+MY_RANK+"]"+" received on TOPIC["+topicPicked.getTopic()+"]");
								}
							}
						}
					}

					//MPI.COMM_WORLD.Barrier();

					for (String topicReset : created.keySet()) {
						created.get(topicReset).setMessage(null);
						MyHashMap mm = new MyHashMap(mh.NUMBER_FIELDS);
						contObj.put(topicReset, mm);
					}

					return true;
				} catch (Exception e) {
					System.err.println("Can't publish:" + "\n"
							+ "    topicName: " + topicName          + "\n"
							+ "    key      : " + key                + "\n"
							+ "    object   : " + object.toString() );
					e.printStackTrace();
					return false;
				}
			}	
		}

		return false;
	}

	@Override
	public boolean asynchronousReceive(String topic, MPIMessageListener listener) {

		return listeners.put(topic, listener)==null;

	}

	
	public boolean setupConnection(Address providerAddr) throws Exception
	{
//		String tohash=MY_RANK+"";
//		for(String topic_name:topics.keySet())
//		{
//			tohash+=topic_name;
//		}
//		String md5=UtilConnectionMPI.MD5(tohash);
//		TreeMap<String, MPITopic> tree=UtilConnectionMPI.checkAndGetMPIConfiguration(md5);
//		if(tree==null)
//		{
			int recieved=0;
			do
			{
				if(recieved==MY_RANK)
				{
					Object[] o = {topics};
					ByteArrayOutputStream output2 = new ByteArrayOutputStream();
					ObjectOutputStream stream2 = new ObjectOutputStream(output2);
					stream2.writeObject(o);
					output2.close();
					stream2.close();
					byte[] bytes=output2.toByteArray();

					MPI.COMM_WORLD.bcast(new int[]{bytes.length},1, MPI.INT, MY_RANK);
					MPI.COMM_WORLD.bcast(bytes, bytes.length, MPI.BYTE, MY_RANK);

				}
				else
				{
					int val[]=new int[1];
					MPI.COMM_WORLD.bcast(val,1, MPI.INT, recieved);

					byte[] bytes=new byte[val[0]];
					MPI.COMM_WORLD.bcast(bytes, val[0],  MPI.BYTE, recieved);

					ByteArrayInputStream byte_stream=new ByteArrayInputStream(bytes);
					//byte_stream.setBuf(bytes);	
					ObjectInputStream stream=new ObjectInputStream(byte_stream);
					Object deSerialized=stream.readObject();
					stream.close();
					byte_stream.close();
					Object[]  o=(Object[]) deSerialized;
					//Object[] o = new Object[1];
					TreeMap<String, MPITopic> recieved_topics = (TreeMap<String,MPITopic>) o[0];

					for (String topic : recieved_topics.keySet()) {
						
						MPITopic current_topic = topics.get(topic);

						if(current_topic==null)
						{
							topics.put(topic, recieved_topics.get(topic));
						}
						else
						{
							TreeSet<Integer> group = recieved_topics.get(topic).getGroup();
							TreeSet<Integer> publisher = recieved_topics.get(topic).getPublisher();

							for(int id : group)
								current_topic.getGroup().add(id);

							for(int id : publisher)
								current_topic.getPublisher().add(id);

						}//end if current_topic==null

					}//end for over recieved_topics

				}//end if-else recieved==MPI.COMM_WORLD.Rank()
				recieved++;
			}while(recieved!=MPI.COMM_WORLD.getSize());

			MPI.COMM_WORLD.barrier();
			//Now i make the groups! exactly one group per topic and i save the order of topics
			ordered_topics=new String[topics.size()];
			int i=0;
			for(String key : topics.keySet())
			{
				MPITopic topicPicked=topics.get(key);
				
				ordered_topics[i]=key;
				i++;

				TreeSet<Integer> group=topics.get(topicPicked.getTopic()).getGroup();
				int group_to_array[] = new int[group.size()];
				int j=0;
				for (Integer integer : group) {
					group_to_array[j]=integer;
					j++;
				}

				log.info("["+MY_RANK+"] building the group for topic "+topicPicked.getTopic()+"]["+group+"]");

//				Group origin_group=null;
//				Group new_group=null;
//				Intracomm comm=null;
//				origin_group = MPI.COMM_WORLD.getGroup();
//				new_group = origin_group.incl(group_to_array);
//				comm = MPI.COMM_WORLD.create(new_group);
//
//				topicPicked.setComm(comm);
				topicPicked.setGroupArray(group_to_array);

				MPI.COMM_WORLD.barrier();
			}
			//save tree
//			UtilConnectionMPI.saveMPIConfiguration(md5,topics);
			makeCommunicator();
//		}else{
//			
//			topics=tree;
//			MPI.COMM_WORLD.barrier();
//			makeCommunicator();
//			return true;
//		}
		return true;
	}
	
	private void makeCommunicator() throws MPIException
	{
		for (MPITopic topic : topics.values()) {
			if(topic.getPublisher().contains(MY_RANK))
				created.put(topic.getTopic(),topic);
			communicators.put(topic.getTopic(), getCommunicator(topic.getGroupArray()));
		}
		all_rank = new int[MPI.COMM_WORLD.getSize()];
		for(int i=0; i<MPI.COMM_WORLD.getSize(); i++)
		{
			all_rank[i]=i;
		}
		ordered_topics=new String[topics.size()];
		int i=0;
		for(String key : topics.keySet())
		{
			ordered_topics[i]=key;
			i++;
		}
	}
	private Intracomm getCommunicator(int group[]) throws MPIException
	{
		Group origin_group=null;
		Group new_group=null;
		Intracomm comm=null;
		origin_group = MPI.COMM_WORLD.getGroup();
		new_group = origin_group.incl(group);
		comm = MPI.COMM_WORLD.create(new_group);
		return comm;
	}
	@Override
	public void setLogging(Level level) throws Exception {
		log.setLevel(level);

	}

	public TreeMap<String,MPITopic> getTopics() {
		// TODO Auto-generated method stub
		return topics;
	}

	@Override
	public boolean asynchronousReceive(String key) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public ArrayList<String> getTopicList() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getConnectionType() {
		// TODO Auto-generated method stub
		return "Connection type:BCAST";
	}

	@Override
	public boolean unsubscribe(String topicName) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}






}
