package dmason.sim.field;
import java.io.Serializable;
import dmason.sim.loadbalancing.MyCellInterface;


/**
 * An interface for list of update  
 * @author dmason
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