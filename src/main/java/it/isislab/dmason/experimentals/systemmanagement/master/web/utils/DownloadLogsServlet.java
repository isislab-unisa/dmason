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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import it.isislab.dmason.experimentals.systemmanagement.master.MasterServer;
import it.isislab.dmason.experimentals.systemmanagement.utils.Simulation;

/**
 * 
 *
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 *
 */
public class DownloadLogsServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	MasterServer masterServer=null;


	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		if(req.getServletContext().getAttribute("masterServer")==null){
			resp.setContentType("text/plain;charset=UTF-8");
			return; 	
		}

		masterServer = (MasterServer) req.getServletContext().getAttribute("masterServer");
		String s_id = (String)req.getParameter("id");
		String path = (String)req.getParameter("path");
		if(s_id==null && path == null){
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		String filePath=null;
		if(s_id!=null){
			int sim_id=Integer.parseInt(s_id);
			masterServer.createZipForHistory(sim_id);//return a boolean true if the file is created 
			Simulation s = masterServer.getSimulationsList().get(sim_id);		
			String log_path=masterServer.getMasterTemporaryFolder();
			filePath= log_path+File.separator+s.getSimName()+s.getSimID()+".zip";
		}else
			filePath = path;


		// reads input file from an absolute path
		File downloadFile = new File(filePath);
		FileInputStream inStream = new FileInputStream(downloadFile);

		// if you want to use a relative path to context root:
		String relativePath = getServletContext().getRealPath("");

		// obtains ServletContext
		ServletContext context = getServletContext();

		// gets MIME type of the file
		String mimeType = context.getMimeType(filePath);
		if (mimeType == null) {        
			// set to binary type if MIME mapping not found
			mimeType = "application/octet-stream";
		}

		// modifies response
		resp.setContentType(mimeType);
		resp.setContentLength((int) downloadFile.length());

		// forces download
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
		resp.setHeader(headerKey, headerValue);

		// obtains response's output stream
		OutputStream outStream = resp.getOutputStream();

		byte[] buffer = new byte[4096];
		int bytesRead = -1;

		while ((bytesRead = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, bytesRead);
		}


		inStream.close();
		outStream.close();

	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

}
