package record;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import recorders.MessageProcessers;

/*This is a class designed to store every message
 * sent in a discord server to disk. This is to increase
 * response times since discord has such a large delay to
 * sending/receiving messages*/
public class Channels {

	private final JDA jda;
	private final long guildId;
	public List<Messages> channels = new ArrayList<>();

	public Channels(JDA jda, long guildId) throws IOException, InterruptedException {
		this.jda = jda;
		this.guildId = guildId;
		Guild guild = jda.getGuildById(guildId);
		List<TextChannel> tempChannels = guild.getTextChannels();
		for (TextChannel channel : tempChannels) {
			channels.add(new Messages(channel.getIdLong(), jda));
		}
	}
	
	public void searchChannels(MessageProcessers processers) {
		for (Messages messages : channels) {
			messages.searchMessages(processers);
		}
	}

	public void fetchAll(long channel) {
		Logger logger = LoggerFactory.getLogger("Fetch");
		for (Messages messages : channels) {
			try
			{
				long start = System.currentTimeMillis();
				messages.fetchMessages();
				long end = System.currentTimeMillis();
				String debug = "Fetched <#"+messages.getId()+"> in "+(end - start) + " ms";
				logger.info(debug);
				jda.getTextChannelById(channel).sendMessage(debug).queue();
				
			} catch (IOException | InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
