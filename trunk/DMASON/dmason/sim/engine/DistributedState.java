/**
 * Copyright 2012 Università degli Studi di Salerno


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

package dmason.sim.engine;

import java.util.ArrayList;

import javax.jms.JMSException;

import dmason.sim.field.CellType;
import dmason.sim.field.DistributedField;
import dmason.sim.field.MessageListener;
import dmason.sim.field.UpdaterThreadForListener;
import dmason.util.connection.Address;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;
import dmason.util.trigger.Trigger;
import dmason.util.visualization.ThreadVisualizationCellMessageListener;
import dmason.util.visualization.ThreadZoomInCellMessageListener;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;

/**
 * An abstract class that inherits all SimState functionalities and adds
 * necessary distributed informations: the number of agents, used to calculate
 * the sequence of agents id, the type of simulated cell, the maximum shift of
 * agents, the number of peers involved in the simulation, the ip and port of
 * server.
 * 
 * @param <E>
 *            the type of locations
 */
public abstract class DistributedState<E> extends SimState {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int NUMAGENTS;
	private int count_id;
	public CellType TYPE;
	public int MAX_DISTANCE;
	public int NUMPEERS;
	public String ip;
	public String port;
	private int MODE;
	private boolean isTOROIDAL;
	
	private String topicPrefix = "";
	// private MersenneTwisterFast randomRegion;
	private ConnectionNFieldsWithActiveMQAPI connection;
	private Trigger TRIGGER;
	private ArrayList<MessageListener> listeners = new ArrayList<MessageListener>();
	private UpdaterThreadForListener u1;
	private UpdaterThreadForListener u2;
	private UpdaterThreadForListener u3;
	private UpdaterThreadForListener u4;
	private UpdaterThreadForListener u5;
	private UpdaterThreadForListener u6;
	private UpdaterThreadForListener u7;
	private UpdaterThreadForListener u8;
	public int rows;
	public int columns;

