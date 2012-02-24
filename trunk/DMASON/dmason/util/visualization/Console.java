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


public class Console extends sim.display.Console
    {
  
    public static ConnectionNFieldsWithActiveMQAPI con;
    public static String id_cell;

    public Console(final GUIState simulation,ConnectionNFieldsWithActiveMQAPI con,String id_cell)
        {
    		super(simulation);
    		this.con=con;
    		this.id_cell=id_cell;
        }
    public void doClose()
        {
    	try {
    		System.out.println("pubblicooooooooooooo");
			con.publishToTopic("EXIT_ZOOM", "GRAPHICS"+id_cell,"GRAPHICS"+id_cell);
		//	Thread.sleep(5000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	super.doClose();
    	
       }
}