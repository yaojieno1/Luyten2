package us.deathmarine.luyten;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * 菜单注册
 *
 * @author IT_CREAT     
 * @date  2021 2021/5/29/029 0:07  
 */
public class MenuRegister {
    private static final Logger LOGGER = new Logger();

    public static void createProjectMenuRegistry() {
        String rootPath = new File("").getAbsolutePath();
        List<String> menus = Arrays.asList(
                getAddRegistry("HKCR\\*\\shell\\luyten", "MUIVerb", "[LuyTen]反编译jar包"),
                getAddRegistry("HKCR\\*\\shell\\luyten\\command", "",
                        String.format("\"cmd.exe /k javaw -jar -Dfile.encoding=UTF-8 %s\\luyten-0.7.0.jar %s\"", rootPath, "%%1")));
        String outPath = FileUtil.splice(rootPath, "Menu-Registry.bat");
        LOGGER.info("[Registry] out path is: %s", outPath);
        FileUtil.writeLines(menus, outPath, "GBK", false);
    }

    public static void createDeleteProjectMenuRegistry() {
        String rootPath = new File("").getAbsolutePath();
        List<String> menus = Arrays.asList(
                getDeleteRegistry(DeleteModel.ALL, "HKCR\\*\\shell\\luyten\\command", ""),
                getDeleteRegistry(DeleteModel.ALL, "HKCR\\*\\shell\\luyten", ""),
                "pause");
        String outPath = FileUtil.splice(rootPath, "Logout-Menu-Registry.bat");
        LOGGER.info("[Logout-Registry] out path is: %s", outPath);
        FileUtil.writeLines(menus, outPath, "GBK", false);
    }

    public static String getAddRegistry(String key, String value, String data) {
        String info = "";
        if (StringUtils.isBlank(value)) {
            info = String.format("REG ADD %s /ve /d %s /f", key, data);
        } else {
            info = String.format("REG ADD %s /v %s /d %s /f", key, value, data);
        }
        return info;
    }

    public static String getDeleteRegistry(DeleteModel deleteModel, String key, String value) {
        String info = "";
        if (DeleteModel.ALL.equals(deleteModel)) {
            info = String.format("REG DELETE %s /f", key);
        } else if (DeleteModel.V.equals(deleteModel)) {
            info = String.format("REG DELETE %s %s %s/f", key, DeleteModel.V.getName(), value);
        } else if (DeleteModel.VE.equals(deleteModel)) {
            info = String.format("REG DELETE %s %s/f", key, DeleteModel.VE.getName());
        } else if (DeleteModel.VA.equals(deleteModel)) {
            info = String.format("REG DELETE %s %s/f", key, DeleteModel.VA.getName());
        }
        return info;
    }

    public static enum DeleteModel {
        /**
         * 删除整个项
         */
        ALL("all"),
        /**
         * 删除项下指定的值
         */
        V("v"),
        /**
         * 删除空值名称的值
         */
        VE("ve"),
        /**
         * 删除该项下面的所有值
         */
        VA("va");

        private final String name;

        DeleteModel(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
