/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Stefan
 */
public class ServerInfo {
    
    private String ipAddress;
    private int portNum;

    public ServerInfo(String input){
      String[] info = input.split(":");
      ipAddress = info[0];
      portNum = Integer.parseInt(info[1]);
    }

    public String getIpAddress(){
      return ipAddress;
    }

    public int getPortNumber(){
      return portNum;
    }

    public void setIpAddress(String ipAddr){
      ipAddress = ipAddr;
    }

    public void setPortNumber(int port){
      portNum = port;
    }
}
    