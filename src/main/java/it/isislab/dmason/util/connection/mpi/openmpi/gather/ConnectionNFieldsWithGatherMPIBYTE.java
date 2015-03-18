package it.isislab.dmason.util.connection.mpi.openmpi.gather;


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

import it.isislab.dmason.util.connection.Address;
import it.isislab.dmason.util.connection.MyHashMap;
import it.isislab.dmason.util.connection.mpi.ConnectionMPI;
import it.isislab.dmason.util.connection.mpi.MPIMessageListener;
import it.isislab.dmason.util.connection.mpi.MPITopic;
import it.isislab.dmason.util.connection.mpi.MPITopicMessage;

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

public class ConnectionNFieldsWithGatherMPIBYTE implements ConnectionMPI 
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

	private ArrayList<Group> nodes_union_group;
	private HashMap<Integer,Intracomm> nodes_union_comm;

	private ArrayList<TreeSet<Integer>> nodes_union_tree;

	public ConnectionNFieldsWithGatherMPIBYTE() throws MPIException {
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

		nodes_union_comm=new HashMap<Integer,Intracomm>();
		nodes_union_tree=new ArrayList<TreeSet<Integer>>();
		for (int i = 0; i < MPI.COMM_WORLD.getSize(); i++) {
			nodes_union_tree.add(new TreeSet<Integer>());
		}
	}
	public void close() throws MPIException 
	{
		MPI.Finalize();
	}
	@Override
	public boolean createTopic(String topicName, int numFields)
	{
		try
		{

			MPITopic topic = new MPITopic(topicName);
			topic.getGroup().add(MY_RANK);
			topic.getPublisher().add(MY_RANK);

			topics.put(topicName,topic);

			contObj.put(topicName, new MyHashMap(numFields));
			created.put(topicName, topic);
			return true;
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
			mh.put(key, object);
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
					int TURN=0;
					do
					{
						int[] sizes=new int[nodes_union_comm.get(TURN).getSize()];
						int[] tosend=new int[1];
						if(TURN==MY_RANK)
						{
							tosend[0]=1;

							ArrayList<MPITopicMessage> wrapper=new ArrayList<MPITopicMessage>();
							ByteArrayOutputStream output2 = new ByteArrayOutputStream();
							ObjectOutputStream stream2 = new ObjectOutputStream(output2);
							stream2.writeObject(wrapper);
							output2.close();
							stream2.close();
							byte[] bytes=output2.toByteArray();

							int size[]={bytes.length};
							
							nodes_union_comm.get(TURN).allGather(
									size,
									//0,
									1,
									MPI.INT,
									sizes,
									//0, 
									sizes.length,
									MPI.INT
									);
							
							int totalSize=0;
							int[] displa=new int[sizes.length];
							displa[0]=0;
							totalSize+=sizes[0];
							for (int i = 1; i < sizes.length; i++) {
								totalSize+=sizes[i];
								displa[i]=displa[i-1]+sizes[i-1];
								
							}
							
							//RICEVO GATHER
							byte byte_recv[]=new byte[totalSize];
							nodes_union_comm.get(TURN).gather(
									bytes,
									//0,
									bytes.length,
									MPI.BYTE,
									byte_recv,
									//0,
									totalSize,//sizes,
									//displa,
									MPI.BYTE,
									nodes_union_comm.get(TURN).getRank()
									);
							

							for (int i = 0; i < displa.length; i++) {
								int start=displa[i];
								int end=i+1<displa.length?displa[i+1]:byte_recv
										.length;
								//								 if(i!=nodes_union_comm.get(TURN).Rank())
								{	

									byte[] tmp=new byte[sizes[i]];
									int k=0;
									for (int j = start; j < end; j++) {
										tmp[k]=byte_recv[j];
										//										System.out.print(Byte.toString(byte_recv[j]));
										k++;
									}

									ByteArrayInputStream byte_stream=new ByteArrayInputStream(tmp);
									//byte_stream.setBuf(tmp);	
									ObjectInputStream stream=new ObjectInputStream(byte_stream);
									Object deSerialized=stream.readObject();
									ArrayList<MPITopicMessage> received=(ArrayList<MPITopicMessage>) deSerialized;
									for(MPITopicMessage message:received)
										listeners.get(message.topic).onMessage(message.message);


								}
							}
							//							ByteInputStream byte_stream=new ByteInputStream();
							//							byte_stream.setBuf(byte_recv);
							//							ObjectInputStream stream=new ObjectInputStream(byte_stream);
							//							for (int i = 0; i < displa.length; i++) {
							//								if(i!=nodes_union_comm.get(TURN).Rank())
							//								{	
							//									stream.readObject();
							//								}
							//								else stream.readObject();
							//							}

						}
						else
						{
							//INVIO GATHER
							Intracomm uCom=nodes_union_comm.get(TURN);
							if(uCom!=null)
							{
								int ranks[]=new int[MPI.COMM_WORLD.getSize()];
								ranks=mpi.Group.translateRanks(
										MPI.COMM_WORLD.getGroup(),
										all_rank,
										uCom.getGroup());

								ArrayList<MPITopicMessage> wrapper=new ArrayList<MPITopicMessage>();
								for(String topic : created.keySet())
								{
									MPITopic topic_i_creat=created.get(topic);

									if(topic_i_creat.getGroup().contains(TURN))
									{
										wrapper.add(new MPITopicMessage(
												topic_i_creat.getTopic(),
												contObj.get(topic_i_creat.getTopic()))
												);

									}
								}
								ByteArrayOutputStream output = new ByteArrayOutputStream();
								ObjectOutputStream stream = new ObjectOutputStream(output);
								stream.writeObject(wrapper);
								output.close();
								byte[] bytes=output.toByteArray();
								int size[]={bytes.length};
								
								
								uCom.allGather(
										size,
										1,
									//	1,
										MPI.INT,
										sizes,
										sizes.length, 
									//	1,
										MPI.INT
										);


								int totalSize=0;
								int[] displa=new int[sizes.length];
								displa[0]=0;
								totalSize+=sizes[0];
								for (int i = 1; i < sizes.length; i++) {
									totalSize+=sizes[i];
									displa[i]=displa[i-1]+sizes[i-1];

								}

								byte byte_recv[]=new byte[totalSize];
								uCom.gather(
										bytes,
									//	0,
										bytes.length,
										MPI.BYTE,
										byte_recv,
									//	0,
										totalSize,//sizes,
										//displa,
										MPI.BYTE,
										ranks[TURN]
										);

							}
						}
						TURN++;

					}while(TURN!=MPI.COMM_WORLD.getSize());

					for (String topicReset : created.keySet()) {
						created.get(topicReset).setMessage(null);
						MyHashMap mm = new MyHashMap(mh.NUMBER_FIELDS);
						contObj.put(topicReset, mm);
					}
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

	@Override
	public boolean setupConnection(Address providerAddr) throws Exception
	{
		
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
							topics.get(topic).getGroup().add(id);

						for(int id : publisher)
							topics.get(topic).getPublisher().add(id);

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

			log.info("["+MY_RANK+"] building the group for topic "+topicPicked.getTopic());
			ordered_topics[i]=key;
			i++;

			TreeSet<Integer> group=topics.get(topicPicked.getTopic()).getGroup();
			int group_to_array[] = new int[group.size()];
			int j=0;
			for (Integer integer : group) {
				group_to_array[j]=integer;
				j++;
			}

			//			Group origin_group=null;
			//			Group new_group=null;
			//			Intracomm comm=null;
			//			origin_group = MPI.COMM_WORLD.Group();
			//			new_group = origin_group.Incl(group_to_array);
			//			comm = MPI.COMM_WORLD.Creat(new_group);

			//			topicPicked.setComm(comm);

			for(Integer node : topicPicked.getGroup())
			{
				TreeSet<Integer> tree_node=nodes_union_tree.get(node);
				tree_node.addAll(topicPicked.getGroup());
			}
			//	MPI.COMM_WORLD.Barrier();
		}
		int n=0;
		for(TreeSet<Integer> tree_node:nodes_union_tree)
		{
			int group_to_array[] = new int[tree_node.size()];
			int j=0;
			for (Integer integer : tree_node) {
				group_to_array[j]=integer;
				j++;
			}

			Group origin_group=null;
			Group new_union_group=null;
			Intracomm union_comm=null;
			origin_group = MPI.COMM_WORLD.getGroup();
			new_union_group = origin_group.incl(group_to_array);
			union_comm = MPI.COMM_WORLD.create(new_union_group);		
			nodes_union_comm.put(n,union_comm);
			//
			n++;
			MPI.COMM_WORLD.barrier();
		}

		return true;
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
		return "Connection type: GATHER";
	}
	@Override
	public boolean unsubscribe(String topicName) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

}
