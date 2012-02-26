package dmason.util.visualization;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.*;
import dmason.util.connection.Address;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;

/**
 * @author Tesla
 */
public class LauncherViewer {

	public static void main(String[] args) {
		
		LauncherViewer lv = new LauncherViewer();
		lv.initComponents();
		lv.LauncherViewer.setVisible(true);
	}

	private void buttonPathActionPerformed(ActionEvent e) {
		 
		JFileChooser file = new JFileChooser();
		file.setCurrentDirectory(new File("."));
		file.setDialogTitle("Select your directory");
		file.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		file.setAcceptAllFileFilterUsed(false);
		if(file.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
		{
			path = file.getCurrentDirectory()+"";
			path = path.substring(0, path.length()-1);
			labelPath.setText(path);
		}
		else
		{
			JOptionPane.showMessageDialog(null, "Not Selected Path");
		}
	}

	private void buttonConnectionActionPerformed(ActionEvent e) {

		//riempimento campi
		address = new Address(fieldAddress.getText(),fieldPort.getText());
		
		connection = new ConnectionNFieldsWithActiveMQAPI();
		
		try {
			connection.setupConnection(address);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Display display = new Display(connection, comboMode.getSelectedIndex(), 
				Integer.parseInt((String) comboNCell.getSelectedItem()), Integer.parseInt(fieldWidth.getText()), 
				Integer.parseInt(fieldHeight.getText()), path, "ANTS");
		display.initComponents();
		display.Display.setVisible(true);
		this.LauncherViewer.dispose();
		
	}

	private void comboModeItemStateChanged(ItemEvent e) {
		
		if(comboMode.getSelectedIndex()==0)
		{
			comboNCell.removeAllItems();
			for (int i = 2; i < 100; i++) {
				
				comboNCell.addItem(i+"");	
			}
		}
		else
		{
			comboNCell.removeAllItems();
			comboNCell.addItem("4");
			comboNCell.addItem("9");
			comboNCell.addItem("16");
			comboNCell.addItem("26");
			comboNCell.addItem("36");
			comboNCell.addItem("49");
			comboNCell.addItem("64");
			comboNCell.addItem("81");
		}
	}

	private void comboNCellItemStateChanged(ItemEvent e) {
		// TODO add your code here
	}

	private void initComponents() {

		LauncherViewer = new JFrame();
		panelMain = new JPanel();
		panelLabel = new JPanel();
		logo = new JLabel();
		panelData = new JPanel();
		panelConnection = new JPanel();
		fieldAddress = new JTextField();
		fieldPort = new JTextField();
		label5 = new JLabel();
		label6 = new JLabel();
		panelInfo = new JPanel();
		label1 = new JLabel();
		comboMode = new JComboBox();
		comboNCell = new JComboBox();
		label2 = new JLabel();
		label3 = new JLabel();
		label4 = new JLabel();
		fieldWidth = new JTextField();
		fieldHeight = new JTextField();
		labelPath = new JLabel();
		buttonPath = new JButton();
		buttonConnection = new JButton();
		
		comboMode.addItem("Horizontal");
		comboMode.addItem("Square");
		
		path = System.getProperty("user.dir");
		
		if(comboMode.getSelectedIndex()==0)
		{
			comboNCell.removeAllItems();
			for (int i = 2; i < 100; i++) {
				
				comboNCell.addItem(i+"");	
			}
		}

		//======== LauncherViewer ========
		{
			LauncherViewer.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			Container LauncherViewerContentPane = LauncherViewer.getContentPane();

			//======== panelMain ========
			{
				//======== panelLabel ========
				{

					//---- logo ----
					logo.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/dmasonglobalview.png")));

					GroupLayout panelLabelLayout = new GroupLayout(panelLabel);
					panelLabel.setLayout(panelLabelLayout);
					panelLabelLayout.setHorizontalGroup(
						panelLabelLayout.createParallelGroup()
							.addGroup(panelLabelLayout.createSequentialGroup()
								.addGap(80, 80, 80)
								.addComponent(logo)
								.addContainerGap(76, Short.MAX_VALUE))
					);
					panelLabelLayout.setVerticalGroup(
						panelLabelLayout.createParallelGroup()
							.addGroup(panelLabelLayout.createSequentialGroup()
								.addContainerGap()
								.addComponent(logo)
								.addContainerGap(21, Short.MAX_VALUE))
					);
				}

				//======== panelData ========
				{

					//======== panelConnection ========
					{
						panelConnection.setBorder(new TitledBorder(null, "Connection", TitledBorder.LEFT, TitledBorder.TOP));

						//---- fieldAddress ----
						fieldAddress.setText("127.0.0.1");

						//---- fieldPort ----
						fieldPort.setText("61616");

						//---- label5 ----
						label5.setText("Server:");

						//---- label6 ----
						label6.setText("Port:");

						GroupLayout panelConnectionLayout = new GroupLayout(panelConnection);
						panelConnection.setLayout(panelConnectionLayout);
						panelConnectionLayout.setHorizontalGroup(
							panelConnectionLayout.createParallelGroup()
								.addGroup(panelConnectionLayout.createSequentialGroup()
									.addContainerGap()
									.addComponent(label5)
									.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
									.addComponent(fieldAddress, GroupLayout.PREFERRED_SIZE, 147, GroupLayout.PREFERRED_SIZE)
									.addGap(32, 32, 32)
									.addComponent(label6)
									.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
									.addComponent(fieldPort, GroupLayout.PREFERRED_SIZE, 109, GroupLayout.PREFERRED_SIZE)
									.addContainerGap(36, Short.MAX_VALUE))
						);
						panelConnectionLayout.setVerticalGroup(
							panelConnectionLayout.createParallelGroup()
								.addGroup(panelConnectionLayout.createSequentialGroup()
									.addGroup(panelConnectionLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
										.addComponent(fieldAddress, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(label5)
										.addComponent(label6)
										.addComponent(fieldPort, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
									.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						);
					}

					//======== panelInfo ========
					{
						panelInfo.setBorder(new TitledBorder(null, "Simulation Info", TitledBorder.LEFT, TitledBorder.TOP));

						//---- label1 ----
						label1.setText("Mode:");

						//---- comboMode ----
						comboMode.addItemListener(new ItemListener() {
							@Override
							public void itemStateChanged(ItemEvent e) {
								comboModeItemStateChanged(e);
							}
						});

						//---- comboNCell ----
						comboNCell.addItemListener(new ItemListener() {
							@Override
							public void itemStateChanged(ItemEvent e) {
								comboNCellItemStateChanged(e);
							}
						});

						//---- label2 ----
						label2.setText("Number Cell:");

						//---- label3 ----
						label3.setText("Width:");

						//---- label4 ----
						label4.setText("Height:");

						//---- fieldWidth ----
						fieldWidth.setText("200");

						//---- fieldHeight ----
						fieldHeight.setText("200");

						//---- labelPath ----
						labelPath.setText("Path: "+path);

						//---- buttonPath ----
						buttonPath.setText("Save Path");
						buttonPath.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								buttonPathActionPerformed(e);
							}
						});

						GroupLayout panelInfoLayout = new GroupLayout(panelInfo);
						panelInfo.setLayout(panelInfoLayout);
						panelInfoLayout.setHorizontalGroup(
							panelInfoLayout.createParallelGroup()
								.addGroup(panelInfoLayout.createSequentialGroup()
									.addContainerGap()
									.addGroup(panelInfoLayout.createParallelGroup()
										.addComponent(labelPath)
										.addComponent(buttonPath)
										.addGroup(panelInfoLayout.createSequentialGroup()
											.addGroup(panelInfoLayout.createParallelGroup()
												.addGroup(panelInfoLayout.createSequentialGroup()
													.addComponent(label1)
													.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
													.addComponent(comboMode, GroupLayout.PREFERRED_SIZE, 145, GroupLayout.PREFERRED_SIZE))
												.addGroup(panelInfoLayout.createSequentialGroup()
													.addComponent(label3)
													.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
													.addComponent(fieldWidth, GroupLayout.PREFERRED_SIZE, 91, GroupLayout.PREFERRED_SIZE)))
											.addGap(24, 24, 24)
											.addGroup(panelInfoLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
												.addComponent(label2)
												.addComponent(label4))
											.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
											.addGroup(panelInfoLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
												.addComponent(fieldHeight, GroupLayout.Alignment.LEADING)
												.addComponent(comboNCell, GroupLayout.Alignment.LEADING, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
									.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						);
						panelInfoLayout.setVerticalGroup(
							panelInfoLayout.createParallelGroup()
								.addGroup(panelInfoLayout.createSequentialGroup()
									.addGroup(panelInfoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
										.addComponent(comboMode, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(label1))
									.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
									.addGroup(panelInfoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
										.addComponent(label3)
										.addComponent(fieldWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
									.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
									.addComponent(labelPath)
									.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
									.addComponent(buttonPath)
									.addGap(24, 24, 24))
								.addGroup(panelInfoLayout.createSequentialGroup()
									.addGroup(panelInfoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
										.addComponent(comboNCell, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(label2))
									.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
									.addGroup(panelInfoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
										.addComponent(fieldHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(label4))
									.addContainerGap())
						);
					}

					//---- buttonConnection ----
					buttonConnection.setText("Connect");
					buttonConnection.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							buttonConnectionActionPerformed(e);
						}
					});

					GroupLayout panelDataLayout = new GroupLayout(panelData);
					panelData.setLayout(panelDataLayout);
					panelDataLayout.setHorizontalGroup(
						panelDataLayout.createParallelGroup()
							.addGroup(panelDataLayout.createSequentialGroup()
								.addContainerGap()
								.addGroup(panelDataLayout.createParallelGroup()
									.addGroup(GroupLayout.Alignment.TRAILING, panelDataLayout.createSequentialGroup()
										.addGap(329, 329, 329)
										.addComponent(buttonConnection)
										.addGap(17, 17, 17))
									.addGroup(GroupLayout.Alignment.TRAILING, panelDataLayout.createSequentialGroup()
										.addComponent(panelConnection, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addContainerGap())
									.addGroup(GroupLayout.Alignment.TRAILING, panelDataLayout.createSequentialGroup()
										.addComponent(panelInfo, GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
										.addContainerGap())))
					);
					panelDataLayout.setVerticalGroup(
						panelDataLayout.createParallelGroup()
							.addGroup(GroupLayout.Alignment.TRAILING, panelDataLayout.createSequentialGroup()
								.addContainerGap()
								.addComponent(panelConnection, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
								.addComponent(panelInfo, GroupLayout.PREFERRED_SIZE, 149, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
								.addComponent(buttonConnection)
								.addGap(18, 18, 18))
					);
				}

				GroupLayout panelMainLayout = new GroupLayout(panelMain);
				panelMain.setLayout(panelMainLayout);
				panelMainLayout.setHorizontalGroup(
					panelMainLayout.createParallelGroup()
						.addGroup(panelMainLayout.createSequentialGroup()
							.addGroup(panelMainLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
								.addComponent(panelLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(panelData, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
							.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
				panelMainLayout.setVerticalGroup(
					panelMainLayout.createParallelGroup()
						.addGroup(GroupLayout.Alignment.TRAILING, panelMainLayout.createSequentialGroup()
							.addComponent(panelLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
							.addComponent(panelData, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				);
			}

			GroupLayout LauncherViewerContentPaneLayout = new GroupLayout(LauncherViewerContentPane);
			LauncherViewerContentPane.setLayout(LauncherViewerContentPaneLayout);
			LauncherViewerContentPaneLayout.setHorizontalGroup(
				LauncherViewerContentPaneLayout.createParallelGroup()
					.addGroup(LauncherViewerContentPaneLayout.createSequentialGroup()
						.addGap(0, 0, Short.MAX_VALUE)
						.addComponent(panelMain, GroupLayout.PREFERRED_SIZE, 482, GroupLayout.PREFERRED_SIZE)
						.addContainerGap())
			);
			LauncherViewerContentPaneLayout.setVerticalGroup(
				LauncherViewerContentPaneLayout.createParallelGroup()
					.addComponent(panelMain, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			);
			LauncherViewer.pack();
			LauncherViewer.setLocationRelativeTo(LauncherViewer.getOwner());
		}
	
	}

	
	private JFrame LauncherViewer;
	private JPanel panelMain;
	private JPanel panelLabel;
	private JLabel logo;
	private JPanel panelData;
	private JPanel panelConnection;
	private JTextField fieldAddress;
	private JTextField fieldPort;
	private JLabel label5;
	private JLabel label6;
	private JPanel panelInfo;
	private JLabel label1;
	private JComboBox comboMode;
	private JComboBox comboNCell;
	private JLabel label2;
	private JLabel label3;
	private JLabel label4;
	private JTextField fieldWidth;
	private JTextField fieldHeight;
	private JLabel labelPath;
	private JButton buttonPath;
	private JButton buttonConnection;

	private ConnectionNFieldsWithActiveMQAPI connection;
	private Address address;
	private String path;
}
