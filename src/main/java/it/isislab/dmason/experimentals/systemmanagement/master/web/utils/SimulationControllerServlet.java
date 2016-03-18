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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.isislab.dmason.experimentals.systemmanagement.master.MultiServerInterface;

/**
 * 
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 *
 */
public class SimulationControllerServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	MultiServerInterface masterServer=null;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/plain;charset=UTF-8");
		if(req.getServletContext().getAttribute("masterServer")==null)
			return;

		masterServer =(MultiServerInterface) req.getServletContext().getAttribute("masterServer");

		String id = (String)req.getParameter("id");
		String op = (String)req.getParameter("op");

		System.out.println(id);
		System.out.println(op);

		if(id != null && op!=null){
			int i = Integer.parseInt(id);
			if(op.equals("play"))
				masterServer.start(i);
			else
				if(op.equals("stop"))
					masterServer.stop(i);
				else
					if(op.equals("pause"))
						masterServer.pause(i);
		}else
			System.out.println("something wrong simulation controller servlet");

	}


	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		doGet(req, resp);
	}
}
