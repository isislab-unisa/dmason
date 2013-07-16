package dmason.master;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;

import dmason.util.SystemManagement.JMasterUI;

public class DeployUI extends JFrame
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Master master;
	private int[] updatableWorkers;
	private File selectedFile = null;
	
	private JLabel lblFile;

	public DeployUI(Master master, int[] updatableWorkers)
	{
		this.master = master;
		this.updatableWorkers = updatableWorkers;
		
		setupLayout();
		
		this.setTitle("Deploy worker JAR");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.pack();
		this.setLocationRelativeTo(null);
	}

	private void setupLayout()
	{
		this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		
		// pnlProgress
		JPanel pnlProgress = new JPanel();
		pnlProgress.setLayout(new BoxLayout(pnlProgress, BoxLayout.Y_AXIS));
		pnlProgress.setBorder(new TitledBorder("Progress"));
		this.add(pnlProgress);
		
		// pnlProgress > lblProgressInfo
		JLabel lblProgressInfo = new JLabel(updatableWorkers.length + (updatableWorkers.length == 1 ? " worker" : " workers") + " will be updated. Please choose the file to deploy.");
		pnlProgress.add(lblProgressInfo);
		pnlProgress.add(Box.createVerticalStrut(10));
		
		// pnlProgress > barProgress
		JProgressBar barProgress = new JProgressBar(0, updatableWorkers.length);
		barProgress.setValue(0);
		barProgress.setString("");
		barProgress.setStringPainted(true);
		pnlProgress.add(barProgress);
		
		// pnlFile
		JPanel pnlFile = new JPanel();
		pnlFile.setLayout(new BorderLayout(10, 10));
		pnlFile.setBorder(new TitledBorder("File"));
		this.add(pnlFile);
		
		// pnlFile > lblFile
		lblFile = new JLabel("No file selected");
		pnlFile.add(lblFile, BorderLayout.CENTER);
		
		// pnlFile > btnFile
		JButton btnFile = new JButton("Browse...");
		pnlFile.add(btnFile, BorderLayout.EAST);
		btnFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser fileChooser = new JFileChooser();
				
				fileChooser.setCurrentDirectory(new File(master.ftpDir));
		        int res = fileChooser.showOpenDialog(DeployUI.this);
		        if (res == JFileChooser.APPROVE_OPTION)
		        {
		        	selectedFile = fileChooser.getSelectedFile();
		        	lblFile.setText(selectedFile.getName());
		        }
			}
		});
		
	}
}
;