package com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor.contract.compile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.loader.LaunchedURLClassLoader;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.*;
import java.io.*;
import java.net.*;
import java.nio.CharBuffer;
import java.util.*;
import java.util.jar.JarEntry;
/**
 * @version V1.0
 * @Title: CusCompiler
 * @Package com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor.contract.compile
 * @Description:
 * @author: xuxm
 * @date: 2020/9/29 23:25
 */
/**
 * Created by xidongzhou1 on 2020/1/17.
 */
public class CusCompiler {
     static final Logger log = LoggerFactory.getLogger(CusCompiler.class);
    static class CustomJavaFileObject implements JavaFileObject {
        private String binaryName;
        private URI uri;
        private String name;

        public String binaryName() {
            return binaryName;
        }

        public CustomJavaFileObject(String binaryName, URI uri) {
            this.uri = uri;
            this.binaryName = binaryName;
            name = uri.getPath() == null ? uri.getSchemeSpecificPart() : uri.getPath();
        }

        @Override
        public Kind getKind() {
            return Kind.CLASS;
        }

        @Override
        public boolean isNameCompatible(String simpleName, Kind kind) {
            String baseName = simpleName + kind.extension;
            return kind.equals(getKind()) && (baseName.equals(getName()) || getName().endsWith("/" + baseName));
        }

        @Override
        public NestingKind getNestingKind() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Modifier getAccessLevel() {
            throw new UnsupportedOperationException();
        }

