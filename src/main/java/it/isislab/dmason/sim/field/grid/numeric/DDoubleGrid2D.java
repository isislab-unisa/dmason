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

package it.isislab.dmason.sim.field.grid.numeric;

import it.isislab.dmason.experimentals.sim.field.support.loadbalancing.MyCellInterface;
import it.isislab.dmason.experimentals.util.visualization.globalviewer.VisualizationUpdateMap;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.grid.numeric.region.RegionDoubleNumeric;
import it.isislab.dmason.sim.field.support.field2D.EntryNum;
import it.isislab.dmason.sim.field.support.field2D.UpdateMap;
import it.isislab.dmason.sim.field.support.field2D.region.RegionMapNumeric;
import it.isislab.dmason.sim.field.support.field2D.region.RegionNumeric;

import java.util.ArrayList;
import java.util.HashMap;

import sim.engine.SimState;
import sim.field.grid.DoubleGrid2D;
import sim.util.Int2D;


/**
 *  Abstract class for Distributed Double Grid 2D
 *
 *  <h3>This Field extends DoubleGrid2D, to be used in a distributed environment. 
 *  All the necessary informations for the distribution of simulation are wrapped in this class.</h3>
 *  It represents the field managed by a single peer.
 *  It adds to the superclass these following informations:
 *  <ul>
 *  <li> Upper Left corner's coordinates</li>
 *  <li> Width and Height of the Field of expertise</li>
 *  <li> The logic of synchronization among the peers, step by step</li>
 *  <li> The number of peers involved in the simulation</li>
 *  <li> The maximum distance of shift for the agents</li>
 *  <li> An arraylist for the neighborhoods topics</li>
 *  <li> A Region that represents the field to simulate</li>
 *  <li> A RegionMap that represents all the border Regions</li>
 *  <li> The simstate of sumulation</li>
 *  <li> An UpdateMap for all the updates</li>
 *  <li> A Connection object for an abstract connection</li>
 *  <li> A CellType object for differentiate the field</li>
 *  </ul>
 *  @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 */
public abstract class DDoubleGrid2D extends DoubleGrid2D implements DistributedField2D<Int2D>{

	/**
	 * The number of peers that will perform the simulation
	 */
	//public int NUMPEERS;

	public int rows;

	public int columns;
	/**
	 * x coordinate of north-west corner
	 */
	public int own_x;
	/**
	 * y coordinate of north-west corner
	 */
	public int own_y;
	/**
	 * effective width of the peer
	 */
	public int my_width;
	/**
	 * effective height of the peer
	 */
	public int my_height;
	/**
	 * It's the region represents the central field of the peer 
	 */
	public RegionDoubleNumeric myfield;
	/**
	 * It represents all the border Regions of the peer
	 */
	public RegionMapNumeric<Integer,EntryNum<Double,Int2D>> rmap=new RegionMapNumeric<Integer,EntryNum<Double,Int2D>>();
	/**
	 * It contains all the region out of the peer
	 */
	public ArrayList<RegionNumeric<Integer, EntryNum<Double,Int2D>>> updates_cache;
	/**
	 * list of cell into field
	 */
	public HashMap<Integer,HashMap<CellType, MyCellInterface>> listGrid;
	/**
	 * It's the distance used for the updates in the simulation
	 */
	public int AOI;
	/**
	 * It contains all the region out of the peer
	 */
	public ArrayList<ArrayList<RegionNumeric<Integer,EntryNum<Double,Int2D>>>> updates_cacheLB;
	/**
	 * It represents the cell
	 */
	public CellType cellType;
	/**
	 * It contains all the updates from the border regions
	 */
	public UpdateMap<Integer,EntryNum<Double,Int2D>> updates=new UpdateMap<Integer,EntryNum<Double,Int2D>>();
	public SimState sm ;
	/**
	 * It contains the neighborhood of the peer
	 */
	public ArrayList<String> neighborhood=new ArrayList<String>();
	
	public VisualizationUpdateMap<String, Object> globals = new VisualizationUpdateMap<String, Object>();


	public DDoubleGrid2D(int width, int height, double initialGridValue) {

		super(width, height);
	}
	
	private boolean isToroidal;
	
	public boolean isToroidal()
	{
		return isToroidal;
	}
	
	public void setToroidal(boolean isToroidal)
	{
		this.isToroidal=isToroidal;
	}

	/**
	 
	 * MOVED INTO DISTRIBUTEDFIELD2D  
	 * 
	 * Provide the double value shift logic among the peers
	 * @param d the double value we want to set in a specific location
	 * @param l is the location where we want to set the value
	 * @param sm is the simstate
	 * @return true if all will go in the right way, false otherwise
	 
	 
	public abstract boolean setDistributedObjectLocation(double d, Int2D l, SimState sm);
    */
}
