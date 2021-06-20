package com.boot.smartrelay.service;

import com.boot.smartrelay.beans.*;
import com.boot.smartrelay.repository.DeviceStatusMemoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class DeviceServiceImpl implements DeviceService {

   final DeviceStatusMemoryRepository deviceStatusMemoryRepository;

    @Override
    public boolean setDeviceStatus(String deviceId, List<Packet> packets) {
        DeviceStatus deviceStatus = new DeviceStatus();
        //1. 디바이스 아이디 설정
        deviceStatus.setDeviceId(deviceId);

        //2. 마지막 커넥션 시간 갱신
        deviceStatus.setLastSec(Instant.now().getEpochSecond());

        //3. 디바이스 패킷 중  currentState 갱신
        List<Integer> currentStates = new ArrayList<>();
        List<String> modes = new ArrayList<>();
        int sizeOfPackets = packets.size();
        for(int channel = 0; channel < sizeOfPackets; channel++){
            Packet packet = packets.get(channel);
            currentStates.add(packet.getCurrentState());
            modes.add(packet.getMode());
        }

        deviceStatus.setStatus(currentStates);
        deviceStatus.setMode(modes);

        boolean setResult = deviceStatusMemoryRepository.setDeviceStatus(deviceStatus);
        return setResult;
    }

    @Override
    public boolean setNewOrder(String deviceId, List<Packet> packets) {
        boolean setResult = deviceStatusMemoryRepository.setDeviceOrder(deviceId, packets);
        return setResult;
    }

    @Override
    public List<Packet> getOrderIfPresent(String deviceId) {
        List<Packet> packets =  deviceStatusMemoryRepository.getOrderIfPresent(deviceId);
        return packets == null ? new ArrayList<Packet>() : packets;
    }

    @Override
    public DeviceStatus getDeviceStatus(String deviceId) {
        DeviceStatus deviceStatus = deviceStatusMemoryRepository.getDeviceStatus(deviceId);
        return deviceStatus == null ? new DeviceStatus() : deviceStatus;
    }

    @Override
    public PacketList getLastOrderByDeviceId(String deviceId) {
        return deviceStatusMemoryRepository.getLastOrderByDeviceId(deviceId);
    }
}
