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
package it.isislab.dmason.util.connection.socket;

import it.isislab.dmason.sim.field.MessageListener;
import it.isislab.dmason.util.connection.jms.ConnectionJMS;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

import org.apache.activemq.command.ActiveMQObjectMessage;
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
public class ProxyConnection implements InvocationHandler,Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ConnectionJMS obj;
	private HashMap<String,MessageListener> table;
	
	public static Object createProxy(Object obj){           
        return Proxy.newProxyInstance(obj.getClass().getClassLoader(),
                                                  obj.getClass().getInterfaces(),
                                                  new ProxyConnection(obj));
     }
	
	private ProxyConnection(Object target)
    {
         this.obj = (ConnectionJMS) target;
    }
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		
		if(method.getName().equals("publishToTopic") )
		{
			if(table.containsKey(args[1]))
					{
						try{
							ActiveMQObjectMessage msg = new ActiveMQObjectMessage();
							msg.setObject((Serializable) args[0]);
							table.get(args[1]).onMessage(msg);
						}catch (Exception e) {
							e.printStackTrace();
						}
						return true;
					}
			
		}
		if(method.getName().equals("setTable"))
		{
			table = (HashMap<String, MessageListener>) args[0];
			return null;
		}
	return method.invoke(obj,args);
	}

}
