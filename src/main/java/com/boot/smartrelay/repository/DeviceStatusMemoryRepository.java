package com.boot.smartrelay.repository;

import com.boot.smartrelay.beans.DeviceStatus;
import com.boot.smartrelay.beans.Packet;
import com.boot.smartrelay.beans.PacketList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DeviceStatusMemoryRepository {

    final ConcurrentMap<String, List<Packet>> ORDER_STATUS_DB;

    final ConcurrentMap<String, Long> DEVICE_TIME_TABLE;

    final DeviceStatusRedisRepository deviceStatusRedisRepository;

    final MongoTemplate mongoTemplate;


    /**
     * Device아이디를 기준으로, 현재 디바이스의 상태를 리턴해줍니다.
     * @param deviceId
     * @return
     */
    public DeviceStatus getDeviceStatus(String deviceId){
        DeviceStatus deviceStatus = null;
        try{
           deviceStatus =  deviceStatusRedisRepository.findById(deviceId).orElse(null);
        }catch (Exception e){
            log.error("deviceId : {} cache 조회 실패 {}", deviceId);
        }
        return deviceStatus;
    }

    public boolean setDeviceStatus(DeviceStatus deviceStatus){
        try{
            DEVICE_TIME_TABLE.put(deviceStatus.getDeviceId(), deviceStatus.getLastSec());
            deviceStatusRedisRepository.save(deviceStatus);
        }catch (Exception e){
            log.error("deviceId : {} cache 작성 실패 {}", deviceStatus.getDeviceId());
            return false;
        }
        return true;
    }

    /**
     * Order Setting입니다. deviceId를 기준으로
     * @param deviceId
     * @param order
     */
    public boolean setDeviceOrder(String deviceId, List<Packet> packets){
        try{
            //1. 오더 명령 캐시에 저장
            ORDER_STATUS_DB.put(deviceId, packets);

            //2. 오더 DB에 저장 -> for 사용자
            boolean existFlg = null != mongoTemplate.findById(deviceId, PacketList.class, CollectionBox.PACKET_LIST_COLLECTION);
            if(existFlg){//존재한다면

                mongoTemplate.remove(Query.query(where("id").is(deviceId)), CollectionBox.PACKET_LIST_COLLECTION);
            }else{      //존재하지 않는다면

            }
            mongoTemplate.save(PacketList.builder().deviceId(deviceId).packets(packets).build(), CollectionBox.PACKET_LIST_COLLECTION);
        }catch (Exception e){
            log.error("order put 에러 deviceId: {}", deviceId);
            return false;
        }
        return true;
    }

    /**
     * Order 가 있다면 가져갑니다.
     * @param deviceId
     * @return
     */
    public List<Packet> getOrderIfPresent(String deviceId){
        List<Packet> order = null;
        try{
            order = ORDER_STATUS_DB.get(deviceId);
            if(order != null){
                ORDER_STATUS_DB.remove(deviceId);
            }
        }catch (Exception e){
            log.error("device Order Map 조회 에러 deviceId: {}", deviceId);
        }
        if(order!=null){
        }
        return order;
    }

    public List<List<String>> getDevicesOnOrOff(){
        List<List<String>>  onOffDevices = new ArrayList<>();
        List<String> onDevices = new ArrayList<>();
        List<String> offDevices = new ArrayList<>();

        long now = Instant.now().getEpochSecond();
        DEVICE_TIME_TABLE.forEach((deviceId, visitedTime)->{
            if(now - visitedTime >= 30){
                if(ORDER_STATUS_DB.containsKey(deviceId)){
                    ORDER_STATUS_DB.remove(deviceId);
                }
                offDevices.add(deviceId);
            }else{
                onDevices.add(deviceId);
            }
        });

        onOffDevices.add(onDevices);
        onOffDevices.add(offDevices);
        return onOffDevices;
    }

    public PacketList getLastOrderByDeviceId(String deviceId){
        PacketList packetList = null;
        try{
            packetList = mongoTemplate.findById(deviceId, PacketList.class, CollectionBox.PACKET_LIST_COLLECTION);
        }catch(Exception e){

        }
        return packetList;
    }

}
