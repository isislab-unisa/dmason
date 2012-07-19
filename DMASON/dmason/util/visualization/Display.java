package dmason.util.visualization;

/* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * NOTE: This class is being updated by Luca Vicidomini. It replaces
 * the previous Display class and introduces new controls and
 * inspectors. If you need the old version of this class, please
 * retrieve it using SVN (look for the last revision committed by
 * Francesco Raia). This version may not include every single function
 * implemented by the old one, so beware!
 * it's in an early stage of progress, you know :)
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 */

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;

import org.apache.kahadb.util.ByteArrayInputStream;

import sim.display.Console;
import sim.display.GUIState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.portrayal.Inspector;
import sim.util.Properties;

import dmason.sim.engine.DistributedState;
import dmason.sim.field.continuous.DContinuous2DFactory;
import dmason.sim.portrayal.DistributedInspector;
import dmason.sim.util.DistributedProperties;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;

/**
 * The visualization and inspection GUI for the Global Viewer.
 * @author unascribed 
 * @author Luca Vicidomini
 *
 */
public class Display extends GUIState
{

	/**
	 * 
	 * @author unascribed
	 *
	 */
	class CellProperties
	{
		public String id;
		public int xu;
		public int yu;
		public int xd;
		public int yd;

		public CellProperties(String id, int xu, int yu, int xd, int yd)
		{
			this.id = id;
			this.xu = xu;
			this.yu = yu;
			this.xd = xd;
			this.yd = yd;
		}	

		public boolean isMine(int x, int y)
		{
			return (x >= xu) && (y >= yu) && (x < xd) && (y < yd);
		}
	}
	
	/**
	 * Updates the global image.
	 * @author unascribed
	 *
	 */
	class Viewer extends Thread
	{
		private boolean isActive = true;
		
		public void run()
		{
			// Will contain the list of cell from which we will take data
			String[] activeCells = new String[numCells];
			
			actualSnap = new BufferedImage(100, 100, BufferedImage.TYPE_3BYTE_BGR);

			while(isActive)
			{
				if (isStarted) 
				{
					try 
					{	
						HashMap<String,Object> snaps = (HashMap<String,Object>)updates.getUpdates(step, numCells);
						BufferedImage tmpImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
						String comboCell = "ALL";//(String)comboBoxCell.getSelectedItem();
						
						// Used for drawing
						int cellsPerDimension = fieldMode == DContinuous2DFactory.HORIZONTAL_DISTRIBUTION_MODE ? numCells : (int)Math.sqrt(numCells);
						
						// Overwrite current list of active cells
						short activeCellsI = 0;
						
						// // //
						// // //
						// NOTE: The creation of activeCells array should be moved
						//       in a more efficient position. It would be better
						//       to build the array once every time the user
						//       changes the comboBoxCell item. However, this
						//       combobox isn't available at current stage
						//       of development...
						// // //
						// // //
						if (!comboCell.equalsIgnoreCase("ALL"))
						{
							activeCells[activeCellsI++] = comboCell;
						}
						else if (fieldMode == DContinuous2DFactory.HORIZONTAL_DISTRIBUTION_MODE)
						{
							for (int j = 0; j < numCells; j++)
								activeCells[activeCellsI++] = "0-" + j;
						}
						else // Square
						{
							for (int i = 0; i < Math.sqrt(numCells); i++)
								for (int j = 0; j < Math.sqrt(numCells); j++)
									activeCells[activeCellsI++] = i + "-" + j;
						}
						
						// Retrieve some common info
						RemoteSnap zeroSnap = (RemoteSnap)snaps.get("0-0");
						time = zeroSnap.time;
						int numProps = zeroSnap.stats.size();
						Object[] propNames = zeroSnap.stats.keySet().toArray();
						Object[][] props = new Object[numProps][activeCells.length];
						
						for (activeCellsI = 0; activeCellsI < activeCells.length; activeCellsI++)
						{
							RemoteSnap snap = (RemoteSnap)snaps.get(activeCells[activeCellsI]);
							
							try
							{
								short i = snap.i;
								short j = snap.j;
								byte[] array = snap.image;
								if (array != null)
								{
									BufferedImage image = ImageIO.read(new ByteArrayInputStream(array));
									tmpImage.createGraphics().drawImage(image, 
											(width  / cellsPerDimension) * j,
											(height / cellsPerDimension) * i,
											null);
								}
							} catch (Exception e) {
								e.printStackTrace();
								JOptionPane.showMessageDialog(null, "Rendering error :(");
								System.exit(-1);
							}
							
							for (int propertyI = 0; propertyI < numProps; propertyI++)
							{
								props[propertyI][activeCellsI] = snap.stats.get(propNames[propertyI]);
							}
						}
						
						// Update the global image
						lblStepValue.setText(step + "");
						actualSnap = tmpImage;
						imageView.repaint();
						step++;
						
						// Update the simulation object
						Display.this.state.schedule.steps = step;
						Display.this.state.schedule.time = time;
						
						Class<?> simClass = Display.this.state.getClass();
						for (int propertyI = 0; propertyI < numProps; propertyI++)
						{
							String propName = (String)propNames[propertyI];
							try
							{
								Method m = simClass.getMethod("reduce" + propName, Object[].class);
								m.invoke(Display.this.state, new Object[] { props[propertyI] } );
							} catch (Exception e) {		
								e.printStackTrace();
							}
						}
						
						// Update the inspector
						Display.this.modelInspector.updateInspector();
						if (Display.this.updateInspector != null)
						{
							Display.this.updateInspector.updateInspector();
						}
					} 
					catch (InterruptedException e) 
					{
						e.printStackTrace();
					}
				}
				else // !started
				{
					lock.lock();
					try
					{
						sin.await();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					lock.unlock();
				}
			}

		}
		
		public void setAlive()
		{
			isActive = false;
		}
	}
	
