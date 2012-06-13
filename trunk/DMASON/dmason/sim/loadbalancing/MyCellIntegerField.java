package dmason.sim.loadbalancing;

import java.io.Serializable;
import java.util.HashMap;
import dmason.sim.field.CellType;
import dmason.sim.field.RegionMap;
import dmason.sim.field.grid.RegionInteger;

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
 * 
 *The class create a cell that is one of the sub-cells that constitute part of the simulation.   
 *@see MyCellInterface
 */
public class MyCellIntegerField implements MyCellInterface,Serializable{

	
	private static final long serialVersionUID = 1L;
	private RegionMap MYRMAP;
	private RegionInteger MYFIELD;
	private String NAME;
	private CellType PARENTTYPE;
	private int own_x; 
	private int own_y;
	private int upl_xx, upl_yy, down_xx, down_yy;
	private int my_width;
	private int my_height;
	private HashMap<Integer, Boolean> positionPublish;
	private HashMap<Integer, Boolean> positionGood;
	private int POSITION;	
	private long step;
	private boolean makeUnion;
	
	/**
	 * Constructor of class with parameters:
	 *  
	 * @param MYRMAP RegionMapLB with all region of this cell
	 * @param MYFIELD  central region of Cell
	 * @param name identifier of field
	 * @param own_x x cordinate of corner up left, origin of cell
	 * @param own_y y cordinate of corner up left, origin of cell
	 * @param my_width width of cell
	 * @param my_height height of cell
	 * @param step number of step
	 * @param parentType CellType of Cell which is divided 
	 * @param positionPublish a HashMap<Integer,Boolean> where the key is the topic and the associated value indicates if is possible to send on this topic  
	 * @param positionGood a HashMap<Integer,Boolean> where the key is the region and the associated value indicates if is possible to send the update
	 * @param position is the position of cell into field
	 */
	public MyCellIntegerField(RegionMap MYRMAP, RegionInteger MYFIELD, String name, int own_x, int own_y, 
			int my_width, int my_height, long step, CellType parentType, HashMap<Integer, 
				Boolean> positionPublish,HashMap<Integer, Boolean> positionGood, int position) {
	
		this.NAME = name;
		this.MYRMAP = MYRMAP;
		this.MYFIELD = MYFIELD;
		this.own_x = own_x;
		this.own_y = own_y;
		this.my_width = my_width;
		this.my_height = my_height;
		this.step = step;
		this.PARENTTYPE = parentType;
		this.POSITION = position;
		this.positionPublish = positionPublish;
		this.positionGood = positionGood;
		this.makeUnion = false;
		this.upl_xx = (Integer) ((RegionInteger)MYRMAP.corner_out_up_left_diag).upl_xx; 
		this.upl_yy = (Integer) ((RegionInteger)MYRMAP.corner_out_up_left_diag).upl_yy;
		this.down_xx = (Integer) ((RegionInteger)MYRMAP.corner_out_down_right_diag).down_xx; 
		this.down_yy = (Integer) ((RegionInteger)MYRMAP.corner_out_down_right_diag).down_xx;
		
	}

	/**
	 * 
	 * @param MYRMAP RegionMapLB with all region of this cell
	 * @param MYFIELD  central region of Cell
	 * @param name identifier of field
	 * @param own_x x cordinate of corner up left, origin of cell
	 * @param own_y y cordinate of corner up left, origin of cell
	 * @param my_width width of cell
	 * @param my_height height of cell
	 * @param step number of step
	 * @param parentType CellType of Cell which is divided 
	 * @param position is the position of cell into field
	 */
	public MyCellIntegerField(RegionMap MYRMAP, RegionInteger MYFIELD, String name, int own_x, int own_y, 
			int my_width, int my_height, long step, CellType parentType, int position) {
		
		this.NAME = name;
		this.MYRMAP = MYRMAP;
		this.MYFIELD = MYFIELD;
		this.own_x = own_x;
		this.own_y = own_y;
		this.my_width = my_width;
		this.my_height = my_height;
		this.step = step;
		this.PARENTTYPE = parentType;
		this.POSITION = position;
		this.makeUnion = false;
		
		this.upl_xx = (Integer) ((RegionInteger)MYRMAP.corner_out_up_left_diag).upl_xx; 
		this.upl_yy = (Integer) ((RegionInteger)MYRMAP.corner_out_up_left_diag).upl_yy;
		this.down_xx = (Integer) ((RegionInteger)MYRMAP.corner_out_down_right_diag).down_xx; 
		this.down_yy = (Integer) ((RegionInteger)MYRMAP.corner_out_down_right_diag).down_xx;
		
		positionPublish = new HashMap<Integer, Boolean>();
		positionGood = new HashMap<Integer, Boolean>();
		
		for (int j2 = 0; j2 <= 8; j2++) {
			positionPublish.put(j2, false);
		}
		
		for (int j2 = 0; j2 <= 16; j2++) {
			
			positionGood.put(j2, false);
		}
	}
	
	@Override
	public HashMap<Integer, Boolean> getPositionGood() {return positionGood;}
	@Override
	public void setPositionGood(HashMap<Integer, Boolean> positionGood) {this.positionGood = positionGood;}
	@Override
	public HashMap<Integer, Boolean> getPositionPublish() {return positionPublish;}
	@Override
	public void setPositionPublish(HashMap<Integer, Boolean> PositionPublish) {this.positionPublish = PositionPublish;}
	@Override
	public CellType getParentCellType() {return PARENTTYPE;}
	@Override
	public void setParentCellType(CellType parentType) {PARENTTYPE = parentType;}
	@Override
	public String getId() {return NAME;}
	@Override
	public void seId(String name){this.NAME = name;}
	@Override
	public RegionMap getMyRMap() {return MYRMAP;}
	@Override
	public void setMYRMAP(Object myRMap) {MYRMAP = (RegionMap)myRMap;}
	@Override
	public RegionInteger getMyField() {return MYFIELD;}
	@Override
	public void setMyField(Object myField) {MYFIELD = (RegionInteger)myField;}
	@Override
	public Object getOwn_x(){return own_x;}
	@Override
	public void setOwn_x(Object own_x){this.own_x = (Integer) own_x;}
	@Override
	public Object getOwn_y() {return own_y;}
	@Override
	public void setOwn_y(Object own_y) {this.own_y = (Integer)own_y;}
	@Override
	public Object getMy_width() {return my_width;}
	@Override
	public void setMy_width(Object my_width) {this.my_width = (Integer)my_width;}
	@Override
	public Object getMy_height() {return my_height;}
	@Override
	public void setMy_height(Object my_height) {this.my_height = (Integer)my_height;}
	@Override
	public int getPosition() {return POSITION;}
	@Override
	public void setPosition(int position) {this.POSITION = position;}
	@Override
	public long getStep() {return step;}
	@Override
	public void setStep(long step) {this.step = step;}
	@Override
	public boolean isUnion() {return this.makeUnion;}
	@Override
	public void setUnion(boolean makeUnion) {this.makeUnion = makeUnion;}
	@Override
	public boolean isMine(Object xx, Object yy) {
		int x = (Integer) xx;
		int y = (Integer) yy;
		return (x>=upl_xx) && (y >= upl_yy) && (x < down_xx) && (y< down_yy);
	}
}
