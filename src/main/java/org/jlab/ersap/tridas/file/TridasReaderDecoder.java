package org.jlab.ersap.tridas.file;

import com.lmax.disruptor.*;
import org.jlab.ersap.tridas.TDecoder;
import org.jlab.ersap.tridas.TRingRawEvent;
import org.jlab.ersap.tridas.TRingRawEventFactory;
import sun.misc.Signal;

import java.nio.ByteBuffer;

import static com.lmax.disruptor.RingBuffer.createSingleProducer;

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
public class TridasReaderDecoder {
    private String fileName;
    private TReader reader;
    private TDecoder decoder;
    private final static int maxRingItems = 32768;
    private RingBuffer<TRingRawEvent> ringBuffer1;
    private Sequence sequence1;
    private SequenceBarrier sequenceBarrier1;
    private boolean started = false;

    public TridasReaderDecoder(String fileName) {
        this.fileName = fileName;
        ringBuffer1 = createSingleProducer(new TRingRawEventFactory(), maxRingItems,
                new YieldingWaitStrategy());
        sequence1 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier1 = ringBuffer1.newBarrier();
        ringBuffer1.addGatingSequences(sequence1);

    }

    public void go() {
        if (!started) {
            reader = new TReader(fileName, 1, ringBuffer1);
            decoder = new TDecoder(ringBuffer1, sequence1, sequenceBarrier1);

            reader.start();
            decoder.start();
            started = true;
        }
    }

    public ByteBuffer getDecodedEvent() throws Exception {
        return decoder.getEvent();
    }

    public void close() {
        started = false;
        reader.exit();
        decoder.exit();
    }

    public static void main(String[] args) {
        TridasReaderDecoder tr = new TridasReaderDecoder(args[0]);
        tr.go();
        Signal.handle(new Signal("INT"),  // SIGINT
                signal -> {
                    System.out.println("Interrupted by Ctrl+C");
                    tr.close();
                    System.exit(0);
                });

    }

}
