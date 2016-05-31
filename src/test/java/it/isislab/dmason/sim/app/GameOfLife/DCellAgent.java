package it.isislab.dmason.sim.app.GameOfLife;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.engine.DistributedState;
import sim.engine.SimState;
import sim.util.Int2D;

public class DCellAgent extends RemoteCellAgent<Int2D> {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public DCellAgent(){}
	public DCellAgent(DistributedState<Int2D> sm, Int2D location){
		pos = location;
	}
	
	@Override
	public void step(SimState state)
	{
		
		DGameOfLife tut = (DGameOfLife)state;
		int count=0;
		int width = tut.grid.my_width;
		int height = tut.grid.my_height;
		
		if(tut.grid.cellType.pos_i==1 && tut.grid.cellType.pos_j==0)
			System.out.println(tut.grid.own_y);
		for(int y=tut.grid.own_y; y<tut.grid.own_y+height; y++)
		{
			for(int x=tut.grid.own_x; x<tut.grid.own_x+width; x++)
			{
				
				//System.out.println("step executed!");
				count = 0;
				// count the number of neighbors around the cell,
				// and for good measure include the cell itself
				for(int dx = -1; dx < 2; dx++)
					for(int dy = -1; dy < 2; dy++)
						count += isAlive(tut.schedule.getSteps(), tut.grid.field[tut.grid.stx(x+dx)][tut.grid.sty(y+dy)]); 

				
				// if the count is 2 or less, or 5 or higher, the cell dies
				// else if the count is 3 exactly, a dead cell becomes live again
				// else the cell stays as it is
				try{
					if (count <= 2 || count >= 5)  // dead
						//tut.grid.field[x][y] = setDie(tut.schedule.getSteps(), tut.grid.field[x][y]);
						tut.grid.setDistributedObjectLocation(new Int2D(x, y), setDie(tut.schedule.getSteps(), tut.grid.field[x][y]), tut);
					else if (count == 3) // life
						//tut.grid.field[x][y] = setAlive(tut.schedule.getSteps(), tut.grid.field[x][y]);
						tut.grid.setDistributedObjectLocation(new Int2D(x, y), setAlive(tut.schedule.getSteps(), tut.grid.field[x][y]), tut);
					else
						if(isAlive(tut.schedule.getSteps(), tut.grid.field[x][y]) == 1)
							tut.grid.setDistributedObjectLocation(new Int2D(x, y), setAlive(tut.schedule.getSteps(), tut.grid.field[x][y]), tut);
					//tut.grid.field[x][y] = setAlive(tut.schedule.getSteps(), tut.grid.field[x][y]);
						else
							//tut.grid.field[x][y] = setDie(tut.schedule.getSteps(), tut.grid.field[x][y]);
							tut.grid.setDistributedObjectLocation(new Int2D(x, y), setDie(tut.schedule.getSteps(), tut.grid.field[x][y]), tut);
				}catch(DMasonException e){
					e.printStackTrace();}
				
				if(y==height-1 && x==width-1 && tut.grid.field[x][y]>0)
				{
					System.out.println("gesocrist "+tut.schedule.getSteps());
				}
			}
		}
		
		try {
			tut.core.setDistributedObjectLocation(pos, this, tut);
		} catch (DMasonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int isAlive(long step, int val)
	{
		return (step % 2 == 0)?
				val % 2:
					val / 2;
	}
	public int setAlive(long step, int val)
	{
		if(step % 2 == 0)
		{
			return ( val < 2 )? 
					val+2:
						val;
		}
		else{
			return (val % 2 == 0)?
					val +1 :
						val;
		}
	}
	public int setDie(long step, int val)
	{
		if(step % 2 == 0)
		{
			return ( val >= 2 )? 
					val-2:
						val;
		}
		else{
			return (val % 2 == 1)?
					val - 1 :
						val;
		}
	}

}
