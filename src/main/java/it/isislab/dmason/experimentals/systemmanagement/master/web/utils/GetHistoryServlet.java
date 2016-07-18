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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import it.isislab.dmason.experimentals.systemmanagement.master.MasterServer;

public class GetHistoryServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 * @author Michele Carillo
	 * @author Carmine Spagnuolo
	 * @author Flavio Serrapica
	 *
	 */

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
		Map<Integer,JSONObject> ordered=new HashMap<Integer,JSONObject>();

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
			in.close();
			String mykey=""+prop.get("simID");
			int keyConv=Integer.parseInt(mykey);
			ordered.put(keyConv, obj);
			//history_list.add(obj);
		}

		for(int k:ordered.keySet()){
			history_list.add(ordered.get(k));
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
