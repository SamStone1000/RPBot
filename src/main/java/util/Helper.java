package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Helper {

	public static long fromChars(char[] chars) {
		long output = 0;
		for (int i = 0; i < chars.length; i++)
		{ output += (long) chars[chars.length - i - 1] << (i * 16); }
		// output = ((long)chars[0] << 48) | ((long)chars[1] << 32) | ((long)chars[2] <<
		// 16) | (long)chars[3];
		return output;
	}

	public static char[] toChars(long input) {
		char one = (char) ((input >> 48) & 0xFFFFL);
		char two = (char) ((input >> 32) & 0xFFFFL);
		char three = (char) ((input >> 16) & 0xFFFFL);
		char four = (char) ((input >> 0) & 0xFFFFL);
		return new char[] { one, two, three, four };
	}

	public static char[] toChars(int input) {
		char one = (char) ((input >> 16) & 0xFFFFL);
		char two = (char) ((input >> 0) & 0xFFFFL);
		return new char[] { one, two };
	}

	public static <K extends Number, V extends Number> void writeMap(Map<K, V> map, File file) {
		String str = "";
		Iterator<Map.Entry<K, V>> itr = map.entrySet().iterator();
		while (itr.hasNext())
		{
			Entry<K, V> entry = itr.next();
			str += new String(Helper.toChars(entry.getKey().longValue()));
			str += new String(Helper.toChars(entry.getValue().intValue()));
		}

		try (FileWriter fileWriter = new FileWriter(file, StandardCharsets.UTF_16BE))
		{
			fileWriter.write(str);
			fileWriter.flush();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static Map<Long, MutableInteger> readMap(File file) {

		Map<Long, MutableInteger> map = new TreeMap<>();
		if (file.exists())
		{

			try (InputStream in = new FileInputStream(file);
					Reader reader = new InputStreamReader(in, StandardCharsets.UTF_16BE);
					BufferedReader buffer = new BufferedReader(reader);)
			{

				char[] id = new char[Long.BYTES / 2];
				char[] count = new char[Integer.BYTES / 2];
				while (buffer.read(id) != -1)
				{
					// just assume that the file is complete
					buffer.read(count);
					map.put(Helper.fromChars(id), new MutableInteger((int) Helper.fromChars(count)));
				}
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return map;
	}

	public static byte[] toBytes(long l) {
		byte[] result = new byte[Long.BYTES];
		for (int i = Long.BYTES - 1; i >= 0; i--)
		{
			result[i] = (byte) (l & 0xFF);
			l >>= Byte.SIZE;
		}
		return result;
	}

	public static long bytesToLong(final byte[] b) {
		long result = 0;
		for (int i = 0; i < Long.BYTES; i++)
		{
			result <<= Byte.SIZE;
			result |= (b[i] & 0xFF);
		}
		return result;
	}

	public static byte[] toBytes(int l) {
		byte[] result = new byte[Integer.BYTES];
		for (int i = Integer.BYTES - 1; i >= 0; i--)
		{
			result[i] = (byte) (l & 0xFF);
			l >>= Byte.SIZE;
		}
		return result;
	}

	public static int bytesToInt(final byte[] b) {
		int result = 0;
		for (int i = 0; i < Integer.BYTES; i++)
		{
			result <<= Byte.SIZE;
			result |= (b[i] & 0xFF);
		}
		return result;
	}
}
