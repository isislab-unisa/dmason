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
package it.isislab.dmason.experimentals.util.visualization.sim.app.DParticles;

import it.isislab.dmason.experimentals.util.visualization.zoomviewerapp.ZoomViewer;
import it.isislab.dmason.util.connection.jms.activemq.ConnectionNFieldsWithActiveMQAPI;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.SparseGrid2D;
/**
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 */
public class Tutorial3View extends SimState 
{
	public DoubleGrid2D trails;
	public SparseGrid2D particles;

	public int gridWidth;
	public int gridHeight;
	
	public int numCell;
	public int mode;
	private boolean isSynchro;
    
    public Tutorial3View(long seed)
    {
        super(seed); 
    }
    
	public Tutorial3View(Object[] args)
	{
		 super(1);
		 con=(ConnectionNFieldsWithActiveMQAPI)args[0];
		 id_Cell=(String)args[1];
		 isSynchro=(Boolean)args[2];
		 this.numCell = (Integer)args[3];
		 int wh = (Integer)args[4];//width
		 int ht = (Integer)args[5];//height
		 this.mode = (Integer)args[6];

		 gridWidth = ZoomViewer.getCellWidth(mode, wh, numCell);
		 gridHeight = ZoomViewer.getCellHeight(mode, ht, numCell);
	}

    @Override
	public void start()
	{
        super.start();
        trails = new DoubleGrid2D(gridWidth, gridHeight);
        particles = new SparseGrid2D(gridWidth, gridHeight);
      
        // Schedule the decreaser
        Steppable decreaser = new Steppable()
            {
            @Override
			public void step(SimState state)
                {
                // decrease the trails
                trails.multiply(0.9);
                }
            static final long serialVersionUID = 6330208160095250478L;
            };
            
        schedule.scheduleRepeating(Schedule.EPOCH,2,decreaser,1);
        
        //View Zoom in Central GUI
    	ZoomViewer zoom;
		try {
			zoom = new ZoomViewer(con,id_Cell,isSynchro,numCell,gridWidth,gridHeight,mode);
	       	//in according order
        	zoom.registerField("particles",particles);
        	zoom.registerField("trails",trails);
        	
        	schedule.scheduleRepeating(new DParticlesAgentUpdate(zoom));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

    public ConnectionNFieldsWithActiveMQAPI con;
	public String id_Cell;
    
    public static void main(String[] args)
        {
        doLoop(Tutorial3View.class, args);
        System.exit(0);
        }    

    static final long serialVersionUID = 9115981605874680023L;

    }
