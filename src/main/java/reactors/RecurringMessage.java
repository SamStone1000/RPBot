package reactors;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import net.dv8tion.jda.api.JDA;
import util.SharedConstants;

public class RecurringMessage implements Job {

	private long channel;
	public void setChannel(long channnelID) {
		this.channel = channnelID;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	private String message;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		SharedConstants.jda.getTextChannelById(channel).sendMessage(message).queue();
		//SharedConstants.GLOBAL_LOGGER.debug(message);
		//SharedConstants.GLOBAL_LOGGER.debug(Long.toString(channel));
	}

}
