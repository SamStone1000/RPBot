package stone.rpbot.scheduled;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import stone.rpbot.record.LyricStore;
import stone.rpbot.util.SharedConstants;

public class DailyLyrics extends ListenerAdapter implements Job {

	private long channelID;
	private long guildID;
	
	public DailyLyrics() {
		// TODO Auto-generated constructor stub
	}
	
	public void setChannel(long channel) {
		this.channelID = channel;
	}
	
	public void setGuild(long guild) {
		this.guildID = guild;
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Logger logger = LoggerFactory.getLogger(getClass());
		try (Statement statement = SharedConstants.DATABASE_CONNECTION.createStatement())
		{
			statement.execute("SELECT internalID FROM "+LyricStore.TABLE);
			ResultSet resultSet = statement.getResultSet();
			ArrayList<Short> ids = new ArrayList<Short>();
			while (resultSet.next()) {
				ids.add(resultSet.getShort(1)); //adds every id to an arraylist
			}
			
			Guild guild = SharedConstants.jda.getGuildById(guildID);
			TextChannel channel = guild.getTextChannelById(channelID);
			
			if (ids.isEmpty())
			{// no lyrics found, send APDO and cancel rest of execution
				MessageCreateData msg = new MessageCreateBuilder()
						.setContent("No lyrics in queue, have the Astronomy Picture of the Day instead")
						.setEmbeds(getAstronomyEmbed()).build();
				channel.sendMessage(msg).queue();
			}
			Short temp = ids.get(new Random().nextInt(ids.size())); //select a random internalID
			statement.execute("SELECT authorID, lyric, name, artist FROM "+LyricStore.TABLE+" WHERE internalID = "+temp.toString()); //getting the lyric that corresponds to the random internalID
			resultSet.close();
			resultSet = statement.getResultSet();
			
			resultSet.next(); //should only be one
			
			long authorID = resultSet.getLong(1);
			EmbedBuilder builder = new EmbedBuilder();
			try
			{ //uses authors name and avatar if they're in the server
			Member author = guild.retrieveMemberById(authorID).complete();
			builder.setAuthor(author.getEffectiveName(), null, author.getEffectiveAvatarUrl());
			} catch (ErrorResponseException e) {
				builder.setAuthor(Long.toString(authorID));
			}
			//logger.debug(Long.toString(resultsLyric.getLong(1)));
			
			
			
			builder.setDescription(resultSet.getString(2));
			builder.addField(resultSet.getString(3), resultSet.getString(4), false);
			builder.addField(EmbedBuilder.ZERO_WIDTH_SPACE, "||InternalID: "+temp+", AuthorID: "+authorID+"||", false);
			builder.setFooter((ids.size() - 1) + " Lyrics remain");
			logger.info("Sending Daily Lyric");
			channel.sendMessageEmbeds(builder.build()).queue();
			
			resultSet.close();
			
			statement.execute("DELETE FROM "+LyricStore.TABLE+" WHERE internalID = "+temp.toString());
		} catch (SQLException e)
		{
			logger.warn(e.toString());
		} finally {
			try
			{
				SharedConstants.DATABASE_CONNECTION.commit();
			} catch (SQLException e)
			{
				// TODO Auto-generated catch block
				logger.warn(e.toString());
			}
		}
	}

	private MessageEmbed getAstronomyEmbed() {
		// try to add the necessary components to embed in try block, if it fails finish
		// embed in catch block instead
		EmbedBuilder builder = new EmbedBuilder();
		try
		{
			HttpRequest request = HttpRequest.newBuilder()
					.uri(new URI("https://api.nasa.gov/planetary/apod?api_key=DEMO_KEY")).GET().build();
			HttpClient client = HttpClient.newHttpClient();
			HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
			JsonNode map = new JsonMapper().readTree(response.body());

			JsonNode photo = map.findValue("hdurl");
			if (photo == null)
				photo = map.findValue("url");
			JsonNode explanation = map.findValue("explanation");
			JsonNode title = map.findValue("title");
			JsonNode copyright = map.findValue("copyright");
			if (map.findValue("media_type").asText().equals("image"))
			{
				builder.setImage(photo.asText());
			}
			else
			{
				builder.addField("Video Link", photo.asText(), false);
			}
			if (photo == null | explanation == null | title == null)
				throw new NullPointerException("hdurl, explanation, or title field in JSON return from APOD was null");

			builder.setTitle(title.asText());
			builder.setDescription(explanation.asText());
			if (copyright != null)
				builder.setFooter("\u00A9 " + copyright);
		} catch (IOException | InterruptedException | URISyntaxException e)
		{
			Logger logger = LoggerFactory.getLogger(this.getClass());
			logger.debug(e.getMessage());
			for (StackTraceElement stack : e.getStackTrace())
			{
				logger.debug(stack.toString());
			}

		}
		return builder.build();

	}

}
