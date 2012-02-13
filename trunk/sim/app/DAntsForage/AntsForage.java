package dmason.sim.app.DAntsForage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.DoubleGrid2D;
import sim.util.Int2D;
import sim.util.Interval;
import dmason.sim.engine.DistributedScheduleMulti;
import dmason.sim.engine.DistributedState;

import dmason.sim.engine.RemoteAgent;
import dmason.sim.field.DistributedField;
import dmason.sim.field.grid.DSparseGrid2D;
import dmason.sim.field.grid.DSparseGrid2DFactory;
import dmason.sim.field.grid.numeric.DDoubleGrid2D;
import dmason.sim.field.grid.numeric.DDoubleGrid2DFactory;
import dmason.sim.field.grid.numeric.DIntGrid2D;
import dmason.sim.field.grid.numeric.DIntGrid2DFactory;
import dmason.util.exception.DMasonException;

public /*strictfp*/ class AntsForage extends DistributedState<Int2D>
    {
    public int GRID_HEIGHT;
    public int GRID_WIDTH;

    public static final int HOME_XMIN = 150;//75;
    public static final int HOME_XMAX = 150;//75;
    public static final int HOME_YMIN = 150;//75;
    public static final int HOME_YMAX = 150;//75;

    public static final int FOOD_XMIN = 50;//25;
    public static final int FOOD_XMAX = 50;//25;
    public static final int FOOD_YMIN = 50;//25;
    public static final int FOOD_YMAX = 50;//25;

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

    public int MODE;
    
    public AntsForage(Object[] params)
        { 
    	super((Integer)params[2],(Integer)params[3],(Integer)params[4],(Integer)params[7],(Integer)params[8], new DistributedScheduleMulti<Int2D>());
    	numAnts = (Integer)params[4];
    	ip = params[0]+"";
    	port = params[1]+"";
    	this.MODE=(Integer)params[9];
    	GRID_WIDTH = (Integer)params[5];
    	GRID_HEIGHT = (Integer)params[6];

        }
        
    public void start()
        {
        super.start();  // clear out the schedule

        try 
        {
        	buggrid = DSparseGrid2DFactory.createDSparseGrid2d(GRID_WIDTH, GRID_HEIGHT,this,super.MAX_DISTANCE,TYPE.pos_i,TYPE.pos_j,super.NUMPEERS,MODE);
        	sites = DIntGrid2DFactory.createDIntGrid2D(GRID_WIDTH, GRID_HEIGHT, this, super.MAX_DISTANCE, TYPE.pos_i, TYPE.pos_j, super.NUMPEERS, MODE, "sites", 0, true);
        	obstacles = DIntGrid2DFactory.createDIntGrid2D(GRID_WIDTH, GRID_HEIGHT, this, super.MAX_DISTANCE, TYPE.pos_i, TYPE.pos_j, super.NUMPEERS, MODE, "obstacles", 0, true);
        	toFoodGrid = DDoubleGrid2DFactory.createDDoubleGrid2D(GRID_WIDTH, GRID_HEIGHT, this, super.MAX_DISTANCE, TYPE.pos_i, TYPE.pos_j, super.NUMPEERS, MODE, "tofoodgrid", 0, false);
            toHomeGrid = DDoubleGrid2DFactory.createDDoubleGrid2D(GRID_WIDTH, GRID_HEIGHT, this, super.MAX_DISTANCE, TYPE.pos_i, TYPE.pos_j, super.NUMPEERS, MODE, "tohomegrid", 0, false);
        }catch (DMasonException e) { e.printStackTrace();}
		
        // make new grids
        //valgrid2 = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT, 0);

        switch( OBSTACLES )
            {
            case NO_OBSTACLES:
                break;
            case ONE_OBSTACLE:
                for( int x = 0 ; x < GRID_WIDTH ; x++ )
                    for( int y = 0 ; y < GRID_HEIGHT ; y++ )
                    {
                    	if(obstacles.rmap.left_mine!=null)
                    	if(obstacles.myfield.isMine(x, y) || 
                    			obstacles.rmap.left_mine.isMine(x, y) ){
                    		obstacles.field[x][y] = 0;
                    	
                    		if( ((x-55)*0.707+(y-35)*0.707)*((x-55)*0.707+(y-35)*0.707)/36+
                    				((x-55)*0.707-(y-35)*0.707)*((x-55)*0.707-(y-35)*0.707)/1024 <= 1 )	
                        			obstacles.field[x][y] = 1;
                    	}
                    	if(obstacles.rmap.right_mine!=null)
                    		if(obstacles.myfield.isMine(x, y) || 
                        			obstacles.rmap.right_mine.isMine(x, y)){
                        		obstacles.field[x][y] = 0;
                        	
                        		if( ((x-55)*0.707+(y-35)*0.707)*((x-55)*0.707+(y-35)*0.707)/36+
                        				((x-55)*0.707-(y-35)*0.707)*((x-55)*0.707-(y-35)*0.707)/1024 <= 1 )	
                            			obstacles.field[x][y] = 1;
                        	}
                    }
                break;
            case TWO_OBSTACLES:
                for( int x = 0 ; x < GRID_WIDTH ; x++ )
                    for( int y = 0 ; y < GRID_HEIGHT ; y++ )
                    {
                    	//if(obstacles.rmap.right_mine!=null && obstacles.rmap.right_out!=null)
                    	//if(obstacles.myfield.isMine(x, y) ||  
                    		//	obstacles.rmap.right_mine.isMine(x, y) 
                    			//	|| obstacles.rmap.right_out.isMine(x, y)){
                    	
                    	if((obstacles.own_x<= x) && (x<(obstacles.own_x+obstacles.my_width)) && (obstacles.own_y<=y)
                    			&& (y<(obstacles.own_y+obstacles.my_height))){
                    		obstacles.field[x][y] = 0;
                    		//if( ((x-45)*0.707+(y-25)*0.707)*((x-45)*0.707+(y-25)*0.707)/36+
                    			//	((x-45)*0.707-(y-25)*0.707)*((x-45)*0.707-(y-25)*0.707)/1024 <= 1 )
                    		if( ((x-90)*0.707+(y-50)*0.707)*((x-90)*0.707+(y-50)*0.707)/72+
                				((x-90)*0.707-(y-50)*0.707)*((x-90)*0.707-(y-50)*0.707)/2048 <= 1 )
                        
                            		obstacles.field[x][y] = 1;
                            
                    		//if( ((x-35)*0.707+(y-70)*0.707)*((x-35)*0.707+(y-70)*0.707)/36+
                    			//	((x-35)*0.707-(y-70)*0.707)*((x-35)*0.707-(y-70)*0.707)/1024 <= 1 )
                    		if( ((x-70)*0.707+(y-140)*0.707)*((x-70)*0.707+(y-140)*0.707)/72+
                    				((x-70)*0.707-(y-140)*0.707)*((x-70)*0.707-(y-140)*0.707)/2048 <= 1 )
                            
                    				obstacles.field[x][y] = 1;
                    	}
                    	/**
                    	if(obstacles.rmap.left_mine!=null && obstacles.rmap.left_out!=null)
                        	if(obstacles.myfield.isMine(x, y) ||  
                        			obstacles.rmap.left_mine.isMine(x, y)
                        			|| obstacles.rmap.left_out.isMine(x, y)){
                        		obstacles.field[x][y] = 0;
                        		//if( ((x-45)*0.707+(y-25)*0.707)*((x-45)*0.707+(y-25)*0.707)/36+
                        			//	((x-45)*0.707-(y-25)*0.707)*((x-45)*0.707-(y-25)*0.707)/1024 <= 1 )
                        		if( ((x-90)*0.707+(y-50)*0.707)*((x-90)*0.707+(y-50)*0.707)/72+
                        				((x-90)*0.707-(y-50)*0.707)*((x-90)*0.707-(y-50)*0.707)/2048 <= 1 )
                                
                                		obstacles.field[x][y] = 1;
                                
                        		//if( ((x-35)*0.707+(y-70)*0.707)*((x-35)*0.707+(y-70)*0.707)/36+
                        		//		((x-35)*0.707-(y-70)*0.707)*((x-35)*0.707-(y-70)*0.707)/1024 <= 1 )
                        		if( ((x-70)*0.707+(y-140)*0.707)*((x-70)*0.707+(y-140)*0.707)/72+
                        				((x-70)*0.707-(y-140)*0.707)*((x-70)*0.707-(y-140)*0.707)/2048 <= 1 )
                                
                        				obstacles.field[x][y] = 1;
                        	}*/
                    }
                break;
            case ONE_LONG_OBSTACLE:
                for( int x = 0 ; x < GRID_WIDTH ; x++ )
                    for( int y = 0 ; y < GRID_HEIGHT ; y++ )
                    {
                    	if(obstacles.rmap.right_mine!=null && obstacles.rmap.right_out!=null) {
                    		
                    		if(obstacles.myfield.isMine(x, y) || 
                    				obstacles.rmap.left_mine.isMine(x, y) || 
                    				obstacles.rmap.right_mine.isMine(x, y) ||
                    				obstacles.rmap.right_out.isMine(x, y)){
                    			obstacles.field[x][y] = 0;
                    			if( (x-60)*(x-60)/1600+(y-50)*(y-50)/25 <= 1 )
                    				obstacles.field[x][y] = 1;
                    		}
                    	}
                    	
                    	if(obstacles.rmap.left_mine!=null && obstacles.rmap.left_out!=null) {
                    		
                    		if(obstacles.myfield.isMine(x, y) || 
                    				obstacles.rmap.left_mine.isMine(x, y) || 
                    				obstacles.rmap.right_mine.isMine(x, y) ||
                    				obstacles.rmap.left_out.isMine(x, y)){
                    			obstacles.field[x][y] = 0;
                    			if( (x-60)*(x-60)/1600+(y-50)*(y-50)/25 <= 1 )
                    				obstacles.field[x][y] = 1;
                    		}
                    	}
                    }
                break;
            }

        
        // initialize the grid with the home and food sites
        for( int x = HOME_XMIN ; x <= HOME_XMAX ; x++ )
            for( int y = HOME_YMIN ; y <= HOME_YMAX ; y++ )
            	if((sites.own_x<= x) && (x<(sites.own_x+sites.my_width)) && (sites.own_y<=y)
            			&& (y<(sites.own_y+sites.my_height))){
            	
            		sites.field[x][y] = HOME;
            	}
        for( int x = FOOD_XMIN ; x <= FOOD_XMAX ; x++ )
            for( int y = FOOD_YMIN ; y <= FOOD_YMAX ; y++ )
            	if((sites.own_x<= x) && (x<(sites.own_x+sites.my_width)) && (sites.own_y<=y)
            			&& (y<(sites.own_y+sites.my_height))){
            	
            		sites.field[x][y] = FOOD;
            	}
        
        Int2D h = new Int2D((HOME_XMAX+HOME_XMIN)/2,(HOME_YMAX+HOME_YMIN)/2);
      
        if((sites.own_x<= h.x) && (h.x<(sites.own_x+sites.my_width)) && (sites.own_y<=h.y)
    			&& (h.y<(sites.own_y+sites.my_height))){
        
        	RemoteAnt ant = new RemoteAnt(this, reward);
        
        	while(buggrid.size() != super.NUMAGENTS){
        	
        		ant.setPos(h);
        	
        		if(buggrid.setObjectLocation(ant, new Int2D(h.getX(), h.getY()))){
        
        			//schedule.scheduleOnce(schedule.getTime()+1.0, ant);
        			schedule.scheduleOnce((Schedule.EPOCH + buggrid.size()), 0, ant);
        			
        			if(buggrid.size() != super.NUMAGENTS){
        			
        				ant = new RemoteAnt(this, reward);
        			}
        		}
        	}
        	
        }

        // Schedule evaporation to happen after the ants move and update
        	schedule.scheduleRepeating(Schedule.EPOCH,1, new Steppable()
           	{
            public void step(SimState state) { toFoodGrid.multiply(evaporationConstant); toHomeGrid.multiply(evaporationConstant); }
            }, 1);
        	
        	((DistributedScheduleMulti) (this.schedule)).addField(buggrid);
        	((DistributedScheduleMulti) (this.schedule)).addField(toFoodGrid);
        	((DistributedScheduleMulti) (this.schedule)).addField(toHomeGrid);
        	
        }
    static final long serialVersionUID = 9115981605874680023L;
    
    public static void main(String[] args)
        {
        doLoop(AntsForage.class, args);
        System.exit(0);
        }
	@Override
	public DistributedField getField() {
		// TODO Auto-generated method stub
		return buggrid;
	}
	@Override
	public SimState getState() {
		// TODO Auto-generated method stub
		return this;
	}
	@Override
	public boolean setPortrayalForObject(Object o) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void addToField(RemoteAgent<Int2D> rm, Int2D loc) {
		// TODO Auto-generated method stub
		buggrid.setObjectLocation(rm, loc);
	}    
    }
    
    
    
    
    
