package dmason.util.SystemManagement;
import java.awt.*;
import java.awt.event.*;
import java.net.InetAddress;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;
import java.util.TimerTask;

import javax.swing.*;

import dmason.util.connection.Address;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;

/**
 * Executable, GUI version worker.
 *
 *@author Mario Fiore Vitale (reconnection)
 */
public class StartWorkerWithGui extends JFrame implements StartWorkerInterface , Observer
{	
	private static final long serialVersionUID = 1L;
	public boolean START = false;
	
	//private static final long SCHEDULE_INTERVAL = 10+1000L;//2 * 60 * 1000L;
	//private static final long SCHEDULE_DELAY = 10 * 1000L;
	//private java.util.Timer timer = new java.util.Timer();
	
	/**
	 * Connection with the provider.
	 */
	private ConnectionNFieldsWithActiveMQAPI connection;
	
	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - aaaa aaaa
	private JScrollPane scrollPane1;
	public JTextArea textArea;
	private JButton btnConnect;
	private JLabel label1;
	private JLabel label2;
	private JComboBox cmbPort;
	private JLabel label3;
	public JLabel labelnumber;
	private JLabel lblLogo;
	private JComboBox cmbIp;

	public StartWorkerWithGui()
	{
		initComponents();
		connection = new ConnectionNFieldsWithActiveMQAPI();
		
		connection.addObserver(this);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
		
		//initSchedule();
	}

	private void initComponents() {
		scrollPane1 = new JScrollPane();
		textArea = new JTextArea();
		textArea.setEditable(false);
		btnConnect = new JButton();
		label1 = new JLabel();
		label2 = new JLabel();
		cmbPort = new JComboBox();
		
		label3 = new JLabel();
		labelnumber = new JLabel();
		lblLogo = new JLabel();
		cmbIp = new JComboBox();
		
		//======== this ========
		setTitle("D.MASON WORKER");
		Container contentPane = getContentPane();
	
		//======== scrollPane1 ========
		{
			scrollPane1.setViewportView(textArea);
		}
	
		//---- btnConnect ----
		btnConnect.setText("Connect");
		btnConnect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnConnectOnClick(e);
			}
		});
	
		//---- label1 ----
		label1.setText("Server IP:");
	
		//---- label2 ----
		label2.setText("Server port:");
	
		//---- label3 ----
		label3.setText("");
	
		//---- labelnumber ----
		labelnumber.setText("");
	
		//---- label4 ----
		lblLogo.setText("Distributed Mason 'failed to load image'");
		lblLogo.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/carmineworker.png")));
	
		//---- comboBox ----
		cmbIp.setEditable(true);
		cmbPort.setEditable(true);
	
		Scanner in=new Scanner(ClassLoader.getSystemClassLoader().getResourceAsStream("dmason/resource/file/urlworker"));
		while(in.hasNext())
		{
			String line=in.nextLine();
			String[] args=line.split(":");
			cmbIp.addItem(args[0]);
			cmbPort.addItem(args[1]);
		}
		
		cmbIp.setSelectedIndex(0);
		cmbPort.setSelectedIndex(0);
	
	
		GroupLayout contentPaneLayout = new GroupLayout(contentPane);
		contentPane.setLayout(contentPaneLayout);
		contentPaneLayout.setHorizontalGroup(
			contentPaneLayout.createParallelGroup()
				.addGroup(contentPaneLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(contentPaneLayout.createParallelGroup()
						.addComponent(lblLogo, GroupLayout.PREFERRED_SIZE, 400, GroupLayout.PREFERRED_SIZE)
						.addGroup(contentPaneLayout.createParallelGroup()
							.addGroup(contentPaneLayout.createSequentialGroup()
								.addGroup(contentPaneLayout.createParallelGroup()
									.addComponent(label2)
									.addComponent(label1))
								.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
									.addComponent(cmbPort)
									.addComponent(cmbIp, 0, 119, Short.MAX_VALUE))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(contentPaneLayout.createParallelGroup()
									.addGroup(contentPaneLayout.createSequentialGroup()
										.addGap(78, 78, 78)
										.addComponent(btnConnect))
									.addGroup(contentPaneLayout.createSequentialGroup()
										.addGap(46, 46, 46)
										.addComponent(label3)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(labelnumber, GroupLayout.PREFERRED_SIZE, 69, GroupLayout.PREFERRED_SIZE))))
							.addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
								.addGap(9, 9, 9)
								.addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 366, GroupLayout.PREFERRED_SIZE)
								.addGap(462, 462, 462))))
					.addGap(20, 20, 20))
		);
		contentPaneLayout.setVerticalGroup(
			contentPaneLayout.createParallelGroup()
				.addGroup(contentPaneLayout.createSequentialGroup()
					.addContainerGap(21, Short.MAX_VALUE)
					.addComponent(lblLogo, GroupLayout.PREFERRED_SIZE, 102, GroupLayout.PREFERRED_SIZE)
					.addGap(18, 18, 18)
					.addGroup(contentPaneLayout.createParallelGroup()
						.addComponent(label1)
						.addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(btnConnect)
							.addComponent(cmbIp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
						.addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(label2)
							.addComponent(cmbPort, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(label3)
							.addComponent(labelnumber)))
					.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
					.addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 238, GroupLayout.PREFERRED_SIZE)
					.addGap(16, 16, 16))
		);
		setSize(410, 495);
		setLocationRelativeTo(getOwner());
	
	}

	/*private void initSchedule() {
		
		timer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				
				if(!connection.isConnected())
					System.out.println("not connected");
				else
					System.out.println("Connected");
			}
		}, SCHEDULE_DELAY, SCHEDULE_INTERVAL);
		
	}*/

	private void btnConnectOnClick(ActionEvent e) {
		
		connect();
		
	}
	
	private void connect()
	{
		try {
			connection.setupConnection(new Address((String)cmbIp.getSelectedItem(), "61616"));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		new PeerDaemonStarter(connection, this);
		cmbIp.setEditable(false);
		cmbPort.setEditable(false);
	}
	@Override
	public void writeMessage(String message) {
		textArea.append(message);
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
		
		if(connection.isConnected())
			textArea.append("Connection restabilished\n");
		
		else
			textArea.append("Connection refused\n");
	}
	
	public static void main(String[] args)
	{
		new StartWorkerWithGui();
	}
}
