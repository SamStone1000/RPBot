package stone.rpbot.scheduled;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.restaction.pagination.MessagePaginationAction;
import net.dv8tion.jda.api.utils.TimeUtil;
import stone.rpbot.util.SharedConstants;

public class CringeDLB implements Job {
	
	private long DLB_CHANNEL = 485967269512478721l;
	private long DLB_USERID = 678404315425144835l;
	private long DLB_GUILDID = 317107616209698816l;

	public CringeDLB() {
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Guild guild = SharedConstants.jda.getGuildById(DLB_GUILDID);
		TextChannel channel = guild.getTextChannelById(DLB_CHANNEL);
		LocalDate nowDate = LocalDate.now(ZoneOffset.of("-4")); //this was coded during DST
		LocalTime pastTime = LocalTime.of(15, 0);
		
		//any id created before this time will be less than this long no matter what
		//and any created after will be larger
		long cutOffTime = TimeUtil.getDiscordTimestamp(LocalDateTime.of(nowDate, pastTime).toInstant(ZoneOffset.of("-4")).toEpochMilli());
		
		//SharedConstants.GLOBAL_LOGGER.debug(Long.toString(cutOffTime));
		//SharedConstants.GLOBAL_LOGGER.debug(nowDate.toString());
		
		MessagePaginationAction history = channel.getIterableHistory();
		boolean foundLyrics = false;
		//SharedConstants.GLOBAL_LOGGER.debug("Trolling DLB");
		for (Message message : history) {
			if (message.getIdLong() < cutOffTime) break;
			if (message.getAuthor().getIdLong() == DLB_USERID) {
				//SharedConstants.GLOBAL_LOGGER.debug(message.getContentRaw());
				if (!message.getEmbeds().isEmpty() || message.getContentRaw().equals("The queue is empty! More lyrics need to be added.")) {
					foundLyrics = true;
					//SharedConstants.GLOBAL_LOGGER.debug("found lyrics");
					break;
				}
			}
			}
		if (foundLyrics) return;
		channel.sendMessage(".force").queue();
	}

}
