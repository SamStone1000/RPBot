package stone.rpbot.slash.song;

import java.util.stream.Stream;

public abstract class FileUtil {
	/**
	 * Checks if given path String is valid for use
	 *
	 * This method checks for any attempts to use the "directories" `.` and `..` to
	 * escape out of the music directory.
	 * 
	 * @return true if string is valid, false if it is invalid
	 */
	public static boolean checkFileString(String path) {
		return !Stream.of(path.split("/")).anyMatch(element -> element.equals(".") || element.equals(".."));
	}
}
