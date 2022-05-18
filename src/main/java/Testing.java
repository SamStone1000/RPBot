
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntUnaryOperator;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.slf4j.LoggerFactory;

import kotlin.jvm.internal.Reflection;
import record.KickedUserHelper;
import util.SharedConstants;


public class Testing {

	public static void main (String[] args) throws SQLException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, IOException, ClassNotFoundException, InstantiationException {
		JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
		ArrayList<? extends JavaFileObject> units = new ArrayList<>();
		File file = new File("temp.java");
		file.createNewFile();
		FileWriter writer = new FileWriter(file);
		String code = "import java.util.function.IntUnaryOperator;"
				+ "public class temp implements IntUnaryOperator {"
				+ "public int applyAsInt(int i) {"
				+ "return (int) (Math.random() * 10);"
				+ "}}";
		writer.write(code);
		writer.close();
		Iterable<? extends JavaFileObject> files = javac.getStandardFileManager(null, null, null).getJavaFileObjects(file);
		javac.getTask(null, null, null, null, null, files).call();
		URLClassLoader loader = new URLClassLoader(new URL[] {new File("./").toURI().toURL()});
		Class<?> clazz = loader.loadClass("temp");
		IntUnaryOperator foo = (IntUnaryOperator) clazz.newInstance();
		System.out.println(foo.applyAsInt(10));
	}
}
