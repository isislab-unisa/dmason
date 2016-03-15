package it.isislab.dmason.experimentals.systemmanagement.master.web.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.hadoop.fs.FileUtil;

import it.isislab.dmason.experimentals.systemmanagement.master.MasterServer;
import it.isislab.dmason.experimentals.systemmanagement.utils.DMasonFileSystem;
import it.isislab.dmason.experimentals.systemmanagement.utils.Simulation;
import it.isislab.dmason.experimentals.systemmanagement.utils.ZipDirectory;

public class DownloadLogsServlet extends HttpServlet {

	MasterServer masterServer=null;

	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		if(req.getServletContext().getAttribute("masterServer")==null){
			resp.setContentType("text/plain;charset=UTF-8");
			return; 	
		}
		
		masterServer = (MasterServer) req.getServletContext().getAttribute("masterServer");
		String s_id = (String)req.getParameter("id");
		if(s_id==null)
			return;
		
		int sim_id=Integer.parseInt(s_id);
		
		
		
		masterServer.createZipForHistory(sim_id);//return a boolean true if the file is created 
		
		Simulation s = masterServer.getSimulationsList().get(sim_id);		
		String log_path=masterServer.getMasterTemporaryFolder();
		String filePath = log_path+File.separator+s.getSimName()+s.getSimID()+".zip";
		
		
		
		//System.out.println("Creato zipppettone!!!");
		// reads input file from an absolute path
        File downloadFile = new File(filePath);
        FileInputStream inStream = new FileInputStream(downloadFile);
         
        // if you want to use a relative path to context root:
        String relativePath = getServletContext().getRealPath("");
        //System.out.println("relativePath = " + relativePath);
         
        // obtains ServletContext
        ServletContext context = getServletContext();
         
        // gets MIME type of the file
        String mimeType = context.getMimeType(filePath);
        if (mimeType == null) {        
            // set to binary type if MIME mapping not found
            mimeType = "application/octet-stream";
        }
        //System.out.println("MIME type: " + mimeType);
         
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
		// TODO Auto-generated method stub
		doGet(req, resp);
	}

}
