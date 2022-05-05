import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import util.SharedConstants;


public class Testing {

	public static void main (String[] args) throws SQLException {
		SharedConstants.init(null);
		ResultSet rs = SharedConstants.DATABASE_CONNECTION.createStatement().executeQuery("SELECT author FROM CHANNEL442853757160521728");
//		ResultSetMetaData rsmd = rs.getMetaData();
//		int count = rsmd.getColumnCount();
//		for (int i = 1; i < count + 1; i++)
//		{
//			System.out.print(rsmd.getColumnLabel(i)+", ");
//		}
//		System.out.print("\n");
//		while (rs.next()) 
//		{
//			for (int i = 1; i < count + 1; i++)
//			{
//				System.out.print(rs.getString(i)+", ");
//			}
//			System.out.print("\n");
//		}
		
		HashSet<Long> authors = new HashSet<Long>();
		while (rs.next())
		{
			authors.add(rs.getLong(1));
		}
		for (Long author : authors)
		{
			System.out.println(author);
			//<@456226577798135808>
			//<@371692613776179200>
			//<@295943341143490563>
		}
	}
}