	public void init_connection() {

		try {
			connection.createTopic(topicPrefix+"GRAPHICS", 1);
			connection.subscribeToTopic(topicPrefix+"GRAPHICS");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ThreadVisualizationCellMessageListener thread = new ThreadVisualizationCellMessageListener(
				(ConnectionNFieldsWithActiveMQAPI) connection,
				((DistributedMultiSchedule) this.schedule));
		thread.start();

		try {
			boolean a = connection.createTopic(topicPrefix+"GRAPHICS" + TYPE,
					((DistributedMultiSchedule) super.schedule).fields.size());
			connection.subscribeToTopic(topicPrefix+"GRAPHICS" + TYPE);
			ThreadZoomInCellMessageListener t_zoom = new ThreadZoomInCellMessageListener(
					(ConnectionNFieldsWithActiveMQAPI) connection,
					TYPE.toString(), (DistributedMultiSchedule) this.schedule);
			t_zoom.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (isTOROIDAL)
			connection_IS_toroidal();
		else
			connection_NO_toroidal();
	}

	private void connection_IS_toroidal() {

		if (MODE == 0 || MODE == 3) { // HORIZONTAL_MODE

			try {

				connection.createTopic(topicPrefix+TYPE.pos_i + "-" + TYPE.pos_j + "L",
						((DistributedMultiSchedule) super.schedule).fields
								.size());
				connection.createTopic(topicPrefix+TYPE.pos_i + "-" + TYPE.pos_j + "R",
						((DistributedMultiSchedule) super.schedule).fields
								.size());

				connection.subscribeToTopic(topicPrefix+TYPE.pos_i + "-"
						+ ((TYPE.pos_j - 1 + NUMPEERS) % NUMPEERS) + "R");
				connection.subscribeToTopic(topicPrefix+TYPE.pos_i + "-"
						+ ((TYPE.pos_j + 1 + NUMPEERS) % NUMPEERS) + "L");

				UpdaterThreadForListener u1 = new UpdaterThreadForListener(
						connection, topicPrefix+TYPE.pos_i + "-"
								+ (((TYPE.pos_j - 1 + NUMPEERS)) % NUMPEERS)
								+ "R",
						((DistributedMultiSchedule) schedule).fields, listeners);
				u1.start();

				UpdaterThreadForListener u2 = new UpdaterThreadForListener(
						connection, topicPrefix+TYPE.pos_i + "-"
								+ (((TYPE.pos_j + 1 + NUMPEERS)) % NUMPEERS)
								+ "L",
						((DistributedMultiSchedule) schedule).fields, listeners);
				u2.start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (MODE==1){// SQUARE_MODE

			try {

				connection.createTopic(topicPrefix+TYPE + "L",
						((DistributedMultiSchedule) super.schedule).fields
								.size());
				connection.createTopic(topicPrefix+TYPE + "R",
						((DistributedMultiSchedule) super.schedule).fields
								.size());
				connection.createTopic(topicPrefix+TYPE + "D",
						((DistributedMultiSchedule) super.schedule).fields
								.size());
				connection.createTopic(topicPrefix+TYPE + "U",
						((DistributedMultiSchedule) super.schedule).fields
								.size());
				connection.createTopic(topicPrefix+TYPE + "CUDL",
						((DistributedMultiSchedule) super.schedule).fields
								.size());
				connection.createTopic(topicPrefix+TYPE + "CUDR",
						((DistributedMultiSchedule) super.schedule).fields
								.size());
				connection.createTopic(topicPrefix+TYPE + "CDDL",
						((DistributedMultiSchedule) super.schedule).fields
								.size());
				connection.createTopic(topicPrefix+TYPE + "CDDR",
						((DistributedMultiSchedule) super.schedule).fields
								.size());

				int i = TYPE.pos_i, j = TYPE.pos_j;
				int sqrt = (int) Math.sqrt(NUMPEERS);

				connection.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
						+ ((j + 1 + columns) % columns) + "L");
				connection.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
						+ ((j - 1 + columns) % columns) + "R");
				connection.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
						+ ((j + columns) % columns) + "U");
				connection.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
						+ ((j + columns) % columns) + "D");
				connection.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
						+ ((j - 1 + columns) % columns) + "CDDR");
				connection.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
						+ ((j + 1 + columns) % columns) + "CDDL");
				connection.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
						+ ((j - 1 + columns) % columns) + "CUDR");
				connection.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
						+ ((j + 1 + columns) % columns) + "CUDL");

				u1 = new UpdaterThreadForListener(
						connection, topicPrefix+((i + rows) % rows) + "-"
								+ ((j + 1 + columns) % columns) + "L",
						((DistributedMultiSchedule) schedule).fields, listeners);
				u1.start();

				u2 = new UpdaterThreadForListener(
						connection, topicPrefix+((i + rows) % rows) + "-"
								+ ((j - 1 + columns) % columns) + "R",
						((DistributedMultiSchedule) schedule).fields, listeners);
				u2.start();

				u3 = new UpdaterThreadForListener(
						connection, topicPrefix+((i + 1 + rows) % rows) + "-"
								+ ((j + columns) % columns) + "U",
						((DistributedMultiSchedule) schedule).fields, listeners);
				u3.start();

				u4 = new UpdaterThreadForListener(
						connection, topicPrefix+((i - 1 + rows) % rows) + "-"
								+ ((j + columns) % columns) + "D",
						((DistributedMultiSchedule) schedule).fields, listeners);
				u4.start();

				u5 = new UpdaterThreadForListener(
						connection, topicPrefix+((i - 1 + rows) % rows) + "-"
								+ ((j - 1 + columns) % columns) + "CDDR",
						((DistributedMultiSchedule) schedule).fields, listeners);
				u5.start();

				u6 = new UpdaterThreadForListener(
						connection, topicPrefix+((i - 1 + rows) % rows) + "-"
								+ ((j + 1 + columns) % columns) + "CDDL",
						((DistributedMultiSchedule) schedule).fields, listeners);
				u6.start();

				u7 = new UpdaterThreadForListener(
						connection, topicPrefix+((i + 1 + rows) % rows) + "-"
								+ ((j - 1 + columns) % columns) + "CUDR",
						((DistributedMultiSchedule) schedule).fields, listeners);
				u7.start();

				u8 = new UpdaterThreadForListener(
						connection, topicPrefix+((i + 1 + rows) % rows) + "-"
								+ ((j + 1 + columns) % columns) + "CUDL",
						((DistributedMultiSchedule) schedule).fields, listeners);
				u8.start();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if (MODE == 2) { // SQUARE BALANCED

			 try {

					connection.createTopic(topicPrefix+TYPE+"L",((DistributedMultiSchedule)super.schedule).fields.size());
					connection.createTopic(topicPrefix+TYPE+"R",((DistributedMultiSchedule)super.schedule).fields.size());
					connection.createTopic(topicPrefix+TYPE+"D",((DistributedMultiSchedule)super.schedule).fields.size());
					connection.createTopic(topicPrefix+TYPE+"U",((DistributedMultiSchedule)super.schedule).fields.size());
					connection.createTopic(topicPrefix+TYPE+"CUDL",((DistributedMultiSchedule)super.schedule).fields.size());
					connection.createTopic(topicPrefix+TYPE+"CUDR",((DistributedMultiSchedule)super.schedule).fields.size());
					connection.createTopic(topicPrefix+TYPE+"CDDL",((DistributedMultiSchedule)super.schedule).fields.size());
					connection.createTopic(topicPrefix+TYPE+"CDDR",((DistributedMultiSchedule)super.schedule).fields.size());
						
					int i=TYPE.pos_i,j=TYPE.pos_j;
					int sqrt=(int)Math.sqrt(NUMPEERS);
							
					connection.subscribeToTopic(topicPrefix+((i+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"L");
					connection.subscribeToTopic(topicPrefix+((i+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"R");
					connection.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"U");
					connection.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"D");
					connection.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"CDDR");
					connection.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"CDDL");
					connection.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"CUDR");
					connection.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"CUDL");
						
					u1 = new UpdaterThreadForListener(connection,topicPrefix+((i+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"L",((DistributedMultiSchedule)schedule).fields,listeners);
					u1.start();
						
					u2 = new UpdaterThreadForListener(connection, topicPrefix+((i+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"R",((DistributedMultiSchedule)schedule).fields,listeners);
					u2.start();
						
					u3 = new UpdaterThreadForListener(connection, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"U",((DistributedMultiSchedule)schedule).fields,listeners);
					u3.start();
						
					u4 = new UpdaterThreadForListener(connection, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"D",((DistributedMultiSchedule)schedule).fields,listeners);
					u4.start();
						
					u5 = new UpdaterThreadForListener(connection, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"CDDR",((DistributedMultiSchedule)schedule).fields,listeners);
					u5.start();
						
					u6 = new UpdaterThreadForListener(connection, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"CDDL",((DistributedMultiSchedule)schedule).fields,listeners);
					u6.start();	
						
					u7 = new UpdaterThreadForListener(connection, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"CUDR",((DistributedMultiSchedule)schedule).fields,listeners);
					u7.start();		
						
					u8 = new UpdaterThreadForListener(connection, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"CUDL",((DistributedMultiSchedule)schedule).fields,listeners);
					u8.start();
						
				}catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 }
		 
		
	}

	private void connection_NO_toroidal() {

		if (MODE == 0 || MODE == 3) { // HORIZONTAL_MODE

			try {

				connection.createTopic(topicPrefix+TYPE + "R",
						((DistributedMultiSchedule) super.schedule).fields
								.size());
				connection.createTopic(topicPrefix+TYPE + "L",
						((DistributedMultiSchedule) super.schedule).fields
								.size());

				connection.subscribeToTopic(topicPrefix+TYPE.getNeighbourLeft() + "R");
				UpdaterThreadForListener u1 = new UpdaterThreadForListener(
						connection, topicPrefix+TYPE.getNeighbourLeft() + "R",
						((DistributedMultiSchedule) schedule).fields, listeners);
				u1.start();

				connection.subscribeToTopic(topicPrefix+TYPE.getNeighbourRight() + "L");
				UpdaterThreadForListener u2 = new UpdaterThreadForListener(
						connection, topicPrefix+TYPE.getNeighbourRight() + "L",
						((DistributedMultiSchedule) schedule).fields, listeners);
				u2.start();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (MODE == 1) { // SQUARE NOT BALANCED

			try {

				connection.createTopic(topicPrefix+TYPE + "L",
						((DistributedMultiSchedule) super.schedule).fields
								.size());
				connection.createTopic(topicPrefix+TYPE + "R",
						((DistributedMultiSchedule) super.schedule).fields
								.size());
				connection.createTopic(topicPrefix+TYPE + "U",
						((DistributedMultiSchedule) super.schedule).fields
								.size());
				connection.createTopic(topicPrefix+TYPE + "D",
						((DistributedMultiSchedule) super.schedule).fields
								.size());
				connection.createTopic(topicPrefix+TYPE + "CDDR",
						((DistributedMultiSchedule) super.schedule).fields
								.size());
				connection.createTopic(topicPrefix+TYPE + "CUDR",
						((DistributedMultiSchedule) super.schedule).fields
								.size());
				connection.createTopic(topicPrefix+TYPE + "CDDL",
						((DistributedMultiSchedule) super.schedule).fields
								.size());
				connection.createTopic(topicPrefix+TYPE + "CUDL",
						((DistributedMultiSchedule) super.schedule).fields
								.size());

				connection.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagLeftUp()
						+ "CDDR");
				UpdaterThreadForListener u1 = new UpdaterThreadForListener(
						connection, topicPrefix+TYPE.getNeighbourDiagLeftUp() + "CDDR",
						((DistributedMultiSchedule) schedule).fields, listeners);
				u1.start();

				connection.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagRightUp()
						+ "CDDL");
				UpdaterThreadForListener u2 = new UpdaterThreadForListener(
						connection, topicPrefix+TYPE.getNeighbourDiagRightUp() + "CDDL",
						((DistributedMultiSchedule) schedule).fields, listeners);
				u2.start();

				connection.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagLeftDown()
						+ "CUDR");
				UpdaterThreadForListener u3 = new UpdaterThreadForListener(
						connection, topicPrefix+TYPE.getNeighbourDiagLeftDown() + "CUDR",
						((DistributedMultiSchedule) schedule).fields, listeners);
				u3.start();

				connection.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagRightDown()
						+ "CUDL");
				UpdaterThreadForListener u4 = new UpdaterThreadForListener(
						connection, topicPrefix+TYPE.getNeighbourDiagRightDown() + "CUDL",
						((DistributedMultiSchedule) schedule).fields, listeners);
				u4.start();

				connection.subscribeToTopic(topicPrefix+TYPE.getNeighbourLeft() + "R");
				UpdaterThreadForListener u5 = new UpdaterThreadForListener(
						connection, topicPrefix+TYPE.getNeighbourLeft() + "R",
						((DistributedMultiSchedule) schedule).fields, listeners);
				u5.start();

				connection.subscribeToTopic(topicPrefix+TYPE.getNeighbourRight() + "L");
				UpdaterThreadForListener u6 = new UpdaterThreadForListener(
						connection, topicPrefix+TYPE.getNeighbourRight() + "L",
						((DistributedMultiSchedule) schedule).fields, listeners);
				u6.start();

				connection.subscribeToTopic(topicPrefix+(TYPE.getNeighbourUp() + "D"));
				UpdaterThreadForListener u7 = new UpdaterThreadForListener(
						connection, topicPrefix+TYPE.getNeighbourUp() + "D",
						((DistributedMultiSchedule) schedule).fields, listeners);
				u7.start();

				connection.subscribeToTopic(topicPrefix+TYPE.getNeighbourDown() + "U");

				UpdaterThreadForListener u8 = new UpdaterThreadForListener(
						connection, topicPrefix+TYPE.getNeighbourDown() + "U",
						((DistributedMultiSchedule) schedule).fields, listeners);
				u8.start();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (MODE == 2) { // SQUARE BALANCED

			 try {

					connection.createTopic(topicPrefix+TYPE+"L",((DistributedMultiSchedule)super.schedule).fields.size());
					connection.createTopic(topicPrefix+TYPE+"R",((DistributedMultiSchedule)super.schedule).fields.size());
					connection.createTopic(topicPrefix+TYPE+"D",((DistributedMultiSchedule)super.schedule).fields.size());
					connection.createTopic(topicPrefix+TYPE+"U",((DistributedMultiSchedule)super.schedule).fields.size());
					connection.createTopic(topicPrefix+TYPE+"CUDL",((DistributedMultiSchedule)super.schedule).fields.size());
					connection.createTopic(topicPrefix+TYPE+"CUDR",((DistributedMultiSchedule)super.schedule).fields.size());
					connection.createTopic(topicPrefix+TYPE+"CDDL",((DistributedMultiSchedule)super.schedule).fields.size());
					connection.createTopic(topicPrefix+TYPE+"CDDR",((DistributedMultiSchedule)super.schedule).fields.size());
						
					int i=TYPE.pos_i,j=TYPE.pos_j;
					int sqrt=(int)Math.sqrt(NUMPEERS);
							
					connection.subscribeToTopic(topicPrefix+((i+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"L");
					connection.subscribeToTopic(topicPrefix+((i+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"R");
					connection.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"U");
					connection.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"D");
					connection.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"CDDR");
					connection.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"CDDL");
					connection.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"CUDR");
					connection.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"CUDL");
						
					u1 = new UpdaterThreadForListener(connection,topicPrefix+((i+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"L",((DistributedMultiSchedule)schedule).fields,listeners);
					u1.start();
						
					u2 = new UpdaterThreadForListener(connection, topicPrefix+((i+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"R",((DistributedMultiSchedule)schedule).fields,listeners);
					u2.start();
						
					u3 = new UpdaterThreadForListener(connection, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"U",((DistributedMultiSchedule)schedule).fields,listeners);
					u3.start();
						
					u4 = new UpdaterThreadForListener(connection, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"D",((DistributedMultiSchedule)schedule).fields,listeners);
					u4.start();
						
					u5 = new UpdaterThreadForListener(connection, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"CDDR",((DistributedMultiSchedule)schedule).fields,listeners);
					u5.start();
						
					u6 = new UpdaterThreadForListener(connection, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"CDDL",((DistributedMultiSchedule)schedule).fields,listeners);
					u6.start();	
						
					u7 = new UpdaterThreadForListener(connection, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"CUDR",((DistributedMultiSchedule)schedule).fields,listeners);
					u7.start();		
						
					u8 = new UpdaterThreadForListener(connection, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"CUDL",((DistributedMultiSchedule)schedule).fields,listeners);
					u8.start();
						
				}catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 }
		 
	}

	public DistributedState() {
		super(null, new DistributedMultiSchedule<E>());

	}

	public DistributedState(int max_d, int rows, int columns, int num_agents, int i,
			int j, String ip, String port, int mode, boolean isToroidal,
			DistributedMultiSchedule<E> sched, String prefix) {
		super(null, sched);
		this.TYPE = new CellType(i, j);
		this.random = new MersenneTwisterFast(this.TYPE.getInitialValue());
		this.MAX_DISTANCE = max_d;
		this.NUMPEERS = rows*columns;
		this.rows = rows;
		this.columns = columns;
		this.NUMAGENTS = num_agents;
		this.count_id = NUMAGENTS * TYPE.getInitialValue();
		this.ip = ip;
		this.port = port;
		this.MODE = mode;
		this.isTOROIDAL = isToroidal;
		
		this.topicPrefix = prefix;
		connection = new ConnectionNFieldsWithActiveMQAPI();
		try {
			connection.setupConnection(new Address(ip, port));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.TRIGGER = new Trigger(connection);

	}
	
	/**
	 * 
	 * @param random
	 *            the MersenneTwisterFast for SimState
	 * @param schedule
	 *            a specific schedule
	 * @param max_d
	 *            maximum shift of an agent
	 * @param num_peers
	 *            the number of peers
	 * @param num_agents
	 *            the number of agents
	 * @param i
	 *            row in the matrix of peers
	 * @param j
	 *            column in the matrix of peers
	 */
	/*public DistributedState(int max_d, int num_peers, int num_agents, int i,
			int j) {
		super(null, new DistributedMultiSchedule<E>());
		this.TYPE = new CellType(i, j);
		this.random = new MersenneTwisterFast(this.TYPE.getInitialValue());
		this.MAX_DISTANCE = max_d;
		this.NUMPEERS = num_peers;
		this.NUMAGENTS = num_agents;
		this.count_id = NUMAGENTS * TYPE.getInitialValue();

	}*/

	/**
	 * 
	 * @param random
	 *            the MersenneTwisterFast for SimState
	 * @param schedule
	 *            a specific schedule
	 * @param max_d
	 *            maximum shift of an agent
	 * @param num_peers
	 *            the number of peers
	 * @param num_agents
	 *            the number of agents
	 * @param i
	 *            row in the matrix of peers
	 * @param j
	 *            column in the matrix of peers
	 */
	/*public DistributedState(Object[] objs) {
		super(null, new DistributedMultiSchedule<E>());
		this.TYPE = new CellType((Integer) objs[5], (Integer) objs[6]);
		this.random = new MersenneTwisterFast(this.TYPE.getInitialValue());
		this.MAX_DISTANCE = (Integer) objs[0];
		this.NUMPEERS = (Integer) objs[1];
		this.NUMAGENTS = (Integer) objs[2];
		this.count_id = NUMAGENTS * TYPE.getInitialValue();

	}*/

	// abstract methods those must be implemented in the subclasses
	public abstract DistributedField<E> getField();

	public abstract void addToField(RemoteAgent<E> rm, E loc);

	public abstract SimState getState();

	public abstract boolean setPortrayalForObject(Object o);

	public CellType getType() {
		return TYPE;
	}

	/**
	 * @return the next available Id
	 */
	public int nextId() {
		return ++count_id;
	}

	/**
	 * this method must be invoked after you set the params ip and port.
	 */
	public ConnectionNFieldsWithActiveMQAPI getConnection() {
		return connection;
	}

	public void setConnection(ConnectionNFieldsWithActiveMQAPI con) {
		this.connection = con;
	}

	public ArrayList<MessageListener> getLocalListener() {
		return listeners;
	}

	public Trigger getTrigger() {
		return TRIGGER;
	}
	
	//added for close connection of current simulation after reset
	public void closeConnection() throws JMSException
	{
		connection.close();
	}

}