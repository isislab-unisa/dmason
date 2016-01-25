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
package it.isislab.dmason.sim.app.DParticles;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.experimentals.tools.batch.data.EntryParam;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.grid.numeric.DDoubleGrid2D;
import it.isislab.dmason.sim.field.grid.numeric.DDoubleGrid2DFactory;
import it.isislab.dmason.sim.field.grid.sparse.DSparseGrid2D;
import it.isislab.dmason.sim.field.grid.sparse.DSparseGrid2DFactory;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.util.Int2D;

/**
 * 
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 *
 */
public class DParticles extends DistributedState<Int2D> {
    
	
    public DSparseGrid2D particles;
    public DDoubleGrid2D trails;

    public int gridWidth ;
    public int gridHeight ;   
    public int MODE;
	public SparseGridPortrayal2D p;
   
    public static String topicPrefix = "";
    

    public DParticles() {
		super();
	
	}
    
    public DParticles(GeneralParam params)
    {    	
    	super(params,new DistributedMultiSchedule<Int2D>(),topicPrefix,params.getConnectionType());
    	this.MODE=params.getMode();
    	gridWidth=params.getWidth();
    	gridHeight=params.getHeight();
    
    } 
    
    public DParticles(GeneralParam params,List<EntryParam<String,Object>>list,String prefix)
    {    	
    	super(params,new DistributedMultiSchedule<Int2D>(),prefix,params.getConnectionType());
    	this.MODE=params.getMode();
    	gridWidth=params.getWidth();
    	gridHeight=params.getHeight();
    	topicPrefix=prefix;
    
    }
    
    

    @Override
	public void start()
    {
    	super.start();

        try 
        {
        	
        	particles = DSparseGrid2DFactory.createDSparseGrid2D(gridWidth, gridHeight,this,super.AOI,TYPE.pos_i,TYPE.pos_j,super.rows,super.columns,MODE, "particles", topicPrefix,false);
        	trails = DDoubleGrid2DFactory.createDDoubleGrid2D(gridWidth, gridHeight,this,super.AOI,TYPE.pos_i,TYPE.pos_j,super.rows,super.columns,MODE,0,false,"trails",topicPrefix,false);
			
		    init_connection();
		  
        }catch (DMasonException e) { e.printStackTrace();}
      

        DParticle p=new DParticle(this);
        
        while(particles.size() != super.NUMAGENTS)
        {		
        	p.setPos(particles.getAvailableRandomLocation());
           
            p.xdir = random.nextInt(3)-1;
            p.ydir = random.nextInt(3)-1;
            
        
            
           	if(particles.setObjectLocation(p, new Int2D(p.pos.getX(),p.pos.getY())))
           	{		
           		schedule.scheduleOnce(schedule.getTime()+1.0,p);
           		
           		if(particles.size() != super.NUMAGENTS)
           			p=new DParticle(this);
           	}
        }
        
     
        // Schedule the decreaser
        Steppable decreaser = new Steppable()
        {
            @Override
			public void step(SimState state)
            {
            	trails.multiply(0.9);
            }
            static final long serialVersionUID = 6330208160095250478L;
        };
            
        schedule.scheduleRepeating(Schedule.EPOCH,2,decreaser,1);
 
    	try {
    		if(getTrigger()!=null)
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
	
	@Override
	public DistributedField2D getField() 
	{
		return particles;
	}

	@Override
	public SimState getState() 
	{
		return this;
	}

	@Override
	public void addToField(RemotePositionedAgent<Int2D> rm,Int2D loc) 
	{		
		particles.setObjectLocation(rm, loc);
	}

	
	public boolean setPortrayalForObject(Object o) 
	{
		if(p!=null)
		  {
			 p.setPortrayalForObject(o, new sim.portrayal.simple.OvalPortrayal2D(Color.YELLOW) );
		    return true;
		  }
		return false;
	}    
	   
}