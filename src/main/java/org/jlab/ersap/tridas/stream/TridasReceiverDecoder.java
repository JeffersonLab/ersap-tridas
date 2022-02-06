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
import com.lmax.disruptor.*;
import org.jlab.ersap.tridas.TDecoderZMQ;
import org.jlab.ersap.tridas.TRingRawEvent;
import org.jlab.ersap.tridas.TRingRawEventFactory;
import sun.misc.Signal;

import java.nio.ByteBuffer;

import static com.lmax.disruptor.RingBuffer.createSingleProducer;

public class TridasReceiverDecoder {
    private int tPort1;
    private TReceiverZMQ receiver1;
    private TDecoderZMQ decoder;
    private final static int maxRingItems = 32768;
    private RingBuffer<TRingRawEvent> ringBuffer1;
    private Sequence sequence1;
    private SequenceBarrier sequenceBarrier1;
    private boolean started = false;

    public TridasReceiverDecoder(int port) {
        this.tPort1 = port;

        ringBuffer1 = createSingleProducer(new TRingRawEventFactory(), maxRingItems,
                new YieldingWaitStrategy());
        sequence1 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier1 = ringBuffer1.newBarrier();
        ringBuffer1.addGatingSequences(sequence1);

    }

    public void go() {
        if(!started) {
            receiver1 = new TReceiverZMQ(tPort1, 1, ringBuffer1);
            decoder = new TDecoderZMQ(ringBuffer1, sequence1, sequenceBarrier1);

            receiver1.start();
            decoder.start();
            started = true;
        }
    }

    public ByteBuffer getDecodedEvent() throws Exception {
        return decoder.getEvent();
    }

    public void close(){
        started = false;
        receiver1.exit();
        decoder.exit();
    }

    public static void main(String[] args) {
        int port1 = Integer.parseInt(args[0]);
        TridasReceiverDecoder td =  new TridasReceiverDecoder(port1);
        td.go();
        Signal.handle(new Signal("INT"),  // SIGINT
                signal -> {
                    System.out.println("Interrupted by Ctrl+C");
                    td.close();
                    System.exit(0);
                });
    }
}
