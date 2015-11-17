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

package it.isislab.dmason.util.management.Worker;

import it.isislab.dmason.exception.NoDigestFoundException;
import it.isislab.dmason.util.management.DigestAlgorithm;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * This class manage the digest associated to jar file
 * @author marvit
 *
 */
public class Digester 
{
	private String digestAlg;
	private String digest;
	
	public Digester(String digAlg)
	{
		digestAlg = digAlg;
	}
	
	public String getDigest(byte data[])
	{
		if(digestAlg.equals(DigestAlgorithm.MD2))
			digest = DigestUtils.md5Hex(data);
		
		if(digestAlg.equals(DigestAlgorithm.MD5))
			digest = DigestUtils.md5Hex(data);
		if(digestAlg.equals(DigestAlgorithm.SHA1))
			digest = DigestUtils.sha256Hex(data);
		
		return digest;
	}
	
	public String getDigest(InputStream data) throws IOException
	{
		if(digestAlg.equals(DigestAlgorithm.MD2))
			digest = DigestUtils.md5Hex(data);
		if(digestAlg.equals(DigestAlgorithm.MD5))
			digest = DigestUtils.md5Hex(data);
		if(digestAlg.equals(DigestAlgorithm.SHA1))
			digest = DigestUtils.sha256Hex(data);
		
		return digest;
	}
	
	public String getDigest(String data)
	{
		if(digestAlg.equals(DigestAlgorithm.MD2))
			digest = DigestUtils.md5Hex(data);
		if(digestAlg.equals(DigestAlgorithm.MD5))
			digest = DigestUtils.md5Hex(data);
		if(digestAlg.equals(DigestAlgorithm.SHA1))
			digest = DigestUtils.sha256Hex(data);
		
		return digest;
	}
	
	public void storeToPropFile(String path) throws NoDigestFoundException
	{
		if( digest == null) throw new NoDigestFoundException();
		else
		{
			Properties prop = new Properties();
			 
	    	try {
	    		
	    		prop.setProperty(digestAlg, digest);
	    		
	    		prop.store(new FileOutputStream(path), null);
	 
	    	} catch (IOException ex) {
	    		ex.printStackTrace();
	        }
		}
		
	}
	
	public String loadFromPropFile(String path) 
	{
		Properties prop = new Properties();
		 
    	try {
            
    		prop.load(new FileInputStream(path));
 
    		return prop.getProperty(digestAlg);
    		
    	} catch (IOException ex) {
    		ex.printStackTrace();
    		return null;
        }
	}
	
}
