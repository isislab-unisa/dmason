/**
 * Copyright 2012 Università degli Studi di Salerno


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

package dmason.sim.field.continuous;

import java.util.ArrayList;
import java.util.HashMap;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.MutableInt2D;
import dmason.sim.field.CellType;
import dmason.sim.field.DistributedField;
import dmason.sim.field.Region;
import dmason.sim.field.RegionMap;
import dmason.sim.field.UpdateMap;
import dmason.sim.field.network.DNetwork;
import dmason.sim.loadbalancing.MyCellInterface;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;
import dmason.util.connection.ConnectionWithJMS;
import dmason.util.connection.ProxyConnection;
import dmason.util.visualization.VisualizationUpdateMap;

/**
 *  Abstract class for Distributed Continuous 2D
 *  
 *  <h3>This Field extends Continuous2D, to be used in a distributed environment. 
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
 */
public abstract class DContinuous2D extends Continuous2D  implements DistributedField<Double2D>
{
	/**
	 * Stores the coordinates of this distributed field relative to the
	 * global field. For instance, assume a global field sized 200x100 units
	 * (width * height). If we split the global field in two distributed field,
	 * we'll obtain a field with <code>cellType</code> 0-0 and a field with
	 * <code>cellType</code> 0-1, each sized 100x100 units. Please note that
	 * a <code>cellType</code> i-j means that the cell is at coordinates (i, j),
	 * where <i>i</i> is the row number, <i>j</i> is the column number.
	 */
	public CellType cellType;
	
	/**
	 * The number of peers involved in the simulation.
	 */
	//public int numPeers;
	
	public int rows;

	public int columns;
	
	/**
	 * x coordinate of north-west corner.
	 */
	public double own_x;

	/**
	 * y coordinate of north-west corner.
	 */
	public double own_y;
	
	/**
	 * This field's width.
	 */
	public double my_width;
	
	/**
	 * This field's height.
	 */
	public double my_height;
	
	
	public int MAX_DISTANCE;
	
	/** Will contain globals parameters */
	public VisualizationUpdateMap<String, Object> globals = new VisualizationUpdateMap<String, Object>();

	public RegionDouble myfield;
	public RegionMap<Double,Double2D> rmap=new RegionMap<Double,Double2D>();
	public ArrayList<Region<Double, Double2D>> updates_cache;
	public int jumpDistance;
	public UpdateMap<Double,Double2D> updates=new UpdateMap<Double,Double2D>();
	public HashMap<Integer,HashMap<CellType, MyCellInterface>> listGrid;
	public ArrayList<ArrayList<Region<Double, Double2D>>> updates_cacheLB;
	public SimState sm;
    public ConnectionWithJMS connection=new ConnectionNFieldsWithActiveMQAPI();
	public ArrayList<String> neighborhood=new ArrayList<String>();
	public ContinuousPortrayal2D p;
	public boolean gui=true;
	
	//network
	public DNetwork network=null;
	
	// <-- instance variables
		
	public DContinuous2D(double discretization, double width, double height) 
	{
		super(discretization, width, height);
	}
	
	public  void attachPortrayal(ContinuousPortrayal2D p){this.p=p;}
	public  ContinuousPortrayal2D getAttachedPortrayal(){return p;}
	public abstract boolean setPortrayalForObject(Object o);
	
}