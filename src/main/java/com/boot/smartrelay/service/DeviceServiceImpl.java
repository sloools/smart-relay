package com.boot.smartrelay.service;

import com.boot.smartrelay.beans.*;
import com.boot.smartrelay.repository.DeviceStatusMemoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Service
public class DeviceServiceImpl implements DeviceService {

   final DeviceStatusMemoryRepository deviceStatusMemoryRepository;
   /**
    * redis에 패킷 저장
    */
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
        List<String> schedules = new ArrayList<>(); // schedule 추가 by song
        int sizeOfPackets = packets.size();
        for(int channel = 0; channel < sizeOfPackets; channel++){
            Packet packet = packets.get(channel);
            currentStates.add(packet.getCurrentState());
            modes.add(packet.getMode());
            schedules.add(packet.getSchedule());
        }

        deviceStatus.setStatus(currentStates);
        deviceStatus.setMode(modes);
        deviceStatus.setSchedule(schedules);

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

	@Override
	public boolean scheduledupcheck(String deviceId) throws ParseException {
		
		boolean isTrue = true;
		String schedule = this.getDeviceStatus(deviceId).getSchedule().toString();
				
		String db = deviceStatusMemoryRepository.getDeviceScheduleRedis(schedule);
		
		String[] dayOfWeek = schedule.split("/");
		String[] dbDayOfWeek = db.split("/");
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("HHmm");
		
		for(int i=0; i<7; i++) {						
			if(!dayOfWeek[i].equals("0")) { // 해당 요일에 입력받은 값이 있을 때
				String schOfEachDay[] = dayOfWeek[i].split(",");
				// 2개 이상의 스케줄이 입력됐을 때 입력값끼리 겹치는지 검증
				if(schOfEachDay.length >= 2) {
		               for(int n=0; n<schOfEachDay.length; n++) {
		                  for(int m=n+1; m<schOfEachDay.length; m++) {
		                	  
		                	  Date leftStartTime, leftEndTime, rightStartTime, rightEndTime;
		                	  
		                	  leftStartTime  = dateFormat.parse(schOfEachDay[n].substring(0,4));
		                	  leftEndTime    = dateFormat.parse(schOfEachDay[n].substring(4));
		                	  rightStartTime = dateFormat.parse(schOfEachDay[m].substring(0,4));
		                	  rightEndTime   = dateFormat.parse(schOfEachDay[m].substring(4));
		                	  		                	                       
		                	  isTrue = compareSchedule(leftStartTime, leftEndTime, rightStartTime, rightEndTime); 
		                	  
		                	  if(!isTrue) return false;
		                	  
		                     
		         
		                  }
		               }				
				}
				
				// Redis에 기존 스케줄이 있을 때 입력값과 겹치는지 검증
				if(!dbDayOfWeek[i].equals("0")){					
					String dbSchedule[] = dbDayOfWeek[i].split(",");
					
					for(String dbTime : dbSchedule) {
						Date dbStartTime, dbEndTime;
						
						dbStartTime = dateFormat.parse(dbTime.substring(0,4));
						dbEndTime   = dateFormat.parse(dbTime.substring(4));
												
						for(String iTime : schOfEachDay) {	
							Date iStartTime, iEndTime;
							
							iStartTime = dateFormat.parse(iTime.substring(0,4));
							iEndTime   = dateFormat.parse(iTime.substring(4));
													 
							isTrue = compareSchedule(dbStartTime, dbEndTime, iStartTime, iEndTime);
							
							if(!isTrue) return false;
						}
					}
				}	
			}
		}
		return true;
	}
	
	private boolean compareSchedule(Date firstStart, Date firstEnd, Date secondStart, Date secondEnd) {
		if(secondStart.after(firstStart) && secondStart.before(firstEnd) && secondEnd.after(firstStart) && secondEnd.before(firstEnd)) {
			System.out.println("시작 시간, 종료 시간이 모두 겹칩니다");
			return false;
		}else if(secondEnd.after(firstStart) && secondEnd.before(firstEnd)) {
			System.out.println("종료 시간이 겹칩니다");
			return false;
		}else if(secondStart.after(firstStart) && secondStart.before(firstEnd)) {
			System.out.println("시작 시간이 겹칩니다");
			return false;
		}else {
			System.out.println("시간이 겹치지 않으므로 insert 됩니다.");
			return true;
		}
	}
}
