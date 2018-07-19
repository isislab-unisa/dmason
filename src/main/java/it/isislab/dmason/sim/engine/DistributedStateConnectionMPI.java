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
import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.DistributedFieldNetwork;
import it.isislab.dmason.sim.field.MessageListener;
import it.isislab.dmason.sim.field.network.DNetwork;
import it.isislab.dmason.sim.field3D.DistributedField3D;
import it.isislab.dmason.sim.field3D.MessageListener3D;
import it.isislab.dmason.sim.field3D.UpdaterThreadForListener3D;
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

import org.apache.http.cookie.SM;

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
	private ArrayList<MessageListener> listeners = new ArrayList<MessageListener>(); //non viene mai usato...utile?
	private ArrayList<MessageListener3D> listeners3D = new ArrayList<MessageListener3D>();
	private ArrayList<DNetworkMPIMessageListener> networkListeners = new ArrayList<DNetworkMPIMessageListener>();
	private DistributedState dm;
	private DistributedMultiSchedule<E> schedule;
	private String topicPrefix;
	private CellType TYPE;
	private int MODE;
	private int NUMPEERS;
	private int rows;
	private int columns;
	private int lenghts;
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
		lenghts=dm.lenghts;
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

	private void init_3Dspatial_connection(){
		boolean toroidal_need=false;
		for(DistributedField3D field:
				((DistributedMultiSchedule<E>)dm.schedule).get3DFields()){
			if(field.isToroidal()){
				toroidal_need=true;
				break;
			}
		}
		if(toroidal_need)
			connection3D_IS_toroidal();
		else
			connection3D_NO_toroidal();
	}

	protected void connection3D_NO_toroidal(){
		if (MODE == DistributedField3D.UNIFORM_PARTITIONING_MODE) {
			try {
				if(lenghts==1){
					if(rows>1 && columns==1){
						if (TYPE.pos_i==0) {
							connectionMPI.createTopic(topicPrefix+TYPE+"S",
									((DistributedMultiSchedule)schedule).fields3D.size());
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDown()+"N");

							MPInFieldsListeners l1 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DDown()+"N",l1);


						} else if(TYPE.pos_i==rows-1){
							connectionMPI.createTopic(topicPrefix+TYPE+"N",
									((DistributedMultiSchedule)schedule).fields3D.size());
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DUp()+"S");

							MPInFieldsListeners l1 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DUp()+"S",l1);

						}else{
							connectionMPI.createTopic(topicPrefix+TYPE+"S",
									((DistributedMultiSchedule)schedule).fields3D.size());
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDown()+"N");
							MPInFieldsListeners l1 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DDown()+"N",l1);

							connectionMPI.createTopic(topicPrefix+TYPE+"N",
									((DistributedMultiSchedule)schedule).fields3D.size());
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DUp()+"S");
							MPInFieldsListeners l2 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DUp()+"S",l2);

						}
					}else if(rows==1 && columns>1){
						if(TYPE.pos_j==0){
							connectionMPI.createTopic(topicPrefix+TYPE+"E",
									((DistributedMultiSchedule) schedule).fields3D.size());
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRight()+"W");

							MPInFieldsListeners l1 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DRight()+"W", l1);
						}else if(TYPE.pos_j==columns-1){
							connectionMPI.createTopic(topicPrefix+TYPE+"W",
									((DistributedMultiSchedule)schedule).fields3D.size());
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DLeft()+"E");

							MPInFieldsListeners l1 = new MPInFieldsListeners(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DLeft()+"E", l1);
						}else{
							connectionMPI.createTopic(topicPrefix+TYPE+"E",
									((DistributedMultiSchedule) schedule).fields3D.size());
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRight()+"W");

							MPInFieldsListeners l1 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DRight()+"W", l1);

							connectionMPI.createTopic(topicPrefix+TYPE+"W",
									((DistributedMultiSchedule)schedule).fields3D.size());
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DLeft()+"E");

							MPInFieldsListeners l2 = new MPInFieldsListeners(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DLeft()+"E", l2);
						}
					}
					else {
						if(TYPE.pos_i>0){
							connectionMPI.createTopic(topicPrefix+TYPE+"N",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows){
							connectionMPI.createTopic(topicPrefix+TYPE+"S",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if (TYPE.pos_j>0){
							connectionMPI.createTopic(topicPrefix+TYPE+"W",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_j<columns){
							connectionMPI.createTopic(topicPrefix+TYPE+"E",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows && TYPE.pos_j<columns){
							connectionMPI.createTopic(topicPrefix+TYPE+"SE",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}

						if(TYPE.pos_i<rows && TYPE.pos_j>0){
							connectionMPI.createTopic(topicPrefix+TYPE+"SW",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i>0 && TYPE.pos_j>0){
							connectionMPI.createTopic(topicPrefix+TYPE+"NW",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i>0 && TYPE.pos_j<columns){
							connectionMPI.createTopic(topicPrefix+TYPE+"NE",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}

						if(TYPE.pos_i>0 && TYPE.pos_j>0){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagLeftUp()+"SE");
							MPInFieldsListeners l1 = new MPInFieldsListeners(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DDiagLeftUp()+"SE", l1);
						}

						if (TYPE.pos_i>0 && TYPE.pos_j<columns){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagRightUp()+"SW");
							MPInFieldsListeners l2 = new MPInFieldsListeners(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DDiagRightUp()+"SW", l2);
						}

						if(TYPE.pos_i<rows && TYPE.pos_j>0){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagLeftDown()+"NE");
							MPInFieldsListeners l3 = new MPInFieldsListeners(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DDiagLeftDown()+"NE", l3);
						}

						if(TYPE.pos_i<rows && TYPE.pos_j<columns){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagRightDown()+"NW");
							MPInFieldsListeners l4 = new MPInFieldsListeners(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DDiagRightDown()+"NW", l4);
						}

						if(TYPE.pos_i>0){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DUp()+"S");
							MPInFieldsListeners l5 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DUp()+"S", l5);
						}

						if(TYPE.pos_i<rows){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDown()+"N");
							MPInFieldsListeners l6 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DDown()+"N", l6);
						}

						if(TYPE.pos_j>0){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DLeft()+"E");
							MPInFieldsListeners l7 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DLeft()+"E", l7);
						}

						if(TYPE.pos_j<columns){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRight()+"W");
							MPInFieldsListeners l8 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DRight()+"W", l8);
						}

					}

				} else {// lenghts >1
					if(rows==1 && columns==1){
						if(TYPE.pos_z==0){
							connectionMPI.createTopic(topicPrefix+TYPE+"R",
									((DistributedMultiSchedule)dm.schedule).fields3D.size());
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRear()+"F");
							MPInFieldsListeners l1 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DRear()+"F", l1);

						}else if(TYPE.pos_z==lenghts-1){
							connectionMPI.createTopic(topicPrefix+TYPE+"F",
									((DistributedMultiSchedule)dm.schedule).fields3D.size());
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DFront()+"R");
							MPInFieldsListeners l1 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DFront()+"R", l1);
						}else{
							connectionMPI.createTopic(topicPrefix+TYPE+"R",
									((DistributedMultiSchedule)dm.schedule).fields3D.size());
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRear()+"F");
							MPInFieldsListeners l1 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DRear()+"F", l1);

							connectionMPI.createTopic(topicPrefix+TYPE+"F",
									((DistributedMultiSchedule)dm.schedule).fields3D.size());
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DFront()+"R");
							MPInFieldsListeners l2 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DFront()+"R", l2);
						}
					}else if (rows>1 && columns==1){

						if(TYPE.pos_i>0){
							connectionMPI.createTopic(topicPrefix+TYPE+"N",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows){
							connectionMPI.createTopic(topicPrefix+TYPE+"S",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_z>0){
							connectionMPI.createTopic(topicPrefix+TYPE+"F",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_z<lenghts){
							connectionMPI.createTopic(topicPrefix+TYPE+"R",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i>0 && TYPE.pos_z>0){
							connectionMPI.createTopic(topicPrefix+TYPE+"NF", (
									(DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i>0 && TYPE.pos_z<lenghts){
							connectionMPI.createTopic(topicPrefix+TYPE+"NR",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows && TYPE.pos_z>0){
							connectionMPI.createTopic(topicPrefix+TYPE+"SF",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows && TYPE.pos_z<lenghts){
							connectionMPI.createTopic(topicPrefix+TYPE+"SR",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}

						if(TYPE.pos_i>0 && TYPE.pos_z>0){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DUpFront()+"SR");
							MPInFieldsListeners l1 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DUpFront()+"SR", l1);
						}
						if(TYPE.pos_i>0 && TYPE.pos_z<lenghts){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DUpRear()+"SF");
							MPInFieldsListeners l2 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DUpRear()+"SF", l2);

						}
						if(TYPE.pos_i<rows && TYPE.pos_z>0){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDownFront()+"NR");
							MPInFieldsListeners l3 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DDownFront()+"NR", l3);
						}
						if(TYPE.pos_i<rows && TYPE.pos_z<lenghts){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDownRear()+"NF");
							MPInFieldsListeners l4 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DDownRear()+"NF", l4);

						}
						if(TYPE.pos_i>0){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DUp()+"S");
							MPInFieldsListeners l5 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DUp()+"S");

						}
						if(TYPE.pos_i<rows){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDown()+"N");
							MPInFieldsListeners l6 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DDown()+"N", l6);

						}
						if(TYPE.pos_z>0){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DFront()+"R");
							MPInFieldsListeners l7 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DFront()+"R", l7);

						}
						if(TYPE.pos_z<lenghts){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRear()+"F");
							MPInFieldsListeners l8 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DRear()+"F", l8);

						}
					} else if (rows==1 && columns>1){
						if(TYPE.pos_j>0){
							connectionMPI.createTopic(topicPrefix+TYPE+"W",
									((DistributedMultiSchedule) schedule).fields3D.size());
						}
						if(TYPE.pos_j<columns){
							connectionMPI.createTopic(topicPrefix+TYPE+"E",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_z>0){
							connectionMPI.createTopic(topicPrefix+TYPE+"F",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_z<lenghts){
							connectionMPI.createTopic(topicPrefix+TYPE+"R",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_j>0 && TYPE.pos_z>0){
							connectionMPI.createTopic(topicPrefix+TYPE+"WF",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_j>0 && TYPE.pos_z<lenghts){
							connectionMPI.createTopic(topicPrefix+TYPE+"WR",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_j<columns && TYPE.pos_z>0 ){
							connectionMPI.createTopic(topicPrefix+TYPE+"EF",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_j<columns && TYPE.pos_z<lenghts){
							connectionMPI.createTopic(topicPrefix+TYPE+"ER",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}

						if(TYPE.pos_j>0 && TYPE.pos_z>0){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DLeftFront()+"ER");
							MPInFieldsListeners l1 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DLeftFront()+"ER", l1);

						}
						if(TYPE.pos_j>0 && TYPE.pos_z<lenghts){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DLeftRear()+"EF");
							MPInFieldsListeners l2 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DLeftRear()+"EF", l2);

						}
						if(TYPE.pos_j<columns && TYPE.pos_z>0 ){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRightFront()+"WR");
							MPInFieldsListeners l3 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DRightFront()+"WR", l3);

						}
						if(TYPE.pos_j<columns && TYPE.pos_z<lenghts){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRightRear()+"WF");
							MPInFieldsListeners l4 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DRightRear()+"WF", l4);

						}
						if(TYPE.pos_j>0){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DLeft()+"E");
							MPInFieldsListeners l5 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DLeft()+"E", l5);

						}
						if(TYPE.pos_j<columns){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRight()+"W");
							MPInFieldsListeners l6 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DRight()+"W", l6);

						}
						if(TYPE.pos_z>0){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DFront()+"R");
							MPInFieldsListeners l7 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DFront()+"R", l7);

						}
						if(TYPE.pos_z<lenghts){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRear()+"F");
							MPInFieldsListeners l8 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DRear()+"F", l8);

						}
					} else{

						if(TYPE.pos_i>0){
							connectionMPI.createTopic(topicPrefix+TYPE+"N",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows){
							connectionMPI.createTopic(topicPrefix+TYPE+"S",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_j>0){
							connectionMPI.createTopic(topicPrefix+TYPE+"W",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_j<columns){
							connectionMPI.createTopic(topicPrefix+TYPE+"E",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_z>0){
							connectionMPI.createTopic(topicPrefix+TYPE+"F",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_z<lenghts){
							connectionMPI.createTopic(topicPrefix+TYPE+"R",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i>0 && TYPE.pos_j>0){
							connectionMPI.createTopic(topicPrefix+TYPE+"NW",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i>0 && TYPE.pos_j<columns){
							connectionMPI.createTopic(topicPrefix+TYPE+"NE",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows && TYPE.pos_j>0){
							connectionMPI.createTopic(topicPrefix+TYPE+"SW",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows && TYPE.pos_j<columns){
							connectionMPI.createTopic(topicPrefix+TYPE+"SE",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i>0 && TYPE.pos_z>0){
							connectionMPI.createTopic(topicPrefix+TYPE+"NF",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i>0 && TYPE.pos_z<lenghts){
							connectionMPI.createTopic(topicPrefix+TYPE+"NR",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows && TYPE.pos_z>0){
							connectionMPI.createTopic(topicPrefix+TYPE+"SF",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows && TYPE.pos_z<lenghts){
							connectionMPI.createTopic(topicPrefix+TYPE+"SR",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_j>0 && TYPE.pos_z>0){
							connectionMPI.createTopic(topicPrefix+TYPE+"WF",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_j>0 && TYPE.pos_z<lenghts){
							connectionMPI.createTopic(topicPrefix+TYPE+"WR",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_j<columns && TYPE.pos_z>0){
							connectionMPI.createTopic(topicPrefix+TYPE+"EF",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_j<columns && TYPE.pos_z<lenghts){
							connectionMPI.createTopic(topicPrefix+TYPE+"ER",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i>0 && TYPE.pos_j>0 && TYPE.pos_z>0){
							connectionMPI.createTopic(topicPrefix+TYPE+"NWF",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i>0 && TYPE.pos_j>0 && TYPE.pos_z<lenghts){
							connectionMPI.createTopic(topicPrefix+TYPE+"NWR",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i>0 && TYPE.pos_j<columns && TYPE.pos_z>0){
							connectionMPI.createTopic(topicPrefix+TYPE+"NEF",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i>0 && TYPE.pos_j<columns && TYPE.pos_z<lenghts){
							connectionMPI.createTopic(topicPrefix+TYPE+"NER",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows && TYPE.pos_j>0 && TYPE.pos_z>0){
							connectionMPI.createTopic(topicPrefix+TYPE+"SWF",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows && TYPE.pos_j>0 && TYPE.pos_z<lenghts){
							connectionMPI.createTopic(topicPrefix+TYPE+"SWR",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows && TYPE.pos_j<columns && TYPE.pos_z>0){
							connectionMPI.createTopic(topicPrefix+TYPE+"SEF",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}
						if(TYPE.pos_i<rows && TYPE.pos_j<columns && TYPE.pos_z<lenghts){
							connectionMPI.createTopic(topicPrefix+TYPE+"SER",
									((DistributedMultiSchedule)schedule).fields3D.size());
						}

						if(TYPE.pos_i>0 && TYPE.pos_j>0 && TYPE.pos_z>0){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagLeftUpFront()+"SER");
							MPInFieldsListeners l1 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DDiagLeftUpFront()+"SER", l1);

						}
						if(TYPE.pos_i>0 && TYPE.pos_j>0 && TYPE.pos_z<lenghts){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagLeftUpRear()+"SEF");
							MPInFieldsListeners l2 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DDiagLeftUpRear()+"SEF", l2);

						}
						if(TYPE.pos_i>0 && TYPE.pos_j<columns && TYPE.pos_z>0){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagRightUpFront()+"SWR");
							MPInFieldsListeners l3 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DDiagRightUpFront()+"SWR", l3);

						}
						if(TYPE.pos_i>0 && TYPE.pos_j<columns && TYPE.pos_z<lenghts){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagRightUpRear()+"SWF");
							MPInFieldsListeners l4 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DDiagRightUpRear()+"SWF", l4);

						}
						if(TYPE.pos_i<rows && TYPE.pos_j>0 && TYPE.pos_z>0){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagLeftDownFront()+"NER");
							MPInFieldsListeners l5 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DDiagLeftDownFront()+"NER", l5);

						}
						if(TYPE.pos_i<rows && TYPE.pos_j>0 && TYPE.pos_z<lenghts){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagLeftDownRear()+"NEF");
							MPInFieldsListeners l6 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DDiagLeftDownRear()+"NEF", l6);

						}
						if(TYPE.pos_i<rows && TYPE.pos_j<columns && TYPE.pos_z>0){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagRightDownFront()+"NWR");
							MPInFieldsListeners l7 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DDiagRightDownFront()+"NWR", l7);

						}
						if(TYPE.pos_i<rows && TYPE.pos_j<columns && TYPE.pos_z<lenghts){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagRightDownRear()+"NWF");
							MPInFieldsListeners l8 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DDiagRightDownRear()+"NWF", l8);

						}
						if(TYPE.pos_i>0 && TYPE.pos_j>0){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagLeftUp()+"SE");
							MPInFieldsListeners l9 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DDiagLeftUp()+"SE", l9);

						}
						if(TYPE.pos_i>0 && TYPE.pos_j<columns){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagRightUp()+"SW");
							MPInFieldsListeners l10 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DDiagRightUp()+"SW",l10);

						}
						if(TYPE.pos_i<rows && TYPE.pos_j>0){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagLeftDown()+"NE");
							MPInFieldsListeners l11 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DDiagLeftDown()+"NE", l11);

						}
						if(TYPE.pos_i<rows && TYPE.pos_j<columns){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDiagRightDown()+"NW");
							MPInFieldsListeners l12 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DDiagRightDown()+"NW", l12);

						}
						if(TYPE.pos_i>0 && TYPE.pos_z>0){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DUpFront()+"SR");
							MPInFieldsListeners l13 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DUpFront()+"SR", l13);
						}
						if(TYPE.pos_i>0 && TYPE.pos_z<lenghts){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DUpRear()+"SF");
							MPInFieldsListeners l14 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DUpRear()+"SF", l14);

						}
						if(TYPE.pos_i<rows && TYPE.pos_z>0){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDownFront()+"NR");
							MPInFieldsListeners l15 = new MPInFieldsListeners(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DDownFront()+"NR", l15);

						}
						if(TYPE.pos_i<rows && TYPE.pos_z<lenghts){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDownRear()+"NF");
							MPInFieldsListeners l16 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DDownRear()+"NF", l16);

						}
						if(TYPE.pos_j>0 && TYPE.pos_z>0){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DLeftFront()+"ER");
							MPInFieldsListeners l17 = new MPInFieldsListeners<>(((DistributedMultiSchedule) schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DLeftFront()+"ER", l17);

						}
						if(TYPE.pos_j>0 && TYPE.pos_z<lenghts){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DLeftRear()+"EF");
							MPInFieldsListeners l18 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DLeftRear()+"EF", l18);

						}
						if(TYPE.pos_j<columns && TYPE.pos_z>0){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRightFront()+"WR");
							MPInFieldsListeners l19 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DRightFront()+"WR", l19);

						}
						if(TYPE.pos_j<columns && TYPE.pos_z<lenghts){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRightRear()+"WF");
							MPInFieldsListeners l20 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DRightRear()+"WF", l20);

						}
						if(TYPE.pos_i>0){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DUp()+"S");
							MPInFieldsListeners l21 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DUp()+"S", l21);

						}
						if(TYPE.pos_i<rows){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DDown()+"N");
							MPInFieldsListeners l22 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DDown()+"N", l22);

						}
						if(TYPE.pos_j>0){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DLeft()+"E");
							MPInFieldsListeners l23 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DLeft()+"E", l23);

						}
						if(TYPE.pos_j<columns){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRight()+"W");
							MPInFieldsListeners l24 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DRight()+"W", l24);

						}
						if(TYPE.pos_z>0){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DFront()+"R");
							MPInFieldsListeners l25 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DFront()+"R",l25);

						}
						if(TYPE.pos_z<lenghts){
							connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbour3DRear()+"F");
							MPInFieldsListeners l26 = new MPInFieldsListeners<>(((DistributedMultiSchedule)schedule).fields3D);
							connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbour3DRear()+"F", l26);

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
						connectionMPI.createTopic(topicPrefix+TYPE+"N",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"S",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
//						connectionMPI.createTopic(topicPrefix+TYPE+"W",
//								((DistributedMultiSchedule)dm.schedule).fields3D.size());
//						connectionMPI.createTopic(topicPrefix+TYPE+"E",
//								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"WF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"WR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"EF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"ER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());



						String topicN=topicPrefix+(((i-1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+lenghts)%lenghts))+"N";
						String topicS=topicPrefix+(((i+1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+lenghts)%lenghts))+"S";
//						String topicW=topicPrefix+(((i+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"W";
//						String topicE=topicPrefix+(((i+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"E";

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


						connectionMPI.subscribeToTopic(topicN);
						connectionMPI.subscribeToTopic(topicS);
//						connectionMPI.subscribeToTopic(topicW);
//						connectionMPI.subscribeToTopic(topicE);
						connectionMPI.subscribeToTopic(topicNF);
						connectionMPI.subscribeToTopic(topicNW);
						connectionMPI.subscribeToTopic(topicNR);
						connectionMPI.subscribeToTopic(topicNE);
						connectionMPI.subscribeToTopic(topicSF);
						connectionMPI.subscribeToTopic(topicSW);
						connectionMPI.subscribeToTopic(topicSR);
						connectionMPI.subscribeToTopic(topicSE);
						connectionMPI.subscribeToTopic(topicWF);
						connectionMPI.subscribeToTopic(topicWR);
						connectionMPI.subscribeToTopic(topicER);
						connectionMPI.subscribeToTopic(topicEF);
						connectionMPI.subscribeToTopic(topicNWF);
						connectionMPI.subscribeToTopic(topicNWR);
						connectionMPI.subscribeToTopic(topicNEF);
						connectionMPI.subscribeToTopic(topicNER);
						connectionMPI.subscribeToTopic(topicSWF);
						connectionMPI.subscribeToTopic(topicSWR);
						connectionMPI.subscribeToTopic(topicSEF);
						connectionMPI.subscribeToTopic(topicSER);

						MPInFieldsListeners l1 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicN,l1);

						MPInFieldsListeners l2 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicS,l2);

