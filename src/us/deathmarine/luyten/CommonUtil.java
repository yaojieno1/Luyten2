package us.deathmarine.luyten;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 该类的作用
 *
 * @author IT_CREAT     
 * @date  2021 2021/5/28/028 21:02  
 */
public class CommonUtil {
    private static final Logger LOGGER = new Logger();

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Object[] collection) {
        return collection == null || collection.length == 0;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static String trim(String str) {
        if (StringUtils.isBlank(str)) {
            return "";
        }
        return str.trim();
    }

    public static List<String> trim(String[] strArrays) {
        if (ArrayUtils.isEmpty(strArrays)) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (String str : strArrays) {
            result.add(str == null ? null : str.trim());
        }
        return result;
    }

    public static List<String> trim(Collection<String> collection) {
        if (isEmpty(collection)) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (String str : collection) {
            result.add(str == null ? null : str.trim());
        }
        return result;
    }

    public static String getProjectPath() {
        try {
            // /E:/code/java/Luyten/target/classes/
            // file:/F:/学习资料/比对工具/luyten-0.7.0.jar!/resources/yml.png
            String path = CommonUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            if (StringUtils.isBlank(path)) {
                return "";
            }
            path = path.replace("+", "%2B");
            path = URLDecoder.decode(path, StandardCharsets.UTF_8.name());
            LOGGER.info("[getJarParent]old path is: %s", path);
            if (path.startsWith("file:/")) {
                path = path.substring(6);
            }
            if (path.contains("!/")) {
                path = path.substring(0, path.indexOf("!/"));
            }
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            if (path.contains("/target/classes/")) {
                path = path.substring(0, path.indexOf("/target/classes/"));
            }
            LOGGER.info("[getJarParent]transfer after path is: %s", path);
            if (FileUtil.isDirectory(path)) {
                return path;
            }
            return FileUtil.getFile(path).getParent();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return "";
    }

    public static String getTempRoot() {
        return FileUtil.splice(getProjectPath().replace("/", File.separator), "temp");
    }

    public static String getTempPath() {
        return FileUtil.splice(getTempRoot(), DateUtils.getCompactAccurateTime());
    }

    public static void main(String[] args) {
        System.out.println(getProjectPath());
    }

    public static String getShowLanguageInfo(Map<?, ?> showLanguageSetting, String key, String command) {
        Object o = showLanguageSetting.get(key);
        if (o == null) {
            return null;
        }
        Map<?, ?> info = (Map<?, ?>) o;
        Object o1 = info.get(command);
        if (o1 == null) {
            return null;
        }
        if (!(o1 instanceof String)) {
            return null;
        }
        return (String) o1;
    }

    public static Map<?, ?> getShowLanguageSetting() {
        InputStream resourceAsStream = CommonUtil.class.getResourceAsStream("/resources/language.json");
        Collection<String> lines = FileUtil.readLines(resourceAsStream, StandardCharsets.UTF_8.name(), true);
        StringBuilder stringBuilder = new StringBuilder();
        for (String line : lines) {
            stringBuilder.append(line.trim());
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(stringBuilder.toString(), Map.class);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }
}
