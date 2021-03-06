/**
 * Copyright 2016 Universita' degli Studi di Salerno


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
package it.isislab.dmason.experimentals.systemmanagement.master.web.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import it.isislab.dmason.experimentals.systemmanagement.master.MasterServer;
import it.isislab.dmason.experimentals.systemmanagement.utils.Simulation;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.util.connection.ConnectionType;

/**
 * 
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 *
 */
public class SubmitSimulationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	MasterServer server = null;
	final static String ACTIVEMQ = "ActiveMQ";
	final static String MPI = "MPI";
	HashMap<String,String> listParams = null;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/plain;charset=UTF-8");
		if (req.getServletContext().getAttribute("masterServer") == null)
			return;

		server = (MasterServer) req.getServletContext().getAttribute("masterServer");
		listParams = new HashMap<>();

		/*if*****************only for new jar upload ***********************************************/

		FileItem jarSim = null;

		if (!ServletFileUpload.isMultipartContent(req)) {
			return;
		} else {
			FileItemFactory itemFact = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(itemFact);
			try {
				List<FileItem> items = upload.parseRequest(req);

				System.out.println(items);
				for (FileItem item: items) {
					if (!item.isFormField()) {
						jarSim = item;
//						item.write(file);
//						System.out.println("hai sottomesso un file " + jarSim);
					} else {
//						System.out.println(item.getFieldName() + " " + item.getString());
						listParams.put(item.getFieldName(), item.getString());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		/********************************************************************/

		/**else ************************************************/

		// PARAMETERS FROM CLIENT		
		String simName = listParams.get("simName");
		int rows = (listParams.get("rows") != null) ? Integer.parseInt(listParams.get("rows")) : 0;
		int columns = (listParams.get("cols") != null) ? Integer.parseInt(listParams.get("cols")) : 0;
		int depths = (listParams.get("depth") !=null) ? Integer.parseInt(listParams.get("depth")) :0;
		int aoi = Integer.parseInt(listParams.get("aoi"));
		int width = Integer.parseInt(listParams.get("width"));
		int height = Integer.parseInt(listParams.get("height"));
		int lenght = (listParams.get("length") !=null) ? Integer.parseInt(listParams.get("length")) :0;
		int numAgent = Integer.parseInt(listParams.get("numAgents"));
		long numStep = Long.parseLong(listParams.get("step"));
		String conType = listParams.get("connectionType");
		String modeType = listParams.get("partitioning");
		int cells = (listParams.get("cells") != null) ? Integer.parseInt(listParams.get("cells")) : -1;
		int mode = (modeType.equals("uniform") || modeType.equals("three-dim")) ? DistributedField2D.UNIFORM_PARTITIONING_MODE : DistributedField2D.NON_UNIFORM_PARTITIONING_MODE;
		String exampleSimulation = listParams.get("exampleSimulation");

		// connection
		int connection = ConnectionType.pureActiveMQ;

		if (conType != null && conType.equalsIgnoreCase(MPI)) {
			connection = ConnectionType.pureMPIParallel; 
			System.out.println("MPI setting are not implemented yet ");
		}

		// topics
		String topics[] = listParams.get("workers").split(",");
		ArrayList<String> topicList = new ArrayList<String>();
		
		int numCell=0;
		switch (mode) {
			case DistributedField2D.UNIFORM_PARTITIONING_MODE:
				if(depths!=0) {
					numCell = rows * columns * depths;
				}else {
					numCell = rows * columns;
				}
				if (numCell < topics.length) {
					for (int i = 0; i < numCell; i++) {
						topicList.add(topics[i]);
					}
				} else {
					for(String x: topics) 
						topicList.add(x);
				}
				break;
			case DistributedField2D.NON_UNIFORM_PARTITIONING_MODE:
				if (cells < topics.length) {
					for (int i = 0; i < cells; i++) {
						topicList.add(topics[i]);
					}
				} else {
					for(String x: topics) 
						topicList.add(x);
				}
				break;
		}

		int simId = server.getKeySim().incrementAndGet();
		String simPath = server.getSimulationsDirectories() + File.separator + simName + simId;

		server.createSimulationDirectoryByID(simName, simId);

		String simPathJar = simPath + File.separator + "jar";
		String jarSimName = "";

		if (exampleSimulation != "" && jarSim.getName().equals("")) {
			File f = new File(exampleSimulation);
			jarSimName = f.getName();
			FileUtils.copyFileToDirectory(f, new File(simPathJar));
		} else {
			// for new sim, must copy this jar in 
			server.copyJarOnDirectory(simPathJar, jarSim);
			jarSimName = jarSim.getName();
		}

		Simulation sim = null;

		if (mode == DistributedField2D.UNIFORM_PARTITIONING_MODE)
			if (depths==0) {
				sim = new Simulation(simName, simPath, simPathJar + File.separator + jarSimName, rows, columns, aoi, width, height, numAgent, numStep, mode, connection);
			}else{
				sim = new Simulation(simName, simPath, simPathJar + File.separator + jarSimName, rows, columns,depths, aoi, width, height,lenght, numAgent, numStep, mode, connection);
			}
		else
			sim = new Simulation(simName, simPath, simPathJar + File.separator+jarSimName, cells, aoi, width, height, numAgent, numStep, mode, connection);	

		sim.setTopicList(topicList);
		sim.setNumWorkers(topicList.size());
		sim.setSimID(simId);
		sim.setTopicPrefix(simName + "-" + simId);

		if (server.submitSimulation(sim)) {
			resp.setStatus(HttpServletResponse.SC_OK);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}
}
