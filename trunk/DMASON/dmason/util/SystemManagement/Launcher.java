package dmason.util.SystemManagement;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * 
 * @author Luca Vicidomini
 *
 */
public class Launcher
{

	public static void main(String[] args)
	{
		HashMap<String, Class<?>> classList = new LinkedHashMap<String, Class<?>>();
		String what = "";
		Class<?> executableClass = null;
		
		classList.put("master", dmason.util.SystemManagement.JMasterUI.class);
		classList.put("worker", dmason.util.SystemManagement.StartWorkerWithGui.class);
		classList.put("server", null);
		classList.put("inspector", dmason.util.visualization.LauncherViewer.class);
		
		if (args.length >= 1)
		{
			what = args[0].toLowerCase();
		}
		
		executableClass = classList.get(what);
		String executableName = new java.io.File(Launcher.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName() + ".jar";
		
		if (executableClass == null)
		{
			System.out.println("Please specify the component you want to launch:");
			System.out.println();

			for (String component : classList.keySet())
			{
				System.out.println("  java -jar " + executableName + component);
			}
			
			System.out.println();
			System.out.println("Visit http://dmason.org for more information.");
			
			System.exit(1);
		}
		
		try {
			// Load all libraries in directory libraries/
			//ClassLoader classLoader = new JarClassLoader(new URL("jar:file:" + executableName + "!/libraries/"));
			//classLoader.getSystemClassLoader().
			
			// args2 will contain args entries except the first one
			String[] args2 = new String[args.length - 1];
			System.arraycopy(args, 1, args2, 0, args2.length);
			
			// Call main method of selected component
			Method mainMethod = executableClass.getDeclaredMethod("main", String[].class);
			mainMethod.invoke(null, new Object[] { args });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
