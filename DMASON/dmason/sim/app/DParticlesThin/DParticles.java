/**
 * Copyright 2012 Università degli Studi di Salerno


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

package dmason.sim.app.DParticlesThin;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import dmason.annotation.Thin;
import dmason.batch.data.GeneralParam;
import dmason.sim.engine.DistributedMultiSchedule;
import dmason.sim.engine.DistributedState;
import dmason.sim.engine.RemoteAgent;
import dmason.sim.field.DistributedField;
import dmason.sim.field.grid.DSparseGrid2D;
import dmason.sim.field.grid.DSparseGrid2DFactory;
import dmason.sim.field.grid.DSparseGrid2DThin;
import dmason.sim.field.grid.numeric.DDoubleGrid2D;
import dmason.sim.field.grid.numeric.DDoubleGrid2DFactory;
import dmason.sim.field.grid.numeric.DDoubleGrid2DThin;
import dmason.util.exception.DMasonException;
import sim.engine.*;
import sim.field.grid.DoubleGrid2D;
import sim.util.*;
@Thin
public class DParticles extends DistributedState<Int2D> {
    
	private static boolean isToroidal=false;
    public DSparseGrid2DThin particles;
    public DDoubleGrid2DThin trails;
    //public DoubleGrid2D trails;

    public int gridWidth ;
    public int gridHeight ;   
    public int MODE;
   
    public static String topicPrefix = "";
    
   // public ArrayList<RemoteAgent<Int2D>> buffer_print=new ArrayList<RemoteAgent<Int2D>>();
    //public PrintWriter printer;
  
  
    public DParticles(GeneralParam params)
    {    	
    	super(params.getMaxDistance(),params.getRows(), params.getColumns(),params.getNumAgents(),params.getI(),
    			params.getJ(),params.getIp(),params.getPort(),params.getMode(),
    			isToroidal,new DistributedMultiSchedule<Int2D>(),topicPrefix);
    	ip = params.getIp();
    	port = params.getPort();
    	this.MODE=params.getMode();
    	gridWidth=params.getWidth();
    	gridHeight=params.getHeight();
    	//((DistributedMultiSchedule)schedule).setThresholdMerge(1);
        //((DistributedMultiSchedule)schedule).setThresholdSplit(5);

    } 
    
    /*public DParticles(Object[] params)
    {    	
    	super((Integer)params[2],(Integer)params[3],(Integer)params[4],(Integer)params[7],
    			(Integer)params[8],(String)params[0],(String)params[1],(Integer)params[9],
    			isToroidal,new DistributedMultiSchedule<Int2D>());
    	ip = params[0]+"";
    	port = params[1]+"";
    	this.MODE=(Integer)params[9];
    	gridWidth=(Integer)params[5];
    	gridHeight=(Integer)params[6];
    	((DistributedMultiSchedule)schedule).setThresholdMerge(1);
        ((DistributedMultiSchedule)schedule).setThresholdSplit(5);

    } */   

    public void start()
    {
    	super.start();

        try 
        {
			//trails = new DoubleGrid2D(gridWidth, gridHeight);
        	trails = DDoubleGrid2DFactory.createDDoubleGrid2DThin(gridWidth, gridHeight,this,super.MAX_DISTANCE,TYPE.pos_i,TYPE.pos_j,super.rows,super.columns,MODE,0,false,"trails",topicPrefix);
			particles = DSparseGrid2DFactory.createDSparseGrid2DThin(gridWidth, gridHeight,this,super.MAX_DISTANCE,TYPE.pos_i,TYPE.pos_j,super.rows,super.columns,MODE, "particles", topicPrefix);
		    init_connection();
		   // String curDir = System.getProperty("user.dir");
			//printer=new PrintWriter(new FileOutputStream(curDir+"/testTOTAgents.txt",true));
        }catch (DMasonException e) { e.printStackTrace();}
        //catch (FileNotFoundException e) {e.printStackTrace();}

        DParticle p=new DParticle(this);
        
        while(particles.size() != super.NUMAGENTS)
        {		
        	particles.setAvailableRandomLocation(p);
           
            p.xdir = random.nextInt(3)-1;
            p.ydir = random.nextInt(3)-1;
            
           	if(particles.setObjectLocationThin(p, new Int2D(p.pos.getX(),p.pos.getY())))
           		//if(particles.setDistributedObjectLocationForPeer(new Int2D(p.pos.getX(),p.pos.getY()), p, this))
           	{		
           		schedule.scheduleOnce(schedule.getTime()+1.0,p);
           		//buffer_print.add(p);
           		if(particles.size() != super.NUMAGENTS)
           			p=new DParticle(this);
           	}
        }
        
      /*  for(RemoteAgent<Int2D> r : buffer_print)
		{
			printer.println(r.getId());
			printer.flush();
		}*/
        // Schedule the decreaser
        Steppable decreaser = new Steppable()
        {
            public void step(SimState state)
            {
            	trails.multiplyThin(0.9);
            }
            static final long serialVersionUID = 6330208160095250478L;
        };
            
        schedule.scheduleRepeating(Schedule.EPOCH,2,decreaser,1);
 
    	try {
			getTrigger().publishToTriggerTopic("Simulation cell "+particles.cellType+" ready...");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public static void main(String[] args)
    {
        doLoop(DParticles.class, args);
        System.exit(0);
    }    

    static final long serialVersionUID = 9115981605874680023L;
	
	public DistributedField getField() 
	{
		return particles;
	}

	public SimState getState() 
	{
		return this;
	}

	public void addToField(RemoteAgent<Int2D> rm,Int2D loc) 
	{		
		particles.setObjectLocationThin(rm, loc);
	}

	public boolean setPortrayalForObject(Object o) 
	{
		if(particles.p!=null)
		  {
			  particles.p.setPortrayalForObject(o, new sim.portrayal.simple.OvalPortrayal2D(Color.YELLOW) );
		    return true;
		  }
		return false;
	}    
	   
}