package com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor.contract.compile;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

/**
 * In-memory compile Java source code as String.
 * 
 * @author
 */
public class JavaStringCompiler {
	private static final Logger log = LoggerFactory.getLogger(JavaStringCompiler.class);
	/**
	 * 编译诊断收集器
	 */
	static DiagnosticCollector<JavaFileObject> DIAGNOSTIC_COLLECTOR = new DiagnosticCollector<>();

	JavaCompiler compiler;
	StandardJavaFileManager stdManager;

	public JavaStringCompiler() {
		this.compiler = ToolProvider.getSystemJavaCompiler();
		this.stdManager = compiler.getStandardFileManager(DIAGNOSTIC_COLLECTOR, null, null);;
	}

	/**
	 * Compile a Java source file in memory.
	 * 
	 * @param fileName
	 *            Java file name, e.g. "Test.java"
	 * @param source
	 *            The source code as String.
	 * @return The compiled results as Map that contains class name as key,
	 *         class binary as value.
	 * @throws IOException
	 *             If compile error.
	 */
	public Map<String, byte[]> compile(String fileName, String source) throws IOException {
		// 设置编译参数
/*		List<String> options = new ArrayList<>();
		options.add("-source");
		options.add("1.6");
		options.add("-target");
		options.add("1.6");*/
		try (MemoryJavaFileManager manager = new MemoryJavaFileManager(stdManager)) {
			//log.info("fileName={}",fileName);
			log.info("source={}",source);
			JavaFileObject javaFileObject = manager.makeStringSource(fileName, source);
			//log.info("javaFileObject={}",JSONObject.toJSONString(javaFileObject));
			CompilationTask task = compiler.getTask(null, manager, DIAGNOSTIC_COLLECTOR, null, null, Arrays.asList(javaFileObject));
			Boolean result = task.call();
			if (result == null || !result.booleanValue()) {
				log.error("DIAGNOSTIC_COLLECTOR={}",DIAGNOSTIC_COLLECTOR.getDiagnostics());
				throw new RuntimeException("Compilation failed.");
			}
			return manager.getClassBytes();
		}
/*		catch (Throwable throwable)
		{
			throw new RuntimeException(throwable.getCause());
		}*/
	}


	/**
	 * Load class from compiled classes.
	 * 
	 * @param name
	 *            Full class name.
	 * @param classBytes
	 *            Compiled results as a Map.
	 * @return The Class instance.
	 * @throws ClassNotFoundException
	 *             If class not found.
	 * @throws IOException
	 *             If load error.
	 */
	public Class<?> loadClass(String name, Map<String, byte[]> classBytes) throws ClassNotFoundException, IOException {
		try (MemoryClassLoader classLoader = new MemoryClassLoader(classBytes)) {
			return classLoader.loadClass(name);
		}
	}
}
