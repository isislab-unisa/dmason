package dmason.util.visualization.DAntsForage;

import java.util.ArrayList;

import sim.display.Console;
import dmason.sim.app.DAntsForage.DAntsForageWithUI;
import dmason.sim.field.grid.DSparseGrid2DFactory;
import dmason.util.connection.Address;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;
import dmason.util.visualization.CloseZoomAppListener;


public class TestZoomAnts {

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

				AntsForageWithUIZoom t=new AntsForageWithUIZoom(new Object[]{con,"1-1"} );
		        Console c=(Console)t.createController();
		       
		        c.removeWindowListener(c.getWindowListeners()[0]);
		        c.addWindowListener(new CloseZoomAppListener(c,con, "1-1"));
		        c.pressPlay();
		   
		
	}

}
