package dmason.util.SystemManagement;

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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import sim.display.Console;
import dmason.sim.engine.DistributedMultiSchedule;
import dmason.sim.field.grid.DSparseGrid2DFactory;
import dmason.util.connection.Address;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;
import dmason.util.connection.MyHashMap;
import dmason.util.connection.MyMessageListener;
import dmason.util.garbagecollector.Start;
import dmason.util.trigger.Trigger;
import dmason.util.trigger.TriggerListener;


/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
/**
 * Provides a GUI to setup the simulation.
 * @author unascribed
 * @author Luca Vicidomini
 * @author Fabio Fulgido
 *
 */
public class JMasterUI extends JFrame{

	/**
	 * An utility class used to represent a simulation class as a
	 * combobox entry -- PLEASE NOTE THIS IS JUST A TEMPORARY
	 * SOLUTION: simulation's definitions shouldn't be written
	 * in JMasterUI class!!!
	 * @author Luca Vicidomini
	 */
	class SimComboEntry
	{
		/**
		 * A short name that will be shown to the user.
		 */
		String shortName;
		
		/**
		 * Qualified name of the class implementing the proper simulation.
		 */
		String fullSimName;
		
		/**
		 * Creates a new entry for the combobox.
		 * @param shortName A short name that will be shown to the user.
		 * @param fullSimName Qualified name of the class implementing the proper simulation.
		 */
		public SimComboEntry(String shortName, String fullSimName) { this.shortName = shortName; this.fullSimName = fullSimName; }
		@Override public String toString() { return shortName; }
	}
	
	public JMasterUI()
	{	
		initComponents();
		setSystemSettingsEnabled(false);
    	config = new HashMap<String, EntryVal<Integer,Boolean>>();
		setTitle("JMasterUI");
		initializeDefaultLabel();
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setResizable(false);
		starter = new Start();
	
	}

	private void initComponents() {

		menuBar1 = new JMenuBar();
		jMenuFile = new JMenu();
		//menuItemOpen = new JMenuItem();
		menuItemExit = new JMenuItem();
		jMenuAbout = new JMenu();
		menuItemInfo = new JMenuItem();
		menuItemHelp = new JMenuItem();
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
		architectureLabel = new JTextArea();
		architectureLabel.setBackground(Color.BLACK);
		architectureLabel.setForeground(Color.GREEN);
		architectureLabel.setEditable(false);
		advancedConfirmBut = new JLabel();
		graphicONcheckBox = new JCheckBox();
		jCheckBoxLoadBalancing = new JCheckBox("Load Balancing", false);
		jCheckBoxLoadBalancing.setEnabled(false);
		jCheckBoxLoadBalancing.setSelected(false);
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
		radioButtonHorizontal.setSelected(true);
		ip = textFieldAddress.getText();
		port = textFieldPort.getText();
		menuBar1 = new JMenuBar();
		jMenuFile = new JMenu();
		menuItemExit = new JMenuItem();
		menuNewSim = new JMenuItem();
		jMenuAbout = new JMenu();
		menuItemInfo = new JMenuItem();
		menuItemHelp = new JMenuItem();
		scrollPane3 = new JScrollPane();
		notifyArea = new JTextArea();
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
		jComboBoxChooseSimulation.addItem(new SimComboEntry("Flockers", "dmason.sim.app.DFlockers.DFlockers"));
		jComboBoxChooseSimulation.addItem(new SimComboEntry("Particles", "dmason.sim.app.DParticles.DParticles"));
		jComboBoxChooseSimulation.addItem(new SimComboEntry("Ants Foraging", "dmason.sim.app.DAntsForage.DAntsForage"));

		selectedSimulation = ((SimComboEntry)jComboBoxChooseSimulation.getSelectedItem()).fullSimName;
		jComboBoxChooseSimulation.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				// Prevent executing listener's actions two times
				if (e.getStateChange() != ItemEvent.SELECTED)
					return;
				selectedSimulation = ((SimComboEntry)jComboBoxChooseSimulation.getSelectedItem()).fullSimName;
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

		jCheckBoxLoadBalancing.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(jCheckBoxLoadBalancing.isSelected())
					labelWriteDistrMode.setText("SQUARE BALANCED MODE");
				else
					labelWriteDistrMode.setText("SQUARE MODE");

			}
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
				jCheckBoxLoadBalancing.setEnabled(false);
			}
		});

		radioButtonSquare.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				radioItemFlag = true;
				jComboRegions.removeAllItems();
				for(int i=2;i<=30;i++)
					jComboRegions.addItem(i*i);
				initializeDefaultLabel();
				radioItemFlag = false;
				jCheckBoxLoadBalancing.setEnabled(true);
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
				
				JOptionPane.showMessageDialog(null,"Region assigned !");
				
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
				jMenuFile.setText("    File    ");
				
				

				menuNewSim.setText("New    ");
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
				jMenuAbout.setText(" ?  ");

				//---- menuItemInfo ----
				menuItemInfo.setText("Info");
				menuItemInfo.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						JOptionPane.showMessageDialog(null, "D-MASON version 1.5","Info",1);
					
						
					}
				});
				
				
				
				jMenuAbout.add(menuItemInfo);

				//---- menuItenHelp ----
				menuItemHelp.setText("Help");
				menuItemHelp.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						try {
							java.net.URI uri = new java.net.URI("http://isis.dia.unisa.it/projects/dmason/");
							try {
								java.awt.Desktop.getDesktop().browse(uri);
							} catch (IOException e) {
								e.printStackTrace();
							}
						} catch (URISyntaxException e) {
							e.printStackTrace();
						}
						
					}
				});
				
				
				
				
				jMenuAbout.add(menuItemHelp);
				
