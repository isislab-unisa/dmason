/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package dmason.util.visualization.DParticles;

import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;
import dmason.util.visualization.ZoomViewer;
import dmason.util.visualization.DAntsForage.DAntsAgentUpdate;
import sim.engine.*;


import sim.field.grid.*;
import sim.util.*;


public class Tutorial3View extends SimState 
{
	public DoubleGrid2D trails;
	public SparseGrid2D particles;

	public int gridWidth = 100;
	public int gridHeight = 100;
	
	private boolean isSynchro;
    
    public Tutorial3View(long seed)
    {
        super(seed);
    }

    public void start()
	{
        super.start();
        trails = new DoubleGrid2D(gridWidth, gridHeight);
        particles = new SparseGrid2D(gridWidth, gridHeight);
      
        // Schedule the decreaser
        Steppable decreaser = new Steppable()
            {
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
			zoom = new ZoomViewer(con,id_Cell, isSynchro);
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
	
	public Tutorial3View(Object[] args)
	{
		 super(1);
		 con=(ConnectionNFieldsWithActiveMQAPI)args[0];
		 id_Cell=(String)args[1];
		 isSynchro=(Boolean)args[1];
	}
    
    public static void main(String[] args)
        {
        doLoop(Tutorial3View.class, args);
        System.exit(0);
        }    

    static final long serialVersionUID = 9115981605874680023L;

    }
