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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TimerTask;


public class TPrintStatistics extends TimerTask {
    private BufferedWriter bw;
    private final boolean f_out;
    private final int streamID;

    public TPrintStatistics(boolean file_out, int streamID) {
        this.streamID = streamID;
        f_out = file_out;
        if (f_out) {
            try {
                bw = new BufferedWriter(new FileWriter("tridas_stream_" + streamID + ".csv"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        if (f_out) {
            try {
                bw.write("time-slice = "+ TReceiver.tTimeSliceId
                        + " missed-frames = "+ TReceiver.numberOfMissedFrames
                        + "\n");
                bw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(" stream:" + streamID
                + " time-slice =" + TReceiver.tTimeSliceId
                + " missed-frames =" + TReceiver.numberOfMissedFrames
        );
    }
}
