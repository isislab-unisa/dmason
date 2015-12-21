package it.isislab.dmason.sim.app.SociallyDamagingBehavior.Analysis;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.text.NumberFormat;

import javax.swing.GroupLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeriesCollection;


public class MainFrame extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JFileChooser choose;
	private BuildData data;
	private JFreeChart chartAgents = null;
	private JFreeChart chartActions = null;
	private ChartPanel panelAgents;
	private ChartPanel panelActions;
	
	public MainFrame() {
		initComponents();
		
	}
	
	private JFreeChart makeChartAgents(JFreeChart chart, XYSeriesCollection dataset){
		chart = ChartFactory.createXYLineChart("Percentual Honest Agents", //titolo

				"Step", //label asse delle X

				"Honest Agents", //label asse dell Y

				dataset, // sorgente dei dati

				PlotOrientation.VERTICAL, //orientamento del grafico

				true, // mostra la legenda

				true, //usa i tooltip

				false
				);

		XYPlot plot = (XYPlot) chart.getPlot();

		XYLineAndShapeRenderer renderer =  new XYLineAndShapeRenderer(true, true);

		plot.setRenderer(renderer);

		renderer.setBaseShapesVisible(true);

		renderer.setBaseShapesFilled(true);

		NumberFormat format = NumberFormat.getNumberInstance();

		format.setMaximumFractionDigits(2);

		XYItemLabelGenerator generator =

				new StandardXYItemLabelGenerator(

						StandardXYItemLabelGenerator.DEFAULT_ITEM_LABEL_FORMAT,

						format, format);

		renderer.setBaseItemLabelGenerator(generator);

		renderer.setBaseItemLabelsVisible(true);
		
		return chart;
	}
	
	private JFreeChart makeChartActions(JFreeChart chart, XYSeriesCollection dataset){
		
		chart = ChartFactory.createXYLineChart("Percentual Honest Actions", //titolo

				"Step", //label asse delle X

				"Honest Actions", //label asse dell Y

				dataset, // sorgente dei dati

				PlotOrientation.VERTICAL, //orientamento del grafico

				true, // mostra la legenda

				true, //usa i tooltip

				false
				);
		XYPlot plot = (XYPlot) chart.getPlot();

		XYLineAndShapeRenderer renderer =  new XYLineAndShapeRenderer(true, true);

		plot.setRenderer(renderer);

		renderer.setBaseShapesVisible(true);

		renderer.setBaseShapesFilled(true);

		NumberFormat format = NumberFormat.getNumberInstance();

		format.setMaximumFractionDigits(2);

		XYItemLabelGenerator generator =

				new StandardXYItemLabelGenerator(

						StandardXYItemLabelGenerator.DEFAULT_ITEM_LABEL_FORMAT,

						format, format);

		renderer.setBaseItemLabelGenerator(generator);

		renderer.setBaseItemLabelsVisible(true);
		
		return chart;
	}

	private void menuItemLoadMouseClicked(MouseEvent e) {
		choose.setFileFilter(new FileFilter() {
			
			@Override
			public String getDescription() {
				return "txt";
			}
			
			@Override
			public boolean accept(File arg0) {
				if (arg0.isDirectory()) return true;
				else if(arg0.isFile())
							return arg0.getName().toLowerCase().endsWith(".txt");
				return false;
			}
		});
		choose.setMultiSelectionEnabled(true);
		choose.showOpenDialog(this);
		File[] chosed = choose.getSelectedFiles();
		if (chosed.length>0){
			data = new BuildData(chosed);
			//chart = new MakeChart("Social Damaging Behaviour", data);
			//chart.setVisible(true);
			
			chartAgents = makeChartAgents(chartAgents, data.getDatasetAgents());
			chartActions = makeChartActions(chartActions, data.getDatasetActions());
			
			panelAgents = new ChartPanel(chartAgents);
			panelActions = new ChartPanel(chartActions);
			panelAgents.setVisible(true);
			panelActions.setVisible(true);
			panelAgents.setPreferredSize(new Dimension(950, 580));
			panelActions.setPreferredSize(new Dimension(950, 580));
			panelGraph.add(panelAgents);
			panelGraph.add(panelActions);
			this.repaint();
		}
		
	}

	private void initComponents() {

		menuBar = new JMenuBar();
		menuFile = new JMenu();
		menuItemLoad = new JMenuItem();
		menuExit = new JMenuItem();
		menuHelp = new JMenu();
		menuItemHelp = new JMenuItem();
		menuItemAbout = new JMenuItem();
		scrollPaneGraph = new JScrollPane();
		panelGraph = new JPanel();
		choose = new JFileChooser();

		//======== this ========
		Container contentPane = getContentPane();

		//======== menuBar ========
		{

			//======== menuFile ========
			{
				menuFile.setText("File");

				//---- menuItemLoad ----
				menuItemLoad.setText("Load folder");
				menuItemLoad.addMouseListener(new MouseListener() {
					
					@Override
					public void mouseReleased(MouseEvent e) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void mousePressed(MouseEvent e) {
						menuItemLoadMouseClicked(e);
						
					}
					
					@Override
					public void mouseExited(MouseEvent e) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void mouseEntered(MouseEvent e) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void mouseClicked(MouseEvent e) {
							
						
					}
				});
				menuFile.add(menuItemLoad);

				//---- menuExit ----
				menuExit.setText("Exit");
				menuExit.addMouseListener(new MouseListener() {
					
					@Override
					public void mouseReleased(MouseEvent arg0) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void mousePressed(MouseEvent arg0) {
						System.exit(0);
						
					}
					
					@Override
					public void mouseExited(MouseEvent arg0) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void mouseEntered(MouseEvent arg0) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void mouseClicked(MouseEvent arg0) {
						
					}
				});
				menuFile.add(menuExit);
			}
			menuBar.add(menuFile);

			//======== menuHelp ========
			{
				menuHelp.setText("?");

				//---- menuItemHelp ----
				menuItemHelp.setText("Help");
				menuHelp.add(menuItemHelp);

				//---- menuItemAbout ----
				menuItemAbout.setText("About");
				menuHelp.add(menuItemAbout);
			}
			menuBar.add(menuHelp);
		}
		setJMenuBar(menuBar);

		//======== scrollPaneGraph ========
		{

			//======== panelGraph ========
			{
				FlowLayout panelGraphLayout = new FlowLayout();
				panelGraph.setLayout(panelGraphLayout);		
				panelGraph.setPreferredSize(new Dimension(1000, 1200));
			}
			scrollPaneGraph.setViewportView(panelGraph);
				
		}

		GroupLayout contentPaneLayout = new GroupLayout(contentPane);
		contentPane.setLayout(contentPaneLayout);
		contentPaneLayout.setHorizontalGroup(
			contentPaneLayout.createParallelGroup()
				.addComponent(scrollPaneGraph, GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE)
		);
		contentPaneLayout.setVerticalGroup(
			contentPaneLayout.createParallelGroup()
				.addComponent(scrollPaneGraph, GroupLayout.DEFAULT_SIZE, 1200, Short.MAX_VALUE)
		);
		pack();
		setLocationRelativeTo(getOwner());
	}

	private JMenuBar menuBar;
	private JMenu menuFile;
	private JMenuItem menuItemLoad;
	private JMenuItem menuExit;
	private JMenu menuHelp;
	private JMenuItem menuItemHelp;
	private JMenuItem menuItemAbout;
	private JScrollPane scrollPaneGraph;
	private JPanel panelGraph;
	
	public static void main(String args[]){
		MainFrame f = new MainFrame();
		f.setSize(1100,600);
		f.setVisible(true);

	}
}
