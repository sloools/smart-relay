package com.boot.smartrelay.beans;

import lombok.*;

/***
 *
 *  JSON
 *   {  manual 0 끈다 1 켠다
 *      'mode' : '(0, 1, s, a)'
 *      'schedule' : /~~~/
 *      'period' : update (혹시 디바이스가 리퀘스트 보내는 주기 변경할 때) (int)
 *   }
 *
 *   JSON
 *   {
 *       'currentState' : (0, 1),
 *       'mode ' : (0, 1, s, a),
 *       'schedule' : /~~~/
 *   }
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Packet {
   private String mode;
   private String schedule;
   private String period;
   private int currentState;
}
