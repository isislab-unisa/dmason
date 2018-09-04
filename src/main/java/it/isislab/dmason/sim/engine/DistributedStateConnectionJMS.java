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

package it.isislab.dmason.sim.engine;

import it.isislab.dmason.annotation.AuthorAnnotation;
import it.isislab.dmason.experimentals.util.management.globals.UpdaterThreadForGlobalsListener;
import it.isislab.dmason.experimentals.util.management.globals.util.UpdateGlobalVarAtStep;
import it.isislab.dmason.experimentals.util.trigger.Trigger;
import it.isislab.dmason.experimentals.util.visualization.globalviewer.ThreadVisualizationCellMessageListener;
import it.isislab.dmason.experimentals.util.visualization.zoomviewerapp.ThreadZoomInCellMessageListener;
import it.isislab.dmason.nonuniform.QuadTree;
import it.isislab.dmason.nonuniform.QuadTree.ORIENTATION;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.DistributedFieldNetwork;
import it.isislab.dmason.sim.field.MessageListener;
import it.isislab.dmason.sim.field.UpdaterThreadForListener;
import it.isislab.dmason.sim.field.network.DNetwork;
import it.isislab.dmason.sim.field.support.network.DNetworkJMSMessageListener;
import it.isislab.dmason.sim.field.support.network.UpdaterThreadJMSForNetworkListener;
import it.isislab.dmason.sim.field3D.DistributedField3D;
import it.isislab.dmason.sim.field3D.MessageListener3D;
import it.isislab.dmason.sim.field3D.UpdaterThreadForListener3D;
import it.isislab.dmason.util.connection.Address;
import it.isislab.dmason.util.connection.MyHashMap;
import it.isislab.dmason.util.connection.jms.ConnectionJMS;
import it.isislab.dmason.util.connection.jms.activemq.ConnectionNFieldsWithActiveMQAPI;
import it.isislab.dmason.util.connection.jms.activemq.MyMessageListener;
import ncsa.j3d.loaders.vtk.CELL_TYPES;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.pig.parser.AliasMasker.type_cast_return;

/**
 *
 * @param <E> the type of locations
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * @author Luca Vicidomini
 */
public class DistributedStateConnectionJMS<E> {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public String ip;
	public String port;
	public ConnectionJMS connectionJMS;
	private ArrayList<MessageListener> listeners = new ArrayList<MessageListener>();
	private ArrayList<MessageListener3D> listeners3D = new ArrayList<MessageListener3D>();
	protected ArrayList<DNetworkJMSMessageListener> networkListeners = new ArrayList<DNetworkJMSMessageListener>();
	protected DistributedState dm;
	protected Trigger TRIGGER;
	protected DistributedMultiSchedule<E> schedule;
	protected String topicPrefix;
	protected CellType TYPE;
	protected int MODE;
	protected int NUMPEERS;
	protected int rows;
	protected int columns;
	protected int lenghts;
	protected HashMap<String, Integer> networkNumberOfSubscribersForField;
	protected boolean perfTrace;

	public DistributedStateConnectionJMS()
	{

	}

	public DistributedStateConnectionJMS(DistributedState dm, String ip,String port) {
		this.ip = ip;
		this.port = port;
		this.dm=dm;
		connectionJMS = new ConnectionNFieldsWithActiveMQAPI();

		/**
		 * 		try {
		 connectionJMS.setupConnection(new Address(ip, port));
		 } catch (Exception e) {
		 // TODO Auto-generated catch block
		 e.printStackTrace();
		 }
		 this.TRIGGER = new Trigger(connectionJMS);
		 */
		schedule=(DistributedMultiSchedule<E>)dm.schedule;
		topicPrefix=dm.topicPrefix;
		TYPE=dm.TYPE;
		MODE=dm.MODE;
		NUMPEERS=dm.NUMPEERS;
		rows=dm.rows;
		columns=dm.columns;
		if(dm.is3D)
			lenghts=dm.lenghts;
		networkNumberOfSubscribersForField=dm.networkNumberOfSubscribersForField;
		perfTrace = dm.isPerfTrace();
	}

