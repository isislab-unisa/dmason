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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JOptionPane;

import sim.display.Console;
/**
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 */
public class CloseZoomAppListener extends WindowAdapter {
    public ConnectionNFieldsWithActiveMQAPI con;
	public String id_Cell;
	public Console c;
	public CloseZoomAppListener(Console c,ConnectionNFieldsWithActiveMQAPI con,
			String id_Cell)
	{
		this.con=con;
		this.id_Cell=id_Cell;
		this.c=c;
	}
	@Override
	public void windowClosing(WindowEvent e)
	{
		try {
			
			con.publishToTopic("EXIT_ZOOM","GRAPHICS"+id_Cell, "GRAPHICS"+id_Cell);
			JOptionPane.showMessageDialog(null,"Successfully Send Disconnection zoom ack!");
			c.doClose();
			
			System.exit(0);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null,"Not Successfully Send Disconnection zoom ack!");
		}
		
	}
	

}
