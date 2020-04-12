package cn.com.sec.ynlogurlscan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * Java8时间日期格式化：DateTimeFormatter
 */
public class TestDateTimeFormatter {
    private static Logger log = LoggerFactory.getLogger(TestDateTimeFormatter.class);
    public static void main(String[] args) {

        DateTimeFormatter dtf = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime ldt = LocalDateTime.now();

        String strDate = ldt.format(dtf);
        System.out.println(strDate);

        DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");
        String strDate2 = dtf2.format(ldt);
        log.info(strDate2);

        LocalDateTime newDate = ldt.parse(strDate2, dtf2);
        log.info("sssss {}---{}",newDate.toString(),"222");

        //对时区的操作ZonedDate/ZonedTime/ZonedDateTime
        Set<String> set = ZoneId.getAvailableZoneIds();
        set.forEach(log::info);

        LocalDateTime ldt3 = LocalDateTime.now(ZoneId.of("Europe/Tallinn"));
        log.info(ldt3.toString());

        LocalDateTime ldt4 = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        ZonedDateTime zdt = ldt4.atZone(ZoneId.of("Asia/Shanghai"));
        log.info(zdt.toString());
    }
}