	//////////////////////////////////////////////////////////////////
	
	private static final long serialVersionUID = 1L;
	
	private ConnectionNFieldsWithActiveMQAPI connection;
	private int fieldMode;
	private int numCells;
	private int width;
	private int height;
	private String absolutePath;
	private String simulation;
	private String simulationClassName;
	
	private JFrame frame;
	private JPanel imageView;
	private BufferedImage actualSnap;
	
	public JLabel lblStepValue;
	public JLabel lblXValue;
	public JLabel lblYValue;
	public JPanel pnlInspector; 
	
	public Viewer view;
	public VisualizationUpdateMap<Long, RemoteSnap> updates;
	public ArrayList<CellProperties> listCells;
	public ThreadVisualizationMessageListener thread;
	public ReentrantLock lock = new ReentrantLock();
	public Condition sin = lock.newCondition();
	
	private int zoomWidth;
	private int zoomHeight;
	
	/** Simulation's step */
	public long step;
	
	/** Simulation's time */
	public double time;
	
	public boolean isStarted = false;
	public boolean isPaused = false;
	public boolean isFirstTime = true;
	
	/** List of workers involved in the simulation */
	private ArrayList<String> workerTopics = new ArrayList<String>();
	
	public Steppable updateSteppable = null;
	
	/** Panel containing inspectable simulation properties */
	public DistributedInspector modelInspector;

	protected Inspector updateInspector;
	