	//	//this is only for test
	//	public DistributedStateConnectionJMS(DistributedState dm, String ip,String port,ConnectionJMS connectionJMS,boolean thisIsATest) {
	//		this.ip = ip;
	//		this.port = port;
	//		this.dm=dm;
	//
	//		this.connectionJMS = new ConnectionNFieldsWithActiveMQAPI();
	//		//this.connectionJMS=connectionJMS;
	//		/**
	//		 * 		try {
	//			connectionJMS.setupConnection(new Address(ip, port));
	//		} catch (Exception e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//		this.TRIGGER = new Trigger(connectionJMS);
	//		 */
	//		schedule=(DistributedMultiSchedule<E>)dm.schedule;
	//		topicPrefix=dm.topicPrefix;
	//		TYPE=dm.TYPE;
	//		MODE=dm.MODE;
	//		NUMPEERS=dm.NUMPEERS;
	//		rows=dm.rows;
	//		columns=dm.columns;
	//		networkNumberOfSubscribersForField=dm.networkNumberOfSubscribersForField;
	//	}

	
	public int CONNECTIONS_CREATED_STATUS_P;
	public boolean CONNECTIONS_CREATED=false;
	public void init_connection() {
		try {
			connectionJMS.setupConnection(new Address(ip, port));
			
			connectionJMS.createTopic("TOPIC_REDUCE", 1);
			connectionJMS.subscribeToTopic("TOPIC_REDUCE");
			
			connectionJMS.asynchronousReceive("TOPIC_REDUCE", new MyMessageListener() {
				
				@Override
				public void onMessage(Message msg) {
					MyHashMap bo = null;
					try {
						bo = (MyHashMap)parseMessage(msg);
					} catch (JMSException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					synchronized (dm.reduceVar) {
						String[] tmp = ((String) bo.get("")).split(",");
						String key = tmp[0];
						if(dm.reduceVar.containsKey(key)) {
							ArrayList tmp_a = (ArrayList) dm.reduceVar.get(key);
							tmp_a.add(tmp[1]);
							dm.reduceVar.put(key,tmp_a);
						}else {
							ArrayList tmp_a = new ArrayList();
							tmp_a.add(tmp[1]);
							dm.reduceVar.put(key,tmp_a);
						}
					}				
				}
			});
			
			if(perfTrace) {
				connectionJMS.createTopic("PERF-TRACE-TOPIC", 1);
			}
			
			if(MODE == DistributedField2D.NON_UNIFORM_PARTITIONING_MODE)
			{
				try {
					connectionJMS.createTopic("CONNECTIONS_CREATED", 1);
					connectionJMS.subscribeToTopic("CONNECTIONS_CREATED");
					connectionJMS.asynchronousReceive("CONNECTIONS_CREATED", new MyMessageListener() {

						@Override
						public void onMessage(Message msg) {
							// TODO Auto-generated method stub
							CONNECTIONS_CREATED_STATUS_P++;
							if(CONNECTIONS_CREATED_STATUS_P==dm.P)
							{
								CONNECTIONS_CREATED=true;
								lock.lock();
								block.signal();
								lock.unlock();
							}
						}
					});
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.TRIGGER = new Trigger(connectionJMS);

		if(((DistributedMultiSchedule<E>)dm.schedule).fields2D.size()>0){
			init_spatial_connection();
		}
		if(((DistributedMultiSchedule<E>)dm.schedule).fields3D.size()>0){
			init_3DSpatial_connection();
		}

		if(((DistributedMultiSchedule<E>)dm.schedule).fieldsNetwork.size()>0)
			init_network_connection();

		//FOR GRAPHIC TESTING

		try {
			connectionJMS.createTopic(topicPrefix+"GRAPHICS", 1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	private  ReentrantLock lock;
	private  Condition block;

	public void initNonUnfiromCommunication(QuadTree q)
	{
		lock=new ReentrantLock();
		block=lock.newCondition();

		try {

			for(ORIENTATION neighbors:q.neighborhood.keySet())
			{
				//System.err.println(this.TYPE+" crea "+ topicPrefix+q.ID + neighbors);
				connectionJMS.createTopic(topicPrefix+q.ID + neighbors,
						schedule.fields2D
								.size());

				//				for(QuadTree neighbor:q.neighborhood.get(neighbors))
				//				{
				//					System.err.println(this.TYPE+" si sottoscrive a  "+topicPrefix+neighbor.ID+QuadTree.swapOrientation(neighbors));
				//					connectionJMS.subscribeToTopic(topicPrefix+neighbor.ID+QuadTree.swapOrientation(neighbors));
				//					UpdaterThreadForListener u1 = new UpdaterThreadForListener(
				//					connectionJMS,topicPrefix+neighbor.ID+QuadTree.swapOrientation(neighbors),schedule.fields2D, listeners);
				//					u1.start();
				//				}

			}

			for(ORIENTATION neighbors:q.toSubscribe.keySet())
			{

				for(QuadTree neighbor:q.toSubscribe.get(neighbors))
				{
					//System.err.println(this.TYPE+" si sottoscrive a  "+topicPrefix+neighbor.ID+(neighbors));
					connectionJMS.subscribeToTopic(topicPrefix+neighbor.ID+(neighbors));
					UpdaterThreadForListener u1 = new UpdaterThreadForListener(
							connectionJMS,topicPrefix+neighbor.ID+(neighbors),schedule.fields2D, listeners);
					u1.start();
				}

			}
			connectionJMS.publishToTopic("READY "+q.ID, "CONNECTIONS_CREATED", "");

			while(!CONNECTIONS_CREATED)
			{
				//Block this thread
				lock.lock();
				block.await();
				lock.unlock();

			}

		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	protected void init_spatial_connection() {
		boolean toroidal_need=false;
		for(DistributedField2D field :
				((DistributedMultiSchedule<E>)dm.schedule).getFields())
		{
			if(field.isToroidal())
			{
				toroidal_need=true;
				break;
			}
		}
		//only for global variables
		dm.upVar=new UpdateGlobalVarAtStep(dm);
		ThreadVisualizationCellMessageListener thread = new ThreadVisualizationCellMessageListener(
				connectionJMS,
				((DistributedMultiSchedule) this.schedule));
		thread.start();

		try {
			boolean a = connectionJMS.createTopic(topicPrefix+"GRAPHICS" + TYPE,
					schedule.fields2D.size());
			connectionJMS.subscribeToTopic(topicPrefix+"GRAPHICS" + TYPE);
			ThreadZoomInCellMessageListener t_zoom = new ThreadZoomInCellMessageListener(
					connectionJMS,
					TYPE.toString(), (DistributedMultiSchedule) this.schedule);
			t_zoom.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (toroidal_need)
		{
			connection_IS_toroidal();
		}
		else
		{
			connection_NO_toroidal();
		}

		// Support for Global Parameters
		try {
			connectionJMS.subscribeToTopic(topicPrefix + "GLOBAL_REDUCED");
			UpdaterThreadForGlobalsListener ug = new UpdaterThreadForGlobalsListener(
					connectionJMS,
					topicPrefix + "GLOBAL_REDUCED",
					schedule.fields2D,
					listeners);
			ug.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected void connection_IS_toroidal() {

		if (MODE == DistributedField2D.UNIFORM_PARTITIONING_MODE) { // HORIZONTAL_MODE
			int i = TYPE.pos_i, j = TYPE.pos_j;
			try {

				//one columns and N rows
				if(rows > 1 && columns == 1){
					connectionJMS.createTopic(topicPrefix+TYPE + "N",
							schedule.fields2D
									.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "S",
							schedule.fields2D
									.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "NW",
							schedule.fields2D
									.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "NE",
							schedule.fields2D
									.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "SW",
							schedule.fields2D
									.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "SE",
							schedule.fields2D
									.size());

					connectionJMS.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "N");
					connectionJMS.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "S");

					connectionJMS.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "SE");
					connectionJMS.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "SW");
					connectionJMS.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "NE");
					connectionJMS.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "NW");

					UpdaterThreadForListener u3 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "N",
							schedule.fields2D, listeners);
					u3.start();

					UpdaterThreadForListener u4 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "S",
							schedule.fields2D, listeners);
					u4.start();

					UpdaterThreadForListener u5 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "SE",
							schedule.fields2D, listeners);
					u5.start();

					UpdaterThreadForListener u6 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "SW",
							schedule.fields2D, listeners);
					u6.start();

					UpdaterThreadForListener u7 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "NE",
							schedule.fields2D, listeners);
					u7.start();

					UpdaterThreadForListener u8 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "NW",
							schedule.fields2D, listeners);
					u8.start();
				}
				//one rows and N columns
				else if(rows==1 && columns>1){
					connectionJMS.createTopic(topicPrefix+TYPE + "W",
							schedule.fields2D
									.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "E",
							schedule.fields2D
									.size());

					connectionJMS.createTopic(topicPrefix+TYPE + "NW",
							schedule.fields2D
									.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "NE",
							schedule.fields2D
									.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "SW",
							schedule.fields2D
									.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "SE",
							schedule.fields2D
									.size());



					connectionJMS.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "W");
					connectionJMS.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "E");

					connectionJMS.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "SE");
					connectionJMS.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "SW");
					connectionJMS.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "NE");
					connectionJMS.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "NW");

					UpdaterThreadForListener u1 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "W",
							schedule.fields2D, listeners);
					u1.start();

					UpdaterThreadForListener u2 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "E",
							schedule.fields2D, listeners);
					u2.start();


					UpdaterThreadForListener u5 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "SE",
							schedule.fields2D, listeners);
					u5.start();

					UpdaterThreadForListener u6 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "SW",
							schedule.fields2D, listeners);
					u6.start();

					UpdaterThreadForListener u7 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "NE",
							schedule.fields2D, listeners);
					u7.start();

					UpdaterThreadForListener u8 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "NW",
							schedule.fields2D, listeners);
					u8.start();
				}else{
					// N rows and N columns
					connectionJMS.createTopic(topicPrefix+TYPE + "W",
							schedule.fields2D
									.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "E",
							schedule.fields2D
									.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "S",
							schedule.fields2D
									.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "N",
							schedule.fields2D
									.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "NW",
							schedule.fields2D
									.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "NE",
							schedule.fields2D
									.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "SW",
							schedule.fields2D
									.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "SE",
							schedule.fields2D
									.size());


					connectionJMS.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "W");
					connectionJMS.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "E");
					connectionJMS.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "N");
					connectionJMS.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "S");
					connectionJMS.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "SE");
					connectionJMS.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "SW");
					connectionJMS.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "NE");
					connectionJMS.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "NW");

					UpdaterThreadForListener u1 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "W",
							schedule.fields2D, listeners);
					u1.start();

					UpdaterThreadForListener u2 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "E",
							schedule.fields2D, listeners);
					u2.start();

					UpdaterThreadForListener u3 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "N",
							schedule.fields2D, listeners);
					u3.start();

					UpdaterThreadForListener u4 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "S",
							schedule.fields2D, listeners);
					u4.start();

					UpdaterThreadForListener u5 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "SE",
							schedule.fields2D, listeners);
					u5.start();

					UpdaterThreadForListener u6 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "SW",
							schedule.fields2D, listeners);
					u6.start();

					UpdaterThreadForListener u7 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "NE",
							schedule.fields2D, listeners);
					u7.start();

					UpdaterThreadForListener u8 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "NW",
							schedule.fields2D, listeners);
					u8.start();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(MODE == DistributedField2D.HORIZONTAL_BALANCED_DISTRIBUTION_MODE){
			int i = TYPE.pos_i, j = TYPE.pos_j;
			try{
				connectionJMS.createTopic(topicPrefix+TYPE + "W",
						schedule.fields2D
								.size());
				connectionJMS.createTopic(topicPrefix+TYPE + "E",
						schedule.fields2D
								.size());

				connectionJMS.createTopic(topicPrefix+TYPE + "NW",
						schedule.fields2D
								.size());
				connectionJMS.createTopic(topicPrefix+TYPE + "NE",
						schedule.fields2D
								.size());
				connectionJMS.createTopic(topicPrefix+TYPE + "SW",
						schedule.fields2D
								.size());
				connectionJMS.createTopic(topicPrefix+TYPE + "SE",
						schedule.fields2D
								.size());



				connectionJMS.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
						+ ((j + 1 + columns) % columns) + "W");
				connectionJMS.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
						+ ((j - 1 + columns) % columns) + "E");

				connectionJMS.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
						+ ((j - 1 + columns) % columns) + "SE");
				connectionJMS.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
						+ ((j + 1 + columns) % columns) + "SW");
				connectionJMS.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
						+ ((j - 1 + columns) % columns) + "NE");
				connectionJMS.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
						+ ((j + 1 + columns) % columns) + "NW");

				UpdaterThreadForListener u1 = new UpdaterThreadForListener(
						connectionJMS, topicPrefix+((i + rows) % rows) + "-"
						+ ((j + 1 + columns) % columns) + "W",
						schedule.fields2D, listeners);
				u1.start();

				UpdaterThreadForListener u2 = new UpdaterThreadForListener(
						connectionJMS, topicPrefix+((i + rows) % rows) + "-"
						+ ((j - 1 + columns) % columns) + "E",
						schedule.fields2D, listeners);
				u2.start();


				UpdaterThreadForListener u5 = new UpdaterThreadForListener(
						connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
						+ ((j - 1 + columns) % columns) + "SE",
						schedule.fields2D, listeners);
				u5.start();

				UpdaterThreadForListener u6 = new UpdaterThreadForListener(
						connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
						+ ((j + 1 + columns) % columns) + "SW",
						schedule.fields2D, listeners);
				u6.start();

				UpdaterThreadForListener u7 = new UpdaterThreadForListener(
						connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
						+ ((j - 1 + columns) % columns) + "NE",
						schedule.fields2D, listeners);
				u7.start();

				UpdaterThreadForListener u8 = new UpdaterThreadForListener(
						connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
						+ ((j + 1 + columns) % columns) + "NW",
						schedule.fields2D, listeners);
				u8.start();
			}catch(Exception e){
				e.printStackTrace();
			}
		} else if (MODE == DistributedField2D.SQUARE_BALANCED_DISTRIBUTION_MODE) { // SQUARE BALANCED

			try {

				connectionJMS.createTopic(topicPrefix+TYPE+"W",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"E",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"S",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"N",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"NW",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"NE",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"SW",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"SE",((DistributedMultiSchedule)schedule).fields2D.size());

				int i=TYPE.pos_i,j=TYPE.pos_j;
				int sqrt=(int)Math.sqrt(NUMPEERS);

				connectionJMS.subscribeToTopic(topicPrefix+((i+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"W");
				connectionJMS.subscribeToTopic(topicPrefix+((i+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"E");
				connectionJMS.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"N");
				connectionJMS.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"S");
				connectionJMS.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"SE");
				connectionJMS.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"SW");
				connectionJMS.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"NE");
				connectionJMS.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"NW");

				UpdaterThreadForListener u1 = new UpdaterThreadForListener(connectionJMS,topicPrefix+((i+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"W",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u1.start();

				UpdaterThreadForListener u2 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"E",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u2.start();

				UpdaterThreadForListener u3 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"N",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u3.start();

				UpdaterThreadForListener u4 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"S",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u4.start();

				UpdaterThreadForListener u5 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"SE",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u5.start();

				UpdaterThreadForListener u6 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"SW",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u6.start();

				UpdaterThreadForListener u7 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"NE",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u7.start();

				UpdaterThreadForListener u8 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"NW",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u8.start();

			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(MODE == DistributedField2D.HORIZONTAL_BALANCED_DISTRIBUTION_MODE){
			int i = TYPE.pos_i, j = TYPE.pos_j;

			//N columns and one row
			try{

				connectionJMS.createTopic(topicPrefix+TYPE + "W",
						schedule.fields2D
								.size());
				connectionJMS.createTopic(topicPrefix+TYPE + "E",
						schedule.fields2D
								.size());

				connectionJMS.createTopic(topicPrefix+TYPE + "NW",
						schedule.fields2D
								.size());
				connectionJMS.createTopic(topicPrefix+TYPE + "NE",
						schedule.fields2D
								.size());
				connectionJMS.createTopic(topicPrefix+TYPE + "SW",
						schedule.fields2D
								.size());
				connectionJMS.createTopic(topicPrefix+TYPE + "SE",
						schedule.fields2D
								.size());



				connectionJMS.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
						+ ((j + 1 + columns) % columns) + "W");
				connectionJMS.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
						+ ((j - 1 + columns) % columns) + "E");

				connectionJMS.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
						+ ((j - 1 + columns) % columns) + "SE");
				connectionJMS.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
						+ ((j + 1 + columns) % columns) + "SW");
				connectionJMS.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
						+ ((j - 1 + columns) % columns) + "NE");
				connectionJMS.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
						+ ((j + 1 + columns) % columns) + "NW");

				UpdaterThreadForListener u1 = new UpdaterThreadForListener(
						connectionJMS, topicPrefix+((i + rows) % rows) + "-"
						+ ((j + 1 + columns) % columns) + "W",
						schedule.fields2D, listeners);
				u1.start();

				UpdaterThreadForListener u2 = new UpdaterThreadForListener(
						connectionJMS, topicPrefix+((i + rows) % rows) + "-"
						+ ((j - 1 + columns) % columns) + "E",
						schedule.fields2D, listeners);
				u2.start();


				UpdaterThreadForListener u5 = new UpdaterThreadForListener(
						connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
						+ ((j - 1 + columns) % columns) + "SE",
						schedule.fields2D, listeners);
				u5.start();

				UpdaterThreadForListener u6 = new UpdaterThreadForListener(
						connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
						+ ((j + 1 + columns) % columns) + "SW",
						schedule.fields2D, listeners);
				u6.start();

				UpdaterThreadForListener u7 = new UpdaterThreadForListener(
						connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
						+ ((j - 1 + columns) % columns) + "NE",
						schedule.fields2D, listeners);
				u7.start();

				UpdaterThreadForListener u8 = new UpdaterThreadForListener(
						connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
						+ ((j + 1 + columns) % columns) + "NW",
						schedule.fields2D, listeners);
				u8.start();
			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (MODE == DistributedField2D.NON_UNIFORM_PARTITIONING_MODE) { // NON UNFIRORM DISTRIBUTION MODE TOROIDAL

			try {
				System.out.println("Non Uniform Partitioning.");
				//				connectionJMS.createTopic(topicPrefix+TYPE+"W",((DistributedMultiSchedule)schedule).fields2D.size());
				//				connectionJMS.createTopic(topicPrefix+TYPE+"E",((DistributedMultiSchedule)schedule).fields2D.size());
				//				connectionJMS.createTopic(topicPrefix+TYPE+"S",((DistributedMultiSchedule)schedule).fields2D.size());
				//				connectionJMS.createTopic(topicPrefix+TYPE+"N",((DistributedMultiSchedule)schedule).fields2D.size());
				//				connectionJMS.createTopic(topicPrefix+TYPE+"NW",((DistributedMultiSchedule)schedule).fields2D.size());
				//				connectionJMS.createTopic(topicPrefix+TYPE+"NE",((DistributedMultiSchedule)schedule).fields2D.size());
				//				connectionJMS.createTopic(topicPrefix+TYPE+"SW",((DistributedMultiSchedule)schedule).fields2D.size());
				//				connectionJMS.createTopic(topicPrefix+TYPE+"SE",((DistributedMultiSchedule)schedule).fields2D.size());
				//
				//				int i=TYPE.pos_i,j=TYPE.pos_j;
				//				int sqrt=(int)Math.sqrt(NUMPEERS);
				//
				//				connectionJMS.subscribeToTopic(topicPrefix+((i+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"W");
				//				connectionJMS.subscribeToTopic(topicPrefix+((i+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"E");
				//				connectionJMS.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"N");
				//				connectionJMS.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"S");
				//				connectionJMS.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"SE");
				//				connectionJMS.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"SW");
				//				connectionJMS.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"NE");
				//				connectionJMS.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"NW");
				//
				//				u1 = new UpdaterThreadForListener(connectionJMS,topicPrefix+((i+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"W",((DistributedMultiSchedule)schedule).fields2D,listeners);
				//				u1.start();
				//
				//				u2 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"E",((DistributedMultiSchedule)schedule).fields2D,listeners);
				//				u2.start();
				//
				//				u3 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"N",((DistributedMultiSchedule)schedule).fields2D,listeners);
				//				u3.start();
				//
				//				u4 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"S",((DistributedMultiSchedule)schedule).fields2D,listeners);
				//				u4.start();
				//
				//				u5 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"SE",((DistributedMultiSchedule)schedule).fields2D,listeners);
				//				u5.start();
				//
				//				u6 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"SW",((DistributedMultiSchedule)schedule).fields2D,listeners);
				//				u6.start();
				//
				//				u7 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"NE",((DistributedMultiSchedule)schedule).fields2D,listeners);
				//				u7.start();
				//
				//				u8 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"NW",((DistributedMultiSchedule)schedule).fields2D,listeners);
				//				u8.start();

			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


	}

	protected void connection_NO_toroidal() {

		if (MODE == DistributedField2D.UNIFORM_PARTITIONING_MODE) { // HORIZONTAL_MODE

			try {

				if(rows>1 && columns==1){
					if(TYPE.pos_i==0){
						//crea sotto e sottomettiti a i+1-spra
						connectionJMS.createTopic(topicPrefix+TYPE + "S",
								schedule.fields2D
										.size());
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourDown() + "N");
						UpdaterThreadForListener u1 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourDown() + "N",
								schedule.fields2D, listeners);
						u1.start();
					}
					else if(TYPE.pos_i == rows-1){
						//crea sopra e sottomettiti a i-1-sotto
						connectionJMS.createTopic(topicPrefix+TYPE + "N",
								schedule.fields2D
										.size());
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourUp() + "S");
						UpdaterThreadForListener u1 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourUp() + "S",
								schedule.fields2D, listeners);
						u1.start();
					}
					else{
						connectionJMS.createTopic(topicPrefix+TYPE + "S",
								schedule.fields2D
										.size());
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourDown() + "N");
						UpdaterThreadForListener u1 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourDown() + "N",
								schedule.fields2D, listeners);
						u1.start();

						connectionJMS.createTopic(topicPrefix+TYPE + "N",
								schedule.fields2D
										.size());
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourUp() + "S");
						UpdaterThreadForListener u2 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourUp() + "S",
								schedule.fields2D, listeners);
						u2.start();
					}
					//crea sopra e sotto e sottomettiti a i-1-sotto e a i+1 sopra

				}
				else if(rows==1 && columns > 1){


					if(TYPE.pos_j < columns){
						connectionJMS.createTopic(topicPrefix+TYPE + "E",
								schedule.fields2D
										.size());

						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourRight() + "W");
						UpdaterThreadForListener u2 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourRight() + "W",
								schedule.fields2D, listeners);
						u2.start();

					}
					if(TYPE.pos_j > 0){
						connectionJMS.createTopic(topicPrefix+TYPE + "W",
								schedule.fields2D
										.size());

						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourLeft() + "E");
						UpdaterThreadForListener u1 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourLeft() + "E",
								schedule.fields2D, listeners);
						u1.start();
					}
				}else{
					//N rows and N columns
					if(TYPE.pos_j > 0)
						connectionJMS.createTopic(topicPrefix+TYPE + "W",
								schedule.fields2D
										.size());
					if(TYPE.pos_j < columns)
						connectionJMS.createTopic(topicPrefix+TYPE + "E",
								schedule.fields2D
										.size());
					if(TYPE.pos_i > 0)
						connectionJMS.createTopic(topicPrefix+TYPE + "N",
								schedule.fields2D
										.size());
					if(TYPE.pos_i < rows)
						connectionJMS.createTopic(topicPrefix+TYPE + "S",
								schedule.fields2D
										.size());
					if(TYPE.pos_i < rows && TYPE.pos_j < columns)
						connectionJMS.createTopic(topicPrefix+TYPE + "SE",
								schedule.fields2D
										.size());
					if(TYPE.pos_i > 0 && TYPE.pos_j < columns)
						connectionJMS.createTopic(topicPrefix+TYPE + "NE",
								schedule.fields2D
										.size());
					if(TYPE.pos_i < rows && TYPE.pos_j > 0)
						connectionJMS.createTopic(topicPrefix+TYPE + "SW",
								schedule.fields2D
										.size());
					if(TYPE.pos_i > 0 && TYPE.pos_j > 0)
						connectionJMS.createTopic(topicPrefix+TYPE + "NW",
								schedule.fields2D
										.size());

					if(TYPE.pos_i > 0 && TYPE.pos_j > 0){
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagLeftUp()
								+ "SE");
						UpdaterThreadForListener u1 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourDiagLeftUp() + "SE",
								schedule.fields2D, listeners);
						u1.start();
					}
					if(TYPE.pos_i > 0 && TYPE.pos_j < columns){
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagRightUp()
								+ "SW");
						UpdaterThreadForListener u2 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourDiagRightUp() + "SW",
								schedule.fields2D, listeners);
						u2.start();
					}

					if(TYPE.pos_i < rows && TYPE.pos_j > 0){
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagLeftDown()
								+ "NE");
						UpdaterThreadForListener u3 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourDiagLeftDown() + "NE",
								schedule.fields2D, listeners);
						u3.start();
					}
					if(TYPE.pos_i < rows && TYPE.pos_j < columns){
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagRightDown()
								+ "NW");
						UpdaterThreadForListener u4 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourDiagRightDown() + "NW",
								schedule.fields2D, listeners);
						u4.start();
					}
					if(TYPE.pos_j > 0){
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourLeft() + "E");
						UpdaterThreadForListener u5 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourLeft() + "E",
								schedule.fields2D, listeners);
						u5.start();
					}
					if(TYPE.pos_j < columns){
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourRight() + "W");
						UpdaterThreadForListener u6 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourRight() + "W",
								schedule.fields2D, listeners);
						u6.start();
					}
					if(TYPE.pos_i > 0){
						connectionJMS.subscribeToTopic(topicPrefix+(TYPE.getNeighbourUp() + "S"));
						UpdaterThreadForListener u7 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourUp() + "S",
								schedule.fields2D, listeners);
						u7.start();
					}
					if(TYPE.pos_i < rows){
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourDown() + "N");
						UpdaterThreadForListener u8 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourDown() + "N",
								schedule.fields2D, listeners);
						u8.start();
					}
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}else if(MODE == DistributedField2D.HORIZONTAL_BALANCED_DISTRIBUTION_MODE){
			// one row and N columns
			try{
				if(TYPE.pos_j < columns){
					connectionJMS.createTopic(topicPrefix+TYPE + "E",
							schedule.fields2D
									.size());

					connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourRight() + "W");
					UpdaterThreadForListener u2 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+TYPE.getNeighbourRight() + "W",
							schedule.fields2D, listeners);
					u2.start();

				}
				if(TYPE.pos_j > 0){
					connectionJMS.createTopic(topicPrefix+TYPE + "W",
							schedule.fields2D
									.size());

					connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourLeft() + "E");
					UpdaterThreadForListener u1 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+TYPE.getNeighbourLeft() + "E",
							schedule.fields2D, listeners);
					u1.start();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		else if (MODE == DistributedField2D.SQUARE_BALANCED_DISTRIBUTION_MODE) { // SQUARE BALANCED

			try {

				connectionJMS.createTopic(topicPrefix+TYPE+"W",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"E",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"S",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"N",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"NW",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"NE",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"SW",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"SE",((DistributedMultiSchedule)schedule).fields2D.size());

				int i=TYPE.pos_i,j=TYPE.pos_j;
				int sqrt=(int)Math.sqrt(NUMPEERS);

				connectionJMS.subscribeToTopic(topicPrefix+((i+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"W");
				connectionJMS.subscribeToTopic(topicPrefix+((i+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"E");
				connectionJMS.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"N");
				connectionJMS.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"S");
				connectionJMS.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"SE");
				connectionJMS.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"SW");
				connectionJMS.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"NE");
				connectionJMS.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"NW");

				UpdaterThreadForListener u1 = new UpdaterThreadForListener(connectionJMS,topicPrefix+((i+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"W",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u1.start();

				UpdaterThreadForListener u2 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"E",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u2.start();

				UpdaterThreadForListener u3 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"N",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u3.start();

				UpdaterThreadForListener u4 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"S",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u4.start();

				UpdaterThreadForListener u5 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"SE",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u5.start();

				UpdaterThreadForListener u6 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"SW",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u6.start();

				UpdaterThreadForListener u7 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"NE",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u7.start();

				UpdaterThreadForListener u8 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"NW",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u8.start();

			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	protected void init_3DSpatial_connection(){
		boolean toroidal_need = false;
		for(DistributedField3D field : ((DistributedMultiSchedule<E>)dm.schedule).get3DFields()){
			if(field.isToroidal()){
				toroidal_need=true;
				break;
			}
		}
		/*
		//only for global variables
		dm.upVar=new UpdateGlobalVarAtStep(dm);
		ThreadVisualizationCellMessageListener thread = new ThreadVisualizationCellMessageListener(
				connectionJMS,
				((DistributedMultiSchedule) this.schedule));
		thread.start();

		try {
			boolean a = connectionJMS.createTopic(topicPrefix+"GRAPHICS" + TYPE,
					schedule.fields3D.size());
			connectionJMS.subscribeToTopic(topicPrefix+"GRAPHICS" + TYPE);
			ThreadZoomInCellMessageListener t_zoom = new ThreadZoomInCellMessageListener(
					connectionJMS,
					TYPE.toString(), (DistributedMultiSchedule) this.schedule);
			t_zoom.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		if (toroidal_need)
		{
			connection3D_IS_toroidal();
		}
		else
		{
			connection3D_NO_toroidal();
		}

		/*	// Support for Global Parameters
		try {
			connectionJMS.subscribeToTopic(topicPrefix + "GLOBAL_REDUCED");
			UpdaterThreadForGlobalsListener ug = new UpdaterThreadForGlobalsListener(
					connectionJMS,
					topicPrefix + "GLOBAL_REDUCED",
					schedule.fields2D,
					listeners);
			ug.start();
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}

	protected void connection3D_NO_toroidal(){
		if (MODE == DistributedField3D.UNIFORM_PARTITIONING_MODE) {
			try {
				if(lenghts==1){
					if(rows>1 && columns==1){
						if (TYPE.pos_i==0) {
							connectionJMS.createTopic(topicPrefix+TYPE+"S",
									((DistributedMultiSchedule)schedule).fields3D.size());
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDown()+"N");
							UpdaterThreadForListener3D u1 = new UpdaterThreadForListener3D(connectionJMS,
									topicPrefix+TYPE.getNeighbour3DDown()+"N", ((DistributedMultiSchedule)schedule).fields3D,
									listeners3D);
							u1.start();
						} else if(TYPE.pos_i==rows-1){
							connectionJMS.createTopic(topicPrefix+TYPE+"N",
									((DistributedMultiSchedule)schedule).fields3D.size());
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DUp()+"S");
							UpdaterThreadForListener3D u1 = new UpdaterThreadForListener3D(connectionJMS,
									topicPrefix+TYPE.getNeighbour3DUp()+"S", ((DistributedMultiSchedule)schedule).fields3D,
									listeners3D);
							u1.start();
						}else{
							connectionJMS.createTopic(topicPrefix+TYPE+"S",
									((DistributedMultiSchedule)schedule).fields3D.size());
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDown()+"N");
							UpdaterThreadForListener3D u1 = new UpdaterThreadForListener3D(connectionJMS,
									topicPrefix+TYPE.getNeighbour3DDown()+"N", ((DistributedMultiSchedule)schedule).fields3D,
									listeners3D);
							u1.start();

							connectionJMS.createTopic(topicPrefix+TYPE+"N",
									((DistributedMultiSchedule)schedule).fields3D.size());
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DUp()+"S");
							UpdaterThreadForListener3D u2 = new UpdaterThreadForListener3D(connectionJMS,
									topicPrefix+TYPE.getNeighbour3DUp()+"S", ((DistributedMultiSchedule)schedule).fields3D,
									listeners3D);
							u2.start();
						}
					}else if(rows==1 && columns>1){
						if(TYPE.pos_j==0){
							connectionJMS.createTopic(topicPrefix+TYPE+"E",
									((DistributedMultiSchedule) schedule).fields3D.size());
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRight()+"W");
							UpdaterThreadForListener3D u1 = new UpdaterThreadForListener3D(connectionJMS,
									topicPrefix+TYPE.getNeighbour3DRight()+"W", ((DistributedMultiSchedule)schedule).fields3D,
									listeners3D);
							u1.start();
						}else if(TYPE.pos_j==columns-1){
							connectionJMS.createTopic(topicPrefix+TYPE+"W",
									((DistributedMultiSchedule)schedule).fields3D.size());
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DLeft()+"E");
							UpdaterThreadForListener3D u1 = new UpdaterThreadForListener3D(connectionJMS,
									topicPrefix+TYPE.getNeighbour3DLeft()+"E",((DistributedMultiSchedule)schedule).fields3D,
									listeners3D);
							u1.start();
						}else{
							connectionJMS.createTopic(topicPrefix+TYPE+"E",
									((DistributedMultiSchedule) schedule).fields3D.size());
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRight()+"W");
							UpdaterThreadForListener3D u1 = new UpdaterThreadForListener3D(connectionJMS,
									topicPrefix+TYPE.getNeighbour3DRight()+"W", ((DistributedMultiSchedule)schedule).fields3D,
									listeners3D);
							u1.start();

							connectionJMS.createTopic(topicPrefix+TYPE+"W",
									((DistributedMultiSchedule)schedule).fields3D.size());
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DLeft()+"E");
							UpdaterThreadForListener3D u2 = new UpdaterThreadForListener3D(connectionJMS,
									topicPrefix+TYPE.getNeighbour3DLeft()+"E",((DistributedMultiSchedule)schedule).fields3D,
									listeners3D);
							u2.start();
						}
					}
					else {
						if(TYPE.pos_i>0){
							connectionJMS.createTopic(topicPrefix+TYPE+"N",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows){
							connectionJMS.createTopic(topicPrefix+TYPE+"S",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if (TYPE.pos_j>0){
							connectionJMS.createTopic(topicPrefix+TYPE+"W",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_j<columns){
							connectionJMS.createTopic(topicPrefix+TYPE+"E",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows && TYPE.pos_j<columns){
							connectionJMS.createTopic(topicPrefix+TYPE+"SE",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}

						if(TYPE.pos_i<rows && TYPE.pos_j>0){
							connectionJMS.createTopic(topicPrefix+TYPE+"SW",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i>0 && TYPE.pos_j>0){
							connectionJMS.createTopic(topicPrefix+TYPE+"NW",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i>0 && TYPE.pos_j<columns){
							connectionJMS.createTopic(topicPrefix+TYPE+"NE",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}

						if(TYPE.pos_i>0 && TYPE.pos_j>0){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagLeftUp()+"SE");
							UpdaterThreadForListener3D u1 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DDiagLeftUp()+"SE" ,
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u1.start();
						}

						if (TYPE.pos_i>0 && TYPE.pos_j<columns){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagRightUp()+"SW");
							UpdaterThreadForListener3D u2 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DDiagRightUp()+"SW",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u2.start();
						}

						if(TYPE.pos_i<rows && TYPE.pos_j>0){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagLeftDown()+"NE");
							UpdaterThreadForListener3D u3 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DDiagLeftDown()+"NE",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u3.start();
						}

						if(TYPE.pos_i<rows && TYPE.pos_j<columns){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagRightDown()+"NW");
							UpdaterThreadForListener3D u4 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DDiagRightDown()+"NW",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u4.start();

						}

						if(TYPE.pos_i>0){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DUp()+"S");
							UpdaterThreadForListener3D u5 = new UpdaterThreadForListener3D(connectionJMS,topicPrefix+TYPE.getNeighbour3DUp()+"S" ,
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u5.start();
						}

						if(TYPE.pos_i<rows){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDown()+"N");
							UpdaterThreadForListener3D u6 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DDown()+"N",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u6.start();
						}

						if(TYPE.pos_j>0){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DLeft()+"E");
							UpdaterThreadForListener3D u7 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DLeft()+"E",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u7.start();
						}

						if(TYPE.pos_j<columns){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRight()+"W");
							UpdaterThreadForListener3D u8 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DRight()+"W",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u8.start();
						}

					}

				} else {// lenghts >1
					if(rows==1 && columns==1){
						if(TYPE.pos_z==0){
							connectionJMS.createTopic(topicPrefix+TYPE+"R",
									((DistributedMultiSchedule)dm.schedule).fields3D.size());
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRear()+"F");
							UpdaterThreadForListener3D u1 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DRear()+"F",
									((DistributedMultiSchedule)dm.schedule).fields3D,listeners3D);
							u1.start();
						}else if(TYPE.pos_z==lenghts-1){
							connectionJMS.createTopic(topicPrefix+TYPE+"F",
									((DistributedMultiSchedule)dm.schedule).fields3D.size());
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DFront()+"R");
							UpdaterThreadForListener3D u1 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DFront()+"R",
									((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
							u1.start();
						}else{
							connectionJMS.createTopic(topicPrefix+TYPE+"R",
									((DistributedMultiSchedule)dm.schedule).fields3D.size());
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRear()+"F");
							UpdaterThreadForListener3D u1 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DRear()+"F",
									((DistributedMultiSchedule)dm.schedule).fields3D,listeners3D);
							u1.start();
							connectionJMS.createTopic(topicPrefix+TYPE+"F",
									((DistributedMultiSchedule)dm.schedule).fields3D.size());
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DFront()+"R");
							UpdaterThreadForListener3D u2 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DFront()+"R",
									((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
							u2.start();
						}
					}else if (rows>1 && columns==1){

						if(TYPE.pos_i>0){
							connectionJMS.createTopic(topicPrefix+TYPE+"N",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows){
							connectionJMS.createTopic(topicPrefix+TYPE+"S",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_z>0){
							connectionJMS.createTopic(topicPrefix+TYPE+"F",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_z<lenghts){
							connectionJMS.createTopic(topicPrefix+TYPE+"R",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i>0 && TYPE.pos_z>0){
							connectionJMS.createTopic(topicPrefix+TYPE+"NF", (
									(DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i>0 && TYPE.pos_z<lenghts){
							connectionJMS.createTopic(topicPrefix+TYPE+"NR",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows && TYPE.pos_z>0){
							connectionJMS.createTopic(topicPrefix+TYPE+"SF",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows && TYPE.pos_z<lenghts){
							connectionJMS.createTopic(topicPrefix+TYPE+"SR",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}

						if(TYPE.pos_i>0 && TYPE.pos_z>0){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DUpFront()+"SR");
							UpdaterThreadForListener3D u1 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DUpFront()+"SR",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u1.start();
						}
						if(TYPE.pos_i>0 && TYPE.pos_z<lenghts){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DUpRear()+"SF");
							UpdaterThreadForListener3D u2 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DUpRear()+"SF",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u2.start();
						}
						if(TYPE.pos_i<rows && TYPE.pos_z>0){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDownFront()+"NR");
							UpdaterThreadForListener3D u3 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DDownFront()+"NR",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u3.start();
						}
						if(TYPE.pos_i<rows && TYPE.pos_z<lenghts){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDownRear()+"NF");
							UpdaterThreadForListener3D u4 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DDownRear()+"NF",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u4.start();
						}
						if(TYPE.pos_i>0){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DUp()+"S");
							UpdaterThreadForListener3D u5 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DUp()+"S",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u5.start();
						}
						if(TYPE.pos_i<rows){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDown()+"N");
							UpdaterThreadForListener3D u6 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DDown()+"N",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u6.start();
						}
						if(TYPE.pos_z>0){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DFront()+"R");
							UpdaterThreadForListener3D u7 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DFront()+"R",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u7.start();
						}
						if(TYPE.pos_z<lenghts){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRear()+"F");
							UpdaterThreadForListener3D u8 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DRear()+"F",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u8.start();
						}
					} else if (rows==1 && columns>1){
						if(TYPE.pos_j>0){
							connectionJMS.createTopic(topicPrefix+TYPE+"W",
									((DistributedMultiSchedule) schedule).fields3D.size());
						}
						if(TYPE.pos_j<columns){
							connectionJMS.createTopic(topicPrefix+TYPE+"E",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_z>0){
							connectionJMS.createTopic(topicPrefix+TYPE+"F",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_z<lenghts){
							connectionJMS.createTopic(topicPrefix+TYPE+"R",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_j>0 && TYPE.pos_z>0){
							connectionJMS.createTopic(topicPrefix+TYPE+"WF",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_j>0 && TYPE.pos_z<lenghts){
							connectionJMS.createTopic(topicPrefix+TYPE+"WR",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_j<columns && TYPE.pos_z>0 ){
							connectionJMS.createTopic(topicPrefix+TYPE+"EF",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_j<columns && TYPE.pos_z<lenghts){
							connectionJMS.createTopic(topicPrefix+TYPE+"ER",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}

						if(TYPE.pos_j>0 && TYPE.pos_z>0){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DLeftFront()+"ER");
							UpdaterThreadForListener3D u1 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DLeftFront()+"ER",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u1.start();
						}
						if(TYPE.pos_j>0 && TYPE.pos_z<lenghts){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DLeftRear()+"EF");
							UpdaterThreadForListener3D u2 = new UpdaterThreadForListener3D(connectionJMS,topicPrefix+TYPE.getNeighbour3DLeftRear()+"EF",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u2.start();
						}
						if(TYPE.pos_j<columns && TYPE.pos_z>0 ){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRightFront()+"WR");
							UpdaterThreadForListener3D u3 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DRightFront()+"WR",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u3.start();
						}
						if(TYPE.pos_j<columns && TYPE.pos_z<lenghts){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRightRear()+"WF");
							UpdaterThreadForListener3D u4 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DRightRear()+"WF",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u4.start();
						}
						if(TYPE.pos_j>0){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DLeft()+"E");
							UpdaterThreadForListener3D u5 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DLeft()+"E",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u5.start();
						}
						if(TYPE.pos_j<columns){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRight()+"W");
							UpdaterThreadForListener3D u6 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DRight()+"W",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u6.start();
						}
						if(TYPE.pos_z>0){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DFront()+"R");
							UpdaterThreadForListener3D u7 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DFront()+"R",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u7.start();
						}
						if(TYPE.pos_z<lenghts){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRear()+"F");
							UpdaterThreadForListener3D u8 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DRear()+"F",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u8.start();
						}
					} else{

						if(TYPE.pos_i>0){
							connectionJMS.createTopic(topicPrefix+TYPE+"N",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows){
							connectionJMS.createTopic(topicPrefix+TYPE+"S",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_j>0){
							connectionJMS.createTopic(topicPrefix+TYPE+"W",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_j<columns){
							connectionJMS.createTopic(topicPrefix+TYPE+"E",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_z>0){
							connectionJMS.createTopic(topicPrefix+TYPE+"F",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_z<lenghts){
							connectionJMS.createTopic(topicPrefix+TYPE+"R",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i>0 && TYPE.pos_j>0){
							connectionJMS.createTopic(topicPrefix+TYPE+"NW",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i>0 && TYPE.pos_j<columns){
							connectionJMS.createTopic(topicPrefix+TYPE+"NE",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows && TYPE.pos_j>0){
							connectionJMS.createTopic(topicPrefix+TYPE+"SW",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows && TYPE.pos_j<columns){
							connectionJMS.createTopic(topicPrefix+TYPE+"SE",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i>0 && TYPE.pos_z>0){
							connectionJMS.createTopic(topicPrefix+TYPE+"NF",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i>0 && TYPE.pos_z<lenghts){
							connectionJMS.createTopic(topicPrefix+TYPE+"NR",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows && TYPE.pos_z>0){
							connectionJMS.createTopic(topicPrefix+TYPE+"SF",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows && TYPE.pos_z<lenghts){
							connectionJMS.createTopic(topicPrefix+TYPE+"SR",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_j>0 && TYPE.pos_z>0){
							connectionJMS.createTopic(topicPrefix+TYPE+"WF",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_j>0 && TYPE.pos_z<lenghts){
							connectionJMS.createTopic(topicPrefix+TYPE+"WR",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_j<columns && TYPE.pos_z>0){
							connectionJMS.createTopic(topicPrefix+TYPE+"EF",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_j<columns && TYPE.pos_z<lenghts){
							connectionJMS.createTopic(topicPrefix+TYPE+"ER",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i>0 && TYPE.pos_j>0 && TYPE.pos_z>0){
							connectionJMS.createTopic(topicPrefix+TYPE+"NWF",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i>0 && TYPE.pos_j>0 && TYPE.pos_z<lenghts){
							connectionJMS.createTopic(topicPrefix+TYPE+"NWR",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i>0 && TYPE.pos_j<columns && TYPE.pos_z>0){
							connectionJMS.createTopic(topicPrefix+TYPE+"NEF",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i>0 && TYPE.pos_j<columns && TYPE.pos_z<lenghts){
							connectionJMS.createTopic(topicPrefix+TYPE+"NER",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows && TYPE.pos_j>0 && TYPE.pos_z>0){
							connectionJMS.createTopic(topicPrefix+TYPE+"SWF",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows && TYPE.pos_j>0 && TYPE.pos_z<lenghts){
							connectionJMS.createTopic(topicPrefix+TYPE+"SWR",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows && TYPE.pos_j<columns && TYPE.pos_z>0){
							connectionJMS.createTopic(topicPrefix+TYPE+"SEF",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows && TYPE.pos_j<columns && TYPE.pos_z<lenghts){
							connectionJMS.createTopic(topicPrefix+TYPE+"SER",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}

						if(TYPE.pos_i>0 && TYPE.pos_j>0 && TYPE.pos_z>0){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagLeftUpFront()+"SER");
							UpdaterThreadForListener3D u1 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DDiagLeftUpFront()+"SER",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u1.start();
						}
						if(TYPE.pos_i>0 && TYPE.pos_j>0 && TYPE.pos_z<lenghts){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagLeftUpRear()+"SEF");
							UpdaterThreadForListener3D u2 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DDiagLeftUpRear()+"SEF",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u2.start();

						}
						if(TYPE.pos_i>0 && TYPE.pos_j<columns && TYPE.pos_z>0){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagRightUpFront()+"SWR");
							UpdaterThreadForListener3D u3 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DDiagRightUpFront()+"SWR",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u3.start();
						}
						if(TYPE.pos_i>0 && TYPE.pos_j<columns && TYPE.pos_z<lenghts){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagRightUpRear()+"SWF");
							UpdaterThreadForListener3D u4 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DDiagRightUpRear()+"SWF",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u4.start();
						}
						if(TYPE.pos_i<rows && TYPE.pos_j>0 && TYPE.pos_z>0){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagLeftDownFront()+"NER");
							UpdaterThreadForListener3D u5 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DDiagLeftDownFront()+"NER",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u5.start();
						}
						if(TYPE.pos_i<rows && TYPE.pos_j>0 && TYPE.pos_z<lenghts){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagLeftDownRear()+"NEF");
							UpdaterThreadForListener3D u6 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DDiagLeftDownRear()+"NEF",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u6.start();
						}
						if(TYPE.pos_i<rows && TYPE.pos_j<columns && TYPE.pos_z>0){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagRightDownFront()+"NWR");
							UpdaterThreadForListener3D u7 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DDiagRightDownFront()+"NWR",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u7.start();
						}
						if(TYPE.pos_i<rows && TYPE.pos_j<columns && TYPE.pos_z<lenghts){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagRightDownRear()+"NWF");
							UpdaterThreadForListener3D u8 = new UpdaterThreadForListener3D(connectionJMS,topicPrefix+TYPE.getNeighbour3DDiagRightDownRear()+"NWF" ,
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u8.start();
						}
						if(TYPE.pos_i>0 && TYPE.pos_j>0){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagLeftUp()+"SE");
							UpdaterThreadForListener3D u9 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DDiagLeftUp()+"SE",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u9.start();
						}
						if(TYPE.pos_i>0 && TYPE.pos_j<columns){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagRightUp()+"SW");
							UpdaterThreadForListener3D u10 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DDiagRightUp()+"SW",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u10.start();
						}
						if(TYPE.pos_i<rows && TYPE.pos_j>0){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagLeftDown()+"NE");
							UpdaterThreadForListener3D u11 = new UpdaterThreadForListener3D(connectionJMS,topicPrefix+TYPE.getNeighbour3DDiagLeftDown()+"NE" ,
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u11.start();
						}
						if(TYPE.pos_i<rows && TYPE.pos_j<columns){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagRightDown()+"NW");
							UpdaterThreadForListener3D u12 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DDiagRightDown()+"NW",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u12.start();
						}
						if(TYPE.pos_i>0 && TYPE.pos_z>0){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DUpFront()+"SR");
							UpdaterThreadForListener3D u13 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DUpFront()+"SR",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u13.start();
						}
						if(TYPE.pos_i>0 && TYPE.pos_z<lenghts){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DUpRear()+"SF");
							UpdaterThreadForListener3D u14 = new UpdaterThreadForListener3D(connectionJMS,topicPrefix+TYPE.getNeighbour3DUpRear()+"SF",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u14.start();
						}
						if(TYPE.pos_i<rows && TYPE.pos_z>0){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDownFront()+"NR");
							UpdaterThreadForListener3D u15 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DDownFront()+"NR",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u15.start();
						}
						if(TYPE.pos_i<rows && TYPE.pos_z<lenghts){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDownRear()+"NF");
							UpdaterThreadForListener3D u16 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DDownRear()+"NF",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u16.start();
						}
						if(TYPE.pos_j>0 && TYPE.pos_z>0){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DLeftFront()+"ER");
							UpdaterThreadForListener3D u17 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DLeftFront()+"ER",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u17.start();
						}
						if(TYPE.pos_j>0 && TYPE.pos_z<lenghts){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DLeftRear()+"EF");
							UpdaterThreadForListener3D u18 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DLeftRear()+"EF",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u18.start();
						}
						if(TYPE.pos_j<columns && TYPE.pos_z>0){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRightFront()+"WR");
							UpdaterThreadForListener3D u19 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DRightFront()+"WR",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u19.start();
						}
						if(TYPE.pos_j<columns && TYPE.pos_z<lenghts){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRightRear()+"WF");
							UpdaterThreadForListener3D u20 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DRightRear()+"WF",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u20.start();
						}
						if(TYPE.pos_i>0){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DUp()+"S");
							UpdaterThreadForListener3D u21 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DUp()+"S",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u21.start();
						}
						if(TYPE.pos_i<rows){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDown()+"N");
							UpdaterThreadForListener3D u22 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DDown()+"N",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u22.start();
						}
						if(TYPE.pos_j>0){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DLeft()+"E");
							UpdaterThreadForListener3D u23 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DLeft()+"E",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u23.start();
						}
						if(TYPE.pos_j<columns){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRight()+"W");
							UpdaterThreadForListener3D u24 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DRight()+"W",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u24.start();
						}
						if(TYPE.pos_z>0){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DFront()+"R");
							UpdaterThreadForListener3D u25 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DFront()+"R",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u25.start();
						}
						if(TYPE.pos_z<lenghts){
							connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRear()+"F");
							UpdaterThreadForListener3D u26 = new UpdaterThreadForListener3D(connectionJMS, topicPrefix+TYPE.getNeighbour3DRear()+"F",
									((DistributedMultiSchedule)schedule).fields3D, listeners3D);
							u26.start();
						}
					}
				}

			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void connection3D_IS_toroidal(){
		if (MODE == DistributedField3D.UNIFORM_PARTITIONING_MODE) {
			int i=TYPE.pos_i; int j = TYPE.pos_j; int z=TYPE.pos_z;
			try{
				if(lenghts==1){
					if(rows>1 && columns==1){

						connectionJMS.createTopic(topicPrefix+TYPE+"N",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"S",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
//						connectionJMS.createTopic(topicPrefix+TYPE+"W",
//								((DistributedMultiSchedule)dm.schedule).fields3D.size());
//						connectionJMS.createTopic(topicPrefix+TYPE+"E",
//								((DistributedMultiSchedule)dm.schedule).fields3D.size());
//						connectionJMS.createTopic(topicPrefix+TYPE+"F",
//								((DistributedMultiSchedule)dm.schedule).fields3D.size());
//						connectionJMS.createTopic(topicPrefix+TYPE+"R",
//								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"WF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"WR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"EF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"ER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());

						String topicN=topicPrefix+(((i-1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+lenghts)%lenghts))+"N";
						String topicS=topicPrefix+(((i+1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+lenghts)%lenghts))+"S";
//						String topicW=topicPrefix+(((i+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"W";
//						String topicE=topicPrefix+(((i+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"E";
//						String topicF=topicPrefix+(((i+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"F";
//						String topicR=topicPrefix+(((i+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"R";

						String topicNF=topicPrefix+(((i-1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"NF";
						String topicNW=topicPrefix+(((i-1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"NW";
						String topicNR=topicPrefix+(((i-1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"NR";
						String topicNE=topicPrefix+(((i-1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"NE";
						String topicSF=topicPrefix+(((i+1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"SF";
						String topicSW=topicPrefix+(((i+1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"SW";
						String topicSR=topicPrefix+(((i+1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"SR";
						String topicSE=topicPrefix+(((i+1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"SE";
						String topicWF=topicPrefix+(((i+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"WF";
						String topicWR=topicPrefix+(((i+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"WR";
						String topicEF=topicPrefix+(((i+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"EF";
						String topicER=topicPrefix+(((i+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"ER";

						String topicNWF=topicPrefix+(((i-1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"NWF";
						String topicNWR=topicPrefix+(((i-1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"NWR";
						String topicNEF=topicPrefix+(((i-1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"NEF";
						String topicNER=topicPrefix+(((i-1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"NER";
						String topicSWF=topicPrefix+(((i+1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"SWF";
						String topicSWR=topicPrefix+(((i+1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"SWR";
						String topicSEF=topicPrefix+(((i+1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"SEF";
						String topicSER=topicPrefix+(((i+1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"SER";

						connectionJMS.subscribeToTopic(topicN);
						connectionJMS.subscribeToTopic(topicS);
//						connectionJMS.subscribeToTopic(topicW);
//						connectionJMS.subscribeToTopic(topicE);
//						connectionJMS.subscribeToTopic(topicF);
//						connectionJMS.subscribeToTopic(topicR);
						connectionJMS.subscribeToTopic(topicNF);
						connectionJMS.subscribeToTopic(topicNW);
						connectionJMS.subscribeToTopic(topicNR);
						connectionJMS.subscribeToTopic(topicNE);
						connectionJMS.subscribeToTopic(topicSF);
						connectionJMS.subscribeToTopic(topicSW);
						connectionJMS.subscribeToTopic(topicSR);
						connectionJMS.subscribeToTopic(topicSE);
						connectionJMS.subscribeToTopic(topicWF);
						connectionJMS.subscribeToTopic(topicWR);
						connectionJMS.subscribeToTopic(topicER);
						connectionJMS.subscribeToTopic(topicEF);
						connectionJMS.subscribeToTopic(topicNWF);
						connectionJMS.subscribeToTopic(topicNWR);
						connectionJMS.subscribeToTopic(topicNEF);
						connectionJMS.subscribeToTopic(topicNER);
						connectionJMS.subscribeToTopic(topicSWF);
						connectionJMS.subscribeToTopic(topicSWR);
						connectionJMS.subscribeToTopic(topicSEF);
						connectionJMS.subscribeToTopic(topicSER);

						UpdaterThreadForListener3D u1 = new UpdaterThreadForListener3D(connectionJMS,topicN,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u1.start();
						UpdaterThreadForListener3D u2 = new UpdaterThreadForListener3D(connectionJMS, topicS,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u2.start();
//						UpdaterThreadForListener3D u3 = new UpdaterThreadForListener3D(connectionJMS, topicW,
//								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
//						u3.start();
//						UpdaterThreadForListener3D u4 = new UpdaterThreadForListener3D(connectionJMS, topicE,
//								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
//						u4.start();
//						UpdaterThreadForListener3D u5 = new UpdaterThreadForListener3D(connectionJMS, topicF,
//								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
//						u5.start();
//						UpdaterThreadForListener3D u6 = new UpdaterThreadForListener3D(connectionJMS, topicR,
//								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
//						u6.start();
						UpdaterThreadForListener3D u7 = new UpdaterThreadForListener3D(connectionJMS, topicNF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u7.start();
						UpdaterThreadForListener3D u8 = new UpdaterThreadForListener3D(connectionJMS, topicNW,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u8.start();
						UpdaterThreadForListener3D u9 = new UpdaterThreadForListener3D(connectionJMS, topicNR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u9.start();
						UpdaterThreadForListener3D u10 = new UpdaterThreadForListener3D(connectionJMS, topicNE,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u10.start();
						UpdaterThreadForListener3D u11 = new UpdaterThreadForListener3D(connectionJMS, topicSF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u11.start();
						UpdaterThreadForListener3D u12 = new UpdaterThreadForListener3D(connectionJMS, topicSW,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u12.start();
						UpdaterThreadForListener3D u13= new UpdaterThreadForListener3D(connectionJMS, topicSR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u13.start();
						UpdaterThreadForListener3D u14 = new UpdaterThreadForListener3D(connectionJMS, topicSE,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u14.start();
						UpdaterThreadForListener3D u15 = new UpdaterThreadForListener3D(connectionJMS, topicWF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u15.start();
						UpdaterThreadForListener3D u16 = new UpdaterThreadForListener3D(connectionJMS, topicWR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u16.start();
						UpdaterThreadForListener3D u17 = new UpdaterThreadForListener3D(connectionJMS, topicEF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u17.start();
						UpdaterThreadForListener3D u18 = new UpdaterThreadForListener3D(connectionJMS, topicER,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u18.start();
						UpdaterThreadForListener3D u19 = new UpdaterThreadForListener3D(connectionJMS, topicNWF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u19.start();
						UpdaterThreadForListener3D u20 = new UpdaterThreadForListener3D(connectionJMS, topicNWR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u20.start();
						UpdaterThreadForListener3D u21 = new UpdaterThreadForListener3D(connectionJMS, topicNEF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u21.start();
						UpdaterThreadForListener3D u22 = new UpdaterThreadForListener3D(connectionJMS, topicNER,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u22.start();
						UpdaterThreadForListener3D u23 = new UpdaterThreadForListener3D(connectionJMS, topicSWF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u23.start();
						UpdaterThreadForListener3D u24 = new UpdaterThreadForListener3D(connectionJMS, topicSWR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u24.start();
						UpdaterThreadForListener3D u25 = new UpdaterThreadForListener3D(connectionJMS, topicSEF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u25.start();
						UpdaterThreadForListener3D u26 = new UpdaterThreadForListener3D(connectionJMS, topicSER,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u26.start();



					}else if(rows==1 && columns>1){

//						connectionJMS.createTopic(topicPrefix+TYPE+"N",
//								((DistributedMultiSchedule)dm.schedule).fields3D.size());
//						connectionJMS.createTopic(topicPrefix+TYPE+"S",
//								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"W",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"E",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
//						connectionJMS.createTopic(topicPrefix+TYPE+"F",
//								((DistributedMultiSchedule)dm.schedule).fields3D.size());
//						connectionJMS.createTopic(topicPrefix+TYPE+"R",
//								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"WF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"WR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"EF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"ER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());

//						String topicN=topicPrefix+(((i-1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+lenghts)%lenghts))+"N";
//						String topicS=topicPrefix+(((i+1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+lenghts)%lenghts))+"S";
						String topicW=topicPrefix+(((i+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"W";
						String topicE=topicPrefix+(((i+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"E";
//						String topicF=topicPrefix+(((i+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"F";
//						String topicR=topicPrefix+(((i+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"R";

						String topicNF=topicPrefix+(((i-1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"NF";
						String topicNW=topicPrefix+(((i-1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"NW";
						String topicNR=topicPrefix+(((i-1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"NR";
						String topicNE=topicPrefix+(((i-1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"NE";
						String topicSF=topicPrefix+(((i+1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"SF";
						String topicSW=topicPrefix+(((i+1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"SW";
						String topicSR=topicPrefix+(((i+1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"SR";
						String topicSE=topicPrefix+(((i+1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"SE";
						String topicWF=topicPrefix+(((i+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"WF";
						String topicWR=topicPrefix+(((i+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"WR";
						String topicEF=topicPrefix+(((i+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"EF";
						String topicER=topicPrefix+(((i+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"ER";

						String topicNWF=topicPrefix+(((i-1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"NWF";
						String topicNWR=topicPrefix+(((i-1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"NWR";
						String topicNEF=topicPrefix+(((i-1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"NEF";
						String topicNER=topicPrefix+(((i-1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"NER";
						String topicSWF=topicPrefix+(((i+1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"SWF";
						String topicSWR=topicPrefix+(((i+1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"SWR";
						String topicSEF=topicPrefix+(((i+1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"SEF";
						String topicSER=topicPrefix+(((i+1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"SER";

//						connectionJMS.subscribeToTopic(topicN);
//						connectionJMS.subscribeToTopic(topicS);
						connectionJMS.subscribeToTopic(topicW);
						connectionJMS.subscribeToTopic(topicE);
//						connectionJMS.subscribeToTopic(topicF);
//						connectionJMS.subscribeToTopic(topicR);
						connectionJMS.subscribeToTopic(topicNF);
						connectionJMS.subscribeToTopic(topicNW);
						connectionJMS.subscribeToTopic(topicNR);
						connectionJMS.subscribeToTopic(topicNE);
						connectionJMS.subscribeToTopic(topicSF);
						connectionJMS.subscribeToTopic(topicSW);
						connectionJMS.subscribeToTopic(topicSR);
						connectionJMS.subscribeToTopic(topicSE);
						connectionJMS.subscribeToTopic(topicWF);
						connectionJMS.subscribeToTopic(topicWR);
						connectionJMS.subscribeToTopic(topicER);
						connectionJMS.subscribeToTopic(topicEF);
						connectionJMS.subscribeToTopic(topicNWF);
						connectionJMS.subscribeToTopic(topicNWR);
						connectionJMS.subscribeToTopic(topicNEF);
						connectionJMS.subscribeToTopic(topicNER);
						connectionJMS.subscribeToTopic(topicSWF);
						connectionJMS.subscribeToTopic(topicSWR);
						connectionJMS.subscribeToTopic(topicSEF);
						connectionJMS.subscribeToTopic(topicSER);

//						UpdaterThreadForListener3D u1 = new UpdaterThreadForListener3D(connectionJMS,topicN,
//								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
//						u1.start();
//						UpdaterThreadForListener3D u2 = new UpdaterThreadForListener3D(connectionJMS, topicS,
//								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
//						u2.start();
						UpdaterThreadForListener3D u3 = new UpdaterThreadForListener3D(connectionJMS, topicW,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u3.start();
						UpdaterThreadForListener3D u4 = new UpdaterThreadForListener3D(connectionJMS, topicE,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u4.start();
//						UpdaterThreadForListener3D u5 = new UpdaterThreadForListener3D(connectionJMS, topicF,
//								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
//						u5.start();
//						UpdaterThreadForListener3D u6 = new UpdaterThreadForListener3D(connectionJMS, topicR,
//								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
//						u6.start();
						UpdaterThreadForListener3D u7 = new UpdaterThreadForListener3D(connectionJMS, topicNF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u7.start();
						UpdaterThreadForListener3D u8 = new UpdaterThreadForListener3D(connectionJMS, topicNW,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u8.start();
						UpdaterThreadForListener3D u9 = new UpdaterThreadForListener3D(connectionJMS, topicNR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u9.start();
						UpdaterThreadForListener3D u10 = new UpdaterThreadForListener3D(connectionJMS, topicNE,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u10.start();
						UpdaterThreadForListener3D u11 = new UpdaterThreadForListener3D(connectionJMS, topicSF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u11.start();
						UpdaterThreadForListener3D u12 = new UpdaterThreadForListener3D(connectionJMS, topicSW,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u12.start();
						UpdaterThreadForListener3D u13= new UpdaterThreadForListener3D(connectionJMS, topicSR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u13.start();
						UpdaterThreadForListener3D u14 = new UpdaterThreadForListener3D(connectionJMS, topicSE,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u14.start();
						UpdaterThreadForListener3D u15 = new UpdaterThreadForListener3D(connectionJMS, topicWF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u15.start();
						UpdaterThreadForListener3D u16 = new UpdaterThreadForListener3D(connectionJMS, topicWR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u16.start();
						UpdaterThreadForListener3D u17 = new UpdaterThreadForListener3D(connectionJMS, topicEF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u17.start();
						UpdaterThreadForListener3D u18 = new UpdaterThreadForListener3D(connectionJMS, topicER,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u18.start();
						UpdaterThreadForListener3D u19 = new UpdaterThreadForListener3D(connectionJMS, topicNWF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u19.start();
						UpdaterThreadForListener3D u20 = new UpdaterThreadForListener3D(connectionJMS, topicNWR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u20.start();
						UpdaterThreadForListener3D u21 = new UpdaterThreadForListener3D(connectionJMS, topicNEF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u21.start();
						UpdaterThreadForListener3D u22 = new UpdaterThreadForListener3D(connectionJMS, topicNER,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u22.start();
						UpdaterThreadForListener3D u23 = new UpdaterThreadForListener3D(connectionJMS, topicSWF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u23.start();
						UpdaterThreadForListener3D u24 = new UpdaterThreadForListener3D(connectionJMS, topicSWR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u24.start();
						UpdaterThreadForListener3D u25 = new UpdaterThreadForListener3D(connectionJMS, topicSEF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u25.start();
						UpdaterThreadForListener3D u26 = new UpdaterThreadForListener3D(connectionJMS, topicSER,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u26.start();



					}else{ // rows>1 && columns>1 && lenght==1
						connectionJMS.createTopic(topicPrefix+TYPE+"N",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"S",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"W",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"E",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
//						connectionJMS.createTopic(topicPrefix+TYPE+"F",
//								((DistributedMultiSchedule)dm.schedule).fields3D.size());
//						connectionJMS.createTopic(topicPrefix+TYPE+"R",
//								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"WF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"WR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"EF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"ER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());

						String topicN=topicPrefix+(((i-1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+lenghts)%lenghts))+"N";
						String topicS=topicPrefix+(((i+1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+lenghts)%lenghts))+"S";
						String topicW=topicPrefix+(((i+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"W";
						String topicE=topicPrefix+(((i+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"E";
//						String topicF=topicPrefix+(((i+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"F";
//						String topicR=topicPrefix+(((i+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"R";

						String topicNF=topicPrefix+(((i-1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"NF";
						String topicNW=topicPrefix+(((i-1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"NW";
						String topicNR=topicPrefix+(((i-1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"NR";
						String topicNE=topicPrefix+(((i-1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"NE";
						String topicSF=topicPrefix+(((i+1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"SF";
						String topicSW=topicPrefix+(((i+1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"SW";
						String topicSR=topicPrefix+(((i+1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"SR";
						String topicSE=topicPrefix+(((i+1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"SE";
						String topicWF=topicPrefix+(((i+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"WF";
						String topicWR=topicPrefix+(((i+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"WR";
						String topicEF=topicPrefix+(((i+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"EF";
						String topicER=topicPrefix+(((i+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"ER";

						String topicNWF=topicPrefix+(((i-1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"NWF";
						String topicNWR=topicPrefix+(((i-1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"NWR";
						String topicNEF=topicPrefix+(((i-1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"NEF";
						String topicNER=topicPrefix+(((i-1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"NER";
						String topicSWF=topicPrefix+(((i+1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"SWF";
						String topicSWR=topicPrefix+(((i+1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"SWR";
						String topicSEF=topicPrefix+(((i+1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"SEF";
						String topicSER=topicPrefix+(((i+1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"SER";

						connectionJMS.subscribeToTopic(topicN);
						connectionJMS.subscribeToTopic(topicS);
						connectionJMS.subscribeToTopic(topicW);
						connectionJMS.subscribeToTopic(topicE);
//						connectionJMS.subscribeToTopic(topicF);
//						connectionJMS.subscribeToTopic(topicR);
						connectionJMS.subscribeToTopic(topicNF);
						connectionJMS.subscribeToTopic(topicNW);
						connectionJMS.subscribeToTopic(topicNR);
						connectionJMS.subscribeToTopic(topicNE);
						connectionJMS.subscribeToTopic(topicSF);
						connectionJMS.subscribeToTopic(topicSW);
						connectionJMS.subscribeToTopic(topicSR);
						connectionJMS.subscribeToTopic(topicSE);
						connectionJMS.subscribeToTopic(topicWF);
						connectionJMS.subscribeToTopic(topicWR);
						connectionJMS.subscribeToTopic(topicER);
						connectionJMS.subscribeToTopic(topicEF);
						connectionJMS.subscribeToTopic(topicNWF);
						connectionJMS.subscribeToTopic(topicNWR);
						connectionJMS.subscribeToTopic(topicNEF);
						connectionJMS.subscribeToTopic(topicNER);
						connectionJMS.subscribeToTopic(topicSWF);
						connectionJMS.subscribeToTopic(topicSWR);
						connectionJMS.subscribeToTopic(topicSEF);
						connectionJMS.subscribeToTopic(topicSER);

						UpdaterThreadForListener3D u1 = new UpdaterThreadForListener3D(connectionJMS,topicN,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u1.start();
						UpdaterThreadForListener3D u2 = new UpdaterThreadForListener3D(connectionJMS, topicS,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u2.start();
						UpdaterThreadForListener3D u3 = new UpdaterThreadForListener3D(connectionJMS, topicW,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u3.start();
						UpdaterThreadForListener3D u4 = new UpdaterThreadForListener3D(connectionJMS, topicE,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u4.start();
//						UpdaterThreadForListener3D u5 = new UpdaterThreadForListener3D(connectionJMS, topicF,
//								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
//						u5.start();
//						UpdaterThreadForListener3D u6 = new UpdaterThreadForListener3D(connectionJMS, topicR,
//								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
//						u6.start();
						UpdaterThreadForListener3D u7 = new UpdaterThreadForListener3D(connectionJMS, topicNF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u7.start();
						UpdaterThreadForListener3D u8 = new UpdaterThreadForListener3D(connectionJMS, topicNW,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u8.start();
						UpdaterThreadForListener3D u9 = new UpdaterThreadForListener3D(connectionJMS, topicNR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u9.start();
						UpdaterThreadForListener3D u10 = new UpdaterThreadForListener3D(connectionJMS, topicNE,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u10.start();
						UpdaterThreadForListener3D u11 = new UpdaterThreadForListener3D(connectionJMS, topicSF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u11.start();
						UpdaterThreadForListener3D u12 = new UpdaterThreadForListener3D(connectionJMS, topicSW,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u12.start();
						UpdaterThreadForListener3D u13= new UpdaterThreadForListener3D(connectionJMS, topicSR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u13.start();
						UpdaterThreadForListener3D u14 = new UpdaterThreadForListener3D(connectionJMS, topicSE,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u14.start();
						UpdaterThreadForListener3D u15 = new UpdaterThreadForListener3D(connectionJMS, topicWF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u15.start();
						UpdaterThreadForListener3D u16 = new UpdaterThreadForListener3D(connectionJMS, topicWR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u16.start();
						UpdaterThreadForListener3D u17 = new UpdaterThreadForListener3D(connectionJMS, topicEF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u17.start();
						UpdaterThreadForListener3D u18 = new UpdaterThreadForListener3D(connectionJMS, topicER,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u18.start();
						UpdaterThreadForListener3D u19 = new UpdaterThreadForListener3D(connectionJMS, topicNWF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u19.start();
						UpdaterThreadForListener3D u20 = new UpdaterThreadForListener3D(connectionJMS, topicNWR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u20.start();
						UpdaterThreadForListener3D u21 = new UpdaterThreadForListener3D(connectionJMS, topicNEF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u21.start();
						UpdaterThreadForListener3D u22 = new UpdaterThreadForListener3D(connectionJMS, topicNER,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u22.start();
						UpdaterThreadForListener3D u23 = new UpdaterThreadForListener3D(connectionJMS, topicSWF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u23.start();
						UpdaterThreadForListener3D u24 = new UpdaterThreadForListener3D(connectionJMS, topicSWR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u24.start();
						UpdaterThreadForListener3D u25 = new UpdaterThreadForListener3D(connectionJMS, topicSEF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u25.start();
						UpdaterThreadForListener3D u26 = new UpdaterThreadForListener3D(connectionJMS, topicSER,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u26.start();

					}
				}else{ //length>1
					if (rows==1 && columns==1){
//						connectionJMS.createTopic(topicPrefix+TYPE+"N",
//								((DistributedMultiSchedule)dm.schedule).fields3D.size());
//						connectionJMS.createTopic(topicPrefix+TYPE+"S",
//								((DistributedMultiSchedule)dm.schedule).fields3D.size());
//						connectionJMS.createTopic(topicPrefix+TYPE+"W",
//								((DistributedMultiSchedule)dm.schedule).fields3D.size());
//						connectionJMS.createTopic(topicPrefix+TYPE+"E",
//								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"F",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"R",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"WF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"WR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"EF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"ER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());

//						String topicN=topicPrefix+(((i-1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+lenghts)%lenghts))+"N";
//						String topicS=topicPrefix+(((i+1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+lenghts)%lenghts))+"S";
//						String topicW=topicPrefix+(((i+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"W";
//						String topicE=topicPrefix+(((i+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"E";
						String topicF=topicPrefix+(((i+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"F";
						String topicR=topicPrefix+(((i+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"R";

						String topicNF=topicPrefix+(((i-1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"NF";
						String topicNW=topicPrefix+(((i-1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"NW";
						String topicNR=topicPrefix+(((i-1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"NR";
						String topicNE=topicPrefix+(((i-1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"NE";
						String topicSF=topicPrefix+(((i+1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"SF";
						String topicSW=topicPrefix+(((i+1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"SW";
						String topicSR=topicPrefix+(((i+1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"SR";
						String topicSE=topicPrefix+(((i+1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"SE";
						String topicWF=topicPrefix+(((i+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"WF";
						String topicWR=topicPrefix+(((i+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"WR";
						String topicEF=topicPrefix+(((i+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"EF";
						String topicER=topicPrefix+(((i+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"ER";

						String topicNWF=topicPrefix+(((i-1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"NWF";
						String topicNWR=topicPrefix+(((i-1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"NWR";
						String topicNEF=topicPrefix+(((i-1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"NEF";
						String topicNER=topicPrefix+(((i-1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"NER";
						String topicSWF=topicPrefix+(((i+1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"SWF";
						String topicSWR=topicPrefix+(((i+1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"SWR";
						String topicSEF=topicPrefix+(((i+1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"SEF";
						String topicSER=topicPrefix+(((i+1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"SER";

//						connectionJMS.subscribeToTopic(topicN);
//						connectionJMS.subscribeToTopic(topicS);
//						connectionJMS.subscribeToTopic(topicW);
//						connectionJMS.subscribeToTopic(topicE);
						connectionJMS.subscribeToTopic(topicF);
						connectionJMS.subscribeToTopic(topicR);
						connectionJMS.subscribeToTopic(topicNF);
						connectionJMS.subscribeToTopic(topicNW);
						connectionJMS.subscribeToTopic(topicNR);
						connectionJMS.subscribeToTopic(topicNE);
						connectionJMS.subscribeToTopic(topicSF);
						connectionJMS.subscribeToTopic(topicSW);
						connectionJMS.subscribeToTopic(topicSR);
						connectionJMS.subscribeToTopic(topicSE);
						connectionJMS.subscribeToTopic(topicWF);
						connectionJMS.subscribeToTopic(topicWR);
						connectionJMS.subscribeToTopic(topicER);
						connectionJMS.subscribeToTopic(topicEF);
						connectionJMS.subscribeToTopic(topicNWF);
						connectionJMS.subscribeToTopic(topicNWR);
						connectionJMS.subscribeToTopic(topicNEF);
						connectionJMS.subscribeToTopic(topicNER);
						connectionJMS.subscribeToTopic(topicSWF);
						connectionJMS.subscribeToTopic(topicSWR);
						connectionJMS.subscribeToTopic(topicSEF);
						connectionJMS.subscribeToTopic(topicSER);

//						UpdaterThreadForListener3D u1 = new UpdaterThreadForListener3D(connectionJMS,topicN,
//								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
//						u1.start();
//						UpdaterThreadForListener3D u2 = new UpdaterThreadForListener3D(connectionJMS, topicS,
//								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
//						u2.start();
//						UpdaterThreadForListener3D u3 = new UpdaterThreadForListener3D(connectionJMS, topicW,
//								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
//						u3.start();
//						UpdaterThreadForListener3D u4 = new UpdaterThreadForListener3D(connectionJMS, topicE,
//								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
//						u4.start();
						UpdaterThreadForListener3D u5 = new UpdaterThreadForListener3D(connectionJMS, topicF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u5.start();
						UpdaterThreadForListener3D u6 = new UpdaterThreadForListener3D(connectionJMS, topicR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u6.start();
						UpdaterThreadForListener3D u7 = new UpdaterThreadForListener3D(connectionJMS, topicNF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u7.start();
						UpdaterThreadForListener3D u8 = new UpdaterThreadForListener3D(connectionJMS, topicNW,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u8.start();
						UpdaterThreadForListener3D u9 = new UpdaterThreadForListener3D(connectionJMS, topicNR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u9.start();
						UpdaterThreadForListener3D u10 = new UpdaterThreadForListener3D(connectionJMS, topicNE,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u10.start();
						UpdaterThreadForListener3D u11 = new UpdaterThreadForListener3D(connectionJMS, topicSF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u11.start();
						UpdaterThreadForListener3D u12 = new UpdaterThreadForListener3D(connectionJMS, topicSW,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u12.start();
						UpdaterThreadForListener3D u13= new UpdaterThreadForListener3D(connectionJMS, topicSR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u13.start();
						UpdaterThreadForListener3D u14 = new UpdaterThreadForListener3D(connectionJMS, topicSE,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u14.start();
						UpdaterThreadForListener3D u15 = new UpdaterThreadForListener3D(connectionJMS, topicWF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u15.start();
						UpdaterThreadForListener3D u16 = new UpdaterThreadForListener3D(connectionJMS, topicWR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u16.start();
						UpdaterThreadForListener3D u17 = new UpdaterThreadForListener3D(connectionJMS, topicEF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u17.start();
						UpdaterThreadForListener3D u18 = new UpdaterThreadForListener3D(connectionJMS, topicER,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u18.start();
						UpdaterThreadForListener3D u19 = new UpdaterThreadForListener3D(connectionJMS, topicNWF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u19.start();
						UpdaterThreadForListener3D u20 = new UpdaterThreadForListener3D(connectionJMS, topicNWR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u20.start();
						UpdaterThreadForListener3D u21 = new UpdaterThreadForListener3D(connectionJMS, topicNEF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u21.start();
						UpdaterThreadForListener3D u22 = new UpdaterThreadForListener3D(connectionJMS, topicNER,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u22.start();
						UpdaterThreadForListener3D u23 = new UpdaterThreadForListener3D(connectionJMS, topicSWF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u23.start();
						UpdaterThreadForListener3D u24 = new UpdaterThreadForListener3D(connectionJMS, topicSWR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u24.start();
						UpdaterThreadForListener3D u25 = new UpdaterThreadForListener3D(connectionJMS, topicSEF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u25.start();
						UpdaterThreadForListener3D u26 = new UpdaterThreadForListener3D(connectionJMS, topicSER,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u26.start();
//


					}else if(rows>1 && columns==1){

						connectionJMS.createTopic(topicPrefix+TYPE+"N",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"S",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
//						connectionJMS.createTopic(topicPrefix+TYPE+"W",
//								((DistributedMultiSchedule)dm.schedule).fields3D.size());
//						connectionJMS.createTopic(topicPrefix+TYPE+"E",
//								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"F",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"R",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"WF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"WR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"EF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"ER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());

						String topicN=topicPrefix+(((i-1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+lenghts)%lenghts))+"N";
						String topicS=topicPrefix+(((i+1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+lenghts)%lenghts))+"S";
//						String topicW=topicPrefix+(((i+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"W";
//						String topicE=topicPrefix+(((i+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"E";
						String topicF=topicPrefix+(((i+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"F";
						String topicR=topicPrefix+(((i+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"R";

						String topicNF=topicPrefix+(((i-1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"NF";
						String topicNW=topicPrefix+(((i-1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"NW";
						String topicNR=topicPrefix+(((i-1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"NR";
						String topicNE=topicPrefix+(((i-1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"NE";
						String topicSF=topicPrefix+(((i+1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"SF";
						String topicSW=topicPrefix+(((i+1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"SW";
						String topicSR=topicPrefix+(((i+1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"SR";
						String topicSE=topicPrefix+(((i+1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"SE";
						String topicWF=topicPrefix+(((i+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"WF";
						String topicWR=topicPrefix+(((i+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"WR";
						String topicEF=topicPrefix+(((i+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"EF";
						String topicER=topicPrefix+(((i+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"ER";

						String topicNWF=topicPrefix+(((i-1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"NWF";
						String topicNWR=topicPrefix+(((i-1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"NWR";
						String topicNEF=topicPrefix+(((i-1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"NEF";
						String topicNER=topicPrefix+(((i-1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"NER";
						String topicSWF=topicPrefix+(((i+1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"SWF";
						String topicSWR=topicPrefix+(((i+1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"SWR";
						String topicSEF=topicPrefix+(((i+1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"SEF";
						String topicSER=topicPrefix+(((i+1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"SER";

						connectionJMS.subscribeToTopic(topicN);
						connectionJMS.subscribeToTopic(topicS);
//						connectionJMS.subscribeToTopic(topicW);
//						connectionJMS.subscribeToTopic(topicE);
						connectionJMS.subscribeToTopic(topicF);
						connectionJMS.subscribeToTopic(topicR);
						connectionJMS.subscribeToTopic(topicNF);
						connectionJMS.subscribeToTopic(topicNW);
						connectionJMS.subscribeToTopic(topicNR);
						connectionJMS.subscribeToTopic(topicNE);
						connectionJMS.subscribeToTopic(topicSF);
						connectionJMS.subscribeToTopic(topicSW);
						connectionJMS.subscribeToTopic(topicSR);
						connectionJMS.subscribeToTopic(topicSE);
						connectionJMS.subscribeToTopic(topicWF);
						connectionJMS.subscribeToTopic(topicWR);
						connectionJMS.subscribeToTopic(topicER);
						connectionJMS.subscribeToTopic(topicEF);
						connectionJMS.subscribeToTopic(topicNWF);
						connectionJMS.subscribeToTopic(topicNWR);
						connectionJMS.subscribeToTopic(topicNEF);
						connectionJMS.subscribeToTopic(topicNER);
						connectionJMS.subscribeToTopic(topicSWF);
						connectionJMS.subscribeToTopic(topicSWR);
						connectionJMS.subscribeToTopic(topicSEF);
						connectionJMS.subscribeToTopic(topicSER);

						UpdaterThreadForListener3D u1 = new UpdaterThreadForListener3D(connectionJMS,topicN,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u1.start();
						UpdaterThreadForListener3D u2 = new UpdaterThreadForListener3D(connectionJMS, topicS,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u2.start();
//						UpdaterThreadForListener3D u3 = new UpdaterThreadForListener3D(connectionJMS, topicW,
//								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
//						u3.start();
//						UpdaterThreadForListener3D u4 = new UpdaterThreadForListener3D(connectionJMS, topicE,
//								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
//						u4.start();
						UpdaterThreadForListener3D u5 = new UpdaterThreadForListener3D(connectionJMS, topicF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u5.start();
						UpdaterThreadForListener3D u6 = new UpdaterThreadForListener3D(connectionJMS, topicR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u6.start();
						UpdaterThreadForListener3D u7 = new UpdaterThreadForListener3D(connectionJMS, topicNF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u7.start();
						UpdaterThreadForListener3D u8 = new UpdaterThreadForListener3D(connectionJMS, topicNW,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u8.start();
						UpdaterThreadForListener3D u9 = new UpdaterThreadForListener3D(connectionJMS, topicNR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u9.start();
						UpdaterThreadForListener3D u10 = new UpdaterThreadForListener3D(connectionJMS, topicNE,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u10.start();
						UpdaterThreadForListener3D u11 = new UpdaterThreadForListener3D(connectionJMS, topicSF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u11.start();
						UpdaterThreadForListener3D u12 = new UpdaterThreadForListener3D(connectionJMS, topicSW,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u12.start();
						UpdaterThreadForListener3D u13= new UpdaterThreadForListener3D(connectionJMS, topicSR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u13.start();
						UpdaterThreadForListener3D u14 = new UpdaterThreadForListener3D(connectionJMS, topicSE,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u14.start();
						UpdaterThreadForListener3D u15 = new UpdaterThreadForListener3D(connectionJMS, topicWF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u15.start();
						UpdaterThreadForListener3D u16 = new UpdaterThreadForListener3D(connectionJMS, topicWR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u16.start();
						UpdaterThreadForListener3D u17 = new UpdaterThreadForListener3D(connectionJMS, topicEF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u17.start();
						UpdaterThreadForListener3D u18 = new UpdaterThreadForListener3D(connectionJMS, topicER,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u18.start();
						UpdaterThreadForListener3D u19 = new UpdaterThreadForListener3D(connectionJMS, topicNWF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u19.start();
						UpdaterThreadForListener3D u20 = new UpdaterThreadForListener3D(connectionJMS, topicNWR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u20.start();
						UpdaterThreadForListener3D u21 = new UpdaterThreadForListener3D(connectionJMS, topicNEF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u21.start();
						UpdaterThreadForListener3D u22 = new UpdaterThreadForListener3D(connectionJMS, topicNER,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u22.start();
						UpdaterThreadForListener3D u23 = new UpdaterThreadForListener3D(connectionJMS, topicSWF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u23.start();
						UpdaterThreadForListener3D u24 = new UpdaterThreadForListener3D(connectionJMS, topicSWR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u24.start();
						UpdaterThreadForListener3D u25 = new UpdaterThreadForListener3D(connectionJMS, topicSEF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u25.start();
						UpdaterThreadForListener3D u26 = new UpdaterThreadForListener3D(connectionJMS, topicSER,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u26.start();


					}else if(rows==1 && columns>1){
//						connectionJMS.createTopic(topicPrefix+TYPE+"N",
//								((DistributedMultiSchedule)dm.schedule).fields3D.size());
//						connectionJMS.createTopic(topicPrefix+TYPE+"S",
//								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"W",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"E",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"F",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"R",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"WF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"WR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"EF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"ER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());

//						String topicN=topicPrefix+(((i-1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+lenghts)%lenghts))+"N";
//						String topicS=topicPrefix+(((i+1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+lenghts)%lenghts))+"S";
						String topicW=topicPrefix+(((i+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"W";
						String topicE=topicPrefix+(((i+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"E";
						String topicF=topicPrefix+(((i+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"F";
						String topicR=topicPrefix+(((i+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"R";

						String topicNF=topicPrefix+(((i-1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"NF";
						String topicNW=topicPrefix+(((i-1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"NW";
						String topicNR=topicPrefix+(((i-1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"NR";
						String topicNE=topicPrefix+(((i-1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"NE";
						String topicSF=topicPrefix+(((i+1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"SF";
						String topicSW=topicPrefix+(((i+1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"SW";
						String topicSR=topicPrefix+(((i+1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"SR";
						String topicSE=topicPrefix+(((i+1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"SE";
						String topicWF=topicPrefix+(((i+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"WF";
						String topicWR=topicPrefix+(((i+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"WR";
						String topicEF=topicPrefix+(((i+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"EF";
						String topicER=topicPrefix+(((i+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"ER";

						String topicNWF=topicPrefix+(((i-1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"NWF";
						String topicNWR=topicPrefix+(((i-1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"NWR";
						String topicNEF=topicPrefix+(((i-1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"NEF";
						String topicNER=topicPrefix+(((i-1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"NER";
						String topicSWF=topicPrefix+(((i+1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"SWF";
						String topicSWR=topicPrefix+(((i+1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"SWR";
						String topicSEF=topicPrefix+(((i+1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"SEF";
						String topicSER=topicPrefix+(((i+1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"SER";

//						connectionJMS.subscribeToTopic(topicN);
//						connectionJMS.subscribeToTopic(topicS);
						connectionJMS.subscribeToTopic(topicW);
						connectionJMS.subscribeToTopic(topicE);
						connectionJMS.subscribeToTopic(topicF);
						connectionJMS.subscribeToTopic(topicR);
						connectionJMS.subscribeToTopic(topicNF);
						connectionJMS.subscribeToTopic(topicNW);
						connectionJMS.subscribeToTopic(topicNR);
						connectionJMS.subscribeToTopic(topicNE);
						connectionJMS.subscribeToTopic(topicSF);
						connectionJMS.subscribeToTopic(topicSW);
						connectionJMS.subscribeToTopic(topicSR);
						connectionJMS.subscribeToTopic(topicSE);
						connectionJMS.subscribeToTopic(topicWF);
						connectionJMS.subscribeToTopic(topicWR);
						connectionJMS.subscribeToTopic(topicER);
						connectionJMS.subscribeToTopic(topicEF);
						connectionJMS.subscribeToTopic(topicNWF);
						connectionJMS.subscribeToTopic(topicNWR);
						connectionJMS.subscribeToTopic(topicNEF);
						connectionJMS.subscribeToTopic(topicNER);
						connectionJMS.subscribeToTopic(topicSWF);
						connectionJMS.subscribeToTopic(topicSWR);
						connectionJMS.subscribeToTopic(topicSEF);
						connectionJMS.subscribeToTopic(topicSER);

//						UpdaterThreadForListener3D u1 = new UpdaterThreadForListener3D(connectionJMS,topicN,
//								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
//						u1.start();
//						UpdaterThreadForListener3D u2 = new UpdaterThreadForListener3D(connectionJMS, topicS,
//								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
//						u2.start();
						UpdaterThreadForListener3D u3 = new UpdaterThreadForListener3D(connectionJMS, topicW,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u3.start();
						UpdaterThreadForListener3D u4 = new UpdaterThreadForListener3D(connectionJMS, topicE,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u4.start();
						UpdaterThreadForListener3D u5 = new UpdaterThreadForListener3D(connectionJMS, topicF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u5.start();
						UpdaterThreadForListener3D u6 = new UpdaterThreadForListener3D(connectionJMS, topicR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u6.start();
						UpdaterThreadForListener3D u7 = new UpdaterThreadForListener3D(connectionJMS, topicNF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u7.start();
						UpdaterThreadForListener3D u8 = new UpdaterThreadForListener3D(connectionJMS, topicNW,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u8.start();
						UpdaterThreadForListener3D u9 = new UpdaterThreadForListener3D(connectionJMS, topicNR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u9.start();
						UpdaterThreadForListener3D u10 = new UpdaterThreadForListener3D(connectionJMS, topicNE,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u10.start();
						UpdaterThreadForListener3D u11 = new UpdaterThreadForListener3D(connectionJMS, topicSF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u11.start();
						UpdaterThreadForListener3D u12 = new UpdaterThreadForListener3D(connectionJMS, topicSW,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u12.start();
						UpdaterThreadForListener3D u13= new UpdaterThreadForListener3D(connectionJMS, topicSR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u13.start();
						UpdaterThreadForListener3D u14 = new UpdaterThreadForListener3D(connectionJMS, topicSE,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u14.start();
						UpdaterThreadForListener3D u15 = new UpdaterThreadForListener3D(connectionJMS, topicWF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u15.start();
						UpdaterThreadForListener3D u16 = new UpdaterThreadForListener3D(connectionJMS, topicWR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u16.start();
						UpdaterThreadForListener3D u17 = new UpdaterThreadForListener3D(connectionJMS, topicEF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u17.start();
						UpdaterThreadForListener3D u18 = new UpdaterThreadForListener3D(connectionJMS, topicER,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u18.start();
						UpdaterThreadForListener3D u19 = new UpdaterThreadForListener3D(connectionJMS, topicNWF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u19.start();
						UpdaterThreadForListener3D u20 = new UpdaterThreadForListener3D(connectionJMS, topicNWR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u20.start();
						UpdaterThreadForListener3D u21 = new UpdaterThreadForListener3D(connectionJMS, topicNEF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u21.start();
						UpdaterThreadForListener3D u22 = new UpdaterThreadForListener3D(connectionJMS, topicNER,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u22.start();
						UpdaterThreadForListener3D u23 = new UpdaterThreadForListener3D(connectionJMS, topicSWF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u23.start();
						UpdaterThreadForListener3D u24 = new UpdaterThreadForListener3D(connectionJMS, topicSWR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u24.start();
						UpdaterThreadForListener3D u25 = new UpdaterThreadForListener3D(connectionJMS, topicSEF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u25.start();
						UpdaterThreadForListener3D u26 = new UpdaterThreadForListener3D(connectionJMS, topicSER,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u26.start();
//

					}else {
						connectionJMS.createTopic(topicPrefix+TYPE+"N",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"S",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"W",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"E",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"F",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"R",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"WF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"WR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"EF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"ER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"NER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionJMS.createTopic(topicPrefix+TYPE+"SER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());

						String topicN=topicPrefix+(((i-1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+lenghts)%lenghts))+"N";
						String topicS=topicPrefix+(((i+1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+lenghts)%lenghts))+"S";
						String topicW=topicPrefix+(((i+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"W";
						String topicE=topicPrefix+(((i+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"E";
						String topicF=topicPrefix+(((i+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"F";
						String topicR=topicPrefix+(((i+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"R";

						String topicNF=topicPrefix+(((i-1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"NF";
						String topicNW=topicPrefix+(((i-1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"NW";
						String topicNR=topicPrefix+(((i-1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"NR";
						String topicNE=topicPrefix+(((i-1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"NE";
						String topicSF=topicPrefix+(((i+1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"SF";
						String topicSW=topicPrefix+(((i+1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"SW";
						String topicSR=topicPrefix+(((i+1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"SR";
						String topicSE=topicPrefix+(((i+1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"SE";
						String topicWF=topicPrefix+(((i+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"WF";
						String topicWR=topicPrefix+(((i+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"WR";
						String topicEF=topicPrefix+(((i+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"EF";
						String topicER=topicPrefix+(((i+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"ER";

						String topicNWF=topicPrefix+(((i-1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"NWF";
						String topicNWR=topicPrefix+(((i-1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"NWR";
						String topicNEF=topicPrefix+(((i-1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"NEF";
						String topicNER=topicPrefix+(((i-1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"NER";
						String topicSWF=topicPrefix+(((i+1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"SWF";
						String topicSWR=topicPrefix+(((i+1+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"SWR";
						String topicSEF=topicPrefix+(((i+1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z-1+lenghts)%lenghts))+"SEF";
						String topicSER=topicPrefix+(((i+1+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+1+lenghts)%lenghts))+"SER";

						connectionJMS.subscribeToTopic(topicN);
						connectionJMS.subscribeToTopic(topicS);
						connectionJMS.subscribeToTopic(topicW);
						connectionJMS.subscribeToTopic(topicE);
						connectionJMS.subscribeToTopic(topicF);
						connectionJMS.subscribeToTopic(topicR);
						connectionJMS.subscribeToTopic(topicNF);
						connectionJMS.subscribeToTopic(topicNW);
						connectionJMS.subscribeToTopic(topicNR);
						connectionJMS.subscribeToTopic(topicNE);
						connectionJMS.subscribeToTopic(topicSF);
						connectionJMS.subscribeToTopic(topicSW);
						connectionJMS.subscribeToTopic(topicSR);
						connectionJMS.subscribeToTopic(topicSE);
						connectionJMS.subscribeToTopic(topicWF);
						connectionJMS.subscribeToTopic(topicWR);
						connectionJMS.subscribeToTopic(topicER);
						connectionJMS.subscribeToTopic(topicEF);
						connectionJMS.subscribeToTopic(topicNWF);
						connectionJMS.subscribeToTopic(topicNWR);
						connectionJMS.subscribeToTopic(topicNEF);
						connectionJMS.subscribeToTopic(topicNER);
						connectionJMS.subscribeToTopic(topicSWF);
						connectionJMS.subscribeToTopic(topicSWR);
						connectionJMS.subscribeToTopic(topicSEF);
						connectionJMS.subscribeToTopic(topicSER);

						UpdaterThreadForListener3D u1 = new UpdaterThreadForListener3D(connectionJMS,topicN,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u1.start();
						UpdaterThreadForListener3D u2 = new UpdaterThreadForListener3D(connectionJMS, topicS,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u2.start();
						UpdaterThreadForListener3D u3 = new UpdaterThreadForListener3D(connectionJMS, topicW,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u3.start();
						UpdaterThreadForListener3D u4 = new UpdaterThreadForListener3D(connectionJMS, topicE,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u4.start();
						UpdaterThreadForListener3D u5 = new UpdaterThreadForListener3D(connectionJMS, topicF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u5.start();
						UpdaterThreadForListener3D u6 = new UpdaterThreadForListener3D(connectionJMS, topicR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u6.start();
						UpdaterThreadForListener3D u7 = new UpdaterThreadForListener3D(connectionJMS, topicNF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u7.start();
						UpdaterThreadForListener3D u8 = new UpdaterThreadForListener3D(connectionJMS, topicNW,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u8.start();
						UpdaterThreadForListener3D u9 = new UpdaterThreadForListener3D(connectionJMS, topicNR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u9.start();
						UpdaterThreadForListener3D u10 = new UpdaterThreadForListener3D(connectionJMS, topicNE,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u10.start();
						UpdaterThreadForListener3D u11 = new UpdaterThreadForListener3D(connectionJMS, topicSF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u11.start();
						UpdaterThreadForListener3D u12 = new UpdaterThreadForListener3D(connectionJMS, topicSW,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u12.start();
						UpdaterThreadForListener3D u13= new UpdaterThreadForListener3D(connectionJMS, topicSR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u13.start();
						UpdaterThreadForListener3D u14 = new UpdaterThreadForListener3D(connectionJMS, topicSE,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u14.start();
						UpdaterThreadForListener3D u15 = new UpdaterThreadForListener3D(connectionJMS, topicWF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u15.start();
						UpdaterThreadForListener3D u16 = new UpdaterThreadForListener3D(connectionJMS, topicWR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u16.start();
						UpdaterThreadForListener3D u17 = new UpdaterThreadForListener3D(connectionJMS, topicEF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u17.start();
						UpdaterThreadForListener3D u18 = new UpdaterThreadForListener3D(connectionJMS, topicER,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u18.start();
						UpdaterThreadForListener3D u19 = new UpdaterThreadForListener3D(connectionJMS, topicNWF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u19.start();
						UpdaterThreadForListener3D u20 = new UpdaterThreadForListener3D(connectionJMS, topicNWR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u20.start();
						UpdaterThreadForListener3D u21 = new UpdaterThreadForListener3D(connectionJMS, topicNEF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u21.start();
						UpdaterThreadForListener3D u22 = new UpdaterThreadForListener3D(connectionJMS, topicNER,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u22.start();
						UpdaterThreadForListener3D u23 = new UpdaterThreadForListener3D(connectionJMS, topicSWF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u23.start();
						UpdaterThreadForListener3D u24 = new UpdaterThreadForListener3D(connectionJMS, topicSWR,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u24.start();
						UpdaterThreadForListener3D u25 = new UpdaterThreadForListener3D(connectionJMS, topicSEF,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u25.start();
						UpdaterThreadForListener3D u26 = new UpdaterThreadForListener3D(connectionJMS, topicSER,
								((DistributedMultiSchedule)dm.schedule).fields3D, listeners3D);
						u26.start();

					}
				}
			}catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}




	@AuthorAnnotation(
			author = {"Francesco Milone","Carmine Spagnuolo"},
			date = "6/3/2014"
	)
	/**
	 * Setup topics for a  distributed field Network.
	 */
	protected void init_network_connection()
	{
		DistributedMultiSchedule dms = schedule;
		ArrayList<DNetwork> networkLists = dms.fieldsNetwork;
		String toSubscribe;

		HashMap<String, ArrayList<DistributedField>> listToPublish = new HashMap<String, ArrayList<DistributedField>>();
		HashMap<String, ArrayList<DNetwork>> listToSubscribe = new HashMap<String, ArrayList<DNetwork>>();


		for (DNetwork distributedNetwork : networkLists) {
			int my_community = (TYPE.pos_i*rows)+TYPE.pos_j;
			ArrayList<Integer> myPublishNeighborhood = distributedNetwork.grpsub.getSubscribers(my_community);
			for (Integer integer : myPublishNeighborhood)
			{
				String my_topic = topicPrefix+"-Network-"+my_community+"-"+integer;
				if(listToPublish.get(my_topic)==null)
				{
					listToPublish.put(my_topic, new ArrayList<DistributedField>());
				}
				listToPublish.get(my_topic).add(distributedNetwork);
			}
			ArrayList<Integer> myNeighborhood = distributedNetwork.grpsub.getPublisher(my_community);
			for (Integer integer : myNeighborhood) {

				toSubscribe=topicPrefix+"-Network-"+integer+"-"+my_community;
				if(listToSubscribe.get(toSubscribe)==null)
				{
					listToSubscribe.put(toSubscribe, new ArrayList<DNetwork>());

				}
				listToSubscribe.get(toSubscribe).add(distributedNetwork);

				if(networkNumberOfSubscribersForField.get(distributedNetwork.getDistributedFieldID())==null)
					networkNumberOfSubscribersForField.put(distributedNetwork.getDistributedFieldID(), new Integer(0));
				networkNumberOfSubscribersForField.put(distributedNetwork.getDistributedFieldID(),
						(networkNumberOfSubscribersForField.get(distributedNetwork.getDistributedFieldID())+1));
			}
		}

		for (DNetwork distributedNetwork : networkLists) {
			((DistributedFieldNetwork)distributedNetwork).setNumberOfUpdatesToSynchro(networkNumberOfSubscribersForField.get(distributedNetwork.getDistributedFieldID()));
		}

		Set<String> keySetToPublish = listToPublish.keySet();
		for(String topicName : keySetToPublish)
		{
			ArrayList<DistributedField> publishers = listToPublish.get(topicName);
			try {
				connectionJMS.createTopic(topicName, publishers.size());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Set<String> keySetToSubscribe = listToSubscribe.keySet();
		for(String topicName : keySetToSubscribe)
		{
			ArrayList<DNetwork> subscribers = listToSubscribe.get(topicName);
			try {
				connectionJMS.subscribeToTopic(topicName);
			} catch (Exception e) {
				e.printStackTrace();
			}

			UpdaterThreadJMSForNetworkListener utfl = new UpdaterThreadJMSForNetworkListener(connectionJMS, topicName, subscribers, networkListeners);
			utfl.start();
		}
	}

	public CellType getType() {
		return TYPE;
	}

	public ConnectionJMS getConnection() {
		return connectionJMS;
	}

	public ArrayList<MessageListener> getLocalListener() {
		return listeners;
	}

	public Trigger getTrigger() {
		return TRIGGER;
	}

	//added for close connection of current simulation after reset
	public void closeConnectionJMS() throws JMSException
	{
		connectionJMS.close();
	}

	public void init_service_connection() {
		dm.upVar=new UpdateGlobalVarAtStep(dm);
		ThreadVisualizationCellMessageListener thread = new ThreadVisualizationCellMessageListener(
				connectionJMS,
				((DistributedMultiSchedule) this.schedule));
		thread.start();

		try {
			boolean a = connectionJMS.createTopic(topicPrefix+"GRAPHICS" + TYPE,
					schedule.fields2D.size());
			connectionJMS.subscribeToTopic(topicPrefix+"GRAPHICS" + TYPE);
			ThreadZoomInCellMessageListener t_zoom = new ThreadZoomInCellMessageListener(
					connectionJMS,
					TYPE.toString(), (DistributedMultiSchedule) this.schedule);
			t_zoom.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Support for Global Parameters
		try {
			connectionJMS.subscribeToTopic(topicPrefix + "GLOBAL_REDUCED");
			UpdaterThreadForGlobalsListener ug = new UpdaterThreadForGlobalsListener(
					connectionJMS,
					topicPrefix + "GLOBAL_REDUCED",
					schedule.fields2D,
					listeners);
			ug.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
}