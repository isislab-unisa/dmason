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
package it.isislab.dmason.experimentals.util.management.garbagecollector;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Start {

        private String start="start\n";
        private String stop="stop\n";
        private String restart="restart\n";
        private Socket socket;
        private BufferedOutputStream out;
        private BufferedInputStream in;
        private Scanner console;
        private boolean r = false;
        
        public Start() {}
        
        public boolean connect(String ip,String port){
                try{
                        socket = new Socket(ip,Integer.parseInt(port));
                        in = new BufferedInputStream(socket.getInputStream());
                        console = new Scanner(in);
                        return true;
                }catch (Exception e) {
                        e.printStackTrace();
                        return false;
                        }
        }
        
        public boolean isConnected()
        {
                if(socket == null)
                        return false;
                return socket.isConnected();
                
        }
        
        public void execute(String cmd){
                try{
                        out = new BufferedOutputStream(socket.getOutputStream());
                        if(cmd.equals("start")){
                                out.write(start.getBytes());
                                out.flush();
                        }
                        if(cmd.equals("stop")){
                                out.write(stop.getBytes());
                                out.flush();
                        }
                        if(cmd.equals("restart")){
                                r = true;
                                out.write(restart.getBytes());
                                out.flush();
                        }
                }catch (Exception e) {
                        e.printStackTrace();
                }
        }
        
        public String receive(){
                return console.nextLine();
        }
        
        public static void main(String[] args){
                Start s = new Start();
                s.connect("127.0.0.1","3333");
                Scanner input = new Scanner(System.in);
                String x="";
                while(input.hasNext())
                {
                        x = input.nextLine();
                        s.execute(x);
                        System.out.println(s.receive());
                }
        }
}