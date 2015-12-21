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

package it.isislab.dmason.util.management;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.jar.Attributes;

import it.isislab.dmason.experimentals.tools.batch.data.EntryParam;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;

/**
 * This class contains the mechanism for dynamically load jar
 * @author marvit
 *
 */
public class JarClassLoader extends URLClassLoader 
{

	private URL url;

	public JarClassLoader(URL url) {
	    super(new URL[] { url });
	    this.url = url;
	}
	
	public String getMainClassName() throws IOException 
	{
	    URL u = new URL("jar", "", url + "!/");
	    JarURLConnection uc = (JarURLConnection)u.openConnection();
	    Attributes attr = uc.getMainAttributes();
	    return attr != null
	                   ? attr.getValue(Attributes.Name.MAIN_CLASS)
	                   : null;
	}

	public void addToClassPath() throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException 
	{
		
			URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
			Class urlClass = URLClassLoader.class;
			Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
			method.setAccessible(true);
			method.invoke(urlClassLoader, new Object[]{url});
	}
	 
	public void invokeClass(String name, Object[] args)
		    throws ClassNotFoundException,
		           NoSuchMethodException,
		           InvocationTargetException
		{
		    Class c = loadClass(name);
		    
		   
		    Method m = c.getMethod("main", new Class[] { args.getClass() });
		    m.setAccessible(true);
		    int mods = m.getModifiers();
		    if (m.getReturnType() != void.class || !Modifier.isStatic(mods) ||
		        !Modifier.isPublic(mods)) {
		        throw new NoSuchMethodException("main");
		    }
		    try {
		        m.invoke(null, new Object[] { args });
		    } catch (IllegalAccessException e) {
		        // This should not happen, as we have disabled access checks
		    }
		}
	
	public Object getInstance(String className, GeneralParam args_sim) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		 Class simClass = loadClass(className);
		 Constructor constr = simClass.getConstructor(new Class[]{ args_sim.getClass() });
		 Object obj = constr.newInstance(new Object[]{ args_sim });
		
		 return obj;
	}
	
	public Object getInstance(String className, GeneralParam args_gen, List<EntryParam<String, Object>> simParam, String topicPrefix) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException 
	{
		Class simClass = loadClass(className);



		Constructor constr = simClass.getConstructor(new Class[]{ args_gen.getClass(),List.class, String.class});
		Object obj = constr.newInstance(new Object[]{ args_gen ,simParam, topicPrefix});

		return obj;
	}
	

	

}
