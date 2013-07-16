/*
 * THIS CLASS HAS BEEN USED FOR TESTING PURPOSES IN THE BEGINNINGS,
 * IT SHOULD BE REMOVED.
 */

//package dmason.util.start;
//
//
//import java.util.ArrayList;
//
//import dmason.batch.data.GeneralParam;
//import dmason.sim.app.DFlockers.DFlockersWithUI;
//import dmason.sim.app.DParticles.DParticlesWithUI;
//import dmason.sim.app.DAntsForage.DAntsForageWithUI;
//import dmason.sim.field.grid.DSparseGrid2DFactory;
//import sim.display.Console;
//
//public class Starter {
//
//	public static void main(String[] args) 
//	{		
//		int rows = 3;
//		int columns = 3;
//		int MAX_DISTANCE=1;
//		int NUM_AGENTS=10;
//		int WIDTH=225;
//		int HEGHT=225;
//		//int MODE=DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE;
//		int MODE=DSparseGrid2DFactory.SQUARE_BALANCED_DISTRIBUTION_MODE;
//		
//		//int TUTORIAL=1;
//		int TUTORIAL=2;
//		//int TUTORIAL=3;
//		if(TUTORIAL==1)
//		if(MODE==DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE)
//		{
//			ArrayList<Console> dants=new ArrayList<Console>();
//			for (int j = 0; j < columns; j++) 
//			{		
//				GeneralParam genParam = new GeneralParam(WIDTH, HEGHT, MAX_DISTANCE, 1,columns,NUM_AGENTS, MODE); 
//				genParam.setI(0);
//				genParam.setJ(j);
//				genParam.setIp("127.0.0.1");
//				genParam.setPort("61616");
//				DParticlesWithUI t=new DParticlesWithUI(genParam); //new Object[]{"127.0.0.1","61616",MAX_DISTANCE,NUM_PEERS,NUM_AGENTS,WIDTH,HEGHT,0,j,MODE}
//				Console c=(Console)t.createController();
//
//				c.pressPause();
//				dants.add(c);
//			}
//			for(Console cc:dants) cc.pressPause();
//		}
//		if(TUTORIAL==1)
//			if(MODE==DSparseGrid2DFactory.SQUARE_DISTRIBUTION_MODE)
//			{
//				ArrayList<Console> dants=new ArrayList<Console>();
//				for (int i = 0; i < rows; i++) {
//					for (int j = 0; j < columns; j++) {
//					
//						GeneralParam genParam = new GeneralParam(WIDTH, HEGHT, MAX_DISTANCE, rows,columns,NUM_AGENTS, MODE); 
//						genParam.setI(i);
//						genParam.setJ(j);
//						genParam.setIp("127.0.0.1");
//						genParam.setPort("61616");
//						DParticlesWithUI t=new DParticlesWithUI(genParam); //new Object[]{"127.0.0.1","61616",MAX_DISTANCE,NUM_PEERS,NUM_AGENTS,WIDTH,HEGHT,i,j,MODE}
//				        Console c=(Console)t.createController();
//				        c.pressPause();
//				        dants.add(c);
//				     }
//				}
//				for(Console cc:dants) cc.pressPause();
//			}else if(MODE==DSparseGrid2DFactory.SQUARE_BALANCED_DISTRIBUTION_MODE)
//			{
//				ArrayList<Console> dpart=new ArrayList<Console>();
//				for (int i = 0; i < rows; i++) {
//					for (int j = 0; j < columns; j++) {
//					
//					GeneralParam genParam = new GeneralParam(WIDTH, HEGHT, MAX_DISTANCE, rows,columns,NUM_AGENTS, MODE); 
//					genParam.setI(i);
//					genParam.setJ(j);
//					genParam.setIp("127.0.0.1");
//					genParam.setPort("61616");
//					DParticlesWithUI t=new DParticlesWithUI(genParam); //new Object[]{"127.0.0.1","61616",MAX_DISTANCE,NUM_PEERS,NUM_AGENTS,WIDTH,HEGHT,i,j,MODE}
//				        Console c=(Console)t.createController();
//				        c.pressPause();
//				        dpart.add(c);
//
//				}
//				}
//				
//				for(Console cc:dpart) cc.pressPause();	
//			}		
//				
//		if(TUTORIAL==2)
//			if(MODE==DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE){
//				ArrayList<Console> dants=new ArrayList<Console>();
//				for (int j = 0; j < columns; j++) 
//				{			
//					GeneralParam genParam = new GeneralParam(WIDTH, HEGHT, MAX_DISTANCE, 1,columns,NUM_AGENTS, MODE); 
//					genParam.setI(0);
//					genParam.setJ(j);
//					genParam.setIp("127.0.0.1");
//					genParam.setPort("61616");
//					DParticlesWithUI t=new DParticlesWithUI(genParam); //new Object[]{"127.0.0.1","61616",MAX_DISTANCE,NUM_PEERS,NUM_AGENTS,WIDTH,HEGHT,0,j,MODE}
//				        Console c=(Console)t.createController();
//				        c.pressPause();
//				        dants.add(c);
//				}
//				for(Console cc:dants) cc.pressPause();
//			}
//		if(TUTORIAL==2)
//			if(MODE==DSparseGrid2DFactory.SQUARE_BALANCED_DISTRIBUTION_MODE)
//			{
//				ArrayList<Console> dants=new ArrayList<Console>();
//				for (int i = 0; i < rows; i++) {
//					for (int j = 0; j < columns; j++) {
//					
//						GeneralParam genParam = new GeneralParam(WIDTH, HEGHT, MAX_DISTANCE, rows,columns,NUM_AGENTS, MODE); 
//						genParam.setI(i);
//						genParam.setJ(j);
//						genParam.setIp("127.0.0.1");
//						genParam.setPort("61616");
//						DParticlesWithUI t=new DParticlesWithUI(genParam); //new Object[]{"127.0.0.1","61616",MAX_DISTANCE,NUM_PEERS,NUM_AGENTS,WIDTH,HEGHT,i,j,MODE}
//					        Console c=(Console)t.createController();
//					        c.pressPause();
//					        dants.add(c);
//				     }
//				}
//				for(Console cc:dants) cc.pressPause();
//			}
//		
//	if(TUTORIAL==3)
//		if(MODE==DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE)
//		{
//			for (int j = 0; j < columns; j++) 
//			{				
//				GeneralParam genParam = new GeneralParam(WIDTH, HEGHT, MAX_DISTANCE, 1,columns,NUM_AGENTS, MODE); 
//				genParam.setI(0);
//				genParam.setJ(j);
//				genParam.setIp("127.0.0.1");
//				genParam.setPort("61616");
//				DParticlesWithUI t=new DParticlesWithUI(genParam); //new Object[]{"127.0.0.1","61616",MAX_DISTANCE,NUM_PEERS,NUM_AGENTS,WIDTH,HEGHT,0,j,MODE}
//				Console c=(Console)t.createController();
//			        
//			    c.pressPause();
//
//			}
//		}
//		if(TUTORIAL==3)
//			if(MODE==DSparseGrid2DFactory.SQUARE_DISTRIBUTION_MODE){
//				ArrayList<Console> dants=new ArrayList<Console>();
//				for (int i = 0; i < rows; i++) {
//					for (int j = 0; j < columns; j++) {
//				
//						GeneralParam genParam = new GeneralParam(WIDTH, HEGHT, MAX_DISTANCE, rows,columns,NUM_AGENTS, MODE); 
//						genParam.setI(i);
//						genParam.setJ(j);
//						genParam.setIp("127.0.0.1");
//						genParam.setPort("61616");
//						DParticlesWithUI t=new DParticlesWithUI(genParam); //new Object[]{"127.0.0.1","61616",MAX_DISTANCE,NUM_PEERS,NUM_AGENTS,WIDTH,HEGHT,i,j,MODE}
//				        Console c=(Console)t.createController();
//				        c.pressPause();
//				        dants.add(c);
//					}
//				}
//				for(Console cc:dants) cc.pressPause();
//			}	
//	}
//}