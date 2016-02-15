package com.cognizant

import org.apache.log4j.Logger

/**
 * Created by guptav on 11/02/16.
 */
class JsonReaderLogger {

    private static final JsonReaderLogger logger = new JsonReaderLogger()

    static JsonReaderLogger getLogger() {
        logger
    }

    def info(String message) {
        SuccessLogger.logger.info(message)
    }

    def error(String message) {
        ErrorLogger.logger.error(message)
    }

    def error(String message, Exception ex) {
        ErrorLogger.logger.error(message, ex)
    }
}

class SuccessLogger {

    private static Logger logger = Logger.getLogger(SuccessLogger.class);

    static Logger getLogger() {
        logger
    }
}

class ErrorLogger {

    private static Logger logger = Logger.getLogger(ErrorLogger.class);

    static Logger getLogger() {
        logger
    }
}