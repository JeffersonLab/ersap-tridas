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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class TPDWorker {
    private final static int TEventTag = 12081972;
    private final List<byte[]> events = new ArrayList<>();
    private int currentIndex;

    public void decode(ByteBuffer tSlice, int tsLength, int nEvents) {

        tSlice.rewind();
        events.clear();

        tSlice.getInt(); // padding
            int magic = tSlice.getInt();
            System.out.println("DDD =="+ String.format("%x", magic) + " " + magic);
            System.out.println("DDD =="+ String.format("%x", tSlice.getInt()) + " " + magic);
            System.out.println("DDD =="+ String.format("%x", tSlice.getInt()) + " " + magic);

//        for (int i = 0; i <= nEvents; i++) {
//            int evtTag = tSlice.getInt();
//            int evtId = tSlice.getInt();
//            tSlice.mark();
//            int evtLength = tSlice.getInt();
//            if (evtTag == TEventTag) {
//                tSlice.reset();
//                final byte[] dst = new byte[evtLength - 8];
//                tSlice.get(dst);
//                events.add(dst);
//            }
//        }
//        currentIndex = events.size();
    }

    public byte[] getEvent() {
        if (events.size() < currentIndex) {
            currentIndex++;
            return events.get(currentIndex);
        } else {
            return null;
        }
    }
}
