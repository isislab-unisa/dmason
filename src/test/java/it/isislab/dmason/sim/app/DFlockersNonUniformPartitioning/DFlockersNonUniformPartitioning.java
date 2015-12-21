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
package it.isislab.dmason.sim.app.DFlockersNonUniformPartitioning;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import it.isislab.dmason.annotation.BatchAnnotation;
import it.isislab.dmason.experimentals.tools.batch.data.EntryParam;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.continuous.DContinuousGrid2D;
import it.isislab.dmason.sim.field.continuous.DContinuousGrid2DFactory;
import sim.engine.SimState;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.simple.AdjustablePortrayal2D;
import sim.portrayal.simple.MovablePortrayal2D;
import sim.portrayal.simple.OrientedPortrayal2D;
import sim.util.Double2D;
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
public class DFlockersNonUniformPartitioning extends DistributedState<Double2D>
{
	ContinuousPortrayal2D p;
	public DFlockersNonUniformPartitioning()
	{
		super();
	}

	public long debug_lastStep = -10;
	public int debug_numAgent = 0;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public DContinuousGrid2D flockers;
	private static boolean isToroidal=true;

	@BatchAnnotation(
			domain = "100-300",
			suggestedValue = "250"
			)
	public double width = 150;
	@BatchAnnotation
	public double height = 150;
	@BatchAnnotation
	public int numFlockers = 20;
	@BatchAnnotation
	public double cohesion = 1.0;
	@BatchAnnotation
	public double avoidance = 1.0;
	@BatchAnnotation
	public double randomness = 1.0;
	@BatchAnnotation
	public double consistency = 1.0;
	@BatchAnnotation
	public double momentum = 1.0;
	@BatchAnnotation
	public double deadFlockerProbability = 0.1;
	@BatchAnnotation
	public double neighborhood = 10;

	public double jump = 0.7;  // how far do we move in a timestep?

	public double getCohesion() { return cohesion; }
	public void setCohesion(double val) { if (val >= 0.0) cohesion = val; }
	public double getAvoidance() { return avoidance; }
	public void setAvoidance(double val) { if (val >= 0.0) avoidance = val; }
	public double getRandomness() { return randomness; }
	public void setRandomness(double val) { if (val >= 0.0) randomness = val; }
	public double getConsistency() { return consistency; }
	public void setConsistency(double val) { if (val >= 0.0) consistency = val; }
	public double getMomentum() { return momentum; }
	public void setMomentum(double val) { if (val >= 0.0) momentum = val; }
	public int getNumFlockers() { return numFlockers; }
	public void setNumFlockers(int val) { if (val >= 1) numFlockers = val; }
	public double getWidth() { return width; }
	public void setWidth(double val) { if (val > 0) width = val; }
	public double getHeight() { return height; }
	public void setHeight(double val) { if (val > 0) height = val; }
	public double getNeighborhood() { return neighborhood; }
	public void setNeighborhood(double val) { if (val > 0) neighborhood = val; }
	public double getDeadFlockerProbability() { return deadFlockerProbability; }
	public void setDeadFlockerProbability(double val) { if (val >= 0.0 && val <= 1.0) deadFlockerProbability = val; }

	public double gridWidth ;
	public double gridHeight ;   
	public int MODE;

	public static String topicPrefix = "";

	// -----------------------------------------------------------------------
	// DEBUG -----------------------------------------------------------------
	// -----------------------------------------------------------------------
	private boolean  checkAgentDuplication = false;
	private FileOutputStream file = null;
	private PrintStream ps = null;


	//    int localTest = 1; int globalTest = -1;
	//    public int getTest() { return localTest; }
	//    public void setTest(int value) { localTest = value; }
	//    public boolean globalTest() { return true; }
	//    public int getGlobalTest() { return globalTest; }
	//    public void setGlobalTest(Object value) { globalTest = (Integer)value; }
	//    public Integer reduceTest(Object[] shard) {
	//    	int globalTest = 0;
	//    	for (int i = 0; i < shard.length; i++) globalTest +=  ((Integer)shard[i]).intValue();
	//    	return globalTest;
	//    } 

