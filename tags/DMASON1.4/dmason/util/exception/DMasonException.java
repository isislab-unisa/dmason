package dmason.util.exception;

/**
 * A specific exception for Distributed Mason
 */
public class DMasonException extends Exception 
{
	public DMasonException() 
	{
		super();
	}

	public DMasonException(String arg0, Throwable arg1) 
	{
		super(arg0, arg1);
	}

	public DMasonException(String arg0) 
	{
		super(arg0);
	}

	public DMasonException(Throwable arg0) 
	{
		super(arg0);
	}	
}
