package org.jlab.ersap.tridas.stream;

import com.lmax.disruptor.RingBuffer;
import org.jlab.ersap.tridas.TRingRawEvent;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.zeromq.ZMQ.PULL;


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

public class TReceiverZMQ extends Thread {
    private int streamId;
    private RingBuffer<TRingRawEvent> ringBuffer;
    private long sequenceNumber;

    private String address;

    public static int tTimeSliceId;
    public static int numberOfMissedFrames;
    private int tTimeSliceLength;

    private ByteBuffer tTimeSliceHeaderBuffer;
    private byte[] tTimeSliceHeader = new byte[20];

    private AtomicBoolean running = new AtomicBoolean(true);

    private ByteBuffer dataBuffer;

    public TReceiverZMQ(int port, int streamId, RingBuffer<TRingRawEvent> ringBuffer) {
        address = "tcp://*:"+port;
        this.ringBuffer = ringBuffer;
        this.streamId = streamId;

        tTimeSliceHeaderBuffer = ByteBuffer.wrap(tTimeSliceHeader);
        tTimeSliceHeaderBuffer.order(ByteOrder.LITTLE_ENDIAN);
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
        System.out.println("DDD ==============");

        tTimeSliceId = dataBuffer.getInt();
        System.out.println(String.format("tsID = %x", tTimeSliceId) + " " + tTimeSliceId);

        dataBuffer.getInt(); // padding

        int nEvents = dataBuffer.getInt();
        evt.setNumberOfEvents(nEvents);
        System.out.println(String.format("nEvents = %x", nEvents) + " " + nEvents);

        tTimeSliceLength = dataBuffer.getInt();
        System.out.println(String.format("tsLength = %x", tTimeSliceLength) + " " + tTimeSliceLength);
        evt.setPayloadLength(tTimeSliceLength - 20);

        numberOfMissedFrames = dataBuffer.getInt();
        System.out.println(String.format("lostFrames = %x", numberOfMissedFrames) + " " + numberOfMissedFrames);

        byte[] payloadData = new byte[evt.getPayloadLength()];
        dataBuffer.get(payloadData);
        evt.setPayload(payloadData);

        System.out.println("DDD ==============");
    }

    public void run() {
        System.out.println("INFO TriDAS receiver service is listening at = " + address);
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(ZMQ.PULL);
            socket.bind(address);
            System.out.println("INFO TriDAS TCPU client connected");

            while (true) {
                if(dataBuffer!=null) dataBuffer.clear();
                byte[] b = socket.recv();
                dataBuffer = ByteBuffer.wrap(b);
                dataBuffer.order(ByteOrder.LITTLE_ENDIAN);
                dataBuffer.rewind();
                while (dataBuffer.position() < dataBuffer.limit()) {
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
        }
    }

    public void exit() {
        this.interrupt();
    }
}
