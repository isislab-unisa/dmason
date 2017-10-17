/**

 * Copyright 2016 Universita' degli Studi di Salerno


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
package it.isislab.dmason.sim.app.GameOfLife;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.engine.DistributedState;
import sim.engine.SimState;
import sim.util.Int2D;

public class DCellAgent extends RemoteCellAgent<Int2D> {

	private static final long serialVersionUID = 1L;

	public DCellAgent() {
	}

	public DCellAgent(DistributedState<Int2D> sm, Int2D location) {
		pos = location;
	}

	@Override
	public void step(SimState state) {
        
		
		DGameOfLife tut = (DGameOfLife) state;
		int count = 0;
		int width = tut.grid.my_width;
		int height = tut.grid.my_height;

		for (int y = tut.grid.own_y; y < tut.grid.own_y + height; y++) {
			for (int x = tut.grid.own_x; x < tut.grid.own_x + width; x++) {

				count = 0;
				/*
				 *  count the number of neighbors around the cell,
				 *  and for good measure include the cell itself
				 */
				
				for (int dx = -1; dx < 2; dx++)
					for (int dy = -1; dy < 2; dy++)
						count += isAlive(tut.schedule.getSteps(),
								tut.grid.field[tut.grid.stx(x + dx)][tut.grid.sty(y + dy)]);

				/* if the count is 2 or less, or 5 or higher, the cell dies
				 *  else if the count is 3 exactly, a dead cell becomes live
				 *  again
				 *  else the cell stays as it is
				 */ 
				
				try {
					if (count <= 2 || count >= 5) // dead
						tut.grid.setDistributedObjectLocation(new Int2D(x, y),
								setDie(tut.schedule.getSteps(), tut.grid.field[x][y]), tut);
					else if (count == 3) // life
						tut.grid.setDistributedObjectLocation(new Int2D(x, y),
								setAlive(tut.schedule.getSteps(), tut.grid.field[x][y]), tut);
					// state not changed
					else if (isAlive(tut.schedule.getSteps(), tut.grid.field[x][y]) == 1)
						tut.grid.setDistributedObjectLocation(new Int2D(x, y),
								setAlive(tut.schedule.getSteps(), tut.grid.field[x][y]), tut);
					else
						tut.grid.setDistributedObjectLocation(new Int2D(x, y),
								setDie(tut.schedule.getSteps(), tut.grid.field[x][y]), tut);
				} catch (DMasonException e) {
					e.printStackTrace();
				}
			}
		}

		try {
			tut.core.setDistributedObjectLocation(pos, this, tut);
		} catch (DMasonException e) {
			e.printStackTrace();
		}
	}


	public int isAlive(long step, int val) {
		return val;
	}

	public int setAlive(long step, int val) {
		return 1;
	}

	public int setDie(long step, int val) {
		return 0;
	}

}
