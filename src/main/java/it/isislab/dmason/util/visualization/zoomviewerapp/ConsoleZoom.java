/**
 * Copyright 2012 Universita' degli Studi di Salerno


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package it.isislab.dmason.util.visualization.zoomviewerapp;

import it.isislab.dmason.util.connection.jms.activemq.ConnectionNFieldsWithActiveMQAPI;
import it.isislab.dmason.util.visualization.globalviewer.Display;
import sim.display.GUIState;
/**
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 */
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
    		} catch (Exception e) {
    			System.out.println("Zoom uncorrectly disconnect. Possible problem in your simulation...");
    			e.printStackTrace();
    		}	
    }
    @Override
	public void doClose()
    {
    		sendAck();
    		pressStop();  
    	
            getSimulation().quit();  
    		dispose();
    	    allControllers.remove(this);
    	    
			
    	   try {
    			Display display = new Display(
    					con, mode, 
    					numCell, width, 
    					height, absolutePath,simul,
    					getSimulation().
    					getSimulationInspectedObject().
    					getClass().
    					getCanonicalName());
    			   			
		
		} catch (Exception e) {
			System.out.println("Problemi in chiusura Console ZOOM!!!!");
			e.printStackTrace();
		}
   	
  
    }
}