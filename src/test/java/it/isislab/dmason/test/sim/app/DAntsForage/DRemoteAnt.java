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
package it.isislab.dmason.test.sim.app.DAntsForage;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.engine.DistributedState;

import java.awt.Color;
import java.awt.Graphics2D;

import sim.engine.SimState;
import sim.portrayal.DrawInfo2D;
import sim.util.Int2D;
import sim.util.IntBag;
/**
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * @author francescomilone
 *
 */
public class DRemoteAnt extends RemoteAnt<Int2D>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public boolean getHasFoodItem() { return hasFoodItem; }
	public void setHasFoodItem(boolean val) { hasFoodItem = val; }
	public boolean hasFoodItem = false;
	double reward = 0;
	Int2D last;
	public DRemoteAnt(){}
	public DRemoteAnt(double initialReward){
	
		reward = initialReward; 
	}
    
	public DRemoteAnt(DistributedState<Int2D> state, double initialReward) 
	{ 
		super(state);
		reward = initialReward; 
	}
	
	@Override
	public void step(SimState state)
    {
		
		depositPheromone(state);
		act(state);
    }
        
	// at present we have only one algorithm: value iteration.  I might
	// revise this and add our alternate (TD) algorithm.  See the papers.
	public void depositPheromone( final SimState state)
    {
		final DAntsForage af = (DAntsForage)state;

		Int2D location = af.buggrid.getObjectLocation(this);
		int x = location.x;
		int y = location.y;
		
		if (DAntsForage.ALGORITHM == DAntsForage.ALGORITHM_VALUE_ITERATION)
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

						/*if((af.toFoodGrid.own_x<= x) && (x<(af.toFoodGrid.own_x+af.toFoodGrid.my_width)) && 
								(af.toFoodGrid.own_y<=y) && (y<(af.toFoodGrid.own_y+af.toFoodGrid.my_height))){*/

							if (_x < 0 || _y < 0 || _x >= af.GRID_WIDTH || _y >= af.GRID_HEIGHT) continue;  // nothing to see here
							double m = af.toFoodGrid.field[_x][_y] * 
							(dx * dy != 0 ? // diagonal corners
									af.diagonalCutDown : af.updateCutDown) +
									reward;
							if (m > max) max = m;
						}

					//}
				//af.toFoodGrid.field[x][y] = max;
				try {
					af.toFoodGrid.setDistributedObjectLocation(new Int2D(x, y),max, state);
				} catch (DMasonException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}     
			}
			else
			{
				double max = af.toHomeGrid.field[x][y];
				for(int dx = -1; dx < 2; dx++)
					for(int dy = -1; dy < 2; dy++)
					{
						int _x = dx+x;
						int _y = dy+y;

						/*if((af.toHomeGrid.own_x<= x) && (x<(af.toHomeGrid.own_x+af.toHomeGrid.my_width)) && 
								(af.toHomeGrid.own_y<=y) && (y<(af.toHomeGrid.own_y+af.toHomeGrid.my_height))){*/

							if (_x < 0 || _y < 0 || _x >= af.GRID_WIDTH || _y >= af.GRID_HEIGHT) continue;  // nothing to see here
							double m = af.toHomeGrid.field[_x][_y] * 
							(dx * dy != 0 ? // diagonal corners
									af.diagonalCutDown : af.updateCutDown) +
									reward;
							if (m > max) max = m;
						}
					//}
				//af.toHomeGrid.field[x][y] = max;
				try {
					af.toHomeGrid.setDistributedObjectLocation(new Int2D(x, y),max, state);
				} catch (DMasonException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		reward = 0.0;
    }

	public void act( final SimState state )
	{
		final DAntsForage af = (DAntsForage)state;

		Int2D location = af.buggrid.getObjectLocation(this);
		int x = location.x;
		int y = location.y;    

		af.buggrid.getNeighborsMaxDistance(x, y, af.buggrid.MAX_DISTANCE, false, new IntBag(), new IntBag());

		if (hasFoodItem)  // follow home pheromone
		{
			double max = DAntsForage.IMPOSSIBLY_BAD_PHEROMONE;
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
			try {
				af.buggrid.setDistributedObjectLocation(new Int2D(max_x, max_y), this, state);
			} catch (DMasonException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}      
			//af.buggrid.setObjectLocation(this, new Int2D(max_x, max_y));
			if (af.sites.field[max_x][max_y] == DAntsForage.HOME)  // reward me next time!  And change my status
			{ reward = af.reward ; hasFoodItem = ! hasFoodItem; 
			if(!af.backHome){

				try {
					if(af.getTrigger()!=null)
						af.getTrigger().publishToTriggerTopic("Step: "+((DistributedState)state).schedule.getSteps()+"- I'm back at home!");
				} catch (Exception e) {
					e.printStackTrace();
				}

				af.backHome = true;
			}
			}
		}
		else
		{
			double max = DAntsForage.IMPOSSIBLY_BAD_PHEROMONE;
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
			try {
				af.buggrid.setDistributedObjectLocation(new Int2D(max_x, max_y), this, state);
			} catch (DMasonException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}      
			if (af.sites.field[max_x][max_y] == DAntsForage.FOOD)  // reward me next time!  And change my status
			{ reward = af.reward; hasFoodItem = ! hasFoodItem; 

			if(!af.foodFounded){

				try {

					af.getTrigger().publishToTriggerTopic("Step: "+((DistributedState)state).schedule.getSteps()+" - Food Founded!");
				} catch (Exception e) {
					e.printStackTrace();
				}

				af.foodFounded = true;
			}
			}
		}
		last = location;

	}

	// a few tweaks by Sean
	private Color noFoodColor = Color.black;
	private Color foodColor = Color.red;
	
	@Override
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