package it.isislab.dmason.experimentals.systemmanagement.master.web.utils;

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

public class ShutdownWorkersServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MasterServer masterServer =null;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/plain;charset=UTF-8");
		if(req.getServletContext().getAttribute("masterServer")==null)
			return;

		masterServer = (MasterServer) req.getServletContext().getAttribute("masterServer");
		String jsonToParse=req.getParameter("topics");
		ArrayList<String> topics=new ArrayList<String>();
		try { JSONParser parser=new JSONParser();
		String ppp=""+parser.parse(jsonToParse);
		JSONObject obj= (JSONObject) parser.parse(ppp);
		JSONArray arr=(JSONArray) obj.get("list");
		for(int i=0; i< arr.size();i++){
			JSONObject j=(JSONObject) arr.get(i);
			topics.add(""+j.get("id"));
		}

		if(masterServer.shutdownAllWorkers(topics)){
			System.out.println("ok");
			resp.setStatus(HttpServletResponse.SC_OK);
		}
		} catch (ParseException e) {
			e.printStackTrace();
		}



	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		doGet(req, resp);
	}

}
