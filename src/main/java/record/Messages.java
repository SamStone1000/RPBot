package record;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.DuplicateFormatFlagsException;
import java.util.List;
import java.util.Scanner;

import org.quartz.utils.counter.sampled.TimeStampedCounterValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.Message.Interaction;
import net.dv8tion.jda.api.entities.MessageActivity;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.entities.MessageSticker;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import recorders.MessageProcessers;
import recorders.Recorder;
import util.Helper;
import util.SharedConstants;

/*
 * At the beginning of the file there will be the id of the last message downloaded
 * Messages are stored on disk in a text file in the format <author id><message length (in bytes)><message>
 * the author id will always be a sequence of 4 characters and thus no separator is needed
 */
public class Messages {

	private long channelId;
	private File messagesFile;
	private long recentId;
	private JDA jda;

	public static final char SEPARATOR = 0xFFFF;
	private final PreparedStatement statement;

	public static void main(String[] args) throws IOException {
		
	}

	public Messages(long channelId, JDA jda) throws IOException, InterruptedException, SQLException {
		this.channelId = channelId;
		this.jda = jda;
		statement = SharedConstants.DATABASE_CONNECTION.prepareStatement("INSERT INTO messages"+channelId+"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
	}

	public boolean sync() throws IOException {
		long currentId;
		TextChannel channel = jda.getTextChannelById(channelId);
		currentId = channel.getLatestMessageIdLong();
		if (recentId == currentId)
			return true;
		else
		{
			fetchMessages(recentId, currentId);
			return true;
		}
	}

	/**
	 * Fetches and writes to disk every message in the given range
	 * 
	 * @param start Starting message id of range, exclusive
	 * @param end   Ending message id of range, inclusive
	 */
	public void fetchMessages(long start, long end) {
		boolean isMessageRetrieved = false;

		try (FileOutputStream fos = new FileOutputStream(messagesFile, true);
				BufferedOutputStream buffer = new BufferedOutputStream(fos))
		{
			TextChannel channel = jda.getTextChannelById(channelId);
			do
			{
				MessageHistory history = channel.getHistoryAfter(start, 100).complete();
				List<Message> messages = history.getRetrievedHistory();
				for (int i = messages.size() - 1; i >= 0; i--)
				{
					Message message = messages.get(i);
					if (!message.getAuthor().isBot())
					{
						byte[] preparedMessage = prepareMessage(message);
						buffer.write(preparedMessage);
					}
					if (message.getIdLong() == end)
					{
						isMessageRetrieved = true;
						break;
					}
				}
				try
				{
					start = messages.get(0).getIdLong();
				} catch (IndexOutOfBoundsException e)
				{
					// if it cant get the zeroth message something must be wrong, exit out early
					Logger logger = LoggerFactory.getLogger("FetchFail");
					isMessageRetrieved = true;
					logger.info(channelId + " failed to get zeroth message");
				}

			} while (!isMessageRetrieved);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		recentId = end;
		// update most recent id downloaded
		try (RandomAccessFile file = new RandomAccessFile(messagesFile, "rwd"))
		{
			file.writeLong(end);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Fetches all messages from the most recent to specified end
	 *
	 * @param end
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void fetchMessages(long end) { fetchMessages(recentId, end); }

	/**
	 * Wipes existing file and rewrites from beginning to end of channel
	 *
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void fetchMessages() {
		TextChannel channel = jda.getTextChannelById(channelId);
		Message beginning = channel.getHistoryFromBeginning(1).complete().getRetrievedHistory().get(0);
		try (FileOutputStream fos = new FileOutputStream(messagesFile, false))
		{
			fos.write(new byte[Long.BYTES]);
			fos.write(prepareMessage(beginning));
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			fetchMessages(beginning.getIdLong(), channel.getLatestMessageIdLong());
	}

	private byte[] prepareMessage(Message message) { return messageToBytes(message); }

	private long getMostRecentIdLong() throws IOException {
		RandomAccessFile randomMessages = new RandomAccessFile(messagesFile, "r");
		long recentId = randomMessages.readLong();
		randomMessages.close();
		return recentId;
	}

	/**
	 * Searches from beginning to end of this Messages history
	 * 
	 * This does not check for new messages before searching
	 * 
	 * @param record
	 */
	public void searchMessages(MessageProcessers processers) {
		try (InputStream in = new FileInputStream(messagesFile);
				BufferedInputStream buffer = new BufferedInputStream(in))
		{
			buffer.skip(Long.BYTES); // skip most recent id
			byte[] lengthBytes = new byte[Integer.BYTES];
			while (buffer.read(lengthBytes) > 0)
			{// stop searching if the end of file has been reached
				int length = Helper.bytesToInt(lengthBytes);
				byte[] messageBytes = buffer.readNBytes((int) length);
				OfflineMessage message = bytesToMessage(messageBytes);
				processers.accept(message);
			}
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public long getId() {
		// TODO Auto-generated method stub
		return channelId;
	}

	public static byte[] messageToBytes(Message message) {
		byte[] content = message.getContentRaw().getBytes(StandardCharsets.UTF_16BE);
		byte[] id = Helper.toBytes(message.getIdLong());
		byte[] authorId = Helper.toBytes(message.getAuthor().getIdLong());
		// List<MessageReaction> reactions = message.getReactions();

		int lengthInteger = content.length + id.length + authorId.length;
		byte[] length = Helper.toBytes(lengthInteger);
		byte[] messageBytes = new byte[lengthInteger + length.length]; // leave enough room for the length integer
		int offset = 0; // help keep track of byte offset
		System.arraycopy(length, 0, messageBytes, offset, length.length); // copy length to start
		offset += length.length;
		System.arraycopy(id, 0, messageBytes, offset, id.length); // copy message id in
		offset += id.length;
		System.arraycopy(authorId, 0, messageBytes, offset, authorId.length); // copy author id in
		offset += authorId.length;
		System.arraycopy(content, 0, messageBytes, offset, content.length); // copy message content in
		offset += content.length;
		return messageBytes;
	}

	public static OfflineMessage bytesToMessage(byte[] bytes) {
		byte[] id = new byte[Long.BYTES];
		byte[] authorId = new byte[Long.BYTES];
		int offset = 0;// skip the length integer
		System.arraycopy(bytes, offset, id, 0, id.length); // copy id out of bytes
		offset += id.length;
		System.arraycopy(bytes, offset, authorId, 0, authorId.length); // copy authorId out of bytes
		offset += authorId.length;
		byte[] content = new byte[bytes.length - offset];
		System.arraycopy(bytes, offset, content, 0, content.length); // copy content out of bytes
		offset += content.length;
		return new OfflineMessage(
				new String(content, StandardCharsets.UTF_16BE), null, false, Helper.bytesToLong(id),
				Helper.bytesToLong(authorId)
		);
	}
	
	public void extractMessageFields(Message message) {
		//The mention lists are evaluated from the content so no need to store them
		//commented out code means its something I would like to store but am too lazy to figure out how/don't need to right now
		//MessageActivity messageActivity = message.getActivity();
		//List<Attachment> attachments = message.getAttachments();
		long author = message.getAuthor().getIdLong();
		//List<ActionRow> components = message.getActionRows();
		String content = message.getContentRaw();
		//Timestamp editedTime = new Timestamp(message.getTimeEdited().toInstant().toEpochMilli());
		//List<MessageEmbed> embeds = message.getEmbeds();
		long flags = message.getFlagsRaw();
		boolean fromWebhook = message.isWebhookMessage();
		long id = message.getIdLong();
		//Interaction interaction = message.getInteraction();
		boolean isTTS = message.isTTS();
		//have to create the mentioned users and roles list from the content when making a message from this data
		MessageReference messageReference = message.getMessageReference();
		String nonce = message.getNonce();
		boolean pinned = message.isPinned();
		//List<MessageReaction> reactions = message.getReactions();
		//List<MessageSticker> stickers = message.getStickers();
		MessageType type = message.getType();
		
		try
		{
			statement.setLong(1, author);
			statement.setString(2, content);
			statement.setLong(3, flags);
			statement.setBoolean(4, fromWebhook);
			statement.setLong(5, id);
			statement.setBoolean(6, isTTS);
			//putting the message reference in
			statement.setLong(7, messageReference.getMessageIdLong());
			statement.setLong(8, messageReference.getChannelIdLong());
			statement.setLong(9, messageReference.getGuildIdLong());
			
			statement.setString(10, nonce);
			statement.setBoolean(11, pinned);
			statement.setInt(12, type.getId());
			statement.execute();
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
