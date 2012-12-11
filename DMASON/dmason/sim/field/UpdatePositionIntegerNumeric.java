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

package dmason.sim.field;

import java.io.Serializable;
import java.util.ArrayList;
import dmason.sim.loadbalancing.MyCellIntegerNumeric;

/**
________________________________________________________________________
|						|						|						|                  
|						|						|						|
|						|						|						|
|						|						|						|
|						|						|						|
|			0			|			1			|			2			|
|		   CDUL			|			UP			|		   CDUR			|
|						|						|						|
|						|						|						|
|_______________________|_______________________|_______________________|
|						|						|						|                                
|						|						|						|
|						|						|		     			|
|						|						|						|
|						|						|						|
|			7			|			8			|		    3       	|
|		   LEFT			|		  CENTER		|		  RIGHT		    |
|						|						|						|
|						|						|						|
|_______________________|_______________________|_______________________|
|						|						|						|                                
|						|						|						|
|						|						|						|
|						|						|						|
|			6			|			5			|			4			|
|		   CDDL			|		   DOWN			|		   CDDR			|
|						|						|						|
|						|						|						|
|						|						|						|
|_______________________|_______________________|_______________________|


 * A class specialized for Integer.Extends ArrayList and implements UpdatePositionInterface 
 *  @see UpdatePositionInterface
 * @author dmason
 *
 */
public class UpdatePositionIntegerNumeric<E> extends ArrayList<E> implements Serializable,UpdatePositionInterface{

	private int POSITION;
	private long STEP;
	private boolean preBalance;
	private boolean preUnion;
	private boolean union;
	private CellType cellType;
	private MyCellIntegerNumeric mC = null;
	private int numAgentExternalCell;
	
	/**
	 * Constructor of class with parameters:
	 * @param step      the step
	 * @param position  the position
	 * @param celltype  the celltype 
	 * @param mC        my cell
	 */
	public UpdatePositionIntegerNumeric(long step,int position,CellType cellType,MyCellIntegerNumeric mC) {	
		this.STEP = step;
		this.POSITION = position;
		this.mC = mC;
		this.cellType = cellType;
		this.preBalance = false;
		this.preUnion = false;
		this.union = false;
	}
	
	@Override
	public boolean isUnion() {return union;}
	@Override
	public void setUnion(boolean union) {this.union = union;}
	@Override
	public boolean isPreUnion() {return preUnion;}
	@Override
	public void setPreUnion(boolean preUnion) {this.preUnion = preUnion;}	
	@Override
	public int getPosition() {return POSITION;}
	@Override
	public void setPosition(int position) {POSITION = position;}	
	@Override
	public long getStep(){return STEP;}
	@Override
	public void setStep(long step){STEP = step;}
	@Override
	public MyCellIntegerNumeric getMyCell() {return mC;}
	@Override
	public void setMyCell(Object mC) {this.mC = (MyCellIntegerNumeric)mC;}
	@Override
	public boolean isPreBalance() {return preBalance;}
	@Override
	public void setPreBalance(boolean preBalance) {this.preBalance = preBalance;}
	@Override
	public CellType getCellType() {return cellType;}
	@Override
	public void setCellType(CellType celltype) {this.cellType = celltype;}
	@Override
	public int getNumAgentExternalCell() {return numAgentExternalCell;}
	@Override
	public void setNumAgentExternalCell(int numAgentExternalCell) {this.numAgentExternalCell = numAgentExternalCell;}
}