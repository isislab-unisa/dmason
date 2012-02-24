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


public class ConsoleZoom extends sim.display.Console
    {
  
    public final ConnectionNFieldsWithActiveMQAPI con;
    public String id_cell;
    final Object isClosingLock = new Object();
    boolean isClosing = false;
    public Display disp;
    
    public ConsoleZoom(final GUIState simulation,ConnectionNFieldsWithActiveMQAPI con,String id_cell, Display display)
        {
    		super(simulation);
    		this.con=con;
    		this.id_cell=id_cell;
    		this.disp=display;
        }
    public ConsoleZoom(final GUIState simulation,ConnectionNFieldsWithActiveMQAPI con,String id_cell)
    {	super(simulation);
		this.con=con;
		this.id_cell=id_cell;
    }
    private void  sendAck()
    {
    
    		System.out.println("kiudo");
    		try {
				con.publishToTopic("EXIT_ZOOM", "GRAPHICS"+id_cell,"GRAPHICS"+id_cell);
				//JOptionPane.showMessageDialog(null, "Zoom correctly disconnect.");
    		  } catch (Exception e) {
    				JOptionPane.showMessageDialog(null, 
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
			con.publishToTopic("ENTER", "GRAPHICS", "GRAPHICS");
			//System.out.println("wewe");
			//disp.updates.forceSblock();
			disp.PAUSE=true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   	
  
    }
}