package org.jlab.ersap.tridas.file;

import com.lmax.disruptor.RingBuffer;
import org.jlab.ersap.tridas.TRingRawEvent;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Copyright (c) 2021, Jefferson Science Associates, all rights reserved.
 * See LICENSE.txt file.
 * Thomas Jefferson National Accelerator Facility
 * Experimental Physics Software and Computing Infrastructure Group
 * 12000, Jefferson Ave, Newport News, VA 23606
 * Phone : (757)-269-7100
 *
 * @author gurjyan on 2/1/22
 * @project ersap-tridas
 */
public class TReader extends Thread {
    private DataInputStream dataInputStream;
    private int streamId;
    private RingBuffer<TRingRawEvent> ringBuffer;
    private long sequenceNumber;

    private String fileName;

    public static int tTimeSliceId;
    public static int numberOfMissedFrames;
    private int tTimeSliceLength;

    private ByteBuffer tTimeSliceHeaderBuffer;
    private byte[] tTimeSliceHeader = new byte[16];

    private ByteBuffer dataBuffer;

    public TReader(String fileName, int streamId, RingBuffer<TRingRawEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
        this.streamId = streamId;
        this.fileName = fileName;

        tTimeSliceHeaderBuffer = ByteBuffer.wrap(tTimeSliceHeader);
        tTimeSliceHeaderBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    private TRingRawEvent get() throws InterruptedException {

        sequenceNumber = ringBuffer.next();
        TRingRawEvent buf = ringBuffer.get(sequenceNumber);
        return buf;
    }

    private void publish() {
        ringBuffer.publish(sequenceNumber);
    }

    private void decodeTimeSliceHeaderOld(TRingRawEvent evt) {
        try {
            tTimeSliceHeaderBuffer.clear();
            dataInputStream.readFully(tTimeSliceHeader);

            tTimeSliceId = tTimeSliceHeaderBuffer.getInt();
            evt.setNumberOfEvents(tTimeSliceHeaderBuffer.getInt());
            tTimeSliceLength = tTimeSliceHeaderBuffer.getInt();
            evt.setPayloadLength(tTimeSliceLength - 16);
            numberOfMissedFrames = tTimeSliceHeaderBuffer.getInt();

            System.out.println("DDD " + tTimeSliceId + " " + evt.getNumberOfEvents() + " " + tTimeSliceLength);

            if (evt.getPayload().length < tTimeSliceLength) {
                byte[] payloadData = new byte[tTimeSliceLength];
                evt.setPayload(payloadData);
            }
            dataInputStream.readFully(evt.getPayload(), 0, tTimeSliceLength);
        } catch (EOFException j) {
            exit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void decodeTimeSliceHeader(TRingRawEvent evt) {

        tTimeSliceId = dataBuffer.getInt();
        dataBuffer.getInt(); //padding
        evt.setNumberOfEvents(dataBuffer.getInt());
        tTimeSliceLength = dataBuffer.getInt();
        evt.setPayloadLength(tTimeSliceLength - 16);
        numberOfMissedFrames = dataBuffer.getInt();

        System.out.println("DDD " + tTimeSliceId + " " + evt.getNumberOfEvents() + " " + tTimeSliceLength);

        byte[] payloadData = new byte[tTimeSliceLength];
        dataBuffer.get(payloadData);
        evt.setPayload(payloadData);
    }

    public void run() {
        try {
            // get dataInputStream from a file
            FileInputStream inputStream
                    = new FileInputStream(
                    fileName);

            dataInputStream = new DataInputStream(inputStream);
            // Count the total bytes
            // form the input stream
            int count = inputStream.available();

            // Create byte array
            byte[] b = new byte[count];

            // Read data into byte array
            int bytes = dataInputStream.read(b);

            // Print number of bytes
            // actually read
            System.out.println(bytes);

            dataBuffer = ByteBuffer.wrap(b);
            dataBuffer.order(ByteOrder.LITTLE_ENDIAN);
            dataBuffer.rewind();

            System.out.println("DDD ==============");
            long tsId = dataBuffer.getInt();
            dataBuffer.getInt(); // padding
            System.out.println(String.format("%x", tsId) + " " + tsId);
            int nEvents = dataBuffer.getInt();
            System.out.println(String.format("%x", nEvents) + " " + nEvents);
            int tsSize = dataBuffer.getInt();
            System.out.println(String.format("%x", tsSize) + " " + tsSize);
            int mFrames = dataBuffer.getInt();
            System.out.println(String.format("%x", mFrames) + " " + mFrames);

            byte[] payloadData = new byte[tTimeSliceLength];
            dataBuffer.get(payloadData);

            System.out.println("DDD ==============");


            System.out.println("DDD ==============");
            tsId = dataBuffer.getInt();
            dataBuffer.getInt(); // padding
            System.out.println(String.format("%x", tsId) + " " + tsId);
            nEvents = dataBuffer.getInt();
            System.out.println(String.format("%x", nEvents) + " " + nEvents);
            tsSize = dataBuffer.getInt();
            System.out.println(String.format("%x", tsSize) + " " + tsSize);
            mFrames = dataBuffer.getInt();
            System.out.println(String.format("%x", mFrames) + " " + mFrames);

            dataBuffer.getInt(); // padding
            int magic = dataBuffer.getInt();
            System.out.println(String.format("%x", magic) + " " + magic);

            System.out.println("DDD ==============");
            System.exit(-1);

        } catch (
                IOException e) {
            e.printStackTrace();
        }

        while (dataBuffer.position() < dataBuffer.limit()) {
            try {
                // Get an empty item from ring
                TRingRawEvent tRingRawEvent = get();
                decodeTimeSliceHeader(tRingRawEvent);
                // Make the buffer available for consumers
                publish();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void exit() {
        try {
            dataInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.interrupt();
    }
}
