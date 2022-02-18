package record;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import recorders.MessageProcessers;
import recorders.Recorder;
import util.Helper;

/*
 * At the beginning of the file there will be the id of the last message downloaded
 * Messages are stored on disk in a text file in the format <author id><message><separator>
 * the author id will always be a sequence of 4 characters and thus no separator is needed
 */
public class Messages {

	private long channelId;
	private File messagesFile;
	private long recentId;
	private JDA jda;
	//measured in characters, length of metadeta before each message on disk
	private static int METADATA_LENGTH = (Long.BYTES / 2) + 1;

	public static final char SEPARATOR = 0xFFFF;

	public static void main(String[] args) throws IOException {
		char foo = 0;
		System.out.println(foo);
	}
	public Messages(long channelId, JDA jda) throws IOException, InterruptedException {
		this.channelId = channelId;
		this.jda = jda;
		this.messagesFile = new File("bin" + File.separator + channelId + ".txt");
		if (messagesFile.exists())
		{
			this.recentId = getMostRecentIdLong();
			sync();
		}
		else
		{
			//fetchMessages();
		}
	}

	public boolean sync() throws IOException {
		long currentId;
		TextChannel channel = jda.getTextChannelById(channelId);
		if (channel.hasLatestMessage()) currentId = channel.getLatestMessageIdLong();
		else return false;
		if (recentId == currentId) return true;
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
	 * @throws IOException
	 */
	public void fetchMessages(long start, long end) throws IOException {
		boolean isMessageRetrieved = false;
		FileWriter writer = new FileWriter(messagesFile, StandardCharsets.UTF_16, true);
		TextChannel channel = jda.getTextChannelById(channelId);
		do
		{
			MessageHistory history = channel.getHistoryAfter(start, 100).complete();
			List<Message> messages = history.getRetrievedHistory();
			for (int i = messages.size() - 1; i >= 0; i--)
			{
				Message message = messages.get(i);
				writer.write(prepareMessage(message));
				if (message.getIdLong() == end)
				{
					isMessageRetrieved = true;
					break;
				}
			}
			start = messages.get(0).getIdLong();
			writer.flush();

		} while (!isMessageRetrieved);
		
		writer.close();
		// update most recent id downloaded
		RandomAccessFile file = new RandomAccessFile(messagesFile, "rwd");
		file.writeLong(end);
		file.close();
	}

	/**
	 * Fetches all messages from the most recent to specified end
	 *
	 * @param end
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void fetchMessages(long end) throws IOException, InterruptedException {
		long recentId = getMostRecentIdLong();
		fetchMessages(recentId, end);
	}

	/**
	 * Wipes existing file and rewrites from beginning to end of channel
	 *
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void fetchMessages() throws IOException, InterruptedException {
		TextChannel channel = jda.getTextChannelById(channelId);
		Message beginning = channel.getHistoryFromBeginning(1).complete().getRetrievedHistory().get(0);
		FileWriter writer = new FileWriter(messagesFile, StandardCharsets.UTF_16);
		writer.write(new char[] { 0, 0, 0, 0 }); // leave 64 bits for the most recent id
		writer.write(prepareMessage(beginning));
		if (channel.hasLatestMessage()) fetchMessages(beginning.getIdLong(), channel.getLatestMessageIdLong());
	}

	private char[] prepareMessage(Message message) {
		char[] messageContent = message.getContentRaw().toCharArray();
		char[] output = new char[METADATA_LENGTH + messageContent.length];
		//copy message into output array with empy header
		System.arraycopy(messageContent, 0, output, METADATA_LENGTH, messageContent.length);
		//copy author id into output
		char[] author = Helper.toChars(message.getAuthor().getIdLong());
		System.arraycopy(author, 0, output, 0, author.length);
		//copy message length into output
		output[author.length] = (char)messageContent.length;
		return output;
	}

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
		try (InputStream in = new FileInputStream(messagesFile))
		{
			in.skip(Long.BYTES);
			Scanner reader = new Scanner(in, StandardCharsets.UTF_16);
			reader.useDelimiter(Character.toString(SEPARATOR));
			while (reader.hasNext()) {
			String buffer = reader.next();
			long id = Helper.fromChars(buffer.substring(0, 5).toCharArray());
			String content = buffer.substring(5);
			processers.accept(content, id);
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
}
