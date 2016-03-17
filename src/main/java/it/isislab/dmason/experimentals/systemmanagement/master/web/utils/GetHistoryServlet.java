package it.isislab.dmason.experimentals.systemmanagement.master.web.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import it.isislab.dmason.experimentals.systemmanagement.master.MasterServer;

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
		JSONObject obj = null;
		JSONArray history_list = new JSONArray();
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
			obj = new JSONObject();
			Enumeration<Object> prop_keys = prop.keys(); 
			while(prop_keys.hasMoreElements()){
				String k = (String) prop_keys.nextElement();
				obj.put(k, prop.getProperty(k));
			}
			history_list.add(obj);
		}
		JSONObject list_to_send = new JSONObject();
		list_to_send.put("history", history_list);
		StringWriter out = new StringWriter();
		list_to_send.writeJSONString(out);

		String jsonText = out.toString();
		//System.out.println(jsonText);
		p.print(jsonText);
		p.close();
	}	

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}
}
