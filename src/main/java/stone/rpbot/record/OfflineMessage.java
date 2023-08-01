package stone.rpbot.record;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageActivity;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.AbstractMessage;
import net.dv8tion.jda.internal.entities.UserImpl;
import stone.rpbot.util.SharedConstants;

public class OfflineMessage extends AbstractMessage {

	private long author;
	private long flags;
	private boolean fromWebhook;
	private long id;
	private MessageReference messageReference;
	private boolean pinned;
	private int type;

	private OfflineMessage(long author, String content, long flags, boolean fromWebhook, long id, boolean isTTS,
			MessageReference messageReference, boolean pinned, int type) {
		super(content, null, isTTS);
		this.author = author;
		this.flags = flags;
		this.fromWebhook = fromWebhook;
		this.id = id;
		this.messageReference = messageReference;
		this.pinned = pinned;
		this.type = type;
	}

	@Override
	public long getApplicationIdLong() {
		unsupported();
		return 0;
	}

	@Override
	public MessageActivity getActivity() {
		unsupported();
		return null;
	}

	@Override
	public MessageReference getMessageReference() {
		return messageReference;
	}

	@Override
	public User getAuthor() {
		return new UserImpl(author, (JDAImpl) SharedConstants.jda);
	}

	@Override
	public long getIdLong() {
		return id;
	}

	@Override
	protected void unsupported() {
		throw new UnsupportedOperationException();
	};

	public static class Builder {

		private long author;
		private String content;
		private long flags;
		private boolean fromWebhook;
		private long id;
		private boolean isTTS;
		private MessageReference messageReference;
		private boolean pinned;
		private int type;

		public Builder() {

		}

		public Message build() {
			return new OfflineMessage(author, content, flags, fromWebhook, id, isTTS, messageReference, pinned, type);
		}

		public void setAuthorId(long author) {
			this.author = author;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public void setFlags(long flags) {
			this.flags = flags;
		}

		public void setFromWebhook(boolean fromWebhook) {
			this.fromWebhook = fromWebhook;
		}

		public void setId(long id) {
			this.id = id;
		}

		public void setIsTTS(boolean isTTS) {
			this.isTTS = isTTS;
		}

		public void setMessageReference(MessageReference messageReference) {
			this.messageReference = messageReference;
		}

		public void setPinned(boolean pinned) {
			this.pinned = pinned;
		}

		public void setType(int type) {
			this.type = type;
		}
	}

}
