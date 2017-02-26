/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Stefan
 */
import java.net.*; import java.io.*; import java.util.*;
public class NameServer {
	NameTable table;
	public NameServer() {
		table = new NameTable();
	}
	public static void main(String[] args) {
		NameServer ns = new NameServer();
		System.out.println("NameServer started:");
		try {
			ServerSocket listener = new ServerSocket(Symbols.ServerPort);
			Socket s;
			while ( (s = listener.accept()) != null) {
				Thread t = new ServerThread(ns.table, s);
				t.start();
			}
		} catch (IOException e) {
			System.err.println("Server aborted:" + e);
		}
	}
}
