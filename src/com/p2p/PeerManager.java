package com.p2p;

import java.io.*;

import java.net.Socket;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.Collections;
import com.p2p.messages.Message;

public class PeerManager {


    public static final byte[] HANDSHAKE_HEADER = "P2PFILESHARINGPROJ".getBytes();
    public static final byte[] ZERO_BITS = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    // Map to maintain list of Peers with successful handshakes.
    private static Map<Integer, Boolean> handshakeSucess = new HashMap<Integer, Boolean>();


    // Socket variables
    private Socket socket = null;
    private BufferedOutputStream bufferedOutputStream = null;
    private BufferedInputStream bufferedInputStream = null;
    private boolean clientValue = false;

    // Initialize ownerId to zero.
    public static int ownerId = 0;

    private int peerId;

    public void setPeerId(int id) {
        this.peerId = id;
    }

    public synchronized int getPeerId() {
        return peerId;
    }

    public byte[] getHandshakeMessage(int peerId) throws IOException {
        return ByteArrayManipulation.mergeByteArray(HANDSHAKE_HEADER, ZERO_BITS,
                ByteArrayManipulation.intToByteArray(peerId));
    }

    public PeerManager(Socket socket) throws IOException {

        synchronized (this) {
            this.socket=socket;
            bufferedInputStream=new BufferedInputStream(socket.getInputStream());
            bufferedOutputStream=new BufferedOutputStream(socket.getOutputStream());
        }

    }

    public synchronized void sndHandshakeMessage() throws IOException {
        // Send the handshake to the peer.
        synchronized (handshakeSucess) {

            // Create the handshake message by concatenating the handshake header, zero bits and the owner peerId
            // (retrieved from the commonConfig hashmap written in peerProcess).
            byte[] concatenateByteArrays = ByteArrayManipulation.mergeByteArray(HANDSHAKE_HEADER, ZERO_BITS, String.valueOf(PeerManager.ownerId).getBytes());


            try {

                // write the owner's created handshake message in the BufferedOutputStream stream of the client socket
                output.write(concatenateByteArrays);
                output.flush();

                // if success, put the client peerId in the handshakeSuccess map of the owner peer.
                handshakeSucess.put(peerId, false);

            } catch (IOException e) {
                LOGGER.severe("Handshake sending failed." + e.getMessage());
            }

        }
    }

