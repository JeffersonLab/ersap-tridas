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
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class TDecodeWorkerFactory extends BasePooledObjectFactory<TPDWorker> {

    @Override
    public TPDWorker create() throws Exception {
        return new TPDWorker();
    }

    @Override
    public PooledObject<TPDWorker> wrap(TPDWorker payloadDecoder) {
        return new DefaultPooledObject<TPDWorker>(payloadDecoder);
    }
}