	public Display(ConnectionNFieldsWithActiveMQAPI con, int mode,
			int numCells, int width, int height, String absolutePath,
			String simulation, String simulationClassName)
	{
		super(null); // We'll set super.guistate later
		this.connection = con;
		this.fieldMode = mode;
		this.numCells = numCells;
		this.width = width;
		this.height = height;
		this.absolutePath = absolutePath;
		this.simulation = simulation;
		this.simulationClassName = simulationClassName;
		updates = new VisualizationUpdateMap<Long, RemoteSnap>();
		listCells = new ArrayList<CellProperties>();
		
		if(mode == 0)
		{
			this.zoomWidth = (int)(width / numCells);
			this.zoomHeight = height;
			
			for(int i = 0; i < numCells; i++) 
			{
				listCells.add(new CellProperties("0-" + i, 
						i * zoomWidth,
						0,
						i * zoomWidth + zoomWidth,
						zoomHeight));
			}
		}
		else
		{
			this.zoomWidth = (int)(width/Math.sqrt(numCells));
			this.zoomHeight = (int)(height/Math.sqrt(numCells));
			
			for (int i = 0; i < Math.sqrt(numCells); i++)
			{
				for (int j = 0; j < Math.sqrt(numCells); j++)
				{
					listCells.add(new CellProperties(i + "-" + j, 
							j * zoomWidth,
							i * zoomHeight,
							j * zoomWidth + zoomWidth,
							i * zoomHeight + zoomHeight));
				}
			}
		}		
		
		try {
			con.createTopic("GRAPHICS",1);
			con.subscribeToTopic("GRAPHICS");
			con.publishToTopic("ENTER", "GRAPHICS", "GRAPHICS");
			
			// Save the list of active workers
			// Needed for (un-)trace commands
			for(String topicName : con.getTopicList())
			{
				if(topicName.startsWith("SERVICE"))
				{
					workerTopics.add(topicName);
					con.createTopic(topicName, 1);
					System.out.println(topicName);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		thread = new ThreadVisualizationMessageListener(connection, this);
		thread.start();
		
		initComponents();
		frame.setVisible(true);
		
		view = new Viewer();
		view.start();
	}


	private void initComponents()
	{
		// FRAME > TOP ROW
		JLabel lblStepCaption = new JLabel("Step: ");
		lblStepValue = new JLabel("-");
		
		JPanel pnlTop = new JPanel();
		pnlTop.setLayout(new FlowLayout(FlowLayout.LEFT));
		pnlTop.add(lblStepCaption);
		pnlTop.add(lblStepValue);
				
		// BOTTOM OF THE GRAPHIC PANEL
		JLabel lblXCaption = new JLabel("X:");
		lblXValue = new JLabel("-");
		lblXValue.setHorizontalAlignment(SwingConstants.RIGHT);
		JLabel lblYCaption = new JLabel("Y:");
		lblYValue = new JLabel("-");
		lblYValue.setHorizontalAlignment(SwingConstants.RIGHT);
		JButton btnSnapshot = new JButton("Save snapshot");
		btnSnapshot.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { onSnapshotClick(e); } });
		
		Dimension labelSize = lblXValue.getPreferredSize();
		labelSize.width *= 15;
		lblXValue.setPreferredSize(labelSize);
		lblXValue.setMinimumSize(labelSize);
		lblYValue.setPreferredSize(labelSize);
		lblYValue.setMinimumSize(labelSize);
		
		JPanel pnlGraphicsControlsLabels = new JPanel();
		pnlGraphicsControlsLabels.setLayout(new FlowLayout(FlowLayout.LEFT));
		pnlGraphicsControlsLabels.add(lblXCaption);
		pnlGraphicsControlsLabels.add(lblXValue);
		pnlGraphicsControlsLabels.add(new JSeparator(SwingConstants.VERTICAL));
		pnlGraphicsControlsLabels.add(lblYCaption);
		pnlGraphicsControlsLabels.add(lblYValue);
		
		JPanel pnlGraphicControls = new JPanel();
		pnlGraphicControls.setLayout(new BorderLayout());
		pnlGraphicControls.add(pnlGraphicsControlsLabels, BorderLayout.CENTER);
		pnlGraphicControls.add(btnSnapshot, BorderLayout.EAST);
		
		// FRAME > CENTER > GRAPHICS (LEFT) > CENTER
		imageView = new JPanel() {
			private static final long serialVersionUID = 1L;	
			public void paintComponent(Graphics g){	super.paintComponent(g); g.drawImage(actualSnap, 0, 0, null); }
		};
		imageView.addMouseMotionListener(new MouseMotionListener() {
			@Override public void mouseMoved(MouseEvent e) { onImageViewMouseMoved(e); }
			@Override public void mouseDragged(MouseEvent e) { }
		});
		
		// FRAME > CENTER > GRAPHICS (LEFT)
		JPanel pnlGraphic = new JPanel();
		pnlGraphic.setLayout(new BorderLayout());
		pnlGraphic.add(imageView, BorderLayout.CENTER);
		pnlGraphic.add(pnlGraphicControls, BorderLayout.SOUTH);
		
		// FRAME > CENTER > INSPECTORS (RIGHT)
		JPanel pnlInspector = new JPanel();
		pnlInspector.setLayout(new BorderLayout());
		
		try
		{
			/*
			 * Note: we want to use the Properties.getProperties method
			 * from MASON. The problem is that it takes as INSTANCE
			 * of the simulation object (and not the CLASS DEFINITION).
			 * So the trick is to manually create a new instance of the
			 * simulation object. The problem is that we need to avoid
			 * an actual connection between the 'fake' simulation instance
			 * and the ActiveMQ server. This will cause some exception
			 * we will need to catch, hiding them to the user.
			 * Another solution may be to create a dedicate constructor
			 * for this goal.
			 */
			Class<?> simClass = Class.forName(simulationClassName);
			Constructor<?> constructor = simClass.getConstructor();
			Object simObj = constructor.newInstance(new Object[] { });
			super.state = (DistributedState)simObj;
			this.modelInspector = new DistributedInspector(new DistributedProperties(state), this);
			pnlInspector.add(new JScrollPane(modelInspector), BorderLayout.CENTER); 
		} catch (Exception e) {
			pnlInspector.add(new JLabel("Could not find class " + simulationClassName + ". Inspector not available."));
		}
		
		/* A controller field for GUISTATE is needed. Let's try this... */
		this.controller = new Console(this) {
			public void registerInspector(Inspector inspector, Stoppable stopper) {
				Display.this.updateSteppable = inspector.getUpdateSteppable();
				Display.this.updateInspector = inspector;
				super.registerInspector(inspector, stopper);
			};
		};
		/* End of controller generation */

		// FRAME > CENTER
		JSplitPane splGraphic_Inspector = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splGraphic_Inspector.setLeftComponent(pnlGraphic);
		splGraphic_Inspector.setRightComponent(pnlInspector);
		splGraphic_Inspector.setOneTouchExpandable(true);
		
		// FRAME
		frame = new JFrame();
		frame.setLayout(new BorderLayout());
		frame.add(pnlTop, BorderLayout.NORTH);
		frame.add(splGraphic_Inspector, BorderLayout.CENTER);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { onWindowClosing(); } });
	}

