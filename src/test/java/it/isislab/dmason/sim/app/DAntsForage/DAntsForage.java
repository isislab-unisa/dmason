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
package it.isislab.dmason.sim.app.DAntsForage;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.grid.numeric.DDoubleGrid2D;
import it.isislab.dmason.sim.field.grid.numeric.DDoubleGrid2DFactory;
import it.isislab.dmason.sim.field.grid.numeric.DIntGrid2D;
import it.isislab.dmason.sim.field.grid.numeric.DIntGrid2DFactory;
import it.isislab.dmason.sim.field.grid.sparse.DSparseGrid2D;
import it.isislab.dmason.sim.field.grid.sparse.DSparseGrid2DFactory;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;
import sim.util.Interval;

/**
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 *
 */
public /*strictfp*/ class DAntsForage extends DistributedState<Int2D>
{

	public int GRID_HEIGHT;
	public int GRID_WIDTH;

	private static final int HOME_XMIN = 80;
	private static final int HOME_XMAX = 80;
	private static final int HOME_YMIN = 78;
	private static final int HOME_YMAX = 78;
	private int HXMIN;
	private int HYMIN;
	private int HXMAX;
	private int HYMAX;

	private static final int FOOD_XMIN = 22;
	private static final int FOOD_XMAX = 22;
	private static final int FOOD_YMIN = 20;
	private static final int FOOD_YMAX = 20;
	private int FXMIN;
	private int FYMIN;
	private int FXMAX;
	private	int FYMAX;

	public static final int NO_OBSTACLES = 0;
	public static final int ONE_OBSTACLE = 1;
	public static final int TWO_OBSTACLES = 2;
	public static final int ONE_LONG_OBSTACLE = 3;

	public static final int OBSTACLES = TWO_OBSTACLES;

	public static final int ALGORITHM_VALUE_ITERATION = 1;
	public static final int ALGORITHM_TEMPORAL_DIFERENCE = 2;
	public static final int ALGORITHM = ALGORITHM_VALUE_ITERATION;

	public static final double IMPOSSIBLY_BAD_PHEROMONE = -1;
	public static final double LIKELY_MAX_PHEROMONE = 3;

	public static final int HOME = 1;
	public static final int FOOD = 2;


	public int numAnts;// = 1000;
	public double evaporationConstant = 0.999;
	public double reward = 1.0;
	public double updateCutDown = 0.9;
	public double diagonalCutDown = computeDiagonalCutDown();
	public double computeDiagonalCutDown() { return Math.pow(updateCutDown, Math.sqrt(2)); }
	public double momentumProbability = 0.8;
	public double randomActionProbability = 0.1;

	public static String topicPrefix = "";

	// some properties
	public int getNumAnts() { return numAnts; }
	public void setNumAnts(int val) {if (val > 0) numAnts = val; }

	public double getEvaporationConstant() { return evaporationConstant; }
	public void setEvaporationConstant(double val) {if (val >= 0 && val <= 1.0) evaporationConstant = val; }

	public double getReward() { return reward; }
	public void setReward(double val) {if (val >= 0) reward = val; }

	public double getCutDown() { return updateCutDown; }
	public void setCutDown(double val) {if (val >= 0 && val <= 1.0) updateCutDown = val;  diagonalCutDown = computeDiagonalCutDown(); }
	public Object domCutDown() { return new Interval(0.0, 1.0); }

	public double getMomentumProbability() { return momentumProbability; }
	public void setMomentumProbability(double val) {if (val >= 0 && val <= 1.0) momentumProbability = val; }
	public Object domMomentumProbability() { return new Interval(0.0, 1.0); }

	public double getRandomActionProbability() { return randomActionProbability; }
	public void setRandomActionProbability(double val) {if (val >= 0 && val <= 1.0) randomActionProbability = val; }
	public Object domRandomActionProbability() { return new Interval(0.0, 1.0); }


	public DIntGrid2D sites;
	public DDoubleGrid2D toFoodGrid;
	public DDoubleGrid2D toHomeGrid;
	public DSparseGrid2D buggrid;
	public DIntGrid2D obstacles;

	// for the trigger
	public boolean foodFounded = false;
	public boolean backHome = false;

	public int MODE;



	/* public DAntsForage(Object[] params)
        { 
    	super((Integer)params[2],(Integer)params[3],(Integer)params[4],(Integer)params[7],
    			(Integer)params[8],(String)params[0],(String)params[1],(Integer)params[9], 
    			isToroidal,new DistributedMultiSchedule<Int2D>());
    	numAnts = (Integer)params[4];
    	ip = params[0]+"";
    	port = params[1]+"";

    	//trigger = new Trigger(ip, port);

    	this.MODE=(Integer)params[9];
    	GRID_WIDTH = (Integer)params[5];
    	GRID_HEIGHT = (Integer)params[6];

    	FXMIN = (FOOD_XMIN * GRID_WIDTH)/100;
    	FYMIN = (FOOD_YMIN * GRID_HEIGHT)/100;
    	FXMAX = (FOOD_XMAX * GRID_WIDTH)/100;
    	FYMAX = (FOOD_YMAX * GRID_HEIGHT)/100;

    	HXMIN = (HOME_XMIN * GRID_WIDTH)/100;
    	HYMIN = (HOME_YMIN * GRID_HEIGHT)/100;
    	HXMAX = (HOME_XMAX * GRID_WIDTH)/100;
    	HYMAX = (HOME_YMAX * GRID_HEIGHT)/100;

        }*/


	public DAntsForage(){
		super();
	}

	public DAntsForage(GeneralParam params)
	{ 
		super(params,new DistributedMultiSchedule<Int2D>(),topicPrefix,params.getConnectionType());
		this.MODE=params.getMode();
		GRID_WIDTH=params.getWidth();
		GRID_HEIGHT=params.getHeight();

		numAnts = params.getNumAgents();


		FXMIN = (FOOD_XMIN * GRID_WIDTH)/100;
		FYMIN = (FOOD_YMIN * GRID_HEIGHT)/100;
		FXMAX = (FOOD_XMAX * GRID_WIDTH)/100;
		FYMAX = (FOOD_YMAX * GRID_HEIGHT)/100;

		HXMIN = (HOME_XMIN * GRID_WIDTH)/100;
		HYMIN = (HOME_YMIN * GRID_HEIGHT)/100;
		HXMAX = (HOME_XMAX * GRID_WIDTH)/100;
		HYMAX = (HOME_YMAX * GRID_HEIGHT)/100;



	}  
	@Override
	public void start()
	{
		super.start();  // clear out the schedule

		try 
		{       	
			toFoodGrid = DDoubleGrid2DFactory.createDDoubleGrid2D(GRID_WIDTH, GRID_HEIGHT, this, super.MAX_DISTANCE, TYPE.pos_i, TYPE.pos_j, super.rows,super.columns, MODE, 0, false, "toFoodGrid", topicPrefix,false);
			toHomeGrid = DDoubleGrid2DFactory.createDDoubleGrid2D(GRID_WIDTH, GRID_HEIGHT, this, super.MAX_DISTANCE, TYPE.pos_i, TYPE.pos_j, super.rows,super.columns, MODE, 0, false, "toHomeGrid", topicPrefix,false);
			buggrid = DSparseGrid2DFactory.createDSparseGrid2D(GRID_WIDTH, GRID_HEIGHT,this,super.MAX_DISTANCE,TYPE.pos_i,TYPE.pos_j,super.rows,super.columns,MODE, "buggrid", topicPrefix,false);
			sites = DIntGrid2DFactory.createDIntGrid2D(GRID_WIDTH, GRID_HEIGHT, this, super.MAX_DISTANCE, TYPE.pos_i, TYPE.pos_j, super.rows,super.columns, MODE, 0, true, "sites", topicPrefix,false);
			obstacles = DIntGrid2DFactory.createDIntGrid2D(GRID_WIDTH, GRID_HEIGHT, this, super.MAX_DISTANCE, TYPE.pos_i, TYPE.pos_j, super.rows,super.columns, MODE, 0, true, "obstacles", topicPrefix,false);
			init_connection();
		}catch (DMasonException e) { e.printStackTrace();}



		int x1 = (45 * GRID_WIDTH)/100;
		int y1 = (25 * GRID_HEIGHT)/100;
		int x2 = (35 * GRID_WIDTH)/100;
		int y2 = (70 * GRID_HEIGHT)/100;
		int a = (36 * GRID_WIDTH)/100;
		int b = (1024 * GRID_WIDTH)/100;

		switch( OBSTACLES )
		{
		case NO_OBSTACLES:
			break;
		case ONE_OBSTACLE:

			x1 = (55 * GRID_WIDTH)/100;
			y1 = (35 * GRID_HEIGHT)/100;

			a = (36 * GRID_WIDTH)/100;
			b = (1024 * GRID_WIDTH)/100;

			for( int x = 0 ; x < GRID_WIDTH ; x++ )
				for( int y = 0 ; y < GRID_HEIGHT ; y++ )
				{
					obstacles.field[x][y] = 0;
					if( ((x-x1)*0.707+(y-y1)*0.707)*((x-x1)*0.707+(y-y1)*0.707)/a+
							((x-x1)*0.707-(y-y1)*0.707)*((x-x1)*0.707-(y-y1)*0.707)/b <= 1 )	
						obstacles.field[x][y] = 1;
				}

			break;
		case TWO_OBSTACLES:

			x1 = (45 * GRID_WIDTH)/100;
			y1 = (25 * GRID_HEIGHT)/100;
			x2 = (35 * GRID_WIDTH)/100;
			y2 = (70 * GRID_HEIGHT)/100;
			a = (36 * GRID_WIDTH)/100;
			b = (1024 * GRID_WIDTH)/100;

			for( int x = 0 ; x < GRID_WIDTH ; x++ )
				for( int y = 0 ; y < GRID_HEIGHT ; y++ )
				{
					obstacles.field[x][y] = 0;
					if( ((x-x1)*0.707+(y-y1)*0.707)*((x-x1)*0.707+(y-y1)*0.707)/a+
							((x-x1)*0.707-(y-y1)*0.707)*((x-x1)*0.707-(y-y1)*0.707)/b <= 1 )

						obstacles.field[x][y] = 1;

					if( ((x-x2)*0.707+(y-y2)*0.707)*((x-x2)*0.707+(y-y2)*0.707)/a+
							((x-x2)*0.707-(y-y2)*0.707)*((x-x2)*0.707-(y-y2)*0.707)/b <= 1 )

						obstacles.field[x][y] = 1;
				}
			break;
		case ONE_LONG_OBSTACLE:

			x1 = ((60 * GRID_WIDTH)/100);
			y1 = (50 * GRID_HEIGHT)/100;

			a = (1600 * GRID_WIDTH)/100;
			b = (25 * GRID_WIDTH)/100;

			for( int x = 0 ; x < GRID_WIDTH ; x++ )
				for( int y = 0 ; y < GRID_HEIGHT ; y++ )
				{
					obstacles.field[x][y] = 0;
					if( (x-x1)*(x-x1)/a+
							(y-y1)*(y-y1)/b <= 1 )
						obstacles.field[x][y] = 1;
				}
			break;
		}


		// initialize the grid with the home and food sites
		for( int x = HXMIN ; x <= HXMAX ; x++ ){

			for( int y = HYMIN ; y <= HYMAX ; y++ ){
				sites.field[x][y] = HOME;
			}
		}
		for( int x = FXMIN ; x <= FXMAX ; x++ )
			for( int y = FYMIN ; y <= FYMAX ; y++ )
				sites.field[x][y] = FOOD;

		Int2D h = new Int2D((HXMAX+HXMIN)/2,(HYMAX+HYMIN)/2);


		if((sites.own_x<= h.x) && (h.x<(sites.own_x+sites.my_width)) && (sites.own_y<=h.y)
				&& (h.y<(sites.own_y+sites.my_height))){

			DRemoteAnt ant = new DRemoteAnt(this, reward);

			while(buggrid.size() != super.NUMAGENTS){

				ant.setPos(h);

				if(buggrid.setObjectLocation(ant, new Int2D(h.getX(), h.getY()))){

					schedule.scheduleOnce(schedule.getTime()+1.0, ant);
					//schedule.scheduleOnce((Schedule.EPOCH ), 0, ant);

					if(buggrid.size() != super.NUMAGENTS){

						ant = new DRemoteAnt(this, reward);
					}
				}
			}

		}

		// Schedule evaporation to happen after the ants move and update
		schedule.scheduleRepeating(Schedule.EPOCH,1, new Steppable()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void step(SimState state) { toFoodGrid.multiply(evaporationConstant); toHomeGrid.multiply(evaporationConstant); }
		}, 1);

		try {
			if(getTrigger()!=null)
				getTrigger().publishToTriggerTopic("Simulation cell "+buggrid.cellType+" ready...");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	static final long serialVersionUID = 9115981605874680023L;

	public static void main(String[] args)
	{
		doLoop(DAntsForage.class, args);
		System.exit(0);
	}
	@Override
	public DistributedField2D<Int2D> getField() {
		// TODO Auto-generated method stub
		return buggrid;
	}
	@Override
	public SimState getState() {
		// TODO Auto-generated method stub
		return this;
	}
	
	@Override
	public void addToField(RemotePositionedAgent<Int2D> rm, Int2D loc) {
		// TODO Auto-generated method stub
		buggrid.setObjectLocation(rm, loc);
	}

}





