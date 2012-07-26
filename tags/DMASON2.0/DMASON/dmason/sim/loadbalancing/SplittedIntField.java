package dmason.sim.loadbalancing;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import sim.engine.SimState;
import sim.util.Int2D;
import dmason.sim.engine.DistributedState;
import dmason.sim.field.CellType;
import dmason.sim.field.RegionMap;
import dmason.sim.field.grid.RegionIntegerLB;

public class SplittedIntField implements Serializable {

	private static final long serialVersionUID = 1L;
	private final int costantDivide=3;//used to divide everycell in a 3x3
	public int NUMPEERS;
	public int own_x;	//x coordinate of north-west corner
	public int own_y;	//y coordinate of north-west corner
	public int my_width;
	public int my_height;
	public RegionIntegerLB myfield;
	public RegionMap<Integer, Int2D> rmap=new RegionMap<Integer, Int2D>();
	private RegionMap<Integer, Int2D> superRmap = null;
	public int MAX_DISTANCE;
	public CellType cellType;
	public CellType parentType;
    public SimState sm ;
	public int superOwnX;
	public int superOwnY;
	public int TOTALWIDTH; // grandezza della simulazione
	public int TOTALHEIGHT;// grandezza della simulazione
	private int SUPERWIDTH;
	private int SUPERHEIGHT;
	private String NAME;
	private int POSITION;
	private HashMap<Integer, Boolean> topics;
	private HashMap<Integer, Boolean> positionGood;
	
	
	/**
	 *Constructor of class with parameters: 
	 *
	 * @param superOwnX is the X coordinate of up left corner of cell that must divide
	 * @param superOwnY is the Y coordinate of up left corner of cell that must divide
	 * @param superWidth is the Width of cell that must divide
	 * @param superHeight is the Height of cell that must divide
	 * @param state the distribute state of simulation
	 * @param mAX_DISTANCE is the max distance for the communication between two or more cells.
	 * @param i is the first number that identifies the cell  
	 * @param j is the second number that identifies the cell
	 * @param numPeers number of peers
	 * @param name is the name of field
	 * @param position if the position of cell into piece of simulation that show
	 * @param parentType is the number of the cell, that divides
	 * @param topics is the list of topic that the single cell must have for the communication
	 * @param positionGood is the list of all side that can send the exact updates
	 */
	public SplittedIntField(int superOwnX, int superOwnY, int superWidth,
			int superHeight, DistributedState state, int mAX_DISTANCE,int i, int j,
				int numPeers, String name, int position, CellType parentType, 
					HashMap<Integer, Boolean> topics, HashMap<Integer, Boolean> positionGood) 
	{
		this.superOwnX = superOwnX ;
		this.superOwnY = superOwnY;
		this.SUPERWIDTH = superWidth ;
		this.SUPERHEIGHT = superHeight;
		this.sm = state;
		this.MAX_DISTANCE = mAX_DISTANCE;
		this.NUMPEERS = numPeers;
		
		this.TOTALWIDTH = superWidth * (int)Math.sqrt(NUMPEERS);
		this.TOTALHEIGHT = superHeight* (int)Math.sqrt(NUMPEERS);
		
		cellType = new CellType(i, j);
		this.parentType = parentType;
		this.NAME = name;
		this.POSITION = position;
		this.topics = topics;
		this.positionGood = positionGood;
		SplitRegionInteger();
	}

