package dmason.util.start;


import java.util.ArrayList;

import dmason.sim.app.DFlockers.DFlockersWithUI;
import dmason.sim.app.DParticles.DParticlesWithUI;
import dmason.sim.app.DAntsForage.DAntsForageWithUI;
import dmason.sim.field.grid.DSparseGrid2DFactory;
import sim.display.Console;

public class Starter {

	public static void main(String[] args) 
	{		
		int NUM_PEERS=4;
		int MAX_DISTANCE=1;
		int NUM_AGENTS=50;
		boolean isTOROIDAL = true;
		int WIDTH=100;
		int HEGHT=100;
		//int MODE=DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE;
		int MODE=DSparseGrid2DFactory.SQUARE_DISTRIBUTION_MODE;
		
		//int TUTORIAL=1;
		int TUTORIAL=2;
		//int TUTORIAL=3;
		if(TUTORIAL==1)
		if(MODE==DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE)
		{
			ArrayList<Console> dants=new ArrayList<Console>();
			for (int j = 0; j < NUM_PEERS; j++) 
			{				
				 DParticlesWithUI t=new DParticlesWithUI(new Object[]{"127.0.0.1","61616",MAX_DISTANCE,NUM_PEERS,NUM_AGENTS,WIDTH,HEGHT,0,j,MODE,isTOROIDAL} );
			        Console c=(Console)t.createController();
			        
			        c.pressPause();
			        dants.add(c);
			}
			for(Console cc:dants) cc.pressPause();
		}
		if(TUTORIAL==1)
			if(MODE==DSparseGrid2DFactory.SQUARE_DISTRIBUTION_MODE)
			{
				ArrayList<Console> dants=new ArrayList<Console>();
				for (int i = 0; i < Math.sqrt(NUM_PEERS); i++) {
					for (int j = 0; j < Math.sqrt(NUM_PEERS); j++) {
					
						DParticlesWithUI t=new DParticlesWithUI(new Object[]{"127.0.0.1","61616",MAX_DISTANCE,NUM_PEERS,NUM_AGENTS,WIDTH,HEGHT,i,j,MODE,isTOROIDAL} );
				        Console c=(Console)t.createController();
				        c.pressPause();
				        dants.add(c);
				     }
				}
				for(Console cc:dants) cc.pressPause();
			}	
				
		if(TUTORIAL==2)
			if(MODE==DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE){
				ArrayList<Console> dants=new ArrayList<Console>();
				for (int j = 0; j < NUM_PEERS; j++) 
				{			
					 DFlockersWithUI t=new DFlockersWithUI(new Object[]{"127.0.0.1","61616",MAX_DISTANCE,NUM_PEERS,NUM_AGENTS,WIDTH,HEGHT,0,j,MODE,isTOROIDAL} );
				        Console c=(Console)t.createController();
				        c.pressPause();
				        dants.add(c);
				}
				for(Console cc:dants) cc.pressPause();
			}
		if(TUTORIAL==2)
			if(MODE==DSparseGrid2DFactory.SQUARE_DISTRIBUTION_MODE)
			{
				ArrayList<Console> dants=new ArrayList<Console>();
				for (int i = 0; i < Math.sqrt(NUM_PEERS); i++) {
					for (int j = 0; j < Math.sqrt(NUM_PEERS); j++) {
					
						DFlockersWithUI t=new DFlockersWithUI(new Object[]{"127.0.0.1","61616",MAX_DISTANCE,NUM_PEERS,NUM_AGENTS,WIDTH,HEGHT,i,j,MODE,isTOROIDAL} );
					        Console c=(Console)t.createController();
					        c.pressPause();
					        dants.add(c);
				     }
				}
				for(Console cc:dants) cc.pressPause();
			}
		
	if(TUTORIAL==3)
		if(MODE==DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE)
		{
			for (int j = 0; j < NUM_PEERS; j++) 
			{				
				DAntsForageWithUI t=new DAntsForageWithUI(new Object[]{"127.0.0.1","61616",MAX_DISTANCE,NUM_PEERS,NUM_AGENTS,WIDTH,HEGHT,0,j,MODE,isTOROIDAL} );
				Console c=(Console)t.createController();
			        
			    c.pressPause();

			}
		}
		if(TUTORIAL==3)
			if(MODE==DSparseGrid2DFactory.SQUARE_DISTRIBUTION_MODE){
				ArrayList<Console> dants=new ArrayList<Console>();
				for (int i = 0; i < Math.sqrt(NUM_PEERS); i++) {
					for (int j = 0; j < Math.sqrt(NUM_PEERS); j++) {
				
						DAntsForageWithUI t=new DAntsForageWithUI(new Object[]{"127.0.0.1","61616",MAX_DISTANCE,NUM_PEERS,NUM_AGENTS,WIDTH,HEGHT,i,j,MODE,isTOROIDAL} );
				        Console c=(Console)t.createController();
				        c.pressPause();
				        dants.add(c);
					}
				}
				for(Console cc:dants) cc.pressPause();
			}	
	}
}