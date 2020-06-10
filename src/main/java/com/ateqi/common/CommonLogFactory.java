package com.ateqi.common;

import org.apache.log4j.Logger;

public class CommonLogFactory {
    public static final Logger processDataLog;
    public static final Logger exceptionLog;

    static {
        processDataLog = Logger.getLogger("processdatafile");
        exceptionLog = Logger.getLogger("exceptionLog");
    }

    public static Logger getProcessDataLog() {
        return processDataLog;
    }

    public static Logger getExceptionLog() {
        return exceptionLog;
    }
}
