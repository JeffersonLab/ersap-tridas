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

/**
 * Event byte[] will have the following structure:
 * uint32_t number of hits
 * uint64_t StartTime in ns
 * uint32_t TSCompleted flag for event belonging to an (un)completed (0)1 TS
 * <p>
 * unsigned int nseeds[6] array that store the number of seeds per trigger type
 * unsigned int plugin_trigtype[8] array - seed-found flags for each kind of plugin
 * unsigned int plugin_nseeds[8] array - n-seeds for each kind of L2 algorithm
 * unsigned int Channel
 * unsigned int Slot
 * unsigned int Crate
 * unsigned int Charge
 * unsigned int T1ns 1 ns counter, 0-65536ns
 * unsigned long long int VTP frame counter
 */
public class TPDWorker {
    private final static int TEventTag = 12081972;
    private final List<byte[]> events = new ArrayList<>();
    private int currentIndex;

    public void decode(ByteBuffer tSlice, int tsLength, int nEvents) {

        events.clear();
        System.out.println("========= DDD============");
        System.out.println("Number of events = " + nEvents);

        for (int i = 0; i < nEvents; i++) {
            tSlice.getInt(); // padding
            int magic = tSlice.getInt();
            System.out.println("DDD ==" + String.format("magic = %x", magic) + " " + magic);
            int evtId = tSlice.getInt();
            System.out.println("DDD ==" + String.format("evtId = %x", evtId) + " " + evtId);
            int evtLength = tSlice.getInt();

            if (magic == TEventTag) {
                final byte[] dst = new byte[evtLength - 16];
                tSlice.get(dst);
                events.add(dst);
            }
        }
        currentIndex = events.size();
            System.out.println("========= DDD============\n");
        }

        public byte[] getEvent () {
            if (events.size() < currentIndex) {
                currentIndex++;
                return events.get(currentIndex);
            } else {
                return null;
            }
        }
    }
