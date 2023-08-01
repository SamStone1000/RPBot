package stone.rpbot.record;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import stone.rpbot.RPBot;
import stone.rpbot.util.SharedConstants;

//This is bad but I have to overwrite MessageReference like this so the types work out
public class LazyReference extends MessageReference {

	private final long messageId;
	private final long channelId;
	private final long guildId;

	public LazyReference(long messageId, long channelId, long guildId) {
		super(guildId, guildId, guildId, null, NullJDA.EMPTY);
		this.messageId = messageId;
		this.channelId = channelId;
		this.guildId = guildId;
	}

	@Override
	public Message getMessage() {
		return RPBot.channels.channels.get(channelId).getMessage(messageId);
	}

	@Override
	public MessageChannelUnion getChannel() {
		return SharedConstants.jda.getChannelById(MessageChannelUnion.class, channelId);
	}

	@Override
	public Guild getGuild() {
		return SharedConstants.jda.getGuildById(guildId);
	}

	@Override
	public long getMessageIdLong() {
		return messageId;
	}

	@Override
	public long getChannelIdLong() {
		return channelId;
	}

	@Override
	public long getGuildIdLong() {
		return guildId;
	}

	@Override
	public String getMessageId() {
		return Long.toUnsignedString(messageId);
	}

	@Override
	public String getChannelId() {
		return Long.toUnsignedString(channelId);
	}

	@Override
	public String getGuildId() {
		return Long.toUnsignedString(guildId);
	}

	@Override
	public JDA getJDA() {
		throw new UnsupportedOperationException(); // bad
	}

}
