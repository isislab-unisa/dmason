/**
 * Copyright 2012 Universita' degli Studi di Salerno


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package it.isislab.dmason.experimentals.tools.launcher.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import it.isislab.dmason.experimentals.tools.launcher.Launcher;

/**
 * 
 * @author Luca Vicidomini
 *
 */
public class Wizard extends JFrame
{
	private static final long serialVersionUID = 1L;
	private static final int MAX_STEP = 6;
	
	// Variables
	private Launcher launcher;
	private int step;
	private boolean launchedWrapper;
	private boolean launchedWorkers;
	private boolean launchedMaster;
	private boolean launchedInspector;
	
	// Interface elements
	private ImageIcon icoStatusNo;
	private ImageIcon icoStatusOk;
	private JPanel pnlCards;
	private JButton btnNext;
	private JButton btnPrev;
	private JButton btnWrapper;
	private JSlider sldNumWorkers;
	private JButton btnWorkers;
	private JButton btnMaster;
	private JButton btnInspector;
	private JLabel lblStatusWrapper;
	private JLabel lblStatusWorkers;
	private JLabel lblStatusMaster;
	private JLabel lblStatusInspector;
	
	public Wizard(Launcher launcher)
	{
		super();
		
		icoStatusNo = new ImageIcon("resources/image/status-unknow.png");
		icoStatusOk = new ImageIcon("resources/image/status-up.png");
		
		// Initialize variables
		this.launcher = launcher;
		step = 1;
		launchedWrapper = false;
		launchedWorkers = false;
		launchedMaster = false;
		launchedInspector = false;
	
		/* Window header */
		JLabel title = new JLabel("D-Mason wizard");
			title.setFont(new Font(title.getFont().getName(), Font.BOLD, title.getFont().getSize() * 15 / 10 ));
			
		JPanel header = new JPanel();
			header.setLayout(new FlowLayout(FlowLayout.LEFT));
			header.add(title);
			
		/* Window left column */
		lblStatusWrapper = new JLabel("ActiveMQ Server", icoStatusNo, SwingConstants.LEADING);
		lblStatusWorkers = new JLabel("Workers", icoStatusNo, SwingConstants.LEADING);
		lblStatusMaster = new JLabel("Master", icoStatusNo, SwingConstants.LEADING);
		lblStatusInspector = new JLabel("Inspector", icoStatusNo, SwingConstants.LEADING);
		
		JPanel leftColumn = new JPanel();
			leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));
			leftColumn.add(lblStatusWrapper);
			leftColumn.add(Box.createRigidArea(new Dimension(0,5)));
			leftColumn.add(lblStatusWorkers);
			leftColumn.add(Box.createRigidArea(new Dimension(0,5)));
			leftColumn.add(lblStatusMaster);
			leftColumn.add(Box.createRigidArea(new Dimension(0,5)));
			leftColumn.add(lblStatusInspector);
		
		/* Window footer */
		btnNext = new JButton("Next  >");
			btnNext.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) {	nextStep();	}});
		
		btnPrev = new JButton("<  Prev");
			btnPrev.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) {	prevStep();	}});
		
		JPanel pnlStepButtons = new JPanel();
			pnlStepButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
			pnlStepButtons.add(btnPrev);
			pnlStepButtons.add(btnNext);
			
		JPanel footer = new JPanel();
			footer.setLayout(new BorderLayout());
			footer.add(pnlStepButtons, BorderLayout.EAST);
			
		/* Window body */
		pnlCards = new JPanel();
		pnlCards.setLayout(new CardLayout());
			pnlCards.add("STEP_1", step1_setup());
			pnlCards.add("STEP_2", step2_setup());
			pnlCards.add("STEP_3", step3_setup());
			pnlCards.add("STEP_4", step4_setup());
			pnlCards.add("STEP_5", step5_setup());
			pnlCards.add("STEP_6", step6_setup());
				
		/* JFrame settings */
		this.setLayout(new BorderLayout(10, 10));
			this.add(header, BorderLayout.NORTH);
			this.add(pnlCards, BorderLayout.CENTER);
			this.add(footer, BorderLayout.SOUTH);
			this.add(leftColumn, BorderLayout.WEST);
			
		this.setTitle("D-Mason wizard");
		this.setMinimumSize(new Dimension(400, 300));
		this.setPreferredSize(new Dimension(600, 400));
		this.pack();
		this.setLocationRelativeTo(null); // Center (JDK >= 1.4)
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e)
			{
				String message = "Do you want to shut down all of D-Mason's components?\n"
					+"\n"
					+"If you choose Yes, D-Mason will be shut down completely;\n"
					+"If you choose No, this wizard will close but D-Mason will\n"
					+"continue running; if you you choose Cancel, no action will\n"
					+"be taken.\n\n";
				/* 
				 * Show the message only if at least one D-Mason's component has been
				 * launched. Wrapper won't be considered, it will be shut down anyway. 
				 */
				int answer = Wizard.this.launchedWorkers || Wizard.this.launchedMaster || Wizard.this.launchedInspector 
					? JOptionPane.showConfirmDialog(Wizard.this, message, "Exit", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)
					: JOptionPane.NO_OPTION;
				if (answer == JOptionPane.YES_OPTION)
				{
					Wizard.this.launcher.stopAllProcesses();
				}
				if (answer != JOptionPane.CANCEL_OPTION)
				{
					Wizard.this.setVisible(false);
					System.exit(0);
				}
			}
		});
		
		// Display first step
		showStep(step);
	}
	
	private void showStep(int step)
	{
		this.step = step;
		((CardLayout)pnlCards.getLayout()).show(pnlCards, "STEP_" + step);
		btnPrev.setEnabled(step != 1);
		btnNext.setEnabled(step != MAX_STEP);
		
		switch (step)
		{
		case 1:
			step1_go();
			break;
		case 2:
			step2_go();
			break;
		case 3:
			step3_go();
			break;
		case 4:
			step4_go();
			break;
		case 5:
			step5_go();
			break;
		case 6:
			step6_go();
			break;
		}
	}
	
	public void nextStep()
	{
		showStep(step + 1);
	}
	
	public void prevStep()
	{
		showStep(step - 1);
	}
	
	/**
	 * Introduction
	 * @return
	 */
	protected JPanel step1_setup()
	{
		JTextArea introText = new JTextArea();
			introText.setLineWrap(true);
			introText.setWrapStyleWord(true);
			introText.setEditable(false);
			introText.setMargin(new Insets(10, 10, 10, 10));
			introText.setAutoscrolls(true);
			introText.setText(
				"Welcome to D-Mason.\n"
				+ "\n"
				+ "This wizard will help you launching D-Mason on a single "
				+ "machine for testing purposes. D-Mason is made of four "
				+ "\"components\" that must be executed in order to run a "
				+ "simulation.\n"
				+ "\n"
				+ "Please hit the \"Next\" button below to continue."
			);
			
		JScrollPane sclIntroText = new JScrollPane(introText);
		
		JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.add(sclIntroText);
		
		return panel;
	}
	
	/**
	 * Wrapper
	 * @return
	 */
	protected JPanel step2_setup()
	{
		JTextArea introText = new JTextArea();
			introText.setLineWrap(true);
			introText.setWrapStyleWord(true);
			introText.setEditable(false);
			introText.setMargin(new Insets(10, 10, 10, 10));
			introText.setAutoscrolls(true);
			introText.setText(
				"The first component to launch is the communication server.\n"
				+ "\n"
				+ "D-Mason uses Apache ActiveMQ as messaging service server. "
				+ "ActiveMQ is embodied in a special D-Mason's component, "
				+ "called \"Wrapper\" that allows starting, stopping and "
				+ "resetting the server using the master console (another "
				+ "D-Mason component that we will launch later).\n"
				+ "\n"
				+ "Click the button below to start the Wrapper and go to "
				+ "the next step."
			);
			
		JScrollPane sclIntroText = new JScrollPane(introText);
			
		btnWrapper = new JButton("Launch the Wrapper");
			btnWrapper.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) {	launchWrapper(); }});
		
		JPanel pnlButtons = new JPanel();
			pnlButtons.setLayout(new FlowLayout(FlowLayout.LEFT));
			pnlButtons.add(btnWrapper);
		
		JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout(10, 10));
			panel.add(sclIntroText, BorderLayout.CENTER);
			panel.add(pnlButtons, BorderLayout.SOUTH);
	
		return panel;
	}
	
	/**
	 * Workers
	 * @return
	 */
	protected JPanel step3_setup()
	{	
		JTextArea introText = new JTextArea();
			introText.setLineWrap(true);
			introText.setWrapStyleWord(true);
			introText.setEditable(false);
			introText.setMargin(new Insets(10, 10, 10, 10));
			introText.setAutoscrolls(true);
			introText.setText(
				"Next, we need to launch one or more workers.\n"
				+ "\n"
				+ "Each worker will be in charge of simulating part of the "
				+ "simulation space.\n"
				+ "\n"
				+ "D-Mason detected " + launcher.getNumCores() + " processors "
				+ "on this machine. The suggested number of workers is "
				+ launcher.getSuggestedWorkers() + ", however you can change "
				+ "the number of workers from the slider below.\n"
				+ "\n"
				+ "Every worker will be launched in a new Java Virtual "
				+ "Machine and may consume an high percentage of memory.\n"
				+ "\n" 
				+ "Once you chose how many workers you want to start, "
				+ "worker(s) will automatically connect to "
				+ "the server and display a confirmation message."
			);
		
		JScrollPane sclIntroText = new JScrollPane(introText);
			
		btnWorkers = new JButton("Launch workers");
			btnWorkers.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) {	launchWorkers(); }});
			
		sldNumWorkers = new JSlider(launcher.getMinWorkers(), launcher.getMaxWorkers(), launcher.getSuggestedWorkers());
			sldNumWorkers.setMinorTickSpacing(1);
			sldNumWorkers.setMajorTickSpacing(1);
			sldNumWorkers.setPaintLabels(true);
			sldNumWorkers.setPaintTicks(true);
			sldNumWorkers.setSnapToTicks(true);
		
		JPanel pnlButtons = new JPanel();
			pnlButtons.setLayout(new FlowLayout(FlowLayout.LEFT));
			pnlButtons.add(sldNumWorkers);
			pnlButtons.add(new JSeparator(SwingConstants.VERTICAL));
			pnlButtons.add(btnWorkers);
		
		JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout(10, 10));
			panel.add(sclIntroText, BorderLayout.CENTER);
			panel.add(pnlButtons, BorderLayout.SOUTH);
	
		return panel;
	}
	
	/**
	 * Master
	 * @return
	 */
	protected JPanel step4_setup()
	{
		JTextArea introText = new JTextArea();
		introText.setLineWrap(true);
		introText.setWrapStyleWord(true);
		introText.setEditable(false);
		introText.setMargin(new Insets(10, 10, 10, 10));
		introText.setAutoscrolls(true);
		introText.setText(
			"Almost done! It's time to launch the Master console, that\n"
			+ "will allow you to select a simulation to run and set its "
			+ "parameters.\n"
			+ "\n"
			+ "Click the button below to start the Master and go to "
			+ "the next step."
		);
		
		JScrollPane sclIntroText = new JScrollPane(introText);
			
		btnMaster = new JButton("Launch the Master");
		btnMaster.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) {	launchMaster(); }});
		
		JPanel pnlButtons = new JPanel();
			pnlButtons.setLayout(new FlowLayout(FlowLayout.LEFT));
			pnlButtons.add(btnMaster);
		
		JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout(10, 10));
			panel.add(sclIntroText, BorderLayout.CENTER);
			panel.add(pnlButtons, BorderLayout.SOUTH);
	
		return panel;
	}
	
	/**
	 * Inspector
	 * @return
	 */
	protected JPanel step5_setup()
	{
		JTextArea introText = new JTextArea();
		introText.setLineWrap(true);
		introText.setWrapStyleWord(true);
		introText.setEditable(false);
		introText.setMargin(new Insets(10, 10, 10, 10));
		introText.setAutoscrolls(true);
		introText.setText(
			"The last, optional, component is the Global Inspector.\n"
			+ "It allows you to get information on the the running "
			+ "simulation.\n"
			+ "\n"
			+ "Click the button below to start the Global Inspector "
			+ "and go to the next step."
		);
		
		JScrollPane sclIntroText = new JScrollPane(introText);
			
		btnInspector = new JButton("Launch the Inspector");
		btnInspector.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) {	launchInspector(); }});
		
		JPanel pnlButtons = new JPanel();
			pnlButtons.setLayout(new FlowLayout(FlowLayout.LEFT));
			pnlButtons.add(btnInspector);
		
		JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout(10, 10));
			panel.add(sclIntroText, BorderLayout.CENTER);
			panel.add(pnlButtons, BorderLayout.SOUTH);
	
		return panel;
	}
	
	/**
	 * Finish
	 * @return
	 */
	protected JPanel step6_setup()
	{
		JTextArea introText = new JTextArea();
		introText.setLineWrap(true);
		introText.setWrapStyleWord(true);
		introText.setEditable(false);
		introText.setMargin(new Insets(10, 10, 10, 10));
		introText.setAutoscrolls(true);
		introText.setText(
			"That's all.\n"
			+ "It allows you to get information on the the running "
			+ "simulation.\n"
			+ "\n"
			+ "Click the button below to start the Global Inspector "
			+ "and go to the next step."
		);
		
		JScrollPane sclIntroText = new JScrollPane(introText);
					
		JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout(10, 10));
			panel.add(sclIntroText, BorderLayout.CENTER);
			
		return panel;
	}
	
	protected void step1_go()
	{
		// Intentionally left blank
	}
	
	protected void step2_go()
	{
		if (!launchedWrapper)
		{
			btnNext.setEnabled(false);
		}
	}
	
	protected void step3_go()
	{
		if (!launchedWorkers)
		{
			btnNext.setEnabled(false);
		}
	}
	
	protected void step4_go()
	{
		if (!launchedMaster)
		{
			btnNext.setEnabled(false);
		}
	}
	
	protected void step5_go()
	{
		// Intentionally left blank
	}
	
	protected void step6_go()
	{
		// Intentionally left blank
	}
	
	protected void launchWrapper()
	{
		launcher.launchWrapper();
		this.launchedWrapper = true;
		lblStatusWrapper.setIcon(icoStatusOk);
		btnWrapper.setEnabled(false);
		btnNext.setEnabled(true);
		nextStep();
	}
	
	protected void launchWorkers()
	{
		for (int i = 0; i < sldNumWorkers.getValue(); i++)
		{
			launcher.launchWorker();
		}
		this.launchedWorkers = true;
		lblStatusWorkers.setIcon(icoStatusOk);
		sldNumWorkers.setEnabled(false);
		btnWorkers.setEnabled(false);
		btnNext.setEnabled(true);
		nextStep();
	}
	
	protected void launchMaster()
	{
		launcher.launchMaster();
		this.launchedMaster = true;
		lblStatusMaster.setIcon(icoStatusOk);
		btnMaster.setEnabled(false);
		btnNext.setEnabled(true);
		nextStep();
	}
	
	protected void launchInspector()
	{
		launcher.launchInspector();
		this.launchedInspector = true;
		lblStatusInspector.setIcon(icoStatusOk);
		btnInspector.setEnabled(false);
		btnNext.setEnabled(true);
		nextStep();
	}
}
