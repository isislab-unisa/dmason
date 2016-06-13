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
package it.isislab.dmason.util.connection.mpi;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.TreeMap;

/**
 * @author Michele Carillo
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 *
 */
public class UtilConnectionMPI {
	public static TreeMap<String, MPITopic> checkAndGetMPIConfiguration(String md5) throws NoSuchAlgorithmException, IOException, ClassNotFoundException
	{
		final String dir = System.getProperty("user.dir");
		File curr_dir=new File(dir+File.separator+"DMASON-CONF-MPI");
		if(!curr_dir.exists())
			if(!curr_dir.mkdir())
				return null;
		TreeMap<String, MPITopic> tree=null;
		for (File filetest : curr_dir.listFiles()) {

			if(filetest.getName().equalsIgnoreCase(md5+".it.isislab.dmason.temp.mpi.configuration.topics"))
			{
				ObjectInputStream stream=new ObjectInputStream(new FileInputStream(filetest));
				tree=(TreeMap<String, MPITopic>) stream.readObject();
				return tree;
			}
		}

		return null;
	}
	public static boolean saveMPIConfiguration(String md5, TreeMap<String, MPITopic> topics) throws NoSuchAlgorithmException, IOException, ClassNotFoundException
	{
		ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream("DMASON-CONF-MPI"+File.separator+md5+".it.isislab.dmason.temp.mpi.configuration.topics"));
		out.writeObject(topics);
		out.close();
		return true;
	}
	public static String MD5(String md5) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
		}
		return null;
	}
}
