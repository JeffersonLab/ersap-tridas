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
import java.nio.ByteOrder;

public class TRingRawEvent {
    private ByteBuffer payloadBuffer;
    private byte[] payload;
    private int payloadLength;
    private int numberOfEvents;


    public TRingRawEvent() {
        payload = new byte[100000];
        payloadBuffer = ByteBuffer.wrap(payload);
        payloadBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public int getPayloadLength() {
        return payloadLength;
    }

    public void setPayloadLength(int payloadLength) {
        this.payloadLength = payloadLength;
    }

    public int getNumberOfEvents() {
        return numberOfEvents;
    }

    public void setNumberOfEvents(int numberOfEvents) {
        this.numberOfEvents = numberOfEvents;
    }

    public ByteBuffer getPayloadBuffer() {
        return payloadBuffer;
    }

    public void setPayloadBuffer(ByteBuffer payloadBuffer) {
        this.payloadBuffer = payloadBuffer;
    }
}
