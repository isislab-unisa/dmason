package dmason.util.visualization;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JOptionPane;

import sim.display.Console;

import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;

public class CloseZoomAppListener extends WindowAdapter {
    public ConnectionNFieldsWithActiveMQAPI con;
	public String id_Cell;
	public Console c;
	public CloseZoomAppListener(Console c,ConnectionNFieldsWithActiveMQAPI con,
			String id_Cell)
	{
		this.con=con;
		this.id_Cell=id_Cell;
		this.c=c;
	}
	public void windowClosing(WindowEvent e)
	{
		try {
			
			con.publishToTopic("EXIT_ZOOM","GRAPHICS"+id_Cell, "GRAPHICS"+id_Cell);
			JOptionPane.showMessageDialog(null,"Successfully Send Disconnection zoom ack!");
			c.doClose();
			
			System.exit(0);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null,"Not Successfully Send Disconnection zoom ack!");
		}
		
	}
	

}
