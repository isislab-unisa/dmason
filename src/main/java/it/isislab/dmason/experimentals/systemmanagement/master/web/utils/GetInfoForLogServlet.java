package it.isislab.dmason.experimentals.systemmanagement.master.web.utils;

import it.isislab.dmason.experimentals.systemmanagement.master.MasterServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class GetInfoForLogServlet extends HttpServlet{

	private static final long serialVersionUID = 1L;
	MasterServer masterServer=null;
	
	static boolean DEBUG = true;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/plain;charset=UTF-8");
		if(req.getServletContext().getAttribute("masterServer")==null)
			return;
		JSONObject file;
		JSONArray list_file = new JSONArray();
		PrintWriter p = resp.getWriter();
		masterServer = (MasterServer) req.getServletContext().getAttribute("masterServer");
		String idSimulation = (String)req.getParameter("id");
		String logsPathName = "";
		
		if(DEBUG)
			logsPathName = "/home/flaser/git/dmason/dmason/master/simulations/perfile/runs";
		else
			logsPathName = masterServer.logForSimulationByID(Integer.parseInt(idSimulation));

		
		
		
		File log_root = new File(logsPathName);
		BufferedReader br = null;
		String sCurrentLine = null;
		String content = "";
		if(log_root.isDirectory()){
			for(File f: log_root.listFiles()){
				System.out.println("leggo "+f.getName());
				if(f.exists()){
					file = new JSONObject();
					//lf[i]={fileName:'file'+i,modifiedDate:"22/01/2016"};
					file.put("fileName", f.getName());
					Date date=new Date(f.lastModified());
					SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy");
					String dateText = df2.format(date);
					file.put("modifiedDate", dateText);
					if(f.canRead()){
						br = new BufferedReader(new FileReader(f));
						while ((sCurrentLine = br.readLine()) != null) {
							content+=sCurrentLine+'\n';
						}
						
						file.put("content", content);
					}
					list_file.add(file);
				}
			}
		}
		br.close();
		JSONObject json_files = new JSONObject();
		json_files.put("files", list_file);
		
		StringWriter out = new StringWriter();
		json_files.writeJSONString(out);

		String jsonText = out.toString();
		System.out.println(jsonText);
		p.print(jsonText);
		p.close();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		doGet(req, resp);
	}
}
