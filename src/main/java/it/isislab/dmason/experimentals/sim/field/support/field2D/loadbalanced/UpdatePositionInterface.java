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

package it.isislab.dmason.experimentals.sim.field.support.field2D.loadbalanced;
import it.isislab.dmason.experimentals.sim.field.support.loadbalancing.MyCellInterface;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.support.field2D.DistributedRegionInterface;

import java.io.Serializable;


/**
 * An interface for list of update  
 * @author it.isislab.dmason
 *
 */
public interface UpdatePositionInterface extends Serializable,DistributedRegionInterface{

	public static int CORNER_DIAG_UP_LEFT=0;//CDUL
	public static int UP=1;//UP
	public static int CORNER_DIAG_UP_RIGHT=2;//CDUR
	public static int RIGHT=3;//RIGHT
	public static int CORNER_DIAG_DOWN_RIGHT=4;//CDDR
	public static int DOWN=5;//DOWN
	public static int CORNER_DIAG_DOWN_LEFT=6;//CDDL
	public static int LEFT=7;//LEFT
	public static int CENTER=8;//CENTER
	
	/**
	 * Return the position
	 * @return the position
	 */
	@Override
	public int getPosition() ;

	/**
	 * Set the position with the param :
	 * @param position 
	 */
	public void setPosition(int position) ;
	
	/**
	 * Return  currently step
	 * @return step
	 */
	@Override
	public long getStep();
	
	/**
	 * Set step with the parmam:
	 * @param step
	 */
	public void setStep(long step);
	
	/**
	 * Return the MyCell
	 * @see MyCellInterface
	 * @return MyCell
	 */
	public Object getMyCell();

	/**
	 * Set MyCell
	 * @param mC MyCell
	 */
	public void setMyCell(Object mC);

	/**
	 * Return the value of isPrebalance
	 * @return true if next step start loadbalancing
	 */
	public boolean isPreBalance();

	/**
	 * Set isPrebalance 
	 * @param preBalance 
	 */
	public void setPreBalance(boolean preBalance);

    /**
     * 
     * @return
     */
	public boolean isUnion() ;

	/**
	 * 
	 * @param union
	 */
	public void setUnion(boolean union) ;
	
	/**
	 * 
	 * @return
	 */
	public boolean isPreUnion() ;

	/**
	 * 
	 * @param preUnion
	 */
	public void setPreUnion(boolean preUnion);
	
	/**
	 * 
	 * @return a celltype of Cell that send the updates
	 */
	public CellType getCellType();
	
	/**
	 * set the CellType of Cell that send the updates 
	 * @param celltype the CellType of Cell that send the updates
	 */
	public void setCellType(CellType celltype);

	/**
	 * return the number of agents of an external microcell
	 * @return the number of agents
	 */
	public int getNumAgentExternalCell();
	
	/**
	 * set the number of agents of an external microcell
	 * @param the number of agents
	 */
	public void setNumAgentExternalCell(int numAgentExternalCell);
}