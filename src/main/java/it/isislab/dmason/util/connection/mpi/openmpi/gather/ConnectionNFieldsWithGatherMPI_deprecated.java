package it.isislab.dmason.util.connection.mpi.openmpi.gather;

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



public class ConnectionNFieldsWithGatherMPI_deprecated
{
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = -3803252417146440187L;
//	private Logger log;
//	private int MY_RANK;
//	private HashMap<String, MyHashMap> contObj;
//	private TreeMap<String,MPITopic> topics;
//
//	private String[] ordered_topics;
//	private int[] all_rank;
//	private TreeMap<String, MPITopic> subscritted;
//	private TreeMap<String,MPITopic> created;
//	private HashMap<String, MPIMessageListener> listeners;
//
//	private ArrayList<Group> nodes_union_group;
//	private HashMap<Integer,Intracomm> nodes_union_comm;
//
//	private ArrayList<TreeSet<Integer>> nodes_union_tree;
//
//	public ConnectionNFieldsWithGatherMPI(String[] args) throws MPIException {
//		contObj = new HashMap<String, MyHashMap>();
//		topics = new TreeMap<String, MPITopic>();
//
//		subscritted = new TreeMap<String, MPITopic>();
//		created = new TreeMap<String, MPITopic>();
//		listeners=new HashMap<String, MPIMessageListener>();
//		log= Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
//
//		MY_RANK=MPI.COMM_WORLD.getRank();
//		all_rank = new int[MPI.COMM_WORLD.getSize()];
//		for(int i=0; i<MPI.COMM_WORLD.getSize(); i++)
//		{
//			all_rank[i]=i;
//		}
//
//		nodes_union_comm=new HashMap<Integer,Intracomm>();
//		nodes_union_tree=new ArrayList<TreeSet<Integer>>();
//		for (int i = 0; i < MPI.COMM_WORLD.getSize(); i++) {
//			nodes_union_tree.add(new TreeSet<Integer>());
//		}
//	}
//	public void close() throws MPIException 
//	{
//		MPI.Finalize();
//	}
//	public boolean createTopic(String topicName, int numFields)
//	{
//		try
//		{
//
//			MPITopic topic = new MPITopic(topicName);
//			topic.getGroup().add(MY_RANK);
//			topic.getPublisher().add(MY_RANK);
//
//			topics.put(topicName,topic);
//
//			contObj.put(topicName, new MyHashMap(numFields));
//			created.put(topicName, topic);
//			return true;
//		} catch (Exception e) {
//			System.err.println("Unable to create topic: " + topicName);
//			e.printStackTrace();
//			return false;
//		}
//	}
//
//	@Override
//	public boolean subscribeToTopic(String topicName) throws Exception{
//		try
//		{
//			if(topics.get(topicName)==null)
//			{
//				MPITopic topic = new MPITopic(topicName);
//				topic.getGroup().add(MY_RANK);
//				topics.put(topicName,topic);
//				subscritted.put(topicName, topic);
//			}else topics.get(topicName).getGroup().add(MY_RANK);
//
//			return true;
//		} catch (Exception e) {
//			System.err.println("Unable to subscribe to topic: " + topicName);
//			e.printStackTrace();
//			return false;
//		}
//	}
//
//
//
//	@Override
//	public synchronized boolean publishToTopic(Serializable object, String topicName, String key)
//	{
//		
//		if (!topicName.equals("") || !(object == null))
//		{
//			MyHashMap mh = contObj.get(topicName);
//			mh.put(key, (Object)object);
//			contObj.put(topicName, mh);
//			created.get(topicName).setMessage(contObj.get(topicName));
//
//			boolean send_now=true;
//			for (String t : created.keySet()) {
//				if(created.get(t).getMessage()==null || !contObj.get(t).isFull())
//				{
//					send_now=false;
//					break;
//				}
//			} 
//
//			if(/* mh.isFull() && */ send_now)
//			{		
//
//
//				try{
//					int TURN=0;
//					do
//					{
//
//						if(TURN==MY_RANK)
//						{
//							//RICEVO GATHER
//							Object objs_send[]=new Object[1];
//							Object objs_recv[]=new Object[nodes_union_comm.get(TURN).getSize()];
//							//							System.out.println(TURN+"] "+MY_RANK+" IN RICEZIONE! root "+nodes_union_comm.get(TURN).Rank());
//							nodes_union_comm.get(TURN).Gather(
//									objs_send,
//									0,
//									1,
//									MPI.OBJECT,
//									objs_recv,
//									0, 
//									1,
//									MPI.OBJECT,
//									nodes_union_comm.get(TURN).getRank()
//									);
//
//
//							//							System.out.println(TURN +"] Ricevuto "+objs_recv+" dal nodo "+MY_RANK);
//							for (int i = 0; i < objs_recv.length; i++) {
//								if(i!=nodes_union_comm.get(TURN).getRank())
//								{	
//									ArrayList<MPITopicMessage> received=(ArrayList<MPITopicMessage>) objs_recv[i];
//									for(MPITopicMessage message:received)
//										listeners.get(message.topic).onMessage(message.message);
//									//	System.out.println(i+" "+received);
//								}
//
//							}
//						}
//						else
//						{
//							//INVIO GATHER
//							Intracomm uCom=nodes_union_comm.get(TURN);
//							if(uCom!=null)
//							{
//
//								int ranks[]=new int[MPI.COMM_WORLD.getSize()];
//								ranks=mpi.Group.translateRanks(
//										MPI.COMM_WORLD.getGroup(),
//										all_rank,
//										uCom.getGroup());
//
//								//								System.out.println(TURN+"] "+MY_RANK+" IN INVIO!" + nodes_union_tree.get(TURN)+" root "+ranks[TURN]);
//
//								ArrayList<MPITopicMessage> wrapper=new ArrayList<MPITopicMessage>();
//								for(String topic : created.keySet())
//								{
//									MPITopic topic_i_creat=created.get(topic);
//
//									if(topic_i_creat.getGroup().contains(TURN))
//									{
//										wrapper.add(new MPITopicMessage(
//												topic_i_creat.getTopic(),
//												contObj.get(topic_i_creat.getTopic()))
//												);
//
//									}
//								}
//								Object objs_send[]=new Object[1];
//								Object objs_recv[]=new Object[nodes_union_comm.get(TURN).getSize()];
//
//								objs_send[0]=wrapper;
//
//								
//								nodes_union_comm.get(TURN).Gather(
//										objs_send,
//										0,
//										1,
//										MPI.OBJECT,
//										objs_recv,
//										0, 
//										1,
//										MPI.OBJECT,
//										ranks[TURN]
//										);
//							}
//						}
//						TURN++;
//
//					}while(TURN!=MPI.COMM_WORLD.getSize());
//
//					for (String topicReset : created.keySet()) {
//						created.get(topicReset).setMessage(null);
//						MyHashMap mm = new MyHashMap(mh.NUMBER_FIELDS);
//						contObj.put(topicReset, mm);
//					}
//				} catch (Exception e) {
//					System.err.println("Can't publish:" + "\n"
//							+ "    topicName: " + topicName          + "\n"
//							+ "    key      : " + key                + "\n"
//							+ "    object   : " + object.toString() );
//					e.printStackTrace();
//					return false;
//				}
//			}	
//		}
//
//		return false;
//	}
//
//	@Override
//	public boolean asynchronousReceive(String topic, MPIMessageListener listener) {
//
//		return listeners.put(topic, listener)==null;
//
//	}
//
//	public boolean setupConnection(Address providerAddr) throws Exception
//	{
//		int recieved=0;
//		do
//		{
//			if(recieved==MY_RANK)
//			{
//				Object[] o = {topics};
//				MPI.COMM_WORLD.Bcast(o, 0, 1, MPI.OBJECT, MY_RANK);
//
//			}
//			else
//			{
//				Object[] o = new Object[1];
//				MPI.COMM_WORLD.Bcast(o, 0, 1, MPI.OBJECT, recieved);
//				TreeMap<String, MPITopic> recieved_topics = (TreeMap<String,MPITopic>) o[0];
//
//				for (String topic : recieved_topics.keySet()) {
//
//					MPITopic current_topic = topics.get(topic);
//					if(current_topic==null)
//					{
//						topics.put(topic, recieved_topics.get(topic));
//					}
//					else
//					{
//						TreeSet<Integer> group = recieved_topics.get(topic).getGroup();
//						TreeSet<Integer> publisher = recieved_topics.get(topic).getPublisher();
//
//						for(int id : group)
//							topics.get(topic).getGroup().add(id);
//
//						for(int id : publisher)
//							topics.get(topic).getPublisher().add(id);
//
//					}//end if current_topic==null
//
//				}//end for over recieved_topics
//
//			}//end if-else recieved==MPI.COMM_WORLD.Rank()
//			recieved++;
//		}while(recieved!=MPI.COMM_WORLD.getSize());
//
//		MPI.COMM_WORLD.barrier();
//		//Now i make the groups! exactly one group per topic and i save the order of topics
//		ordered_topics=new String[topics.size()];
//		int i=0;
//		for(String key : topics.keySet())
//		{
//			MPITopic topicPicked=topics.get(key);
//
//			log.info("["+MY_RANK+"] building the group for topic "+topicPicked.getTopic());
//			ordered_topics[i]=key;
//			i++;
//
//			TreeSet<Integer> group=topics.get(topicPicked.getTopic()).getGroup();
//			int group_to_array[] = new int[group.size()];
//			int j=0;
//			for (Integer integer : group) {
//				group_to_array[j]=integer;
//				j++;
//			}
//
//			//			Group origin_group=null;
//			//			Group new_group=null;
//			//			Intracomm comm=null;
//			//			origin_group = MPI.COMM_WORLD.Group();
//			//			new_group = origin_group.Incl(group_to_array);
//			//			comm = MPI.COMM_WORLD.Creat(new_group);
//
//			//			topicPicked.setComm(comm);
//
//			for(Integer node : topicPicked.getGroup())
//			{
//				TreeSet<Integer> tree_node=nodes_union_tree.get(node);
//				tree_node.addAll(topicPicked.getGroup());
//			}
//			//	MPI.COMM_WORLD.Barrier();
//		}
//		int n=0;
//		for(TreeSet<Integer> tree_node:nodes_union_tree)
//		{
//			int group_to_array[] = new int[tree_node.size()];
//			int j=0;
//			for (Integer integer : tree_node) {
//				group_to_array[j]=integer;
//				j++;
//			}
//			
//			Group origin_group=null;
//			Group new_union_group=null;
//			Intracomm union_comm=null;
//			origin_group = MPI.COMM_WORLD.getGroup();
//			new_union_group = origin_group.incl(group_to_array);
//			union_comm = MPI.COMM_WORLD.create(new_union_group);		
//			nodes_union_comm.put(n,union_comm);
////
////			if(MY_RANK==0)
////				System.out.println("Gruppo "+n+"]"+tree_node+" "+union_comm);
//			n++;
//			MPI.COMM_WORLD.barrier();
//		}
//
//		return true;
//	}
//	@Override
//	public void setLogging(Level level) throws Exception {
//		log.setLevel(level);
//
//	}
//
//	public TreeMap<String,MPITopic> getTopics() {
//		// TODO Auto-generated method stub
//		return topics;
//	}
//
//	@Override
//	public boolean asynchronousReceive(String key) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//	@Override
//	public ArrayList<String> getTopicList() throws Exception {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	@Override
//	public String getConnectionType() {
//		// TODO Auto-generated method stub
//		return "Connection type:GATHER";
//	}
}