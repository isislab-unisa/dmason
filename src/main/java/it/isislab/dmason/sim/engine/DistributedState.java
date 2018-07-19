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

import it.isislab.dmason.experimentals.tools.batch.data.EntryParam;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.experimentals.util.management.globals.util.UpdateGlobalVarAtStep;
import it.isislab.dmason.experimentals.util.trigger.Trigger;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField;
import it.isislab.dmason.sim.field.MessageListener;
import it.isislab.dmason.util.connection.Connection;
import it.isislab.dmason.util.connection.ConnectionType;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.jms.JMSException;
import sim.engine.SimState;
import ec.util.MersenneTwisterFast;

/**
 * An abstract class that inherits all SimState functionalities and adds
 * necessary distributed informations: the number of agents, used to calculate
 * the sequence of agents id, the type of simulated cell, the maximum shift of
 * agents, the number of peers involved in the simulation, the ip and port of
 * server.
 *
 * @param <E>  type of locations
 *
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * @author Luca Vicidomini
 * @author Matteo D'Auria
 */
public abstract class DistributedState<E> extends SimState {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public int NUMAGENTS;
	protected int count_id;
	public int AOI;
	public int NUMPEERS;
	public int MODE;
	public HashMap<String, Integer> networkNumberOfSubscribersForField = new HashMap<String, Integer>();
	public int rows;
	public int columns;
	public int lenghts; // third dimension
	public boolean is3D;
	public String topicPrefix = "";
	public int P;
	public CellType TYPE;
	public UpdateGlobalVarAtStep upVar = null;
	protected boolean isPureMPI = false;
	protected boolean isPureAMQ = false;
	protected boolean isHybrid = false;
	protected DistributedStateConnectionJMS<E> serviceJMS;
	protected DistributedStateConnectionMPI<E> serviceMPI;

	public PrintStream out;

	public void setOutputStream(PrintStream out)
	{
		this.out=out;
	}

	public DistributedState() {
		super(null, new DistributedMultiSchedule<E>());
	}

	public DistributedState(DistributedMultiSchedule<E> schedule) {
		super(null, schedule);
	}


	public DistributedState(GeneralParam params,
							DistributedMultiSchedule<E> sched, String prefix,
							int typeOfConnection) {
		super(null, sched);
		P=params.getP();
		long randomizer = 0;
		if (prefix.startsWith("Batch"))
			randomizer = System.currentTimeMillis();
		if(params.getIs3D()){
			this.TYPE = new CellType(params.getI(), params.getJ(),0);
		} else {
			this.TYPE = new CellType(params.getI(), params.getJ());
		}

		this.random = new MersenneTwisterFast(randomizer
				+ this.TYPE.getInitialValue());
		this.AOI = params.getAoi();
		if(params.getIs3D()){
			this.NUMPEERS = params.getRows() * params.getColumns()*1;
		}else{
			this.NUMPEERS = params.getRows() * params.getColumns();
		}

		this.rows = params.getRows();
		this.columns = params.getColumns();
		this.lenghts = 1;
		this.NUMAGENTS = params.getNumAgents();
		this.count_id = NUMAGENTS * TYPE.getInitialValue();
		this.MODE = params.getMode();
		this.topicPrefix = prefix;
		this.is3D = params.getIs3D();

		switch (typeOfConnection) {
			case ConnectionType.pureActiveMQ:
				serviceJMS = new DistributedStateConnectionJMS(this,
						params.getIp(), params.getPort());
				isPureAMQ = true;
				break;
			case ConnectionType.hybridActiveMQMPIBcast:
				serviceJMS = new DistributedStateConnectionJMS(this,
						params.getIp(), params.getPort());
				serviceMPI = new DistributedStateConnectionMPI(this,
						typeOfConnection);
				isHybrid = true;
				break;
			case ConnectionType.hybridActiveMQMPIGather:
				serviceJMS = new DistributedStateConnectionJMS(this,
						params.getIp(), params.getPort());
				serviceMPI = new DistributedStateConnectionMPI(this,
						typeOfConnection);
				isHybrid = true;
				break;
			case ConnectionType.hybridActiveMQMPIParallel:
				serviceJMS = new DistributedStateConnectionJMS(this,
						params.getIp(), params.getPort());
				serviceMPI = new DistributedStateConnectionMPI(this,
						typeOfConnection);
				isHybrid = true;
				break;
			case ConnectionType.hybridMPIMultipleThreads:
				serviceJMS = new DistributedStateConnectionJMS(this,
						params.getIp(), params.getPort());
				serviceMPI = new DistributedStateConnectionMPI(this,
						typeOfConnection);
				isHybrid = true;
				break;
			case ConnectionType.pureMPIBcast:
				serviceMPI = new DistributedStateConnectionMPI(this,
						typeOfConnection);
				isPureMPI = true;
				break;
			case ConnectionType.pureMPIGather:
				serviceMPI = new DistributedStateConnectionMPI(this,
						typeOfConnection);
				isPureMPI = true;
				break;
			case ConnectionType.pureMPIParallel:
				serviceMPI = new DistributedStateConnectionMPI(this,
						typeOfConnection);
				isPureMPI = true;
				break;
			case ConnectionType.pureMPIMultipleThreads:
				serviceMPI = new DistributedStateConnectionMPI(this,
						typeOfConnection);
				isPureMPI = true;
				break;
			default:
				break;
		}

	}


	public void init_connection() {
		if (isPureAMQ) {
			serviceJMS.init_connection();
		}
		if (isPureMPI) {
			serviceMPI.init_connection();
		}
		if (isHybrid) {
			serviceMPI.init_connection();
			serviceJMS.init_service_connection();
		}
	}

	// abstract methods those must be implemented in the subclasses
	public abstract DistributedField<E> getField();

	public abstract void addToField(RemotePositionedAgent<E> rm, E loc);

	public abstract SimState getState();

	public CellType getType() {
		return TYPE;
	}

	/**
	 * @return the next available Id
	 */
	public int nextId() {
		return ++count_id;
	}

	public ArrayList<MessageListener> getLocalListener() {
		if (serviceJMS == null)
			return new ArrayList<MessageListener>();
		return serviceJMS.getLocalListener();
	}

	public Trigger getTrigger() {
		if (serviceJMS == null)
			return null;
		return serviceJMS.getTrigger();
	}

	// added for close connection of current simulation after reset
	public void closeConnectionJMS() throws JMSException {
		if (serviceJMS == null)
			return;
		serviceJMS.closeConnectionJMS();
	}

	public Connection getCommunicationWorkerConnection() {
		if (isPureMPI || isHybrid)
			return serviceMPI.getConnection();

		return serviceJMS.getConnection();

	}

	public Connection getCommunicationManagementConnection() {
		if (isPureMPI)
			return null;
		return serviceJMS.getConnection();
	}

	public DistributedStateConnectionJMS getDistributedStateConnectionJMS() {

		return serviceJMS;
	}

	public Connection getCommunicationVisualizationConnection() {
		if (isPureMPI)
			return null;
		return serviceJMS.getConnection();
	}

	protected void setSimulationParameters(
			List<EntryParam<String, Object>> simulationParameters) {
		for (EntryParam<String, Object> entryParam : simulationParameters) {
			try {
				this.getClass().getDeclaredField(entryParam.getParamName())
						.set(this, entryParam.getParamValue());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		}
	}

}