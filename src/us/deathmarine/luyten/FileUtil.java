package us.deathmarine.luyten;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 文件工具类
 *
 * @author IT_CREAT     
 * @date  2021 2021/4/29/029 4:56  
 */
public class FileUtil {
    private static final Logger LOGGER = new Logger();

    /**
     * 路径拼接（系统路径分割符拼接路径）
     *
     * @param paths 路径各部分
     * @return 拼接后的路径
     */
    public static String splice(String... paths) {
        if (ArrayUtils.isEmpty(paths)) {
            return "";
        }
        return String.join(File.separator, paths);
    }

    /**
     * 是否是文件
     *
     * @param path 路径
     * @return boolean
     */
    public static boolean isFile(String path) {
        return isFile(getFile(path));
    }

    /**
     * 是否是文件
     *
     * @param file 文件file
     * @return boolean
     */
    public static boolean isFile(File file) {
        return file != null && file.isFile();
    }

    /**
     * 是否是文件夹
     *
     * @param path 路径
     * @return boolean
     */
    public static boolean isDirectory(String path) {
        return isDirectory(getFile(path));
    }

    /**
     * 是否是文件夹
     *
     * @param file 文件file
     * @return boolean
     */
    public static boolean isDirectory(File file) {
        return file != null && file.isDirectory();
    }

