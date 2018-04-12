package com.p2p;

import java.net.Socket;

public class PeerThread implements Runnable{

	Socket socket=null;
	boolean client;
	
	
	
	
	public Socket getSocket() {
		return socket;
	}



	public void setSocket(Socket socket) {
		this.socket = socket;
	}



	public boolean isClient() {
		return client;
	}



	public void setClient(boolean client) {
		this.client = client;
	}

	public boolean checkClient(){
		return this.client==true;
	}


	public PeerThread(Socket socket, boolean client, int peerId ){
		this.socket=socket;
		this.client=client;
		
		if(!checkClient()){
			
		
		}
		else{
			
		}
		
		InitialSetupThread ist=new InitialSetupThread();
		ist.start();
	
	}

	
	
	@Override
	public void run() {
		
		
		
		
		
	}
	
	
	
	
}
