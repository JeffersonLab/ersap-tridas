package org.jlab.ersap.tridas.ana;


import j4np.hipo5.data.Bank;
import j4np.hipo5.data.Event;
import j4np.hipo5.data.Schema;
import j4np.hipo5.io.HipoWriter;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Copyright (c) 2021, Jefferson Science Associates, all rights reserved.
 * See LICENSE.txt file.
 * Thomas Jefferson National Accelerator Facility
 * Experimental Physics Software and Computing Infrastructure Group
 * 12000, Jefferson Ave, Newport News, VA 23606
 * Phone : (757)-269-7100
 *
 * @author gurjyan on 2/11/22
 * @project ersap-tridas
 */
public class DataFile2HipoBank {
    private final Schema rawSchema;
    private final HipoWriter w;
    private String inputFileName;
    private String hipoFileName;
    private DataInputStream dataInputStream;
    private ByteBuffer dataBuffer;

    public DataFile2HipoBank(String inputFileName, String hipoFileName) {
        this.inputFileName = inputFileName;
        this.hipoFileName = hipoFileName;

        Schema.SchemaBuilder builder = new Schema.SchemaBuilder("raw::data", 1200, 1);
        builder.addEntry("channel", "I", "channel number");
        builder.addEntry("slot", "I", "slot number");
        builder.addEntry("crate", "I", "crate number");
        builder.addEntry("charge", "I", "accumulated charge");
        builder.addEntry("time", "I", "time of the hit");
        builder.addEntry("frame", "L", "frame count");
        rawSchema = builder.build();
//        rawSchema.show();

        w = new HipoWriter();
        w.getSchemaFactory().addSchema(rawSchema);
//        w.getSchemaFactory().show();

        w.open(hipoFileName);

    }

    public synchronized void evtWrite(int channel, int slot, int crate, int charge, int time, long frame) {
        Event event = new Event();
        Bank rBank = new Bank(rawSchema, 1);
        int row = 0;
        rBank.putInt(0, row, channel);
        rBank.putInt(1, row, slot);
        rBank.putInt(2, row, crate);
        rBank.putInt(3, row, charge);
        rBank.putInt(4, row, time);
        rBank.putLong(5, row, frame);
        row++;
//        rBank.show();
        event.reset();
        event.write(rBank);
//        event.scanShow();
        w.addEvent(event);
    }

    public void close() {
        w.close();
    }

    public static void main(String[] args) {
        DataFile2HipoBank dfh = new DataFile2HipoBank(args[0], args[1]);

        byte[] b;
        int bytes;
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(
                    dfh.inputFileName);

            dfh.dataInputStream = new DataInputStream(inputStream);
            // Count the total bytes
            // form the input stream
            int count = inputStream.available();

            // Create byte array
            b = new byte[count];

            // Read data into byte array
            bytes = dfh.dataInputStream.read(b);

            // Print number of bytes
            // actually read
            System.out.println(bytes);
            dfh.dataBuffer = ByteBuffer.wrap(b);
            dfh.dataBuffer.order(ByteOrder.LITTLE_ENDIAN);
            dfh.dataBuffer.flip();
            dfh.dataBuffer.rewind();

        } catch (IOException e) {
            e.printStackTrace();
        }

        while (dfh.dataBuffer.position() < dfh.dataBuffer.limit()) {
            int channel = dfh.dataBuffer.getInt();
            int slot = dfh.dataBuffer.getInt();
            int crate = dfh.dataBuffer.getInt();
            int charge = dfh.dataBuffer.getInt();
            int time = dfh.dataBuffer.getInt();
            long frame_count = dfh.dataBuffer.getLong();
            System.out.println("DDD " +
                    "channel = " + channel
                    + " slot = " + slot
                    + " crate = " + crate
                    + " charge = " + charge
                    + " time = " + time
                    + " frame = " + frame_count
            );
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            dfh.evtWrite(channel, slot, crate, charge, time, frame_count);
        }
        dfh.close();
    }
}
