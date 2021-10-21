package com.quantumlyy.gravekeeper.util;

import com.quantumlyy.gravekeeper.GraveKeeper;

import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.logging.log4j.Level;

// PrintStream wrapper for the mod logger, useful for exception stack dumps
public class LoggerPrintStream extends PrintStream {
	
	protected final Level level;
	
	public LoggerPrintStream(final Level level) {
		super(new OutputStream() {
			@Override
			public void write(final int b) {
				// no operation
			}
		});
		this.level = level;
	}
	
	@Override
	public void println(final String message) {
		this.logString(message);
	}
	
	@Override
	public void println(final Object object) {
		this.logString(String.valueOf(object));
	}
	
	protected void logString(final String message) {
		GraveKeeper.logger.log(level, message);
	}
}
