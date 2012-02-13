package dmason.util.SystemManagement;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import sim.display.Console;
import sim.util.Int2D;
import dmason.util.connection.Address;
import dmason.util.connection.ConnectionWithActiveMQAPI;
import dmason.util.connection.ConnectionWithJMS;
import dmason.util.connection.MyMessageListener;
import dmason.util.garbagecollector.Server;
import dmason.util.garbagecollector.Start;
import dmason.sim.field.grid.DSparseGrid2DFactory;

/**
 * @author Ada Mancuso
 * This is a UI for the MASTER Console.It's used to start a distributed simulations from a centralized point.
 * First we've to connect to the Server,that in our case is ActiveMQBroker,just typing ipAddress and port.
 * Then we can set some parameters like number of agents,distance(this is a particular simulation parameter),field's width and height,
 * number of regions in which we want to divide the simulation space,type of distribution,etc...
 * We can set the following parameters ('set' stay for deliver to every node in the network theirs simulation's field informations)
 * in two ways:
 * -default mode = if regions number is evenly divisible by the number of nodes connected to the network the starter automatically will distribute
 * 				   n regions for each peer,else we'll only see an error advice.
 * -customize mode = we can give different numbers of regions to each peer. In this way we can take advantage of heterogeneous networks,
 * 					 in which a node can be more powerful than another so it do really simulate better.In this configuration mode we can take 
 * 					 platform informations of each node only by double-clicking on its icon and decide how many load we've to assign to it.
 * Moreover there're in the File Menu many utilities:
 * -New : Clean the UI in order to start  anew simulation
 * -Open : we can choose the simulation.class we want to run
 * -Server : we can manage by a simple UI the server,precisely we can start,stop,restart it.
 * -Configuration : we can set some parameters as GUI,that if true-assigned provide a Display2D in order to see
 * 	the simulation flow,and as MULTITHREAD to choose if a peer have to run field's regions into an unique JavaVirtualMachine or
 * 	different JavaVirtualMachines for each field's region.
 * -Exit : dispose the UI.
 */

public class JMasterUI extends JFrame {

	public JMasterUI() {
		initComponents();
		config = new HashMap<String, Integer>();
		initializeDefaultLabel();
		setSize(855,600);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		starter = new Start();
	}

	private void connect(){
		try {
			address = new Address(textFieldAddress.getText(),textFieldPort.getText());
			master = new MasterDaemonStarter(address.getIPaddress(),address.getPort());
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
	private void initComponents() {
		jPanelConnection = new JPanel();
		jLabelAddress = new JLabel();
		jLabelPort = new JLabel();
		textFieldAddress = new JTextField();
		textFieldPort = new JTextField();
		button1 = new JButton();
		refreshServerLabel = new JLabel();
		jPanelDistribution = new JPanel();
		radioButtonHorizontal = new JRadioButton();
		radioButtonSquare = new JRadioButton();
		jLabelHori = new JLabel();
		jLabelSquare = new JLabel();
		jLabelRegions = new JLabel();
		jLabelMaxDistance = new JLabel();
		jLabelWidth = new JLabel();
		textFieldDistance = new JTextField();
		jComboRegions = new JComboBox();
		textFieldWidth = new JTextField();
		jLabelHeight2 = new JLabel();
		textFieldAgents = new JTextField();
		textFieldDimension = new JTextField();
		tabbedPane2 = new JTabbedPane();
		panel4 = new JPanel();
		label3 = new JLabel();
		labelRegionsResume = new JLabel();
		numOfPeerResume = new JLabel();
		regForPeerResume = new JLabel();
		labelWriteReg = new JLabel();
		labelWriteNumOfPeer = new JLabel();
		labelWriteRegForPeer = new JLabel();
		labelWidthRegion = new JLabel();
		labelheightRegion = new JLabel();
		labelDistrMode = new JLabel();
		labelWriteRegWidth = new JLabel();
		labelWriteRegHeight = new JLabel();
		labelWriteDistrMode = new JLabel();
		button4 = new JButton();
		panel3 = new JPanel();
		scrollPane2 = new JScrollPane();
		tree1 = new JTree();
		root = new DefaultMutableTreeNode("Simulation");
		peerInfoStatus = new JPanel();
		advancedAssCombo = new JComboBox();
		internalFrame1 = new JInternalFrame();
		label8 = new JTextArea();
		advancedConfirmBut = new JLabel();
		button3 = new JButton();
		jLabelWidth2 = new JLabel();
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
		labelStopButton = new JLabel();
		labelStopButton2 = new JLabel();
		labelPauseButton = new JLabel();
		step = new JLabel();
		labelWriteStep = new JLabel();
		panel1 = new JPanel();
		ip = textFieldAddress.getText();
		port = textFieldPort.getText();
		advancedAssCombo.setEnabled(false);
		advancedConfirmBut.setEnabled(false);
		ButtonGroup b = new ButtonGroup();
		b.add(radioButtonHorizontal);
		b.add(radioButtonSquare);
		radioButtonHorizontal.setSelected(true);
		label8.setBackground(Color.BLACK);
		label8.setForeground(Color.GREEN);
		label8.setEditable(false);
		for(int i=2;i<100;i++)
			jComboRegions.addItem(i);
		jComboRegions.setSelectedItem(2);
		jComboRegions.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if(!radioItemFalg)
					initializeDefaultLabel();
			}
		});
		
