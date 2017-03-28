package com.spitchenko.focusstart.utils.logger;

import android.util.Log;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Date: 26.03.17
 * Time: 18:19
 *
 * @author anatoliy
 */
public class LogCatHandler extends Handler {

    @Override
    public void publish(final LogRecord record) {
        if (null == record)
        {
            return;
        }
        final String message = (null == record.getMessage() ? "" : record.getMessage());
        if (Level.SEVERE == record.getLevel())
        {
            Log.e(record.getLoggerName(), message);
        }
        else if (Level.WARNING == record.getLevel())
        {
            Log.w(record.getLoggerName(), message);
        }
        else
        {
            Log.i(record.getLoggerName(), message);
        }
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }

    public static void publishInfoRecord(final String message) {
        final LogCatHandler logCatHandler = new LogCatHandler();
        final LogRecord record = new LogRecord(Level.INFO, message);
        logCatHandler.publish(record);
    }
}
