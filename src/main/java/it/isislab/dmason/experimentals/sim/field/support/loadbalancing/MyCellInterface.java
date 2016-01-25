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

package it.isislab.dmason.experimentals.sim.field.support.loadbalancing;

import it.isislab.dmason.sim.field.CellType;

import java.util.HashMap;

/**
*
* only mine
*  
*                                                                 CORNER DIAG 
*                                                                 /    
*  															  /
*  ________________________|__|__|______UP_OUT__________ _|__|__|/________________________|
* |__|___________________|_|__|__|10|___UP_MINE_______|11 |__|__|__|___________________|__|
* |__|___________________|_|__|9_|0_|_______1_________| 2_|12 |__|__|___________________|__|
* |                        |__|__|                     /  |__|__|                         |
* |                        L  L  |                    /   |  R  R                         |
* |                        E  E  |                   /    |  I  I                         |
* |                        F  F  |              CORNER    |  G  G                         |
* |                        T  T 7|         8     MINE     |3 H  H                         |
* |                        |  |  |                        |  T  T                         |
* |                        O  M  |       MYFIELD          |  |  |                         |
* |                        U  I  |                        |  M  O                         |
* |                        T__N__|                        |__I__U                         |
* |________________________|__|__|______________________ _|__|__|_________________________|
* |__|__________________|__|__|16|6_|DOWN_MINE_5______|4_ |13|__|__|____________________|_|
* |__|__________________|__|__|__|15|DOWN_OUT_________|14 |__|__|__|____________________|_|
   |
 * An interface with for all type of cell
 * @author it.isislab.dmason
 *
 */
public interface MyCellInterface {
	
	public static int CORNER_DIAG_UP_LEFT_LEFT=9;
	public static int CORNER_DIAG_UP_LEFT_UP=10;
	public static int CORNER_DIAG_UP_RIGHT_UP=11;
	public static int CORNER_DIAG_UP_RIGHT_RIGHT=12;
	public static int CORNER_DIAG_DOWN_RIGHT_RIGHT=13;
	public static int CORNER_DIAG_DOWN_RIGHT_DOWN=14;
	public static int CORNER_DIAG_DOWN_LEFT_DOWN=15;
	public static int CORNER_DIAG_DOWN_LEFT_LEFT=16;
	
	
	
	public static int NORTH_WEST=0;
	public static int NORTH=1;
	public static int NORTH_EAST=2; 
	public static int EAST=3;
	public static int SOUTH_EAST=4;
	public static int SOUTH=5;
	public static int SOUTH_WEST=6;
	public static int WEST=7;
	public static int CENTER=8;

	/**
	 * Returns a HashMap of all position of regions out that can to send the updates
	 * @return a HashMap<Integer, Boolean> when the Key is the position and the value is true or false,
	 * 			 depending on whether or not to send updates
	 */
	public HashMap<Integer, Boolean> getPositionGood() ;

	/**
	 * Sets the HashMap of all position of regions out that can to send the updates.
	 * @param positionGood a HashMap<Integer, Boolean> when the Key is the position and the value is true or false,
	 * 			 depending on whether or not to send updates.
	 */
	public void setPositionGood(HashMap<Integer, Boolean> positionGood);

	/**
	 * Return a HashMap of all topics on which is possible to send the updates.
	 * @return a HashMap<Integer, Boolean> when the Key is the topic and the value is true or false,
	 * 			 depending on whether or not to send updates
	 */
	public HashMap<Integer, Boolean> getPositionPublish();

	/**
	 * Sets the HashMap of all topic of MyCell that must to send the updates.
	 * @param topics a HashMap<Integer, Boolean> when the Key is the topic and the value is true or false,
	 * 			 depending on whether or not to send updates.
	 */
	public void setPositionPublish(HashMap<Integer, Boolean> positionPublish);

	/**
	 * Returns a celltype belonging to the cell that has divided 
	 * @return CellType  belonging to the cell that has divided 
	 */
	public CellType getParentCellType();

	/**
	 * Sets a celltype belonging to the cell that has divided
	 * @param parentType is a CellType of belonging to the cell that has divided 
	 */
	public void setParentCellType(CellType parentType);

	/**
	 * Return the name associate at field
	 * @return a String that indicates the name associate at field. 
	 */
	public String getId();
	
	/**
	 *  Sets the name associate at field
	 * @param name the name that you want to associate at field
	 */
	public void seId(String name);
	
	/**
	 * Return a RegionMap of the MyCell. The return value is an Object for to make the method as generic as possible
	 * @return a RegionMap
	 */
	public Object getMyRMap();

	/**
	 * Sets the RegionMap
	 * @param myRMap the new RegionMap that replace the old
	 */
	public void setMYRMAP(Object myRMap) ;

	/**
	 * Return a MyField of the MyCell. The return value is an Object for to make the method as generic as possible
	 * @return an object that extends Region
	 */
	public Object getMyField() ;

	/**
	 * Sets the MyField region of Cell
	 * @param myField the new MyField that replace the old
	 */
	public void setMyField(Object myField) ;
	
	/**
	 * It's the x cordinate of the corner up left of Cell
	 * @return the x cordinate of the corner up left of Cell
	 */
	public Object getOwn_x();

	/**
	 * Sets the x cordinate of the corner up left of Cell
	 * @param own_x the new x cordinate of the corner up left of Cell
	 */
	public void setOwn_x(Object own_x);
	
	/**
	 * It's the y cordinate of the corner up left of Cell
	 * @return the y cordinate of the corner up left of Cell
	 */
	public Object getOwn_y() ;

	/**
	 * Sets the y cordinate of the corner up left of Cell
	 * @param own_y the new y cordinate of the corner up left of Cell
	 */
	public void setOwn_y(Object own_y) ;

	/**
	 * Returns the width of cell
	 * @return the width of cell
	 */
	public Object getMy_width();

	/**
	 * Sets the width of cell
	 * @param my_width the new width of Cell
	 */
	public void setMy_width(Object my_width);

	/**
	 * Return the height of cell
	 * @return the height of cell
	 */
	public Object getMy_height();

	/**
	 * Sets the height of cell
	 * @param my_height the new height of cell
	 */
	public void setMy_height(Object my_height);

	/**
	 * is the position of cell within the zone that occupies a part of the simulation
	 * @return the position number of Cell
	 */
	public int getPosition();

	/**
	 * Sets the position of cell within the zone that occupies a part of the simulation
	 * @param position the new position of cell
	 */
	public void setPosition(int position);
	
	/**
	 * Return the number of step
	 * @return a long number associated at number of step
	 */
	public long getStep();

	/**
	 * Sets the new value of steps
	 * @param step the new value which will replace the old one
	 */
	public void setStep(long step);
	
	/**
	 * 
	 * @return
	 */
	public boolean isUnion();
	
	/**
	 * 
	 * @param makeUnion
	 */
	public void setUnion(boolean makeUnion);
	
	/**
	 * verify if the agents is contained in the microcell
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isMine(Object x ,Object y);
}
