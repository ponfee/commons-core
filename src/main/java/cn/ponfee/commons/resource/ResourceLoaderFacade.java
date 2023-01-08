/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.resource;

import cn.ponfee.commons.io.Files;
import cn.ponfee.commons.reflect.ClassUtils;
import cn.ponfee.commons.util.Strings;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 资源文件加载门面类
 * <pre>
 *  Class.getResourceAsStream(path)：以'/'开头表示classpath根路径（内部还是由ClassLoader获取），不以'/'开头表示相对此类的路径
 *  ClassLoader.getResourceAsStream(path)：从classpath根路径下获取（path不能以'/'开头）
 *  ServletContext.getResourceAsStream(path)：从WebAPP根目录下取资源，'/'开头和不以'/'开头情况一样
 * </pre>
 *
 * <ul>
 *   <li>classpath:<p>以'/'开头表示在jar包中的绝对路径（内部还是由ClassLoader获取），不以'/'开头表示在jar包中与指定类的相对路径
 *   </li>
 *   <li>webapp:</li>
 *   <li>file:</li>
 * </ul>
 * <p>default classpath:
 *
 * <pre>
 *  ResourceLoaderFacade.getResource("StringUtils.class", StringUtils.class);
 *  ResourceLoaderFacade.getResource("/mybatis-conf.xml", ResourceLoaderFacade.class); // 类所在jar包中的绝对路径
 *  ResourceLoaderFacade.getResource("mybatis-conf.xml", ResourceLoaderFacade.class); // 类所在jar包中且相对该类的路径
 *  ResourceLoaderFacade.getResource("/log4j2.xml");
 *  ResourceLoaderFacade.getResource("log4j2.xml");
 *  ResourceLoaderFacade.getResource("file:d:/import.txt");
 * </pre>
 *
 * @author Ponfee
 */
public final class ResourceLoaderFacade {

    //private static final String CP_ALL_PREFIX = "classpath*:";
    private static final String WEB_PREFIX = "webapp:";
    static final String FS_PREFIX = "file:";
    private static final Pattern PATTERN = Pattern.compile("^(\\s*(?i)(classpath|webapp|file):\\s*)?(.+)$");

    private static final ClassPathResourceLoader  CP_LOADER = new ClassPathResourceLoader();
    private static final FileSystemResourceLoader FS_LOADER = new FileSystemResourceLoader();
    private static final WebappResourceLoader    WEB_LOADER = new WebappResourceLoader();

    public static void setServletContext(@Nonnull ServletContext servletContext) {
        if (servletContext != null) {
            WEB_LOADER.setServletContext(servletContext);
        }
    }

    public static Resource getResource(String filePath, Class<?> contextClass) {
        return getResource(filePath, contextClass, null);
    }

    public static Resource getResource(String filePath, String encoding) {
        return getResource(filePath, null, encoding);
    }

    public static Resource getResource(String filePath) {
        return getResource(filePath, null, null);
    }

    /**
     * 文件资源加载
     * @param filePath      "/"表示根路径开始，其它为相对路径
     * @param contextClass
     * @param encoding
     * @return
     */
    public static Resource getResource(String filePath, Class<?> contextClass, String encoding) {
        Matcher matcher = PATTERN.matcher(filePath);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid file path: " + filePath);
        }
        if (StringUtils.isEmpty(encoding)) {
            encoding = Files.UTF_8;
        }

        filePath = Strings.cleanPath(matcher.group(3).trim());
        switch (ObjectUtils.defaultIfNull(matcher.group(1), "").toLowerCase()) {
            case FS_PREFIX:
                return FS_LOADER.getResource(filePath, encoding);
            case WEB_PREFIX:
                return WEB_LOADER.getResource(resolveWebapp(filePath), encoding);
            default:
                // 内部用的classLoader加载，不能以“/”开头，XX.class.getResourceAsStream("/com/x/file/myfile.xml")才能以“/”开头
                // "/"开头表示取根路径，非"/"开头则加上contextClass的包路径（如果contextClass不为空）
                filePath = resolveClasspath(filePath, contextClass);
                return CP_LOADER.getResource(filePath, contextClass, encoding); // 默认为classpath
        }
    }

    /**
     * 路径默认为空串
     * 
     * @param extensions
     * @param contextClass
     * @return
     */
    public static List<Resource> listResources(String[] extensions, Class<?> contextClass) {
        return listResources("", extensions, false, contextClass, Files.UTF_8);
    }

    public static List<Resource> listResources(String dir, String[] extensions, boolean recursive) {
        return listResources(dir, extensions, recursive, null, Files.UTF_8);
    }

    public static List<Resource> listResources(String dir, String[] extensions,
                                               boolean recursive, String encoding) {
        return listResources(dir, extensions, recursive, null, encoding);
    }

    /**
     * 路径匹配过滤加载
     * @param dir         "/"表示根路径开始，其它为相对路径
     * @param extensions
     * @param recursive
     * @param contextClass
     * @param encoding
     * @return
     */
    public static List<Resource> listResources(String dir, String[] extensions, boolean recursive,
                                               Class<?> contextClass, String encoding) {
        if (StringUtils.isBlank(dir)) {
            dir = ".";
        }

        Matcher matcher = PATTERN.matcher(dir);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid directory: " + dir);
        }
        if (StringUtils.isEmpty(encoding)) {
            encoding = Files.UTF_8;
        }

        dir = Strings.cleanPath(matcher.group(3).trim());
        switch (ObjectUtils.defaultIfNull(matcher.group(1), "").toLowerCase()) {
            case FS_PREFIX:
                return FS_LOADER.listResources(dir, extensions, recursive);
            case WEB_PREFIX:
                return WEB_LOADER.listResources(resolveWebapp(dir), extensions, recursive, encoding);
            default:
                // 内部用的classLoader加载，不能以“/”开头，XX.class.getResourceAsStream("/com/x/file/myfile.xml")才能以“/”开头
                // "/"开头表示取根路径，非"/"开头则加上contextClass的包路径（如果contextClass不为空）
                dir = resolveClasspath(dir, contextClass);
                return CP_LOADER.listResources(dir, extensions, recursive, contextClass, encoding); // default classpath
        }
    }

    private static String resolveWebapp(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path;
    }

    private static String resolveClasspath(String path, Class<?> contextClass) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        } else if (contextClass != null) {
            path = ClassUtils.getPackagePath(contextClass) + "/" + path;
        }
        return path;
    }

}
