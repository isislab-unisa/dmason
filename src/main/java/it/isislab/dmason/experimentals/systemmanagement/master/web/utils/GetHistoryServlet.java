package it.isislab.dmason.experimentals.systemmanagement.master.web.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import it.isislab.dmason.experimentals.systemmanagement.master.MasterServer;
import it.isislab.dmason.experimentals.systemmanagement.master.MultiServerInterface;

public class GetHistoryServlet extends HttpServlet {
	
	MasterServer myServer =null;
	

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/plain;charset=UTF-8");
		if(req.getServletContext().getAttribute("masterServer")==null){
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return; 	
		}
		PrintWriter p = resp.getWriter();
		myServer = (MasterServer) req.getServletContext().getAttribute("masterServer");
		String hitory_pathName = myServer.getMasterHistory();
		File f_history = new File(hitory_pathName);
		if(!f_history.exists() || !f_history.isDirectory()){
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return; 	
		}
		JsonObject obj = null;
		JsonArray history_list = new JsonArray();
		File[] cur_dir_files = null;
		Properties prop = null;
		InputStream in = null;
		for(File f: f_history.listFiles()){
			if(!f.isDirectory()) continue;
			cur_dir_files = f.listFiles(new FileFilter() {
				
				@Override
				public boolean accept(File pathname) {
					
					return pathname.getName().endsWith(".history");
				}
			});
			prop = new Properties();
			in = new FileInputStream(cur_dir_files[0]);
			prop.load(in);
			obj = new JsonObject();
			obj.add("", null);
		}
		p.close();
	}	

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}
}
