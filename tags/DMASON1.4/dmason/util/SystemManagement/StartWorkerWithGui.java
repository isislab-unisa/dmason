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
public class StartWorkerWithGui extends JFrame {
	
	private static final long serialVersionUID = 1L;
	public boolean START=false;
	private ConnectionNFieldsWithActiveMQAPI connection;
	public StartWorkerWithGui() {
		initComponents();
		connection = new ConnectionNFieldsWithActiveMQAPI();
		setVisible(true);
	}


	private void button1ActionPerformed(ActionEvent e) {
		try {
			connection.setupConnection(new Address((String)comboBox.getSelectedItem(),"61616"));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		PeerDaemonStarter p = new PeerDaemonStarter(connection,this);
	}

	private void initComponents() {
		scrollPane1 = new JScrollPane();
		textArea = new JTextArea();
		button1 = new JButton();
		label1 = new JLabel();
		label2 = new JLabel();
		combobox2 = new JComboBox();
		label3 = new JLabel();
		labelnumber = new JLabel();
		label4 = new JLabel();
		comboBox = new JComboBox();

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
		comboBox.setEditable(true);
		combobox2.setEditable(true);

		Scanner in=new Scanner(ClassLoader.getSystemClassLoader().getResourceAsStream("dmason/resource/file/urlworker"));
		while(in.hasNext())
		{
			String line=in.nextLine();
			String[] args=line.split(":");
			comboBox.addItem(args[0]);
			combobox2.addItem(args[1]);
			
		}
		
		comboBox.setSelectedIndex(0);
		combobox2.setSelectedIndex(0);
	

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
									.addComponent(combobox2)
									.addComponent(comboBox, 0, 119, Short.MAX_VALUE))
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
							.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
						.addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(label2)
							.addComponent(combobox2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(label3)
							.addComponent(labelnumber)))
					.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
					.addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 238, GroupLayout.PREFERRED_SIZE)
					.addGap(16, 16, 16))
		);
		setSize(410, 495);
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - aaaa aaaa
	private JScrollPane scrollPane1;
	public JTextArea textArea;
	private JButton button1;
	private JLabel label1;
	private JLabel label2;
	private JComboBox combobox2;
	private JLabel label3;
	public JLabel labelnumber;
	private JLabel label4;
	private JComboBox comboBox;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	
	public static void main(String[] args){
		StartWorkerWithGui worker = new StartWorkerWithGui();
		worker.setVisible(true);
	}
}
