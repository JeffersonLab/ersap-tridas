package org.jlab.ersap.tridas;

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
import com.lmax.disruptor.*;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;


public class TDecoder extends Thread{

    private AtomicBoolean running = new AtomicBoolean(true);
    private RingBuffer<TRingRawEvent> ringBuffer;
    private Sequence sequence;
    private SequenceBarrier barrier;
    private long nextSequence;
    private long availableSequence;

    private ExecutorService threadPool;
    private TDecodeWorkerPool pool;

    private TPDWorker myWorker;

    public TDecoder(RingBuffer<TRingRawEvent> ringBuffer,
                    Sequence sequence,
                    SequenceBarrier barrier) {

        this.ringBuffer = ringBuffer;
        this.sequence = sequence;
        this.barrier = barrier;

        nextSequence = sequence.get() + 1L;
        availableSequence = -1L;
        threadPool = Executors.newFixedThreadPool(48);
        pool = createWorkerPool(48);
    }

    private TDecodeWorkerPool createWorkerPool(int size) {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxIdle(1);
        config.setMaxTotal(size);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
        return new TDecodeWorkerPool(new TDecodeWorkerFactory(), config);
    }

    /**
     * Get the next available item from output ring buffer.
     * Do NOT call this multiple times in a row!
     * Be sure to call "put" before calling this again.
     *
     * @return next available item in ring buffer.
     * @throws InterruptedException e
     */
    public TRingRawEvent get() throws InterruptedException {
        TRingRawEvent item = null;
        try {
            if (availableSequence < nextSequence) {
                availableSequence = barrier.waitFor(nextSequence);
            }
            item = ringBuffer.get(nextSequence);
        } catch (final TimeoutException | AlertException ex) {
            // never happen since we don't use timeout wait strategy
            ex.printStackTrace();
        }
        return item;
    }

    public void put() throws InterruptedException {
        // Tell input (crate) ring that we're done with the item we're consuming
        sequence.set(nextSequence);
        // Go to next item to consume on input ring
        nextSequence++;
    }

    public void run() {
        while (running.get()) {
            try {
                // Get an item from ring and parse the payload
                TRingRawEvent buf = get();
                if (buf.getPayload().length > 0) {
                    int numOfEvents = buf.getNumberOfEvents();
                    int payloadLength = buf.getPayloadLength();

                    System.out.println(" ----- DDD -------- "+numOfEvents+" "+payloadLength);
                    byte[] ba = buf.getPayload();
                    ByteBuffer tSlice = ByteBuffer.wrap(ba);
                    System.out.println(" ----- "+ tSlice.limit()+" "+ tSlice.position());
                    tSlice.flip(); // padding
                    System.out.println(" ----- "+ tSlice.limit()+" "+ tSlice.position());
//                    int magic = tSlice.getInt();
//                    System.out.println("DDD =="+ String.format("%x", magic) + " " + magic);
//                    System.out.println("DDD =="+ String.format("%x", tSlice.getInt()) + " " + magic);
//                    System.out.println("DDD =="+ String.format("%x", tSlice.getInt()) + " " + magic);
                    System.out.printf(" ----- DDD --------");

                    ByteBuffer payload = cloneByteBuffer(buf.getPayloadBuffer());
                    put();
                    // using object pool
                    Runnable r = () -> {
                        try {
                            TPDWorker worker = pool.borrowObject();
                            worker.decode(payload, payloadLength, numOfEvents);
                            pool.returnObject(worker);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    };
                    threadPool.execute(r);
                } else {
                    put();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public byte[] getEvent () throws Exception {
        if(myWorker != null && myWorker.getEvent() != null) {
            return myWorker.getEvent();
        } else {
            pool.returnObject(myWorker);
            myWorker = pool.borrowObject();
            return myWorker.getEvent();
        }
    }

    public void exit () {
        running.set(false);
        this.interrupt();
    }

    public static ByteBuffer cloneByteBuffer(final ByteBuffer original) {

        // Create clone with same capacity as original.
        final ByteBuffer clone = (original.isDirect()) ?
                ByteBuffer.allocateDirect(original.capacity()) :
                ByteBuffer.allocate(original.capacity());

        original.rewind();
        clone.put(original);
        clone.flip();
        clone.order(original.order());
        return clone;
    }

}
