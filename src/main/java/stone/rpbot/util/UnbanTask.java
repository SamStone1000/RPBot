package stone.rpbot.util;

import java.util.TimerTask;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class UnbanTask extends TimerTask {

	private String memberId;
	private long guildId;
	private JDA jda;

	public UnbanTask(Member member) {
		this.memberId = member.getId();
		this.guildId = member.getGuild().getIdLong();
		this.jda = member.getJDA();
	}

	@Override
	public void run() { jda.getGuildById(guildId).unban(User.fromId(memberId)).queue(); }
}