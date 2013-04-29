package dmason.master;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import dmason.master.Master.WorkerInfoList;
import dmason.master.model.WorkersTable;

public class JMasterUI extends JFrame implements Observer
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String TAB_KEY_CLUSTER_MANAGEMENT = "CLUSTER_MANAGEMENT";
	private static final String TAB_KEY_CREATE_SIMULATION = "CREATE_SIMULATION";
	
	private HashMap<String, JPanel> tabs = new HashMap<String, JPanel>();

	private static ConnectionUI connectionFrame;
	private static Master master;
	private static JMasterUI masterUI;
	
	private JTable tblWorkers;

	public JMasterUI() {
		super();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("D-Mason Master UI");
		setupLayout();
		this.pack();
		this.setLocationRelativeTo(null);
	}
	
	private void setupLayout()
	{
		// this
		this.setLayout(new BorderLayout());
		
		// this > menuBar
		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		
		// this > menuBar > menuAbout
		JMenuItem menuAbout = new JMenuItem("About...");
		menuBar.add(menuAbout);
		
		// this > navigationTabs
		JTabbedPane navigationTabs = new JTabbedPane();
		this.add(navigationTabs, BorderLayout.CENTER);
		
		// this > navigationTabs > tabPaneClusterManagement
		JPanel tabPaneClusterManagement = new JPanel();
		navigationTabs.addTab("Cluster Management", tabPaneClusterManagement);
		this.tabs.put(JMasterUI.TAB_KEY_CLUSTER_MANAGEMENT, tabPaneClusterManagement);
		this.setupLayout_ClusterManagement(tabPaneClusterManagement);
		
		// this > navigationTabs > tabPaneCreateSimulation
		JPanel tabPaneCreateSimulation = new JPanel();
		navigationTabs.addTab("New simulation...", tabPaneCreateSimulation);
		this.tabs.put(JMasterUI.TAB_KEY_CREATE_SIMULATION, tabPaneCreateSimulation);
		this.setupLayout_CreateSimulation(tabPaneCreateSimulation);
		
		
	}

	private void setupLayout_ClusterManagement(JPanel panel)
	{
		// panel
		panel.setLayout(new BorderLayout(10,10));
		
		// panel > pnlServerControl
		JPanel pnlServerControl = new JPanel();
		pnlServerControl.setLayout(new GridBagLayout());
		pnlServerControl.setBorder(new TitledBorder("Server control"));
		panel.add(pnlServerControl, BorderLayout.NORTH);
		
		// panel > pnlServerControl > lblInfo
		JLabel lblInfo = new JLabel("Connected @ 127.0.0.1 : 61616");
		lblInfo.setAlignmentX(CENTER_ALIGNMENT);
		pnlServerControl.add(lblInfo, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 10, 5, 10), 0, 0));
		
		// panel > pnlServerControl > btnServerControlStart
		JButton btnServerControlStart = new JButton("Start");
		pnlServerControl.add(btnServerControlStart, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 10, 10, 0), 0, 0));
		
		// panel > pnlServerControl > btnServerControlRestart
		JButton btnServerControlRestart = new JButton("Restart");
		pnlServerControl.add(btnServerControlRestart, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 10, 5), 0, 0));
		
		// panel > pnlServerControl > btnServerControlStop
		JButton btnServerControlStop = new JButton("Stop");
		pnlServerControl.add(btnServerControlStop, new GridBagConstraints(2, 1, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 10, 10), 0, 0));
		
		// panel > pnlWorkers
		JPanel pnlWorkers = new JPanel();
		pnlWorkers.setLayout(new BorderLayout());
		pnlWorkers.setBorder(new TitledBorder("Workers"));
		panel.add(pnlWorkers, BorderLayout.CENTER);
		
		// panel > pnlWorkers > tblWorkers
		tblWorkers = new JTable(new WorkersTable());
		tblWorkers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tblWorkers.setCellSelectionEnabled(false);
		tblWorkers.setColumnSelectionAllowed(false);
		tblWorkers.setRowSelectionAllowed(true);
		JScrollPane sclWorkers = new JScrollPane(tblWorkers);
		pnlWorkers.add(sclWorkers, BorderLayout.CENTER);
		
		// panel > pnlWorkers > pnlWorkersControl
		JPanel pnlWorkersControl = new JPanel();
		pnlWorkersControl.setLayout(new BoxLayout(pnlWorkersControl, BoxLayout.Y_AXIS));
		pnlWorkers.add(pnlWorkersControl, BorderLayout.SOUTH);
		
		// panel > pnlWorkers > pnlWorkersControl > 1
		JPanel pnlWorkersControl_1 = new JPanel();
		pnlWorkersControl_1.setLayout(new GridLayout(1, 4, 10, 10));
		pnlWorkersControl.add(pnlWorkersControl_1);
		pnlWorkersControl.add(Box.createVerticalStrut(10));
		
		// panel > pnlWorkers > pnlWorkersControl > 2
		JPanel pnlWorkersControl_2 = new JPanel();
		pnlWorkersControl_2.setLayout(new GridLayout(1, 2, 10, 10));
		pnlWorkersControl.add(pnlWorkersControl_2);
		
		// panel > pnlWorkers > pnlWorkersControl > 1 > btnRefresh
		JButton btnRefresh = new JButton("Refresh");
		pnlWorkersControl_1.add(btnRefresh);
		btnRefresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				master.refreshWorkerList();
			}
		});
		
		// panel > pnlWorkers > pnlWorkersControl > 1 > btnSelectAll
		JButton btnSelectAll = new JButton("Select all");
		pnlWorkersControl_1.add(btnSelectAll);
		
		// panel > pnlWorkers > pnlWorkersControl > 1 > btnSelectNone
		JButton btnSelectNone = new JButton("Select none");
		pnlWorkersControl_1.add(btnSelectNone);
		
		// panel > pnlWorkers > pnlWorkersControl > 1 > btnSelectInvert
		JButton btnSelectInvert = new JButton("Select invert");
		pnlWorkersControl_1.add(btnSelectInvert);
		
		// panel > pnlWorkers > pnlWorkersControl > 2 > btnUpdate
		JButton btnDeploy = new JButton("Deploy");
		pnlWorkersControl_2.add(btnDeploy);
		btnDeploy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int[] selectedRows = tblWorkers.getSelectedRows();
				
				DeployUI deployUI = new DeployUI(master, selectedRows);
				deployUI.setVisible(true);
			}
		});
		
		// panel > pnlWorkers > pnlWorkersControl > 2 > btnRestart
		JButton btnSelectRestart = new JButton("Restart");
		pnlWorkersControl_2.add(btnSelectRestart);
	}
	
	private void setupLayout_CreateSimulation(JPanel panel)
	{
		// TODO Auto-generated method stub
		
	}

	public static void main(String[] args)
	{
		// Allows use of Java RMI
		System.setProperty("java.security.policy", ClassLoader.getSystemClassLoader().getResource("configuration/policyall.policy").toString());
		
		master = new Master();
		
		connectionFrame = new ConnectionUI();
		connectionFrame.addConnectActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (null == masterUI)
				{
					masterUI = new JMasterUI();
					master.addObserver(masterUI);	
				}
				
				boolean connected = master.connect(connectionFrame.getAddress(), connectionFrame.getPort());
				if (connected)
				{
					connectionFrame.setVisible(false);
					masterUI.setVisible(true);
				}
				else
				{
					// TODO Handle
				}
			}
		});
		connectionFrame.setVisible(true);
	}

	@Override
	public void update(Observable o, final Object arg)
	{
		if (arg instanceof WorkerInfoList)
		{
			SwingUtilities.invokeLater(new Runnable(){public void run(){
				tblWorkers.setModel(new WorkersTable((WorkerInfoList)arg));
			}});
		}
	}
	
}
