package com.boot.smartrelay.schedule;

import com.boot.smartrelay.beans.DeviceStatus;
import com.boot.smartrelay.beans.PacketList;
import com.boot.smartrelay.repository.CollectionBox;
import com.boot.smartrelay.repository.DeviceStatusMemoryRepository;
import com.boot.smartrelay.repository.DeviceStatusRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class DeviceConditionCheckServiceImpl implements DeviceConditionCheckService {

    final DeviceStatusMemoryRepository deviceStatusMemoryRepository;

    final MongoTemplate mongoTemplate;

    final ConcurrentMap<String, Boolean> ON_DEVICE_MAP;

    @Scheduled(fixedRate = 60000)
    @Override
    public void deviceServiceCheck(){
        List<List<String>> status = deviceStatusMemoryRepository.getDevicesOnOrOff();
        List<String> on = status.get(0);
        List<String> off = status.get(1);

        on.forEach((deviceId)-> {
            ON_DEVICE_MAP.put(deviceId, true);
        });
        off.forEach((deviceId)-> {
            ON_DEVICE_MAP.put(deviceId, false);
        });
        if(off.size() >= 1) {
            mongoTemplate.remove(Query.query(where("deviceId").in(off)), PacketList.class, CollectionBox.PACKET_LIST_COLLECTION);
        }


    }

    public boolean checkIsNowAliveDevice(String deviceId){
        boolean flg =  ON_DEVICE_MAP.containsKey(deviceId);
        if(!flg)return false;
        flg = ON_DEVICE_MAP.getOrDefault(deviceId, false);
        return flg;
    }


}
