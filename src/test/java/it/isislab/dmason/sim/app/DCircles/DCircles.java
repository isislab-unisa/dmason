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
package it.isislab.dmason.sim.app.DCircles;

import java.awt.Color;
import java.util.List;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.experimentals.tools.batch.data.EntryParam;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.continuous.DContinuousGrid2D;
import it.isislab.dmason.sim.field.continuous.DContinuousGrid2DFactory;
import sim.engine.SimState;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.util.Double2D;

public class DCircles extends DistributedState<Double2D> {

	private static final long serialVersionUID = 1L;

	/**
	 * it's used for creating circle agents
	 */
	public static final double DIAMETER = 4;

	protected DContinuousGrid2D circles = null;

	private String topicPrefix = "";
	
	/**
	 * field Width
	 */
	public double gridWidth ;
	/**
	 * field Height
	 */
	public double gridHeight ;
	
	public int MODE;
	
	public ContinuousPortrayal2D p;
	
	/**
	 * empty costructor for Serialize
	 */
	public DCircles() { super();}
	
	/**
	 * Constructor 
	 * @param params
	 */
	public DCircles(GeneralParam params,String prefix) {
		super(params, new DistributedMultiSchedule<Double2D>(),prefix,params.getConnectionType());
		this.MODE=params.getMode();
		this.topicPrefix=prefix;
		gridWidth=params.getWidth();
		gridHeight=params.getHeight();
	}

	public DCircles(GeneralParam params,List<EntryParam<String, Object>> simParams,String prefix)
	{
		super(params,new DistributedMultiSchedule<Double2D>(), prefix,params.getConnectionType());
		this.MODE=params.getMode();
		this.topicPrefix = prefix;
		gridWidth=params.getWidth();
		gridHeight=params.getHeight();
		 
		for (EntryParam<String, Object> entryParam : simParams) {

			try {
				this.getClass().getDeclaredField(entryParam.getParamName()).set(this, entryParam.getParamValue());
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		for (EntryParam<String, Object> entryParam : simParams) {

			try {
				System.out.println(this.getClass().getDeclaredField(entryParam.getParamName()).get(this));
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	@Override
	public void start()
	{
		super.start();
		/* For setting the agent in circular way, we have to make a invisible circle within the field.
		   Next, we need to know what will be the circle center.
		   For calculating the radius, basically we take the half of smaller side (in this case we don't worry about double values, because the filed supports double coordinates).
		   During the casual position generation, the agent's location will be regenerate until it is within the circle area.
		*/
		
		Double2D center = new Double2D(gridWidth/2, gridHeight/2);
		double radius = (gridWidth < gridHeight)?gridWidth/2:gridHeight/2;
		Double2D loc = null;
		try 
		{
			circles = DContinuousGrid2DFactory.createDContinuous2D(8.0,gridWidth, gridHeight,this,
					super.AOI,TYPE.pos_i,TYPE.pos_j,super.rows,super.columns,MODE,"circles", topicPrefix,false);
			init_connection();
		} catch (DMasonException e) { e.printStackTrace(); }
		
		DCircle f=new DCircle(this,new Double2D(0,0));

		while(circles.size() != super.NUMAGENTS / super.NUMPEERS)
		{
			//circular positions distribution 
			loc = circles.getAvailableRandomLocation();
			//if the position is not within the radius, recalculate it
			while(DCircle.euclideanDistance(center, loc) > radius)
				loc = circles.getAvailableRandomLocation();
			
			f.setPos(loc);

			if(circles.setObjectLocation(f, f.pos))
			{
				Color c=new Color(0,0,0);/*
						this.random.nextInt(255),
						this.random.nextInt(255),
						this.random.nextInt(255));*/
				f.setColor(c);
				schedule.scheduleOnce(f);
				f= new DCircle(this,new Double2D(0,0));
			}
		}
		
	}
	@Override
	public DistributedField2D getField() {
		// TODO Auto-generated method stub
		return circles;
	}

	@Override
	public void addToField(RemotePositionedAgent rm, Double2D loc) {
		circles.setObjectLocation(rm, loc);
	}

	@Override
	public SimState getState() {
		// TODO Auto-generated method stub
		return this;
	}

	
	public static void main(String[] args)
	{
		doLoop(DCircles.class, args);
		System.exit(0);
	}
}
