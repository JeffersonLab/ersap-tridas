package org.jlab.ersap.tridas.stream;

/**
 * Copyright (c) 2021, Jefferson Science Associates, all rights reserved.
 * See LICENSE.txt file.
 * Thomas Jefferson National Accelerator Facility
 * Experimental Physics Software and Computing Infrastructure Group
 * 12000, Jefferson Ave, Newport News, VA 23606
 * Phone : (757)-269-7100
 *
 * @author gurjyan on 1/29/22
 * @project ersap-tridas
 */
import com.lmax.disruptor.RingBuffer;
import org.jlab.ersap.tridas.TPrintStatistics;
import org.jlab.ersap.tridas.TRingRawEvent;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;

public class TReceiver extends Thread {
    private DataInputStream dataInputStream;
    private int streamId;
    private RingBuffer<TRingRawEvent> ringBuffer;
    private long sequenceNumber;

    private ServerSocket serverSocket;
    private int tPort;

    public static int tTimeSliceId;
    public static int numberOfMissedFrames;
    private int tTimeSliceLength;

    private ByteBuffer tTimeSliceHeaderBuffer;
    private byte[] tTimeSliceHeader = new byte[16];

    private AtomicBoolean running = new AtomicBoolean(true);

    public TReceiver(int port, int streamId, RingBuffer<TRingRawEvent> ringBuffer) {
        this.tPort = port;
        this.ringBuffer = ringBuffer;
        this.streamId = streamId;

        tTimeSliceHeaderBuffer = ByteBuffer.wrap(tTimeSliceHeader);
        tTimeSliceHeaderBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public TReceiver(int port, int streamId, RingBuffer<TRingRawEvent> ringBuffer, int statPeriod) {
        this.tPort = port;
        this.ringBuffer = ringBuffer;
        this.streamId = streamId;

        tTimeSliceHeaderBuffer = ByteBuffer.wrap(tTimeSliceHeader);
        tTimeSliceHeaderBuffer.order(ByteOrder.LITTLE_ENDIAN);
        // Timer for measuring and printing statistics.
        Timer timer = new Timer();
        timer.schedule(new TPrintStatistics(false, streamId), 0, statPeriod * 1000);
    }

    /**
     * Get the next available item in ring buffer for writing data.
     *
     * @return next available item in ring buffer.
     * @throws InterruptedException if thread interrupted.
     */
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(tPort);
            System.out.println("Server is listening on port " + tPort);
            Socket socket = serverSocket.accept();
            System.out.println("TriDAS client connected");
            InputStream input = socket.getInputStream();
            dataInputStream = new DataInputStream(new BufferedInputStream(input, 65536));
        } catch (
                IOException e) {
            e.printStackTrace();
        }

        while (running.get()) {
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
        running.set(false);
        try {
            dataInputStream.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.interrupt();
    }
}
