
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Stefan
 */
public class ClientAssuranceThread extends Thread{
    protected ThreadLocal<PrintWriter> pOut;
    
    public ClientAssuranceThread(Socket client){
        pOut = new ThreadLocal<>();
        try {
            pOut.set(new PrintWriter(client.getOutputStream()));
        } catch (IOException ex) {
            Logger.getLogger(ClientAssuranceThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run(){
        PrintWriter pout = (PrintWriter) pOut.get();
        try{
            this.sleep(50);
            pout.print(Symbols.assuranceMessage);
            pout.flush();
        } catch(InterruptedException e){
            pout.close();
            return;
        }
    }
    
    
    
}
