package util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;

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
		
		public static void init(JDA jda) throws SQLException {
			SharedConstants.jda = jda;
			DATABASE_CONNECTION = DriverManager.getConnection(SQL_CONNECTION);
			DATABASE_CONNECTION.setAutoCommit(false);
			if (jda == null) return;
			Logger logger = LoggerFactory.getLogger(SharedConstants.class);
			Statement statement = DATABASE_CONNECTION.createStatement();
			ResultSet tables = DATABASE_CONNECTION.getMetaData().getTables(null, null, null, new String[] {"TABLE"});
			HashSet<String> existingTables = new HashSet<String>();
			while (tables.next())
			{
				existingTables.add(tables.getString("TABLE_NAME"));
			}
			for (String s : existingTables)
			{
				logger.debug(s);
			}
			List<Guild> guilds = jda.getGuilds();
			for (Guild guild : guilds)
			{
				List<GuildChannel> channels = guild.getChannels();
				for (GuildChannel channel : channels)
				{
					logger.debug(channel.toString());
					logger.debug(channel.getType().toString());
					if (channel.getType() != ChannelType.TEXT) continue;
					logger.debug("foo");
					String tableName = "CHANNEL"+channel.getId();
					if (existingTables.contains(tableName)) continue;
					logger.info("Creating table for "+channel.toString());
					statement.execute("CREATE TABLE "+tableName+"(author BIGINT, content VARCHAR(2000), flags BIGINT, fromWebHook BOOLEAN, id BIGINT PRIMARY KEY, isTTS BOOLEAN, referenceMessage BIGINT, referenceChannel BIGINT, referenceGuild BIGINT, isPinned BOOLEAN, type INT)");
				}
			}
			DATABASE_CONNECTION.commit();
		}
}
