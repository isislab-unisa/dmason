package it.isislab.dmason.sim.app.SociallyDamagingBehavior;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.engine.DistributedState;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Orientable2D;
import sim.util.Bag;
import sim.util.Double2D;

public class DHuman extends RemoteHuman<Double2D> implements Steppable, Orientable2D
{

	public Double2D lastd = new Double2D(0,0);
	public Color behav_color;
	public DBehaviour behavior;
	public double dx;
	public double dy;
	
	/*SDB*/
	public ArrayDeque<PastData> agentPast;
	public double fitness;
	public double dna;
	public double ce = 0.0;
	public double cei = 0.0;	
	public double tpi = 0.0;
	public boolean honestAction;
	
	//Model 2-3
	public double neighFitness;
	//Model 2-3
	
	//Model 4-5
	public double min_aoi_aggregation = 0;
	public double max_aoi_aggregation = 0;
	public static double sigma1 = 2.0;
	public static double sigma2 = 3.0;
	public double numNeighPunished = 0;
	public double numNeighDamager = 0;
	public boolean isPunished;
	public boolean isActionDishonest;
	public double punprob=0;
	//Model 4-5
	/*SDB*/
 
	public DHuman(){}
	public DHuman(DistributedState state, Double2D location) { 
		//super(new SimplePortrayal2D(), 0, 4.0,Color.GREEN,OrientedPortrayal2D.SHAPE_COMPASS);
		super(state);
		DSociallyDamagingBehavior theHuman = (DSociallyDamagingBehavior)state;
		dna = 0.0;
		agentPast = new ArrayDeque<PastData>();
		super.pos = location;
		fitness=state.random.nextInt(100);
		honestAction = false;
		isPunished = false;
		isActionDishonest = false;
		if((theHuman.getMODEL()==theHuman.MODEL3_AGGREGATION_MOVEMENT) || 
				(theHuman.getMODEL()==theHuman.MODEL4_MEMORY))
		{
			min_aoi_aggregation = theHuman.MIN_AOI_AGGREGATION_MODEL3;
			max_aoi_aggregation = theHuman.MAX_AOI_AGGREGATION_MODEL3;
		}
	}
	