        @Override
        public URI toUri() {
            return uri;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public InputStream openInputStream() throws IOException {
            return uri.toURL().openStream();
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Writer openWriter() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getLastModified() {
            return 0;
        }

        @Override
        public boolean delete() {
            throw new UnsupportedOperationException();
        }
    }

    static class MemoryInputJavaFileObject extends SimpleJavaFileObject {
        final String code;

        MemoryInputJavaFileObject(String name, String code) {
            super(URI.create(name.replaceAll("\\.", "/") + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharBuffer getCharContent(boolean ignoreEncodingErrors) {
            return CharBuffer.wrap(code);
        }
    }

    static class MemoryOutputJavaFileObject extends SimpleJavaFileObject {
        final String name;
        Map<String, byte[]> class_out;

        MemoryOutputJavaFileObject(String name, Map<String, byte[]> out) {
            super(URI.create(name.replaceAll("\\.", "/") + Kind.SOURCE.extension), Kind.CLASS);
            this.name = name;
            this.class_out = out;
        }

        @Override
        public OutputStream openOutputStream() {
            return new FilterOutputStream(new ByteArrayOutputStream()) {
                @Override
                public void close() throws IOException {
                    out.close();
                    ByteArrayOutputStream bos = (ByteArrayOutputStream) out;
                    class_out.put(name, bos.toByteArray());
                }
            };
        }
    }

    static class SpringBootJarFileManager implements JavaFileManager {
        private URLClassLoader classLoader;
        private StandardJavaFileManager standardJavaFileManager;
        final Map<String, byte[]> classBytes = new HashMap<>();

        SpringBootJarFileManager(StandardJavaFileManager standardJavaFileManager, URLClassLoader systemLoader) {
            this.classLoader = new URLClassLoader(systemLoader.getURLs(), systemLoader);
            this.standardJavaFileManager = standardJavaFileManager;
        }

        @Override
        public ClassLoader getClassLoader(Location location) {
            return classLoader;
        }

        private List<JavaFileObject> find(String packageName) {
            List<JavaFileObject> result = new ArrayList<>();
            String javaPackageName = packageName.replaceAll("\\.", "/");
            try {
                Enumeration<URL> urls = classLoader.findResources(javaPackageName);
                while (urls.hasMoreElements()) {
                    URL ll = urls.nextElement();
                    String ext_form = ll.toExternalForm();
                    int ext_formIndex=ext_form.lastIndexOf("!");
                    if(ext_formIndex<0) continue;
                    String jar = ext_form.substring(0, ext_formIndex);
                    String pkg = ext_form.substring(ext_formIndex + 1);

                    JarURLConnection conn = (JarURLConnection) ll.openConnection();
                    conn.connect();
                    Enumeration<JarEntry> jar_items = conn.getJarFile().entries();
                    while (jar_items.hasMoreElements()) {
                        JarEntry item = jar_items.nextElement();
                        if (item.isDirectory() || (!item.getName().endsWith(".class"))) {
                            continue;
                        }
                        if (item.getName().lastIndexOf("/") != (pkg.length() - 1)) {
                            continue;
                        }
                        String name = item.getName();
                        URI uri = URI.create(jar + "!/" + name);
                        String binaryName = name.replaceAll("/", ".");
                        binaryName = binaryName.substring(0, binaryName.indexOf(JavaFileObject.Kind.CLASS.extension));
                        result.add(new CustomJavaFileObject(binaryName, uri));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
            Iterable<JavaFileObject> ret = null;
            if (location == StandardLocation.PLATFORM_CLASS_PATH) {
                ret = standardJavaFileManager.list(location, packageName, kinds, recurse);
            } else if (location == StandardLocation.CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS)) {
                ret = find(packageName);
                if (ret == null || (!ret.iterator().hasNext())) {
                    ret = standardJavaFileManager.list(location, packageName, kinds, recurse);
                }
            } else {
                ret = Collections.emptyList();
            }
            return ret;
        }

        @Override
        public String inferBinaryName(Location location, JavaFileObject file) {
            String ret = "";
            if (file instanceof CustomJavaFileObject) {
                ret = ((CustomJavaFileObject)file).binaryName;
            } else {
                ret = standardJavaFileManager.inferBinaryName(location, file);
            }
            return ret;
        }

        @Override
        public boolean isSameFile(FileObject a, FileObject b) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean handleOption(String current, Iterator<String> remaining) {
            return standardJavaFileManager.handleOption(current, remaining);
        }

        @Override
        public boolean hasLocation(Location location) {
            return location == StandardLocation.CLASS_PATH || location == StandardLocation.PLATFORM_CLASS_PATH;
        }

        @Override
        public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
            classBytes.clear();
        }

        @Override
        public int isSupportedOption(String option) {
            return -1;
        }

        public Map<String, byte[]> getClassBytes() {
            return new HashMap<String, byte[]>(this.classBytes);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind,
                                                   FileObject sibling) throws IOException {
            if (kind == JavaFileObject.Kind.CLASS) {
                return new MemoryOutputJavaFileObject(className, classBytes);
            } else {
                return standardJavaFileManager.getJavaFileForOutput(location, className, kind, sibling);
            }
        }
    }

    private static class MemoryClassLoader extends LaunchedURLClassLoader {
        Map<String, byte[]> classBytes = new HashMap<>();

        public MemoryClassLoader(Map<String, byte[]> classBytes, ClassLoader classLoader) {
            super(new URL[0], classLoader);
            this.classBytes.putAll(classBytes);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            System.out.println("findClass: " + name);
            byte[] buf = classBytes.get(name);
            if (buf == null) {
                return super.findClass(name);
            }
            classBytes.remove(name);
            return defineClass(name, buf, 0, buf.length);
        }
    }


    public Class<?> loadClass(String name, Map<String, byte[]> classBytes) throws Exception {
        ClassLoader loader = new ClassLoader() {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                Class<?> r = null;
                if (classBytes.containsKey(name)) {
                    byte[] buf = classBytes.get(name);
                    r =  defineClass(name, buf, 0, buf.length);
                } else {
                    r = systemClassLoader.loadClass(name);
                }
                return r;
            }
        };
        return loader.loadClass(name);
    }

    private URLClassLoader systemClassLoader;
    public CusCompiler(URLClassLoader loader) {
        systemClassLoader = loader;
    }
    static DiagnosticCollector<JavaFileObject> DIAGNOSTIC_COLLECTOR = new DiagnosticCollector<>();

    public Map<String, byte[]> compile(String className, String code) throws Exception {
        long aa=System.currentTimeMillis();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager stdManager = compiler.getStandardFileManager(DIAGNOSTIC_COLLECTOR, null, null);
        SpringBootJarFileManager springBootJarFileManager = new SpringBootJarFileManager(stdManager, systemClassLoader);
        JavaFileObject javaFileObject = new MemoryInputJavaFileObject(className, code);
        log.info("编译智能合约耗时_编译_1:{}",System.currentTimeMillis()-aa);
        List<String> options = new ArrayList<>();
        long bb=System.currentTimeMillis();
        //options.addAll(Arrays.asList("-classpath", System.getProperty("java.class.path"), "-bootclasspath", System.getProperty("sun.boot.class.path"), "-extdirs", System.getProperty("java.ext.dirs")));
        JavaCompiler.CompilationTask task = compiler.getTask(null, springBootJarFileManager, DIAGNOSTIC_COLLECTOR, null, null, Arrays.asList(javaFileObject));
        Boolean compileRet= task.call();
        log.info("编译智能合约耗时_编译_2:{}",System.currentTimeMillis()-bb);
        if (compileRet == null || (!compileRet.booleanValue())) {
            log.error("DIAGNOSTIC_COLLECTOR={}",DIAGNOSTIC_COLLECTOR.getDiagnostics());
            throw new RuntimeException("java filter compile error");
        }
        for (String key : springBootJarFileManager.getClassBytes().keySet()) {
            log.info("class: " + key + " len: " + Integer.valueOf(springBootJarFileManager.getClassBytes().get(key).length).toString());
        }
        return springBootJarFileManager.getClassBytes();
    }
}
