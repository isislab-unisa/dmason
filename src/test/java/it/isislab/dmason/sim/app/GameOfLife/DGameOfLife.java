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
package it.isislab.dmason.sim.app.GameOfLife;

import java.util.List;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.experimentals.tools.batch.data.EntryParam;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.grid.numeric.DIntGrid2D;
import it.isislab.dmason.sim.field.grid.numeric.DIntGrid2DFactory;
import it.isislab.dmason.sim.field.grid.sparse.DSparseGrid2D;
import it.isislab.dmason.sim.field.grid.sparse.DSparseGrid2DFactory;
import sim.engine.SimState;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.util.Int2D;

public class DGameOfLife extends DistributedState<Int2D> {

	private static final long serialVersionUID = 1L;

	public DIntGrid2D grid = null;
	public DSparseGrid2D core = null;
	protected FastValueGridPortrayal2D p;

	private String topicPrefix = "";

	/**
	 * field Width
	 */
	public int gridWidth;
	/**
	 * field Height
	 */
	public int gridHeight;

	public int MODE;

	public static final int[][] b_heptomino = new int[][] { 
		{ 0, 0, 0, 0, 0 }, 
		{ 0, 0, 1, 0, 0 }, 
		{ 0, 0, 0, 1, 0 },
		{ 0, 1, 1, 1, 0 }, 
		{ 0, 0, 0, 0, 0 } };

	/**
	 * empty costructor for Serialize
	 */
	public DGameOfLife() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param params
	 */
	public DGameOfLife(GeneralParam params, String prefix) {
		super(params, new DistributedMultiSchedule<Int2D>(), prefix, params.getConnectionType());
		this.MODE = params.getMode();
		gridWidth = params.getWidth();
		gridHeight = params.getHeight();
		this.topicPrefix = prefix;
	}

	public DGameOfLife(GeneralParam params, List<EntryParam<String, Object>> simParams, String prefix) {
		super(params, new DistributedMultiSchedule<Int2D>(), prefix, params.getConnectionType());
		this.topicPrefix = prefix;
		this.MODE = params.getMode();
		gridWidth = params.getWidth();
		gridHeight = params.getHeight();

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

	void seedGrid() {		
		//min density
		if(grid.own_x==0 && grid.own_y==0){
			Int2D loc = new Int2D(0, 0);
			for (int x = 0; x < b_heptomino.length; x++)
				for (int y = 0; y < b_heptomino[x].length; y++)
					grid.field[loc.x + x][loc.y + y] = b_heptomino[x][y];
		}
			
		//max density
//		for (int i = 0; i < grid.getHeight()/5; i++) {
//			for (int j = 0; j < grid.getWidth()/5; j++) {		
//				Int2D loc = new Int2D(5*i,5*j);
//				for (int x = 0; x < b_heptomino.length; x++)
//					for (int y = 0; y < b_heptomino[x].length; y++)
//						grid.field[loc.x + x][loc.y + y] = b_heptomino[x][y];
//			}
//		}
	}

	@Override
	public void start() {
		super.start();
		System.out.println("Start Inizio");

		try {
			grid = DIntGrid2DFactory.createDIntGrid2D(gridWidth, gridHeight, this, super.AOI, TYPE.pos_i, TYPE.pos_j,
					super.rows, super.columns, MODE, 0, false, "gameoflife", topicPrefix, true);
			core = DSparseGrid2DFactory.createDSparseGrid2D(gridWidth, gridHeight, this, super.AOI, TYPE.pos_i,
					TYPE.pos_j, super.rows, super.columns, MODE, "gameoflifeCore", topicPrefix, true);
			init_connection();
		} catch (DMasonException e) {
			e.printStackTrace();
		}

		seedGrid();
//		DCellAgent a = new DCellAgent(this, core.getAvailableRandomLocation());
//		if (core.setObjectLocation(a, a.pos))
//			schedule.scheduleOnce(a);
		
		
		DCellAgent a=new DCellAgent(this, new Int2D(0,0));
		a.setPos(core.getAvailableRandomLocation());
		
		if(core.setObjectLocation(a, a.pos)) {
			schedule.scheduleOnce(a);
		}
		
		
	}

	@Override
	public DistributedField2D getField() {
		return grid;
	}
	

	@Override
	public void addToField(RemotePositionedAgent rm, Int2D loc){
		core.setObjectLocation(rm, loc);
	}

	@Override
	public SimState getState() {
		// TODO Auto-generated method stub
		return this;
	}

	public static void main(String[] args) {
		doLoop(DGameOfLife.class, args);
		System.exit(0);
	}
}
