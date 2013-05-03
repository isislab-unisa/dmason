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

package dmason.sim.loadbalancing;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import sim.util.Int2D;
import dmason.sim.field.CellType;
import dmason.sim.field.DistributedField;
import dmason.sim.field.EntryNum;
import dmason.sim.field.RegionNumeric;
import dmason.sim.field.grid.numeric.RegionDoubleNumericLB;

/**
 * Class for load balancing for field of double value and coordinates integer 
 * @see LoadBalancingInterface
 *
 * @param <E> type of cordinates
 */
public class LoadBalancingDoubleNumeric <E> implements LoadBalancingInterface{

	public ArrayList<MyCellInterface> list;

	/**
	 * void constructor
	 */
	public LoadBalancingDoubleNumeric(){

		list = new ArrayList<MyCellInterface>();

	}

	@Override
	public ArrayList<MyCellInterface> createRegions(DistributedField x,
			Object superWidth, Object superHeight, int MAX_DISTANCE,
			Object superOwnX, Object superOwnY, int numPeer) {
		
		int []lP = {0,1,2,7,8,3,6,5,4};
		int k = 0;
		HashMap<Integer, Boolean> topics;
		HashMap<Integer, Boolean> positionGood;

		for (int i = 0; i < 3; i++) 
		{
			for (int j = 0; j < 3; j++) 
			{	
				MyCellDoubleNumeric mD;

				if((lP[k]%2==0) && (lP[k]!=8))
				{
					topics = new HashMap<Integer, Boolean>();
					positionGood = new HashMap<Integer, Boolean>();

					int antiPosition = (lP[k]+4+8)%8;

					for (int j2 = 0; j2 < 8; j2++) {

						if(j2 == ((antiPosition-1+8)%8))	
							topics.put(j2, false);
						else
							if(j2 == antiPosition)
								topics.put(j2, false);
							else
								if(j2 == ((antiPosition+1+8)%8))
									topics.put(j2, false);
								else
									topics.put(j2, true);	
					}

					initializePositionGood(topics, positionGood);

					SplittedDoubleNumeric split = new SplittedDoubleNumeric((Integer)superOwnX, (Integer)superOwnY, (Integer)superWidth, 
							(Integer)superHeight, x.getState(), MAX_DISTANCE, i, j, numPeer, x.getID(), 
							lP[k], x.getState().TYPE,topics,positionGood);

					mD = split.createMyDivision();

					list.add(mD);
				}
				else	
					if((lP[k]%2!=0))
					{
						topics = new HashMap<Integer, Boolean>();
						positionGood = new HashMap<Integer, Boolean>();

						for (int j2 = 0; j2 < 8; j2++) 
						{
							if(j2 == ((lP[k]-1+8)%8))	
								topics.put(j2, true);
							else
								if(j2 == lP[k])
									topics.put(j2, true);
								else
									if(j2 == ((lP[k]+1+8)%8))
										topics.put(j2, true);
									else
										topics.put(j2, false);
						}

						initializePositionGood(topics, positionGood);
						
						SplittedDoubleNumeric split = new SplittedDoubleNumeric((Integer)superOwnX, (Integer)superOwnY, (Integer)superWidth, 
								(Integer)superHeight, x.getState(), MAX_DISTANCE, i, j, numPeer, x.getID(), 
								lP[k], x.getState().TYPE,topics,positionGood);

						mD = split.createMyDivision();

						list.add(mD);
					}
					else{

						topics = null;
						positionGood = null;

						SplittedDoubleNumeric split = new SplittedDoubleNumeric((Integer)superOwnX, (Integer)superOwnY, (Integer)superWidth, 
								(Integer)superHeight, x.getState(), MAX_DISTANCE, i, j, numPeer, x.getID(), 
								lP[k], x.getState().TYPE,topics,positionGood);

						mD = split.createMyDivision();

						list.add(mD);
					}

				k++;
			}
		}

		return list;

	}
     
	/**
	 * Set the position good 
	 * @param topics         a hash of topics
	 * @param positionGood   boolean value  for only key
	 */
	private void initializePositionGood(HashMap<Integer, Boolean> topics, HashMap<Integer, Boolean> positionGood) {

		//INIZIALIZZAZIONE POSITIONGOOD
		int h = 10; 

		for (int l = 0; l <= 16; l++) {

			positionGood.put(l, true);
		}

		for (int l = 0; l <8 ; l++) {

			if(topics.get(l)==false)
				positionGood.put(l, false);
		}

		//RIEMPIAMO POSITIONGOOD ESATTE
		for (int l = 0; l < 7; l+=2) 
		{	
			if(!topics.get((l+1+8)%8))
			{
				positionGood.put(h+l, false);
			}
			if(!topics.get((l-1+8)%8))
			{
				positionGood.put(h+l-1, false);
			}
		}
	}


