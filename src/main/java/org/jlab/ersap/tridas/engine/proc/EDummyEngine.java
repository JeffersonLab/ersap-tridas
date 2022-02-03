package org.jlab.ersap.tridas.engine.proc;

import org.jlab.epsci.ersap.base.ErsapUtil;
import org.jlab.epsci.ersap.engine.Engine;
import org.jlab.epsci.ersap.engine.EngineData;
import org.jlab.epsci.ersap.engine.EngineDataType;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.Set;

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
public class EDummyEngine implements Engine {
    int i;
    private static final String PRINT_INTERVAL = "print-interval";
    private int pi;

    @Override
    public EngineData configure(EngineData input) {
        if (input.getMimeType().equalsIgnoreCase(EngineDataType.JSON.mimeType())) {
            String source = (String) input.getData();
            JSONObject data = new JSONObject(source);
            if (data.has(PRINT_INTERVAL)) {
                pi = data.getInt(PRINT_INTERVAL);
            }
        }
        return null;
    }

    @Override
    public EngineData execute(EngineData input) {
        if ((i++ % pi) == 0) {
            ByteBuffer bb = (ByteBuffer)input.getData();
            bb.rewind();
            System.out.println("in Java "+ bb.getInt() + " "+bb.limit());
        }
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        return input;
    }

    @Override
    public EngineData executeGroup(Set<EngineData> inputs) {
        return null;
    }

    @Override
    public Set<EngineDataType> getInputDataTypes() {
        return ErsapUtil.buildDataTypes(EngineDataType.BYTES,
                EngineDataType.JSON);
    }

    @Override
    public Set<EngineDataType> getOutputDataTypes() {
        return ErsapUtil.buildDataTypes(EngineDataType.BYTES);
    }

    @Override
    public Set<String> getStates() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Dummy engine.";
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

    @Override
    public String getAuthor() {
        return "vg";
    }

    @Override
    public void reset() {

    }

    @Override
    public void destroy() {

    }
}
