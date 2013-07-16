package dmason.master;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class ConnectionUI extends JFrame 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txtAddress;
	private JTextField txtPort;
	private JButton btnConnect;
	
	private ActionListener connectActionListener;
	
	public ConnectionUI()
	{
		super();
		this.setTitle("Connection");
		setupLayout();
		this.pack();
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void setupLayout()
	{
		JLabel lblAddress = new JLabel("Address:");
		txtAddress = new JTextField("127.0.0.1");
		Dimension addressDimensions = txtAddress.getPreferredSize();
		addressDimensions.width = 200;
		txtAddress.setPreferredSize(addressDimensions);

		JLabel lblPort = new JLabel("Port:");
		txtPort = new JTextField("61616");
		Dimension portDimensions = txtPort.getPreferredSize();
		portDimensions.width *= 1.5;
		txtPort.setPreferredSize(portDimensions);
		
		btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				ConnectionUI.this.txtAddress.setEnabled(false);
				ConnectionUI.this.txtPort.setEnabled(false);
				ConnectionUI.this.btnConnect.setEnabled(false);
				
				connectActionListener.actionPerformed(e);
			}
		});
		
		this.setLayout(new GridBagLayout());
		this.add(lblAddress, new GridBagConstraints(0, 0, 1, 1, .0, .0, GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, new Insets(10, 10, 0, 10), 0, 0));
		this.add(txtAddress, new GridBagConstraints(1, 0, 1, 1, .0, .0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, new Insets(10, 0, 0, 10), 0, 0));
		this.add(lblPort, new GridBagConstraints(0, 1, 1, 1, .0, .0, GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, new Insets(10, 10, 0, 10), 0, 0));
		this.add(txtPort, new GridBagConstraints(1, 1, 1, 1, .0, .0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(10, 0, 0, 10), 0, 0));
		this.add(btnConnect, new GridBagConstraints(1, 2, 1, 1, .0, .0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(10, 0, 10, 10), 0, 0));
		
		btnConnect.getRootPane().setDefaultButton(btnConnect);
	}


	public void addConnectActionListener(ActionListener actionListener)
	{
		connectActionListener = actionListener;		
	}
	
	public String getAddress()
	{
		return txtAddress.getText();
	}
	
	public String getPort()
	{
		return txtPort.getText();
	}


}
