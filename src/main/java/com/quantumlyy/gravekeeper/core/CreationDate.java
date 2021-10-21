package com.quantumlyy.gravekeeper.core;

import com.quantumlyy.gravekeeper.GraveKeeperConfig;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.time.DurationFormatUtils;

public class CreationDate {
  public long seconds;
  public String string;
  
  public CreationDate() {
    final ZonedDateTime utcTimeStamp = ZonedDateTime.now(ZoneOffset.UTC);
    seconds = utcTimeStamp.getLong(ChronoField.INSTANT_SECONDS);
    string = utcTimeStamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss.SSS"));
  }
  
  public CreationDate(final String string) {
    seconds = ZonedDateTime.parse(string, DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss.SSS").withZone(ZoneOffset.UTC))
                           .getLong(ChronoField.INSTANT_SECONDS);
    this.string = string;
  }
  
  @Nonnull
  public String getElapsedTime() {
    final long elapsedTime = seconds - new CreationDate().seconds;
    return DurationFormatUtils.formatDurationWords(Math.abs(elapsedTime * 1000L), true, true);
  }
  
  public static long getRemainingSeconds(final long creationDate) {
    if (GraveKeeperConfig.INSTANT_FOREIGN_COLLECTION) {
      return 0L;
    }
    if (GraveKeeperConfig.OWNER_ONLY_COLLECTION) {
      return Long.MAX_VALUE;
    }
    return (creationDate + GraveKeeperConfig.EXPIRE_TIME_SECONDS_VALUE) - new CreationDate().seconds;
  }
}