		button1.addActionListener(new ActionListener() {
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
					radioItemFalg = true;
					jComboRegions.removeAllItems();
					for(int i=2;i<=100;i++)
						jComboRegions.addItem(i);
					initializeDefaultLabel();
					radioItemFalg = false;
				}
			});
			
			radioButtonSquare.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					radioItemFalg = true;
						jComboRegions.removeAllItems();
					for(int i=2;i<=10;i++)
						jComboRegions.addItem(i*i);
					initializeDefaultLabel();
					radioItemFalg = false;
				}
			});
			
			button3.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					submitCustomizeMode();
				}
			});
			
			button4.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					submitDefautMode();
				}
			});
			
			advancedConfirmBut.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent arg0) {
					confirm();
					res -= (Integer)advancedAssCombo.getSelectedItem();
					advancedAssCombo.removeAllItems();
					for(int i=1;i<=res;i++)
						advancedAssCombo.addItem(i);
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

		//======== jPanelConnection ========
		{
			jPanelConnection.setBorder(new TitledBorder("Connection"));
			jPanelConnection.setPreferredSize(new Dimension(215, 125));

			//---- jLabelAddress ----
			jLabelAddress.setText("IP Address :");

			//---- jLabelPort ----
			jLabelPort.setText("Port :");

			//---- textFieldAddress ----
			textFieldAddress.setText("127.0.0.1");

			//---- textFieldPort ----
			textFieldPort.setText("61616");

			//---- button1 ----
			button1.setText("OK");

			//---- refreshServerLabel ----
			refreshServerLabel.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/refresh.png")));
			

			GroupLayout jPanelConnectionLayout = new GroupLayout(jPanelConnection);
			jPanelConnection.setLayout(jPanelConnectionLayout);
			jPanelConnectionLayout.setHorizontalGroup(
				jPanelConnectionLayout.createParallelGroup()
					.addGroup(GroupLayout.Alignment.TRAILING, jPanelConnectionLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(jLabelAddress)
						.addGap(28, 28, 28)
						.addComponent(textFieldAddress, GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
						.addGap(71, 71, 71)
						.addComponent(jLabelPort)
						.addGap(35, 35, 35)
						.addComponent(textFieldPort, GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
						.addGap(142, 142, 142)
						.addComponent(refreshServerLabel)
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(button1)
						.addContainerGap())
			);
			jPanelConnectionLayout.setVerticalGroup(
				jPanelConnectionLayout.createParallelGroup()
					.addGroup(jPanelConnectionLayout.createSequentialGroup()
						.addGroup(jPanelConnectionLayout.createParallelGroup()
							.addGroup(jPanelConnectionLayout.createSequentialGroup()
								.addContainerGap()
								.addGroup(jPanelConnectionLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(jLabelAddress)
									.addComponent(textFieldAddress, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
									.addComponent(jLabelPort)
									.addComponent(textFieldPort, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)))
							.addGroup(jPanelConnectionLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(button1)
								.addComponent(refreshServerLabel)))
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
			);
		}

		//======== jPanelDistribution ========
		{
			jPanelDistribution.setBorder(new TitledBorder("Settings"));

			//---- radioButtonHorizontal ----
			radioButtonHorizontal.setText("HORIZONTAL");

			//---- radioButtonSquare ----
			radioButtonSquare.setText("SQUARE");

			//---- jLabelHori ----
			jLabelHori.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/hori.png")));


			//---- jLabelSquare ----
			jLabelSquare.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/square.png")));
			

			//---- jLabelRegions ----
			jLabelRegions.setText("REGIONS :");

			//---- jLabelMaxDistance ----
			jLabelMaxDistance.setText("MAX_DISTANCE :");

			//---- jLabelWidth ----
			jLabelWidth.setText("WIDTH :");

			//---- textFieldDistance ----
			textFieldDistance.setText("1");

			//---- textFieldWidth ----
			textFieldWidth.setText("200");

			//---- jLabelHeight2 ----
			jLabelHeight2.setText("AGENTS :");

			//---- textFieldAgents ----
			textFieldAgents.setText("15");

			//---- textFieldDimension ----
			textFieldDimension.setText("200");
			
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
			
			textFieldDimension.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent arg0) {}
				
				@Override
				public void keyReleased(KeyEvent arg0) {
					initializeDefaultLabel();
				}
				
				@Override
				public void keyPressed(KeyEvent arg0) {}
			});
			
			

			//======== tabbedPane2 ========
			{

				//======== panel4 ========
				{
					panel4.setBorder(new EtchedBorder());
					panel4.setPreferredSize(new Dimension(350, 331));

					//---- label3 ----
					label3.setText("Simulation Configuration Settings");
					label3.setFont(label3.getFont().deriveFont(label3.getFont().getStyle() | Font.BOLD, label3.getFont().getSize() + 8f));

					//---- labelRegionsResume ----
					labelRegionsResume.setText("REGIONS :");

					//---- numOfPeerResume ----
					numOfPeerResume.setText("NUMBER OF PEERS :");

					//---- regForPeerResume ----
					regForPeerResume.setText("REGIONS FOR PEER :");

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

					//---- button4 ----
					button4.setText("Set");

					GroupLayout panel4Layout = new GroupLayout(panel4);
					panel4.setLayout(panel4Layout);
					panel4Layout.setHorizontalGroup(
						panel4Layout.createParallelGroup()
							.addGroup(GroupLayout.Alignment.TRAILING, panel4Layout.createSequentialGroup()
								.addContainerGap()
								.addGroup(panel4Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
									.addComponent(button4)
									.addGroup(panel4Layout.createSequentialGroup()
										.addGroup(panel4Layout.createParallelGroup()
											.addComponent(label3, GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
											.addGroup(panel4Layout.createSequentialGroup()
												.addGap(6, 6, 6)
												.addGroup(panel4Layout.createParallelGroup()
													.addComponent(numOfPeerResume)
													.addComponent(regForPeerResume)
													.addComponent(labelWidthRegion)
													.addComponent(labelheightRegion)
													.addComponent(labelDistrMode)
													.addComponent(labelRegionsResume))))
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
										.addGroup(panel4Layout.createParallelGroup()
											.addComponent(labelWriteNumOfPeer, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
											.addComponent(labelWriteReg)
											.addComponent(labelWriteRegForPeer)
											.addComponent(labelWriteRegWidth)
											.addComponent(labelWriteRegHeight)
											.addComponent(labelWriteDistrMode))))
								.addGap(92, 92, 92))
					);
					panel4Layout.setVerticalGroup(
						panel4Layout.createParallelGroup()
							.addGroup(panel4Layout.createSequentialGroup()
								.addContainerGap()
								.addComponent(label3, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
								.addGap(28, 28, 28)
								.addGroup(panel4Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(labelRegionsResume)
									.addComponent(labelWriteReg))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panel4Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(numOfPeerResume)
									.addComponent(labelWriteNumOfPeer))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panel4Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(regForPeerResume)
									.addComponent(labelWriteRegForPeer))
								.addGap(6, 6, 6)
								.addGroup(panel4Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(labelWidthRegion)
									.addComponent(labelWriteRegWidth))
								.addGap(6, 6, 6)
								.addGroup(panel4Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(labelheightRegion)
									.addComponent(labelWriteRegHeight))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panel4Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(labelDistrMode)
									.addComponent(labelWriteDistrMode, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 87, Short.MAX_VALUE)
								.addComponent(button4)
								.addGap(27, 27, 27))
					);
				}
				tabbedPane2.addTab("Default", panel4);


				//======== panel3 ========
				{
					panel3.setBorder(new EtchedBorder());

					//======== scrollPane2 ========
					{

						//---- tree1 ----
						tree1.setModel(new DefaultTreeModel(root));
						DefaultTreeCellRenderer render = new DefaultTreeCellRenderer();
						render.setOpenIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/network.png")));
								
						render.setLeafIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/computer.gif")));
						
						render.setClosedIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/network.png")));

						tree1.setCellRenderer(render);
						tree1.setRowHeight(25);
						scrollPane2.setViewportView(tree1);
						tree1.addTreeSelectionListener(new TreeSelectionListener() {
							
							@Override
							public void valueChanged(TreeSelectionEvent arg0) {
								if(arg0.getPath().getLastPathComponent().equals(root)){
									advancedAssCombo.setEnabled(true);
									advancedConfirmBut.setEnabled(true);
									total = (Integer)jComboRegions.getSelectedItem();
									res = total;
									advancedAssCombo.removeAllItems();
									for(int i=1;i<res;i++)
										advancedAssCombo.addItem(i);
								}
								else
									clickTreeListener();
							}
						});
					}

					//======== peerInfoStatus ========
					{
						peerInfoStatus.setBorder(new TitledBorder("Data"));

						//======== internalFrame1 ========
						{
							internalFrame1.setVisible(true);
							Container internalFrame1ContentPane = internalFrame1.getContentPane();

							//---- label8 ----
							label8.setText("");

							GroupLayout internalFrame1ContentPaneLayout = new GroupLayout(internalFrame1ContentPane);
							internalFrame1ContentPane.setLayout(internalFrame1ContentPaneLayout);
							internalFrame1ContentPaneLayout.setHorizontalGroup(
								internalFrame1ContentPaneLayout.createParallelGroup()
									.addGroup(internalFrame1ContentPaneLayout.createSequentialGroup()
										.addContainerGap()
										.addComponent(label8, GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
										.addContainerGap())
							);
							internalFrame1ContentPaneLayout.setVerticalGroup(
								internalFrame1ContentPaneLayout.createParallelGroup()
									.addGroup(internalFrame1ContentPaneLayout.createSequentialGroup()
										.addContainerGap()
										.addComponent(label8, GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
										.addContainerGap())
							);
						}

						//---- advancedConfirmBut ----
						advancedConfirmBut.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/ok.png")));


						GroupLayout peerInfoStatusLayout = new GroupLayout(peerInfoStatus);
						peerInfoStatus.setLayout(peerInfoStatusLayout);
						peerInfoStatusLayout.setHorizontalGroup(
							peerInfoStatusLayout.createParallelGroup()
								.addGroup(peerInfoStatusLayout.createSequentialGroup()
									.addContainerGap()
									.addComponent(internalFrame1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addContainerGap())
								.addGroup(GroupLayout.Alignment.TRAILING, peerInfoStatusLayout.createSequentialGroup()
									.addContainerGap(93, Short.MAX_VALUE)
									.addComponent(advancedAssCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addGap(18, 18, 18)
									.addComponent(advancedConfirmBut, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
									.addGap(25, 25, 25))
						);
						peerInfoStatusLayout.setVerticalGroup(
							peerInfoStatusLayout.createParallelGroup()
								.addGroup(peerInfoStatusLayout.createSequentialGroup()
									.addComponent(internalFrame1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addGap(8, 8, 8)
									.addGroup(peerInfoStatusLayout.createParallelGroup()
										.addComponent(advancedConfirmBut)
										.addComponent(advancedAssCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
									.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						);
					}

					//---- button3 ----
					button3.setText("Set");
					GroupLayout panel3Layout = new GroupLayout(panel3);
					panel3.setLayout(panel3Layout);
					panel3Layout.setHorizontalGroup(
						panel3Layout.createParallelGroup()
							.addGroup(panel3Layout.createSequentialGroup()
								.addContainerGap()
								.addComponent(scrollPane2, GroupLayout.PREFERRED_SIZE, 217, GroupLayout.PREFERRED_SIZE)
								.addGap(12, 12, 12)
								.addComponent(peerInfoStatus, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addContainerGap())
							.addGroup(GroupLayout.Alignment.TRAILING, panel3Layout.createSequentialGroup()
								.addContainerGap(440, Short.MAX_VALUE)
								.addComponent(button3)
								.addContainerGap())
					);
					panel3Layout.setVerticalGroup(
						panel3Layout.createParallelGroup()
							.addGroup(panel3Layout.createSequentialGroup()
								.addGroup(panel3Layout.createParallelGroup()
									.addComponent(peerInfoStatus, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addGroup(panel3Layout.createSequentialGroup()
										.addGap(6, 6, 6)
										.addComponent(scrollPane2, GroupLayout.PREFERRED_SIZE, 267, GroupLayout.PREFERRED_SIZE)))
								.addGap(18, 18, 18)
								.addComponent(button3)
								.addContainerGap(13, Short.MAX_VALUE))
					);
				}
				tabbedPane2.addTab("Advanced", panel3);

			}

			//---- jLabelWidth2 ----
			jLabelWidth2.setText("HEIGHT :");

			GroupLayout jPanelDistributionLayout = new GroupLayout(jPanelDistribution);
			jPanelDistribution.setLayout(jPanelDistributionLayout);
			jPanelDistributionLayout.setHorizontalGroup(
				jPanelDistributionLayout.createParallelGroup()
					.addGroup(jPanelDistributionLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(jPanelDistributionLayout.createParallelGroup()
							.addGroup(jPanelDistributionLayout.createSequentialGroup()
								.addGroup(jPanelDistributionLayout.createParallelGroup()
									.addComponent(radioButtonHorizontal)
									.addComponent(radioButtonSquare)
									.addComponent(jLabelRegions)
									.addComponent(jLabelMaxDistance)
									.addComponent(jLabelWidth)
									.addComponent(jLabelWidth2))
								.addGap(28, 28, 28)
								.addGroup(jPanelDistributionLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
									.addGroup(jPanelDistributionLayout.createParallelGroup()
										.addComponent(jLabelHori)
										.addComponent(jLabelSquare)
										.addGroup(jPanelDistributionLayout.createSequentialGroup()
											.addGap(6, 6, 6)
											.addGroup(jPanelDistributionLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
												.addComponent(textFieldAgents, GroupLayout.Alignment.LEADING)
												.addComponent(textFieldDimension, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
												.addComponent(textFieldDistance, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
												.addComponent(textFieldWidth, GroupLayout.Alignment.LEADING))))
									.addComponent(jComboRegions, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
							.addComponent(jLabelHeight2))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
						.addComponent(tabbedPane2, GroupLayout.PREFERRED_SIZE, 546, GroupLayout.PREFERRED_SIZE)
						.addContainerGap())
			);
			jPanelDistributionLayout.setVerticalGroup(
				jPanelDistributionLayout.createParallelGroup()
					.addGroup(jPanelDistributionLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(jPanelDistributionLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(radioButtonHorizontal)
							.addComponent(jLabelHori))
						.addGap(18, 18, 18)
						.addGroup(jPanelDistributionLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(radioButtonSquare)
							.addComponent(jLabelSquare))
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(jPanelDistributionLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(jLabelRegions)
							.addComponent(jComboRegions, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanelDistributionLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(jLabelMaxDistance)
							.addComponent(textFieldDistance, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
						.addGap(6, 6, 6)
						.addGroup(jPanelDistributionLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addGroup(jPanelDistributionLayout.createSequentialGroup()
								.addComponent(jLabelWidth, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanelDistributionLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(jLabelWidth2, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
									.addComponent(textFieldDimension, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)))
							.addComponent(textFieldWidth, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanelDistributionLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(jLabelHeight2)
							.addComponent(textFieldAgents, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)))
					.addComponent(tabbedPane2, GroupLayout.PREFERRED_SIZE, 384, GroupLayout.PREFERRED_SIZE)
			);
		}

		//======== menuBar1 ========
		{

			//======== jMenuFile ========
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

		//---- labelStopButton ----
		labelStopButton.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/NotPlaying.png")));
		labelStopButton.addMouseListener(new MouseListener() {
			
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
				labelStopButton.setIcon(new ImageIcon("dmason/resource/image/Playing.png"));
				try {
					master.play();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		//---- labelStopButton2 ----	
		labelStopButton2.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/NotStopped.png")));
		labelStopButton2.addMouseListener(new MouseListener() {
			
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
				labelStopButton2.setIcon(new ImageIcon("dmason/resource/image/Stopped.png"));
				try {
					master.stop();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		//---- labelPauseButton ----
		labelPauseButton.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/PauseOff.png")));
		labelPauseButton.addMouseListener(new MouseListener() {
			
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
				labelPauseButton.setIcon(new ImageIcon("dmason/resource/image/PauseOn.png"));
				try {
					master.pause();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		
		//======== panel1 ========
		{

			//---- writeStepLabel ----
			labelWriteStep.setText("txt");

			GroupLayout panel1Layout = new GroupLayout(panel1);
			panel1.setLayout(panel1Layout);
			panel1Layout.setHorizontalGroup(
				panel1Layout.createParallelGroup()
					.addGroup(GroupLayout.Alignment.TRAILING, panel1Layout.createSequentialGroup()
						.addContainerGap(29, Short.MAX_VALUE)
						.addComponent(labelWriteStep))
			);
			panel1Layout.setVerticalGroup(
				panel1Layout.createParallelGroup()
					.addGroup(panel1Layout.createSequentialGroup()
						.addComponent(labelWriteStep)
						.addContainerGap(12, Short.MAX_VALUE))
			);
		}
		step.setText("Step : ");
		labelWriteStep.setText("");


		GroupLayout contentPaneLayout = new GroupLayout(contentPane);
		contentPane.setLayout(contentPaneLayout);
		contentPaneLayout.setHorizontalGroup(
			contentPaneLayout.createParallelGroup()
				.addComponent(menuBar1, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 883, Short.MAX_VALUE)
				.addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
					.addContainerGap(34, Short.MAX_VALUE)
					.addComponent(jPanelDistribution, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(20, 20, 20))
				.addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
					.addContainerGap(637, Short.MAX_VALUE)
					.addComponent(step)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addComponent(panel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(34, 34, 34)
					.addComponent(labelStopButton)
					.addGap(12, 12, 12)
					.addComponent(labelPauseButton)
					.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
					.addComponent(labelStopButton2)
					.addGap(52, 52, 52))
				.addGroup(contentPaneLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(jPanelConnection, GroupLayout.PREFERRED_SIZE, 825, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(52, Short.MAX_VALUE))
		);
		contentPaneLayout.setVerticalGroup(
			contentPaneLayout.createParallelGroup()
				.addGroup(contentPaneLayout.createSequentialGroup()
					.addComponent(menuBar1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
					.addComponent(jPanelConnection, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addComponent(jPanelDistribution, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(18, 18, 18)
					.addGroup(contentPaneLayout.createParallelGroup()
						.addComponent(labelStopButton, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
						.addComponent(panel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(step)
						.addComponent(labelPauseButton, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
						.addComponent(labelStopButton2, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE))
					.addGap(35, 35, 35))
		);
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	private ArrayList<String> checkSyntaxForm(int num,int width,int height,int numAgentsForPeer){
		
		ArrayList<String> errors = new ArrayList<String>();
		System.out.println("Num_regions = "+NUM_REGIONS);
		System.out.println("total = "+total);
		System.out.println("Peers = "+root.getChildCount());
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
		System.out.println(total);
		ArrayList<String> errors = null;
		WIDTH = Integer.parseInt(textFieldWidth.getText());
		HEIGHT = Integer.parseInt(textFieldDimension.getText());
		NUM_REGIONS = Integer.parseInt(""+jComboRegions.getSelectedItem());
		NUM_AGENTS = Integer.parseInt(textFieldAgents.getText());
		if(radioButtonHorizontal.isSelected())
			MODE = DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE;
		else
			MODE = DSparseGrid2DFactory.SQUARE_DISTRIBUTION_MODE;
		errors = checkSyntaxForm(NUM_REGIONS,(Integer)WIDTH,(Integer)HEIGHT,NUM_AGENTS);
		// Wrong data inserted
		if(errors.size() > 0){
			String x="ERRORS"+"\n";
			for(String e : errors)
				x=x+e+"\n";
			JOptionPane.showMessageDialog(null,x);
		}
		getSteps();
		//sim = files.getSelectedFile().getName();
		master.start(NUM_REGIONS, (Integer)WIDTH, (Integer)HEIGHT, NUM_AGENTS,MODE, config);
	}
	
	private void submitDefautMode(){
		ArrayList<String> errors = new ArrayList<String>();
		//checkSyntaxForm(NUM_REGIONS,(Integer)WIDTH,(Integer)HEIGHT,NUM_AGENTS);
		WIDTH = Integer.parseInt(textFieldWidth.getText());
		HEIGHT = Integer.parseInt(textFieldDimension.getText());
		NUM_REGIONS = Integer.parseInt(""+jComboRegions.getSelectedItem());
		NUM_AGENTS = Integer.parseInt(textFieldAgents.getText());
		if(radioButtonHorizontal.isSelected())
			MODE = DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE;
		else
			MODE = DSparseGrid2DFactory.SQUARE_DISTRIBUTION_MODE;
			if (NUM_REGIONS % root.getChildCount() != 0)
				errors.add("NUM_REGIONS < > = NUM_PEERS\n,please set Advanced mode!");
			if(errors.size() == 0){
				int div = NUM_REGIONS / root.getChildCount();
				try{
						for(String topic : master.getTopicList())
							config.put(topic, div);
						getSteps();
				}catch (Exception e) {
					e.printStackTrace();
				}
				master.start(NUM_REGIONS, (Integer)WIDTH, (Integer)HEIGHT, NUM_AGENTS,MODE, config);
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
		PeerStatusInfo info=null;
		if(connected && dont){
			key = tree1.getLastSelectedPathComponent().toString();
			if(key.startsWith("SERVICE")){
				info = master.getLatestUpdate(key);
				if(info!=null)
					label8.setText("IP : "+info.getAddress()+"\n"+"OS : "+info.getoS()+"\n"+"Architecture : "+info.getArch()+"\n"+"Number of Core : "+info.getNum_core());
			}
		}
	}
	
	private void confirm(){
		String key = tree1.getLastSelectedPathComponent().toString();
		int reg = (Integer)advancedAssCombo.getSelectedItem();
		config.put(key, reg);
	}
	
	private void getSteps(){
		connection = new ConnectionWithActiveMQAPI(new MyMessageListener() {
			long initial_time=-1;
			PrintWriter printer;
			@Override
			public void onMessage(Message arg0) {
				Object o=null;
				try {
					o = parseMessage(arg0);
				} catch (JMSException e) {
					e.printStackTrace();
				}
				Long step=(Long)o;
				
				
				if(step==0)
				{
					try {
						printer=new PrintWriter(new FileOutputStream("test_cells_"+NUM_REGIONS+"_agents_"+NUM_AGENTS+"_width_"+WIDTH+"_height_"+HEIGHT+".txt"));
						initial_time=System.currentTimeMillis();
						printer.println("Number regions:"+NUM_REGIONS+" Number agents:"+NUM_AGENTS+" Width:"+WIDTH+" Height:"+HEIGHT);
						printer.println("Step :0 Time:"+initial_time);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if(step == 100)
				{
					long fifty_time=System.currentTimeMillis();
					printer.println("Step :100 Time: "+fifty_time);
					
					double time= (fifty_time - initial_time ) / 1000.0;
					
					
					
					printer.println("Total Time : "+time);
					printer.close();
				}
				labelWriteStep.setText(""+(String)o.toString());
				}
		});
		try{
			connection.setupConnection(address);
			connection.createTopic("step");
			connection.subscribeToTopic("step");
			connection.asynchronousReceive("step");
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
		if(textFieldDimension.getText().equals(""))
			textFieldDimension.setText("0");
		HEIGHT = Integer.parseInt(textFieldDimension.getText());
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
	private JPanel jPanelConnection;
	private JLabel jLabelAddress;
	private JLabel jLabelPort;
	private JTextField textFieldAddress;
	private JTextField textFieldPort;
	private JButton button1;
	private JLabel refreshServerLabel;
	private JPanel jPanelDistribution;
	private JRadioButton radioButtonHorizontal;
	private JRadioButton radioButtonSquare;
	private JLabel jLabelHori;
	private JLabel jLabelSquare;
	private JLabel jLabelRegions;
	private JLabel jLabelMaxDistance;
	private JLabel jLabelWidth;
	private JTextField textFieldDistance;
	private JComboBox jComboRegions;
	private JTextField textFieldWidth;
	private JLabel jLabelHeight2;
	private JTextField textFieldAgents;
	private JTextField textFieldDimension;
	private JTabbedPane tabbedPane2;
	private JPanel panel4;
	private JLabel label3;
	private JLabel labelRegionsResume;
	private JLabel numOfPeerResume;
	private JLabel regForPeerResume;
	private JLabel labelWriteReg;
	private JLabel labelWriteNumOfPeer;
	private JLabel labelWriteRegForPeer;
	private JLabel labelWidthRegion;
	private JLabel labelheightRegion;
	private JLabel labelDistrMode;
	private JLabel labelWriteRegWidth;
	private JLabel labelWriteRegHeight;
	private JLabel labelWriteDistrMode;
	private JButton button4;
	private JPanel panel3;
	private JScrollPane scrollPane2;
	private JTree tree1;
	private JPanel peerInfoStatus;
	private JComboBox advancedAssCombo;
	private JInternalFrame internalFrame1;
	private JTextArea label8;
	private JLabel advancedConfirmBut;
	private JButton button3;
	private JLabel jLabelWidth2;
	private JMenuBar menuBar1;
	private JMenu jMenuFile;
	private JMenuItem menuItemOpen;
	private JMenuItem menuItemExit;
	private JMenuItem menuItemServer;
	private JMenuItem menuNewSim;
	private JMenuItem menuConfSim;
	private JMenu jMenuAbout;
	private JMenuItem menuItemInfo;
	private JMenuItem menuItenHelp;
	private JLabel labelStopButton;
	private JLabel labelStopButton2;
	private JLabel labelPauseButton;
	private JLabel step;
	private JLabel labelWriteStep;
	private JPanel panel1;
	private MasterDaemonStarter master;
	private String ip;
	private String port;
	private DefaultMutableTreeNode root;
	private boolean connected = false;
	private HashMap<String,Integer> config;
	private int total=0;
	boolean radioItemFalg=false;
	private Address address;
	int res;
	private ConnectionWithJMS connection;
	private Start starter;
	private Console c;
	private boolean dont=true;
	private int MODE;
	private Object WIDTH;
	private Object HEIGHT;
	private int NUM_REGIONS;
	private int NUM_AGENTS;
	private JMasterUI me = this;
	private String sim;
	private JFileChooser files;
	private boolean gui;
	public boolean centralGui = true;
	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args){
		JFrame a = new JMasterUI();
		a.setVisible(true);
	}
}
