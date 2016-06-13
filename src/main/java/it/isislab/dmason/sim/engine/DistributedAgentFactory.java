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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 */
public class DistributedAgentFactory implements MethodInterceptor, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;



	private ArrayList<String> methods_read=new ArrayList<String>();
	private ArrayList<String> methods_write=new ArrayList<String>();


	public DistributedAgentFactory(Class state){

		for(Method m:state.getMethods())
			{
				if(m.getName().contains("get"))
					methods_read.add(m.getName());
				else 
					if(m.getName().contains("set"))
					 methods_write.add(m.getName());
			
			}

	}

	@Override
	public Object intercept(Object arg0, Method method, Object[] arg2, MethodProxy arg3) throws Throwable {
			String id=this.toString();
			Object retValFromSuper = null;
			if(methods_read.contains(method.getName()))
			{
				HashMap<String,Object> deferredUpdates=((DistributedMultiSchedule)arg2[0]).deferredUpdates;
				if(deferredUpdates.get(id+method.getName())!=null)return deferredUpdates.get(id+method.getName());
				else
				{
					retValFromSuper = arg3.invokeSuper(arg0, arg2);
					return retValFromSuper;

				}
			}else
				if(methods_write.contains(method.getName()))
				{
					HashMap<String,Object> deferredUpdates=((DistributedMultiSchedule)arg2[0]).deferredUpdates;
					String method_get=method.getName().replace("set", "get");
					if(deferredUpdates.get(id+method_get)==null)
					{
						Method m_get=arg0.getClass().getDeclaredMethod(method_get, DistributedMultiSchedule.class);
						if(m_get!=null)
						{
							Object ret=m_get.invoke(arg0, new Object[]{arg2[0]});
							deferredUpdates.put(id+method_get,ret);

						}else{
							throw new DMasonException("Problems in the snapshot mechanism.");
						}
					}


					try {
						retValFromSuper = arg3.invokeSuper(arg0, arg2);
					} catch (Throwable t) {
						t.printStackTrace();
					}
					return retValFromSuper;
				}

			try {
				retValFromSuper = arg3.invokeSuper(arg0, arg2);
			} catch (Throwable t) {
				t.printStackTrace();
			}
			return retValFromSuper;

	}


	public static RemoteAgent newIstance(
			Class<?> remote_agent, 
			Class[] paramas_classes,
			Object[] paramas_values,
			Class state) {

		Enhancer e = new Enhancer();
		e.setSuperclass(remote_agent);
		e.setInterfaces(remote_agent.getInterfaces());
		e.setCallback(new DistributedAgentFactory(state));
		Object obj=e.create(paramas_classes, paramas_values);

		RemoteAgent ag=(RemoteAgent)obj;

		return ag;
	}




}
