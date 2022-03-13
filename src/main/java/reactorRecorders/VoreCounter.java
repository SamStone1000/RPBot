package reactorRecorders;

import java.io.File;
import java.math.BigInteger;
import java.util.Map;
import java.util.regex.Pattern;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import record.KickedUserHelper;
import util.MutableInteger;

public class VoreCounter extends E621Counter {

	public VoreCounter(String term, Pattern pattern, boolean shouldEffect, long channelId, String searchTerms,
			JDA jda, Pattern triggerTerm) {
		super(term, pattern, shouldEffect, channelId, searchTerms, jda, triggerTerm);
		// TODO Auto-generated constructor stub
	}

	public VoreCounter(Pattern pattern, Map<Long, MutableInteger> counts, File file, boolean shouldAffect,
			long channelId, String searchTerms, JDA jda, boolean shouldResetCounts) {
		super(pattern, counts, file, shouldAffect, channelId, searchTerms, jda, shouldResetCounts);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void accept(Message message) {
		if (super.test(message.getContentRaw(), message.getAuthor().getIdLong())) {
			if (shouldAffect) {
				Member author = message.getMember();
				BigInteger duration = KickedUserHelper.kickForVore(author, super.getCount(author.getIdLong()));
				if (duration.compareTo(BigInteger.ZERO) != 0) {
				String output = author.getEffectiveName() + " has been banned for ";
				output += duration.toString();
				output += " minutes";
				message.reply(output).queue();
				}
			}
		}
		if (shouldAffect) {
			if (triggerTerm.matcher(message.getContentRaw()).matches())
				sendImage();
		}
	}

}
