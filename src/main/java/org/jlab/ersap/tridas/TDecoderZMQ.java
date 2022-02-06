package org.jlab.ersap.tridas;

import com.lmax.disruptor.*;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Copyright (c) 2021, Jefferson Science Associates, all rights reserved.
 * See LICENSE.txt file.
 * Thomas Jefferson National Accelerator Facility
 * Experimental Physics Software and Computing Infrastructure Group
 * 12000, Jefferson Ave, Newport News, VA 23606
 * Phone : (757)-269-7100
 *
 * @author gurjyan on 2/5/22
 * @project ersap-tridas
 */
public class TDecoderZMQ extends Thread {

    private AtomicBoolean running = new AtomicBoolean(true);
    private RingBuffer<TRingRawEvent> ringBuffer;
    private Sequence sequence;
    private SequenceBarrier barrier;
    private long nextSequence;
    private long availableSequence;

    public TDecoderZMQ (RingBuffer<TRingRawEvent> ringBuffer,
                        Sequence sequence,
                        SequenceBarrier barrier) {

        this.ringBuffer = ringBuffer;
        this.sequence = sequence;
        this.barrier = barrier;

        nextSequence = sequence.get() + 1L;
        availableSequence = -1L;
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

    public ByteBuffer getEvent() throws Exception {
            // Get an item from ring and parse the payload
            TRingRawEvent ringEvent = get();
            ByteBuffer payload = cloneByteBuffer(ringEvent.getPayloadBuffer());
            put();
            return payload;
        }

    public void exit() {
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