//						MPInFieldsListeners l3 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);
//
//						connectionMPI.asynchronousReceive(topicW,l3);
//
//						MPInFieldsListeners l4 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);
//
//						connectionMPI.asynchronousReceive(topicE,l4);

						MPInFieldsListeners l5 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNF,l5);

						MPInFieldsListeners l6 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNW,l6);

						MPInFieldsListeners l7 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNR,l7);

						MPInFieldsListeners l8 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNE,l8);

						MPInFieldsListeners l9 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSF,l9);

						MPInFieldsListeners l10 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSW,l10);

						MPInFieldsListeners l11 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSR,l11);

						MPInFieldsListeners l12 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSE,l12);

						MPInFieldsListeners l13 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicWF,l13);

						MPInFieldsListeners l14 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicWR,l14);

						MPInFieldsListeners l15 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicEF,l15);

						MPInFieldsListeners l16 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicER,l16);

						MPInFieldsListeners l17 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNWF,l17);

						MPInFieldsListeners l18 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNWR,l18);

						MPInFieldsListeners l19 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNEF,l19);

						MPInFieldsListeners l20 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNER,l20);

						MPInFieldsListeners l21 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSWF,l21);

						MPInFieldsListeners l22 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSWR,l22);

						MPInFieldsListeners l23 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSEF,l23);

						MPInFieldsListeners l24 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSER,l24);



					}else if(rows==1 && columns>1){


						connectionMPI.createTopic(topicPrefix+TYPE+"W",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"E",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"WF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"WR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"EF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"ER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());

						String topicW=topicPrefix+(((i+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"W";
						String topicE=topicPrefix+(((i+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"E";

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

						connectionMPI.subscribeToTopic(topicW);
						connectionMPI.subscribeToTopic(topicE);
						connectionMPI.subscribeToTopic(topicNF);
						connectionMPI.subscribeToTopic(topicNW);
						connectionMPI.subscribeToTopic(topicNR);
						connectionMPI.subscribeToTopic(topicNE);
						connectionMPI.subscribeToTopic(topicSF);
						connectionMPI.subscribeToTopic(topicSW);
						connectionMPI.subscribeToTopic(topicSR);
						connectionMPI.subscribeToTopic(topicSE);
						connectionMPI.subscribeToTopic(topicWF);
						connectionMPI.subscribeToTopic(topicWR);
						connectionMPI.subscribeToTopic(topicER);
						connectionMPI.subscribeToTopic(topicEF);
						connectionMPI.subscribeToTopic(topicNWF);
						connectionMPI.subscribeToTopic(topicNWR);
						connectionMPI.subscribeToTopic(topicNEF);
						connectionMPI.subscribeToTopic(topicNER);
						connectionMPI.subscribeToTopic(topicSWF);
						connectionMPI.subscribeToTopic(topicSWR);
						connectionMPI.subscribeToTopic(topicSEF);
						connectionMPI.subscribeToTopic(topicSER);

						MPInFieldsListeners l1 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicW,l1);

						MPInFieldsListeners l2 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicE,l2);

						MPInFieldsListeners l3 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNF,l3);

						MPInFieldsListeners l4 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNW,l4);

						MPInFieldsListeners l5 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNR,l5);

						MPInFieldsListeners l6 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNE,l6);

						MPInFieldsListeners l7 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSF,l7);

						MPInFieldsListeners l8 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSW,l8);

						MPInFieldsListeners l9 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSR,l9);

						MPInFieldsListeners l10 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSE,l10);

						MPInFieldsListeners l11 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicWF,l11);

						MPInFieldsListeners l12 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicWR,l12);

						MPInFieldsListeners l13 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicEF,l13);

						MPInFieldsListeners l14 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicER,l14);

						MPInFieldsListeners l15 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNWF,l15);

						MPInFieldsListeners l16 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNWR,l16);

						MPInFieldsListeners l17 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNEF,l17);

						MPInFieldsListeners l18 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNER,l18);

						MPInFieldsListeners l19 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSWF,l19);

						MPInFieldsListeners l20	 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSWR,l20);

						MPInFieldsListeners l21 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSEF,l21);

						MPInFieldsListeners l22 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSER,l22);

					}else{ // rows>1 && columns>1 && lenght==1
						connectionMPI.createTopic(topicPrefix+TYPE+"N",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"S",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"W",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"E",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"WF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"WR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"EF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"ER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());

						String topicN=topicPrefix+(((i-1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+lenghts)%lenghts))+"N";
						String topicS=topicPrefix+(((i+1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+lenghts)%lenghts))+"S";
						String topicW=topicPrefix+(((i+rows)%rows)+"-"+((j+1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"W";
						String topicE=topicPrefix+(((i+rows)%rows)+"-"+((j-1+columns)%columns)+"-"+((z+lenghts)%lenghts))+"E";

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

						connectionMPI.subscribeToTopic(topicN);
						connectionMPI.subscribeToTopic(topicS);
						connectionMPI.subscribeToTopic(topicW);
						connectionMPI.subscribeToTopic(topicE);
						connectionMPI.subscribeToTopic(topicNF);
						connectionMPI.subscribeToTopic(topicNW);
						connectionMPI.subscribeToTopic(topicNR);
						connectionMPI.subscribeToTopic(topicNE);
						connectionMPI.subscribeToTopic(topicSF);
						connectionMPI.subscribeToTopic(topicSW);
						connectionMPI.subscribeToTopic(topicSR);
						connectionMPI.subscribeToTopic(topicSE);
						connectionMPI.subscribeToTopic(topicWF);
						connectionMPI.subscribeToTopic(topicWR);
						connectionMPI.subscribeToTopic(topicER);
						connectionMPI.subscribeToTopic(topicEF);
						connectionMPI.subscribeToTopic(topicNWF);
						connectionMPI.subscribeToTopic(topicNWR);
						connectionMPI.subscribeToTopic(topicNEF);
						connectionMPI.subscribeToTopic(topicNER);
						connectionMPI.subscribeToTopic(topicSWF);
						connectionMPI.subscribeToTopic(topicSWR);
						connectionMPI.subscribeToTopic(topicSEF);
						connectionMPI.subscribeToTopic(topicSER);

						MPInFieldsListeners l1 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicN,l1);

						MPInFieldsListeners l2 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicS,l2);

						MPInFieldsListeners l3 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicW,l3);

						MPInFieldsListeners l4 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicE,l4);

						MPInFieldsListeners l5 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNF,l5);

						MPInFieldsListeners l6 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNW,l6);

						MPInFieldsListeners l7 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNR,l7);

						MPInFieldsListeners l8 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNE,l8);

						MPInFieldsListeners l9 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSF,l9);

						MPInFieldsListeners l10 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSW,l10);

						MPInFieldsListeners l11 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSR,l11);

						MPInFieldsListeners l12 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSE,l12);

						MPInFieldsListeners l13 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicWF,l13);

						MPInFieldsListeners l14 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicWR,l14);

						MPInFieldsListeners l15 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicEF,l15);

						MPInFieldsListeners l16 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicER,l16);

						MPInFieldsListeners l17 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNWF,l17);

						MPInFieldsListeners l18 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNWR,l18);

						MPInFieldsListeners l19 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNEF,l19);

						MPInFieldsListeners l20 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNER,l20);

						MPInFieldsListeners l21 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSWF,l21);

						MPInFieldsListeners l22 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSWR,l22);

						MPInFieldsListeners l23 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSEF,l23);

						MPInFieldsListeners l24 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSER,l24);

					}
				}else{ //length>1
					if (rows==1 && columns==1){

						connectionMPI.createTopic(topicPrefix+TYPE+"F",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"R",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"WF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"WR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"EF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"ER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());

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

						connectionMPI.subscribeToTopic(topicF);
						connectionMPI.subscribeToTopic(topicR);
						connectionMPI.subscribeToTopic(topicNF);
						connectionMPI.subscribeToTopic(topicNW);
						connectionMPI.subscribeToTopic(topicNR);
						connectionMPI.subscribeToTopic(topicNE);
						connectionMPI.subscribeToTopic(topicSF);
						connectionMPI.subscribeToTopic(topicSW);
						connectionMPI.subscribeToTopic(topicSR);
						connectionMPI.subscribeToTopic(topicSE);
						connectionMPI.subscribeToTopic(topicWF);
						connectionMPI.subscribeToTopic(topicWR);
						connectionMPI.subscribeToTopic(topicER);
						connectionMPI.subscribeToTopic(topicEF);
						connectionMPI.subscribeToTopic(topicNWF);
						connectionMPI.subscribeToTopic(topicNWR);
						connectionMPI.subscribeToTopic(topicNEF);
						connectionMPI.subscribeToTopic(topicNER);
						connectionMPI.subscribeToTopic(topicSWF);
						connectionMPI.subscribeToTopic(topicSWR);
						connectionMPI.subscribeToTopic(topicSEF);
						connectionMPI.subscribeToTopic(topicSER);

						MPInFieldsListeners l1 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicF,l1);

						MPInFieldsListeners l2 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicR,l2);

						MPInFieldsListeners l3 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNF,l3);

						MPInFieldsListeners l4 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNW,l4);

						MPInFieldsListeners l5 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNR,l5);

						MPInFieldsListeners l6 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNE,l6);

						MPInFieldsListeners l7 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSF,l7);

						MPInFieldsListeners l8 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSW,l8);

						MPInFieldsListeners l9 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSR,l9);

						MPInFieldsListeners l10 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSE,l10);

						MPInFieldsListeners l11 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicWF,l11);

						MPInFieldsListeners l12 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicWR,l12);

						MPInFieldsListeners l13 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicEF,l13);

						MPInFieldsListeners l14 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicER,l14);

						MPInFieldsListeners l15 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNWF,l15);

						MPInFieldsListeners l16 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNWR,l16);

						MPInFieldsListeners l17 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNEF,l17);

						MPInFieldsListeners l18 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNER,l18);

						MPInFieldsListeners l19 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSWF,l19);

						MPInFieldsListeners l20 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSWR,l20);

						MPInFieldsListeners l21 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSEF,l21);
						MPInFieldsListeners l22 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSER,l22);

						//


					}else if(rows>1 && columns==1){

						connectionMPI.createTopic(topicPrefix+TYPE+"N",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"S",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"F",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"R",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"WF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"WR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"EF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"ER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());

						String topicN=topicPrefix+(((i-1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+lenghts)%lenghts))+"N";
						String topicS=topicPrefix+(((i+1+rows)%rows)+"-"+((j+columns)%columns)+"-"+((z+lenghts)%lenghts))+"S";
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

						connectionMPI.subscribeToTopic(topicN);
						connectionMPI.subscribeToTopic(topicS);
						connectionMPI.subscribeToTopic(topicF);
						connectionMPI.subscribeToTopic(topicR);
						connectionMPI.subscribeToTopic(topicNF);
						connectionMPI.subscribeToTopic(topicNW);
						connectionMPI.subscribeToTopic(topicNR);
						connectionMPI.subscribeToTopic(topicNE);
						connectionMPI.subscribeToTopic(topicSF);
						connectionMPI.subscribeToTopic(topicSW);
						connectionMPI.subscribeToTopic(topicSR);
						connectionMPI.subscribeToTopic(topicSE);
						connectionMPI.subscribeToTopic(topicWF);
						connectionMPI.subscribeToTopic(topicWR);
						connectionMPI.subscribeToTopic(topicER);
						connectionMPI.subscribeToTopic(topicEF);
						connectionMPI.subscribeToTopic(topicNWF);
						connectionMPI.subscribeToTopic(topicNWR);
						connectionMPI.subscribeToTopic(topicNEF);
						connectionMPI.subscribeToTopic(topicNER);
						connectionMPI.subscribeToTopic(topicSWF);
						connectionMPI.subscribeToTopic(topicSWR);
						connectionMPI.subscribeToTopic(topicSEF);
						connectionMPI.subscribeToTopic(topicSER);

						MPInFieldsListeners l1 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicN,l1);

						MPInFieldsListeners l2 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicS,l2);

						MPInFieldsListeners l3 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicF,l3);

						MPInFieldsListeners l4 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicR,l4);

						MPInFieldsListeners l5 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNF,l5);

						MPInFieldsListeners l6 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNW,l6);

						MPInFieldsListeners l7 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNR,l7);

						MPInFieldsListeners l8 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNE,l8);

						MPInFieldsListeners l9 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSF,l9);

						MPInFieldsListeners l10 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSW,l10);

						MPInFieldsListeners l11 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSR,l11);

						MPInFieldsListeners l12 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSE,l12);

						MPInFieldsListeners l13 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicWF,l13);

						MPInFieldsListeners l14 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicWR,l14);

						MPInFieldsListeners l15 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicEF,l15);

						MPInFieldsListeners l16 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicER,l16);

						MPInFieldsListeners l17 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNWF,l17);

						MPInFieldsListeners l18 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNWR,l18);

						MPInFieldsListeners l19 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNEF,l19);

						MPInFieldsListeners l20 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNER,l20);

						MPInFieldsListeners l21 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSWF,l21);

						MPInFieldsListeners l22 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSWR,l22);

						MPInFieldsListeners l23 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSEF,l23);

						MPInFieldsListeners l24 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSER,l24);


					}else if(rows==1 && columns>1){

						connectionMPI.createTopic(topicPrefix+TYPE+"W",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"E",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"F",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"R",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"WF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"WR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"EF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"ER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());

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

						connectionMPI.subscribeToTopic(topicW);
						connectionMPI.subscribeToTopic(topicE);
						connectionMPI.subscribeToTopic(topicF);
						connectionMPI.subscribeToTopic(topicR);
						connectionMPI.subscribeToTopic(topicNF);
						connectionMPI.subscribeToTopic(topicNW);
						connectionMPI.subscribeToTopic(topicNR);
						connectionMPI.subscribeToTopic(topicNE);
						connectionMPI.subscribeToTopic(topicSF);
						connectionMPI.subscribeToTopic(topicSW);
						connectionMPI.subscribeToTopic(topicSR);
						connectionMPI.subscribeToTopic(topicSE);
						connectionMPI.subscribeToTopic(topicWF);
						connectionMPI.subscribeToTopic(topicWR);
						connectionMPI.subscribeToTopic(topicER);
						connectionMPI.subscribeToTopic(topicEF);
						connectionMPI.subscribeToTopic(topicNWF);
						connectionMPI.subscribeToTopic(topicNWR);
						connectionMPI.subscribeToTopic(topicNEF);
						connectionMPI.subscribeToTopic(topicNER);
						connectionMPI.subscribeToTopic(topicSWF);
						connectionMPI.subscribeToTopic(topicSWR);
						connectionMPI.subscribeToTopic(topicSEF);
						connectionMPI.subscribeToTopic(topicSER);

						MPInFieldsListeners l1 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicW,l1);

						MPInFieldsListeners l2 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicE,l2);

						MPInFieldsListeners l3 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicF,l3);

						MPInFieldsListeners l4 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicR,l4);

						MPInFieldsListeners l5 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNF,l5);

						MPInFieldsListeners l6 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNW,l6);

						MPInFieldsListeners l7 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNR,l7);

						MPInFieldsListeners l8 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNE,l8);

						MPInFieldsListeners l9 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSF,l9);

						MPInFieldsListeners l10 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSW,l10);

						MPInFieldsListeners l11 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSR,l11);

						MPInFieldsListeners l12 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSE,l12);

						MPInFieldsListeners l13 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicWF,l13);

						MPInFieldsListeners l14 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicWR,l14);

						MPInFieldsListeners l15 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicEF,l15);

						MPInFieldsListeners l16 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicER,l16);

						MPInFieldsListeners l17 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNWF,l17);

						MPInFieldsListeners l18 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNWR,l18);

						MPInFieldsListeners l19 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNEF,l19);

						MPInFieldsListeners l20 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNER,l20);

						MPInFieldsListeners l21 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSWF,l21);

						MPInFieldsListeners l22 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSWR,l22);

						MPInFieldsListeners l23 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSEF,l23);

						MPInFieldsListeners l24 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSER,l24);


					}else {
						connectionMPI.createTopic(topicPrefix+TYPE+"N",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"S",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"W",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"E",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"F",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"R",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SW",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SE",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"WF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"WR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"EF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"ER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"NER",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SWF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SWR",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SEF",
								((DistributedMultiSchedule)dm.schedule).fields3D.size());
						connectionMPI.createTopic(topicPrefix+TYPE+"SER",
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

						connectionMPI.subscribeToTopic(topicN);
						connectionMPI.subscribeToTopic(topicS);
						connectionMPI.subscribeToTopic(topicW);
						connectionMPI.subscribeToTopic(topicE);
						connectionMPI.subscribeToTopic(topicF);
						connectionMPI.subscribeToTopic(topicR);
						connectionMPI.subscribeToTopic(topicNF);
						connectionMPI.subscribeToTopic(topicNW);
						connectionMPI.subscribeToTopic(topicNR);
						connectionMPI.subscribeToTopic(topicNE);
						connectionMPI.subscribeToTopic(topicSF);
						connectionMPI.subscribeToTopic(topicSW);
						connectionMPI.subscribeToTopic(topicSR);
						connectionMPI.subscribeToTopic(topicSE);
						connectionMPI.subscribeToTopic(topicWF);
						connectionMPI.subscribeToTopic(topicWR);
						connectionMPI.subscribeToTopic(topicER);
						connectionMPI.subscribeToTopic(topicEF);
						connectionMPI.subscribeToTopic(topicNWF);
						connectionMPI.subscribeToTopic(topicNWR);
						connectionMPI.subscribeToTopic(topicNEF);
						connectionMPI.subscribeToTopic(topicNER);
						connectionMPI.subscribeToTopic(topicSWF);
						connectionMPI.subscribeToTopic(topicSWR);
						connectionMPI.subscribeToTopic(topicSEF);
						connectionMPI.subscribeToTopic(topicSER);

						MPInFieldsListeners l1 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicN,l1);

						MPInFieldsListeners l2 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicS,l2);

						MPInFieldsListeners l3 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicW,l3);

						MPInFieldsListeners l4 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicE,l4);

						MPInFieldsListeners l5 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicF,l5);

						MPInFieldsListeners l6 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicR,l6);

						MPInFieldsListeners l7 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNF,l7);

						MPInFieldsListeners l8 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNW,l8);

						MPInFieldsListeners l9 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNR,l9);

						MPInFieldsListeners l10 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNE,l10);

						MPInFieldsListeners l11 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSF,l11);

						MPInFieldsListeners l12 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSW,l12);

						MPInFieldsListeners l13 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSR,l13);

						MPInFieldsListeners l14 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSE,l14);

						MPInFieldsListeners l15 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicWF,l15);

						MPInFieldsListeners l16 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicWR,l16);

						MPInFieldsListeners l17 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicEF,l17);

						MPInFieldsListeners l18 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicER,l18);

						MPInFieldsListeners l19 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNWF,l19);

						MPInFieldsListeners l20 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNWR,l20);

						MPInFieldsListeners l21 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNEF,l21);

						MPInFieldsListeners l22 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicNER,l22);

						MPInFieldsListeners l23 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSWF,l23);

						MPInFieldsListeners l24 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSWR,l24);

						MPInFieldsListeners l25 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSEF,l25);

						MPInFieldsListeners l26 = new MPInFieldsListeners(((DistributedMultiSchedule) schedule).fields3D);

						connectionMPI.asynchronousReceive(topicSER,l26);

					}
				}
			}catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}

	public void init_connection() {
		if(((DistributedMultiSchedule<E>)dm.schedule).fields2D.size()>0)
			init_spatial_connection();
		if(((DistributedMultiSchedule<E>)dm.schedule).fields3D.size()>0)
			init_3Dspatial_connection();
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
								+ "NW");
						connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbourDiagRightDown()
								+ "NW",l);
					}
					if(TYPE.pos_i < rows-1 && TYPE.pos_j > 0){
						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagLeftDown()
								+ "NE");
						connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbourDiagLeftDown()
								+ "NE",l);
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
						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourRight() + "W");
						connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbourRight() + "W",l);
					}
					if(TYPE.pos_j > 0){
						connectionMPI.subscribeToTopic(topicPrefix+TYPE.getNeighbourLeft() + "E");
						connectionMPI.asynchronousReceive(topicPrefix+TYPE.getNeighbourLeft() + "E",l);
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

	public ArrayList<MessageListener3D> getLocal3DListener() {
		return listeners3D;
	}

}