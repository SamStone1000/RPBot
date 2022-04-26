import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import record.Messages;
import record.OfflineMessage;
import util.Helper;
import util.SharedConstants;

public class DatabaseConverter {

	public DatabaseConverter() {}

	public static void main(String[] args) throws SQLException {
		SharedConstants.init();
		//Statement statement = SharedConstants.DATABASE_CONNECTION.createStatement();
		//statement.execute("CREATE TABLE Channel577853894147702817(id BIGINT PRIMARY KEY, author BIGINT NOT NULL, content VARCHAR(2000) NOT NULL)");
		//statement.close();
		try (InputStream in = new FileInputStream(new File(SharedConstants.MESSAGES_FOLDER + "577853894147702817.txt"));
				BufferedInputStream buffer = new BufferedInputStream(in);
				Statement statement = SharedConstants.DATABASE_CONNECTION.createStatement();
				PreparedStatement prepared = SharedConstants.DATABASE_CONNECTION.prepareStatement("INSERT INTO Channel577853894147702817 VALUES(?, ?, ?)"))
		{
			//statement.execute("CREATE TABLE Channel577853894147702817(id BIGINT PRIMARY KEY, author BIGINT NOT NULL, content VARCHAR(2000) NOT NULL)");
			/*
			buffer.skip(Long.BYTES); // skip most recent id
			byte[] lengthBytes = new byte[Integer.BYTES];
			while (buffer.read(lengthBytes) > 0)
			{// stop searching if the end of file has been reached
				int length = Helper.bytesToInt(lengthBytes);
				byte[] messageBytes = buffer.readNBytes((int) length);
				OfflineMessage message = Messages.bytesToMessage(messageBytes);
				prepared.setLong(1, message.getIdLong());
				prepared.setLong(2, message.getAuthorLong());
				prepared.setString(3, message.getContentRaw());
				prepared.execute();
			}
			*/
			long start = System.currentTimeMillis();
			statement.execute("SELECT content FROM Channel577853894147702817");
			long end = System.currentTimeMillis();
			System.out.println(end - start + " ms");
			ResultSet rs = statement.getResultSet();
			rs.next();
			System.out.println(rs.getString(1));
			
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			SharedConstants.DATABASE_CONNECTION.commit();
		}
	}
}
