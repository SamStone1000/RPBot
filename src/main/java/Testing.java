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
		String dataBaseName = "RPStore";
		long start = System.currentTimeMillis();
		System.setProperty("derby.language.sequence.preallocator", "1");
		Connection conn = DriverManager.getConnection("jdbc:derby:"+dataBaseName +";create=true");
		conn.setAutoCommit(false);
		long end = System.currentTimeMillis();
		System.out.println(end - start);
		Statement statement = conn.createStatement();
		//statement.execute("DROP TABLE lyrics");
		//statement.execute("create table lyricstore(internalID SMALLINT GENERATED ALWAYS AS IDENTITY (INCREMENT BY 1, CYCLE), authorid bigint, lyric varchar(4000), name varchar(256), artist varchar(256))");
//		PreparedStatement prepared = conn.prepareStatement("insert into lyrics values(DEFAULT, ?, ?)");
//		
//		prepared.setLong(1, (long) (Math.random() * Long.MAX_VALUE));
//		prepared.setString(2, "foo");
//		prepared.execute();
//			
//			statement.execute("select internalID from lyrics");
//			ResultSet rs = statement.getResultSet();
//			
//			
//			ArrayList<Short> ids = new ArrayList<Short>();
//			while (rs.next()) {
//				ids.add(rs.getShort(1));
//			}
//			
//			statement.execute("select * from lyricstore");
//			ResultSet rs = statement.getResultSet();
//			
//			int colCount = rs.getMetaData().getColumnCount();
//			while (rs.next()){
//			    for(int i = 1; i < colCount; i++){
//			       System.out.print(rs.getString(i) + ", ");
//			    }
//			    System.out.println(rs.getString(colCount));
//			}
//			
//			Short temp = ids.get(new Random().nextInt(ids.size()));
//			System.out.println(temp);
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
