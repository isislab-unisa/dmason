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
package it.isislab.dmason.sim.app.DParticlesNonUniformPartitionig;

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
import java.util.Random;

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
public class DParticlesNonUniformPartitioning extends DistributedState<Int2D> {
    
	
    protected DSparseGrid2D particles;
    protected DDoubleGrid2D trails;
    protected SparseGridPortrayal2D p;
    
    
    public int gridWidth ;
    public int gridHeight ;   
    public int MODE;
	
   
    private String topicPrefix = "";
    
    private int P;
    public DParticlesNonUniformPartitioning() {
		super();
	
	}
    
    public DParticlesNonUniformPartitioning(GeneralParam params, String prefix)
    {    	
    	super(params,new DistributedMultiSchedule<Int2D>(params.getNumAgents(),params.getP(),params.getWidth(),params.getHeight(),params.getAoi()),prefix,params.getConnectionType());
    	this.MODE=params.getMode();
    	this.topicPrefix=prefix;
    	gridWidth=params.getWidth();
    	gridHeight=params.getHeight();
    	P=params.getP();
    } 
    
    /**
	 * DParticles
	 * 
	 * @param params general in order to build a distributed field
	 * @param list   list parameters for this simulation 
	 * @param prefix a string added to topic to generate an unique topic for this simulation 
	 */
    public DParticlesNonUniformPartitioning(GeneralParam params,List<EntryParam<String,Object>>simParams,String prefix)
    {    	
    	super(params,new DistributedMultiSchedule<Int2D>(params.getNumAgents(),params.getP(),params.getWidth(),params.getHeight(),params.getAoi()),prefix,params.getConnectionType());
    	this.MODE=params.getMode();
    	gridWidth=params.getWidth();
    	gridHeight=params.getHeight();
    	topicPrefix=prefix;
    	P=params.getP();
    	for (EntryParam<String, Object> entryParam : simParams) {

			try {
				this.getClass().getDeclaredField(entryParam.getParamName()).set(this, entryParam.getParamValue());
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		for (EntryParam<String, Object> entryParam : simParams) {

			try {
				System.out.println(this.getClass().getDeclaredField(entryParam.getParamName()).get(this));
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

    
    }
    
    

    @Override
	public void start()
    {
    	super.start();

        try 
        {
        	particles = DSparseGrid2DFactory.createDSparseGrid2DNonUniform(gridWidth, gridHeight,this,super.AOI,TYPE.pos_j,P,MODE, "particles", topicPrefix,false);
        	trails = DDoubleGrid2DFactory.createDDoubleGrid2DNonUniform(gridWidth, gridHeight,this,super.AOI,TYPE.pos_j,P,MODE, 0, false,"trails", topicPrefix,false);
		    init_connection();
		    generateNonUniformAgentsDistribution(this.NUMAGENTS, P, gridWidth, gridHeight, gridWidth/P,1);
		  
        }catch (DMasonException e) { e.printStackTrace();}
      
    }
    
    private void generateNonUniformAgentsDistribution(int num_agents,int P,double width,double height,double dispersion,int unbalace) {
		class Processor{
			double x;
			double y;
			public Processor(double x, double y)
			{
				this.x=x;
				this.y=y;
			}
		}
		Random r=new Random(0);
		ArrayList<Processor> processors=new ArrayList<Processor>();

		for(int i=0;i< (P*unbalace);i++)
			processors.add(new Processor(r.nextDouble()*width, r.nextDouble()*height));

		DParticleNonUniformPartitioning p=new DParticleNonUniformPartitioning(this,new Int2D(0,0));
		for(int i=0;i< (P*unbalace);i++)
		{
			for(int x=0;x<this.NUMAGENTS/(P*unbalace);x++)
			{
				p.setPos(particles.getAvailableRandomLocation());
			       
		        p.xdir = random.nextInt(3)-1;
		        p.ydir = random.nextInt(3)-1;
		        
		        Int2D loc = new Int2D(p.pos.getX(),p.pos.getY());
		        
		       
		        if(!((DistributedMultiSchedule)schedule).scheduleOnceNonUniform(p, p.pos.x, p.pos.y,particles,loc))
				{
					System.err.println("error position");
				}	
     	
		    	particles.setObjectLocation(p, loc);
		    	p=new DParticleNonUniformPartitioning(this,new Int2D(0,0));
		    
		    	
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
    
    /*DParticleNonUniformPartitioning p=new DParticleNonUniformPartitioning(this,new Int2D(0,0));
    
    while(particles.size() != super.NUMAGENTS)
    {		
    	p.setPos(particles.getAvailableRandomLocation());
       
        p.xdir = random.nextInt(3)-1;
        p.ydir = random.nextInt(3)-1;
        
    
        
       	if(particles.setObjectLocation(p, new Int2D(p.pos.getX(),p.pos.getY())))
       	{		
       		schedule.scheduleOnce(schedule.getTime()+1.0,p);
       		
       		if(particles.size() != super.NUMAGENTS)
       			p=new DParticleNonUniformPartitioning(this,new Int2D(0,0));
       	}
    }
    
 
    // Schedule the decreaser
    Steppable decreaser = new Steppable()
    {
        @Override
		public void step(SimState state)
        {
//        	flavio
//        	trails.multiply(0.9);
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
	}*/

    public static void main(String[] args)
    {
        doLoop(DParticlesNonUniformPartitioning.class, args);
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