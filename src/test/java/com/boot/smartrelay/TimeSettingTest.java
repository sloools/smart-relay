package com.boot.smartrelay;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.Date;

public class TimeSettingTest {
    @Test
    void testUtcZeroTime(){
        Instant instant = Instant.now();

        String output = instant.toString();
        System.out.println(output);
        System.out.println(instant.getEpochSecond());
        System.out.println(System.currentTimeMillis() / 1000L);
        System.out.println(Instant.now().toString());
    }



}
