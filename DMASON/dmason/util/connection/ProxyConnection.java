package dmason.util.connection;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import org.apache.activemq.command.ActiveMQObjectMessage;

import dmason.sim.field.DistributedRegion;
import dmason.sim.field.MessageListener;

public class ProxyConnection implements InvocationHandler,Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ConnectionWithJMS obj;
	private HashMap<String,MessageListener> table;
	
	public static Object createProxy(Object obj){           
        return Proxy.newProxyInstance(obj.getClass().getClassLoader(),
                                                  obj.getClass().getInterfaces(),
                                                  new ProxyConnection(obj));
     }
	
	private ProxyConnection(Object target)
    {
         this.obj = (ConnectionWithJMS) target;
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
