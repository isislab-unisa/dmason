package dmason.util.visualization.DAntsForage;

import sim.engine.SimState;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;
import sim.util.Interval;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;
import dmason.util.visualization.ZoomViewer;

public /*strictfp*/ class AntsForageZoom extends SimState {
	
	public AntsForageZoom(long seed) {
		super(seed);
		// TODO Auto-generated constructor stub
	}
	
	public ConnectionNFieldsWithActiveMQAPI con;
	public String id_Cell;
	public int numCell;
	public int mode;
	public int local_ux;
	public int local_uy;
	public int local_dx;
	public int local_dy;
	int wh;
	int ht;
	
	public AntsForageZoom(Object[] args)
	{
		super(1);
		con=(ConnectionNFieldsWithActiveMQAPI)args[0];
		id_Cell=(String)args[1];
		isSynchro=(Boolean)args[2];
		this.numCell = (Integer)args[3];
		this.wh = (Integer)args[4];//width
		this.ht = (Integer)args[5];//height
		this.mode = (Integer)args[6];
		
		GRID_WIDTH = ZoomViewer.getCellWidth(mode, wh, numCell);
		GRID_HEIGHT = ZoomViewer.getCellHeight(mode, ht, numCell);
		
		Int2D locUXY = ZoomViewer.getCellUpLeftCoordinates(mode,id_Cell,numCell,wh, ht);
		Int2D locDXY = ZoomViewer.getCellDownRightCoordinates(mode,id_Cell,numCell,wh, ht);
		this.local_ux = locUXY.getX();
		this.local_uy = locUXY.getY();
		this.local_dx = locDXY.getX();
		this.local_dy = locDXY.getY();
		
    	FXMIN = (FOOD_XMIN * wh)/100;
    	FYMIN = (FOOD_YMIN * ht)/100;
    	FXMAX = (FOOD_XMAX * wh)/100;
    	FYMAX = (FOOD_YMAX * ht)/100;
    	
    	HXMIN = (HOME_XMIN * wh)/100;
    	HYMIN = (HOME_YMIN * ht)/100;
    	HXMAX = (HOME_XMAX * wh)/100;
    	HYMAX = (HOME_YMAX * ht)/100;
	}
	public boolean isSynchro; 
	public int GRID_HEIGHT;
	public int GRID_WIDTH;

	public static final int HOME_XMIN = 80;
	public static final int HOME_XMAX = 80;
	public static final int HOME_YMIN = 78;
	public static final int HOME_YMAX = 78;
    private int HXMIN;
    private int HYMIN;
    private int HXMAX;
    private int HYMAX;

	public static final int FOOD_XMIN = 22;
	public static final int FOOD_XMAX = 22;
	public static final int FOOD_YMIN = 20;
	public static final int FOOD_YMAX = 20;
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


	public int numAnts = 1000;
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


	public IntGrid2D sites = new IntGrid2D(GRID_WIDTH, GRID_HEIGHT,0);
	public DoubleGrid2D toFoodGrid = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT,0);
	public DoubleGrid2D toHomeGrid = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT,0);
	public SparseGrid2D buggrid = new SparseGrid2D(GRID_WIDTH, GRID_HEIGHT);
	public IntGrid2D obstacles = new IntGrid2D(GRID_WIDTH, GRID_HEIGHT,0);


	public void start()
	{
		super.start();  // clear out the schedule

		// make new grids
		sites = new IntGrid2D(GRID_WIDTH, GRID_HEIGHT,0);
		toFoodGrid = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT,0);
		toHomeGrid = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT,0);
		buggrid = new SparseGrid2D(GRID_WIDTH, GRID_HEIGHT);
		obstacles = new IntGrid2D(GRID_WIDTH, GRID_HEIGHT, 0);
		
		int x1 = (45 * wh)/100;
    	int y1 = (25 * ht)/100;
    	int x2 = (35 * wh)/100;
    	int y2 = (70 * ht)/100;
    	int a = (36 * wh)/100;
    	int b = (1024 * wh)/100;
		
        switch( OBSTACLES )
        {
        case NO_OBSTACLES:
            break;
        case ONE_OBSTACLE:
            
        	x1 = (55 * wh)/100;
        	y1 = (35 * ht)/100;

        	a = (36 * wh)/100;
        	b = (1024 * wh)/100;

            for( int x = 0 ; x < wh ; x++ )
                for( int y = 0 ; y < ht ; y++ )
                {
                	if((x>local_ux) && (y>local_uy) && (x<local_dx) && (y<local_dy)){
	                    obstacles.field[x-local_ux][y-local_uy] = 0;
	            		if( ((x-x1)*0.707+(y-y1)*0.707)*((x-x1)*0.707+(y-y1)*0.707)/a+
	            				((x-x1)*0.707-(y-y1)*0.707)*((x-x1)*0.707-(y-y1)*0.707)/b <= 1 )	
	                			obstacles.field[x-local_ux][y-local_uy] = 1;
                	}
                }
            break;
        case TWO_OBSTACLES:
        	
        	x1 = (45 * wh)/100;
        	y1 = (25 * ht)/100;
        	x2 = (35 * wh)/100;
        	y2 = (70 * ht)/100;
        	a = (36 * wh)/100;
        	b = (1024 * wh)/100;

        	for( int x = 0 ; x < wh ; x++ )
                for( int y = 0 ; y < ht ; y++ )
                {
                	if((x>=local_ux) && (y>=local_uy) && (x<local_dx) && (y<local_dy)){
	                    obstacles.field[x-local_ux][y-local_uy] = 0;
	            		if( ((x-x1)*0.707+(y-y1)*0.707)*((x-x1)*0.707+(y-y1)*0.707)/a+
	                			((x-x1)*0.707-(y-y1)*0.707)*((x-x1)*0.707-(y-y1)*0.707)/b <= 1 )
	                        obstacles.field[x-local_ux][y-local_uy] = 1;
                	
	            		if( ((x-x2)*0.707+(y-y2)*0.707)*((x-x2)*0.707+(y-y2)*0.707)/a+
	            				((x-x2)*0.707-(y-y2)*0.707)*((x-x2)*0.707-(y-y2)*0.707)/b <= 1 )
	            			obstacles.field[x-local_ux][y-local_uy] = 1;
                	}
                }
            break;
        case ONE_LONG_OBSTACLE:
        	
        	x1 = ((60 * wh)/100);
        	y1 = (50 * ht)/100;

        	a = (1600 * wh)/100;
        	b = (25 * wh)/100;

            for( int x = 0 ; x < wh ; x++ )
                for( int y = 0 ; y < ht ; y++ )
                    {
                	if((x>local_ux) && (y>local_uy) && (x<local_dx) && (y<local_dy)){
	                    obstacles.field[x-local_ux][y-local_uy] = 0;
	                    if( (x-x1)*(x-x1)/a+
	                        (y-y1)*(y-y1)/b <= 1 )
	                        obstacles.field[x-local_ux][y-local_uy] = 1;
	                    }
                	}
            break;
        }

    
    // initialize the grid with the home and food sites
        if((HXMIN>=local_ux) && (HYMIN>=local_uy) && (HXMAX<=local_dx) && (HXMAX<=local_dy)){
		    for( int x = HXMIN ; x <= HXMAX ; x++ ){
		    	for( int y = HYMIN ; y <= HYMAX ; y++ ){
		    		if((x>=local_ux) && (y>=local_uy) && (x<=local_dx) && (y<=local_dy))
		        		sites.field[x-local_ux][y-local_uy] = HOME;
		    	}
		    }
        }
        if((FXMIN>=local_ux) && (FYMIN>=local_uy) && (FXMAX<=local_dx) && (FXMAX<=local_dy)){
		    for( int x = FXMIN ; x <= FXMAX ; x++ )
		        for( int y = FYMIN ; y <= FYMAX ; y++ )
		        	if((x>=local_ux) && (y>=local_uy) && (x<=local_dx) && (y<=local_dy))
		        		sites.field[x-local_ux][y-local_uy] = FOOD;
        }
		//View Zoom in Central GUI

		ZoomViewer zoom;
		try {
			
			zoom = new ZoomViewer(con,id_Cell,isSynchro,numCell,GRID_WIDTH,GRID_HEIGHT,mode);
			//in according order
			zoom.registerField("toFoodGrid",toFoodGrid);
			zoom.registerField("toHomeGrid",toFoodGrid);
			zoom.registerField("buggrid",buggrid);

			schedule.scheduleRepeating(new DAntsAgentUpdate(zoom));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	static final long serialVersionUID = 9115981605874680023L;

	public static void main(String[] args)
	{
		doLoop(AntsForageZoom.class, args);
		System.exit(0);
	}
}