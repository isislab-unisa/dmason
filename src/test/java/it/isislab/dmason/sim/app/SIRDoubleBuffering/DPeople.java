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
package it.isislab.dmason.sim.app.SIRDoubleBuffering;

import java.util.List;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.experimentals.tools.batch.data.EntryParam;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.DistributedField;
import it.isislab.dmason.sim.field.continuous.DContinuousGrid2D;
import it.isislab.dmason.sim.field.continuous.DContinuousGrid2DFactory;
import sim.engine.SimState;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.util.Double2D;

public class DPeople extends DistributedState<Double2D> {

	public DContinuousGrid2D environment;
	ContinuousPortrayal2D p;
	public double gridWidth ;
	public double gridHeight ;   
	public int MODE;
	
	public String topicPrefix = "";


	public DPeople() {
		super();
		// TODO Auto-generated constructor stub
	}

	
	public DPeople(GeneralParam params,String prefix)
	{    	
		super(params,new DistributedMultiSchedule<Double2D>(),prefix,params.getConnectionType());
		this.topicPrefix=prefix;
		this.MODE=params.getMode();
		this.gridWidth=params.getWidth();
		this.gridHeight=params.getHeight();
	}

	public DPeople(GeneralParam params,List<EntryParam<String, Object>> simParams, String prefix)
	{    	
		super(params,new DistributedMultiSchedule<Double2D>(), prefix,params.getConnectionType());
		this.MODE=params.getMode();
		gridWidth=params.getWidth();
		gridHeight=params.getHeight();
		topicPrefix = prefix; 
		for (EntryParam<String, Object> entryParam : simParams) {

			try {
				this.getClass().getDeclaredField(entryParam.getParamName()).set(this, entryParam.getParamValue());
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

		for (EntryParam<String, Object> entryParam : simParams) {

			try {
				System.out.println(this.getClass().getDeclaredField(entryParam.getParamName()).get(this));
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
	
	public void start()
	{
		super.start();
		
		try 
		{
			environment = DContinuousGrid2DFactory.createDContinuous2D(8.0,gridWidth, gridHeight,this,
					super.AOI,TYPE.pos_i,TYPE.pos_j,super.rows,super.columns,MODE,"SIR", topicPrefix,false);
			init_connection();
		} catch (DMasonException e) { e.printStackTrace(); }
		
		boolean isInfected = (random.nextDouble()<=0.3); 

		/*DHuman f= (DHuman) DistributedAgentFactory.newIstance(
				DHuman.class,
				new Class[]{SimState.class,Double2D.class,Boolean.class},
				new Object[]{this,new Double2D(0,0),isInfected},
				DHumanState.class);*/
		DHuman f = new DHuman(this, new Double2D(0,0),isInfected);
		
		while(environment.size() != super.NUMAGENTS / super.NUMPEERS)
		{
			f.setPos(environment.getAvailableRandomLocation());

			if(environment.setObjectLocation(f, f.pos))
			{
				schedule.scheduleOnce(f);
				/*f= (DHuman) DistributedAgentFactory.newIstance(
						DHuman.class,
						new Class[]{SimState.class,Double2D.class,Boolean.class},
						new Object[]{this,f.getPos(),isInfected},
						DHumanState.class);*/
				f= new DHuman(this, new Double2D(0,0), (random.nextDouble()<=0.3));
			}

		}
		
	}
	
	
	@Override
	public DistributedField<Double2D> getField() {
		// TODO Auto-generated method stub
		return environment;
	}

	@Override
	public void addToField(RemotePositionedAgent<Double2D> rm, Double2D loc) {
		environment.setObjectLocation(rm, loc);
		
	}

	@Override
	public SimState getState() {
		// TODO Auto-generated method stub
		return this;
	}

}
