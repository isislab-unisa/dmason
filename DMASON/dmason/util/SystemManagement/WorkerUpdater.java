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

package dmason.util.SystemManagement;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Properties;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;

import javax.swing.WindowConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;



import dmason.util.connection.Address;
import dmason.util.exception.NoDigestFoundException;


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
 * This class manage the Worker Update process
 * @author marvit
 *
 */
public class WorkerUpdater extends javax.swing.JFrame 
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6511653587829803136L;
	
	private JPanel jPanelMain;
	private JProgressBar jProgressBarUpdate;
	private JButton jButtonUpdateWorker;
	private JButton jButtonChoseUpJar;
	private JLabel jLabelUpdated;
	private JTextField jTextFieldPathUpJar;
	private JLabel jLabelUpdatedWorker;
	private JLabel jLabelSeparator;
	private JLabel jLabelTotalWorker;
	private JLabel jLabelUpdateProgress;
	
	private DelegatedObservable obs = new DelegatedObservable();
	private MasterDaemonStarter master;
	
	private int workerUpdated;
	private int total;
	private Address fTPAddress;
	private File updateFile;
	private String FTP_HOME;
	private String SEPARATOR;
	private String UPDATE_DIR;
	private ArrayList<String> toUpdate;
	


	public WorkerUpdater() {
		super();
		initGUI();
	}


	public WorkerUpdater(Address fptAddress, String ftpHome, String sEPARATOR2,
			MasterDaemonStarter master2, int size, String updateDir) {
		super();
		
		this.fTPAddress = fptAddress;
		this.FTP_HOME = ftpHome;
		this.SEPARATOR = sEPARATOR2;
		this.master = master2;
		this.total = size;
		this.UPDATE_DIR = updateDir;
		
		
		initGUI();
	}


	/** 
	 * 
	 * @param fptAddress
	 * @param ftpHome
	 * @param sEPARATOR2 path separator
	 * @param master2
	 * @param size
	 * @param updateDir
	 * @param toUpdate used for update some worker
	 */
	public WorkerUpdater(Address fptAddress, String ftpHome, String sEPARATOR2,
			MasterDaemonStarter master2, int size, String updateDir,
			ArrayList<String> toUpdate) {
		super();
		
		this.fTPAddress = fptAddress;
		this.FTP_HOME = ftpHome;
		this.SEPARATOR = sEPARATOR2;
		this.master = master2;
		this.total = size;
		this.UPDATE_DIR = updateDir;
		this.toUpdate = toUpdate;
		
		initGUI();
	}


	private void initGUI() 
	{
		try {
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			{
				jPanelMain = new JPanel();
				GroupLayout jPanelMainLayout = new GroupLayout((JComponent)jPanelMain);
				jPanelMain.setLayout(jPanelMainLayout);
				getContentPane().add(jPanelMain, BorderLayout.CENTER);
				jPanelMain.setPreferredSize(new java.awt.Dimension(503, 190));
				{
					jLabelUpdateProgress = new JLabel();
					jLabelUpdateProgress.setText("Update Progress");
				}
				{
					jProgressBarUpdate = new JProgressBar();
					jProgressBarUpdate.setValue(0);
				}
				{
					jLabelTotalWorker = new JLabel();
					jLabelTotalWorker.setText("0");
					jLabelTotalWorker.setText(Integer.toString(total));
					
					System.out.println("Total: "+total);
				}
				{
					jLabelUpdated = new JLabel();
					jLabelUpdated.setText("Updated:");
				}
				{
					jLabelSeparator = new JLabel();
					jLabelSeparator.setText("of");
				}
				{
					jLabelUpdatedWorker = new JLabel();
					jLabelUpdatedWorker.setText("" + workerUpdated);
				}
				{
					jButtonUpdateWorker = new JButton();
					jButtonUpdateWorker.setText("Update Worker");
					jButtonUpdateWorker.addActionListener(new ActionListener() 
					{
						
						public void actionPerformed(ActionEvent evt) {
							startUpdate();
						}
	
					});
				}
				{
					jButtonChoseUpJar = new JButton();
					jButtonChoseUpJar.setIcon(new ImageIcon(getClass().getClassLoader().getResource("dmason/resource/image/openFolder.png")));
					jButtonChoseUpJar.setPreferredSize(new java.awt.Dimension(14, 7));
					jButtonChoseUpJar.setSize(16, 16);
					jButtonChoseUpJar.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							updateFile = showFileChooser();
							if(updateFile != null)
								jTextFieldPathUpJar.setText(updateFile.getAbsolutePath());
						}
					});
				}
				
				
				
				{
					jTextFieldPathUpJar = new JTextField();
					jTextFieldPathUpJar.setText("PathUpdateJar");
				}
				jPanelMainLayout.setHorizontalGroup(jPanelMainLayout.createSequentialGroup()
					.addGroup(jPanelMainLayout.createParallelGroup()
					    .addGroup(GroupLayout.Alignment.LEADING, jPanelMainLayout.createSequentialGroup()
					        .addComponent(jTextFieldPathUpJar, GroupLayout.PREFERRED_SIZE, 202, GroupLayout.PREFERRED_SIZE)
					        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					        .addComponent(jButtonChoseUpJar, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE))
					    .addGroup(GroupLayout.Alignment.LEADING, jPanelMainLayout.createSequentialGroup()
					        .addComponent(jButtonUpdateWorker, GroupLayout.PREFERRED_SIZE, 146, GroupLayout.PREFERRED_SIZE)
					        .addGap(25)
					        .addComponent(jLabelUpdated, GroupLayout.PREFERRED_SIZE, 58, GroupLayout.PREFERRED_SIZE)
					        .addGap(6)))
					.addComponent(jLabelUpdatedWorker, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addComponent(jLabelSeparator, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addComponent(jLabelTotalWorker, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
					.addGroup(jPanelMainLayout.createParallelGroup()
					    .addGroup(jPanelMainLayout.createSequentialGroup()
					        .addComponent(jProgressBarUpdate, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					        .addGap(0, 0, Short.MAX_VALUE))
					    .addGroup(GroupLayout.Alignment.LEADING, jPanelMainLayout.createSequentialGroup()
					        .addComponent(jLabelUpdateProgress, GroupLayout.PREFERRED_SIZE, 102, GroupLayout.PREFERRED_SIZE)
					        .addGap(0, 46, Short.MAX_VALUE)))
					.addContainerGap(144, 144));
				jPanelMainLayout.setVerticalGroup(jPanelMainLayout.createSequentialGroup()
					.addContainerGap(25, 25)
					.addGroup(jPanelMainLayout.createParallelGroup()
					    .addComponent(jTextFieldPathUpJar, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					    .addComponent(jButtonChoseUpJar, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE))
					.addGap(25)
					.addGroup(jPanelMainLayout.createParallelGroup()
					    .addGroup(GroupLayout.Alignment.LEADING, jPanelMainLayout.createSequentialGroup()
					        .addGap(0, 0, Short.MAX_VALUE)
					        .addComponent(jLabelUpdateProgress, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					        .addComponent(jProgressBarUpdate, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					        .addGap(0, 6, GroupLayout.PREFERRED_SIZE))
					    .addGroup(jPanelMainLayout.createSequentialGroup()
					        .addGap(13)
					        .addGroup(jPanelMainLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					            .addComponent(jButtonUpdateWorker, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					            .addComponent(jLabelUpdatedWorker, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					            .addComponent(jLabelSeparator, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					            .addComponent(jLabelTotalWorker, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					            .addComponent(jLabelUpdated, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))))
					.addContainerGap(249, 249));
			}
			pack();
			this.setSize(508, 218);
			
		} catch (Exception e) {
		    //add your error handling code here
			e.printStackTrace();
		}
	}

	/**
	* Auto-generated main method to display this JFrame
	*/
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				WorkerUpdater inst = new WorkerUpdater();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}
		});
	}
	
	
	private void setAutomaticMode(boolean isOn)
	{
		jButtonChoseUpJar.setVisible(!isOn);
		jTextFieldPathUpJar.setVisible(!isOn);
		jButtonUpdateWorker.setVisible(!isOn);
	}
	
	/**
	 * Start automatic update
	 */
	public void startAutoUpdate()
	{
		setAutomaticMode(true);

		if(fTPAddress != null)
		{
			if( updateFile != null)
			{
				UpdateData ud = new UpdateData(updateFile.getName(), fTPAddress);
				master.notifyUpdate(ud, toUpdate);
				
			}
			
		}
	}
	
	/**
	 * Called from UI
	 */
	public void startUpdate() 
	{

		if(fTPAddress != null)
		{
			if( updateFile != null)
			{
				File dest = new File(FTP_HOME+SEPARATOR+UPDATE_DIR+SEPARATOR+updateFile.getName());

				try {
					FileUtils.copyFile(updateFile, dest);

					Digester dg = new Digester(DigestAlgorithm.MD5);

					InputStream in = new FileInputStream(dest);
					dg.getDigest(in);

					String fileName = FilenameUtils.removeExtension(updateFile.getName());
					dg.storeToPropFile(FTP_HOME+SEPARATOR+UPDATE_DIR+SEPARATOR+fileName+".hash");


				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoDigestFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				UpdateData ud = new UpdateData(updateFile.getName(), fTPAddress);
				master.notifyUpdateAll(ud);
			}
		}
	}
	

	public File showFileChooser()
	{
		JFileChooser fileChooser = new JFileChooser();
		
		fileChooser.setCurrentDirectory(new File(FTP_HOME));
        int n = fileChooser.showOpenDialog(WorkerUpdater.this);
        if (n == JFileChooser.APPROVE_OPTION) 
        {
          return  fileChooser.getSelectedFile();
        }
        else
        	return null;
	}
	
	/**
	 * Called by MasterUI for set progress
	 */
	public void setProgress()
	{
		workerUpdated++;
		int total = Integer.parseInt(jLabelTotalWorker.getText());
		
		jProgressBarUpdate.setValue((int) ((workerUpdated *100)/total));
		jLabelUpdatedWorker.setText(""+workerUpdated);
		
		if(workerUpdated == total)
		{	
			// notify MasterUI
			obs.setChanged();
			obs.notifyObservers();
		}
	}
	
	public Observable getObservable() {return obs;}
	
	public MasterDaemonStarter getMaster() {
		return master;
	}

	
	/** Getter and setter section **/
	public void setMaster(MasterDaemonStarter master) {
		this.master = master;
	}

	public int getWorkerUpdated() {
		return workerUpdated;
	}

	public void setWorkerUpdated(int workerUpdated) {
		this.workerUpdated = workerUpdated;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public Address getfTPAddress() {
		return fTPAddress;
	}

	public void setfTPAddress(Address fTPAddress) {
		this.fTPAddress = fTPAddress;
	}

	public File getUpdateFile() {
		return updateFile;
	}

	public void setUpdateFile(File updateFile) {
		this.updateFile = updateFile;
	}

	public String getFTP_HOME() {
		return FTP_HOME;
	}

	public void setFTP_HOME(String fTP_HOME) {
		FTP_HOME = fTP_HOME;
	}

	public String getSEPARATOR() {
		return SEPARATOR;
	}

	public void setSEPARATOR(String sEPARATOR) {
		SEPARATOR = sEPARATOR;
	}

	public String getUPDATE_DIR() {
		return UPDATE_DIR;
	}

	public void setUPDATE_DIR(String uPDATE_DIR) {
		UPDATE_DIR = uPDATE_DIR;
	}
	/** End Getter and setter section **/
	
	
	//A subclass of Observable that allows delegation.
	public class DelegatedObservable extends Observable 
	{
		public void clearChanged() {
			super.clearChanged();
		}
		public void setChanged() {
			super.setChanged();
		}
	}

}
