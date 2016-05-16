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
package it.isislab.dmason.experimentals.systemmanagement.loader;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
* A customize Classloader to avoid dynamically classloading problem 
* of the same path from multiple jar 
* 
* @author Michele Carillo
* @author Carmine Spagnuolo
* @author Flavio Serrapica
*
*/
public class DMasonClassLoader extends ClassLoader {
	private static boolean DEBUG_FIND_CLASS = false;


	public DMasonClassLoader(ClassLoader parent){

		super(parent);

	}


	public DMasonClassLoader(){}

	/**
	 * Find the specific class
	 * 
	 * @param pathname of class. Class name without .class extension
	 * 
	 */
	protected Class findClass(String name) throws ClassNotFoundException
	{
		byte[] classBytes = null;
		try
		{
			if(DEBUG_FIND_CLASS) System.out.println(name);

			//load byte of class in a matrix of bytes
			classBytes = loadClassBytes(name);

		}
		catch(IOException exception)
		{
			throw new ClassNotFoundException(name);
		}

		Class cl = defineClass(null, classBytes, 0 ,classBytes.length);

		if(cl == null)
		{
			throw new ClassNotFoundException(name);
		}

		return cl;

	}

	/**
	 * Load the bytes of class 
	 * @param classname without .class
	 * @return byte[] with bytecode of class.
	 */
	private byte[] loadClassBytes(String name) throws IOException
	{
		name = name + ".class";

		FileInputStream in = null;

		try
		{

			in = new FileInputStream(name);

			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int ch;

			while((ch = in.read()) != -1)
			{
				byte b = (byte)ch;

				buffer.write(b);
			}

			in.close();

			return buffer.toByteArray();
		}
		finally
		{
			if(in != null) in.close();
		}
	}
}
