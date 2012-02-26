package dmason.util.connection;

public class DMasonConnectionFactory {
	
	public static ConnectionWithJMS createConnection(boolean flag){
		
		if(flag)
		{
			return (ConnectionWithJMS) ProxyConnection.createProxy(new ConnectionNFieldsWithActiveMQAPI());
		}
		else
			return new ConnectionNFieldsWithActiveMQAPI();
	}

}