	private int P;
	public DFlockersNonUniformPartitioning(GeneralParam params)
	{    	
		super(params,new DistributedMultiSchedule<Double2D>(params.getNumAgents(),params.getP(),params.getWidth(),params.getHeight(),params.getMaxDistance()), "",params.getConnectionType());
		this.MODE=params.getMode();
		gridWidth=params.getWidth();
		gridHeight=params.getHeight();
		if(checkAgentDuplication)
		{
			try {
				file = new FileOutputStream("0) "+super.TYPE+".txt");
				ps = new PrintStream(file);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		P=params.getP();
	}

	public DFlockersNonUniformPartitioning(GeneralParam params,List<EntryParam<String, Object>> simParams, String prefix)
	{    	
		super(params,new DistributedMultiSchedule<Double2D>(params.getNumAgents(),params.getP(),params.getWidth(),params.getHeight(),params.getMaxDistance()), prefix,params.getConnectionType());
		this.MODE=params.getMode();
		gridWidth=params.getWidth();
		gridHeight=params.getHeight();
		topicPrefix = prefix; 
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

		if(checkAgentDuplication)
		{
			try {
				file = new FileOutputStream("0) "+super.TYPE+".txt");
				ps = new PrintStream(file);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}


	@Override
	public void start()
	{
		super.start();


		flockers = DContinuousGrid2DFactory.createDContinuous2DNonUniform(neighborhood/1.5, gridWidth, gridHeight, this, super.MAX_DISTANCE, TYPE.pos_j, P, MODE, "flcokers", topicPrefix, true);
	
		init_connection();

		generateNonUniformAgentsDistribution(this.NUMAGENTS, P, gridWidth, gridHeight, gridWidth/P,1);


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

		DFlockerNonUniformPartitioning f=new DFlockerNonUniformPartitioning(this,new Double2D(0,0));
		for(int i=0;i< (P*unbalace);i++)
		{
			for(int x=0;x<this.NUMAGENTS/(P*unbalace);x++)
			{
				Color c=new Color(
						128 + this.random.nextInt(128),
						128 + this.random.nextInt(128),
						128 + this.random.nextInt(128));
				f.setColor(c);
				double xvalue=(r.nextDouble()*neighborhood);
				double yvalue=(r.nextDouble()*neighborhood);

				double px=processors.get(i).x+xvalue<=0?width-xvalue:(processors.get(i).x+xvalue>width?xvalue:processors.get(i).x+xvalue);
				double py=processors.get(i).y+yvalue<=0?height-yvalue:(processors.get(i).y+yvalue>height?yvalue:processors.get(i).y+yvalue);

				if(px<0 || px > width || py < 0 || py> height)
				{
					System.out.println("Error location generated"+px+" "+py);
				}

				Double2D location = new Double2D(px,py);

				f.setPos(location);

				if(!((DistributedMultiSchedule)schedule).scheduleOnceNonUniform(f, f.pos.x, f.pos.y,flockers,location))
				{
					System.err.println("error position");
				}
				flockers.setObjectLocation(f, location);
				f=new DFlockerNonUniformPartitioning(this,new Double2D(0,0));

			}
		}

	}

	public static void main(String[] args)
	{
		doLoop(DFlockersNonUniformPartitioning.class, args);
		System.exit(0);
	}

	@Override
	public DistributedField2D getField() 
	{
		return flockers;
	}

	@Override
	public void addToField(RemotePositionedAgent rm, Double2D loc) 
	{
		flockers.setObjectLocation(rm,loc);
		
		setPortrayalForObject(rm);

	}

	@Override
	public SimState getState() 
	{
		return this;
	}


	public boolean setPortrayalForObject(Object o) 
	{
		if(p!=null)
		{
			DFlockerNonUniformPartitioning f=(DFlockerNonUniformPartitioning)o;
			SimplePortrayal2D pp = new AdjustablePortrayal2D(new MovablePortrayal2D(new OrientedPortrayal2D(new SimplePortrayal2D(),0,4.0,
					f.getColor(),
					OrientedPortrayal2D.SHAPE_COMPASS)));
			p.setPortrayalForObject(o, pp);
			
			return true;
		}
		return false;
	}    
}