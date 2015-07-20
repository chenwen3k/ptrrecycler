package ptrrecycler.wendy.com.util;

import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Date;

public class DateTimeUtils {
    public static final long second = 1000;
    public static final long minute = 60 * second;
    public static final long hour = 60 * minute;
    public static final long day = 24 * hour;
    public static final long SIMPLE_MDD_CACHE_TIME = 10 * day;
    public static String getDateInMillis(long timeSeconds, String format) {
        return DateFormat.format(format, timeSeconds).toString();
    }
    public static String getRefreshTimeText(long refreshTime){
        if(refreshTime == 0){
            return "";
        }
        String tips;
        long now = System.currentTimeMillis();
        long offset = now - refreshTime;
        Date nowDate = new Date(now);
        Date reDate = new Date(refreshTime);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(nowDate);
        int y = calendar.get(Calendar.YEAR);
        calendar.setTime(reDate);
        int y1 = calendar.get(Calendar.YEAR);
        if(offset < minute){
            tips = "刚刚";
        }else if(offset >= minute && offset < hour){
            tips = (offset / minute) + "分钟以前";
        }else if(offset >= hour && offset < day){
            tips = (offset / hour) + "小时以前";
        }else if(offset >= day && offset < day * 3){
            tips = (offset / day) + "天以前";
        }else if(offset >= day * 3 && y == y1){
            tips = DateTimeUtils.getDateInMillis(refreshTime, "MM月dd日");
        }else{
            tips = DateTimeUtils.getDateInMillis(refreshTime, "yyyy年MM月dd日");
        }
        return tips;
    }
}
