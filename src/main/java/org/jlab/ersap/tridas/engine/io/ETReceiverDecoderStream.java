package org.jlab.ersap.tridas.engine.io;

import org.jlab.epsci.ersap.engine.EngineDataType;
import org.jlab.epsci.ersap.std.services.AbstractEventReaderService;
import org.jlab.epsci.ersap.std.services.EventReaderException;
import org.jlab.ersap.tridas.stream.TridasReceiverDecoder;
import org.json.JSONObject;

import java.nio.ByteOrder;
import java.nio.file.Path;

/**
 * Copyright (c) 2021, Jefferson Science Associates, all rights reserved.
 * See LICENSE.txt file.
 * Thomas Jefferson National Accelerator Facility
 * Experimental Physics Software and Computing Infrastructure Group
 * 12000, Jefferson Ave, Newport News, VA 23606
 * Phone : (757)-269-7100
 *
 * @author gurjyan on 2/3/22
 * @project ersap-tridas
 */
public class ETReceiverDecoderStream extends AbstractEventReaderService<TridasReceiverDecoder> {
    private static final String PORT1 = "port1";

    @Override
    protected TridasReceiverDecoder createReader(Path file, JSONObject opts) throws EventReaderException {
        int port1 = opts.has(PORT1) ? opts.getInt(PORT1) : 6400;
        try {
            TridasReceiverDecoder t = new TridasReceiverDecoder(port1);
            t.go();
            return t;
        } catch (Exception e) {
            throw new EventReaderException(e);
        }
    }

    @Override
    protected void closeReader() {
     reader.close();
    }

    @Override
    protected int readEventCount() throws EventReaderException {
        return Integer.MAX_VALUE;
    }

    @Override
    protected ByteOrder readByteOrder() throws EventReaderException {
        return ByteOrder.LITTLE_ENDIAN;
    }

    @Override
    protected Object readEvent(int eventNumber) throws EventReaderException {
        try {
            return reader.getDecodedEvent();
        } catch (Exception e) {
            throw new EventReaderException(e);
        }
    }

    @Override
    protected EngineDataType getDataType() {
        return EngineDataType.BYTES;
    }
}
