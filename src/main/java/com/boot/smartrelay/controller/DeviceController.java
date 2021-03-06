package com.boot.smartrelay.controller;
import com.boot.smartrelay.beans.DeviceStatus;
import com.boot.smartrelay.beans.Packet;
import com.boot.smartrelay.beans.PacketList;
import com.boot.smartrelay.beans.PacketWrapper;
import com.boot.smartrelay.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@RequestMapping("/device")
@RequiredArgsConstructor
@Controller
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping(value = "/order/{deviceId}", consumes = "application/json", produces = "application/json")
    @ResponseBody
    List<Packet> connectionWithDevice(@PathVariable("deviceId") String deviceId, @RequestBody List<Packet> packets){

        log.info("packet 도착 - 커넥션 확인 with deviceId : " + deviceId);
        //1. status 설정
        deviceService.setDeviceStatus(deviceId , packets);
        //2. if 새로운 order 가 있다면 get

        List<Packet> result = deviceService.getOrderIfPresent(deviceId);
        return result;
    }

    @GetMapping(value = "/status/{deviceId}")
    @ResponseBody
    DeviceStatus deviceStatus(@PathVariable("deviceId") String deviceId){
        return deviceService.getDeviceStatus(deviceId);
    }


    @PostMapping(value = "/user", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public String setOrder(@RequestBody PacketList packetList){
        boolean result = deviceService.setNewOrder(packetList.getDeviceId(), packetList.getPackets());
        if(result){
            return "Y";
        }
        return "N";
    }

    @PostMapping(value = "/user/scheduleMode")
    public String setScheduledOrder(ModelMap model, @RequestParam("deviceId") String deviceId, @RequestParam("channel") int channel, @ModelAttribute Packet packet){
        if(channel <= 0 || !StringUtils.hasLength(deviceId)){
            //TODO 예외처리 하기
            return "ERROR";
        }
        
         // 스케줄 시간 검증 로직
        boolean scheduleValidation;
		try {
			scheduleValidation = deviceService.scheduledupcheck(packet.getSchedule());
			if(!scheduleValidation) {
				return "ERROR";
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        deviceService.setNewOrder(deviceId, makePacketList(channel, packet));
        model.addAttribute("message", "스케쥴 모드 모드 설정이 완료되었습니다.");
        return "user/order_add_success";
    }


    @GetMapping(value = "/user/autoMode")
    public String autoMode(ModelMap model, @RequestParam("deviceId") String deviceId,  @RequestParam("channel") int channel){
        if(channel <= 0 || !StringUtils.hasLength(deviceId)){
            //TODO 예외처리 하기
            return "user/order_add_success";
        }
        Packet packet = Packet.builder().mode("a").build();
        deviceService.setNewOrder(deviceId, makePacketList(channel, packet));
        model.addAttribute("message", "오토 모드 설정 완료되었습니다.");
        return "user/order_add_success";
    }

    @GetMapping(value = "/user/offMode")
    public String offMode(ModelMap model, @RequestParam("deviceId") String deviceId, @RequestParam("channel") int channel){
        if(channel <= 0 || !StringUtils.hasLength(deviceId)){
            //TODO 예외처리 하기
            return "ERROR";
        }
        Packet packet = Packet.builder().mode("0").build();
        deviceService.setNewOrder(deviceId, makePacketList(channel, packet));
        model.addAttribute("message", "전원 종료 설정이 완료되었습니다.");
        return "user/order_add_success";
    }

    @GetMapping(value = "/user/onMode")
    public String onMode(ModelMap model, @RequestParam("deviceId") String deviceId, @RequestParam("channel") int channel){
        if(channel <= 0 || !StringUtils.hasLength(deviceId)){
            //TODO 예외처리 하기
            return "ERROR";
        }
        Packet packet = Packet.builder().mode("1").build();
        deviceService.setNewOrder(deviceId, makePacketList(channel, packet));
        model.addAttribute("message", "전원 ON 설정이 완료되었습니다.");
        return "user/order_add_success";
    }

    @GetMapping(value = "/user/repeatMode")
    public String repeatMode(ModelMap model, @RequestParam("deviceId") String deviceId, @RequestParam("channel") int channel, @RequestParam("schedule") String schedule){
        if(channel <= 0 || !StringUtils.hasLength(deviceId)){
            //TODO 예외처리 하기
            return "ERROR";
        }
        Packet packet = Packet.builder().mode("r").schedule(schedule).build();
        deviceService.setNewOrder(deviceId, makePacketList(channel, packet));
        model.addAttribute("message", "반복 모드 설정이 완료되었습니다.");
        return "user/order_add_success";
    }


    private List<Packet> makePacketList(int channel, Packet packet){
        List<Packet> packets = new ArrayList<>();
        for(int k = 1; k <= 3; k++){
            if(channel == k){
                packets.add(packet);
            }else{
                packets.add(Packet.builder().mode("x").build());
            }
        }
        return packets;
    }
}
