/**
 * Copyright 2012 Università degli Studi di Salerno
 

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

package dmason.batch;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
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

import dmason.annotation.batch;
import dmason.batch.data.Batch;
import dmason.batch.data.Param;
import dmason.batch.data.ParamFixed;
import dmason.batch.data.ParamRange;
import dmason.sim.field.grid.DSparseGrid2DFactory;
import dmason.util.SystemManagement.JarClassLoader;

public class BatchWizard extends JFrame {

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
	private static DefaultMutableTreeNode generalParams;
	private static DefaultMutableTreeNode simParams;
	
	private String paramType;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
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
		setTitle("Batch wizard");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 716, 477);
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
		gl_panel.setHorizontalGroup(gl_panel
				.createParallelGroup(Alignment.TRAILING)
				.addComponent(panel_3, GroupLayout.DEFAULT_SIZE, 690,
						Short.MAX_VALUE)
				.addGroup(
						Alignment.LEADING,
						gl_panel.createSequentialGroup()
								.addContainerGap()
								.addGroup(
										gl_panel.createParallelGroup(
												Alignment.LEADING)
												.addGroup(
														gl_panel.createSequentialGroup()
																.addGap(10)
																.addComponent(
																		panel_4,
																		GroupLayout.DEFAULT_SIZE,
																		676,
																		Short.MAX_VALUE)
																.addContainerGap())
												.addGroup(
														gl_panel.createSequentialGroup()
																.addComponent(
																		panel_1,
																		GroupLayout.DEFAULT_SIZE,
																		282,
																		Short.MAX_VALUE)
																.addPreferredGap(
																		ComponentPlacement.RELATED)
																.addComponent(
																		panel_2,
																		GroupLayout.DEFAULT_SIZE,
																		388,
																		Short.MAX_VALUE)
																.addGap(4)))));
		gl_panel.setVerticalGroup(gl_panel
				.createParallelGroup(Alignment.TRAILING)
				.addGroup(
						gl_panel.createSequentialGroup()
								.addComponent(panel_3,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addGroup(
										gl_panel.createParallelGroup(
												Alignment.BASELINE)
												.addComponent(
														panel_1,
														GroupLayout.PREFERRED_SIZE,
														319,
														GroupLayout.PREFERRED_SIZE)
												.addComponent(
														panel_2,
														GroupLayout.PREFERRED_SIZE,
														321,
														GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(panel_4,
										GroupLayout.PREFERRED_SIZE, 38,
										GroupLayout.PREFERRED_SIZE).addGap(6)));

		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				createXML();
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

		top = new DefaultMutableTreeNode("Simulation Params");

		JScrollPane scrollPaneTree = new JScrollPane();

		GroupLayout gl_panel_2 = new GroupLayout(panel_2);
		gl_panel_2.setHorizontalGroup(gl_panel_2.createParallelGroup(
				Alignment.TRAILING).addGroup(
				Alignment.LEADING,
				gl_panel_2
						.createSequentialGroup()
						.addContainerGap()
						.addComponent(scrollPaneTree, GroupLayout.DEFAULT_SIZE,
								361, Short.MAX_VALUE).addContainerGap()));
		gl_panel_2.setVerticalGroup(gl_panel_2.createParallelGroup(
				Alignment.LEADING).addGroup(
				gl_panel_2
						.createSequentialGroup()
						.addContainerGap()
						.addComponent(scrollPaneTree,
								GroupLayout.PREFERRED_SIZE, 271,
								GroupLayout.PREFERRED_SIZE)
						.addContainerGap(67, Short.MAX_VALUE)));

		final JTree treeParams = new JTree(top);
		scrollPaneTree.setViewportView(treeParams);
		treeParams.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent selected) {
				// DefaultMutableTreeNode parent =
				// selected.getPath().getParentPath()

				DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeParams
						.getLastSelectedPathComponent();
				if (node.getParent() == simParams || node.getParent() == generalParams) {

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
						System.out.println("fixed");
						ParamFixed pf = (ParamFixed) selectedParam;
						lblParamType.setText(pf.getName() + ": " + pf.getType());
						textFieldRuns.setText("" + pf.getRuns());
						textFieldValue.setText(pf.getValue());
						rdbtnFixed.doClick();

						lblMessage.setVisible(false);
						setModifyControlEnable(true);

					}

					if (selectedParam instanceof ParamRange) {
						System.out.println("Range");	
						ParamRange pf = (ParamRange) selectedParam;
						lblParamType.setText(pf.getName() + ": " + pf.getType());
						textFieldRuns.setText("" + pf.getRuns());
						textFieldStartValue.setText(pf.getStart());
						textFieldEndValue.setText(pf.getEnd());
						textFieldIncrement.setText(pf.getIncrement());
						rdbtnRange.doClick();

						lblMessage.setVisible(false);
						setModifyControlEnable(true);
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
			public void actionPerformed(ActionEvent e) {
				ArrayList<Param> params = loadParams();
				if (params != null) {
					createNodes(top, params);
					treeParams.expandPath(new TreePath(top.getPath()));
					treeParams.expandPath(new TreePath(simParams.getPath()));
					treeParams.expandPath(new TreePath(generalParams.getPath()));
				}

				lblTotTests.setText(totTestsMessage + " " + getTotTests());

			}
		});

		JButton bntChooseSimulation = new JButton();
		bntChooseSimulation.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {

				simulationFile = showFileChooser();
				if (simulationFile != null) {
					textFieldSimJarPath.setText(simulationFile
							.getAbsolutePath());
					btnLoadParams.setEnabled(true);
				}
			}

		});
		bntChooseSimulation.setIcon(new ImageIcon(BatchWizard.class
				.getResource("/dmason/resource/image/openFolder.png")));
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

		JLabel lblParameterSpace = new JLabel("Parameter Space");
		lblParameterSpace.setFont(new Font("Tahoma", Font.BOLD, 11));

		lblValue = new JLabel("Value:");

		textFieldValue = new JTextField();
		textFieldValue.setColumns(10);

		lblStartValue = new JLabel("Start value:");

		textFieldStartValue = new JTextField();
		textFieldStartValue.setColumns(10);

		lblEndValue = new JLabel("End value:");

		textFieldEndValue = new JTextField();
		textFieldEndValue.setColumns(10);

		lblIncrement = new JLabel("Increment:");

		textFieldIncrement = new JTextField();
		textFieldIncrement.setColumns(10);

		rdbtnFixed = new JRadioButton("Fixed");
		rdbtnFixed.setSelected(true);
		rdbtnFixed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				setRangeControlVisibility(false);
				setFixedControlVisibility(true);
			}

		});

		rdbtnRange = new JRadioButton("Range");
		rdbtnRange.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setRangeControlVisibility(true);
				setFixedControlVisibility(false);
			}
		});
		// Group the radio buttons.
		ButtonGroup group = new ButtonGroup();
		group.add(rdbtnFixed);
		group.add(rdbtnRange);

		setRangeControlVisibility(false);
		setFixedControlVisibility(false);

		lblMessage = new JLabel(message);

		btnModify = new JButton("Modify");
		btnModify.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {

				if (rdbtnFixed.isSelected()) {
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
				} else {
					System.out.println("ok");
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
				lblMessage.setVisible(true);
				setModifyControlEnable(false);

				int tot = getTotTests();
				if (tot >= testAlertThreshold)
					lblTotTests.setForeground(Color.RED);
				else
					lblTotTests.setForeground(Color.BLACK);

				lblTotTests.setText(totTestsMessage + " " + getTotTests());

			}
		});

		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				lblMessage.setVisible(true);
				setModifyControlEnable(false);

			}
		});

		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1
				.setHorizontalGroup(gl_panel_1
						.createParallelGroup(Alignment.TRAILING)
						.addGroup(
								gl_panel_1
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												gl_panel_1
														.createParallelGroup(
																Alignment.LEADING)
														.addComponent(
																lblParamType)
														.addGroup(
																gl_panel_1
																		.createSequentialGroup()
																		.addComponent(
																				lblRuns)
																		.addPreferredGap(
																				ComponentPlacement.RELATED)
																		.addComponent(
																				textFieldRuns,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.DEFAULT_SIZE,
																				GroupLayout.PREFERRED_SIZE))
														.addComponent(
																lblParameterSpace)
														.addComponent(
																rdbtnFixed)
														.addComponent(
																rdbtnRange)
														.addGroup(
																gl_panel_1
																		.createSequentialGroup()
																		.addComponent(
																				lblValue)
																		.addPreferredGap(
																				ComponentPlacement.RELATED)
																		.addComponent(
																				textFieldValue,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.DEFAULT_SIZE,
																				GroupLayout.PREFERRED_SIZE))
														.addGroup(
																gl_panel_1
																		.createSequentialGroup()
																		.addComponent(
																				lblStartValue)
																		.addPreferredGap(
																				ComponentPlacement.RELATED)
																		.addComponent(
																				textFieldStartValue,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.DEFAULT_SIZE,
																				GroupLayout.PREFERRED_SIZE))
														.addGroup(
																gl_panel_1
																		.createSequentialGroup()
																		.addComponent(
																				lblEndValue)
																		.addPreferredGap(
																				ComponentPlacement.UNRELATED)
																		.addComponent(
																				textFieldEndValue,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.DEFAULT_SIZE,
																				GroupLayout.PREFERRED_SIZE))
														.addComponent(
																lblMessage)
														.addGroup(
																gl_panel_1
																		.createSequentialGroup()
																		.addComponent(
																				lblIncrement)
																		.addPreferredGap(
																				ComponentPlacement.UNRELATED)
																		.addComponent(
																				textFieldIncrement,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.DEFAULT_SIZE,
																				GroupLayout.PREFERRED_SIZE)))
										.addContainerGap(111, Short.MAX_VALUE))
						.addGroup(
								gl_panel_1
										.createSequentialGroup()
										.addContainerGap(134, Short.MAX_VALUE)
										.addComponent(btnModify)
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addComponent(btnCancel)
										.addContainerGap()));
		gl_panel_1
				.setVerticalGroup(gl_panel_1
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								gl_panel_1
										.createSequentialGroup()
										.addGap(4)
										.addComponent(lblMessage)
										.addGap(18)
										.addComponent(lblParamType)
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addGroup(
												gl_panel_1
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(lblRuns)
														.addComponent(
																textFieldRuns,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addGap(18)
										.addComponent(lblParameterSpace)
										.addPreferredGap(
												ComponentPlacement.UNRELATED)
										.addComponent(rdbtnFixed)
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addComponent(rdbtnRange)
										.addPreferredGap(
												ComponentPlacement.UNRELATED)
										.addGroup(
												gl_panel_1
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(lblValue)
														.addComponent(
																textFieldValue,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addGroup(
												gl_panel_1
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																lblStartValue)
														.addComponent(
																textFieldStartValue,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addGroup(
												gl_panel_1
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																lblEndValue)
														.addComponent(
																textFieldEndValue,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addGroup(
												gl_panel_1
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																lblIncrement)
														.addComponent(
																textFieldIncrement,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addGroup(
												gl_panel_1
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(btnModify)
														.addComponent(btnCancel))
										.addContainerGap(51, Short.MAX_VALUE)));
		panel_1.setLayout(gl_panel_1);
		panel.setLayout(gl_panel);

		setModifyControlEnable(false);
	}

	private void setModifyControlEnable(boolean flag) {
		// TODO Auto-generated method stub
		rdbtnFixed.setEnabled(flag);
		rdbtnRange.setEnabled(flag);
		btnModify.setEnabled(flag);
		btnCancel.setEnabled(flag);
		textFieldRuns.setEnabled(flag);

	}

	private void setRangeControlVisibility(boolean flag) {
		lblStartValue.setVisible(flag);
		lblEndValue.setVisible(flag);
		lblIncrement.setVisible(flag);
		textFieldStartValue.setVisible(flag);
		textFieldEndValue.setVisible(flag);
		textFieldIncrement.setVisible(flag);

	}

	private void setFixedControlVisibility(boolean flag) {
		lblValue.setVisible(flag);
		textFieldValue.setVisible(flag);

	}

	private static void createXML() {
		XStream xstream = new XStream(new DomDriver("UTF-8"));

		xstream.processAnnotations(Batch.class);
		xstream.processAnnotations(Param.class);
		xstream.processAnnotations(ParamRange.class);
		xstream.processAnnotations(ParamFixed.class);

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

		Batch b = new Batch(paramSim, paramGen, simulationFile.getName());
		String xml = xstream.toXML(b);
		System.out.println(xml);

		try {
			FileWriter fstream = new FileWriter(
					FilenameUtils.removeExtension(simulationFile.getName())
							+ "_" + configFileName);
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

		generalParams = new DefaultMutableTreeNode("General Params");
		simParams = new DefaultMutableTreeNode("Simulation Params");
		
		top.add(generalParams);
		top.add(simParams);

		generalParams.add(new DefaultMutableTreeNode(new ParamFixed("Width","int",1,"100")));
		generalParams.add(new DefaultMutableTreeNode(new ParamFixed("Height","int",1,"100")));
		generalParams.add(new DefaultMutableTreeNode(new ParamFixed("MaxDistance","int",1,"100")));
		generalParams.add(new DefaultMutableTreeNode(new ParamFixed("NumRegions","int",1,"2")));
		generalParams.add(new DefaultMutableTreeNode(new ParamFixed("NumAgents","int",1,"15")));
		generalParams.add(new DefaultMutableTreeNode(new ParamFixed("Mode","int",1,""+DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE)));
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
				tot *= ((Integer.parseInt(p.getEnd()) - Integer.parseInt(p.getStart()))
						/Integer.parseInt(p.getIncrement())) + 1;
			}
		}
		
		for (int i = 0; i < generalParams.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) generalParams
					.getChildAt(i);
			if (child.getUserObject() instanceof ParamFixed)
				tot *= 1;
			if (child.getUserObject() instanceof ParamRange) {
				ParamRange p = ((ParamRange) child.getUserObject());
				tot *= ((Integer.parseInt(p.getEnd()) - Integer.parseInt(p.getStart()))
						/Integer.parseInt(p.getIncrement())) + 1;
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

	private ArrayList<Param> loadParams() {

		URL url;
		try {
			url = new URL("file:" + simulationFile.getAbsolutePath());

			JarClassLoader cl = new JarClassLoader(url);

			cl.addToClassPath();

			String main = cl.getMainClassName();

			System.out.println("Main: " + main);

			Class c = cl.loadClass(main);

			Object instance = Class.forName(main).newInstance();

			Field[] fields = c.getDeclaredFields();

			ArrayList<Param> paramList = new ArrayList<Param>();

			for (Field field : fields) {
				field.setAccessible(true);
				if (field.isAnnotationPresent(batch.class)) {
					ParamFixed pf = new ParamFixed(field.getName(), field
							.getType().toString(), 1, field.get(instance)
							.toString());
					paramList.add(pf);
					System.out.println("Field: " + field.getName() + " type: "
							+ field.getType() + " value: "
							+ field.get(instance));
				}
			}

			return paramList;
			/*
			 * for (Param param : paramList) {
			 * 
			 * ParamFixed p = (ParamFixed) param;
			 * System.out.println(p.getName()+" "+p.getType()+" "+p.getValue());
			 * }
			 */
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
}
