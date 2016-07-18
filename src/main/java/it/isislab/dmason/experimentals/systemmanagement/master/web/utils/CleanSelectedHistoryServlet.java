package it.isislab.dmason.experimentals.systemmanagement.master.web.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import it.isislab.dmason.experimentals.systemmanagement.master.MasterServer;

/**
 * Delete all files from history folder for selected simulations
 * 
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 *
 */
public class CleanSelectedHistoryServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	MasterServer myServer =null;
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/plain;charset=UTF-8");
		if(req.getServletContext().getAttribute("masterServer")==null){
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return; 	
		}

		myServer = (MasterServer) req.getServletContext().getAttribute("masterServer");
		String hitory_pathName = myServer.getMasterHistory();
		File f_history = new File(hitory_pathName);
		if(!f_history.exists() || !f_history.isDirectory()){
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return; 	
		}

		String jsonToParse=req.getParameter("paths");
		ArrayList<String> simPaths=new ArrayList<String>();
		try { JSONParser parser=new JSONParser();
		String ppp=""+parser.parse(jsonToParse);
		JSONObject obj= (JSONObject) parser.parse(ppp);
		JSONArray arr=(JSONArray) obj.get("paths");
		for(int i=0; i< arr.size();i++){
			JSONObject j=(JSONObject) arr.get(i);
			simPaths.add(""+j.get("path"));
		}
		if(myServer.deleteHistory(simPaths)){
			resp.setStatus(HttpServletResponse.SC_OK);
		}		
		} catch (ParseException e) {
			e.printStackTrace();
		}



		resp.setStatus(HttpServletResponse.SC_OK);

	}


	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doGet(req, resp);
	}

}
