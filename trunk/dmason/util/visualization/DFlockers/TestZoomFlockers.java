package dmason.util.visualization.DFlockers;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import sim.display.Console;
import sim.display.GUIState;
import dmason.sim.app.DAntsForage.DAntsForageWithUI;
import dmason.sim.field.grid.DSparseGrid2DFactory;
import dmason.util.connection.Address;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;
import dmason.util.visualization.CloseZoomAppListener;

public class TestZoomFlockers {

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
	
		FlockersWithUIView t=new FlockersWithUIView(new Object[]{con,"1-1"} );
		Console c=(Console)t.createController();
        
		c.removeWindowListener(c.getWindowListeners()[0]);
        c.addWindowListener(new CloseZoomAppListener(c,con, "1-1"));
		c.pressPlay();
		
	
	}

}
