package com.quantumlytangled.chestedgravestones.core;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

public class CreationDate {

  public long seconds;
  public String string;

  CreationDate() {
    final ZonedDateTime utcTimeStamp = ZonedDateTime.now(ZoneOffset.UTC);
    seconds = utcTimeStamp.getLong(ChronoField.INSTANT_SECONDS);
    string = utcTimeStamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss.n"));
  }

  public static long getRemainingSeconds(long creationDate) {
    if (ChestedGravestonesConfig.INSTANT_FOREIGN_COLLECTION) {
      return 0L;
    }
    if (ChestedGravestonesConfig.OWNER_ONLY_COLLECTION) {
      return Long.MAX_VALUE;
    }
    return (creationDate + ChestedGravestonesConfig.EXPIRE_TIME_SECONDS) - new CreationDate().seconds;
  }
}