	/**
	 * Action executed when the user moves the mouse on the global image.
	 * @param evt
	 */
	private void onImageViewMouseMoved(MouseEvent evt)
	{
		lblXValue.setText("" + evt.getX());
		lblYValue.setText("" + evt.getY());		
	}
	
	/**
	 * Event listener for the snapshot button.
	 * @param evt
	 */
	private void onSnapshotClick(ActionEvent evt)
	{
		// Generate a filename based on current date and time
		GregorianCalendar gc = new GregorianCalendar();
		String filename = gc.get(Calendar.YEAR) + "-" + gc.get(Calendar.MONTH)
				+ "-" + gc.get(Calendar.DAY_OF_MONTH) + gc.get(Calendar.HOUR)
				+ "." + gc.get(Calendar.MINUTE) + "." + gc.get(Calendar.SECOND);
		String filepath = System.getProperty("user.dir") + "/snap_" + filename + ".png";
		
		try
		{
			File outputfile = new File(filepath);
		    ImageIO.write(actualSnap, "png", outputfile);
	
		    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
		    	    null,
		    	    "Snapshot saved as " + filename + "\n" + "Do you want to open this snapshot now?",
		    	    "Open snapshot",
		    	    JOptionPane.YES_NO_OPTION))
			{				
				Desktop dt = Desktop.getDesktop();
			    dt.open(outputfile);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Can't save the snapshot to:" + "\n"
					+ filepath + "\n"
					+ "Please ensure you have write permission on the directory and there is enough free space.");
			e.printStackTrace();
		}
	}

	/**
	 * Action executed when the user tries to close the window.
	 */
	protected void onWindowClosing()
	{
		try {
			connection.publishToTopic("EXIT", "GRAPHICS", "GRAPHICS");
			JOptionPane.showMessageDialog(null, "Successfully disconnected!");
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(null, "Disconnection failed: this may cause problems in future views!");
			e1.printStackTrace();
		}
		System.exit(0);
	}
	
	public void sblock()
	{
		lock.lock();
		sin.signal();
		lock.unlock();
	}
	
	public void addSnapShot(RemoteSnap remSnap)
	{
		updates.put(remSnap.step, remSnap);
	}

	public void close()
	{
		view.setAlive();
		frame.dispose();
	}
	
	/**
	 * Send a message to workers to start tracing a parameter
	 * @param what The parameter to trace
	 */
	public void startTracing(String what)
	{
		try {
			for(String topicName : workerTopics)
				connection.publishToTopic(what, topicName, "trace");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Send a message to workers to stop tracing a parameter
	 * @param what The parameter to stop tracing
	 */
	public void stopTracing(String what)
	{
		try {
			for(String topicName : workerTopics)
				connection.publishToTopic(what, topicName, "untrace");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}
