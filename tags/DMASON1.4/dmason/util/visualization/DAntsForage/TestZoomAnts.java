package dmason.util.visualization.DAntsForage;

import dmason.util.connection.Address;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;
import dmason.util.visualization.ConsoleZoom;


public class TestZoomAnts {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String id = "1-1";
		int numCell = 4;
		int width = 400;
		int height = 400;
		int mode = 1;
		
		ConnectionNFieldsWithActiveMQAPI con=new ConnectionNFieldsWithActiveMQAPI();
		try {
			con.setupConnection(new Address("127.0.0.1", "61616"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		AntsForageWithUIZoom simulazione=new AntsForageWithUIZoom(new Object[]{con,id,true,numCell,width,height,mode} );

		ConsoleZoom c=new ConsoleZoom(simulazione,con,"1-1");
		c.setVisible(true);
		c.pressPlay();		
	}

}
