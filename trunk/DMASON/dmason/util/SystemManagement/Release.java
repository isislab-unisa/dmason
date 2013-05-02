package dmason.util.SystemManagement;

import java.util.Calendar;
import java.util.Date;

/**
 * This class is just a centralized class where we store information about the current D-MASON version. 
 * @author Luca Vicidomini
 *
 */
public class Release
{
	public static int MAJOR_VERSION = 2;
	public static int MINOR_VERSION = 1;
	
	public static String PRODUCT_NAME = "D-MASON"; 
	
	public static int RELEASE_DATE_YEAR  = 2013;
	public static int RELEASE_DATE_MONTH = 05;
	public static int RELEASE_DATE_DATE  = 02;
	
	public static String VERSION_STRING = MAJOR_VERSION + "." + MINOR_VERSION;
	public static String PRODUCT_RELEASE = PRODUCT_NAME + " " + VERSION_STRING;
	
	public static Date getReleaseDate()
	{
		Calendar cal = Calendar.getInstance();
		cal.set(RELEASE_DATE_YEAR, RELEASE_DATE_MONTH, RELEASE_DATE_DATE);
		return cal.getTime();
	}
}
