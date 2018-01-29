/**
 * Copyright 2018 Universita' degli Studi di Salerno
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.isislab.dmason.experimentals.systemmanagement.master.web.utils;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Bisogno Simone (05121/1177)
 *
 */
public class GetEC2InfoServlet
		extends HttpServlet
{
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		// send EC2 info for request panel
		this.retrieveEC2Info(request, response);

		response.setStatus(HttpServletResponse.SC_OK);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		// send EC2 info for request panel
		this.retrieveEC2Info(request, response);

		response.setStatus(HttpServletResponse.SC_OK);
	}

	// helper methods
	private void retrieveEC2Info(HttpServletRequest req, HttpServletResponse res)
	{
		LOGGER.setLevel(Level.ALL); // set to Level.OFF to disable logging

		// actually retrieve necessary EC2 data
		
	}

	// constants
	private static final Logger LOGGER = Logger.getGlobal();
	private static final long serialVersionUID = 1L;
}
