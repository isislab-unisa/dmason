package it.isislab.dmason.experimentals.systemmanagement.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Console {

	private java.io.Console console;
	private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	public Console()
	{
		 console = System.console();
		
		 if(console==null)
		 {
			 System.err.println("****************************************************************************************");
			 System.err.println("****************************************************************************************");
			 System.err.println("*           	###  #####  ###  #####  #                                              *");
			 System.err.println("*           	 #  #     #  #  #     # #         ##   #####                           *");
			 System.err.println("*           	 #  #        #  #       #        #  #  #    #                          *");
			 System.err.println("*           	 #   #####   #   #####  #       #    # #####                           *");
			 System.err.println("*           	 #        #  #        # #       ###### #    #                          *");
			 System.err.println("*           	 #  #     #  #  #     # #       #    # #    #                          *");
			 System.err.println("*           	###  #####  ###  #####  ####### #    # #####                           *");
			 System.err.println("*           	                                                                       *");
			 System.err.println("**********DMASON client started in not safe mode, some problems may arise!**************");
			 System.err.println("****************************************************************************************");
			 System.err.println("****************************************************************************************");
			 System.err.println();
		 }
	}
	public void printf(String format,String str)
	{
		if(console!=null)
		{
			console.printf(format, str);
		}else
			System.out.println(String.format(format, str));
	}
	public void printf(String format,Object...objs)
	{
		if(console!=null)
		{
			console.printf(format, objs);
		}else
			System.out.println(String.format(format, objs));
	}
	public void printf(String str)
	{
		if(console!=null)
		{
			console.printf( str);
		}else
			System.out.println(str);
	}
	public String readLine(String format,Object...objs) throws IOException
	{
		if(console!=null)
		{
			return console.readLine(format, objs);
		}
		else{
			this.printf(format, objs);
			return reader.readLine();
		}
	}

}
