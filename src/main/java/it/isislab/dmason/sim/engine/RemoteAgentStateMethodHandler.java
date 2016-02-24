package it.isislab.dmason.sim.engine;

import it.isislab.dmason.exception.DMasonException;

import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * Interface needed for memory consistency of the agents
 *
 */
public class RemoteAgentStateMethodHandler implements Serializable
{

	private static MethodHandle lookupGetter(Class objdefinition,String variablename, Class type) {
		try {
			return MethodHandles.lookup().findGetter(objdefinition, variablename, type);

		} catch ( Throwable e ) {
			e.printStackTrace();

			return null;
		}
	}
	private static MethodHandle lookupSetter(Class objdefinition,String variablename, Class type) {
		try {
			return MethodHandles.lookup().findSetter(objdefinition, variablename, type);

		} catch ( Throwable e ) {
			e.printStackTrace();

			return null;
		}
	}
	private HashMap<String, MethodHandle> methods=new HashMap<String,MethodHandle>();
	
	public RemoteAgentStateMethodHandler(Class objClass,ArrayList<StateVariable> statevariables) {
		if(statevariables==null) return;
		for(StateVariable variable:statevariables)
		{
			methods.put("get"+variable.getName(), lookupGetter(objClass, variable.getName(),variable.getType()));
			methods.put("set"+variable.getName(), lookupSetter(objClass, variable.getName(),variable.getType()));
		}
	}
	public void setState(DistributedMultiSchedule schedule, Object agent, String name, Object newstate) throws Throwable
	{
		MethodHandle mget=methods.get("get"+name);
		MethodHandle mset=methods.get("set"+name);

	
		if(mset==null || mget==null)
		{
			throw new DMasonException("Error in variable name for agent consistency state");
		}


		if(schedule.deferredUpdates.get(agent.hashCode()+mget.hashCode())==null)
			schedule.deferredUpdates.put(agent.hashCode()+mget.hashCode(), mget.invoke(agent));

		mset.invoke(agent, newstate);

	}

	public Object getState(DistributedMultiSchedule schedule,Object agent, String name) throws Throwable
	{
		MethodHandle mget=methods.get("get"+name);

	
		if(mget==null)
		{
			throw new DMasonException("Error in variable name for agent consistency state");
		}

		return (schedule.deferredUpdates.get(agent.hashCode()+mget.hashCode())!=null)?
				schedule.deferredUpdates.get(agent.hashCode()+mget.hashCode()):
					mget.invoke(agent);

	}
	
	
	public void setStateWithLookup(DistributedMultiSchedule schedule, Object agent, String name, Object newstate, Class fieldtype) throws Throwable
	{
		MethodHandle mget=lookupGetter(agent.getClass(),name, fieldtype);
		MethodHandle mset=lookupSetter(agent.getClass(),name, fieldtype);

	
		if(mset==null || mget==null)
		{
			throw new DMasonException("Error in variable name for agent consistency state");
		}


		if(schedule.deferredUpdates.get(agent.hashCode()+mget.hashCode())==null)
			schedule.deferredUpdates.put(agent.hashCode()+mget.hashCode(), mget.invoke(agent));

		mset.invoke(agent, newstate);

	}

	public Object getStateWithLookup(DistributedMultiSchedule schedule,Object agent, String name,Class fieldtype) throws Throwable
	{
		MethodHandle mget=lookupGetter(agent.getClass(),name, fieldtype);

	
		if(mget==null)
		{
			throw new DMasonException("Error in variable name for agent consistency state");
		}

		return (schedule.deferredUpdates.get(agent.hashCode()+mget.hashCode())!=null)?
				schedule.deferredUpdates.get(agent.hashCode()+mget.hashCode()):
					mget.invoke(agent);

	}
	
	
	
	///////
	/*public void setStateReflection(DistributedMultiSchedule schedule,RemoteAgent agent, String name, Object ... newstate) throws DMasonException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		Method mset=null;
		Method mget=null;

		for(Method m:stateInterface.getMethods())
		{
			if(m.getName().contains(name))
				if(m.getName().contains("get")) mget=m;
				else
					if(m.getName().contains("set")) mset=m;
		}
		if(mset==null || mget==null)
		{
			throw new DMasonException("Error in variable name for agent consistency state");
		}


		if(schedule.deferredUpdates.get(agent.hashCode()+mget.getName())==null)
			schedule.deferredUpdates.put(agent.hashCode()+mget.getName(),mget.invoke(agent));

		mset.invoke(agent, newstate);

	}
	public Object getStateRelection(DistributedMultiSchedule schedule,RemoteAgent agent, String name) throws DMasonException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		Method mget=null;

		for(Method m:stateInterface.getMethods())
		{
			if(m.getName().contains(name))
				if(m.getName().contains("get")) mget=m;
		}
		if(mget==null)
		{
			throw new DMasonException("Error in variable name for agent consistency state");
		}

		return (schedule.deferredUpdates.get(agent.hashCode()+mget.getName())!=null)?schedule.deferredUpdates.get(agent.hashCode()+mget.getName()): mget.invoke(agent);

	}*/
}