	/**
	 * Create region of Integer field
	 * Calculate the right points for any cell
	 */
	private void SplitRegionInteger(){
		// own width and height
		my_width=(int)( SUPERWIDTH/costantDivide);
		my_height=(int) (SUPERHEIGHT /costantDivide);
		
		if(cellType.pos_i == cellType.pos_j){
			if(cellType.pos_i == 0){
				own_x= superOwnX;
				own_y= superOwnY;
			}else{
				own_x = superOwnX+(my_width*cellType.pos_j);
				own_y = superOwnY+(my_height*cellType.pos_i);
			}
			
		}
		if(cellType.pos_i > cellType.pos_j){
			own_x = superOwnX+(my_width*cellType.pos_j);
			own_y = superOwnY +(my_height * cellType.pos_i);		
		}
		
		if(cellType.pos_i < cellType.pos_j){
			own_x = superOwnX + (my_width * cellType.pos_j);
			own_y = superOwnY + (my_height*cellType.pos_i);
		}
		
		//my field
		myfield=new RegionIntegerLB(own_x+MAX_DISTANCE,own_y+MAX_DISTANCE,own_x+my_width-MAX_DISTANCE, 
				own_y+my_height-MAX_DISTANCE,TOTALWIDTH,TOTALHEIGHT);

		//corner mine up left
		rmap.corner_mine_up_left=new RegionIntegerLB(own_x, own_y,own_x+MAX_DISTANCE, 
				own_y+MAX_DISTANCE,TOTALWIDTH, TOTALHEIGHT);	
		//corner out up left		
		rmap.corner_out_up_left_diag_center=new RegionIntegerLB((own_x-MAX_DISTANCE + TOTALWIDTH)%TOTALWIDTH, 
				(own_y-MAX_DISTANCE+TOTALHEIGHT)%TOTALHEIGHT,(own_x+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+TOTALHEIGHT)%TOTALHEIGHT,TOTALWIDTH, TOTALHEIGHT);
		rmap.corner_out_up_left_diag_up=new RegionIntegerLB((own_x + TOTALWIDTH)%TOTALWIDTH, 
				(own_y-MAX_DISTANCE+TOTALHEIGHT)%TOTALHEIGHT, ((own_x+TOTALWIDTH)+MAX_DISTANCE)%TOTALWIDTH, 
				(own_y+TOTALHEIGHT)%TOTALHEIGHT,TOTALWIDTH, TOTALHEIGHT);
		rmap.corner_out_up_left_diag_left=new RegionIntegerLB((own_x-MAX_DISTANCE + TOTALWIDTH)%TOTALWIDTH, 
				(own_y+TOTALHEIGHT)%TOTALHEIGHT,(own_x+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+TOTALHEIGHT+MAX_DISTANCE)%TOTALHEIGHT,TOTALWIDTH, TOTALHEIGHT);
		
		//corner mine up right		
		rmap.corner_mine_up_right=new RegionIntegerLB(own_x+my_width-MAX_DISTANCE, own_y, 
				own_x+my_width, own_y+MAX_DISTANCE,TOTALWIDTH, TOTALHEIGHT);
		//corner out up right
		rmap.corner_out_up_right_diag_center = new RegionIntegerLB((own_x+my_width+TOTALWIDTH)%TOTALWIDTH, 
				(own_y-MAX_DISTANCE+TOTALHEIGHT)%TOTALHEIGHT, (own_x+my_width+MAX_DISTANCE+TOTALWIDTH)
				%TOTALWIDTH, (own_y+TOTALHEIGHT)%TOTALHEIGHT,TOTALWIDTH, TOTALHEIGHT);
		rmap.corner_out_up_right_diag_up=new RegionIntegerLB((own_x+my_width+TOTALWIDTH-MAX_DISTANCE)
				%TOTALWIDTH, (own_y-MAX_DISTANCE+TOTALHEIGHT)%TOTALHEIGHT,(own_x+my_width+TOTALWIDTH)
				%TOTALWIDTH, (own_y+TOTALHEIGHT)%TOTALHEIGHT,TOTALWIDTH, TOTALHEIGHT);
		rmap.corner_out_up_right_diag_right=new RegionIntegerLB((own_x+my_width+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+TOTALHEIGHT)%TOTALHEIGHT,(own_x+my_width+MAX_DISTANCE+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+TOTALHEIGHT+MAX_DISTANCE)%TOTALHEIGHT,TOTALWIDTH, TOTALHEIGHT);
		
		// corner mine down left
		rmap.corner_mine_down_left=new RegionIntegerLB(own_x, own_y+my_height-MAX_DISTANCE,
				own_x+MAX_DISTANCE, own_y+my_height,TOTALWIDTH, TOTALHEIGHT);
		//corner out down left
		rmap.corner_out_down_left_diag_center=new RegionIntegerLB((own_x-MAX_DISTANCE+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT,(own_x+TOTALWIDTH)%TOTALWIDTH,
				(own_y+my_height+MAX_DISTANCE+TOTALHEIGHT)%TOTALHEIGHT,TOTALWIDTH, TOTALHEIGHT);
		rmap.corner_out_down_left_diag_down=new RegionIntegerLB((own_x+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT,(own_x+TOTALWIDTH+MAX_DISTANCE)%TOTALWIDTH,
				(own_y+my_height+TOTALHEIGHT+MAX_DISTANCE)%TOTALHEIGHT,TOTALWIDTH, TOTALHEIGHT);
		rmap.corner_out_down_left_diag_left=new RegionIntegerLB((own_x-MAX_DISTANCE+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+my_height+TOTALHEIGHT-MAX_DISTANCE)%TOTALHEIGHT,(own_x+TOTALWIDTH)%TOTALWIDTH,
				(own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT,TOTALWIDTH, TOTALHEIGHT);

		//corner mine down right
		rmap.corner_mine_down_right=new RegionIntegerLB(own_x+my_width-MAX_DISTANCE, 
				own_y+my_height-MAX_DISTANCE,own_x+my_width,own_y+my_height,TOTALWIDTH,TOTALHEIGHT);		
		//corner out down right
		rmap.corner_out_down_right_diag_center=new RegionIntegerLB((own_x+my_width+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT,(own_x+my_width+MAX_DISTANCE+TOTALWIDTH)
				%TOTALWIDTH,(own_y+my_height+MAX_DISTANCE+TOTALHEIGHT)%TOTALHEIGHT,TOTALWIDTH, TOTALHEIGHT);
		rmap.corner_out_down_right_diag_down=new RegionIntegerLB((own_x+my_width+TOTALWIDTH-MAX_DISTANCE)
				%TOTALWIDTH, (own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT,(own_x+my_width+TOTALWIDTH)
				%TOTALWIDTH,(own_y+my_height+MAX_DISTANCE+TOTALHEIGHT)%TOTALHEIGHT,TOTALWIDTH, TOTALHEIGHT); 
		rmap.corner_out_down_right_diag_right=new RegionIntegerLB((own_x+my_width+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+my_height+TOTALHEIGHT-MAX_DISTANCE)%TOTALHEIGHT,(own_x+my_width+TOTALWIDTH
				+MAX_DISTANCE)%TOTALWIDTH,(own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT,TOTALWIDTH, 
				TOTALHEIGHT);

		//mine left		
		rmap.left_mine=new RegionIntegerLB(own_x,own_y+MAX_DISTANCE,own_x + MAX_DISTANCE, 
				own_y+my_height-MAX_DISTANCE,TOTALWIDTH,TOTALHEIGHT);
		//out left
		rmap.left_out=new RegionIntegerLB((own_x-MAX_DISTANCE+TOTALWIDTH)%TOTALWIDTH,
				((own_y+TOTALHEIGHT)+MAX_DISTANCE)%TOTALHEIGHT,(own_x+TOTALWIDTH)%TOTALWIDTH, 
				(((own_y+my_height)+TOTALHEIGHT)-MAX_DISTANCE)%TOTALHEIGHT,TOTALWIDTH,
				TOTALHEIGHT);
		
		//mine right
		rmap.right_mine=new RegionIntegerLB(own_x + my_width - MAX_DISTANCE,own_y+MAX_DISTANCE,
				own_x +my_width , own_y+my_height-MAX_DISTANCE,TOTALWIDTH,
				TOTALHEIGHT);
		//out right
		rmap.right_out=new RegionIntegerLB((own_x+my_width+TOTALWIDTH)%TOTALWIDTH,
				((own_y+TOTALHEIGHT)+MAX_DISTANCE)%TOTALHEIGHT,(own_x+my_width+MAX_DISTANCE+TOTALWIDTH)
				%TOTALWIDTH, ((own_y+my_height+TOTALHEIGHT)-MAX_DISTANCE)%TOTALHEIGHT,
				TOTALWIDTH,TOTALHEIGHT);		

		//mine up
		rmap.up_mine=new RegionIntegerLB(own_x+MAX_DISTANCE,own_y,own_x+my_width-MAX_DISTANCE, 
				own_y + MAX_DISTANCE ,TOTALWIDTH,TOTALHEIGHT);
		//out up
		rmap.up_out=new RegionIntegerLB(((own_x+TOTALWIDTH)+MAX_DISTANCE)%TOTALWIDTH, 
				(own_y - MAX_DISTANCE+TOTALHEIGHT)%TOTALHEIGHT,((own_x+ my_width +TOTALWIDTH)-MAX_DISTANCE)
				%TOTALWIDTH,(own_y+TOTALHEIGHT)%TOTALHEIGHT,TOTALWIDTH,
				TOTALHEIGHT);		
		
		//mine down
		rmap.down_mine=new RegionIntegerLB(own_x+MAX_DISTANCE,own_y+my_height-MAX_DISTANCE,own_x+my_width-MAX_DISTANCE, 
				(own_y+my_height),TOTALWIDTH,TOTALHEIGHT);
		//out down
		rmap.down_out=new RegionIntegerLB( ((own_x+TOTALWIDTH)+MAX_DISTANCE) %TOTALWIDTH,
				(own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT,
				((own_x+my_width+TOTALWIDTH)-MAX_DISTANCE)%TOTALWIDTH, 
				(own_y+my_height+MAX_DISTANCE+TOTALHEIGHT)%TOTALHEIGHT,
				TOTALWIDTH,TOTALHEIGHT);		
	}
	
	//only for testing...used for print region created... stipa ca truov
	public void stampa()
	{	
		
	Class o=this.rmap.getClass();
		
		Field[] fields = o.getDeclaredFields();
		
		System.out.println("MYFIELD DI "+cellType.toString()+": "+myfield.toString());
		
		for (int z = 0; z < fields.length; z++)
		{
			fields[z].setAccessible(true);
			
			try
			{
				String name=fields[z].getName();
		    	Method method = o.getMethod("get"+name, null);
		    	Object returnValue = method.invoke(rmap, null);
		    	
		    	
		    	System.out.println("name="+name +" "+ returnValue+"TTH "+TOTALHEIGHT+"TTW"+TOTALWIDTH+"owny+my_heit="+(own_y+my_height));
		    	
		    
			}
			catch (IllegalArgumentException e){e.printStackTrace();} 
			catch (IllegalAccessException e) {e.printStackTrace();} 
			catch (SecurityException e) {e.printStackTrace();} 
			catch (NoSuchMethodException e) {e.printStackTrace();} 
			catch (InvocationTargetException e) {e.printStackTrace();}
		}	
	}
	
	public MyCellIntegerField createMyDivision()
	{
		if((topics != null) && (positionGood != null))
			return new MyCellIntegerField(rmap, myfield, NAME, own_x, own_y, my_width, my_height, ((DistributedState)sm).schedule.getSteps(), parentType, topics, positionGood, POSITION);
		else
			return new MyCellIntegerField(rmap, myfield, NAME, own_x, own_y, my_width, my_height, ((DistributedState)sm).schedule.getSteps(), parentType, POSITION);
	}

}