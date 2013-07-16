package dmason.sim.globals.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dmason.sim.engine.DistributedState;
import dmason.sim.util.DistributedProperties;
import dmason.util.connection.ConnectionWithJMS;

/**
 * 
 * A support class for the computing of global variables
 * @author Michele Carillo, Ada Mancuso, Flavio Serrapica, Carmine Spagnuolo, Francesco Raia
 *
 */
public class UpdateGlobalVarAtStep {
	
	private ArrayList<String> globalVar = new ArrayList<String>();
	private HashMap<String, Long> globalEveryVar = new HashMap<String, Long>();
	private DistributedState<?> simState=null;
	private Class<?> simClass=null;
	private DistributedProperties distroProp = null;
		
	public UpdateGlobalVarAtStep(DistributedState<?> sm) {
		
		simState = sm;
		simClass = simState.getClass();
		distroProp = new DistributedProperties(simState);
		ArrayList<String> globalsNames = new ArrayList<String>();
		
		Method m2 = null;
		long EVERY_STEP;
		
		for (int pi = 0; pi < distroProp.numProperties(); pi++)
		{
			if (distroProp.isGlobal(pi))
			{
				// Build the list containing global parameters' names
				globalsNames.add(distroProp.getName(pi));
			}
		}
		
		try {
			
			for (String name : globalsNames){
				if (name.contains("Every") || name.contains("Reinitialize")){

					m2 = simClass.getMethod("get" + name +"ValueOf", (Class<?>[])null);
					EVERY_STEP = (Long)m2.invoke(sm, new Object [0]);
					globalEveryVar.put(name, EVERY_STEP);
				}
				else{
					globalVar.add(name);
				}
			}
			
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @return the global variables names list
	 */
	public ArrayList<String> getGlobalVar() { return globalVar; }

	/**
	 * 
	 * @return the global every variable names list
	 */
	public int getGlobalEveryVarForStep(long step) {
		int count = 0;
		for (String key: globalEveryVar.keySet())
			if (step % (globalEveryVar.get(key)) == 0){
				count++;
			}
		
		return count; 
	}
	
	/**
	 * 
	 * @param step The step of simulation
	 * @return all global variables for the indicated step
	 */
	public ArrayList<String> getAllGlobalVarForStep(long step){
		ArrayList<String> varForStep = new ArrayList<String>();
		for (String name : globalVar) { varForStep.add(name);}
		for (String key: globalEveryVar.keySet())
			if (step % (globalEveryVar.get(key)) == 0){
				varForStep.add(key);
			}
		return (varForStep.size() >0)?varForStep:null;
	}
	
	
}
