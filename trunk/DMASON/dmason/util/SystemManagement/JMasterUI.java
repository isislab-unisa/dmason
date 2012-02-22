package dmason.util.SystemManagement;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import sim.display.Console;
import dmason.sim.field.grid.DSparseGrid2DFactory;
import dmason.util.connection.Address;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;
import dmason.util.connection.MyHashMap;
import dmason.util.connection.MyMessageListener;
import dmason.util.garbagecollector.Server;
import dmason.util.garbagecollector.Start;
import dmason.util.trigger.Trigger;
import dmason.util.trigger.TriggerListener;


/**
 * 
 * Class JMasterUI
 *
 */
public class JMasterUI extends JFrame {
	
	public JMasterUI() {

		initComponents();
		//config = new HashMap<String, Integer>();
		config = new HashMap<String, EntryVal<Integer,Boolean>>();
		setTitle("JMasterUI");
		initializeDefaultLabel();
		//setSize(855,600);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setResizable(false);
		starter = new Start();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		limitStep = 1000;
		menuBar1 = new JMenuBar();
		jMenuFile = new JMenu();
		menuItemOpen = new JMenuItem();
		menuItemExit = new JMenuItem();
		jMenuAbout = new JMenu();
		menuItemInfo = new JMenuItem();
		menuItenHelp = new JMenuItem();
		panelMain = new JPanel();
		jPanelContainerConnection = new JPanel();
		jPanelConnection = new JPanel();
		jLabelAddress = new JLabel();
		textFieldAddress = new JTextField();
		jLabelPort = new JLabel();
		textFieldPort = new JTextField();
		refreshServerLabel = new JLabel();
		buttonRefreshServerLabel = new JButton();
		jPanelContainerSettings = new JPanel();
		jPanelSetDistribution = new JPanel();
		jPanelSettings = new JPanel();
		radioButtonHorizontal = new JRadioButton();
		radioButtonSquare = new JRadioButton();
		jLabelHorizontal = new JLabel();
		jLabelSquare = new JLabel();
		jLabelRegions = new JLabel();
		jLabelMaxDistance = new JLabel();
		jLabelWidth = new JLabel();
		textFieldMaxDistance = new JTextField();
		jComboRegions = new JComboBox();
		textFieldWidth = new JTextField();
		jLabelHeight = new JLabel();
		textFieldHeight = new JTextField();
		jLabelAgents = new JLabel();
		textFieldAgents = new JTextField();
		jLabelChooseSimulation = new JLabel();
		jComboBoxChooseSimulation = new JComboBox();
		jComboBoxNumRegionXPeer = new JComboBox();
		jPanelContainerTabbedPane = new JPanel();
		tabbedPane2 = new JTabbedPane();
		jPanelDefault = new JPanel();
		labelSimulationConfigSet = new JLabel();
		labelRegionsResume = new JLabel();
		labelNumOfPeerResume = new JLabel();
		labelRegForPeerResume = new JLabel();
		labelWriteReg = new JLabel();
		labelWriteNumOfPeer = new JLabel();
		labelWriteRegForPeer = new JLabel();
		labelWidthRegion = new JLabel();
		labelheightRegion = new JLabel();
		labelDistrMode = new JLabel();
		labelWriteRegWidth = new JLabel();
		labelWriteRegHeight = new JLabel();
		labelWriteDistrMode = new JLabel();
		graphicONcheckBox2 = new JCheckBox();
		jPanelSetButton = new JPanel();
		buttonSetConfigDefault = new JButton();
		jPanelAdvanced = new JPanel();
		jPanelAdvancedMain = new JPanel();
		peerInfoStatus = new JDesktopPane();
		internalFrame1 = new JInternalFrame();
		label8 = new JTextArea();
		label8.setBackground(Color.BLACK);
		label8.setForeground(Color.GREEN);
		label8.setEditable(false);
		advancedConfirmBut = new JLabel();
		graphicONcheckBox = new JCheckBox();
		scrollPaneTree = new JScrollPane();
		tree1 = new JTree();
		buttonSetConfigAdvanced = new JButton();
		jLabelPlayButton = new JLabel();
		jLabelPauseButton = new JLabel();
		jPanelNumStep = new JPanel();
		writeStepLabel = new JLabel();
		jLabelStep = new JLabel();
		jLabelStopButton = new JLabel();
		scrollPane1 = new JScrollPane();
		peerInfoStatus1 = new JDesktopPane();
		root = new DefaultMutableTreeNode("Simulation");
		ButtonGroup b = new ButtonGroup();
		b.add(radioButtonHorizontal);
		b.add(radioButtonSquare);
		ip = textFieldAddress.getText();
		port = textFieldPort.getText();
		menuBar1 = new JMenuBar();
		jMenuFile = new JMenu();
		menuItemOpen = new JMenuItem();
		menuItemExit = new JMenuItem();
		menuItemServer = new JMenuItem();
		menuConfSim = new JMenuItem();
		menuNewSim = new JMenuItem();
		jMenuAbout = new JMenu();
		menuItemInfo = new JMenuItem();
		menuItenHelp = new JMenuItem();
		scrollPane3 = new JScrollPane();
		textField1 = new JTextArea();
		panelConsole = new JPanel();
		buttonSetConfigDefault2 = new JButton();
		jPanelSetButton2 = new JPanel();
		graphicONcheckBox = new JCheckBox();
		graphicONcheckBox.setEnabled(false);
		graphicONcheckBox.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				
					withGui = graphicONcheckBox.isSelected();
			}
		});
		
		jLabelChooseSimulation = new JLabel();
		jComboBoxChooseSimulation = new JComboBox();
		
		jComboBoxChooseSimulation.addItem("Flockers");
		jComboBoxChooseSimulation.addItem("Ant Foraging");
		jComboBoxChooseSimulation.addItem("Particles");
		jComboBoxChooseSimulation.setSelectedIndex(0);
		selectedSimulation = jComboBoxChooseSimulation.getSelectedItem().toString();
		jComboBoxChooseSimulation.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				selectedSimulation = jComboBoxChooseSimulation.getSelectedItem().toString();
				
			}
		});
		
		for(int i=2;i<100;i++)
			jComboRegions.addItem(i);
		
		jComboRegions.setSelectedItem(2);
		jComboRegions.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if(!radioItemFlag)
					initializeDefaultLabel();
			}
		});
		
		buttonRefreshServerLabel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				connect();
			}
		});
		
		refreshServerLabel.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
				if (starter.isConnected())
					starter.execute("restart");
				else
					JOptionPane.showMessageDialog(null,"Not connected to the Server!");
			}
			
			@Override
			public void mousePressed(MouseEvent arg0) {}
			
			@Override
			public void mouseExited(MouseEvent arg0) {}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {}
			
			@Override
			public void mouseClicked(MouseEvent arg0) {}
		});
		
		radioButtonHorizontal.addActionListener(new ActionListener() {
				
			@Override
			public void actionPerformed(ActionEvent arg0) {
				radioItemFlag = true;
				jComboRegions.removeAllItems();
				for(int i=2;i<=100;i++)
					jComboRegions.addItem(i);
				initializeDefaultLabel();
				radioItemFlag = false;
			}
		});
			
		radioButtonSquare.addActionListener(new ActionListener() {
				
			@Override
			public void actionPerformed(ActionEvent e) {
				radioItemFlag = true;
					jComboRegions.removeAllItems();
				for(int i=2;i<=10;i++)
						jComboRegions.addItem(i*i);
				initializeDefaultLabel();
				radioItemFlag = false;
			}
		});
			
		buttonSetConfigDefault2.addActionListener(new ActionListener() {
				
			@Override
			public void actionPerformed(ActionEvent e) {
				submitCustomizeMode();
			}
		});
			
		buttonSetConfigDefault.addActionListener(new ActionListener() {
				
			@Override
			public void actionPerformed(ActionEvent e) {
				submitDefaultMode();
			}
		});
			
		advancedConfirmBut.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
				confirm();
				res -= (Integer)jComboBoxNumRegionXPeer.getSelectedItem();
					
				withGui = graphicONcheckBox.isSelected();
					
				jComboBoxNumRegionXPeer.removeAllItems();
				graphicONcheckBox.setSelected(false);
				for(int i=1;i<=res;i++)
					jComboBoxNumRegionXPeer.addItem(i);
			}
				
			@Override
			public void mousePressed(MouseEvent arg0) {}
				
			@Override
			public void mouseExited(MouseEvent arg0) {}
				
			@Override
			public void mouseEntered(MouseEvent arg0) {}
				
			@Override
			public void mouseClicked(MouseEvent arg0) {}
		});
		

		//======== this ========

		Container contentPane = getContentPane();

		//======== menuBar1 ========
		{
			{
				jMenuFile.setText("File");
				
				menuNewSim.setText("New");
				menuNewSim.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						me.dispose();
						me = null;
						JMasterUI p = new JMasterUI();
						p.setVisible(true);
					}
				});
				
				jMenuFile.add(menuNewSim);

				//---- menuItemOpen ----
				menuItemOpen.setText("Open");
				menuItemOpen.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						 files = new JFileChooser();
						 files.addActionListener(new ActionListener() {
								
								@Override
								public void actionPerformed(ActionEvent arg0) {
										
										String tmp = files.getSelectedFile().getName();
										if(tmp.endsWith(".class")){
											sim = tmp.replaceAll(".class","");
											System.out.println(sim);
										}
										else
										{
											JOptionPane.showMessageDialog(null,"Invalid file format!");										}
								}
							});
						 files.setFileFilter(new FileFilter() {
							
							@Override
							public String getDescription() {
								return "Definizione di classi";
							}
							
							@Override
							public boolean accept(File f) {
								return f.getName().endsWith(".class");
							}
						});
						 files.showOpenDialog(null);
					}
				});
				jMenuFile.add(menuItemOpen);
				
				menuItemServer.setText("Server");
				menuItemServer.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						Server s = new Server(starter);
						s.setVisible(true);
					}
				});
				jMenuFile.add(menuItemServer);
				
				menuConfSim.setText("Configuration");
				menuConfSim.add(new Checkbox("GUI"));
				menuConfSim.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
							
					}
				});
				jMenuFile.add(menuConfSim);
				//---- menuItemExit ----
				menuItemExit.setText("Exit");
				menuItemExit.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						me.dispose();
					}
				});
				
				jMenuFile.add(menuItemExit);
			}
			menuBar1.add(jMenuFile);

			//======== jMenuAbout ========
			{
				jMenuAbout.setText("?");

				//---- menuItemInfo ----
				menuItemInfo.setText("Info");
				jMenuAbout.add(menuItemInfo);

				//---- menuItenHelp ----
				menuItenHelp.setText("Help");
				jMenuAbout.add(menuItenHelp);
			}
			menuBar1.add(jMenuAbout);
		}
		
		setJMenuBar(menuBar1);

		//======== panelMain ========
		{

			//======== jPanelContainerConnection ========
			{

				//======== jPanelConnection ========
				{
					jPanelConnection.setBorder(new TitledBorder("Connection"));
					jPanelConnection.setPreferredSize(new Dimension(215, 125));

					//---- jLabelAddress ----
					jLabelAddress.setText("IP Address :");

					//---- textFieldAddress ----
					textFieldAddress.setText("127.0.0.1");

					//---- jLabelPort ----
					jLabelPort.setText("Port :");

					//---- textFieldPort ----
					textFieldPort.setText("61616");

					//---- refreshServerLabel ----
					refreshServerLabel.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/refresh.png")));

					//---- buttonRefreshServerLabel ----
					buttonRefreshServerLabel.setText("OK");

					GroupLayout jPanelConnectionLayout = new GroupLayout(jPanelConnection);
					jPanelConnection.setLayout(jPanelConnectionLayout);
					jPanelConnectionLayout.setHorizontalGroup(
						jPanelConnectionLayout.createParallelGroup()
							.addGroup(jPanelConnectionLayout.createSequentialGroup()
								.addGap(29, 29, 29)
								.addComponent(jLabelAddress)
								.addGap(18, 18, 18)
								.addComponent(textFieldAddress, GroupLayout.PREFERRED_SIZE, 177, GroupLayout.PREFERRED_SIZE)
								.addGap(59, 59, 59)
								.addComponent(jLabelPort)
								.addGap(18, 18, 18)
								.addComponent(textFieldPort, GroupLayout.PREFERRED_SIZE, 177, GroupLayout.PREFERRED_SIZE)
								.addGap(78, 78, 78)
								.addComponent(refreshServerLabel)
								.addGap(18, 18, 18)
								.addComponent(buttonRefreshServerLabel)
								.addContainerGap(76, Short.MAX_VALUE))
					);
					jPanelConnectionLayout.setVerticalGroup(
						jPanelConnectionLayout.createParallelGroup()
							.addGroup(jPanelConnectionLayout.createSequentialGroup()
								.addContainerGap()
								.addGroup(jPanelConnectionLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(jLabelAddress)
									.addComponent(jLabelPort)
									.addComponent(textFieldPort, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
									.addComponent(textFieldAddress, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
								.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
							.addGroup(GroupLayout.Alignment.TRAILING, jPanelConnectionLayout.createSequentialGroup()
								.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(jPanelConnectionLayout.createParallelGroup()
									.addComponent(buttonRefreshServerLabel)
									.addComponent(refreshServerLabel))
								.addContainerGap())
					);
				}

				GroupLayout jPanelContainerConnectionLayout = new GroupLayout(jPanelContainerConnection);
				jPanelContainerConnection.setLayout(jPanelContainerConnectionLayout);
				jPanelContainerConnectionLayout.setHorizontalGroup(
					jPanelContainerConnectionLayout.createParallelGroup()
						.addGroup(jPanelContainerConnectionLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(jPanelConnection, GroupLayout.PREFERRED_SIZE, 829, GroupLayout.PREFERRED_SIZE)
							.addContainerGap(153, Short.MAX_VALUE))
				);
				jPanelContainerConnectionLayout.setVerticalGroup(
					jPanelContainerConnectionLayout.createParallelGroup()
						.addComponent(jPanelConnection, GroupLayout.PREFERRED_SIZE, 71, GroupLayout.PREFERRED_SIZE)
				);
			}

			//======== jPanelContainerSettings ========
			{

				GroupLayout jPanelContainerSettingsLayout = new GroupLayout(jPanelContainerSettings);
				jPanelContainerSettings.setLayout(jPanelContainerSettingsLayout);
				jPanelContainerSettingsLayout.setHorizontalGroup(
					jPanelContainerSettingsLayout.createParallelGroup()
						.addGap(0, 1, Short.MAX_VALUE)
				);
				jPanelContainerSettingsLayout.setVerticalGroup(
					jPanelContainerSettingsLayout.createParallelGroup()
						.addGap(0, 481, Short.MAX_VALUE)
				);
			}

			//======== jPanelSetDistribution ========
			{
				jPanelSetDistribution.setBorder(new TitledBorder("Settings"));

				//======== jPanelSettings ========
				{

					//---- radioButtonHorizontal ----
					radioButtonHorizontal.setText("HORIZONTAL");

					//---- radioButtonSquare ----
					radioButtonSquare.setText("SQUARE");

					jLabelHorizontal.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/hori.png")));

					//---- jLabelSquare ----
					jLabelSquare.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/square.png")));

					//---- jLabelRegions ----
					jLabelRegions.setText("REGIONS :");

					//---- jLabelMaxDistance ----
					jLabelMaxDistance.setText("MAX_DISTANCE :");

					//---- jLabelWidth ----
					jLabelWidth.setText("WIDTH :");

					//---- textFieldMaxDistance ----
					textFieldMaxDistance.setText("1");

					//---- textFieldWidth ----
					textFieldWidth.setText("200");

					//---- jLabelHeight ----
					jLabelHeight.setText("HEIGHT :");

					//---- textFieldHeight ----
					textFieldHeight.setText("200");

					//---- jLabelAgents ----
					jLabelAgents.setText("AGENTS :");

					//---- textFieldAgents ----
					textFieldAgents.setText("15");


					
					textFieldWidth.addKeyListener(new KeyListener() {
						
						@Override
						public void keyTyped(KeyEvent e) {}
						
						@Override
						public void keyReleased(KeyEvent e) {
							initializeDefaultLabel();
						}
						
						@Override
						public void keyPressed(KeyEvent e) {}
					});
					
					textFieldHeight.addKeyListener(new KeyListener() {
						
						@Override
						public void keyTyped(KeyEvent arg0) {}
						
						@Override
						public void keyReleased(KeyEvent arg0) {
							initializeDefaultLabel();
						}
						
						@Override
						public void keyPressed(KeyEvent arg0) {}
					});
					
					//---- jLabelChooseSimulation ----
					jLabelChooseSimulation.setText("Choose your simulation:");

					//---- jComboBoxChooseSimulation ----
					jComboBoxChooseSimulation.setMaximumRowCount(10);

					GroupLayout jPanelSettingsLayout = new GroupLayout(jPanelSettings);
					jPanelSettings.setLayout(jPanelSettingsLayout);
					jPanelSettingsLayout.setHorizontalGroup(
						jPanelSettingsLayout.createParallelGroup()
							.addGroup(jPanelSettingsLayout.createSequentialGroup()
								.addGap(17, 17, 17)
								.addGroup(jPanelSettingsLayout.createParallelGroup()
									.addComponent(jLabelChooseSimulation, GroupLayout.PREFERRED_SIZE, 245, GroupLayout.PREFERRED_SIZE)
									.addGroup(jPanelSettingsLayout.createSequentialGroup()
										.addGroup(jPanelSettingsLayout.createParallelGroup()
											.addComponent(jLabelMaxDistance)
											.addComponent(jLabelWidth)
											.addComponent(jLabelHeight)
											.addComponent(jLabelRegions)
											.addComponent(jLabelAgents))
										.addGap(35, 35, 35)
										.addGroup(jPanelSettingsLayout.createParallelGroup()
											.addComponent(textFieldAgents, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE)
											.addComponent(textFieldHeight, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE)
											.addComponent(textFieldWidth, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE)
											.addComponent(textFieldMaxDistance, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE)
											.addComponent(jComboRegions, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
									.addGroup(jPanelSettingsLayout.createSequentialGroup()
										.addComponent(radioButtonHorizontal)
										.addGap(28, 28, 28)
										.addComponent(jLabelHorizontal))
									.addGroup(jPanelSettingsLayout.createSequentialGroup()
										.addComponent(radioButtonSquare)
										.addGap(52, 52, 52)
										.addComponent(jLabelSquare))
									.addComponent(jComboBoxChooseSimulation, GroupLayout.PREFERRED_SIZE, 214, GroupLayout.PREFERRED_SIZE)))
					);
					jPanelSettingsLayout.setVerticalGroup(
						jPanelSettingsLayout.createParallelGroup()
							.addGroup(jPanelSettingsLayout.createSequentialGroup()
								.addContainerGap()
								.addGroup(jPanelSettingsLayout.createParallelGroup()
									.addComponent(radioButtonHorizontal)
									.addComponent(jLabelHorizontal))
								.addGap(18, 18, 18)
								.addGroup(jPanelSettingsLayout.createParallelGroup()
									.addComponent(radioButtonSquare)
									.addComponent(jLabelSquare))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(jPanelSettingsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(jLabelRegions)
									.addComponent(jComboRegions, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanelSettingsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(jLabelMaxDistance)
									.addComponent(textFieldMaxDistance, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanelSettingsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(jLabelWidth, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
									.addComponent(textFieldWidth, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
								.addGap(10, 10, 10)
								.addGroup(jPanelSettingsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(jLabelHeight, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
									.addComponent(textFieldHeight, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanelSettingsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(jLabelAgents)
									.addComponent(textFieldAgents, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
								.addGap(35, 35, 35)
								.addComponent(jLabelChooseSimulation)
								.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
								.addComponent(jComboBoxChooseSimulation, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(52, 52, 52))
					);
				}

				//======== jPanelContainerTabbedPane ========
				{

					//======== tabbedPane2 ========
					{

						//======== jPanelDefault ========
						{
							jPanelDefault.setBorder(new EtchedBorder());
							jPanelDefault.setPreferredSize(new Dimension(350, 331));

							//---- labelSimulationConfigSet ----
							labelSimulationConfigSet.setText("Simulation Configuration Settings");
							labelSimulationConfigSet.setFont(labelSimulationConfigSet.getFont().deriveFont(labelSimulationConfigSet.getFont().getStyle() | Font.BOLD, labelSimulationConfigSet.getFont().getSize() + 8f));

							//---- labelRegionsResume ----
							labelRegionsResume.setText("REGIONS :");

							//---- labelNumOfPeerResume ----
							labelNumOfPeerResume.setText("NUMBER OF PEERS :");

							//---- labelRegForPeerResume ----
							labelRegForPeerResume.setText("REGIONS FOR PEER :");

							//---- labelWriteReg ----
							labelWriteReg.setText("text");

							//---- labelWriteNumOfPeer ----
							labelWriteNumOfPeer.setText("text");

							//---- labelWriteRegForPeer ----
							labelWriteRegForPeer.setText("text");

							//---- labelWidthRegion ----
							labelWidthRegion.setText("REGION WIDTH :");

							//---- labelheightRegion ----
							labelheightRegion.setText("REGION HEIGHT :");

							//---- labelDistrMode ----
							labelDistrMode.setText("DISTRIBUTION MODE :");

							//---- labelWriteRegWidth ----
							labelWriteRegWidth.setText("text");

							//---- labelWriteRegHeight ----
							labelWriteRegHeight.setText("text");

							//---- labelWriteDistrMode ----
							labelWriteDistrMode.setText("text");

							//---- graphicONcheckBox2 ----
							graphicONcheckBox2.setText("Graphic ON");

							//======== jPanelSetButton ========
							{

								//---- buttonSetConfigDefault ----
								buttonSetConfigDefault.setText("Set");

								GroupLayout jPanelSetButtonLayout = new GroupLayout(jPanelSetButton);
								jPanelSetButton.setLayout(jPanelSetButtonLayout);
								jPanelSetButtonLayout.setHorizontalGroup(
									jPanelSetButtonLayout.createParallelGroup()
										.addGroup(GroupLayout.Alignment.TRAILING, jPanelSetButtonLayout.createSequentialGroup()
											.addContainerGap(522, Short.MAX_VALUE)
											.addComponent(buttonSetConfigDefault, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE)
											.addGap(72, 72, 72))
								);
								jPanelSetButtonLayout.setVerticalGroup(
									jPanelSetButtonLayout.createParallelGroup()
										.addGroup(GroupLayout.Alignment.TRAILING, jPanelSetButtonLayout.createSequentialGroup()
											.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
											.addComponent(buttonSetConfigDefault)
											.addContainerGap())
								);
							}

							GroupLayout jPanelDefaultLayout = new GroupLayout(jPanelDefault);
							jPanelDefault.setLayout(jPanelDefaultLayout);
							jPanelDefaultLayout.setHorizontalGroup(
								jPanelDefaultLayout.createParallelGroup()
									.addGroup(jPanelDefaultLayout.createSequentialGroup()
										.addContainerGap()
										.addGroup(jPanelDefaultLayout.createParallelGroup()
											.addComponent(labelSimulationConfigSet, GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE)
											.addGroup(jPanelDefaultLayout.createSequentialGroup()
												.addGap(6, 6, 6)
												.addGroup(jPanelDefaultLayout.createParallelGroup()
													.addGroup(jPanelDefaultLayout.createSequentialGroup()
														.addGroup(jPanelDefaultLayout.createParallelGroup()
															.addComponent(labelNumOfPeerResume)
															.addComponent(labelRegForPeerResume)
															.addComponent(labelWidthRegion)
															.addComponent(labelheightRegion)
															.addComponent(labelDistrMode)
															.addComponent(labelRegionsResume))
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 189, Short.MAX_VALUE)
														.addGroup(jPanelDefaultLayout.createParallelGroup()
															.addComponent(labelWriteNumOfPeer, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
															.addComponent(labelWriteReg)
															.addComponent(labelWriteRegForPeer)
															.addComponent(labelWriteRegWidth)
															.addComponent(labelWriteRegHeight)
															.addComponent(labelWriteDistrMode))
														.addGap(118, 118, 118))
													.addComponent(graphicONcheckBox2))))
										.addGap(211, 211, 211))
									.addComponent(jPanelSetButton, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							);
							jPanelDefaultLayout.setVerticalGroup(
								jPanelDefaultLayout.createParallelGroup()
									.addGroup(jPanelDefaultLayout.createSequentialGroup()
										.addContainerGap()
										.addComponent(labelSimulationConfigSet, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
										.addGap(28, 28, 28)
										.addGroup(jPanelDefaultLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
											.addGroup(jPanelDefaultLayout.createSequentialGroup()
												.addComponent(labelRegionsResume)
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(labelNumOfPeerResume)
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(labelRegForPeerResume)
												.addGap(6, 6, 6)
												.addComponent(labelWidthRegion)
												.addGap(6, 6, 6)
												.addComponent(labelheightRegion)
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(labelDistrMode))
											.addGroup(jPanelDefaultLayout.createSequentialGroup()
												.addComponent(labelWriteReg)
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(labelWriteNumOfPeer)
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(labelWriteRegForPeer)
												.addGap(6, 6, 6)
												.addComponent(labelWriteRegWidth)
												.addGap(6, 6, 6)
												.addComponent(labelWriteRegHeight)
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(labelWriteDistrMode, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)))
										.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(graphicONcheckBox2)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 91, Short.MAX_VALUE)
										.addComponent(jPanelSetButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							);
						}
						tabbedPane2.addTab("Default", jPanelDefault);


						//======== jPanelAdvanced ========
						{
							jPanelAdvanced.setBorder(new EtchedBorder());

							//======== jPanelAdvancedMain ========
							{

								//======== scrollPaneTree ========
								{

									//---- tree1 ----
									tree1.setModel(new DefaultTreeModel(root));
									DefaultTreeCellRenderer render = new DefaultTreeCellRenderer();
									
									render.setOpenIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/network.png")));
											
									render.setLeafIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/computer.gif")));
									
									render.setClosedIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/network.png")));
									tree1.setCellRenderer(render);
									tree1.setRowHeight(25);
									scrollPaneTree.setViewportView(tree1);
									tree1.addTreeSelectionListener(new TreeSelectionListener() {
										
										@Override
										public void valueChanged(TreeSelectionEvent arg0) {
											if(arg0.getPath().getLastPathComponent().equals(root)){
												jComboBoxNumRegionXPeer.setEnabled(true);
												advancedConfirmBut.setEnabled(true);
												graphicONcheckBox.setEnabled(true);
												total = (Integer)jComboRegions.getSelectedItem();
												res = total;
												jComboBoxNumRegionXPeer.removeAllItems();
												for(int i=1;i<res;i++)
													jComboBoxNumRegionXPeer.addItem(i);
											}
											else
												clickTreeListener();
										}
									});

								}

								//======== peerInfoStatus ========
								{
									peerInfoStatus.setBorder(new TitledBorder("Settings"));
									peerInfoStatus.setBackground(Color.lightGray);

									//======== peerInfoStatus1 ========
									{
										peerInfoStatus1.setBackground(Color.lightGray);
										peerInfoStatus1.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
										peerInfoStatus1.setBorder(null);

										//======== internalFrame1 ========
										{
											internalFrame1.setVisible(true);
											Container internalFrame1ContentPane = internalFrame1.getContentPane();

											//======== scrollPane1 ========
											{

												//---- label8 ----
												label8.setText("blaaaaaaaa");
												scrollPane1.setViewportView(label8);
											}

											GroupLayout internalFrame1ContentPaneLayout = new GroupLayout(internalFrame1ContentPane);
											internalFrame1ContentPane.setLayout(internalFrame1ContentPaneLayout);
											internalFrame1ContentPaneLayout.setHorizontalGroup(
												internalFrame1ContentPaneLayout.createParallelGroup()
													.addGroup(internalFrame1ContentPaneLayout.createSequentialGroup()
														.addContainerGap()
														.addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
														.addContainerGap())
											);
											internalFrame1ContentPaneLayout.setVerticalGroup(
												internalFrame1ContentPaneLayout.createParallelGroup()
													.addGroup(internalFrame1ContentPaneLayout.createSequentialGroup()
														.addContainerGap()
														.addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
														.addContainerGap())
											);
										}
										peerInfoStatus1.add(internalFrame1, JLayeredPane.DEFAULT_LAYER);
										internalFrame1.setBounds(15, 0, 365, 160);
									}

									//---- graphicONcheckBox ----
									graphicONcheckBox.setText("Graphic ON");

									//---- advancedConfirmBut ----
									advancedConfirmBut.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/ok.png")));

									GroupLayout peerInfoStatusLayout = new GroupLayout(peerInfoStatus);
									peerInfoStatus.setLayout(peerInfoStatusLayout);
									peerInfoStatusLayout.setHorizontalGroup(
										peerInfoStatusLayout.createParallelGroup()
											.addGroup(peerInfoStatusLayout.createSequentialGroup()
												.addGroup(peerInfoStatusLayout.createParallelGroup()
													.addGroup(peerInfoStatusLayout.createSequentialGroup()
														.addGap(26, 26, 26)
														.addComponent(graphicONcheckBox)
														.addGap(73, 73, 73)
														.addComponent(jComboBoxNumRegionXPeer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
														.addGap(80, 80, 80)
														.addComponent(advancedConfirmBut, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE))
													.addGroup(peerInfoStatusLayout.createSequentialGroup()
														.addContainerGap()
														.addComponent(peerInfoStatus1, GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)))
												.addContainerGap())
									);
									peerInfoStatusLayout.setVerticalGroup(
										peerInfoStatusLayout.createParallelGroup()
											.addGroup(peerInfoStatusLayout.createSequentialGroup()
												.addComponent(peerInfoStatus1, GroupLayout.PREFERRED_SIZE, 175, GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addGroup(peerInfoStatusLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
													.addGroup(GroupLayout.Alignment.LEADING, peerInfoStatusLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
														.addComponent(graphicONcheckBox)
														.addComponent(jComboBoxNumRegionXPeer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
													.addComponent(advancedConfirmBut, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE))
												.addContainerGap(16, Short.MAX_VALUE))
									);
								}

								GroupLayout jPanelAdvancedMainLayout = new GroupLayout(jPanelAdvancedMain);
								jPanelAdvancedMain.setLayout(jPanelAdvancedMainLayout);
								jPanelAdvancedMainLayout.setHorizontalGroup(
									jPanelAdvancedMainLayout.createParallelGroup()
										.addGroup(jPanelAdvancedMainLayout.createSequentialGroup()
											.addContainerGap()
											.addComponent(scrollPaneTree, GroupLayout.PREFERRED_SIZE, 207, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
											.addComponent(peerInfoStatus, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
											.addContainerGap())
								);
								jPanelAdvancedMainLayout.setVerticalGroup(
									jPanelAdvancedMainLayout.createParallelGroup()
										.addGroup(GroupLayout.Alignment.TRAILING, jPanelAdvancedMainLayout.createSequentialGroup()
											.addContainerGap()
											.addGroup(jPanelAdvancedMainLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
												.addComponent(peerInfoStatus, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(scrollPaneTree, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE))
											.addContainerGap())
								);
							}

							//======== jPanelSetButton2 ========
							{

								//---- buttonSetConfigDefault2 ----
								buttonSetConfigDefault2.setText("Set");

								GroupLayout jPanelSetButton2Layout = new GroupLayout(jPanelSetButton2);
								jPanelSetButton2.setLayout(jPanelSetButton2Layout);
								jPanelSetButton2Layout.setHorizontalGroup(
									jPanelSetButton2Layout.createParallelGroup()
										.addGroup(GroupLayout.Alignment.TRAILING, jPanelSetButton2Layout.createSequentialGroup()
											.addContainerGap(522, Short.MAX_VALUE)
											.addComponent(buttonSetConfigDefault2, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE)
											.addGap(72, 72, 72))
								);
								jPanelSetButton2Layout.setVerticalGroup(
									jPanelSetButton2Layout.createParallelGroup()
										.addGroup(GroupLayout.Alignment.TRAILING, jPanelSetButton2Layout.createSequentialGroup()
											.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
											.addComponent(buttonSetConfigDefault2)
											.addContainerGap())
								);
							}

							GroupLayout jPanelAdvancedLayout = new GroupLayout(jPanelAdvanced);
							jPanelAdvanced.setLayout(jPanelAdvancedLayout);
							jPanelAdvancedLayout.setHorizontalGroup(
								jPanelAdvancedLayout.createParallelGroup()
									.addComponent(jPanelSetButton2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
									.addGroup(jPanelAdvancedLayout.createSequentialGroup()
										.addComponent(jPanelAdvancedMain, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addContainerGap())
							);
							jPanelAdvancedLayout.setVerticalGroup(
								jPanelAdvancedLayout.createParallelGroup()
									.addGroup(GroupLayout.Alignment.TRAILING, jPanelAdvancedLayout.createSequentialGroup()
										.addContainerGap()
										.addComponent(jPanelAdvancedMain, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addGap(18, 18, 18)
										.addComponent(jPanelSetButton2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							);
						}
						tabbedPane2.addTab("Advanced", jPanelAdvanced);

					}

					//======== panelConsole ========
					{

						//======== scrollPane3 ========
						{

							//---- textField1 ----
							textField1.setEditable(false);
							scrollPane3.setViewportView(textField1);
						}

						GroupLayout panelConsoleLayout = new GroupLayout(panelConsole);
						panelConsole.setLayout(panelConsoleLayout);
						panelConsoleLayout.setHorizontalGroup(
							panelConsoleLayout.createParallelGroup()
								.addGroup(panelConsoleLayout.createParallelGroup()
									.addGroup(panelConsoleLayout.createSequentialGroup()
										.addGap(0, 0, Short.MAX_VALUE)
										.addComponent(scrollPane3, GroupLayout.PREFERRED_SIZE, 679, GroupLayout.PREFERRED_SIZE)
										.addGap(0, 0, Short.MAX_VALUE)))
								.addGap(0, 679, Short.MAX_VALUE)
						);
						panelConsoleLayout.setVerticalGroup(
							panelConsoleLayout.createParallelGroup()
								.addGroup(panelConsoleLayout.createParallelGroup()
									.addGroup(panelConsoleLayout.createSequentialGroup()
										.addGap(0, 0, Short.MAX_VALUE)
										.addComponent(scrollPane3, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE)
										.addGap(0, 0, Short.MAX_VALUE)))
								.addGap(0, 76, Short.MAX_VALUE)
						);
					}

					GroupLayout jPanelContainerTabbedPaneLayout = new GroupLayout(jPanelContainerTabbedPane);
					jPanelContainerTabbedPane.setLayout(jPanelContainerTabbedPaneLayout);
					jPanelContainerTabbedPaneLayout.setHorizontalGroup(
						jPanelContainerTabbedPaneLayout.createParallelGroup()
							.addGroup(GroupLayout.Alignment.TRAILING, jPanelContainerTabbedPaneLayout.createSequentialGroup()
								.addContainerGap()
								.addGroup(jPanelContainerTabbedPaneLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
									.addComponent(panelConsole, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
									.addComponent(tabbedPane2, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 679, Short.MAX_VALUE))
								.addContainerGap())
					);
					jPanelContainerTabbedPaneLayout.setVerticalGroup(
						jPanelContainerTabbedPaneLayout.createParallelGroup()
							.addGroup(jPanelContainerTabbedPaneLayout.createSequentialGroup()
								.addGap(3, 3, 3)
								.addComponent(tabbedPane2, GroupLayout.PREFERRED_SIZE, 384, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(panelConsole, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					);
				}
				GroupLayout jPanelSetDistributionLayout = new GroupLayout(jPanelSetDistribution);
				jPanelSetDistribution.setLayout(jPanelSetDistributionLayout);
				jPanelSetDistributionLayout.setHorizontalGroup(
					jPanelSetDistributionLayout.createParallelGroup()
						.addGroup(jPanelSetDistributionLayout.createSequentialGroup()
							.addComponent(jPanelSettings, GroupLayout.PREFERRED_SIZE, 254, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
							.addComponent(jPanelContainerTabbedPane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
				jPanelSetDistributionLayout.setVerticalGroup(
					jPanelSetDistributionLayout.createParallelGroup()
						.addGroup(jPanelSetDistributionLayout.createSequentialGroup()
							.addGroup(jPanelSetDistributionLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
								.addComponent(jPanelContainerTabbedPane, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jPanelSettings, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addContainerGap())
				);
			}

			//---- jLabelPlayButton ----
			jLabelPlayButton.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/NotStopped.png")));

			//---- jLabelPauseButton ----
			jLabelPauseButton.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/PauseOff.png")));


			//---- labelStopButton ----
			jLabelPlayButton.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/NotPlaying.png")));
			jLabelPlayButton.addMouseListener(new MouseListener() {
			
				@Override
				public void mouseReleased(MouseEvent arg0) {}
			
				@Override
				public void mousePressed(MouseEvent arg0) {}
			
				@Override
				public void mouseExited(MouseEvent arg0) {}
			
				@Override
				public void mouseEntered(MouseEvent arg0) {}
			
				@Override
				public void mouseClicked(MouseEvent arg0) {
					jLabelPlayButton.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/Playing.png")));
					try {
						master.play();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			//---- labelStopButton2 ----	
			jLabelStopButton.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/NotStopped.png")));
			jLabelStopButton.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent e) {}
				
				@Override
				public void mousePressed(MouseEvent e) {}
				
				@Override
				public void mouseExited(MouseEvent e) {}
				
				@Override
				public void mouseEntered(MouseEvent e) {}
				
				@Override
				public void mouseClicked(MouseEvent e) {
					jLabelStopButton.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/Stopped.png")));
					try {
						master.stop();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
			
			//---- labelPauseButton ----
			jLabelPauseButton.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/PauseOff.png")));
			jLabelPauseButton.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent e) {}
				
				@Override
				public void mousePressed(MouseEvent e) {}
				
				@Override
				public void mouseExited(MouseEvent e) {}
				
				@Override
				public void mouseEntered(MouseEvent e) {}
				
				@Override
				public void mouseClicked(MouseEvent e) {
					jLabelPauseButton.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/PauseOn.png")));
					try {
						master.pause();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});



			//======== jPanelNumStep ========
			{

				//---- writeStepLabel ----
				writeStepLabel.setText("txt");

				GroupLayout jPanelNumStepLayout = new GroupLayout(jPanelNumStep);
				jPanelNumStep.setLayout(jPanelNumStepLayout);
				jPanelNumStepLayout.setHorizontalGroup(
					jPanelNumStepLayout.createParallelGroup()
						.addGroup(GroupLayout.Alignment.TRAILING, jPanelNumStepLayout.createSequentialGroup()
							.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(writeStepLabel))
				);
				jPanelNumStepLayout.setVerticalGroup(
					jPanelNumStepLayout.createParallelGroup()
						.addGroup(jPanelNumStepLayout.createSequentialGroup()
							.addComponent(writeStepLabel)
							.addContainerGap(12, Short.MAX_VALUE))
				);
			}

			GroupLayout panelMainLayout = new GroupLayout(panelMain);
			panelMain.setLayout(panelMainLayout);
			panelMainLayout.setHorizontalGroup(
				panelMainLayout.createParallelGroup()
					.addGroup(panelMainLayout.createSequentialGroup()
						.addComponent(jPanelContainerSettings, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jPanelSetDistribution, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addContainerGap())
					.addComponent(jPanelContainerConnection, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(GroupLayout.Alignment.TRAILING, panelMainLayout.createSequentialGroup()
						.addContainerGap(642, Short.MAX_VALUE)
						.addComponent(jLabelStep)
						.addGap(4, 4, 4)
						.addComponent(jPanelNumStep, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(24, 24, 24)
						.addComponent(jLabelPlayButton)
						.addGap(10, 10, 10)
						.addComponent(jLabelPauseButton)
						.addGap(10, 10, 10)
						.addComponent(jLabelStopButton)
						.addGap(189, 189, 189))
			);
			panelMainLayout.setVerticalGroup(
				panelMainLayout.createParallelGroup()
					.addGroup(panelMainLayout.createSequentialGroup()
						.addComponent(jPanelContainerConnection, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGroup(panelMainLayout.createParallelGroup()
							.addGroup(panelMainLayout.createSequentialGroup()
								.addGap(35, 35, 35)
								.addComponent(jPanelContainerSettings, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
							.addGroup(panelMainLayout.createSequentialGroup()
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jPanelSetDistribution, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(panelMainLayout.createParallelGroup()
							.addComponent(jLabelStep)
							.addComponent(jPanelNumStep, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(jLabelPlayButton, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
							.addComponent(jLabelPauseButton, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
							.addComponent(jLabelStopButton, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE))
						.addContainerGap())
			);
		}

		GroupLayout contentPaneLayout = new GroupLayout(contentPane);
		contentPane.setLayout(contentPaneLayout);
		contentPaneLayout.setHorizontalGroup(
			contentPaneLayout.createParallelGroup()
				.addComponent(panelMain, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		);
		contentPaneLayout.setVerticalGroup(
			contentPaneLayout.createParallelGroup()
				.addComponent(panelMain, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		);
		pack();
		setLocationRelativeTo(null);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}
	
	private void connect(){
		try {
			ip = textFieldAddress.getText();
			port = textFieldPort.getText();
			address = new Address(textFieldAddress.getText(),textFieldPort.getText());
			connection = new ConnectionNFieldsWithActiveMQAPI();
			connection.setupConnection(address);
			
			master = new MasterDaemonStarter(connection);
			
			if(!master.connectToServer())
				JOptionPane.showMessageDialog(null,"Wrong data!!!");
			else{
				JOptionPane.showMessageDialog(null,"Connected to the server!!!");
				connected = true;
				ArrayList<String> peers = master.getTopicList();
				for(String p : peers)
				{
					master.info(p);
					root.add(new DefaultMutableTreeNode(p));
				}
				labelWriteNumOfPeer.setText(""+peers.size());
				dont = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	private ArrayList<String> checkSyntaxForm(int num,int width,int height,int numAgentsForPeer){
		
		ArrayList<String> errors = new ArrayList<String>();

		//System.out.println("Num_regions = "+NUM_REGIONS);
		//System.out.println("total = "+total);
		//System.out.println("Peers = "+root.getChildCount());
		
		
		
	
		
		// check total number of regions coincides with total number of assignments
		if((NUM_REGIONS % root.getChildCount()) != 0 || total != NUM_REGIONS)
			errors.add("Please check regions number! \n");
		//check field's misures if horizontal mode is selected
		if(radioButtonHorizontal.isSelected() && width%num != 0)
				errors.add("Cannot divide the field in "+num+" regions! \n");
		//check field's misures if square mode is selected
		if(radioButtonSquare.isSelected() && (width % Math.sqrt(num) != 0 || height % Math.sqrt(num) != 0))
				errors.add("Cannot divide field in Sqrt(REGIONS),please check field parameter \n");
		//check id agents number > 0
		if(numAgentsForPeer<=0)
				errors.add("Missing number of agents\n");
				
		return errors;
	}

	private void submitCustomizeMode(){
		//System.out.println(total +" JmasterUi submitCustomizeMode()");
		ArrayList<String> errors = null;
		WIDTH = Integer.parseInt(textFieldWidth.getText());
		HEIGHT = Integer.parseInt(textFieldHeight.getText());
		NUM_REGIONS = Integer.parseInt(""+jComboRegions.getSelectedItem());
		NUM_AGENTS = Integer.parseInt(textFieldAgents.getText());
		MAX_DISTANCE = Integer.parseInt(textFieldMaxDistance.getText());
		try {
			file = new FileOutputStream("test_cells_"+NUM_REGIONS+"_agents_"+NUM_AGENTS+"_width_"+WIDTH+"_height_"+HEIGHT+".txt");
			printer=new PrintStream(file);
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		if(radioButtonHorizontal.isSelected())
			MODE = DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE;
		else
			MODE = DSparseGrid2DFactory.SQUARE_DISTRIBUTION_MODE;
		errors = checkSyntaxForm(NUM_REGIONS,(Integer)WIDTH,(Integer)HEIGHT,NUM_AGENTS);
		
		(new Trigger(connection)).asynchronousReceiveToTriggerTopic(new TriggerListener(textField1,file, printer));
		
		// Wrong data inserted
		if(errors.size() > 0){
			String x="ERRORS"+"\n";
			for(String e : errors)
				x=x+e+"\n";
			JOptionPane.showMessageDialog(null,x);
		}
		getSteps();
		//sim = files.getSelectedFile().getName();
		
		master.start(NUM_REGIONS, (Integer)WIDTH, (Integer)HEIGHT, NUM_AGENTS,MAX_DISTANCE,MODE, config,selectedSimulation);
	}
	
	private void submitDefaultMode(){
		ArrayList<String> errors = new ArrayList<String>();
		//checkSyntaxForm(NUM_REGIONS,(Integer)WIDTH,(Integer)HEIGHT,NUM_AGENTS);
		WIDTH = Integer.parseInt(textFieldWidth.getText());
		HEIGHT = Integer.parseInt(textFieldHeight.getText());
		NUM_REGIONS = Integer.parseInt(""+jComboRegions.getSelectedItem());
		NUM_AGENTS = Integer.parseInt(textFieldAgents.getText());
		MAX_DISTANCE = Integer.parseInt(textFieldMaxDistance.getText());
		withGui = graphicONcheckBox2.isSelected();
		try {
			file = new FileOutputStream("test_cells_"+NUM_REGIONS+"_agents_"+NUM_AGENTS+"_width_"+WIDTH+"_height_"+HEIGHT+".txt");
			printer=new PrintStream(file);
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		(new Trigger(connection)).asynchronousReceiveToTriggerTopic(new TriggerListener(textField1,file,printer));
		
		if(radioButtonHorizontal.isSelected())
			MODE = DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE;
		else
			MODE = DSparseGrid2DFactory.SQUARE_DISTRIBUTION_MODE;
			if (NUM_REGIONS % root.getChildCount() != 0)
				errors.add("NUM_REGIONS < > = NUM_PEERS\n,please set Advanced mode!");
			if(errors.size() == 0){
				int div = NUM_REGIONS / root.getChildCount();
				EntryVal<Integer, Boolean> value; 
				try{
						for(String topic : master.getTopicList()){
							value = new EntryVal(div, withGui);
							//config.put(topic, div);
							config.put(topic, value);
						}
						getSteps();
				}catch (Exception e) {
					e.printStackTrace();
				}
				master.start(NUM_REGIONS, (Integer)WIDTH, (Integer)HEIGHT, NUM_AGENTS,MAX_DISTANCE,MODE, config,selectedSimulation);
			}
			else{
				String x="";
				for(String s : errors)
					x=x+s;
				JOptionPane.showMessageDialog(null,x);
			}
	}
	
	private void clickTreeListener(){
		String key ="";
		String[] address;
		PeerStatusInfo info=null;
		if(connected && dont){
			key = tree1.getLastSelectedPathComponent().toString();
			if(key.startsWith("SERVICE")){
				address = key.split("-");
				info = master.getLatestUpdate(address[0]+"-"+address[1]);
				if(info!=null)
					label8.setText("IP : "+info.getAddress()+"\n"+"OS : "+info.getoS()+"\n"+"Architecture : "+info.getArch()+"\n"+"Number of Core : "+info.getNum_core());
			}
		}
	}
	
	private void confirm(){
		String key = tree1.getLastSelectedPathComponent().toString();
		int reg = (Integer)jComboBoxNumRegionXPeer.getSelectedItem();
		withGui = graphicONcheckBox.isSelected();
		EntryVal<Integer,Boolean> value = new EntryVal<Integer,Boolean>(reg,withGui);
		//config.put(key,reg);
		config.put(key, value);
	}
	
	private void getSteps(){
		connectionForSteps = new ConnectionNFieldsWithActiveMQAPI(new MyMessageListener() {
			long initial_time=-1;
			Long step;
			@Override
			public void onMessage(Message arg0) {
				Object o=null; 
				try {
					o = parseMessage(arg0);
				} catch (JMSException e) {
					e.printStackTrace();
				}
				MyHashMap mh = (MyHashMap)o;
				if(mh.get("step")!=null){
					step=(Long)mh.get("step");
					
					if(step==0)
						{
							initial_time=System.currentTimeMillis();
							printer.println("Number regions:"+NUM_REGIONS+" Number agents:"+NUM_AGENTS+" Width:"+WIDTH+" Height:"+HEIGHT);
							printer.println("Step :0 Time:"+initial_time);
						}
						else if(step == limitStep)
						{
							long fifty_time=System.currentTimeMillis();
							printer.println("Step :"+limitStep+" Time: "+fifty_time);
							
							long time= (fifty_time - initial_time );
							
							printer.println("Total Time : "+time);
							printer.close();
						}
						writeStepLabel.setText(""+mh.get("step"));
				}
			}
		});
		try{
			connectionForSteps.setupConnection(address);
			connectionForSteps.createTopic("step",1);
			connectionForSteps.subscribeToTopic("step");
			connectionForSteps.asynchronousReceive("step");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initializeDefaultLabel(){
		NUM_REGIONS = (Integer)jComboRegions.getSelectedItem();
		int numOfPeer = root.getChildCount();
		if(textFieldWidth.getText().equals(""))
			textFieldWidth.setText("0");
		WIDTH = Integer.parseInt(textFieldWidth.getText());
		if(textFieldHeight.getText().equals(""))
			textFieldHeight.setText("0");
		HEIGHT = Integer.parseInt(textFieldHeight.getText());
		labelWriteReg.setText(""+NUM_REGIONS);
		labelWriteNumOfPeer.setText(""+numOfPeer);
		if(numOfPeer > 0 && (NUM_REGIONS % numOfPeer) == 0)
			labelWriteRegForPeer.setText(""+NUM_REGIONS/numOfPeer);
		else 
			labelWriteRegForPeer.setText("");
		NUM_AGENTS = Integer.parseInt(textFieldAgents.getText());
		if(radioButtonHorizontal.isSelected()){
			MODE = DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE;
			String w="";
			String h=""+HEIGHT;
			if((Integer)WIDTH % NUM_REGIONS == 0)
				w = ""+(Integer)WIDTH/ NUM_REGIONS;
			labelWriteRegWidth.setText(""+w);
			labelWriteRegHeight.setText(""+h);
			labelWriteDistrMode.setText("HORIZONTAL MODE");
		}
		else{
			MODE = DSparseGrid2DFactory.SQUARE_DISTRIBUTION_MODE;
			int rad = (int) Math.sqrt(NUM_REGIONS);
			String w="";
			String h="";
			if((Integer)WIDTH % rad == 0){
				w = "" + (Integer)WIDTH/NUM_REGIONS;
				h = "" + (Integer)HEIGHT/NUM_REGIONS;
			}
			labelWriteRegWidth.setText(""+w);
			labelWriteRegHeight.setText(""+h);
			labelWriteDistrMode.setText("SQUARE MODE");
		}
	}
	
	public static void main(String[] args){
		JFrame a = new JMasterUI();
		a.setVisible(true);
	}

	private JMenuItem menuItemServer;
	private JMenuItem menuNewSim;
	private JMenuItem menuConfSim;
	private JScrollPane scrollPaneTree;	//scrollPaneTree
	private JLabel advancedConfirmBut;	//advancedConfirmBut;
	private JCheckBox graphicONcheckBox;
	private JButton buttonSetConfigAdvanced;
	private JLabel jLabelPlayButton;	//labelstopbutton
	private JLabel jLabelPauseButton;	//labelpausebutton
	private JPanel jPanelNumStep;		//panel1
	private JLabel writeStepLabel;		//labelWriteStep;
	private JLabel jLabelStep;	//step
	private JLabel jLabelStopButton;	//labelStopButton2
	private String selectedSimulation;
	private JScrollPane scrollPane1;
	private JDesktopPane peerInfoStatus1;
	private MasterDaemonStarter master;
	private String ip;
	private String port;
	private DefaultMutableTreeNode root;
	private boolean connected = false;
	private HashMap<String,EntryVal<Integer,Boolean>> config;
	private int total=0;
	boolean radioItemFlag=false;	//radioItemFalg
	private Address address;
	int res;
	private ConnectionNFieldsWithActiveMQAPI connection;
	private ConnectionNFieldsWithActiveMQAPI connectionForSteps;
	private Start starter;
	private Console c;
	private boolean dont=true;
	private int MODE;
	private Object WIDTH;
	private Object HEIGHT;
	private int NUM_REGIONS;
	private int NUM_AGENTS;
	private int MAX_DISTANCE;
	private JMasterUI me = this;
	private String sim;
	private JFileChooser files;
	private boolean gui;
	public boolean centralGui = true;
	private static final long serialVersionUID = 1L;
	private boolean withGui = false;
	private JTree tree1;
	private JTextArea label8;
	private JDesktopPane peerInfoStatus;
	private JInternalFrame internalFrame1;
	private JPanel panelConsole;
	
	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JMenuBar menuBar1;
	private JMenu jMenuFile;
	private JMenuItem menuItemOpen;
	private JMenuItem menuItemExit;
	private JMenu jMenuAbout;
	private JMenuItem menuItemInfo;
	private JMenuItem menuItenHelp;
	private JPanel panelMain;
	private JPanel jPanelContainerConnection;
	private JPanel jPanelConnection;
	private JLabel jLabelAddress;
	private JTextField textFieldAddress;
	private JLabel jLabelPort;
	private JTextField textFieldPort;
	private JLabel refreshServerLabel;
	private JButton buttonRefreshServerLabel;
	private JPanel jPanelContainerSettings;
	private JPanel jPanelSetDistribution;
	private JPanel jPanelSettings;
	private JRadioButton radioButtonHorizontal;
	private JRadioButton radioButtonSquare;
	private JLabel jLabelHorizontal;
	private JLabel jLabelSquare;
	private JLabel jLabelRegions;
	private JLabel jLabelMaxDistance;
	private JLabel jLabelWidth;
	private JTextField textFieldMaxDistance;
	private JComboBox jComboRegions;
	private JComboBox jComboBoxNumRegionXPeer;
	private JTextField textFieldWidth;
	private JLabel jLabelHeight;
	private JTextField textFieldHeight;
	private JLabel jLabelAgents;
	private JTextField textFieldAgents;
	private JLabel jLabelChooseSimulation;
	private JComboBox jComboBoxChooseSimulation;
	private JPanel jPanelContainerTabbedPane;
	private JTabbedPane tabbedPane2;
	private JPanel jPanelDefault;
	private JLabel labelSimulationConfigSet;
	private JLabel labelRegionsResume;
	private JLabel labelNumOfPeerResume;
	private JLabel labelRegForPeerResume;
	private JLabel labelWriteReg;
	private JLabel labelWriteNumOfPeer;
	private JLabel labelWriteRegForPeer;
	private JLabel labelWidthRegion;
	private JLabel labelheightRegion;
	private JLabel labelDistrMode;
	private JLabel labelWriteRegWidth;
	private JLabel labelWriteRegHeight;
	private JLabel labelWriteDistrMode;
	private JCheckBox graphicONcheckBox2;
	private JPanel jPanelSetButton;
	private JButton buttonSetConfigDefault;
	private JPanel jPanelAdvanced;
	private JPanel jPanelAdvancedMain;
	private JPanel jPanelSetButton2;
	private JButton buttonSetConfigDefault2;
	private JScrollPane scrollPane3;
	private JTextArea textField1;
	// codice profiling
	private FileOutputStream file;
	private PrintStream printer;
	private int limitStep;
	// fine codice profiling
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
