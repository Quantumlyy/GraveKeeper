package com.quantumlytangled.gravekeeper.core;

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
    string = utcTimeStamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss.SSS"));
  }

  public static long getRemainingSeconds(long creationDate) {
    if (GraveKeeperConfig.INSTANT_FOREIGN_COLLECTION) {
      return 0L;
    }
    if (GraveKeeperConfig.OWNER_ONLY_COLLECTION) {
      return Long.MAX_VALUE;
    }
    return (creationDate + GraveKeeperConfig.EXPIRE_TIME_SECONDS) - new CreationDate().seconds;
  }
}
