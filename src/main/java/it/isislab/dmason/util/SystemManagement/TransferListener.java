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

package it.isislab.dmason.util.SystemManagement;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 * This class is a listener for the download operation from FTP
 * 
 * Note: Currently not used
 * @author marvit
 *
 */
public class TransferListener implements FTPDataTransferListener
{

	private long filesize;
	private long totalByteDownloaded = 0;
	
	private JProgressBar progBar;
	private JLabel status;
	
	public TransferListener(long size, JProgressBar jProgressBarDownload, JLabel jLabelStatus) {
		// TODO Auto-generated constructor stub
		filesize = size;
		progBar = jProgressBarDownload;
		status = jLabelStatus;
	}

	@Override
	public void aborted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void completed() {
		// TODO Auto-generated method stub
		status.setText("Download completed");
	}

	@Override
	public void failed() {
		status.setText("Download failed");
	}

	@Override
	public void started() {
		status.setText("Start Downloading");
	}

	@Override
	public void transferred(int byteTranf) {
		
		status.setText("Downloading...");
		totalByteDownloaded += byteTranf;
		//A:B = n:100 -> n = (A * 100) / B
		// TODO Auto-generated method stub
		progBar.setValue((int) ((totalByteDownloaded *100)/filesize));
	}

}
