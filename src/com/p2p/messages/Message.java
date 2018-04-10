package com.p2p.messages;

import com.p2p.ByteArrayManipulation;
import com.p2p.PeerManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class Message {


    public enum MESSAGE{
        CHOKE((byte)0),
        UNCHOKE((byte)1),
        INTERESTED((byte)2),
        NOT_INTERESTED((byte)3),
        HAVE((byte)4),
        BITFIELD((byte)5),
        REQUEST((byte)6),
        PIECE((byte)7);

        byte messageValue = -1;

        MESSAGE(byte b){
            this.messageValue = b;
        }
    }

    private int length;
    private MESSAGE type;
    private byte[] payload;

    public Message(MESSAGE type, byte[] payload){

        this.length = (payload == null ? 0 : payload.length);
        this.type = type;
        this.payload = payload;
    }

    public static byte[] getOriginalMessage(String payload, MESSAGE msgType) throws IOException {

        int l = payload.getBytes().length;
        byte[] msgL = ByteArrayManipulation.intToByteArray(l + 2 -1); // plus one for message type
        return ByteArrayManipulation.mergeByteArray(msgL,
                ByteArrayManipulation.mergeByteArray(msgType.messageValue, payload.getBytes()));
    }

    public static byte[] getOriginalMessage(MESSAGE msgType) throws IOException {

        byte[] msgL = ByteArrayManipulation.intToByteArray(1+1-1); // plus one for message type
        return ByteArrayManipulation.mergeByte(msgL, msgType.messageValue);
    }

    public static byte[] getOriginalMessage(byte[] payload, MESSAGE msgType) throws IOException {

        byte[] msgL = ByteArrayManipulation.intToByteArray(payload.length + 1 -1 + 1); // plus one for message type
        return ByteArrayManipulation.mergeByteArray(ByteArrayManipulation.mergeByte(msgL, msgType.messageValue), payload);
    }

    public static byte[] readOriginalMessage(InputStream in, Message.MESSAGE bitfield) {

        byte[] lengthByte = new byte[4];
        int read = 0-1;
        byte[] data = null;
        try {
            read = in.read(lengthByte);

            if (read != 4) {
                System.out.println("Incorrent message length.");
            }

            int dataLength = ByteArrayManipulation.byteArrayToInt(lengthByte);

            //read msg type
            byte[] msgType = new byte[1];

            in.read(msgType);
            //System.out.println("hey:"+msgType[0]);
            if (msgType[0] != bitfield.messageValue) {

                System.out.println("Incorrect message type sent"+ Arrays.toString(msgType) + " "+  PeerManager.ownerId+ " "+ bitfield.messageValue);
            }

            else {
                int actualDataLength = dataLength - 1;
                data = new byte[actualDataLength];
                //System.out.println(in + " "+ Arrays.toString(data) + " " + actualDataLength);
                data = ByteArrayManipulation.readBytes(in, data, actualDataLength);
            }

        } catch (IOException e) {

            System.out.println("Could not read length of actual message");
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //System.out.println("returning in readoriginal");
        return data;
    }


    public MESSAGE getType(){
        return type;
    };

    public int getLength(){
        return length;
    };

    public byte[] getPayload(){
        return payload;
    };

}
