package util;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SharedConstants {

		public static final String BIN = "botBin"+File.separatorChar;
		public static final String COUNTER_FOLDER = BIN + "counters" + File.separatorChar;
		public static final String REACT_RECORD_FOLDER = BIN + "reactorRecords" + File.separatorChar;
		public static final String MESSAGES_FOLDER = BIN + "messages" + File.separatorChar;
		
		public static final String ROLES_FOLDER = BIN + "roles" + File.separatorChar;
		
		public static final Logger GLOBAL_LOGGER = LoggerFactory.getLogger("Global Debug");
}
