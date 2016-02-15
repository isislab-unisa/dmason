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
package it.isislab.dmason.sim.app.DFlockers;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.experimentals.tools.batch.data.EntryParam;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.continuous.DContinuousGrid2D;
import it.isislab.dmason.sim.field.continuous.DContinuousGrid2DFactory;
import java.awt.Color;
import java.util.List;
import sim.engine.SimState;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.simple.AdjustablePortrayal2D;
import sim.portrayal.simple.MovablePortrayal2D;
import sim.portrayal.simple.OrientedPortrayal2D;
import sim.util.Double2D;
/**
 * 
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 *
 */
public class DFlockers extends DistributedState<Double2D>
{
	public DFlockers()
	{
		super();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public DContinuousGrid2D flockers;


	public int numFlockers = 20;
	public double cohesion = 1.0;
	public double avoidance = 1.0;
	public double randomness = 1.0;
	public double consistency = 1.0;
	public double momentum = 1.0;
	public double deadFlockerProbability = 0.1;
	public double neighborhood = 10;
	public double jump = 0.7;  // how far do we move in a timestep?


	public double gridWidth ;
	public double gridHeight ;   
	public int MODE;

	protected ContinuousPortrayal2D p;

	/**
	 * 
	 * @param params in order to build a distributed field
	 * @param simParams parameters for this simulation
	 * @param prefix a string added to topic to generate an unique topic for this simulation
	 */
	public DFlockers(GeneralParam params,String prefix)
	{    	
		super(params,new DistributedMultiSchedule<Double2D>(),prefix,params.getConnectionType());
		this.topicPrefix=prefix;
		this.MODE=params.getMode();
		this.gridWidth=params.getWidth();
		this.gridHeight=params.getHeight();
	}

	/**
	 * DFlockers
	 * 
	 * @param params in order to build a distributed field
	 * @param list   parameters for this simulation 
	 * @param prefix a string added to topic to generate an unique topic for this simulation 
	 */
	public DFlockers(GeneralParam params,List<EntryParam<String, Object>> simParams, String prefix)
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


	@Override
	public void start()
	{

		/*	TEST java.net.URL url = this.getClass().getResource("file.txt");
		 InputStream reader =url.openStream();
		 StringWriter writer=new StringWriter();
		 IOUtils.copy(reader, writer);
		 System.out.println(writer.toString());
		 reader.close();
		 writer.close();
		 */



		super.start();

		// set up the flockers field.  It looks like a discretization
		// of about neighborhood / 1.5 is close to optimal for us.  Hmph,
		// that's 16 hash lookups! I would have guessed that 
		// neighborhood * 2 (which is about 4 lookups on average)
		// would be optimal.  Go figure.
		try 
		{
			flockers = DContinuousGrid2DFactory.createDContinuous2D(neighborhood/1.5,gridWidth, gridHeight,this,
					super.AOI,TYPE.pos_i,TYPE.pos_j,super.rows,super.columns,MODE,"flockers", topicPrefix,true);
			init_connection();
		} catch (DMasonException e) { e.printStackTrace(); }


		/*
		 * Spawn agents only on cell 0-0
		 */
		//if ( (TYPE.pos_i == 0 && TYPE.pos_j == 0) )
		{

			DFlocker f=new DFlocker(this,new Double2D(0,0));
			int j=0;

			while(flockers.size() != super.NUMAGENTS / super.NUMPEERS)
			{



				f.setPos(flockers.getAvailableRandomLocation());

				//    			if (random.nextBoolean(deadFlockerProbability))
				//    				f.dead = true;

				if(flockers.setObjectLocation(f, f.pos))
				{
					Color c=new Color(
							128 + this.random.nextInt(128),
							128 + this.random.nextInt(128),
							128 + this.random.nextInt(128));
					f.setColor(c);
					schedule.scheduleOnce(f);
					f=new DFlocker(this,new Double2D(0,0));
				}

				j++;
			}
		} //if ( (TYPE.pos_i == 0 && TYPE.pos_j == 0) )

		try {
			if(getTrigger()!=null)
				getTrigger().publishToTriggerTopic("Simulation cell "+flockers.cellType+" ready...");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		doLoop(DFlockers.class, args);
		System.exit(0);
	}

	@Override
	public DistributedField2D getField() 
	{
		return flockers;
	}

	@Override
	public void addToField(RemotePositionedAgent rm, Double2D loc) 
	{
		flockers.setObjectLocation(rm,loc);
		setPortrayalForObject(rm);

	}

	@Override
	public SimState getState() 
	{
		return this;
	}

	public boolean setPortrayalForObject(Object o) 
	{
		if(p!=null)
		{
			DFlocker f=(DFlocker)o;
			SimplePortrayal2D pp = new AdjustablePortrayal2D(new MovablePortrayal2D(new OrientedPortrayal2D(new SimplePortrayal2D(),0,4.0,
					f.getColor(),
					OrientedPortrayal2D.SHAPE_COMPASS)));
			p.setPortrayalForObject(o, pp);
			return true;
		}
		return false;
	}    
}