package util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;

public class SharedConstants {

		public static final String BIN = "botBin"+File.separatorChar;
		public static final String COUNTER_FOLDER = BIN + "counters" + File.separatorChar;
		public static final String REACT_RECORD_FOLDER = BIN + "reactorRecords" + File.separatorChar;
		public static final String MESSAGES_FOLDER = BIN + "messages" + File.separatorChar;
		
		public static final String ROLES_FOLDER = BIN + "roles" + File.separatorChar;
		
		public static final String SQL_CONNECTION = "jdbc:derby:RPStore";
		public static Connection DATABASE_CONNECTION;
		
		public static final Logger GLOBAL_LOGGER = LoggerFactory.getLogger("Global Debug");
		
		public static final ScheduledThreadPoolExecutor SCHEDULER = new ScheduledThreadPoolExecutor(2);
		
		public static JDA jda;
		
		public static void init() throws SQLException {
				DATABASE_CONNECTION = DriverManager.getConnection(SQL_CONNECTION);
				DATABASE_CONNECTION.setAutoCommit(false);
		}
}
