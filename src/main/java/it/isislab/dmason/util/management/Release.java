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

import java.util.Calendar;
import java.util.Date;

/**
 * This class is just a centralized class where we store information about the current D-MASON version. 
 * @author Luca Vicidomini
 *
 */
public class Release
{
	public static int MAJOR_VERSION = 3;
	public static int MINOR_VERSION = 0;
	
	public static String PRODUCT_NAME = "D-MASON"; 
	
	public static int RELEASE_DATE_YEAR  = 2014;
	public static int RELEASE_DATE_MONTH = 03;
	public static int RELEASE_DATE_DATE  = 31;
	
	public static String VERSION_STRING = MAJOR_VERSION + "." + MINOR_VERSION;
	public static String PRODUCT_RELEASE = PRODUCT_NAME + " " + VERSION_STRING;
	
	public static Date getReleaseDate()
	{
		Calendar cal = Calendar.getInstance();
		cal.set(RELEASE_DATE_YEAR, RELEASE_DATE_MONTH, RELEASE_DATE_DATE);
		return cal.getTime();
	}
}
