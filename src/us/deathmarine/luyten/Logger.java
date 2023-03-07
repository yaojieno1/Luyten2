package us.deathmarine.luyten;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 该类的作用
 *
 * @author IT_CREAT     
 * @date  2021 2021/5/27/027 23:08  
 */
public class Logger {
    private static final String EMPTY_STR = "";
    private static final int PERCENT_SIGN = '%';

    public void info(String info, Object... objects) {
        System.out.println(out(info, objects));
    }

    public void error(String info, Object... objects) {
        System.err.println(out(info, objects));
    }

    public void error(String info, Exception exception) {
        System.err.println(info);
        exception.printStackTrace();
    }

    public void warn(String info, Object... objects) {
        System.out.println(out(info, objects));
    }

    private String out(String info, Object... objects) {
        if (StringUtils.isBlank(info)) {
            return EMPTY_STR;
        }
        return String.format(replace(info, objects.length), objects);
    }

    private String replace(String info, int size) {
        AtomicInteger count = new AtomicInteger(0);
        int[] ints = info.chars().map(item -> {
            if (item == PERCENT_SIGN) {
                int len = count.incrementAndGet();
                if (len > size) {
                    return ' ';
                }
            }
            return item;
        }).toArray();
        return new String(ints, 0, ints.length);
    }
}