//				jMenuAbout.addActionListener(new ActionListener() {
//					
//					@Override
//					public void actionPerformed(ActionEvent e) {
//						
//						try {
//							System.out.println("entro");
//							java.net.URI uri = new java.net.URI("http://www.google.com");
//							System.out.println(uri);
//							try {
//								java.awt.Desktop.getDesktop().browse(uri);
//							} catch (IOException e1) {
//								// TODO Auto-generated catch block
//								e1.printStackTrace();
//							}
//							
//						} catch (URISyntaxException e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
//						
//					}
//				});
				
				
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




					textFieldAgents.addKeyListener(new KeyListener() {

						@Override
						public void keyTyped(KeyEvent e) {}

						@Override
						public void keyReleased(KeyEvent e) {
							initializeDefaultLabel();
						}

						@Override
						public void keyPressed(KeyEvent e) {}
					});





					textFieldMaxDistance.addKeyListener(new KeyListener() {

						@Override
						public void keyTyped(KeyEvent e) {}

						@Override
						public void keyReleased(KeyEvent e) {
							initializeDefaultLabel();
						}

						@Override
						public void keyPressed(KeyEvent e) {}
					});




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
								{
									jCheckBoxLoadBalancing.setText("Load Balancing");
								}

								GroupLayout jPanelSetButtonLayout = new GroupLayout(jPanelSetButton);
								jPanelSetButton.setLayout(jPanelSetButtonLayout);
								jPanelSetButtonLayout.setVerticalGroup(jPanelSetButtonLayout.createSequentialGroup()
									.addContainerGap()
									.addGroup(jPanelSetButtonLayout.createParallelGroup()
									    .addGroup(GroupLayout.Alignment.LEADING, jPanelSetButtonLayout.createSequentialGroup()
									        .addComponent(jCheckBoxLoadBalancing, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
									        .addGap(0, 8, Short.MAX_VALUE))
									    .addGroup(GroupLayout.Alignment.LEADING, jPanelSetButtonLayout.createSequentialGroup()
									        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 0, Short.MAX_VALUE)
									        .addComponent(buttonSetConfigDefault, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)))
									.addContainerGap());
								jPanelSetButtonLayout.setHorizontalGroup(jPanelSetButtonLayout.createSequentialGroup()
									.addContainerGap(17, 17)
									.addComponent(jCheckBoxLoadBalancing, GroupLayout.PREFERRED_SIZE, 114, GroupLayout.PREFERRED_SIZE)
									.addGap(0, 397, Short.MAX_VALUE)
									.addComponent(buttonSetConfigDefault, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE)
									.addContainerGap(72, 72));
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
												architectureLabel.setText("Architecture Information");
												scrollPane1.setViewportView(architectureLabel);
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
					
					//======== jPanel Simulation Parameters
					jPanelSimParams = new JPanel();
					{
						jPanelSimParams.setBorder(new EtchedBorder());
						tabbedPane2.addTab("Simulation parameters", jPanelSimParams);
					}

					//======== panelConsole ========
					{

						//======== scrollPane3 ========
						{

							//---- textField1 ----
							notifyArea.setEditable(false);
							scrollPane3.setViewportView(notifyArea);
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
					jLabelPauseButton.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/PauseOff.png")));
					jLabelStopButton.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/NotStopped.png")));
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
					jLabelPlayButton.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/NotPlaying.png")));
					jLabelPauseButton.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/PauseOff.png")));
					
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
					jLabelStopButton.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/NotStopped.png")));
					jLabelPlayButton.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/NotPlaying.png")));
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
		
		refreshServerLabel.setVisible(false);
	}

	private void connect(){
		try
		{
			ip = textFieldAddress.getText();			    
			port = textFieldPort.getText();  
			address = new Address(textFieldAddress.getText(),textFieldPort.getText());
			connection = new ConnectionNFieldsWithActiveMQAPI();
			connection.setupConnection(address);
			master = new MasterDaemonStarter(connection);

			if (!master.connectToServer())
			{
				notifyArea.append("Connection refused to " + textFieldAddress.getText()+", please check IP address and port.\n");
			}

			else{
				setConnectionSettingsEnabled(false);
				setSystemSettingsEnabled(true);
				notifyArea.append("Connection estabilished.\n");
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



		// check total number of regions coincides with total number of assignments
		if((numRegions % root.getChildCount()) != 0 || total != numRegions)
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
		//check field's measures if square mode balanced is selected
		//the width and the height must be equals, and both must be divisible by sqrt(peer)*3
		if(radioButtonSquare.isSelected() && jCheckBoxLoadBalancing.isSelected() && (width % (Math.sqrt(num)*3) != 0 || height % (Math.sqrt(num)*3) != 0))
			errors.add("Cannot divide field in Sqrt(REGIONS)*3 for the Load Balanced Mode,please check field parameter \n");

		return errors;
	}

	private void submitCustomizeMode()
	{
		//System.out.println(total +" JmasterUi submitCustomizeMode()");
		ArrayList<String> errors = null;
		WIDTH = Integer.parseInt(textFieldWidth.getText());   
		HEIGHT = Integer.parseInt(textFieldHeight.getText());
		numRegions = Integer.parseInt(""+jComboRegions.getSelectedItem());
		numAgents = Integer.parseInt(textFieldAgents.getText());
		maxDistance = Integer.parseInt(textFieldMaxDistance.getText());
		
		
			try {
				file = new FileHandler("test_cells_"+numRegions+"_agents_"+numAgents+"_width_"+WIDTH+"_height_"+HEIGHT+".txt");
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
		if(radioButtonHorizontal.isSelected())
			MODE = DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE;
		else if(radioButtonSquare.isSelected() && !jCheckBoxLoadBalancing.isSelected())
			MODE = DSparseGrid2DFactory.SQUARE_DISTRIBUTION_MODE;
		else if(radioButtonSquare.isSelected() && jCheckBoxLoadBalancing.isSelected())
			MODE = DSparseGrid2DFactory.SQUARE_BALANCED_DISTRIBUTION_MODE;

		(new Trigger(connection)).asynchronousReceiveToTriggerTopic(new TriggerListener(notifyArea,logger));

		// Wrong data inserted
		if(errors.size() > 0){
			String x="ERRORS"+"\n";
			for(String e : errors)
				x=x+e+"\n";
			JOptionPane.showMessageDialog(null,x);
		}
		getSteps();
		//sim = files.getSelectedFile().getName();

		JOptionPane.showMessageDialog(null,"Setting completed !");

		master.start(numRegions, (Integer)WIDTH, (Integer)HEIGHT, numAgents,maxDistance,MODE, config,selectedSimulation,this);
	}

	private void submitDefaultMode(){
		ArrayList<String> errors = new ArrayList<String>();
		//checkSyntaxForm(NUM_REGIONS,(Integer)WIDTH,(Integer)HEIGHT,NUM_AGENTS);
		WIDTH = Integer.parseInt(textFieldWidth.getText());
		HEIGHT = Integer.parseInt(textFieldHeight.getText());
		numRegions = Integer.parseInt(""+jComboRegions.getSelectedItem());
		numAgents = Integer.parseInt(textFieldAgents.getText());
		maxDistance = Integer.parseInt(textFieldMaxDistance.getText());
		withGui = graphicONcheckBox2.isSelected();
		
			try {
				file = new FileHandler("test_cells_"+numRegions+"_agents_"+numAgents+"_width_"+WIDTH+"_height_"+HEIGHT+".log");
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		

		(new Trigger(connection)).asynchronousReceiveToTriggerTopic(new TriggerListener(notifyArea,logger));

		if(radioButtonHorizontal.isSelected())
			MODE = DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE;
		else if(radioButtonSquare.isSelected() && !jCheckBoxLoadBalancing.isSelected())
			MODE = DSparseGrid2DFactory.SQUARE_DISTRIBUTION_MODE;
		else if(radioButtonSquare.isSelected() && jCheckBoxLoadBalancing.isSelected())
			MODE = DSparseGrid2DFactory.SQUARE_BALANCED_DISTRIBUTION_MODE;
		
		if (numRegions % root.getChildCount() != 0)
			errors.add("NUM_REGIONS < > = NUM_PEERS\n,please set Advanced mode!");
		if(errors.size() == 0){
			int div = numRegions / root.getChildCount();
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
			JOptionPane.showMessageDialog(null,"Setting completed !");

			
			master.start(numRegions, (Integer)WIDTH, (Integer)HEIGHT, numAgents,maxDistance,MODE, config,selectedSimulation,this);
		
		
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
			
				info = master.getLatestUpdate(key);
				if(info!=null)
					architectureLabel.setText("IP : "+info.getAddress()+"\n"+"OS : "+info.getoS()+"\n"+"Architecture : "+info.getArch()+"\n"+"Number of Core : "+info.getNum_core());
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
						logger.addHandler(file);
						logger.info("Number regions:"+numRegions+" Number agents:"+numAgents+" Width:"+WIDTH+" Height:"+HEIGHT);
						logger.info("Step :0 Time:"+initial_time);
					}
					else if(step == limitStep)
					{
						long fifty_time=System.currentTimeMillis();
						logger.info("Step :"+limitStep+" Time: "+fifty_time);

						long time= (fifty_time - initial_time );

						logger.info("Total Time : "+time);
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

		//controllo solo numeri non negativi
		String regex="(\\d)+|((\\d)+\\.(\\d)+)";

		numRegions = (Integer)jComboRegions.getSelectedItem();
		int numOfPeer = root.getChildCount();
		
		if(textFieldMaxDistance.getText().equals(""))
			textFieldMaxDistance.setText("0");

		boolean checkDist=true;

		while(checkDist){

			String dist=textFieldMaxDistance.getText();
			boolean validateDist=dist.matches(regex);
			if(!validateDist){	
				String	newDist=  JOptionPane.showInputDialog(null,"Insert a number","Number Format Error", 0);
				textFieldMaxDistance.setText(newDist);
			}

			else{
				checkDist=false;
				maxDistance = Integer.parseInt(textFieldMaxDistance.getText());
			}
		}

		//width
		if(textFieldWidth.getText().equals(""))
			textFieldWidth.setText("0");

		boolean checkWidth=true;
		while(checkWidth){

			String width=textFieldWidth.getText();

			boolean validateWidth=width.matches(regex) ;
			if(!validateWidth){	
				//JOptionPane.showInputDialog("Insert a number please W!");
				String	newWidth=JOptionPane.showInputDialog(null,"Insert a number","Number Format Error", 0);
				textFieldWidth.setText(newWidth);
			}

			else{
				checkWidth=false;
				WIDTH = Integer.parseInt(textFieldWidth.getText());
			}
		}

		if(textFieldHeight.getText().equals(""))
			textFieldHeight.setText("0");

		boolean checkHeight=true;

		while(checkHeight){
			String height=textFieldHeight.getText();
			boolean validateHeight=height.matches(regex) ;
			if(!validateHeight){
				String newHeight=JOptionPane.showInputDialog(null,"Insert a number","Number Format Error", 0);
				textFieldHeight.setText(newHeight);
			}
			else{
				checkHeight=false;
				HEIGHT = Integer.parseInt(textFieldHeight.getText());
			}
		}

		labelWriteReg.setText(""+numRegions);
		labelWriteNumOfPeer.setText(""+numOfPeer);
		if(numOfPeer > 0 && (numRegions % numOfPeer) == 0)			
			labelWriteRegForPeer.setText(""+numRegions/numOfPeer);
		else 
			labelWriteRegForPeer.setText("");

		if(textFieldAgents.getText().equals(""))
			textFieldAgents.setText("0");

		boolean checkAgents=true;

		while(checkAgents){
			String agents=textFieldAgents.getText();
			boolean validateAgents=agents.matches(regex);
			if(!validateAgents){
				String newAgents=JOptionPane.showInputDialog(null,"Insert a number","Number Format Error",0);
				textFieldAgents.setText(newAgents);
			}
			else{
				checkAgents=false;
				numAgents = Integer.parseInt(textFieldAgents.getText());
			}
		}

		if(radioButtonHorizontal.isSelected()){
			MODE = DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE;
			String w="";
			String h=""+HEIGHT;
			if((Integer)WIDTH % numRegions == 0)
				w = ""+(Integer)WIDTH/numRegions;
			labelWriteRegWidth.setText(""+w);
			labelWriteRegHeight.setText(""+h);
			labelWriteDistrMode.setText("HORIZONTAL MODE");
		}
		else if(radioButtonSquare.isSelected() && !jCheckBoxLoadBalancing.isSelected()){
			MODE = DSparseGrid2DFactory.SQUARE_DISTRIBUTION_MODE;
			int rad = (int) Math.sqrt(numRegions);
			String w="";
			String h="";
			if((Integer)WIDTH % rad == 0){
				w = "" + (int)((Integer)WIDTH/Math.sqrt(numRegions));
				h = "" + (int)((Integer)HEIGHT/Math.sqrt(numRegions));
			}
			labelWriteRegWidth.setText(""+w);
			labelWriteRegHeight.setText(""+h);
			labelWriteDistrMode.setText("SQUARE MODE");
		}
		else if (radioButtonSquare.isSelected() && jCheckBoxLoadBalancing.isSelected()){
			MODE = DSparseGrid2DFactory.SQUARE_BALANCED_DISTRIBUTION_MODE;
			int rad = (int) Math.sqrt(numRegions);
			String w="";
			String h="";
			if((Integer)WIDTH % rad == 0){
				w = "" + (Integer)WIDTH/numRegions;
				h = "" + (Integer)HEIGHT/numRegions;
			}
			labelWriteRegWidth.setText(""+w);
			labelWriteRegHeight.setText(""+h);
			labelWriteDistrMode.setText("SQUARE BALANCED MODE");
		}
	}

	public static void main(String[] args){
		JFrame a = new JMasterUI();
		a.setVisible(true);
	}

	private JMenuItem menuNewSim;
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
	private JCheckBox jCheckBoxLoadBalancing;
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
	private int numRegions;
	private int numAgents;
	private int maxDistance;
	private JMasterUI me = this;
	//private JFileChooser files;
	public boolean centralGui = true;
	private static final long serialVersionUID = 1L;
	private boolean withGui = false;
	private JTree tree1;
	private JTextArea architectureLabel;
	private JDesktopPane peerInfoStatus;
	private JInternalFrame internalFrame1;
	private JPanel panelConsole;

	private JMenuBar menuBar1;
	private JMenu jMenuFile;
	//private JMenuItem menuItemOpen;
	private JMenuItem menuItemExit;
	private JMenu jMenuAbout;
	private JMenuItem menuItemInfo;
	private JMenuItem menuItemHelp;
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
	private JPanel jPanelSimParams;
	private JButton buttonSetConfigDefault2;
	private JScrollPane scrollPane3;
	private JTextArea notifyArea;


	// codice profiling
	private static final Logger logger = Logger.getLogger(JMasterUI.class.getCanonicalName());
 	FileHandler file;
	private int limitStep;
	// fine codice profiling

	
	public void setConnectionSettingsEnabled(boolean enabled)
	{
		textFieldAddress.setEnabled(enabled);
		textFieldPort.setEnabled(enabled);
		//buttonRefreshServerLabel.setEnabled(enabled);
	}
	
	public void setSystemSettingsEnabled(boolean enabled)
	{
		textFieldAgents.setEnabled(enabled);
		textFieldHeight.setEnabled(enabled);
		textFieldWidth.setEnabled(enabled);
		textFieldMaxDistance.setEnabled(enabled);
		radioButtonHorizontal.setEnabled(enabled);
		radioButtonSquare.setEnabled(enabled);
		jComboRegions.setEnabled(enabled);
		jComboBoxChooseSimulation.setEnabled(enabled);
	}
}