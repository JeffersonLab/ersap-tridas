package org.jlab.ersap.tridas.engine.io;

import org.jlab.epsci.ersap.engine.EngineDataType;
import org.jlab.epsci.ersap.std.services.AbstractEventWriterService;
import org.jlab.epsci.ersap.std.services.EventWriterException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
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
public class ETWriter extends AbstractEventWriterService<FileWriter> {
    private static final String EVENTS = "file-events";
    private int evtCount;
    private int fileCount;
    private int numFileEvents;
    private Path file;

    @Override
    protected FileWriter createWriter(Path file, JSONObject opts)
            throws EventWriterException {
        numFileEvents = opts.has(EVENTS) ? opts.getInt(EVENTS) : 100000;
        this.file = file;
        try {
            return new FileWriter(file.toString());
        } catch (IOException e) {
            throw new EventWriterException(e);
        }
    }

    @Override
    protected void closeWriter() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void writeEvent(Object event) throws EventWriterException {
        evtCount++;
        try {
            writer.write((char[]) event);
            if (evtCount >= numFileEvents) {
                evtCount = 0;
                writer.close();
                writer = new FileWriter(file.toString() + fileCount++);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected EngineDataType getDataType() {
        return EngineDataType.BYTES;
    }
}
