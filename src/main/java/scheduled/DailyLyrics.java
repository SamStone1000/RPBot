package scheduled;

import java.sql.Connection;
import java.sql.DriverManager;
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

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import record.LyricStore;
import util.SharedConstants;

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
			
			if (ids.isEmpty()) {//no lyrics found, send message and cancel rest of execution
				channel.sendMessage("No lyrics found :(").queue();
				return;
			}
			Short temp = ids.get(new Random().nextInt(ids.size())); //select a random internalID
			statement.execute("SELECT authorID, lyric, name, artist FROM "+LyricStore.TABLE+" WHERE internalID = "+temp.toString()); //getting the lyric that corresponds to the random internalID
			resultSet.close();
			resultSet = statement.getResultSet();
			
			resultSet.next(); //should only be one
			
			Member author = guild.retrieveMemberById(resultSet.getLong(1)).complete();
			//logger.debug(Long.toString(resultsLyric.getLong(1)));
			EmbedBuilder builder = new EmbedBuilder();
			
			builder.setAuthor(author.getEffectiveName(), null, author.getEffectiveAvatarUrl());
			builder.setDescription(resultSet.getString(2));
			builder.addField(resultSet.getString(3), resultSet.getString(4), false);
			logger.info("Sending Daily Lyric");
			channel.sendMessageEmbeds(builder.build()).queue();
			
			resultSet.close();
		} catch (SQLException e)
		{
			logger.warn(e.toString());
		}/* finally {
		try //shutdown database safely to prevent leaks
		{
		DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException se)
        {
            if (( (se.getErrorCode() == 50000)
                    && ("XJ015".equals(se.getSQLState()) ))) {
                // we got the expected exception
                logger.debug("Derby shut down normally");
                // Note that for single database shutdown, the expected
                // SQL state is "08006", and the error code is 45000.
            } else {
                // if the error code or SQLState is different, we have
                // an unexpected exception (shutdown failed)
                logger.debug("Derby did not shut down normally");
            }
        }
		} */
		
	}

}
