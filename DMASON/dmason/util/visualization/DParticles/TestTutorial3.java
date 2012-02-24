package dmason.util.visualization.DParticles;

import sim.display.Console;
import dmason.util.connection.Address;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;
import dmason.util.visualization.CloseZoomAppListener;
import dmason.util.visualization.ConsoleZoom;
import dmason.util.visualization.DAntsForage.AntsForageWithUIZoom;

public class TestTutorial3 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ConnectionNFieldsWithActiveMQAPI con=new ConnectionNFieldsWithActiveMQAPI();
		try {
			con.setupConnection(new Address("127.0.0.1", "61616"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		Tutorial3ViewWithUI t=new Tutorial3ViewWithUI(new Object[]{con,"0-1", true} );
		ConsoleZoom c=new ConsoleZoom(t,con,"1-1");
		c.setVisible(true);
		c.pressPlay();
		
	}
}
