package dmason.util.visualization;

import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.display.Prefs;
import sim.display.SimApplet;
import sim.display.SimpleController;
import sim.engine.*;
import java.awt.*;
import java.text.*;
import java.util.*;
import ec.util.*;
import java.io.*;
import sim.util.*;
import sim.util.gui.*;
import sim.portrayal.*;
import java.lang.ref.*;
import java.lang.reflect.*;
import java.util.prefs.*;


public class ConsoleZoom extends sim.display.Console {
  
   
	public final ConnectionNFieldsWithActiveMQAPI con;
    public String id_cell;
    final Object isClosingLock = new Object();
    boolean isClosing = false;
    public Display disp;
    int mode; int numCell;
	int width; int height; String absolutePath;
	String simul;
    
    public ConsoleZoom(final GUIState simulation,ConnectionNFieldsWithActiveMQAPI con,String id_cell)
        {
    		super(simulation);
    		this.con=con;
    		this.id_cell=id_cell;
    		
        }
    public ConsoleZoom(GUIState simulation,
			ConnectionNFieldsWithActiveMQAPI con, String id_cell,
			boolean isClosing, Display disp, int mode, int numCell, int width,
			int height, String absolutePath, String simul) {
		super(simulation);
		this.con = con;
		this.id_cell = id_cell;
		this.isClosing = isClosing;
		this.disp = disp;
		this.mode = mode;
		this.numCell = numCell;
		this.width = width;
		this.height = height;
		this.absolutePath = absolutePath;
		this.simul = simul;
	}
    private void  sendAck()
    {

    		try {
				con.publishToTopic("EXIT_ZOOM", "GRAPHICS"+id_cell,"GRAPHICS"+id_cell);
				System.out.println("Zoom correctly disconnect.");
    		  } catch (Exception e) {
    				System.out.println(
    						"Zoom uncorrectly disconnect. Possible problem in your simulation...");
    				e.printStackTrace();
    			}
			
    }
    public void doClose()
    {
    		sendAck();
    		pressStop();  
    	
            simulation.quit();  
    		dispose();
    	    allControllers.remove(this);
    	    
			
    	   try {
			
    			Display display = new Display(con, mode, 
    					numCell, width, 
    					height, absolutePath,simul);
    			display.initComponents();
    			display.Display.setVisible(true);
    			
		
		} catch (Exception e) {
			System.out.println("Problemi in chiusura Console ZOOM!!!!");
			e.printStackTrace();
		}
   	
  
    }
}