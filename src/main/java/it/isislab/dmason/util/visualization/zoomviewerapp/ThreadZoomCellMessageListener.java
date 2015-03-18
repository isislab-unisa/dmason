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
/**
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 */
public class ThreadZoomCellMessageListener extends Thread{
	
	
	private ConnectionNFieldsWithActiveMQAPI connection;
	private String id_cell;
	private ZoomViewer zoom;
	public ThreadZoomCellMessageListener(ConnectionNFieldsWithActiveMQAPI conn,String id_cell,ZoomViewer zoom)
	{
		this.id_cell=id_cell;
		this.connection=conn;
		this.zoom=zoom;
	}
	
	@Override
	public void run()
	{
		try{
			connection.createTopic("GRAPHICS"+id_cell,1);
			connection.subscribeToTopic("GRAPHICS"+id_cell);
			connection.asynchronousReceive("GRAPHICS"+id_cell,
					new ZoomCellMessageListener(zoom));
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
