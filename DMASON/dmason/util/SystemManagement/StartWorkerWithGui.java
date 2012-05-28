package dmason.util.SystemManagement;
import java.awt.*;
import java.awt.event.*;
import java.util.Scanner;
import javax.swing.*;

import dmason.util.connection.Address;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;

/**
 *Class for StartWorkerWithGui
 *
 */
public class StartWorkerWithGui extends JFrame implements StartWorkerInterface {
	
	private static final long serialVersionUID = 1L;
	public boolean START=false;
	private ConnectionNFieldsWithActiveMQAPI connection;
	
	
	public StartWorkerWithGui() {
		initComponents();
		connection = new ConnectionNFieldsWithActiveMQAPI();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}

	private void button1ActionPerformed(ActionEvent e) {
		try {
			connection.setupConnection(new Address((String)comboBoxServer.getSelectedItem(), "61616"));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		PeerDaemonStarter p = new PeerDaemonStarter(connection,this);
		getComboBoxServer().setEditable(false);
		getComboBoxPort().setEditable(false);
	}

	

	private void initComponents() {
		scrollPane1 = new JScrollPane();
		textArea = new JTextArea();
		textArea.setEditable(false);
		button1 = new JButton();
		label1 = new JLabel();
		label2 = new JLabel();
		comboBoxPort = new JComboBox();
		
		label3 = new JLabel();
		labelnumber = new JLabel();
		label4 = new JLabel();
		comboBoxServer = new JComboBox();
		
		//======== this ========
		setTitle("D.MASON WORKER");
		Container contentPane = getContentPane();

		//======== scrollPane1 ========
		{
			scrollPane1.setViewportView(textArea);
		}

		//---- button1 ----
		button1.setText("connect");
		button1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				button1ActionPerformed(e);
			}
		});

		//---- label1 ----
		label1.setText("Server ip:");

		//---- label2 ----
		label2.setText("Server port ip:");

		//---- label3 ----
		label3.setText("");

		//---- labelnumber ----
		labelnumber.setText("");

		//---- label4 ----
		label4.setText("Distributed Mason 'failed to load image'");
		
		label4.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/carmineworker.png")));

		//---- comboBox ----
		comboBoxServer.setEditable(true);
		comboBoxPort.setEditable(true);

		Scanner in=new Scanner(ClassLoader.getSystemClassLoader().getResourceAsStream("dmason/resource/file/urlworker"));
		while(in.hasNext())
		{
			String line=in.nextLine();
			String[] args=line.split(":");
			comboBoxServer.addItem(args[0]);
			comboBoxPort.addItem(args[1]);
			
		}
		
		comboBoxServer.setSelectedIndex(0);
		comboBoxPort.setSelectedIndex(0);
	

		GroupLayout contentPaneLayout = new GroupLayout(contentPane);
		contentPane.setLayout(contentPaneLayout);
		contentPaneLayout.setHorizontalGroup(
			contentPaneLayout.createParallelGroup()
				.addGroup(contentPaneLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(contentPaneLayout.createParallelGroup()
						.addComponent(label4, GroupLayout.PREFERRED_SIZE, 400, GroupLayout.PREFERRED_SIZE)
						.addGroup(contentPaneLayout.createParallelGroup()
							.addGroup(contentPaneLayout.createSequentialGroup()
								.addGroup(contentPaneLayout.createParallelGroup()
									.addComponent(label2)
									.addComponent(label1))
								.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
									.addComponent(comboBoxPort)
									.addComponent(comboBoxServer, 0, 119, Short.MAX_VALUE))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(contentPaneLayout.createParallelGroup()
									.addGroup(contentPaneLayout.createSequentialGroup()
										.addGap(78, 78, 78)
										.addComponent(button1))
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
					.addComponent(label4, GroupLayout.PREFERRED_SIZE, 102, GroupLayout.PREFERRED_SIZE)
					.addGap(18, 18, 18)
					.addGroup(contentPaneLayout.createParallelGroup()
						.addComponent(label1)
						.addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(button1)
							.addComponent(comboBoxServer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
						.addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(label2)
							.addComponent(comboBoxPort, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
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

	public JTextArea getTextArea() {
		return textArea;
	}


	public void setTextArea(JTextArea textArea) {
		this.textArea = textArea;
	}


	public JButton getButton1() {
		return button1;
	}


	public void setButton1(JButton button1) {
		this.button1 = button1;
	}


	public JComboBox getComboBoxPort() {
		return comboBoxPort;
	}


	public void setComboBoxPort(JComboBox comboBoxPort) {
		this.comboBoxPort = comboBoxPort;
	}


	public JComboBox getComboBoxServer() {
		return comboBoxServer;
	}


	public void setComboBoxServer(JComboBox comboBoxServer) {
		this.comboBoxServer = comboBoxServer;
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - aaaa aaaa
	private JScrollPane scrollPane1;
	public JTextArea textArea;
	private JButton button1;
	private JLabel label1;
	private JLabel label2;
	private JComboBox comboBoxPort;
	private JLabel label3;
	public JLabel labelnumber;
	private JLabel label4;
	private JComboBox comboBoxServer;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	
	public static void main(String[] args){
		StartWorkerWithGui worker = new StartWorkerWithGui();
		worker.setVisible(true);
	}


	@Override
	public void writeMessage(String message) {
	
		textArea.append(message);
		
	}
}
