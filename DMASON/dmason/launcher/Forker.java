package dmason.launcher;

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