    public synchronized int acceptHandshakeMessage() {

        try {
            byte[] byteArr = new byte[32];


            // Read from the peer(should be client) socket's bufferedinputstream into the byteArr.
            input.read(byteArr);

            // Obtain the peerId from the last four bytes of handshake message
            byte[] reqByteArrRange = Arrays.copyOfRange(byteArr, 28, 32);
            Integer peerId = Integer.parseInt(new String(reqByteArrRange));

            // if a client peer
            if (clientValue) {

                // if the client peer exists in the handshakeSuccess map of owner peer and
                // if this client's handshake has not been received yet
                if (handshakeSucess.containsKey(peerId) && handshakeSucess.get(peerId) == false) {

                    // set the received handshake flag of client peer to true
                    handshakeSucess.put(peerId, true);
                    System.out.println("Valid peer id:" + peerId);
                } else {
                    System.out.println("Invalid peer id:" + peerId);
                }

            }

            // return the client's peerId which sent a handshake only in response to a handshake sent by owner peer.
            return peerId;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }


    //static int fileSize = Integer.parseInt(CommonPeerConfig.retrieveCommonConfig().get("FileSize"));
    static int pieceSize = Integer.parseInt(CommonPeerConfig.retrieveCommonConfig().get("PieceSize"));
    static int fileSize = Integer.parseInt(CommonPeerConfig.retrieveCommonConfig().get("FileSize"));
    private final static byte[] ownerBitField;
    static long numOfPieces = 0;
    static {


        if (fileSize % pieceSize == 0) {
            numOfPieces = fileSize / pieceSize;
        }
        else {
            numOfPieces = fileSize / pieceSize + 1;
        }
        ownerBitField = new byte[(int) Math.ceil(numOfPieces / 8.0f)];
    }

    public static synchronized byte[] getOwnerBitField() {
        return ownerBitField;
    }



    // Sends a Message of type BitField

    public synchronized void sndBitFieldMessageToPeer() {

        try{

            byte[] ownerBitField = getOwnerBitField();

            // create the actual message by merging message length, type and payload
            byte[] actualMessage = Message.getOriginalMessage(ownerBitField, Message.MESSAGE.BITFIELD);

            // write the actual message in the peer socket's output stream
            output.write(actualMessage);
            System.out.println("see port for send bit:" + this.socket.getPort());
            System.out.println("see2:" + Arrays.toString(actualMessage));
            output.flush();
        }
        catch (IOException e){
            System.out.println("Sending of bit field message failed." + e.getMessage());
        }

    }

    byte[] bitFieldMesssageOfPeer;
    public synchronized byte[]  getbitFieldMessageOfPeer(){
        return bitFieldMesssageOfPeer;
    }


    public synchronized void readBitFieldMessageOfPeer() {

        // read the array of bytes from client peer socket bufferedinputstream into bitFieldMessageOfPeer byte array
        System.out.println("see port for read bit:"+this.socket.getPort());
        bitFieldMesssageOfPeer = Message.readOriginalMessage(input , Message.MESSAGE.BITFIELD);
        System.out.println("returning"+this.socket.getPort()+ " "+ this.getPeerId());
    }

    // Sends a Message of type Interested
    public synchronized void sendInterestedMessage() throws IOException {

        // Obtains the concatenated msgL, messageValue of INTERESTED message
        byte[] actualMessage = Message.getOriginalMessage(Message.MESSAGE.INTERESTED);

        try {

            // write the INTERESTED message into the client socket bufferedoutputstream
            output.write(actualMessage);
            output.flush();

        } catch (IOException e) {
            System.out.println("Sending of interested message failed: " + e.getMessage());
        }
    }

    // Sends a Message of type NotInterested
    public synchronized void sendNotInterestedMessage() throws IOException {

        // Obtains the concatenated msgL, messageValue of INTERESTED message
        byte[] originalMessage = Message.getOriginalMessage(Message.MESSAGE.NOT_INTERESTED);

        try {

            // write the NOT_INTERESTED message into the client socket bufferedoutputstream
            output.write(originalMessage);
            output.flush();

        } catch (IOException e) {
            System.out.println("Sending of not interested message failed: " + e.getMessage());
        }
    }

    // Send choke message
    public synchronized  void sendChokeMessage() throws IOException {
        byte[] actualMessage = Message.getOriginalMessage(Message.MESSAGE.CHOKE);
        try {
            output.write(actualMessage);
            output.flush();

        } catch (IOException e) {
            System.out.println("io exception in reading " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Send unchoke message
    public synchronized void sendUnchokeMessage() throws IOException {
        byte[] actualMessage = Message.getOriginalMessage(Message.MESSAGE.UNCHOKE);
        try {
            output.write(actualMessage);
            output.flush();

        } catch (IOException e) {
            System.out.println("io exception in reading " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean choked = true;

    public synchronized void setChoked(boolean n) {
        choked = n;
    }

    public synchronized boolean isChoked() {
        return choked;
    }


    public static Map<Integer, Long> peerRequestTime = Collections.synchronizedMap(new HashMap<Integer, Long>());
    // Send request message
    public synchronized void sendRequestMessage(int indexOfPiece) throws IOException {

        // if the requested piece index is >=0
        if (indexOfPiece >= 0) {

            // obtain the byte array of requested pieceIndex
            byte[] pieceIndexByteArray = ByteArrayManipulation.intToByteArray(indexOfPiece);

            // obtain the request message by concatenating message length, message value and payload of request message
            byte[] originalMessage = Message.getOriginalMessage(
                    pieceIndexByteArray, Message.MESSAGE.REQUEST);
            try {

                // write the original request message created into the peer's socket output buffer
                output.write(originalMessage);
                output.flush();

                // Add the peer id and its time of request in the peerRequestTime hashmap
                PeerManager.peerRequestTime.put(peerId, System.nanoTime());

            } catch (IOException e) {

                System.out.println("Exception encountered while writing in sendRequestMessage:" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Send piece message

    static byte[] sharedDataArr = new byte[Integer.parseInt(CommonPeerConfig.retrieveCommonConfig().get("FileSize"))];
    public synchronized void sendPieceMessage(int pieceIndex) throws IOException {

        int pI = pieceIndex;

        int pieceSizeFromFile = Integer.parseInt(CommonPeerConfig.retrieveCommonConfig().get(
                "PieceSize"));

        int startIndex = pieceSizeFromFile * pieceIndex;

        int endIndex = startIndex + pieceSizeFromFile - 1;

        if (endIndex >= sharedDataArr.length) {
            endIndex = sharedDataArr.length - 1;
        }
        //special case
        //if pieceSize is greater than the entire file left

        byte[] data = new byte[endIndex - startIndex + 1 + 4]; // 4 is for pieceIndex

        // populate the piece index
        byte[] pieceIndexByteArray = ByteArrayManipulation.intToByteArray(pieceIndex);

        for (int i = 0; i < 4; i++) {
            data[i] = pieceIndexByteArray[i];
        }

        // populate the data
        for (int i = startIndex; i <= endIndex ; i++) {
            data[i - startIndex + 4] = sharedDataArr[i];
        }

        // obtain the piece message by concatenating message length, message value and payload
        byte[] originalMessage = Message.getOriginalMessage(data,
                Message.MESSAGE.PIECE);
        try {
            System.out.println("The actual message size is " + originalMessage.length);
            // write the created piece message into the peer's socket output stream
            output.write(originalMessage);
            output.flush();
        } catch (IOException e) {
            System.out.println("io exception in reading " + e.getMessage());
            e.printStackTrace();
        }
    }

    static byte[] requestedBitField = new byte[(int) Math.ceil(numOfPieces / 8.0f)];
    public static synchronized byte[] getRequestedBitField() {
        return requestedBitField;
    }


    public static synchronized void setIndexOfPieceRequested(int index, int indexFromRight) {
        requestedBitField[index] |= (1 << indexFromRight);
    }

    private int indexOfRequestedPiece;

    public synchronized  void setindexOfRequestedPiece(int index) {
        indexOfRequestedPiece = index;
    }

    // TODO: Test this function
    public synchronized int getNextBitFieldIndexToRequest() {

        /// request a piece owner peer doesn't have and did not request from other peers,
        // select next piece to request index randomly
        byte[] reqstdUntilnow = getRequestedBitField();
        byte[] ntHavendNtReqst = new byte[bitFieldMesssageOfPeer.length]; // to store bytes that I don't have
        byte[] bitFieldReqAndHave = new byte[bitFieldMesssageOfPeer.length];
        byte[] ownerbitfield = getOwnerBitField();
        System.out.println("Arrays.toString(reqstdUntilnow) = " + Arrays.toString(reqstdUntilnow));

        for (int i = 0; i < reqstdUntilnow.length; i++) {
            bitFieldReqAndHave[i] = (byte) (reqstdUntilnow[i] & ownerbitfield[i]);
        }

        // determine bits I dont have.
        for (int i = 0; i < bitFieldReqAndHave.length; i++) {
            ntHavendNtReqst[i] = (byte) ((bitFieldReqAndHave[i] ^ bitFieldMesssageOfPeer[i]) & ~bitFieldReqAndHave[i]);
        }

        System.out.println("Arrays.toString(peerBitFieldMsg) = " + Arrays.toString(bitFieldMesssageOfPeer));
        System.out.println("Arrays.toString(getMyBitField()) = " + Arrays.toString(getOwnerBitField()));
        System.out.println("Arrays.toString(bitFieldReqAndHave) = " + Arrays.toString(bitFieldReqAndHave));
        System.out.println("Arrays.toString(ntHavendNtReqst) = " + Arrays.toString(ntHavendNtReqst));

        int count = 0;
        int pos = 0;
        for (int i = 0; i < ntHavendNtReqst.length; i++) {
            count = 8 * i;
            byte temp = ntHavendNtReqst[i];
            Byte b = new Byte(temp);

            pos = 0;
            while (temp != 0 && pos < 8) {
                if ((temp & (1 << pos)) != 0) {
                    setIndexOfPieceRequested(i, pos);
                    pos = 7 - pos;
                    int index = count + pos;
                    setindexOfRequestedPiece(index);
                    // set the ith bit as 1
                    return index;
                }
                ++pos;
            }
        }

        System.out.println("Arrays.toString(myBitField) = " + Arrays.toString(getOwnerBitField()));
        return -1;
    }

}



}



