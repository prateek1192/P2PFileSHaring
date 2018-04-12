package com.p2p;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.Socket;

public class PeerThread implements Runnable{

	Socket socket=null;
	boolean client;
    PeerManager connection;

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

    public synchronized PeerManager retrievePeerConnected() {
        return connection;
    }

    public synchronized PeerManager getPeerConnected(Socket s) {
        return new PeerManager(s);
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

		synchronized (socket){
		    connection=new PeerManager(socket);
        }
		
		if(!checkClient()){
			
		
		}
		else{
			
		}
		
		InitialSetupThread ist=new InitialSetupThread();
		ist.run();

	
	}

	
	
	@Override
	public void run() {


		try{

            InputStream is=socket.getInputStream();
            BufferedInputStream bufferedInputStream=new BufferedInputStream(is);

            while(true){



            }




        }catch (Exception e){
		    e.printStackTrace();
        }
		
		
	}
	
	
	
	
}
