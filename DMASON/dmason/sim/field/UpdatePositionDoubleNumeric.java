package dmason.sim.field;

import java.io.Serializable;
import java.util.ArrayList;
import dmason.sim.loadbalancing.MyCellDoubleNumeric;

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

 * A class specialized for Double.Extends ArrayList and implements UpdatePositionInterface 
 * @see UpdatePositionInterface
 * @author lizard87
 *
 * @param <E>
 */
public class UpdatePositionDoubleNumeric<E> extends ArrayList<E> implements Serializable,UpdatePositionInterface{
	
	private static final long serialVersionUID = 1L;
	public static int CORNER_DIAG_UP_LEFT=0;
	public static int UP=1;
	public static int CORNER_DIAG_UP_RIGHT=2;
	public static int RIGHT=3;
	public static int CORNER_DIAG_DOWN_RIGHT=4;
	public static int DOWN=5;
	public static int CORNER_DIAG_DOWN_LEFT=6;
	public static int LEFT=7;
	public static int CENTER=8;
	
	private int POSITION;
	private long STEP;
	private boolean preBalance;
	private boolean preUnion;
	private boolean union;
	private CellType cellType;
	private MyCellDoubleNumeric mC = null;
	private int numAgentExternalCell;
	
	/**
	 * Constructor of class with parameters:
	 * @param step      the step
	 * @param position  the position
	 * @param celltype  the celltype 
	 * @param mC        my cell
	 */
	public UpdatePositionDoubleNumeric(long step,int position,CellType cellType,MyCellDoubleNumeric mC) {	
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
	public MyCellDoubleNumeric getMyCell() {return mC;}
	@Override
	public void setMyCell(Object mC) {this.mC = (MyCellDoubleNumeric)mC;}
	@Override
	public boolean isPreBalance() {return preBalance;}
	@Override
	public void setPreBalance(boolean preBalance) {this.preBalance = preBalance;}
	@Override
	public CellType getCellType() {return cellType;}
	@Override
	public void setCellType(CellType cellType) {this.cellType = cellType;}
	@Override
	public int getNumAgentExternalCell() {return numAgentExternalCell;}
	@Override
	public void setNumAgentExternalCell(int numAgentExternalCell) {this.numAgentExternalCell = numAgentExternalCell;}
}
