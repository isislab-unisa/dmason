package it.isislab.dmason.experimentals.systemmanagement;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class Test {

	public static void main(String[] args) throws IOException, JSchException {
		// TODO Auto-generated method stub
		JSch jsch=new JSch();
		Session session=jsch.getSession(System.getProperty("user.name"), "172.16.15.20", 22);
		jsch.addIdentity(System.getProperty("user.home")+File.separator+".ssh"+File.separator+"id_rsa");
		Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
		session.connect();

		ChannelExec channel=(ChannelExec) session.openChannel("exec");
		BufferedReader in=new BufferedReader(new InputStreamReader(channel.getInputStream()));
		channel.setCommand("pwd;");
		channel.connect();

		String msg=null;
		while((msg=in.readLine())!=null){
		  System.out.println(msg);
		}

		channel.disconnect();
		session.disconnect();
	}

}
