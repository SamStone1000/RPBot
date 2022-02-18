import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class testing {

	public static void main(String[] args) throws IOException {
		FileWriter writer = new FileWriter(new File("bin\\counter\\vore.txt"));
		writer.close();
	}

}
