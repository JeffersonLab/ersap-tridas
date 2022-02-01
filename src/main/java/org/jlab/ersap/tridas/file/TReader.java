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

    private void decodeTimeSliceHeader(TRingRawEvent evt) {
        try {
            tTimeSliceHeaderBuffer.clear();
            dataInputStream.readFully(tTimeSliceHeader);

            tTimeSliceId = tTimeSliceHeaderBuffer.getInt();
            evt.setNumberOfEvents(tTimeSliceHeaderBuffer.getInt());
            tTimeSliceLength = tTimeSliceHeaderBuffer.getInt();
            evt.setPayloadLength(tTimeSliceLength - 16);
            numberOfMissedFrames = tTimeSliceHeaderBuffer.getInt();

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

    public void run() {
        try {
            // get dataInputStream from a file
            FileInputStream inputStream
                    = new FileInputStream(
                    fileName);

            dataInputStream = new DataInputStream(inputStream);
        } catch (
                IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                // Get an empty item from ring
                TRingRawEvent tRingRawEvent = get();
                decodeTimeSliceHeader(tRingRawEvent);
                // Make the buffer available for consumers
                publish();
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
