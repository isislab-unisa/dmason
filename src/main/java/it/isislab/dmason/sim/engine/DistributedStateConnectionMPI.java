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

				connectionMPI.createTopic(topicPrefix+TYPE.pos_i + "-" + TYPE.pos_j + "L",
						schedule.fields2D
						.size());
				connectionMPI.createTopic(topicPrefix+TYPE.pos_i + "-" + TYPE.pos_j + "R",
						schedule.fields2D
						.size());

				connectionMPI.subscribeToTopic(topicPrefix+TYPE.pos_i + "-"
						+ ((TYPE.pos_j - 1 + NUMPEERS) % NUMPEERS) + "R");
				connectionMPI.subscribeToTopic(topicPrefix+TYPE.pos_i + "-"
						+ ((TYPE.pos_j + 1 + NUMPEERS) % NUMPEERS) + "L");

				MPInFieldsListeners l1 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

				connectionMPI.asynchronousReceive(topicPrefix+TYPE.pos_i + "-"
						+ ((TYPE.pos_j - 1 + NUMPEERS) % NUMPEERS) + "R",l1);

				MPInFieldsListeners l2 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

				connectionMPI.asynchronousReceive(topicPrefix+TYPE.pos_i + "-"
						+ ((TYPE.pos_j + 1 + NUMPEERS) % NUMPEERS) + "L",l2);

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
					connectionMPI.createTopic(topicPrefix+TYPE + "U",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "D",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "CUDL",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "CUDR",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "CDDL",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "CDDR",
							schedule.fields2D
							.size());

					connectionMPI.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "U");
					connectionMPI.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "D");

					connectionMPI.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "CDDR");
					connectionMPI.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "CDDL");
					connectionMPI.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "CUDR");
					connectionMPI.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "CUDL");

					MPInFieldsListeners l3 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "U",l3);

					MPInFieldsListeners l4 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "D",l4);

					MPInFieldsListeners l5 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "CDDR",l5);

					MPInFieldsListeners l6 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "CDDL",l6);

					MPInFieldsListeners l7 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "CUDR",l7);

					MPInFieldsListeners l8 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "CUDL",l8);
				}
				//one rows and N columns
				else if(rows==1 && columns>1){
					connectionMPI.createTopic(topicPrefix+TYPE + "L",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "R",
							schedule.fields2D
							.size());

					connectionMPI.createTopic(topicPrefix+TYPE + "CUDL",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "CUDR",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "CDDL",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "CDDR",
							schedule.fields2D
							.size());



					connectionMPI.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "L");
					connectionMPI.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "R");

					connectionMPI.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "CDDR");
					connectionMPI.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "CDDL");
					connectionMPI.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "CUDR");
					connectionMPI.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "CUDL");

					MPInFieldsListeners l1 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "L",l1);

					MPInFieldsListeners l2 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "R",l2);

					MPInFieldsListeners l5 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "CDDR",l5);

					MPInFieldsListeners l6 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "CDDL",l6);

					MPInFieldsListeners l7 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "CUDR",l7);

					MPInFieldsListeners l8 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "CUDL",l8);

				}
				else{
					// N rows and N columns
					connectionMPI.createTopic(topicPrefix+TYPE + "L",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "R",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "D",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "U",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "CUDL",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "CUDR",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "CDDL",
							schedule.fields2D
							.size());
					connectionMPI.createTopic(topicPrefix+TYPE + "CDDR",
							schedule.fields2D
							.size());

					int sqrt = (int) Math.sqrt(NUMPEERS);

					connectionMPI.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "L");

					connectionMPI.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "R");

					connectionMPI.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "U");

					connectionMPI.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "D");

					connectionMPI.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "CDDR");

					connectionMPI.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "CDDL");

					connectionMPI.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "CUDR");

					connectionMPI.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "CUDL");

					MPInFieldsListeners l1 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "L",l1);

					MPInFieldsListeners l2 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "R",l2);

					MPInFieldsListeners l3 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "U",l3);

					MPInFieldsListeners l4 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "D",l4);

					MPInFieldsListeners l5 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "CDDR",l5);

					MPInFieldsListeners l6 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "CDDL",l6);

					MPInFieldsListeners l7 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "CUDR",l7);

					MPInFieldsListeners l8 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields2D);

					connectionMPI.asynchronousReceive(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "CUDL",l8);
				}

			}catch(Exception e){
				e.printStackTrace();
			}
		}
		else if (MODE ==  DistributedField2D.SQUARE_BALANCED_DISTRIBUTION_MODE) { // SQUARE BALANCED

			try {

				connectionMPI.createTopic(topicPrefix+TYPE+"L",schedule.fields2D.size());
				connectionMPI.createTopic(topicPrefix+TYPE+"R",schedule.fields2D.size());
				connectionMPI.createTopic(topicPrefix+TYPE+"D",schedule.fields2D.size());
				connectionMPI.createTopic(topicPrefix+TYPE+"U",schedule.fields2D.size());
				connectionMPI.createTopic(topicPrefix+TYPE+"CUDL",schedule.fields2D.size());
				connectionMPI.createTopic(topicPrefix+TYPE+"CUDR",schedule.fields2D.size());
				connectionMPI.createTopic(topicPrefix+TYPE+"CDDL",schedule.fields2D.size());
				connectionMPI.createTopic(topicPrefix+TYPE+"CDDR",schedule.fields2D.size());

				int i=TYPE.pos_i,j=TYPE.pos_j;
				int sqrt=(int)Math.sqrt(NUMPEERS);

				connectionMPI.subscribeToTopic(topicPrefix+((i+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"L");
				connectionMPI.subscribeToTopic(topicPrefix+((i+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"R");
				connectionMPI.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"U");
				connectionMPI.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"D");
				connectionMPI.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"CDDR");
				connectionMPI.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"CDDL");
				connectionMPI.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"CUDR");
				connectionMPI.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"CUDL");

				MPInFieldsListeners l1 = new MPInFieldsListeners(schedule.fields2D);

				connectionMPI.asynchronousReceive(topicPrefix+((i+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"L",l1);

				MPInFieldsListeners l2 = new MPInFieldsListeners(schedule.fields2D);

				connectionMPI.asynchronousReceive(topicPrefix+((i+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"R",l2);

				MPInFieldsListeners l3 = new MPInFieldsListeners(schedule.fields2D);

				connectionMPI.asynchronousReceive(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"U",l3);

				MPInFieldsListeners l4 = new MPInFieldsListeners(schedule.fields2D);

				connectionMPI.asynchronousReceive(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"D",l4);

				MPInFieldsListeners l5 = new MPInFieldsListeners(schedule.fields2D);

				connectionMPI.asynchronousReceive(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"CDDR",l5);

				MPInFieldsListeners l6 = new MPInFieldsListeners(schedule.fields2D);

				connectionMPI.asynchronousReceive(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"CDDL",l6);

				MPInFieldsListeners l7 = new MPInFieldsListeners(schedule.fields2D);

				connectionMPI.asynchronousReceive(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"CUDR",l7);

				MPInFieldsListeners l8 = new MPInFieldsListeners(schedule.fields2D);

				connectionMPI.asynchronousReceive(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"CUDL",l8);

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
					connectionMPI.createTopic(topicPrefix+TYPE + "R",
							schedule.fields2D
							.size());

					connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourRight() + "L");

					MPInFieldsListeners l1 = new MPInFieldsListeners(schedule.fields2D);

					connectionMPI.asynchronousReceive(
							topicPrefix+TYPE.getNeighbourRight() + "L",l1);


				}
				if(TYPE.pos_j > 0){
					connectionMPI.createTopic(topicPrefix+TYPE + "L",
							schedule.fields2D
							.size());

					connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourLeft() + "R");

					MPInFieldsListeners l2 = new MPInFieldsListeners(schedule.fields2D);

					connectionMPI.asynchronousReceive(
							topicPrefix+TYPE.getNeighbourLeft() + "R",l2);
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
						connectionMPI.createTopic(topicPrefix+TYPE + "D",
								schedule.fields2D
								.size());
						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourDown() + "U");
						connectionMPI.asynchronousReceive(topicPrefix+(TYPE.getNeighbourDown() + "U"),l);
					}
					else if(TYPE.pos_i == rows-1){
						//crea sopra e sottomettiti a i-1-sotto
						connectionMPI.createTopic(topicPrefix+TYPE + "U",
								schedule.fields2D
								.size());
						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourUp() + "D");
						connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbourUp() + "D",l);
					}
					else{
						connectionMPI.createTopic(topicPrefix+TYPE + "D",
								schedule.fields2D
								.size());
						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourDown() + "U");
						connectionMPI.asynchronousReceive(topicPrefix+(TYPE.getNeighbourDown() + "U"),l);

						connectionMPI.createTopic(topicPrefix+TYPE + "U",
								schedule.fields2D
								.size());
						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourUp() + "D");
						connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbourUp() + "D",l);
					}	
				}
				else if(rows==1 && columns > 1){
					MPInFieldsListeners l = new MPInFieldsListeners(schedule.fields2D);
					if(TYPE.pos_j < columns){
						connectionMPI.createTopic(topicPrefix+TYPE + "R",
								schedule.fields2D
								.size());

						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourRight() + "L");
						connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbourRight() + "L",l);

					}
					if(TYPE.pos_j > 0){
						connectionMPI.createTopic(topicPrefix+TYPE + "L",
								schedule.fields2D
								.size());

						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourLeft() + "R");
						connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbourLeft() + "R",l);
					}
				}
				else{
					//N rows and N columns

					if(TYPE.pos_j > 0)
						connectionMPI.createTopic(topicPrefix+TYPE + "L",
								schedule.fields2D
								.size());
					if(TYPE.pos_j < columns-1)
						connectionMPI.createTopic(topicPrefix+TYPE + "R",
								schedule.fields2D
								.size());
					if(TYPE.pos_i > 0)
						connectionMPI.createTopic(topicPrefix+TYPE + "U",
								schedule.fields2D
								.size());
					if(TYPE.pos_i < rows-1)
						connectionMPI.createTopic(topicPrefix+TYPE + "D",
								schedule.fields2D
								.size());
					if(TYPE.pos_i < rows-1 && TYPE.pos_j < columns-1)
						connectionMPI.createTopic(topicPrefix+TYPE + "CDDR",
								schedule.fields2D
								.size());
					if(TYPE.pos_i > 0 && TYPE.pos_j < columns-1)
						connectionMPI.createTopic(topicPrefix+TYPE + "CUDR",
								schedule.fields2D
								.size());
					if(TYPE.pos_i < rows-1 && TYPE.pos_j > 0)
						connectionMPI.createTopic(topicPrefix+TYPE + "CDDL",
								schedule.fields2D
								.size());
					if(TYPE.pos_i > 0 && TYPE.pos_j > 0)
						connectionMPI.createTopic(topicPrefix+TYPE + "CUDL",
								schedule.fields2D
								.size());

					MPInFieldsListeners l = new MPInFieldsListeners(schedule.fields2D);

					if(TYPE.pos_i < rows-1 && TYPE.pos_j < columns-1){
						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagRightDown()
						+ "CUDL");
						connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbourDiagRightDown()
						+ "CUDL",l);
					}
					if(TYPE.pos_i < rows-1 && TYPE.pos_j > 0){
						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagLeftDown()
						+ "CUDR");
						connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbourDiagLeftDown()
						+ "CUDR",l);
					}

					if(TYPE.pos_i > 0 && TYPE.pos_j < columns-1){
						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagRightUp()
						+ "CDDL");
						connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbourDiagRightUp()
						+ "CDDL",l);
					}
					if(TYPE.pos_i > 0 && TYPE.pos_j > 0){
						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagLeftUp()
						+ "CDDR");
						connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbourDiagLeftUp()
						+ "CDDR",l);
					}
					if(TYPE.pos_j < columns-1){
						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourRight() + "L");
						connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbourRight() + "L",l);
					}
					if(TYPE.pos_j > 0){
						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourLeft() + "R");
						connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbourLeft() + "R",l);
					}
					if(TYPE.pos_i < rows-1){	
						connectionMPI.subscribeToTopic(topicPrefix+(TYPE.getNeighbourDown() + "U"));
						connectionMPI.asynchronousReceive(topicPrefix+(TYPE.getNeighbourDown() + "U"),l);
					}
					if(TYPE.pos_i > 0){
						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourUp() + "D");
						connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbourUp() + "D",l);
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