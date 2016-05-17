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

package it.isislab.dmason.sim.engine;

import it.isislab.dmason.annotation.AuthorAnnotation;
import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.DistributedFieldNetwork;
import it.isislab.dmason.sim.field.MessageListener;
import it.isislab.dmason.sim.field.network.DNetwork;
import it.isislab.dmason.util.connection.mpi.ConnectionMPI;
import it.isislab.dmason.util.connection.mpi.DNetworkMPIMessageListener;
import it.isislab.dmason.util.connection.mpi.MPInFieldsListeners;
import it.isislab.dmason.util.connection.mpi.openmpi.bcast.ConnectionNFieldsWithBcastMPIBYTE;
import it.isislab.dmason.util.connection.mpi.openmpi.gather.ConnectionNFieldsWithGatherMPIBYTE;
import it.isislab.dmason.util.connection.mpi.openmpi.parallel.ConnectionNFieldsWithParallelSchedulerMPI;
import it.isislab.dmason.util.connection.mpi.openmpi.parallel.ConnectionNFieldsWithThreadsMPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;


import mpi.MPIException;

/**
 * 
 * @param <E>
 *            the type of locations
 *            
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * @author Luca Vicidomini       
 */
public class DistributedStateConnectionMPI<E> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<MessageListener> listeners = new ArrayList<MessageListener>();
	private ArrayList<DNetworkMPIMessageListener> networkListeners = new ArrayList<DNetworkMPIMessageListener>();
	private DistributedState dm;
	private DistributedMultiSchedule<E> schedule;
	private String topicPrefix;
	private CellType TYPE;
	private int MODE;
	private int NUMPEERS;
	private int rows;
	private int columns;
	private HashMap<String, Integer> networkNumberOfSubscribersForField;
	private ConnectionMPI connectionMPI;


	public DistributedStateConnectionMPI(DistributedState dm,int connectionType) {

		this.dm=dm;
		try {

			switch (connectionType) {
			case 1:
				connectionMPI = new ConnectionNFieldsWithBcastMPIBYTE();
				break;
			case -1:
				connectionMPI = new ConnectionNFieldsWithBcastMPIBYTE();
				break;
			case 2:
				connectionMPI = new ConnectionNFieldsWithGatherMPIBYTE();
				break;
			case -2:
				connectionMPI = new ConnectionNFieldsWithGatherMPIBYTE();
				break;
			case 3:
				connectionMPI = new ConnectionNFieldsWithParallelSchedulerMPI();
				break;
			case -3:
				connectionMPI = new ConnectionNFieldsWithParallelSchedulerMPI();
				break;
			case 4:
				connectionMPI =  new ConnectionNFieldsWithThreadsMPI();
				break;
			case -4:
				connectionMPI =  new ConnectionNFieldsWithThreadsMPI();
				break;
			default:
				throw new DMasonException("No Connection Type defined.");
			}

		} catch (MPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DMasonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		schedule=(DistributedMultiSchedule<E>)dm.schedule;
		topicPrefix=dm.topicPrefix;
		TYPE=dm.TYPE;
		MODE=dm.MODE;
		NUMPEERS=dm.NUMPEERS;
		rows=dm.rows;
		columns=dm.columns;
		networkNumberOfSubscribersForField=dm.networkNumberOfSubscribersForField;
	}

	private void init_spatial_connection() {
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
		if (toroidal_need)
			connection_IS_toroidal();
		else
			connection_NO_toroidal();

	}

	public void init_connection() {
		if(((DistributedMultiSchedule<E>)dm.schedule).fields2D.size()>0)
			init_spatial_connection();
		if(((DistributedMultiSchedule<E>)dm.schedule).fieldsNetwork.size()>0)
			init_network_connection();

		init_MPI();
	}
	

	private void init_MPI()
	{
		try {
			connectionMPI.setupConnection(null);
			connectionMPI.setLogging(Level.OFF);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void connection_IS_toroidal() {

		if (MODE == DistributedField2D.HORIZONTAL_BALANCED_DISTRIBUTION_MODE) { // HORIZONTAL_MODE

			try {

				System.out.println("NO BALANCED MODE");
//				connectionMPI.createTopic(topicPrefix+TYPE.pos_i + "-" + TYPE.pos_j + "E",
//						schedule.fields2D
//						.size());
//				connectionMPI.createTopic(topicPrefix+TYPE.pos_i + "-" + TYPE.pos_j + "W",
//						schedule.fields2D
//						.size());
//
//				connectionMPI.subscribeToTopic(topicPrefix+TYPE.pos_i + "-"
//						+ ((TYPE.pos_j - 1 + NUMPEERS) % NUMPEERS) + "W");
//				connectionMPI.subscribeToTopic(topicPrefix+TYPE.pos_i + "-"
//						+ ((TYPE.pos_j + 1 + NUMPEERS) % NUMPEERS) + "E");
//
//				MPInFieldsListeners l1 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);
//
//				connectionMPI.asynchronousReceive(topicPrefix+TYPE.pos_i + "-"
//						+ ((TYPE.pos_j - 1 + NUMPEERS) % NUMPEERS) + "W",l1);
//
//				MPInFieldsListeners l2 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);
//
//				connectionMPI.asynchronousReceive(topicPrefix+TYPE.pos_i + "-"
//						+ ((TYPE.pos_j + 1 + NUMPEERS) % NUMPEERS) + "E",l2);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(MODE == DistributedField2D.UNIFORM_PARTITIONING_MODE){
			int i = TYPE.pos_i, j = TYPE.pos_j;
			try{

				//one columns and N rows
				if(rows > 1 && columns == 1){
					connectionMPI.createTopic(topicPrefix+TYPE + "N",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "S",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "NW",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "NE",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "SW",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "SE",
							schedule.fields2D
							.size());

					connectionMPI.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "N");
					connectionMPI.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "S");

					connectionMPI.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "SE");
					connectionMPI.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "SW");
					connectionMPI.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "NE");
					connectionMPI.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "NW");

					MPInFieldsListeners l3 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "N",l3);

					MPInFieldsListeners l4 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "S",l4);

					MPInFieldsListeners l5 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "SE",l5);

					MPInFieldsListeners l6 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "SW",l6);

					MPInFieldsListeners l7 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "NE",l7);

					MPInFieldsListeners l8 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "NW",l8);
				}
				//one rows and N columns
				else if(rows==1 && columns>1){
					connectionMPI.createTopic(topicPrefix+TYPE + "W",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "E",
							schedule.fields2D
							.size());

					connectionMPI.createTopic(topicPrefix+TYPE + "NW",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "NE",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "SW",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "SE",
							schedule.fields2D
							.size());



					connectionMPI.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "W");
					connectionMPI.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "E");

					connectionMPI.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "SE");
					connectionMPI.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "SW");
					connectionMPI.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "NE");
					connectionMPI.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "NW");

					MPInFieldsListeners l1 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "W",l1);

					MPInFieldsListeners l2 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "E",l2);

					MPInFieldsListeners l5 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "SE",l5);

					MPInFieldsListeners l6 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "SW",l6);

					MPInFieldsListeners l7 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "NE",l7);

					MPInFieldsListeners l8 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "NW",l8);

				}
				else{
					// N rows and N columns
					connectionMPI.createTopic(topicPrefix+TYPE + "W",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "E",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "S",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "N",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "NW",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "NE",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "SW",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "SE",
							schedule.fields2D
							.size());

					int sqrt = (int) Math.sqrt(NUMPEERS);

					connectionMPI.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "W");

					connectionMPI.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "E");

					connectionMPI.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "N");

					connectionMPI.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "S");

					connectionMPI.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "SE");

					connectionMPI.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "SW");

					connectionMPI.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "NE");

					connectionMPI.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "NW");

					MPInFieldsListeners l1 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "W",l1);

					MPInFieldsListeners l2 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "E",l2);

					MPInFieldsListeners l3 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "N",l3);

					MPInFieldsListeners l4 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "S",l4);

					MPInFieldsListeners l5 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "SE",l5);

					MPInFieldsListeners l6 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "SW",l6);

					MPInFieldsListeners l7 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "NE",l7);

					MPInFieldsListeners l8 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "NW",l8);
				}

			}catch(Exception e){
				e.printStackTrace();
			}
		}
		else if (MODE ==  DistributedField2D.SQUARE_BALANCED_DISTRIBUTION_MODE) { // SQUARE BALANCED

			try {

				connectionMPI.createTopic(topicPrefix+TYPE+"E",schedule.fields2D.size());
				connectionMPI.createTopic(topicPrefix+TYPE+"W",schedule.fields2D.size());
				connectionMPI.createTopic(topicPrefix+TYPE+"S",schedule.fields2D.size());
				connectionMPI.createTopic(topicPrefix+TYPE+"N",schedule.fields2D.size());
				connectionMPI.createTopic(topicPrefix+TYPE+"NW",schedule.fields2D.size());
				connectionMPI.createTopic(topicPrefix+TYPE+"NE",schedule.fields2D.size());
				connectionMPI.createTopic(topicPrefix+TYPE+"SW",schedule.fields2D.size());
				connectionMPI.createTopic(topicPrefix+TYPE+"SE",schedule.fields2D.size());

				int i=TYPE.pos_i,j=TYPE.pos_j;
				int sqrt=(int)Math.sqrt(NUMPEERS);

				connectionMPI.subscribeToTopic(topicPrefix+((i+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"E");
				connectionMPI.subscribeToTopic(topicPrefix+((i+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"W");
				connectionMPI.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"N");
				connectionMPI.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"S");
				connectionMPI.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"SE");
				connectionMPI.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"SW");
				connectionMPI.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"NE");
				connectionMPI.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"NW");

				MPInFieldsListeners l1 = new MPInFieldsListeners(schedule.fields2D);

				connectionMPI.asynchronousReceive(topicPrefix+((i+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"E",l1);

				MPInFieldsListeners l2 = new MPInFieldsListeners(schedule.fields2D);

				connectionMPI.asynchronousReceive(topicPrefix+((i+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"W",l2);

				MPInFieldsListeners l3 = new MPInFieldsListeners(schedule.fields2D);

				connectionMPI.asynchronousReceive(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"N",l3);

				MPInFieldsListeners l4 = new MPInFieldsListeners(schedule.fields2D);

				connectionMPI.asynchronousReceive(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"S",l4);

				MPInFieldsListeners l5 = new MPInFieldsListeners(schedule.fields2D);

				connectionMPI.asynchronousReceive(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"SE",l5);

				MPInFieldsListeners l6 = new MPInFieldsListeners(schedule.fields2D);

				connectionMPI.asynchronousReceive(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"SW",l6);

				MPInFieldsListeners l7 = new MPInFieldsListeners(schedule.fields2D);

				connectionMPI.asynchronousReceive(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"NE",l7);

				MPInFieldsListeners l8 = new MPInFieldsListeners(schedule.fields2D);

				connectionMPI.asynchronousReceive(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"NW",l8);

			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void connection_NO_toroidal() {

		if (MODE ==  DistributedField2D.HORIZONTAL_BALANCED_DISTRIBUTION_MODE) { // HORIZONTAL_MODE

			try {

				if(TYPE.pos_j != columns-1){
					connectionMPI.createTopic(topicPrefix+TYPE + "W",
							schedule.fields2D
							.size());

					connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourRight() + "E");

					MPInFieldsListeners l1 = new MPInFieldsListeners(schedule.fields2D);

					connectionMPI.asynchronousReceive(
							topicPrefix+TYPE.getNeighbourRight() + "E",l1);


				}
				if(TYPE.pos_j > 0){
					connectionMPI.createTopic(topicPrefix+TYPE + "E",
							schedule.fields2D
							.size());

					connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourLeft() + "W");

					MPInFieldsListeners l2 = new MPInFieldsListeners(schedule.fields2D);

					connectionMPI.asynchronousReceive(
							topicPrefix+TYPE.getNeighbourLeft() + "W",l2);
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} 
		else if(MODE ==  DistributedField2D.UNIFORM_PARTITIONING_MODE){
			try{
				if(rows>1 && columns==1){
					MPInFieldsListeners l = new MPInFieldsListeners(schedule.fields2D);
					if(TYPE.pos_i==0){
						//crea sotto e sottomettiti a i+1-spra
						connectionMPI.createTopic(topicPrefix+TYPE + "S",
								schedule.fields2D
								.size());
						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourDown() + "N");
						connectionMPI.asynchronousReceive(topicPrefix+(TYPE.getNeighbourDown() + "N"),l);
					}
					else if(TYPE.pos_i == rows-1){
						//crea sopra e sottomettiti a i-1-sotto
						connectionMPI.createTopic(topicPrefix+TYPE + "N",
								schedule.fields2D
								.size());
						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourUp() + "S");
						connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbourUp() + "S",l);
					}
					else{
						connectionMPI.createTopic(topicPrefix+TYPE + "S",
								schedule.fields2D
								.size());
						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourDown() + "N");
						connectionMPI.asynchronousReceive(topicPrefix+(TYPE.getNeighbourDown() + "N"),l);

						connectionMPI.createTopic(topicPrefix+TYPE + "N",
								schedule.fields2D
								.size());
						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourUp() + "S");
						connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbourUp() + "S",l);
					}	
				}
				else if(rows==1 && columns > 1){
					MPInFieldsListeners l = new MPInFieldsListeners(schedule.fields2D);
					if(TYPE.pos_j < columns){
						connectionMPI.createTopic(topicPrefix+TYPE + "W",
								schedule.fields2D
								.size());

						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourRight() + "W");
						connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbourRight() + "W",l);

					}
					if(TYPE.pos_j > 0){
						connectionMPI.createTopic(topicPrefix+TYPE + "W",
								schedule.fields2D
								.size());

						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourLeft() + "E");
						connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbourLeft() + "E",l);
					}
				}
				else{
					//N rows and N columns

					if(TYPE.pos_j > 0)
						connectionMPI.createTopic(topicPrefix+TYPE + "W",
								schedule.fields2D
								.size());
					if(TYPE.pos_j < columns-1)
						connectionMPI.createTopic(topicPrefix+TYPE + "E",
								schedule.fields2D
								.size());
					if(TYPE.pos_i > 0)
						connectionMPI.createTopic(topicPrefix+TYPE + "N",
								schedule.fields2D
								.size());
					if(TYPE.pos_i < rows-1)
						connectionMPI.createTopic(topicPrefix+TYPE + "S",
								schedule.fields2D
								.size());
					if(TYPE.pos_i < rows-1 && TYPE.pos_j < columns-1)
						connectionMPI.createTopic(topicPrefix+TYPE + "SE",
								schedule.fields2D
								.size());
					if(TYPE.pos_i > 0 && TYPE.pos_j < columns-1)
						connectionMPI.createTopic(topicPrefix+TYPE + "NE",
								schedule.fields2D
								.size());
					if(TYPE.pos_i < rows-1 && TYPE.pos_j > 0)
						connectionMPI.createTopic(topicPrefix+TYPE + "SW",
								schedule.fields2D
								.size());
					if(TYPE.pos_i > 0 && TYPE.pos_j > 0)
						connectionMPI.createTopic(topicPrefix+TYPE + "NW",
								schedule.fields2D
								.size());

					MPInFieldsListeners l = new MPInFieldsListeners(schedule.fields2D);

					if(TYPE.pos_i < rows-1 && TYPE.pos_j < columns-1){
						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagRightDown()
						+ "NE");
						connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbourDiagRightDown()
						+ "NE",l);
					}
					if(TYPE.pos_i < rows-1 && TYPE.pos_j > 0){
						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagLeftDown()
						+ "NW");
						connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbourDiagLeftDown()
						+ "NW",l);
					}

					if(TYPE.pos_i > 0 && TYPE.pos_j < columns-1){
						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagRightUp()
						+ "SW");
						connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbourDiagRightUp()
						+ "SW",l);
					}
					if(TYPE.pos_i > 0 && TYPE.pos_j > 0){
						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagLeftUp()
						+ "SE");
						connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbourDiagLeftUp()
						+ "SE",l);
					}
					if(TYPE.pos_j < columns-1){
						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourRight() + "E");
						connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbourRight() + "E",l);
					}
					if(TYPE.pos_j > 0){
						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourLeft() + "W");
						connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbourLeft() + "W",l);
					}
					if(TYPE.pos_i < rows-1){	
						connectionMPI.subscribeToTopic(topicPrefix+(TYPE.getNeighbourDown() + "N"));
						connectionMPI.asynchronousReceive(topicPrefix+(TYPE.getNeighbourDown() + "N"),l);
					}
					if(TYPE.pos_i > 0){
						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourUp() + "S");
						connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbourUp() + "S",l);
					}
				}
			}catch (Exception e) {
				// TODO Auto-generated catch block
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
	private void init_network_connection()
	{
		DistributedMultiSchedule dms = schedule;
		ArrayList<DNetwork> networkLists = dms.fieldsNetwork;


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

				String toSubscribe=topicPrefix+"-Network-"+integer+"-"+my_community;

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
				//				System.out.println(" "+TYPE+" C["+topicName+"]");
				connectionMPI.createTopic(topicName, publishers.size());
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
				//				System.out.println(" "+TYPE+" S["+topicName+"]");
				connectionMPI.subscribeToTopic(topicName);

				DNetworkMPIMessageListener m = new DNetworkMPIMessageListener(schedule.fieldsNetwork, topicName);
				if(connectionMPI.asynchronousReceive(topicName,m)==false)
					throw new Exception("Error in saving listener for topic "+topicName);

			} catch (Exception e) {
				e.printStackTrace();
			}


		}
	}

	public CellType getType() {
		return TYPE;
	}

	public ConnectionMPI getConnection() {
		return connectionMPI;
	}

	public ArrayList<MessageListener> getLocalListener() {
		return listeners;
	}
}