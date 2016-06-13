/**
 * Copyright 2016 Universita' degli Studi di Salerno


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
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
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
