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

package it.isislab.dmason.sim.field.grid.sparse;

import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.UpdateCell;
import it.isislab.dmason.sim.field.grid.region.RegionInteger;
import it.isislab.dmason.sim.field.support.field2D.UpdateMap;
import it.isislab.dmason.sim.field.support.field2D.region.Region;
import it.isislab.dmason.sim.field.support.field2D.region.RegionMap;
import it.isislab.dmason.sim.field.support.loadbalancing.MyCellIntegerField;
import it.isislab.dmason.sim.field.support.loadbalancing.MyCellInterface;

import java.util.ArrayList;
import java.util.HashMap;

import sim.engine.SimState;
import sim.field.grid.SparseGrid2D;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.util.Int2D;

/**
 *  Abstract class for Distributed Sparse Grid 2D
 *
 *  <h3>This Field extends SparseGrid2D, to be used in a distributed environment. 
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
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 */

public abstract class DSparseGrid2D extends SparseGrid2D implements DistributedField2D<Int2D>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//public int NUMPEERS;
	public int rows;

	public int columns;
	public int own_x;	//x coordinate of north-west corner
	public int own_y;	//y coordinate of north-west corner
	public int my_width;
	public int my_height;
	public int jumpDistance;
	public RegionInteger myfield;
	public RegionMap<Integer,Int2D> rmap=new RegionMap<Integer,Int2D>();
	public ArrayList<Region<Integer, Int2D>> updates_cache;
	public HashMap<Integer,HashMap<CellType, MyCellInterface>> listGrid;
	public ArrayList<ArrayList<Region<Integer, Int2D>>> updates_cacheLB;
	public UpdateCell<Integer,MyCellIntegerField> updateCell = new UpdateCell<Integer, MyCellIntegerField>();

	//public int MAX_DISTANCE;
	public CellType cellType;
	public UpdateMap<Integer,Int2D> updates=new UpdateMap<Integer,Int2D>();
	public SimState sm ;
	public ArrayList<String> neighborhood=new ArrayList<String>();
	public boolean gui = true;
	public SparseGridPortrayal2D p;
	private boolean isToroidal;
	
	public boolean isToroidal()
	{
		return isToroidal;
	}
	
	public void setToroidal(boolean isToroidal)
	{
		this.isToroidal=isToroidal;
	}

	public DSparseGrid2D(int width, int height) 
	{
		super(width, height);
	}
	public  void attachPortrayal(SparseGridPortrayal2D p){this.p=p;}

}