	@Override
	public void step(SimState state)
	{      
		DSociallyDamagingBehavior dsdbState = (DSociallyDamagingBehavior)state;
		
		pos = dsdbState.human_being.getObjectLocation(this);

		behavior=(dna>5)?new Honest():new Dishonest();
		behav_color=(dna>5)?Color.GREEN:Color.RED;

		Bag b;
		Bag entryNeigh = new Bag();
		
		if(dsdbState.getMODEL()==dsdbState.MODEL0_RANDOM_DAMAGING)
		{
			b = getNeighbors(dsdbState);
			dx = 0;
			dy = 0;
		}
		else
			if(dsdbState.getMODEL()==dsdbState.MODEL1_PROPORTIONAL_DAMAGING)
			{
				b  = getNeighbors(dsdbState);
				dx = 0;
				dy = 0;

				if(state.schedule.getSteps()!=0)
				{
					if(dsdbState.allHumans.size()<dsdbState.numHumanBeing){
						double tot = dsdbState.totalFitness+this.fitness;
						dsdbState.allHumans.add(new EntryAgent<Double, DHuman>(tot, this));
						dsdbState.totalFitness = tot;
					}
					else
					{
						double tot = dsdbState.totalFitness+this.fitness;
						dsdbState.allHumans.add(new EntryAgent<Double, DHuman>(tot, this));
						dsdbState.allHumans.sort(new Comparator<EntryAgent<Double, DHuman>>() {
							@Override
							public int compare(EntryAgent<Double, DHuman> o1, EntryAgent<Double, DHuman> o2) {
								if(o1.getFitSum()>o2.getFitSum()) return 1;
								else if(o1.getFitSum()<o2.getFitSum()) return -1;
								return 0;
							}
						});
						dsdbState.totalFitness = tot;
						dsdbState.lastAllHumans = dsdbState.allHumans;
						dsdbState.lastTotalFitness = dsdbState.totalFitness;
						dsdbState.allHumans = new Bag();
						dsdbState.totalFitness = 0;
					}
				}
			}
			else
				if(dsdbState.getMODEL()==dsdbState.MODEL2_RANDOM_MOVEMENT)
				{
					b = getNeighbors(dsdbState);
					Double2D rand = behavior.randomness(dsdbState);
					Double2D mome = momentum();

					dx = rand.x + dsdbState.momentum * mome.x;
					dy = rand.y + dsdbState.momentum * mome.y;

					// renormalize to the given step size
					double dis = Math.sqrt(dx*dx+dy*dy);
					if (dis>0)
					{
						dx = dx / dis * dsdbState.jump;
						dy = dy / dis * dsdbState.jump;
					}

					b.sort(new Comparator<DHuman>() {
						@Override
						public int compare(DHuman o1, DHuman o2) {
							if(o1.fitness>o2.fitness) return 1;
							else if(o1.fitness<o2.fitness) return -1;
							return 0;
						}
					});

					neighFitness = 0;

					for (Object o : b) {
						DHuman h = (DHuman)o;
						neighFitness += h.fitness;
						entryNeigh.add(new EntryAgent<Double, DHuman>(neighFitness, h));
					}
				}
				else
					if(dsdbState.getMODEL()==dsdbState.MODEL3_AGGREGATION_MOVEMENT)
					{
						b = getAggregatedNeighbors(dsdbState);
						
						double valoreMedio = 0;
						double valoreMedio2 = 0;
						double varianza = 0;

						for(Object o : b)
						{
							DHuman h = (DHuman)o;
							
							valoreMedio += h.dna * (1/(double)b.size());
						
							valoreMedio2 += (h.dna*h.dna) * (1/(double)b.size());
						}
					    
						
						varianza = (valoreMedio2) - ((valoreMedio)*(valoreMedio));
						double deviazione=Math.sqrt(varianza);
						
						if((deviazione > sigma2) && (max_aoi_aggregation < dsdbState.neighborhood))
							max_aoi_aggregation += 1.0;
						else
							if((deviazione < sigma1) && (max_aoi_aggregation>min_aoi_aggregation))
								max_aoi_aggregation -= 1.0;									
						
						b.sort(new Comparator<DHuman>() {
							@Override
							public int compare(DHuman o1, DHuman o2) {
								if(o1.fitness>o2.fitness) return 1;
								else if(o1.fitness<o2.fitness) return -1;
								return 0;
							}
						});

						neighFitness = 0;

						for (Object o : b) {
							DHuman h = (DHuman)o;
							neighFitness += h.fitness;
							entryNeigh.add(new EntryAgent<Double, DHuman>(neighFitness, h));
						}
						
						Double2D avoid = behavior.avoidance(this,b,dsdbState.human_being);
						Double2D cohe = behavior.cohesion(this,b,dsdbState.human_being);
						Double2D cons = behavior.consistency(this,b,dsdbState.human_being);
						Double2D mome = momentum();
						
						dx = dsdbState.cohesion * cohe.x + dsdbState.avoidance * avoid.x + dsdbState.consistency* cons.x + dsdbState.momentum * mome.x;
						dy = dsdbState.cohesion * cohe.y + dsdbState.avoidance * avoid.y + dsdbState.consistency* cons.y + dsdbState.momentum * mome.y;
						
						// renormalize to the given step size
						double dis = Math.sqrt(dx*dx+dy*dy);
						if (dis>0)
						{
							dx = dx / dis * dsdbState.jump;
							dy = dy / dis * dsdbState.jump;
						}
					}
					else
					{
						if(agentPast.size()>9)
						{
							agentPast.removeFirst();
							agentPast.add(new PastData(numNeighPunished, numNeighDamager, dna));
						}
						else
							agentPast.add(new PastData(numNeighPunished, numNeighDamager, dna));
						
						b = getAggregatedNeighbors(dsdbState);
						numNeighPunished = 0;
						numNeighDamager = 0;
						double valoreMedio = 0;
						double valoreMedio2 = 0;
						double varianza = 0;

						for(Object o : b)
						{
							DHuman h = (DHuman)o;
							
							if(h.isPunished)
								numNeighPunished++;
							
							if(h.isActionDishonest)
								numNeighDamager++;
							
							valoreMedio += h.dna * (1/(double)b.size());
							valoreMedio2 += (h.dna*h.dna) * (1/(double)b.size());
						}
					    
						for(PastData pd : agentPast)
						{
							numNeighPunished += pd.getNumNeighPunished();
							numNeighDamager += pd.getNumNeighDamager();
						}
						
						if(numNeighDamager!=0.0)
							punprob = (numNeighPunished/numNeighDamager);
						else
							punprob = 0.0;

						varianza = (valoreMedio2) - ((valoreMedio)*(valoreMedio));
						double deviazione=Math.sqrt(varianza);
						
						if((deviazione > sigma2) && (max_aoi_aggregation < dsdbState.neighborhood))
							max_aoi_aggregation += 1.0;
						else
							if((deviazione < sigma1) && (max_aoi_aggregation>min_aoi_aggregation))
								max_aoi_aggregation -= 1.0;									
						
						b.sort(new Comparator<DHuman>() {
							@Override
							public int compare(DHuman o1, DHuman o2) {
								if(o1.fitness>o2.fitness) return 1;
								else if(o1.fitness<o2.fitness) return -1;
								return 0;
							}
						});

						neighFitness = 0;

						for (Object o : b) {
							DHuman h = (DHuman)o;
							neighFitness += h.fitness;
							entryNeigh.add(new EntryAgent<Double, DHuman>(neighFitness, h));
						}
						
						Double2D avoid = behavior.avoidance(this,b,dsdbState.human_being);
						Double2D cohe = behavior.cohesion(this,b,dsdbState.human_being);
						Double2D cons = behavior.consistency(this,b,dsdbState.human_being);
						Double2D mome = momentum();
						
						dx = dsdbState.cohesion * cohe.x + dsdbState.avoidance * avoid.x + dsdbState.consistency* cons.x + dsdbState.momentum * mome.x;
						dy = dsdbState.cohesion * cohe.y + dsdbState.avoidance * avoid.y + dsdbState.consistency* cons.y + dsdbState.momentum * mome.y;
						
						// renormalize to the given step size
						double dis = Math.sqrt(dx*dx+dy*dy);
						if (dis>0)
						{
							dx = dx / dis * dsdbState.jump;
							dy = dy / dis * dsdbState.jump;
						}
					}

		behavior.action(this, dsdbState, b, entryNeigh);

		//Social Influence
		behavior.calculateCEI(this, dsdbState, b);
		behavior.socialInfluence(this, b);

		dataLogger(dsdbState);
		
		lastd = new Double2D(dx,dy);
		pos = new Double2D(dsdbState.human_being.stx(pos.x + dx), dsdbState.human_being.sty(pos.y + dy));
		try {
			dsdbState.human_being.setDistributedObjectLocation(pos, this, dsdbState);
		} catch (DMasonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void dataLogger(DSociallyDamagingBehavior dsdbState) {
		if((dsdbState.numHonest+dsdbState.numDishonest)<dsdbState.numHumanBeing-1){
			if(behavior instanceof Honest)
				dsdbState.numHonest++;
			else
				dsdbState.numDishonest++;
		}
		else
			if((dsdbState.numHonest+dsdbState.numDishonest)==dsdbState.numHumanBeing-1)
			{
				if(behavior instanceof Honest)
					dsdbState.numHonest++;
				else
					dsdbState.numDishonest++;
				if(dsdbState.logging)
				{
					dsdbState.ps.println(dsdbState.schedule.getSteps()+";"+dsdbState.numHonest+";"+
						dsdbState.numDishonest+";"+dsdbState.numHonestAction+";"+
						dsdbState.numDishonestAction);
					dsdbState.ps.flush();
				}
				dsdbState.honestAction = dsdbState.numHonestAction;
				dsdbState.numHonestAction = 0;
				dsdbState.dishonestAction = dsdbState.numDishonestAction;
				dsdbState.numDishonestAction = 0;
				dsdbState.honest = dsdbState.numHonest;
				dsdbState.numHonest = 0;
				dsdbState.dishonest = dsdbState.numDishonest;
				dsdbState.numDishonest = 0;
			}
	}
	
	class Direction extends ArrayList<DHuman> implements Comparable
	{
		double dx;
		double dy;
		public Direction(double ddx,double ddy)
		{
			dx=ddx;
			dy=ddy;
		}
		@Override
		public int compareTo(Object o) {
			if(((Direction)o).size() < this.size()) return -1;
			else if(((Direction)o).size() > this.size()) return 1;
			return 0;
		}
	}
	
	@Override
	public final void draw(Object object, Graphics2D graphics, DrawInfo2D info)
    {
	       // this code was stolen from OvalPortrayal2D
		   graphics.setColor(behav_color);
		   int x = (int)(info.draw.x - info.draw.width / 2.0);
	       int y = (int)(info.draw.y - info.draw.height / 2.0);
	       int width = (int)(info.draw.width);
	       int height = (int)(info.draw.height);
	       graphics.fillOval(x,y,width, height);
    }
	
	@Override
	public double orientation2D()
	{
		if (lastd.x == 0 && lastd.y == 0) return 0;
		return Math.atan2(lastd.y, lastd.x);
	}
	
	public double getFitness() {return fitness;}
	public void setFitness(double fitness) {this.fitness = fitness;}
	public double getDna() {return dna;}
	public void setDna(double dna) 
	{
		this.dna = dna;
		behavior=(dna>5)?new Honest():new Dishonest();
		behav_color=(dna>5)?Color.GREEN:Color.RED;
	}
	public Bag getNeighbors(DSociallyDamagingBehavior dsdbState){return dsdbState.human_being.getObjectsExactlyWithinDistance(pos, dsdbState.neighborhood, true);}
	public Bag getAggregatedNeighbors(DSociallyDamagingBehavior dsdbState){return dsdbState.human_being.getObjectsExactlyWithinDistance(pos, max_aoi_aggregation, true);}
	public double getOrientation() {return orientation2D();}
	@Override
	public void setOrientation2D(double val){lastd = new Double2D(Math.cos(val),Math.sin(val));}
	public Double2D momentum(){return lastd;}
	
	
	public Color getBehav_Color(){return behav_color;}
	@Override
	public String toString() {
		return "DHuman [dna=" + dna + ", id="+ id + ", fitness=" + fitness + "]";
	}
	
}
