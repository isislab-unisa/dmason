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

package it.isislab.dmason.experimentals.tools.batch;

import it.isislab.dmason.annotation.BatchAnnotation;
import it.isislab.dmason.annotation.ThinAnnotation;
import it.isislab.dmason.annotation.ValueAnnotation;
import it.isislab.dmason.experimentals.tools.batch.data.Batch;
import it.isislab.dmason.experimentals.tools.batch.data.Param;
import it.isislab.dmason.experimentals.tools.batch.data.ParamDistribution;
import it.isislab.dmason.experimentals.tools.batch.data.ParamDistributionExponential;
import it.isislab.dmason.experimentals.tools.batch.data.ParamDistributionNormal;
import it.isislab.dmason.experimentals.tools.batch.data.ParamDistributionUniform;
import it.isislab.dmason.experimentals.tools.batch.data.ParamFixed;
import it.isislab.dmason.experimentals.tools.batch.data.ParamList;
import it.isislab.dmason.experimentals.tools.batch.data.ParamRange;
import it.isislab.dmason.util.management.JarClassLoader;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.io.FilenameUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class BatchWizard extends JFrame 
{

	public enum DistributionType {
		none, uniform, exponential, normal
	}
	
	private JPanel contentPane;
	private JTextField textFieldRuns;
	private JTextField textFieldValue;
	private JTextField textFieldStartValue;
	private JTextField textFieldEndValue;
	private JTextField textFieldIncrement;
	private JTextField textFieldSimJarPath;
	private JLabel lblEndValue;
	private JLabel lblIncrement;
	private JLabel lblStartValue;
	private JLabel lblValue;
	private JLabel lblParamType;
	private JRadioButton rdbtnFixed;
	private JRadioButton rdbtnRange;
	private JLabel lblMessage;

	private static String configFileName = "batchParams.xml";
	private static File simulationFile;
	private Param selectedParam;
	private int selectedParamIndex;
	private static DefaultMutableTreeNode top;
	private JPanel panel_1;
	private JButton btnModify;
	private JButton btnCancel;
	private String message = "Select a parameter to modify it";
	private JLabel lblTotTests;
	private String totTestsMessage = "Tot tests: ";

	private int testAlertThreshold = 100;
	private JButton btnLoadParams;
	private static DefaultMutableTreeNode generalParams = new DefaultMutableTreeNode("General Params");
	private static DefaultMutableTreeNode simParams = new DefaultMutableTreeNode("Simulation Params");
	
	private String paramType;
	private JTextField textFieldList;
	private JTextField textFieldA;
	private JTextField textFieldB;
	private JComboBox jComboBoxDistribution;
	protected DistributionType selectedDistribution;
	private JLabel lblDistribution;
	private JLabel lblCommaSeparatedList;
	private JLabel lblA;
	private JLabel lblB;
	private JRadioButton rdbtnByvalues;
	private JRadioButton rdbtnByDistribution;
	private JLabel lblOfValues;
	private JTextField textFieldNumberOfValues;
	private static JTextField textFieldNumberOfWorkers;
	private static JLabel lblSuggested;
	private static JLabel lblDomain;
	private HashMap<String, ValueAnnotation> suggestion;
	protected boolean isThin;
	private static JCheckBox checkBoxLoadBalancing;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					BatchWizard frame = new BatchWizard();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public BatchWizard() {
		setPreferredSize(new Dimension(800, 600));
		setTitle("Batch wizard");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 849, 620);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(1, 0, 0, 0));

		JPanel panel = new JPanel();
		panel.setToolTipText("");
		contentPane.add(panel);

		panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(UIManager
				.getBorder("TitledBorder.border"), "Param Option",
				TitledBorder.LEADING, TitledBorder.TOP, null,
				new Color(0, 0, 0)));

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "Params List",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));

		JPanel panel_3 = new JPanel();

		JPanel panel_4 = new JPanel();
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_panel.createSequentialGroup()
							.addComponent(panel_4, GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)
							.addGap(20))
						.addGroup(gl_panel.createSequentialGroup()
							.addComponent(panel_2, GroupLayout.DEFAULT_SIZE, 545, Short.MAX_VALUE)
							.addGap(4))))
				.addComponent(panel_3, GroupLayout.DEFAULT_SIZE, 823, Short.MAX_VALUE)
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel.createSequentialGroup()
					.addComponent(panel_3, GroupLayout.PREFERRED_SIZE, 47, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_panel.createSequentialGroup()
							.addComponent(panel_2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addGap(25)
							.addComponent(panel_4, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE))
						.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addContainerGap())
		);

		final JButton btnSave = new JButton("Save");
		btnSave.setEnabled(false);
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				File saveFile = SaveFileChooser();
				createXML(saveFile.getAbsoluteFile().getPath());
			}
		});

		lblTotTests = new JLabel(totTestsMessage);
		GroupLayout gl_panel_4 = new GroupLayout(panel_4);
		gl_panel_4.setHorizontalGroup(gl_panel_4.createParallelGroup(
				Alignment.TRAILING).addGroup(
				gl_panel_4
						.createSequentialGroup()
						.addGap(21)
						.addComponent(lblTotTests)
						.addPreferredGap(ComponentPlacement.RELATED, 515,
								Short.MAX_VALUE).addComponent(btnSave)
						.addGap(21)));
		gl_panel_4.setVerticalGroup(gl_panel_4.createParallelGroup(
				Alignment.LEADING)
				.addGroup(
						gl_panel_4
								.createSequentialGroup()
								.addContainerGap()
								.addGroup(
										gl_panel_4
												.createParallelGroup(
														Alignment.BASELINE)
												.addComponent(btnSave)
												.addComponent(lblTotTests))
								.addContainerGap(GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)));
		panel_4.setLayout(gl_panel_4);

		top = new DefaultMutableTreeNode("Parameters");

		JScrollPane scrollPaneTree = new JScrollPane();
		
		JLabel lblNumberOfWorkers = new JLabel("Number of Workers:");
		
		textFieldNumberOfWorkers = new JTextField();
		textFieldNumberOfWorkers.setText("1");
		textFieldNumberOfWorkers.setColumns(10);
		textFieldNumberOfWorkers.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				if(textFieldNumberOfWorkers.isVisible() )
				{
				boolean checkNumberOfWorkers=true;
				
				while(checkNumberOfWorkers){
			
					String dist=textFieldNumberOfWorkers.getText();
					boolean validateDist=dist.matches("(\\d)+");
					if(!validateDist){	
						String	newDist=  JOptionPane.showInputDialog(null,"Insert a number","Number Format Error", 0);
						textFieldNumberOfWorkers.setText(newDist);
					}
			
					else{
						checkNumberOfWorkers=false;
					}
				}
				}
			}
		});
		
		checkBoxLoadBalancing = new JCheckBox("Load Balancing", false);
		checkBoxLoadBalancing.setEnabled(true);

		GroupLayout gl_panel_2 = new GroupLayout(panel_2);
		gl_panel_2.setHorizontalGroup(
			gl_panel_2.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_2.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
						.addComponent(scrollPaneTree, GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
						.addGroup(gl_panel_2.createSequentialGroup()
							.addComponent(lblNumberOfWorkers)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(textFieldNumberOfWorkers, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE))
						.addComponent(checkBoxLoadBalancing, GroupLayout.PREFERRED_SIZE, 114, GroupLayout.PREFERRED_SIZE))
					.addContainerGap())
		);
		gl_panel_2.setVerticalGroup(
			gl_panel_2.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_2.createSequentialGroup()
					.addContainerGap()
					.addComponent(scrollPaneTree, GroupLayout.PREFERRED_SIZE, 271, GroupLayout.PREFERRED_SIZE)
					.addGap(33)
					.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNumberOfWorkers)
						.addComponent(textFieldNumberOfWorkers, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(checkBoxLoadBalancing, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(56, Short.MAX_VALUE))
		);

		final JTree treeParams = new JTree(top);
		scrollPaneTree.setViewportView(treeParams);
		treeParams.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent selected) {
				// DefaultMutableTreeNode parent =
				// selected.getPath().getParentPath()

				
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeParams
						.getLastSelectedPathComponent();
			if(node.getParent() != null)
			{
				if (node.getParent() == simParams|| node.getParent().equals(generalParams)) {

					selectedParam = (Param) node.getUserObject();
					if(node.getParent() == simParams)
					{
						selectedParamIndex = simParams.getIndex(node);
						paramType = "simParam";
					}
						
					else
					{
						selectedParamIndex = generalParams.getIndex(node);
						paramType = "generalParam";
					}
					if (selectedParam instanceof ParamFixed) {
						ParamFixed pf = (ParamFixed) selectedParam;
						lblParamType.setText(pf.getName() + ": " + pf.getType());
						if(suggestion.get(pf.getName()) != null)
						{
							lblDomain.setText("Domain: "+suggestion.get(pf.getName()).getDomain());
							lblSuggested.setText("Suggested Value: "+suggestion.get(pf.getName()).getSuggestedValue());
						}
						
						textFieldRuns.setText("" + pf.getRuns());
						textFieldValue.setText(pf.getValue());
						rdbtnFixed.doClick();

						lblMessage.setVisible(false);
						setModifyControlEnable(true);

					}

					if (selectedParam instanceof ParamRange) {
						ParamRange pf = (ParamRange) selectedParam;
						lblParamType.setText(pf.getName() + ": " + pf.getType());
						if(suggestion.get(pf.getName()) != null)
						{
							lblDomain.setText("Domain: "+suggestion.get(pf.getName()).getDomain());
							lblSuggested.setText("Suggested Value: "+suggestion.get(pf.getName()).getSuggestedValue());
						}
						textFieldRuns.setText("" + pf.getRuns());
						textFieldStartValue.setText(pf.getStart());
						textFieldEndValue.setText(pf.getEnd());
						textFieldIncrement.setText(pf.getIncrement());
						rdbtnRange.doClick();

						lblMessage.setVisible(false);
						setModifyControlEnable(true);
					}

					if (selectedParam instanceof ParamList) {
						ParamList pl = (ParamList) selectedParam;
						if(suggestion.get(pl.getName()) != null)
						{
							lblDomain.setText("Domain: "+suggestion.get(pl.getName()).getDomain());
							lblSuggested.setText("Suggested Value: "+suggestion.get(pl.getName()).getSuggestedValue());
						}
						lblParamType.setText(pl.getName() + ": " + pl.getType());
						textFieldRuns.setText("" + pl.getRuns());
						StringBuilder b = new StringBuilder();
						boolean isFirst = true;
						for (String element : pl.getValues()) {
							if(isFirst)
							{	
								b.append(element);
								isFirst = false;
							}
							else
								b.append(","+element);
						}
						textFieldList.setText(b.toString());
						rdbtnByvalues.doClick();

						setListControlvisibility(true);
						lblMessage.setVisible(false);
						setModifyControlEnable(true);
					}
					if (selectedParam instanceof ParamDistribution) {
						DistributionType distType = DistributionType.none;
						if(selectedParam instanceof ParamDistributionUniform)
						{
							ParamDistributionUniform pu = (ParamDistributionUniform) selectedParam;
							lblParamType.setText(pu.getName() + ": " + pu.getType());
							if(suggestion.get(pu.getName()) != null)
							{
								lblDomain.setText("Domain: "+suggestion.get(pu.getName()).getDomain());
								lblSuggested.setText("Suggested Value: "+suggestion.get(pu.getName()).getSuggestedValue());
							}
							
							textFieldRuns.setText("" + pu.getRuns());
							textFieldA.setText(pu.getA());
							textFieldB.setText(pu.getB());
							textFieldNumberOfValues.setText("" +pu.getNumberOfValues());
							distType = DistributionType.uniform;
						}
						if(selectedParam instanceof ParamDistributionExponential)
						{
							ParamDistributionExponential pe = (ParamDistributionExponential) selectedParam;
							lblParamType.setText(pe.getName() + ": " + pe.getType());
							if(suggestion.get(pe.getName()) != null)
							{
								
								lblDomain.setText("Domain: "+suggestion.get(pe.getName()).getDomain());
								lblSuggested.setText("Suggested Value: "+suggestion.get(pe.getName()).getSuggestedValue());
							}
							
							
							textFieldRuns.setText("" + pe.getRuns());
							textFieldA.setText(pe.getLambda());
							textFieldNumberOfValues.setText("" +pe.getNumberOfValues());
							distType = DistributionType.exponential;
						}
						if(selectedParam instanceof ParamDistributionNormal)
						{
							ParamDistributionNormal pn = (ParamDistributionNormal) selectedParam;
							lblParamType.setText(pn.getName() + ": " + pn.getType());
							if(suggestion.get(pn.getName()) != null)
							{
								lblDomain.setText("Domain: "+suggestion.get(pn.getName()).getDomain());
								lblSuggested.setText("Suggested Value: "+suggestion.get(pn.getName()).getSuggestedValue());
							}
							
							textFieldRuns.setText("" + pn.getRuns());
							textFieldA.setText(pn.getMean());
							textFieldB.setText(pn.getStdDev());
							textFieldNumberOfValues.setText("" +pn.getNumberOfValues());
							distType = DistributionType.normal;
							
						}
						
						
						rdbtnByDistribution.doClick();
						setDistributionControlVisibility(distType);
						setDistributionComboBoxVisibility(true);
						lblMessage.setVisible(false);
						setModifyControlEnable(true);
					}
				}
			}
			}
		});

		panel_2.setLayout(gl_panel_2);

		JLabel lblSelectSimulationJar = new JLabel("Select simulation jar:");

		textFieldSimJarPath = new JTextField();
		textFieldSimJarPath.setColumns(10);

		btnLoadParams = new JButton("Load Params");
		btnLoadParams.setEnabled(false);
		btnLoadParams.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ArrayList<Param> params = loadParams();
				if (params != null) {
					top.removeAllChildren();
					createNodes(top, params);
					treeParams.expandPath(new TreePath(top.getPath()));
					treeParams.expandPath(new TreePath(simParams.getPath()));
					treeParams.expandPath(new TreePath(generalParams.getPath()));
				}

				lblTotTests.setText(totTestsMessage + " " + getTotTests());
				btnSave.setEnabled(true);
			}
		});

		JButton bntChooseSimulation = new JButton();
		bntChooseSimulation.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				simulationFile = showFileChooser();
				if (simulationFile != null) {
					textFieldSimJarPath.setText(simulationFile
							.getAbsolutePath());
					btnLoadParams.setEnabled(true);
					
					isThin = isThinSimulation(simulationFile);
					checkBoxLoadBalancing.setEnabled(!isThin);
					
				}
			}

		});
		bntChooseSimulation.setIcon(new ImageIcon(BatchWizard.class
				.getResource("/it.isislab.dmason/resource/image/openFolder.png")));
		GroupLayout gl_panel_3 = new GroupLayout(panel_3);
		gl_panel_3.setHorizontalGroup(gl_panel_3.createParallelGroup(
				Alignment.LEADING).addGroup(
				gl_panel_3
						.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblSelectSimulationJar)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(textFieldSimJarPath,
								GroupLayout.PREFERRED_SIZE, 250,
								GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(bntChooseSimulation,
								GroupLayout.PREFERRED_SIZE, 30,
								GroupLayout.PREFERRED_SIZE).addGap(26)
						.addComponent(btnLoadParams)
						.addContainerGap(172, Short.MAX_VALUE)));
		gl_panel_3
				.setVerticalGroup(gl_panel_3
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								gl_panel_3
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												gl_panel_3
														.createParallelGroup(
																Alignment.LEADING)
														.addGroup(
																gl_panel_3
																		.createParallelGroup(
																				Alignment.BASELINE)
																		.addComponent(
																				lblSelectSimulationJar)
																		.addComponent(
																				textFieldSimJarPath,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.DEFAULT_SIZE,
																				GroupLayout.PREFERRED_SIZE)
																		.addComponent(
																				bntChooseSimulation,
																				GroupLayout.PREFERRED_SIZE,
																				25,
																				GroupLayout.PREFERRED_SIZE))
														.addComponent(
																btnLoadParams))
										.addContainerGap(
												GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));
		panel_3.setLayout(gl_panel_3);

		lblParamType = new JLabel("Param : type");
		lblParamType.setFont(new Font("Tahoma", Font.BOLD, 11));

		JLabel lblRuns = new JLabel("Runs:");
		textFieldRuns = new JTextField();
		textFieldRuns.setColumns(10);
		textFieldRuns.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				if(textFieldRuns.isVisible())
				{
				boolean checkRuns=true;
				
				while(checkRuns){
			
					String dist=textFieldRuns.getText();
					boolean validateDist=dist.matches("(\\d)+");
					if(!validateDist){	
						String	newDist=  JOptionPane.showInputDialog(null,"Insert a number","Number Format Error", 0);
						textFieldRuns.setText(newDist);
					}
			
					else{
						checkRuns=false;
					}
				}
				}
			}
		});
		JLabel lblParameterSpace = new JLabel("Parameter Space");
		lblParameterSpace.setFont(new Font("Tahoma", Font.BOLD, 11));

		lblValue = new JLabel("Value:");

		textFieldValue = new JTextField();
		textFieldValue.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				String regex="(\\d)+|((\\d)+\\.(\\d)+)";
				if(textFieldValue.isVisible())
				{
				boolean checkValue=true;
				
				while(checkValue){
			
					String dist=textFieldValue.getText();
					boolean validateDist=dist.matches(regex);
					if(!validateDist){	
						String	newDist=  JOptionPane.showInputDialog(null,"Insert a number","Number Format Error", 0);
						textFieldValue.setText(newDist);
					}
			
					else{
						checkValue=false;
					}
				}
				}
			}
		});
		textFieldValue.setColumns(10);
		/*textFieldValue.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent arg0) {}

			@Override
			public void keyReleased(KeyEvent arg0) {
				checkError();


			}

			@Override
			public void keyPressed(KeyEvent arg0) {}
		});
*/
		lblStartValue = new JLabel("Start value:");

		textFieldStartValue = new JTextField();
		textFieldStartValue.setText("1");
		textFieldStartValue.setColumns(10);
		textFieldStartValue.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				String regex="(\\d)+|((\\d)+\\.(\\d)+)";
				if(textFieldStartValue.isVisible() )
				{
				boolean checkStartValue=true;
				
				while(checkStartValue){
			
					String dist=textFieldStartValue.getText();
					boolean validateDist=dist.matches(regex);
					if(!validateDist){	
						String	newDist=  JOptionPane.showInputDialog(null,"Insert a number","Number Format Error", 0);
						textFieldStartValue.setText(newDist);
					}
			
					else{
						checkStartValue=false;
					}
				}
				}
			}
		});

		lblEndValue = new JLabel("End value:");

		textFieldEndValue = new JTextField();
		textFieldEndValue.setText("1");
		textFieldEndValue.setColumns(10);
		textFieldEndValue.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				String regex="(\\d)+|((\\d)+\\.(\\d)+)";
				if(textFieldEndValue.isVisible() )
				{
				boolean checkEndValue=true;
				
				while(checkEndValue){
			
					String dist=textFieldEndValue.getText();
					boolean validateDist=dist.matches(regex);
					if(!validateDist){	
						String	newDist=  JOptionPane.showInputDialog(null,"Insert a number","Number Format Error", 0);
						textFieldEndValue.setText(newDist);
					}
			
					else{
						checkEndValue=false;
					}
				}
				}
			}
		});

		lblIncrement = new JLabel("Increment:");

		textFieldIncrement = new JTextField();
		textFieldIncrement.setText("1");
		textFieldIncrement.setColumns(10);
		textFieldIncrement.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				String regex="(\\d)+|((\\d)+\\.(\\d)+)";
				if(textFieldIncrement.isVisible() )
				{
					
				boolean checkIncrement=true;
				
				while(checkIncrement){
			
					String dist=textFieldIncrement.getText();
					boolean validateDist=dist.matches(regex);
					if(!validateDist){	
						String	newDist=  JOptionPane.showInputDialog(null,"Insert a number","Number Format Error", 0);
						textFieldIncrement.setText(newDist);
					}
			
					else{
						checkIncrement=false;
					}
				}
				}
			}
		});

		rdbtnFixed = new JRadioButton("Fixed");
		rdbtnFixed.setSelected(true);
		rdbtnFixed.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				setListControlvisibility(false);
				setDistributionComboBoxVisibility(false);
				setRangeControlVisibility(false);
				setFixedControlVisibility(true);
				setDistributionControlVisibility(DistributionType.none);
			}

		});

		rdbtnRange = new JRadioButton("Range");
		rdbtnRange.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setRangeControlVisibility(true);
				setFixedControlVisibility(false);
				setListControlvisibility(false);
				setDistributionComboBoxVisibility(false);
				setDistributionControlVisibility(DistributionType.none);
			}
		});
		
		rdbtnByvalues = new JRadioButton("By Values");
		rdbtnByvalues.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setRangeControlVisibility(false);
				setFixedControlVisibility(false);
				setListControlvisibility(true);
				setDistributionComboBoxVisibility(false);
				setDistributionControlVisibility(DistributionType.none);
			}
		});
		rdbtnByDistribution = new JRadioButton("By Distribution");
		rdbtnByDistribution.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setRangeControlVisibility(false);
				setFixedControlVisibility(false);
				setListControlvisibility(false);
				setDistributionComboBoxVisibility(true);
				setDistributionControlVisibility(DistributionType.none);
			}
		});
		// Group the radio buttons.
		ButtonGroup group = new ButtonGroup();
		group.add(rdbtnFixed);
		group.add(rdbtnRange);
		group.add(rdbtnByvalues);
		group.add(rdbtnByDistribution);

		setRangeControlVisibility(false);
		setFixedControlVisibility(false);

		lblMessage = new JLabel(message);

		btnModify = new JButton("Modify");
		btnModify.addActionListener(new ActionListener() {

			

			@Override
			public void actionPerformed(ActionEvent arg0) {

				if (rdbtnFixed.isSelected()) 
				{
					ParamFixed param = new ParamFixed(selectedParam.getName(),
							selectedParam.getType(), Integer
									.parseInt(textFieldRuns.getText()),
							textFieldValue.getText());
					DefaultMutableTreeNode p = new DefaultMutableTreeNode(param);

					if(paramType.equals("simParam"))
					{
						simParams.remove(selectedParamIndex);

						simParams.insert(p, selectedParamIndex);
					}
					else
					{
						generalParams.remove(selectedParamIndex);

						generalParams.insert(p, selectedParamIndex);
					}
					
					treeParams.updateUI();

					/*
					 * ((ParamFixed)
					 * selectedParam).setValue(textFieldValue.getText());
					 * selectedParam
					 * .setRuns(Integer.parseInt(textFieldRuns.getText()));
					 * treeParams.repaint();
					 */
				} 
				if(rdbtnRange.isSelected())
				{
					ParamRange param = new ParamRange(selectedParam.getName(),
							selectedParam.getType(), Integer
									.parseInt(textFieldRuns.getText()),
							textFieldStartValue.getText(), textFieldEndValue
									.getText(), textFieldIncrement.getText());
					DefaultMutableTreeNode p = new DefaultMutableTreeNode(param);

					if(paramType.equals("simParam"))
					{
						simParams.remove(selectedParamIndex);

						simParams.insert(p, selectedParamIndex);
					}
					else
					{
						generalParams.remove(selectedParamIndex);

						generalParams.insert(p, selectedParamIndex);
					}
					treeParams.updateUI();
					// treeParams.repaint();

				}
				if(rdbtnByvalues.isSelected())
				{
					StringTokenizer st = new StringTokenizer(textFieldList.getText(), ",");
					ArrayList<String> values = new ArrayList<String>();
					while(st.hasMoreTokens())
						values.add(st.nextToken());
						
					ParamList param = new ParamList(selectedParam.getName(),
							selectedParam.getType(), Integer
							.parseInt(textFieldRuns.getText()),values);
					DefaultMutableTreeNode p = new DefaultMutableTreeNode(param);

					if(paramType.equals("simParam"))
					{
						simParams.remove(selectedParamIndex);

						simParams.insert(p, selectedParamIndex);
					}
					else
					{
						generalParams.remove(selectedParamIndex);

						generalParams.insert(p, selectedParamIndex);
					}
					treeParams.updateUI();
					// treeParams.repaint();

				}
				if(rdbtnByDistribution.isSelected())
				{
					DefaultMutableTreeNode p;
					switch (selectedDistribution) 
					{
						case uniform:
							
						p = new DefaultMutableTreeNode(new ParamDistributionUniform(selectedParam.getName(),
								selectedParam.getType(), 
								Integer.parseInt(textFieldRuns.getText()), textFieldA.getText(),textFieldB.getText(),
								Integer.parseInt(textFieldNumberOfValues.getText())
						));
						break;
						case exponential:
							
							 
						p = new DefaultMutableTreeNode(new ParamDistributionExponential(selectedParam.getName(),
								selectedParam.getType(), 
								Integer.parseInt(textFieldRuns.getText()), textFieldA.getText(),
								Integer.parseInt(textFieldNumberOfValues.getText())
						));
						break;
						
						case normal:
							
							 
							p = new DefaultMutableTreeNode(new ParamDistributionNormal(selectedParam.getName(),
									selectedParam.getType(), 
									Integer.parseInt(textFieldRuns.getText()), textFieldA.getText(),textFieldA.getText(),
									Integer.parseInt(textFieldNumberOfValues.getText())
							));
						break;
						default:
							p = new DefaultMutableTreeNode();
						break;
					}
					
					

					if(paramType.equals("simParam"))
					{
						simParams.remove(selectedParamIndex);

						simParams.insert(p, selectedParamIndex);
					}
					else
					{
						generalParams.remove(selectedParamIndex);

						generalParams.insert(p, selectedParamIndex);
					}
					treeParams.updateUI();
					// treeParams.repaint();

				}
				lblMessage.setVisible(true);
				setModifyControlEnable(false);
				setDistributionControlVisibility(DistributionType.none);
				setDistributionComboBoxVisibility(false);
				setListControlvisibility(false);

				int tot = getTotTests();
				if (tot >= testAlertThreshold)
					lblTotTests.setForeground(Color.RED);
				else
					lblTotTests.setForeground(Color.BLACK);

				lblTotTests.setText(totTestsMessage + " " + tot);

			}
		});

		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				lblMessage.setVisible(true);
				setModifyControlEnable(false);

			}
		});
		
		
		
		lblCommaSeparatedList = new JLabel("List:");
		lblCommaSeparatedList.setVisible(false);
		
		textFieldList = new JTextField();
		textFieldList.setVisible(false);
		textFieldList.setToolTipText("Comma separated");
		textFieldList.setColumns(10);
		textFieldList.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				/*if(textFieldList.isVisible())
				{
				boolean checkList=true;
				
				while(checkList){
			
					String dist=textFieldList.getText();
					boolean validateDist=dist.matches("(\\d)+|((\\d)+\\.(\\d)+)(,(\\d)+|((\\d)+\\.(\\d)+))*");
					if(!validateDist){	
						String	newDist=  JOptionPane.showInputDialog(null,"Insert comma separate number list","Number Format Error", 0);
						textFieldList.setText(newDist);
					}
			
					else{
						checkList=false;
					}
				}
				}*/
			}
		});
		
		lblDistribution = new JLabel("Distribution");
		lblDistribution.setVisible(false);
		
		lblA = new JLabel("a:");
		lblA.setVisible(false);
		
		textFieldA = new JTextField();
		textFieldA.setText("1");
		textFieldA.setVisible(false);
		textFieldA.setColumns(10);
		textFieldA.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				String regex="(\\d)+|((\\d)+\\.(\\d)+)";

				if(textFieldA.isVisible())
				{
					boolean checkA=true;
					
					while(checkA){
				
						String dist=textFieldA.getText();
						boolean validateDist=dist.matches(regex);
						if(!validateDist){	
							String	newDist=  JOptionPane.showInputDialog(null,"Insert a number","Number Format Error", 0);
							textFieldA.setText(newDist);
						}
				
						else{
							checkA=false;
						}
					}
				}
			}
		});
		
		lblB = new JLabel("b:");
		lblB.setVisible(false);
		
		textFieldB = new JTextField();
		textFieldB.setText("1");
		textFieldB.setVisible(false);
		textFieldB.setColumns(10);
		textFieldB.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				
				String regex="(\\d)+|((\\d)+\\.(\\d)+)";
				if(textFieldB.isVisible() )
				{
				boolean checkB=true;
				
				while(checkB){
			
					String dist=textFieldB.getText();
					boolean validateDist=dist.matches(regex);
					if(!validateDist){	
						String	newDist=  JOptionPane.showInputDialog(null,"Insert a number","Number Format Error", 0);
						textFieldB.setText(newDist);
					}
			
					else{
						checkB=false;
					}
				}
				}
			}
		});
		jComboBoxDistribution = new JComboBox();
		jComboBoxDistribution.setVisible(false);
		jComboBoxDistribution.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// Prevent executing listener's actions two times
				if (e.getStateChange() != ItemEvent.SELECTED)
					return;
				selectedDistribution = ((DistributionType)jComboBoxDistribution.getSelectedItem());
				
				setDistributionControlVisibility(DistributionType.none);
				setDistributionControlVisibility(selectedDistribution);
				
			}
		});
		
		lblOfValues = new JLabel("# of values:");
		lblOfValues.setVisible(false);
		
		textFieldNumberOfValues = new JTextField();
		textFieldNumberOfValues.setText("1");
		textFieldNumberOfValues.setVisible(false);
		textFieldNumberOfValues.setColumns(10);
		textFieldNumberOfValues.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				if(textFieldNumberOfValues.isVisible())
				{
				boolean checkNumberOfValues=true;
				
				while(checkNumberOfValues){
			
					String dist=textFieldNumberOfValues.getText();
					boolean validateDist=dist.matches("(\\d)+");
					if(!validateDist){	
						String	newDist=  JOptionPane.showInputDialog(null,"Insert a number","Number Format Error", 0);
						textFieldNumberOfValues.setText(newDist);
					}
			
					else{
						checkNumberOfValues=false;
					}
				}
				}
			}
		});
		
		lblSuggested = new JLabel("Suggested Value:");
		
		lblDomain = new JLabel("Domain:");
		
		

		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(
			gl_panel_1.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_1.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_panel_1.createSequentialGroup()
							.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
								.addComponent(lblParamType)
								.addComponent(lblMessage)
								.addComponent(lblSuggested))
							.addContainerGap(77, Short.MAX_VALUE))
						.addGroup(gl_panel_1.createSequentialGroup()
							.addComponent(btnModify)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnCancel)
							.addContainerGap())
						.addGroup(gl_panel_1.createSequentialGroup()
							.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
								.addComponent(lblParameterSpace)
								.addGroup(gl_panel_1.createSequentialGroup()
									.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
										.addComponent(rdbtnFixed)
										.addComponent(rdbtnRange))
									.addGap(31)
									.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
										.addComponent(rdbtnByDistribution)
										.addComponent(rdbtnByvalues)))
								.addGroup(gl_panel_1.createSequentialGroup()
									.addGroup(gl_panel_1.createParallelGroup(Alignment.TRAILING)
										.addGroup(gl_panel_1.createSequentialGroup()
											.addComponent(lblValue)
											.addPreferredGap(ComponentPlacement.RELATED, 59, Short.MAX_VALUE)
											.addComponent(textFieldValue, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
										.addGroup(Alignment.LEADING, gl_panel_1.createSequentialGroup()
											.addComponent(lblStartValue)
											.addPreferredGap(ComponentPlacement.RELATED, 46, Short.MAX_VALUE)
											.addComponent(textFieldStartValue, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
										.addGroup(gl_panel_1.createSequentialGroup()
											.addComponent(lblCommaSeparatedList)
											.addPreferredGap(ComponentPlacement.RELATED, 69, Short.MAX_VALUE)
											.addComponent(textFieldList, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
										.addGroup(gl_panel_1.createSequentialGroup()
											.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
												.addComponent(lblEndValue)
												.addComponent(lblIncrement)
												.addComponent(lblDistribution))
											.addGap(45)
											.addGroup(gl_panel_1.createParallelGroup(Alignment.TRAILING)
												.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
													.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING, false)
														.addComponent(jComboBoxDistribution, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
														.addComponent(textFieldA, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
														.addComponent(textFieldB, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
														.addComponent(textFieldNumberOfValues, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
													.addComponent(textFieldIncrement, 89, 89, 89))
												.addComponent(textFieldEndValue, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
									.addPreferredGap(ComponentPlacement.RELATED, 8, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_panel_1.createSequentialGroup()
									.addComponent(lblA)
									.addPreferredGap(ComponentPlacement.RELATED, 173, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_panel_1.createSequentialGroup()
									.addComponent(lblB)
									.addPreferredGap(ComponentPlacement.RELATED, 173, GroupLayout.PREFERRED_SIZE))
								.addComponent(lblOfValues)
								.addGroup(gl_panel_1.createSequentialGroup()
									.addComponent(lblRuns)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(textFieldRuns, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED, 65, GroupLayout.PREFERRED_SIZE)))
							.addGap(35))
						.addGroup(gl_panel_1.createSequentialGroup()
							.addComponent(lblDomain)
							.addContainerGap(186, Short.MAX_VALUE))))
		);
		gl_panel_1.setVerticalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addGap(4)
					.addComponent(lblMessage)
					.addGap(18)
					.addComponent(lblParamType)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblSuggested)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblDomain)
					.addGap(7)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblRuns)
						.addComponent(textFieldRuns, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblParameterSpace)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
						.addComponent(rdbtnFixed)
						.addComponent(rdbtnByvalues))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
						.addComponent(rdbtnRange)
						.addComponent(rdbtnByDistribution))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
						.addComponent(textFieldValue, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblValue))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
						.addComponent(textFieldList, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblCommaSeparatedList))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblStartValue)
						.addComponent(textFieldStartValue, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblEndValue)
						.addComponent(textFieldEndValue, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
						.addComponent(textFieldIncrement, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblIncrement))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
						.addComponent(jComboBoxDistribution, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblDistribution))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblA)
						.addComponent(textFieldA, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblB)
						.addComponent(textFieldB, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblOfValues)
						.addComponent(textFieldNumberOfValues, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnModify)
						.addComponent(btnCancel)))
		);
		panel_1.setLayout(gl_panel_1);
		panel.setLayout(gl_panel);

		setModifyControlEnable(false);
		loadDistribution();
	}
	
	private void checkError(){
		
		//controllo solo numeri non negativi
		
		String regex="(\\d)+|((\\d)+\\.(\\d)+)";

		if(textFieldA.isVisible() )
		{
			boolean checkA=true;
			
			while(checkA){
		
				String dist=textFieldA.getText();
				boolean validateDist=dist.matches(regex);
				if(!validateDist){	
					String	newDist=  JOptionPane.showInputDialog(null,"Insert a number","Number Format Error", 0);
					textFieldA.setText(newDist);
				}
		
				else{
					checkA=false;
				}
			}
		}
		
		if(textFieldB.isVisible() )
		{
		boolean checkB=true;
		
		while(checkB){
	
			String dist=textFieldB.getText();
			boolean validateDist=dist.matches(regex);
			if(!validateDist){	
				String	newDist=  JOptionPane.showInputDialog(null,"Insert a number","Number Format Error", 0);
				textFieldB.setText(newDist);
			}
	
			else{
				checkB=false;
			}
		}
		}
		
		if(textFieldEndValue.isVisible() )
		{
		boolean checkEndValue=true;
		
		while(checkEndValue){
	
			String dist=textFieldEndValue.getText();
			boolean validateDist=dist.matches(regex);
			if(!validateDist){	
				String	newDist=  JOptionPane.showInputDialog(null,"Insert a number","Number Format Error", 0);
				textFieldEndValue.setText(newDist);
			}
	
			else{
				checkEndValue=false;
			}
		}
		}
		
		if(textFieldIncrement.isVisible() )
		{
		boolean checkIncrement=true;
		
		while(checkIncrement){
	
			String dist=textFieldIncrement.getText();
			boolean validateDist=dist.matches(regex);
			if(!validateDist){	
				String	newDist=  JOptionPane.showInputDialog(null,"Insert a number","Number Format Error", 0);
				textFieldIncrement.setText(newDist);
			}
	
			else{
				checkIncrement=false;
			}
		}
		}
		

		if(textFieldStartValue.isVisible() )
		{
		boolean checkStartValue=true;
		
		while(checkStartValue){
	
			String dist=textFieldStartValue.getText();
			boolean validateDist=dist.matches(regex);
			if(!validateDist){	
				String	newDist=  JOptionPane.showInputDialog(null,"Insert a number","Number Format Error", 0);
				textFieldStartValue.setText(newDist);
			}
	
			else{
				checkStartValue=false;
			}
		}
		}
		
		if(textFieldValue.isVisible())
		{
		boolean checkValue=true;
		
		while(checkValue){
	
			String dist=textFieldValue.getText();
			boolean validateDist=dist.matches(regex);
			if(!validateDist){	
				String	newDist=  JOptionPane.showInputDialog(null,"Insert a number","Number Format Error", 0);
				textFieldValue.setText(newDist);
			}
	
			else{
				checkValue=false;
			}
		}
		}
		
		if(textFieldList.isVisible())
		{
		boolean checkList=true;
		
		while(checkList){
	
			String dist=textFieldList.getText();
			boolean validateDist=dist.matches("(\\d)+|((\\d)+\\.(\\d)+)((,)(\\d)+|((\\d)+\\.(\\d)+))*");
			if(!validateDist){	
				String	newDist=  JOptionPane.showInputDialog(null,"Insert comma separate number list","Number Format Error", 0);
				textFieldList.setText(newDist);
			}
	
			else{
				checkList=false;
			}
		}
		}
		
		if(textFieldNumberOfValues.isVisible())
		{
		boolean checkNumberOfValues=true;
		
		while(checkNumberOfValues){
	
			String dist=textFieldNumberOfValues.getText();
			boolean validateDist=dist.matches("(\\d)+");
			if(!validateDist){	
				String	newDist=  JOptionPane.showInputDialog(null,"Insert a number","Number Format Error", 0);
				textFieldNumberOfValues.setText(newDist);
			}
	
			else{
				checkNumberOfValues=false;
			}
		}
		}
		

		if(textFieldNumberOfWorkers.isVisible() )
		{
		boolean checkNumberOfWorkers=true;
		
		while(checkNumberOfWorkers){
	
			String dist=textFieldNumberOfWorkers.getText();
			boolean validateDist=dist.matches("(\\d)+");
			if(!validateDist){	
				String	newDist=  JOptionPane.showInputDialog(null,"Insert a number","Number Format Error", 0);
				textFieldNumberOfWorkers.setText(newDist);
			}
	
			else{
				checkNumberOfWorkers=false;
			}
		}
		}
		
		if(textFieldRuns.isVisible())
		{
		boolean checkRuns=true;
		
		while(checkRuns){
	
			String dist=textFieldRuns.getText();
			boolean validateDist=dist.matches("(\\d)+");
			if(!validateDist){	
				String	newDist=  JOptionPane.showInputDialog(null,"Insert a number","Number Format Error", 0);
				textFieldRuns.setText(newDist);
			}
	
			else{
				checkRuns=false;
			}
		}
		}
	}
	private void loadDistribution()
	{
		jComboBoxDistribution.setModel(new DefaultComboBoxModel(DistributionType.values()));
	}

	private void setModifyControlEnable(boolean flag) {
		// TODO Auto-generated method stub
		rdbtnFixed.setEnabled(flag);
		rdbtnRange.setEnabled(flag);
		rdbtnByDistribution.setEnabled(flag);
		rdbtnByvalues.setEnabled(flag);
		btnModify.setEnabled(flag);
		btnCancel.setEnabled(flag);
		textFieldRuns.setEnabled(flag);
		
		textFieldNumberOfWorkers.setEnabled(flag);
		checkBoxLoadBalancing.setEnabled(flag);

	}

	private void setRangeControlVisibility(boolean flag) {
		lblStartValue.setVisible(flag);
		lblEndValue.setVisible(flag);
		lblIncrement.setVisible(flag);
		textFieldStartValue.setVisible(flag);
		textFieldEndValue.setVisible(flag);
		textFieldIncrement.setVisible(flag);

	}
	
	private void setListControlvisibility(boolean flag)
	{
		lblCommaSeparatedList.setVisible(flag);
		textFieldList.setVisible(flag);
	}
	
	private void setDistributionComboBoxVisibility(boolean flag)
	{
		jComboBoxDistribution.setVisible(flag);
		lblDistribution.setVisible(flag);
	}
	
	private void setDistributionControlVisibility(DistributionType dist)
	{
		switch (dist) 
		{
		case uniform:
			lblA.setText("a:");
			lblB.setText("b:");
			lblA.setVisible(true);
			lblB.setVisible(true);
			textFieldA.setVisible(true);
			textFieldB.setVisible(true);
			lblOfValues.setVisible(true);
			textFieldNumberOfValues.setVisible(true);
		break;
		case exponential:
			lblA.setText("lambda:");
			lblA.setVisible(true);
			textFieldA.setVisible(true);
			lblOfValues.setVisible(true);
			textFieldNumberOfValues.setVisible(true);
			break;
		case normal:
			lblA.setText("mean:");
			lblB.setText("standard deviation:");
			lblA.setVisible(true);
			lblB.setVisible(true);
			textFieldA.setVisible(true);
			textFieldB.setVisible(true);
			lblOfValues.setVisible(true);
			textFieldNumberOfValues.setVisible(true);
			break;
		default:
			lblA.setVisible(false);
			lblB.setVisible(false);
			textFieldA.setVisible(false);
			textFieldB.setVisible(false);
			lblOfValues.setVisible(false);
			textFieldNumberOfValues.setVisible(false);
			break;
		}
		
	}

	private void setFixedControlVisibility(boolean flag) {
		lblValue.setVisible(flag);
		textFieldValue.setVisible(flag);

	}

	private static void createXML(String saveFile) {
		XStream xstream = new XStream(new DomDriver("UTF-8"));

		xstream.processAnnotations(Batch.class);
		xstream.processAnnotations(Param.class);
		xstream.processAnnotations(ParamRange.class);
		xstream.processAnnotations(ParamFixed.class);
		xstream.processAnnotations(ParamDistributionExponential.class);
		xstream.processAnnotations(ParamDistributionNormal.class);
		xstream.processAnnotations(ParamDistributionUniform.class);
		xstream.processAnnotations(ParamList.class);

		ArrayList<Param> paramSim = new ArrayList<Param>();
		for (int i = 0; i < simParams.getChildCount(); i++) {

			// if(top.getChildAt(i) instanceof ParamFixed)
			// {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) simParams
					.getChildAt(i);
			paramSim.add((Param) node.getUserObject());
			// }

		}
		
		ArrayList<Param> paramGen = new ArrayList<Param>();
		for (int i = 0; i < generalParams.getChildCount(); i++) {

			// if(top.getChildAt(i) instanceof ParamFixed)
			// {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) generalParams
					.getChildAt(i);
			paramGen.add((Param) node.getUserObject());
			// }

		}

		int numOfWorker = Integer.parseInt(textFieldNumberOfWorkers.getText());
		boolean balanced =  checkBoxLoadBalancing.isSelected();
		Batch b = new Batch(paramSim, paramGen, simulationFile.getName(),numOfWorker,balanced);
		String xml = xstream.toXML(b);

		try {
			FileWriter fstream = new FileWriter(saveFile
					);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
			xstream.toXML(b, out);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// FileOutputStream fs = new
		// FileOutputStream("generated_simulation.xml");

		// xstream.toXML(b, fs);

	}

	private void createNodes(DefaultMutableTreeNode top,
			ArrayList<Param> paramList) {

		//generalParams = new DefaultMutableTreeNode("General Params");
		//simParams = new DefaultMutableTreeNode("Simulation Params");
		
		top.add(generalParams);
		top.add(simParams);

		generalParams.add(new DefaultMutableTreeNode(new ParamFixed("Width","int",1,"100")));
		generalParams.add(new DefaultMutableTreeNode(new ParamFixed("Height","int",1,"100")));
		generalParams.add(new DefaultMutableTreeNode(new ParamFixed("MaxDistance","int",1,"100")));
		generalParams.add(new DefaultMutableTreeNode(new ParamFixed("NumAgents","int",1,"15")));
		generalParams.add(new DefaultMutableTreeNode(new ParamFixed("Rows","int",1,"1")));
		generalParams.add(new DefaultMutableTreeNode(new ParamFixed("Columns","int",1,"2")));
		generalParams.add(new DefaultMutableTreeNode(new ParamFixed("MaxStep","long",1,"1000")));
		
		for (Param param : paramList) {

			//p = new DefaultMutableTreeNode(param);
			// p.add(new DefaultMutableTreeNode(param));
			simParams.add(new DefaultMutableTreeNode(param));
		}

	}

	private int getTotTests() {
		int tot = 1;
		for (int i = 0; i < simParams.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) simParams
					.getChildAt(i);
			if (child.getUserObject() instanceof ParamFixed)
				tot *= 1;
			if (child.getUserObject() instanceof ParamRange) {
				ParamRange p = ((ParamRange) child.getUserObject());
				
				tot *= (((p.getEnd().contains(".") ? Double.parseDouble(p.getEnd()):Integer.parseInt(p.getEnd())) - 
						(p.getStart().contains(".") ? Double.parseDouble(p.getStart()):Integer.parseInt(p.getStart())))
						/(p.getIncrement().contains(".") ? Double.parseDouble(p.getIncrement()):Integer.parseInt(p.getIncrement()))) + 1;
			}
			if (child.getUserObject() instanceof ParamDistribution) {
				ParamDistribution p = ((ParamDistribution) child.getUserObject());
				tot *= p.getNumberOfValues();
			}
			if (child.getUserObject() instanceof ParamList) {
				ParamList p = ((ParamList) child.getUserObject());
				tot *= p.getValues().size();
			}
		}
		
		for (int i = 0; i < generalParams.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) generalParams
					.getChildAt(i);
			if (child.getUserObject() instanceof ParamFixed)
				tot *= 1;
			if (child.getUserObject() instanceof ParamRange) {
				ParamRange p = ((ParamRange) child.getUserObject()); 
				tot *= (((p.getEnd().contains(".") ? Double.parseDouble(p.getEnd()):Integer.parseInt(p.getEnd())) - 
						(p.getStart().contains(".") ? Double.parseDouble(p.getStart()):Integer.parseInt(p.getStart())))
						/(p.getIncrement().contains(".") ? Double.parseDouble(p.getIncrement()):Integer.parseInt(p.getIncrement()))) + 1;
			}
			
			if (child.getUserObject() instanceof ParamDistribution) {
				ParamDistribution p = ((ParamDistribution) child.getUserObject());
				tot *= p.getNumberOfValues();
			}
			if (child.getUserObject() instanceof ParamList) {
				ParamList p = ((ParamList) child.getUserObject());
				tot *= p.getValues().size();
			}
		}

		return tot;
	}

	public File showFileChooser() {
		JFileChooser fileChooser = new JFileChooser();
		// fileChooser.setCurrentDirectory(new File(FTP_HOME));
		int n = fileChooser.showOpenDialog(BatchWizard.this);
		if (n == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile();
		} else
			return null;
	}
	
	public File SaveFileChooser() {
		JFileChooser fileChooser = new JFileChooser();
		// fileChooser.setCurrentDirectory(new File(FTP_HOME));
		fileChooser.setSelectedFile(new File(FilenameUtils.removeExtension(simulationFile.getName())
				+ "_" + configFileName));
		int n = fileChooser.showSaveDialog(BatchWizard.this);
		if (n == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile();
		} else
			return null;
	}

	private ArrayList<Param> loadParams() {

		suggestion = new HashMap<String,ValueAnnotation>();
		
		URL url;
		try {
			url = new URL("file:" + simulationFile.getAbsolutePath());

			JarClassLoader cl = new JarClassLoader(url);

			cl.addToClassPath();

			String main = cl.getMainClassName();

			Class c = cl.loadClass(main);

			Object instance = Class.forName(main).newInstance();

			Field[] fields = c.getDeclaredFields();

			ArrayList<Param> paramList = new ArrayList<Param>();

			for (Field field : fields) {
				field.setAccessible(true);
				if (field.isAnnotationPresent(BatchAnnotation.class)) 
				{
					ParamFixed pf = new ParamFixed(field.getName(), field
							.getType().toString(), 1, field.get(instance)
							.toString());
					paramList.add(pf);
					
					BatchAnnotation ann = field.getAnnotation(BatchAnnotation.class);	
		
					suggestion.put(field.getName(),new ValueAnnotation(ann.domain(), ann.suggestedValue()));
				}
			}

			return paramList;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}
	
	private boolean isThinSimulation(File simFile) {

		URL url;
		Class c;
		Object instance;
		try {
					
				url = new URL("file:" + simFile.getAbsolutePath());
	
				JarClassLoader cl = new JarClassLoader(url);
	
				cl.addToClassPath();
	
				String main = cl.getMainClassName();

				c = cl.loadClass(main);
			
			return c.isAnnotationPresent(ThinAnnotation.class);
	
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;

	}
}