    /**
     * 每行输出到文件
     *
     * @param writer 输出流
     * @param msg    信息
     */
    public static void println(PrintWriter writer, String msg) {
        if (writer != null && msg != null) {
            try {
                writer.println(msg);
            } catch (RuntimeException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 输出错误异常
     *
     * @param ex     异常
     * @param writer 输出流
     */
    public static void printStackTrace(Exception ex, PrintWriter writer) {
        if (ex != null && writer != null) {
            try {
                ex.printStackTrace(writer);
            } catch (RuntimeException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 获得输出流
     *
     * @param filePath 文件地址
     * @param isAppend 是否以追加内容的方式打开
     * @return PrintWriter
     */
    public static PrintWriter getPrintWriter(String filePath, boolean isAppend) {
        if (Objects.isNull(filePath)) {
            return null;
        }
        String decodePath;
        try {
            decodePath = URLDecoder.decode(filePath, "utf-8");
        } catch (IOException e) {
            LOGGER.error(String.format("decode file path exception: %s", filePath), e);
            return null;
        }
        File file = createFile(decodePath, false);
        if (file == null) {
            return null;
        }
        try {
            return new PrintWriter(new FileOutputStream(file, isAppend));
        } catch (IOException e) {
            LOGGER.error(String.format("crate file path exception: %s", filePath), e);
            return null;
        }
    }

    /**
     * 获得文件file
     *
     * @param filePath 文件地址
     * @return File
     */
    public static File getFile(String filePath) {
        if (filePath == null) {
            return null;
        }
        return new File(filePath);
    }

    /**
     * 文件是否存在
     *
     * @param filePath 文件地址
     * @return boolean
     */
    public static boolean isExists(String filePath) {
        return !Objects.isNull(filePath) && isExists(getFile(filePath));
    }

    /**
     * 文件是否存在
     *
     * @param file 文件
     * @return boolean
     */
    public static boolean isExists(File file) {
        return file != null && file.exists();
    }

    /**
     * 创建文件夹
     *
     * @param file 文件file
     * @return boolean
     */
    public static boolean mkdirs(File file) {
        if (file == null) {
            return false;
        }
        if (file.exists()) {
            return !file.isFile();
        }
        return file.mkdirs();
    }

    /**
     * 删除文件（不能删除文件夹）
     *
     * @param path 路径
     * @return 删除成功与否
     */
    public static boolean deleteFile(String path) {
        File file = getFile(path);
        return deleteFile(file);
    }

    /**
     * 删除文件（不能删除文件夹）
     *
     * @param file 文件
     * @return 删除成功与否
     */
    public static boolean deleteFile(File file) {
        if (!isExists(file)) {
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        }
        LOGGER.warn(String.format("the file to be deleted is a folder, and the deletion failed, %s", getPathInfo(file)));
        return false;
    }

    /**
     * 创建一个文件(最终处理后文件依然存在则返回文件对象，否则返回null)
     *
     * @param path           路径
     * @param isExistsDelete 存在文件是否先删除,可能会失败
     * @return File
     */
    public static File createFile(String path, boolean isExistsDelete) {
        File file = getFile(path);
        return createFile(file, isExistsDelete);
    }

    /**
     * 创建一个文件(最终处理后文件依然存在则返回文件对象，否则返回null)
     *
     * @param file           待创建file
     * @param isExistsDelete 存在文件是否先删除,可能会失败
     * @return File
     */
    public static File createFile(File file, boolean isExistsDelete) {
        if (file == null) {
            return null;
        }
        if (isExists(file)) {
            if (file.isDirectory()) {
                LOGGER.error(String.format("a folder with the same path already exists: %s", getPathInfo(file)));
                return null;
            }
            if (isExistsDelete) {
                boolean delete = deleteFile(file);
                if (!delete) {
                    LOGGER.warn(String.format("delete exists file path fail: %s", getPathInfo(file)));
                    return file;
                }
            } else {
                return file;
            }
        }
        // 删除成功后才会在下面重新创建
        boolean mkdirs = mkdirs(file.getParentFile());
        if (!mkdirs) {
            return null;
        }
        try {
            boolean newFile = file.createNewFile();
            if (newFile) {
                LOGGER.info(String.format("crate file path: %s, %s", getPathInfo(file), true));
                return file;
            }
            LOGGER.error(String.format("crate file path: %s, %s", getPathInfo(file), false));
            return null;
        } catch (IOException e) {
            LOGGER.error(String.format("crate file path exception: %s", getPathInfo(file)), e);
            return null;
        }
    }

    private static String getPathInfo(File file) {
        return StringUtils.isBlank(file.toString()) ? file.getAbsolutePath() : file.toString();
    }

    /**
     * 创建一个文件(最终处理后文件依然存在则返回文件对象，否则返回null)
     *
     * @param path           路径
     * @param isExistsDelete 存在文件是否先删除,可能会失败
     * @return File
     */
    public static File createDir(String path, boolean isExistsDelete) {
        File file = getFile(path);
        return createDir(file, isExistsDelete);
    }

    public static File createDir(File file, boolean isExistsDelete) {
        if (file == null) {
            return null;
        }
        if (isExists(file)) {
            if (file.isFile()) {
                LOGGER.error(String.format("a file with the same path already exists: %s", getPathInfo(file)));
                return null;
            }
            if (isExistsDelete) {
                boolean delete = deleteDir(file);
                if (!delete) {
                    LOGGER.warn(String.format("delete exists file path fail: %s", getPathInfo(file)));
                    return file;
                }
            } else {
                return file;
            }
        }
        return mkdirs(file) ? file : null;
    }

    /**
     * 删除文件夹
     *
     * @param path 文件地址
     * @return boolean
     */
    public static boolean deleteDir(String path) {
        return deleteDir(path, false);
    }

    /**
     * 删除文件夹
     *
     * @param path                 文件地址
     * @param isPointProjectDelete 当路径指向当前项目路径是否删除（path为空字符串的情况下）
     * @return boolean
     */
    public static boolean deleteDir(String path, boolean isPointProjectDelete) {
        if (!isPointProjectDelete && StringUtils.isBlank(path)) {
            return true;
        }
        File file = getFile(path);
        return deleteDir(file);
    }

    /**
     * 删除文件夹
     *
     * @param file 文件夹file
     * @return boolean
     */
    public static boolean deleteDir(File file) {
        if (!isExists(file)) {
            return true;
        }
        if (file.isDirectory()) {
            try {
                FileUtils.deleteDirectory(file);
                return true;
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                return false;
            }
        }
        LOGGER.warn(String.format("the file to be deleted is a file, and the deletion failed, %s", getPathInfo(file)));
        return false;
    }

    /**
     * 文件是否存在
     *
     * @param path 路径
     * @return boolean
     */
    public static boolean exists(String path) {
        return exists(getFile(path));
    }

    /**
     * 文件是否存在
     *
     * @param file 文件file
     * @return boolean
     */
    public static boolean exists(File file) {
        return file != null && file.exists();
    }

    /**
     * 关闭流
     *
     * @param closeables 流
     */
    public static void close(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }


    /**
     * 列出文件夹下所有的文件及其子目录
     *
     * @param dir 文件夹
     * @return 结果
     */
    public static List<File> listFileAndDir(String dir) {
        if (StringUtils.isBlank(dir)) {
            return Collections.emptyList();
        }
        File file = new File(dir);
        return listFileAndDir(file);
    }

    /**
     * 列出文件夹下所有的文件及其子目录
     *
     * @param file 文件夹
     * @return 结果
     */
    public static List<File> listFileAndDir(File file) {
        if (file == null || !file.exists() || !file.isDirectory()) {
            return Collections.emptyList();
        }
        ArrayList<File> results = new ArrayList<>();
        iterateFile(file, results);
        return results;
    }

    private static void iterateFile(File file, List<File> files) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null && !ArrayUtils.isEmpty(children)) {
                for (File path : children) {
                    iterateFile(path, files);
                }
            }
        } else {
            files.add(file);
        }
    }

    /**
     * 获取文件的创建时间
     *
     * @param file 文件file
     * @return 毫秒值
     */
    public static long getCreateTime(File file) {
        BasicFileAttributes fileAttributes = getFileAttributes(file);
        if (fileAttributes == null) {
            return 0;
        }
        return fileAttributes.creationTime().to(TimeUnit.MILLISECONDS);
    }

    /**
     * 获取文件的最后一次修改时间
     *
     * @param file 文件file
     * @return 毫秒值（可能为空）
     */
    public static long getLastModifiedTime(File file) {
        BasicFileAttributes fileAttributes = getFileAttributes(file);
        if (fileAttributes == null) {
            return 0;
        }
        return fileAttributes.lastModifiedTime().to(TimeUnit.MILLISECONDS);
    }

    /**
     * 获取文件的最后一次访问时间
     *
     * @param file 文件file
     * @return 毫秒值（可能为空）
     */
    public static long getLastAccessTime(File file) {
        BasicFileAttributes fileAttributes = getFileAttributes(file);
        if (fileAttributes == null) {
            return 0;
        }
        return fileAttributes.lastAccessTime().to(TimeUnit.MILLISECONDS);
    }

    /**
     * 获得文件的描述
     *
     * @param file 文件
     * @return BasicFileAttributes
     */
    public static BasicFileAttributes getFileAttributes(File file) {
        return getFileAttributes(file, BasicFileAttributes.class);
    }

    /**
     * 获得文件的描述
     *
     * @param file  文件
     * @param clazz 描述类class
     * @param <T>   BasicFileAttributes接口实现类
     * @return 描述
     */
    public static <T extends BasicFileAttributes> T getFileAttributes(File file, Class<T> clazz) {
        if (!isExists(file)) {
            return null;
        }
        T attributes;
        try {
            attributes = Files.readAttributes(file.toPath(), clazz);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
        return attributes;
    }

    /**
     * 获取输入流
     *
     * @param path 路径
     * @return InputStream
     */
    public static InputStream getInputStream(String path) {
        File file = getFile(path);
        if (exists(file) && file.isFile()) {
            try {
                return new FileInputStream(file);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * 获取BufferedReader流
     *
     * @param path    路径
     * @param charSet 文件编码
     * @return BufferedReader
     */
    public static BufferedReader getBufferedReader(String path, String charSet) {
        InputStream inputStream = getInputStream(path);
        if (inputStream == null) {
            return null;
        }
        try {
            return new BufferedReader(new InputStreamReader(inputStream, charSet));
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage(), e);
            close(inputStream);
        }
        return null;
    }

    /**
     * 获取输入流
     *
     * @param path 路径
     * @return InputStream
     */
    public static OutputStream getOutputStream(String path, boolean isAppend) {
        if (StringUtils.isBlank(path)) {
            return null;
        }
        File file = createFile(path, false);
        if (file == null) {
            return null;
        }
        try {
            return new FileOutputStream(file, isAppend);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取BufferedWriter
     *
     * @param path     路径
     * @param charSet  编码
     * @param isAppend 是否追加在文件末尾
     * @return BufferedWriter
     */
    public static BufferedWriter getBufferedWriter(String path, String charSet, boolean isAppend) {
        OutputStream outputStream = getOutputStream(path, isAppend);
        if (outputStream == null) {
            return null;
        }
        try {
            return new BufferedWriter(new OutputStreamWriter(outputStream, charSet));
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage(), e);
            close(outputStream);
        }
        return null;
    }

    /**
     * 按行写入
     *
     * @param info 信息
     * @param path 输出路径
     */
    public static void writeLines(String info, String path) {
        writeLines(info, path, StandardCharsets.UTF_8.name(), false);
    }

    /**
     * 按行写入
     *
     * @param info     信息
     * @param path     输出路径
     * @param charSet  编码
     * @param isAppend 是否存在追加在末尾
     */
    public static void writeLines(String info, String path, String charSet, boolean isAppend) {
        BufferedWriter bufferedWriter = getBufferedWriter(path, charSet, isAppend);
        try {
            if (bufferedWriter != null) {
                bufferedWriter.write(info + System.lineSeparator());
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            close(bufferedWriter);
        }
    }

    /**
     * 逐行写入
     *
     * @param infos 信息集合
     * @param path  输出路径
     */
    public static void writeLines(Collection<String> infos, String path) {
        writeLines(infos, path, StandardCharsets.UTF_8.name(), false);
    }

    /**
     * 逐行写入
     *
     * @param infos    信息集合
     * @param path     输出路径
     * @param charSet  编码
     * @param isAppend 是否存在追加在末尾
     */
    public static void writeLines(Collection<String> infos, String path, String charSet, boolean isAppend) {
        if (CommonUtil.isEmpty(infos)) {
            return;
        }
        BufferedWriter bufferedWriter = getBufferedWriter(path, charSet, isAppend);
        try {
            if (bufferedWriter != null) {
                StringBuilder stringBuilder = new StringBuilder();
                for (String info : infos) {
                    stringBuilder.append(info)
                            .append(System.lineSeparator());
                }
                bufferedWriter.write(stringBuilder.toString());
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            close(bufferedWriter);
        }
    }

    /**
     * 按行读取文件
     *
     * @param path 文件路径
     * @return 行内容集合
     */
    public static Collection<String> readLines(String path) {
        return readLines(getFile(path), StandardCharsets.UTF_8.name());
    }

    /**
     * 按行读取文件
     *
     * @param path    文件路径
     * @param charSet 文件编码
     * @return 行内容集合
     */
    public static Collection<String> readLines(String path, String charSet) {
        return readLines(getFile(path), charSet);
    }

    /**
     * 按行读取文件
     *
     * @param file 文件file
     * @return 行内容集合
     */
    public static Collection<String> readLines(File file) {
        return readLines(file, StandardCharsets.UTF_8.name());
    }

    /**
     * 按行读取文件
     *
     * @param file    文件file
     * @param charSet 文件编码
     * @return 行内容集合
     */
    public static Collection<String> readLines(File file, String charSet) {
        if (!exists(file)) {
            return Collections.emptyList();
        }
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = getBufferedReader(file.getAbsolutePath(), charSet);
            if (bufferedReader == null) {
                return Collections.emptyList();
            }
            List<String> lines = new ArrayList<>();
            String line = bufferedReader.readLine();
            while (line != null) {
                lines.add(line);
                line = bufferedReader.readLine();
            }
            return lines;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            close(bufferedReader);
        }
        return Collections.emptyList();
    }

    /**
     * 按行读取文件
     *
     * @param inputStream       输入流
     * @param charSet           文件编码
     * @param isAutoCloseStream 无论发生什么，最后都自动关闭所有流
     * @return 行内容集合
     */
    public static Collection<String> readLines(InputStream inputStream, String charSet, boolean isAutoCloseStream) {
        if (inputStream == null) {
            return Collections.emptyList();
        }
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, charSet);
            bufferedReader = new BufferedReader(inputStreamReader);
            List<String> lines = new ArrayList<>();
            String line = bufferedReader.readLine();
            while (line != null) {
                lines.add(line);
                line = bufferedReader.readLine();
            }
            return lines;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (isAutoCloseStream) {
                close(bufferedReader, inputStreamReader, inputStream);
            }
        }
        return Collections.emptyList();
    }

    /**
     * 拷贝文件
     *
     * @param srcPath           原路径
     * @param descPath          目标路径
     * @param isAutoCloseStream 是否自动关闭流（无论出现什么情况，最终都主动关闭流）
     * @return boolean
     */
    public static boolean copy(String srcPath, String descPath, boolean isAutoCloseStream) {
        return copy(getFile(srcPath), getFile(descPath), isAutoCloseStream);
    }

    /**
     * 拷贝文件
     *
     * @param srcFile           源文件
     * @param descFile          目标文件
     * @param isAutoCloseStream 是否自动关闭流（无论出现什么情况，最终都主动关闭流）
     * @return boolean
     */
    public static boolean copy(File srcFile, File descFile, boolean isAutoCloseStream) {
        if (!exists(srcFile)) {
            LOGGER.error("src path not exists:", getPathInfo(srcFile));
            return false;
        }
        File file = createFile(descFile, false);
        if (file == null) {
            LOGGER.error("desc path create fail:", getPathInfo(descFile));
            return false;
        }
        return copy(getInputStream(srcFile.getAbsolutePath()), getOutputStream(file.getAbsolutePath(), false), isAutoCloseStream);
    }

    /**
     * 拷贝文件
     *
     * @param inputStream       输入流
     * @param outputStream      输出流
     * @param isAutoCloseStream 是否自动关闭流（无论出现什么情况，最终都主动关闭流）
     * @return boolean
     */
    public static boolean copy(InputStream inputStream, OutputStream outputStream, boolean isAutoCloseStream) {
        try {
            if (inputStream == null || outputStream == null) {
                LOGGER.error("inputStream : %s, outputStream: %s ,include null values ​​in the stream", inputStream, outputStream);
                return false;
            }
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            return true;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        } finally {
            if (isAutoCloseStream) {
                close(inputStream, outputStream);
            }
        }
    }
}
