package stone.rpbot.reactorRecorders;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import stone.rpbot.recorders.Counter;
import stone.rpbot.util.MutableInteger;

public class ImageCounter extends Counter implements ReactorRecord {

	private long channelId;
	private String searchTerms;
	private JDA jda;
	protected Pattern triggerTerm;
	private static long last = 0;

	public ImageCounter(String term, Pattern pattern, boolean shouldEffect, long channelId, String searchTerms, JDA jda,
			Pattern triggerTerm) {
		super(term, pattern, shouldEffect);
		this.channelId = channelId;
		this.searchTerms = searchTerms;
		this.jda = jda;
		this.triggerTerm = triggerTerm;
	}

	public ImageCounter(Pattern pattern, Map<Long, MutableInteger> counts, File file, boolean shouldAffect,
			long channelId, String searchTerms, JDA jda, boolean shouldResetCounts) {
		super(pattern, counts, file, shouldAffect, shouldResetCounts);
		this.channelId = channelId;
		this.searchTerms = searchTerms;
		this.jda = jda;
	}

	@Override
	public void accept(Message message) {
		super.test(message.getContentRaw(), message.getAuthor().getIdLong());
		if (shouldAffect)
			if (triggerTerm.matcher(message.getContentRaw()).matches())
				sendImage();
	}

	@Override
	public ReactorRecord copyOf(boolean shouldResetCounts, boolean shouldAffect) {
		return new ImageCounter(pattern, counts, file, shouldAffect, channelId, searchTerms, jda, shouldResetCounts);
	}

	protected void sendImage() {
		long now = System.currentTimeMillis();
		if (last + 2000 > now) { // an image every 2 seconds
			last = now;
			try {
				LoggerFactory.getLogger("E621").debug("Sending Image");
				HttpURLConnection connection = (HttpURLConnection) new URL(
						"https://e621.net/posts.json?tags=" + searchTerms + "+order:random+limit:1").openConnection();
				connection.setRequestMethod("GET");
				connection.setRequestProperty("User-Agent", "RPBot/2.0 by SamStone");
				connection.connect();
				InputStream stream = connection.getInputStream();
				ObjectMapper mapper = new ObjectMapper();
				Map<String, Object> jsonMap = mapper.readValue(stream, LinkedHashMap.class);
				ArrayList posts = (ArrayList) jsonMap.get("posts");
				LinkedHashMap post = (LinkedHashMap) posts.get(0);
				LinkedHashMap file = (LinkedHashMap) post.get("file");
				String url = (String) file.get("url");
				String md5 = (String) file.get("md5");
				HttpURLConnection connection2 = (HttpURLConnection) new URL(url).openConnection();
				connection2.setRequestMethod("GET");
				connection2.setRequestProperty("User-Agent", "RPBot/2.0 by SamStone");
				connection2.connect();
				TextChannel channel = jda.getTextChannelById(channelId);
				channel.sendFiles(FileUpload.fromData(connection2.getInputStream(),
						"SPOILER_" + url.substring(url.length() - md5.length() - 3))).queue();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}