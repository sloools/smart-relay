package com.boot.smartrelay.service;


import com.boot.smartrelay.beans.DeviceStatus;
import com.boot.smartrelay.beans.Packet;
import com.boot.smartrelay.beans.PacketList;

import java.util.List;

public interface DeviceService {
    boolean setDeviceStatus(String deviceId, List<Packet> packet);

    boolean setNewOrder(String deviceId, List<Packet> order);

    DeviceStatus getDeviceStatus(String deviceId);

    List<Packet> getOrderIfPresent(String deviceId);

    PacketList getLastOrderByDeviceId(String deviceId);
    
    boolean scheduledupcheck(String schedule) throws ParseException;
}