	/**
	 * Use java Reflect  only for testing
	 */
	private void verifyCell(MyCellIntegerField region, CellType type){

		Class o=region.getMyRMap().getClass();

		Field[] fields = o.getDeclaredFields();

		System.out.println("MYFIELD DI "+type.toString()+": "+region.getMyField().toString());

		for (int z = 0; z < fields.length; z++)
		{
			fields[z].setAccessible(true);

			try
			{
				String name=fields[z].getName();
				Method method = o.getMethod("get"+name, null);
				Object returnValue = method.invoke(region.getMyRMap(), null);

				System.out.println("NameField="+name+"--"+returnValue);

			}
			catch (IllegalArgumentException e){e.printStackTrace();} 
			catch (IllegalAccessException e) {e.printStackTrace();} 
			catch (SecurityException e) {e.printStackTrace();} 
			catch (NoSuchMethodException e) {e.printStackTrace();} 
			catch (InvocationTargetException e) {e.printStackTrace();}
		}
	}

	@Override
	public int calculatePositionForBalance(int pos){

		return (pos+4+8)%8;		
	}

	@Override
	public boolean addForBalance(Object loc, Object element, Object myCell) {
		
		MyCellDoubleNumeric m = (MyCellDoubleNumeric)myCell;
		double i = (Double) element;
		Int2D location = (Int2D) loc ;
		
		if(((RegionDoubleNumericLB)m.getMyField()).isMine(location.x, location.y))
		{	
			return ((RegionDoubleNumericLB)m.getMyField()).addEntryNum(new EntryNum(i, location));
		}
		else
		{
			Class o=m.getMyRMap().getClass();

			Field[] fields = o.getDeclaredFields();

			for (int z = 0; z < fields.length; z++)
			{
				fields[z].setAccessible(true);

				try
				{
					String name=fields[z].getName();
					Method method = o.getMethod("get"+name, null);
					Object returnValue = method.invoke(m.getMyRMap(), null);

					if(returnValue!=null)
					{
						RegionNumeric<Integer,EntryNum<Double,Int2D>> region=((RegionNumeric<Integer,EntryNum<Double,Int2D>>)returnValue);

						if(region.isMine(location.x,location.y))
						{   
							return region.addEntryNum(new EntryNum<Double,Int2D>(i, location));
						}    
					}

				}
				catch (IllegalArgumentException e){e.printStackTrace();} 
				catch (IllegalAccessException e) {e.printStackTrace();} 
				catch (SecurityException e) {e.printStackTrace();} 
				catch (NoSuchMethodException e) {e.printStackTrace();} 
				catch (InvocationTargetException e) {e.printStackTrace();}
			}
		}

		return false;
	}
	
	public boolean addForUnion(Object loc, Object element, Object myCell) {
		
		MyCellDoubleNumeric m = (MyCellDoubleNumeric)myCell;
		double i = (Double) element;
		Int2D location = (Int2D) loc ;
		int position = calculatePositionForBalance(m.getPosition());
		
		if(m.getMyField().isMine(location.x, location.y))
		{
			return ((RegionDoubleNumericLB)m.getMyField()).addEntryNum(new EntryNum(i, location));
		}
		else
		{
			Class o=m.getMyRMap().getClass();
				
			Field[] fields = o.getDeclaredFields();
				
			for (int z = 0; z < fields.length; z++)
			{
				fields[z].setAccessible(true);
					
				try
				{
					String name=fields[z].getName();
			    	Method method = o.getMethod("get"+name, null);
			    	Object returnValue = method.invoke(m.getMyRMap(), null);
				    	
			    	if(returnValue!=null)
			    	{
		   				//if(name.contains("mine"))
		   				
			    		RegionNumeric<Integer,EntryNum<Double,Int2D>> region=((RegionNumeric<Integer,EntryNum<Double,Int2D>>)returnValue);
				    		
			    		if(region.isMine(location.x,location.y))
			    	    {   
			    			return region.addEntryNum(new EntryNum<Double,Int2D>(i, location));
				    	}    
				    }
				    
				}
				catch (IllegalArgumentException e){e.printStackTrace();} 
				catch (IllegalAccessException e) {e.printStackTrace();} 
				catch (SecurityException e) {e.printStackTrace();} 
				catch (NoSuchMethodException e) {e.printStackTrace();} 
				catch (InvocationTargetException e) {e.printStackTrace();}
			}
		}

		return false;
	}
}
