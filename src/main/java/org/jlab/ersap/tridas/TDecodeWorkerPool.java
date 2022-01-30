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
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class TDecodeWorkerPool extends GenericObjectPool<TPDWorker> {

    public TDecodeWorkerPool(PooledObjectFactory<TPDWorker> factory) {
        super(factory);
    }

    public TDecodeWorkerPool(PooledObjectFactory<TPDWorker> factory,
                             GenericObjectPoolConfig config) {
        super(factory, config);
    }
}
