package record;

import net.dv8tion.jda.api.entities.MessageActivity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.internal.entities.AbstractMessage;
import net.dv8tion.jda.internal.entities.UserSnowflakeImpl;

public class OfflineMessage extends AbstractMessage {

	long id;
	UserSnowflake author;

	public OfflineMessage(String content, String nonce, boolean isTTS, long id, long authorId) {
		super(content, nonce, isTTS);
		this.id = id;
		this.author = UserSnowflake.fromId(authorId);
	}
	@Override
	public MessageActivity getActivity() {
		unsupported();
		return null;
	}

	@Override
	public long getIdLong() { return id; }

	@Override
	protected void unsupported() {

	}

	@Override
	public User getAuthor() { return null; } //will fix later

}
