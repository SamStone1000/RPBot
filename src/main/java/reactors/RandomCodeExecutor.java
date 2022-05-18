package reactors;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import reactorRecorders.KarmaCounter;
import util.DiscordWriter;
import util.SharedConstants;

public class RandomCodeExecutor implements Consumer<Message> {

	private static Pattern lambdaPattern = Pattern.compile("\\( *(?:<@!?)(\\d*?)(?:>) *\\) *-> *\\{(.+)\\}");
	private KarmaCounter karmaCounter;
	
	public RandomCodeExecutor(KarmaCounter counter) { 
		this.karmaCounter = counter;
	}

	@Override
	public void accept(Message message) {
		if (message.getAuthor().getIdLong() != 275383746306244608l) return;
		Matcher lambdaMatcher = lambdaPattern.matcher(message.getContentRaw());
		if (lambdaMatcher.find())
		{
			long id = Long.valueOf(lambdaMatcher.group(1));
			int karmaCount = karmaCounter.getKarma(id);
			String code = "import java.util.function.IntUnaryOperator;"
					+ "public class Temp implements IntUnaryOperator{"
					+ "public int applyAsInt(int i){"
					+ lambdaMatcher.group(2)
					+ "}}";
			
			JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
			ArrayList<? extends JavaFileObject> units = new ArrayList<>();
			File file = new File("Temp.java");
			try
			{
			file.createNewFile();
			FileWriter writer = new FileWriter(file);
			writer.write(code);
			writer.close();
			Iterable<? extends JavaFileObject> files = javac.getStandardFileManager(null, null, null).getJavaFileObjects(file);
			javac.getTask(new DiscordWriter(SharedConstants.jda, message.getGuild().getIdLong(), message.getChannel().getIdLong()), null, null, null, null, files).call();
			URLClassLoader loader = new URLClassLoader(new URL[] {new File("./").toURI().toURL()});
			Class<?> clazz = loader.loadClass("Temp");
			IntUnaryOperator foo = (IntUnaryOperator) clazz.newInstance();
			Member user = message.getGuild().retrieveMemberById(id).complete();
			String output = user.getEffectiveName() + " now has " + foo.applyAsInt(karmaCount) + " karma, ";
			message.reply(output).queue();
			} catch (Exception e)
			{
				message.reply(e.toString()).queue();
			}
		}
	 }
	
	

}
