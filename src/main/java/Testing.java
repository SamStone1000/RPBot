import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.TimeZone;

import net.dv8tion.jda.api.utils.TimeUtil;

public class Testing {

	public static void main(String[] args) throws IOException {
		System.out.println(ZoneId.systemDefault().getRules());
	}

}
