package dmason.util.connection;

public class DMasonConnectionFactory {
	
	public static ConnectionWithJMS createConnection(boolean flag){
		
		if(flag)
		{
			return (ConnectionWithJMS) ProxyConnection.createProxy(new ConnectionWithActiveMQAPI());
		}
		else
			return new ConnectionWithActiveMQAPI();
	}

}
