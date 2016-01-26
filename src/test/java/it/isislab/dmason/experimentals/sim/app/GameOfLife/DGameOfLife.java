package it.isislab.dmason.experimentals.sim.app.GameOfLife;

import java.awt.Color;
import java.util.List;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.experimentals.tools.batch.data.EntryParam;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.sim.app.DFlockers.DFlocker;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.grid.numeric.DIntGrid2D;
import it.isislab.dmason.sim.field.grid.numeric.DIntGrid2DFactory;
import sim.app.tutorial1and2.Tutorial1;
import sim.engine.SimState;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.util.Double2D;
import sim.util.Int2D;

public class DGameOfLife extends DistributedState<Int2D> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DIntGrid2D grid = null;
	
	protected FastValueGridPortrayal2D p;

	private String topicPrefix = "";
	
	/**
	 * field Width
	 */
	public int gridWidth ;
	/**
	 * field Height
	 */
	public int gridHeight ;
	
	public int MODE;
		
	public static final int[][] b_heptomino = new int[][]
		    {{0, 1, 1},
		         {1, 1, 0},
		         {0, 1, 1},
		         {0, 0, 1}};
	
	/**
	 * empty costructor for Serialize
	 */
	public DGameOfLife() { super();}
	
	
	/**
	 * Constructor 
	 * @param params
	 */
	public DGameOfLife(GeneralParam params, String prefix) {
		super(params, new DistributedMultiSchedule<Int2D>(), prefix, params.getConnectionType());
		this.MODE=params.getMode();
		gridWidth=params.getWidth();
		gridHeight=params.getHeight();
	}

	public DGameOfLife(GeneralParam params,List<EntryParam<String, Object>> simParams, String prefix)
	{
		super(params,new DistributedMultiSchedule<Int2D>(), prefix,params.getConnectionType());
		this.topicPrefix = prefix; 
		this.MODE=params.getMode();
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
	
	void seedGrid()
	{
		// we stick a b_heptomino in the center of the grid
		for(int x=0;x<b_heptomino.length;x++)
			for(int y=0;y<b_heptomino[x].length;y++)
				grid.field  [grid.own_x+x][grid.own_y+y]=	b_heptomino[x][y];
	}
	
	
	@Override
	public void start()
	{
		super.start();
	
		try 
		{
			grid = DIntGrid2DFactory.createDIntGrid2D(gridWidth, gridHeight, this, super.AOI, TYPE.pos_i,TYPE.pos_j, super.rows,super.columns,MODE, 0, false, "gameoflife", topicPrefix, true);
			init_connection();
		} catch (DMasonException e) { e.printStackTrace(); }
		
		seedGrid();
        schedule.scheduleOnce(new DCellAgent(this,grid.getAvailableRandomLocation()));		
	}
	@Override
	public DistributedField2D getField() {
		// TODO Auto-generated method stub
		return grid;
	}

	@Override
	public void addToField(RemotePositionedAgent rm, Int2D loc) {System.err.println("wuamn");}

	@Override
	public SimState getState() {
		// TODO Auto-generated method stub
		return this;
	}
		
	public static void main(String[] args)
	{
		doLoop(DGameOfLife.class, args);
		System.exit(0);
	}
}
