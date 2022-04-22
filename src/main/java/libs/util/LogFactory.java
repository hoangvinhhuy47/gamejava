package libs.util;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.OutputStreamWriter;

/**
 * Created by tuanhoang on 4/13/17.
 */
public class LogFactory {
    public static Logger getLogger(String name) {
        Logger logger = Logger.getLogger(name);

        DailyRollingFileAppender appender = new DailyRollingFileAppender();
        appender.setName(name);
        String s = "log/" + name + ".log";
        appender.setFile("log/" + name + ".log");
        appender.setLayout(new PatternLayout("%d{dd-MM-yyyy HH:mm:ss} %-5p [%t] - %m%n"));
        appender.setEncoding("UTF-8");
        appender.setAppend(true);
        appender.activateOptions();

        ConsoleAppender ca = new ConsoleAppender();
        ca.setWriter(new OutputStreamWriter(System.out));
        ca.setLayout(new PatternLayout("%d{dd-MM-yyyy HH:mm:ss} %-5p [%t] - %m%n"));
        logger.addAppender(appender);
        logger.addAppender(ca);
        logger.setAdditivity(false);

        return logger;
    }
}
