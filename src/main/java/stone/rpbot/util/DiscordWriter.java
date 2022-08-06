package stone.rpbot.util;

import java.io.IOException;
import java.io.Writer;
import java.nio.CharBuffer;

import net.dv8tion.jda.api.JDA;

public class DiscordWriter extends Writer {

	private JDA jda;
	private long channelID;
	private long guildID;

	public DiscordWriter(JDA jda, long guildID, long channelID) {
		this.jda = jda;
		this.guildID = guildID;
		this.channelID = channelID;
		}

	public DiscordWriter(Object lock) { super(lock);
	// TODO Auto-generated constructor stub
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		char[] output = new char[len];
		System.arraycopy(cbuf, off, output, 0, len);
		try
		{
		jda.getGuildById(guildID).getTextChannelById(channelID).sendMessage(CharBuffer.wrap(output)).queue();
		} catch (IllegalStateException e)
		{
			
		}
	 }

	@Override
	public void flush() throws IOException { // TODO Auto-generated method stub
	 }

	@Override
	public void close() throws IOException { // TODO Auto-generated method stub
	 }

}
