import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


public class Testing {

	public static void main (String[] args) throws SQLException {
		String dataBaseName = "test";
		long start = System.currentTimeMillis();
		System.setProperty("derby.language.sequence.preallocator", "1");
		Connection conn = DriverManager.getConnection("jdbc:derby:"+dataBaseName +";create=true");
		conn.setAutoCommit(false);
		long end = System.currentTimeMillis();
		System.out.println(end - start);
		Statement statement = conn.createStatement();
		//statement.execute("DROP TABLE lyrics");
		//statement.execute("create table lyrics(internalID SMALLINT GENERATED ALWAYS AS IDENTITY (INCREMENT BY 1, CYCLE), authorid bigint, content varchar(2000))");
		PreparedStatement prepared = conn.prepareStatement("insert into lyrics values(DEFAULT, ?, ?)");
		
		prepared.setLong(1, (long) (Math.random() * Long.MAX_VALUE));
		prepared.setString(2, "foo");
		prepared.execute();
			
			statement.execute("select internalID from lyrics");
			ResultSet rs = statement.getResultSet();
			
			
			ArrayList<Short> ids = new ArrayList<Short>();
			while (rs.next()) {
				ids.add(rs.getShort(1));
			}
			
			statement.execute("select * from lyrics");
			rs = statement.getResultSet();
			
			while (rs.next()) {
				System.out.print(rs.getShort(1) + ": ");
				System.out.print(rs.getLong(2) + ", ");
				System.out.println(rs.getString(3));
			}
			
			Short temp = ids.get(new Random().nextInt(ids.size()));
			System.out.println(temp);
			conn.commit();
			try 
			{
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
			} catch (SQLException se)
            {
                if (( (se.getErrorCode() == 50000)
                        && ("XJ015".equals(se.getSQLState()) ))) {
                    // we got the expected exception
                    System.out.println("Derby shut down normally");
                    // Note that for single database shutdown, the expected
                    // SQL state is "08006", and the error code is 45000.
                } else {
                    // if the error code or SQLState is different, we have
                    // an unexpected exception (shutdown failed)
                    System.err.println("Derby did not shut down normally");
                }
            }
	}
}
