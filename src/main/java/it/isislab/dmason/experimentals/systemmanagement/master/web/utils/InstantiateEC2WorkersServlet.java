/**
 * Copyright 2017 Universita' degli Studi di Salerno
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
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesResult;

import it.isislab.dmason.experimentals.systemmanagement.backends.amazonaws.AmazonService;

/**
 *
 * @author Simone Bisogno
 *
 */
public class InstantiateEC2WorkersServlet
		extends HttpServlet
{
	// constants
	private static final Logger LOGGER = Logger.getGlobal();
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		this.newEC2Instance(request, response);

		// set response
		response.setContentType("text/plain;charset=UTF-8");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		this.newEC2Instance(request, response);

		// set response
		response.setContentType("text/plain;charset=UTF-8");
	}

	// helper methods
	private void newEC2Instance(HttpServletRequest request, HttpServletResponse response)
	{
		// comment or properly edit following line to enable logging
//		LOGGER.setLevel(Level.OFF);

		// extract data from request
		String ec2Type = request.getParameter("instancetype");
		if (ec2Type == null || ec2Type == "")
		{
			LOGGER.severe("EC2 type is null or invalid!");
			return;
		}

		int numInstances = 0;
		try
		{
			numInstances = Integer.parseInt(request.getParameter("numinstances"));
		}
		catch (NumberFormatException e)
		{
			LOGGER.severe(e.getClass().getSimpleName() + ": " + e.getMessage() + ".");
		}

		LOGGER.info("Received " + numInstances + " new " + ec2Type + " EC2 instance request");

		// create instance on EC2
		RunInstancesResult instancesResult = null;
		AmazonService.setType(ec2Type);
		AmazonService.boot();
		try
		{
			instancesResult = AmazonService.createInstance(numInstances);
		}
		catch (IOException e)
		{
			LOGGER.severe(e.getClass().getSimpleName() + ": " + e.getMessage() + ".");
		}
		Iterator<Instance> instanceIterator = instancesResult.getReservation().getInstances().iterator();
		while (instanceIterator.hasNext())
		{
			LOGGER.info("Generated instance " + instanceIterator.next().getInstanceId());
		}
	}
}
