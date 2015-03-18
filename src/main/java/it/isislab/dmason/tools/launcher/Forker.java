/**
 * Copyright 2012 Universita' degli Studi di Salerno


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
package it.isislab.dmason.tools.launcher;

import java.util.ArrayList;

	/**
	 * Execute a Java program in a new <i>system</i> thread by launching a new JVM.
	 * @author Luca Vicidomini
	 *
	 */
	class Forker
	{
		private ArrayList<String> commands;
		
		public Forker(Class<?> classToLaunch)
		{
			String separator = System.getProperty("file.separator");
			String classpath = System.getProperty("java.class.path");
			String path = System.getProperty("java.home")
					+ separator + "bin" + separator + "java";
			
			commands = new ArrayList<String>();
			commands.add(path);
			commands.add("-cp");
			commands.add(classpath);
			commands.add(classToLaunch.getName());
		}
		
		public void add(String command)
		{
			this.commands.add(command);
		}

		public Process launch()
		{
			Process subprocess = null;
			try 
			{				
				ProcessBuilder processBuilder = new ProcessBuilder(commands);
				subprocess = processBuilder.start();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return subprocess;
		}

	}