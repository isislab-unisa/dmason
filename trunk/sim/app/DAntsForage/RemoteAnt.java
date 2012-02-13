package dmason.sim.app.DAntsForage;

import java.awt.Color;
import java.awt.Graphics2D;

import dmason.sim.engine.DistributedState;
import dmason.sim.engine.RemoteAgent;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.portrayal.DrawInfo2D;
import sim.util.Int2D;

public class RemoteAnt extends RemoteAgent<Int2D>
{
	public boolean getHasFoodItem() { return hasFoodItem; }
	public void setHasFoodItem(boolean val) { hasFoodItem = val; }
	public boolean hasFoodItem = false;
	double reward = 0;
    
	Int2D last;
	public RemoteAnt(){}
	public RemoteAnt(double initialReward){
	
		reward = initialReward; 
	}
    
	public RemoteAnt(DistributedState<Int2D> state, double initialReward) 
	{ 
		super(state);
		reward = initialReward; 
	}
	
	public void step(SimState state)
    {
		
		depositPheromone(state);
		act(state);
    }
    
    
// at present we have only one algorithm: value iteration.  I might
// revise this and add our alternate (TD) algorithm.  See the papers.
    
    
public void depositPheromone( final SimState state)
    {
    final AntsForage af = (AntsForage)state;
            
    Int2D location = af.buggrid.getObjectLocation(this);
    int x = location.x;
    int y = location.y;
            
    if (af.ALGORITHM == af.ALGORITHM_VALUE_ITERATION)
        {
        // test all around
        if (hasFoodItem)  // deposit food pheromone
            {
            double max = af.toFoodGrid.field[x][y];
            for(int dx = -1; dx < 2; dx++)
                for(int dy = -1; dy < 2; dy++)
                {
                    int _x = dx+x;
                    int _y = dy+y; 
                    
                	if((af.toFoodGrid.own_x<= x) && (x<(af.toFoodGrid.own_x+af.toFoodGrid.my_width)) && 
                			(af.toFoodGrid.own_y<=y) && (y<(af.toFoodGrid.own_y+af.toFoodGrid.my_height))){
                		
                        if (_x < 0 || _y < 0 || _x >= af.GRID_WIDTH || _y >= af.GRID_HEIGHT) continue;  // nothing to see here
                        double m = af.toFoodGrid.field[_x][_y] * 
                            (dx * dy != 0 ? // diagonal corners
                            af.diagonalCutDown : af.updateCutDown) +
                            reward;
                        if (m > max) max = m;
                	}
                 
                }
            //af.toFoodGrid.field[x][y] = max;
            af.toFoodGrid.setDistributedObjectLocation(max, new Int2D(x, y), state);     
            }
        else
            {
            double max = af.toHomeGrid.field[x][y];
            for(int dx = -1; dx < 2; dx++)
                for(int dy = -1; dy < 2; dy++)
                {
                    int _x = dx+x;
                    int _y = dy+y;
                        
                	if((af.toHomeGrid.own_x<= x) && (x<(af.toHomeGrid.own_x+af.toHomeGrid.my_width)) && 
                			(af.toHomeGrid.own_y<=y) && (y<(af.toHomeGrid.own_y+af.toHomeGrid.my_height))){
                	
                		if (_x < 0 || _y < 0 || _x >= af.GRID_WIDTH || _y >= af.GRID_HEIGHT) continue;  // nothing to see here
                        double m = af.toHomeGrid.field[_x][_y] * 
                            (dx * dy != 0 ? // diagonal corners
                            af.diagonalCutDown : af.updateCutDown) +
                            reward;
                        if (m > max) max = m;
                	}
                }
            //af.toHomeGrid.field[x][y] = max;
            af.toHomeGrid.setDistributedObjectLocation(max, new Int2D(x, y), state);
            }
        }
    /**
    for (int i = 0; i < 100; i++) {
		for (int j = 0; j < 100; j++) {
			
			System.out.print(af.toHomeGrid.field[j][i]+" ");
		}
		System.out.println();
	}*/
    reward = 0.0;
    }

public void act( final SimState state )
    {
    final AntsForage af = (AntsForage)state;
            
    Int2D location = af.buggrid.getObjectLocation(this);
    int x = location.x;
    int y = location.y;
            
    if (hasFoodItem)  // follow home pheromone
        {
        double max = af.IMPOSSIBLY_BAD_PHEROMONE;
        int max_x = x;
        int max_y = y;
        int count = 2;
        for(int dx = -1; dx < 2; dx++)
            for(int dy = -1; dy < 2; dy++)
                {
                int _x = dx+x;
                int _y = dy+y;
                if ((dx == 0 && dy == 0) ||
                    _x < 0 || _y < 0 ||
                    _x >= af.GRID_WIDTH || _y >= af.GRID_HEIGHT || 
                    af.obstacles.field[_x][_y] == 1) continue;  // nothing to see here
               
                double m = af.toHomeGrid.field[_x][_y];
                if (m > max)
                    {
                    count = 2;
                    }
                // no else, yes m > max is repeated
                if (m > max || (m == max && state.random.nextBoolean(1.0 / count++)))  // this little magic makes all "==" situations equally likely
                    {
                    max = m;
                    max_x = _x;
                    max_y = _y;
                    }
                }
        if (max == 0 && last != null)  // nowhere to go!  Maybe go straight
            {
            if (state.random.nextBoolean(af.momentumProbability))
                {
                int xm = x + (x - last.x);
                int ym = y + (y - last.y);
                if (xm >= 0 && xm < af.GRID_WIDTH && ym >= 0 && ym < af.GRID_HEIGHT && af.obstacles.field[xm][ym] == 0)
                    { max_x = xm; max_y = ym; }
                }
            }
        else if (state.random.nextBoolean(af.randomActionProbability))  // Maybe go randomly
            {
            int xd = (state.random.nextInt(3) - 1);
            int yd = (state.random.nextInt(3) - 1);
            int xm = x + xd;
            int ym = y + yd;
            if (!(xd == 0 && yd == 0) && xm >= 0 && xm < af.GRID_WIDTH && ym >= 0 && ym < af.GRID_HEIGHT && af.obstacles.field[xm][ym] == 0)
                { max_x = xm; max_y = ym; }
            }
        af.buggrid.setDistributedObjectLocation(new Int2D(max_x, max_y), this, state);      
        //af.buggrid.setObjectLocation(this, new Int2D(max_x, max_y));
        if (af.sites.field[max_x][max_y] == af.HOME)  // reward me next time!  And change my status
            { reward = af.reward ; hasFoodItem = ! hasFoodItem; }
        }
    else
        {
        double max = af.IMPOSSIBLY_BAD_PHEROMONE;
        int max_x = x;
        int max_y = y;
        int count = 2;
        for(int dx = -1; dx < 2; dx++)
            for(int dy = -1; dy < 2; dy++)
                {
                int _x = dx+x;
                int _y = dy+y;
                if ((dx == 0 && dy == 0) ||
                    _x < 0 || _y < 0 ||
                    _x >= af.GRID_WIDTH || _y >= af.GRID_HEIGHT || 
                    af.obstacles.field[_x][_y] == 1) continue;  // nothing to see here
                
                double m = af.toFoodGrid.field[_x][_y];
                if (m > max)
                    {
                    count = 2;
                    }
                //no else, yes m > max is repeated
                if (m > max || (m == max && state.random.nextBoolean(1.0 / count++)))  // this little magic makes all "==" situations equally likely
                    {
                    max = m;
                    max_x = _x;
                    max_y = _y;
                    }
                }
        if (max == 0 && last != null)  // nowhere to go!  Maybe go straight
            {
            if (state.random.nextBoolean(af.momentumProbability))
                {
                int xm = x + (x - last.x);
                int ym = y + (y - last.y);
                if (xm >= 0 && xm < af.GRID_WIDTH && ym >= 0 && ym < af.GRID_HEIGHT && af.obstacles.field[xm][ym] == 0)
                    { max_x = xm; max_y = ym; }
                }
            }
        else if (state.random.nextBoolean(af.randomActionProbability))  // Maybe go randomly
            {
            int xd = (state.random.nextInt(3) - 1);
            int yd = (state.random.nextInt(3) - 1);
            int xm = x + xd;
            int ym = y + yd;
            if (!(xd == 0 && yd == 0) && xm >= 0 && xm < af.GRID_WIDTH && ym >= 0 && ym < af.GRID_HEIGHT && af.obstacles.field[xm][ym] == 0)
                { max_x = xm; max_y = ym; }
            }
        //af.buggrid.setObjectLocation(this, new Int2D(max_x, max_y));
        af.buggrid.setDistributedObjectLocation(new Int2D(max_x, max_y), this, state);      
        if (af.sites.field[max_x][max_y] == af.FOOD)  // reward me next time!  And change my status
            { reward = af.reward; hasFoodItem = ! hasFoodItem; }
        }
    last = location;
    }

// a few tweaks by Sean
private Color noFoodColor = Color.black;
private Color foodColor = Color.red;
public final void draw(Object object, Graphics2D graphics, DrawInfo2D info)
    {
    if( hasFoodItem )
        graphics.setColor( foodColor );
    else
        graphics.setColor( noFoodColor );

    // this code was stolen from OvalPortrayal2D
    int x = (int)(info.draw.x - info.draw.width / 2.0);
    int y = (int)(info.draw.y - info.draw.height / 2.0);
    int width = (int)(info.draw.width);
    int height = (int)(info.draw.height);
    graphics.fillOval(x,y,width, height);

    }

}

