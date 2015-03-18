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
package it.isislab.dmason.util.visualization.sim.app.DAntsForage;

import it.isislab.dmason.util.connection.Address;
import it.isislab.dmason.util.connection.jms.activemq.ConnectionNFieldsWithActiveMQAPI;
import it.isislab.dmason.util.visualization.zoomviewerapp.ConsoleZoom;
/**
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 */
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
