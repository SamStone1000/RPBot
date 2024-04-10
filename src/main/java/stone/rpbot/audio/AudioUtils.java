package stone.rpbot.audio;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.sampled.AudioInputStream;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public abstract class AudioUtils {
	
    /**
     * The number of bytes per buffer for 20 ms
     */
    public static final int PACKET_ARRAY_LENGTH;
    
    static {
        int byteSize = Byte.SIZE;
        float sampleRate = AudioSendHandler.INPUT_FORMAT.getSampleRate() / 1000f;
        int sampleSize = AudioSendHandler.INPUT_FORMAT.getSampleSizeInBits();
        int bufferTime = 40; // packet length in ms don't know its doubled
        PACKET_ARRAY_LENGTH = (int) (bufferTime * (sampleRate * sampleSize) / byteSize);
    }
    
    public static final byte[] EMPTY_PACKET = new byte[PACKET_ARRAY_LENGTH];
    
    /**
     * Fills the supplied queue with <length> ByteBuffers
     *
     * Automatically closes the stream once the end is reached, and returns false
     *
     * @param packets The queue to fill with ByteBuffers
     * @param audioStream The stream to pull audio data from
     * @param length The number of packets to pull from the audioStream
     * @return false if the audioStream ran out of audio data, true otherwise
     */
    public static boolean fillAudioQueue(Queue<byte[]> output, AudioSupplier audioSupplier, int length) {
            for (int i = 0; i < length; i++) {
                byte[] packet = audioSupplier.getPacket();
                // this weird order is to ensure that the queue doesn't have a unprocessed packet in it (which could be seen on another thread potentially)
                output.add(packet);
                if (audioSupplier.isClosed()) {
                    return false;
                }
            }
        return true;
    }
    
    public static boolean fillAudioQueue(Queue<byte[]> output, AudioInputStream audioStream, AtomicInteger readAhead, int length) {
    	for (int i = 0; i < length; i++) {
    		byte[] packet = new byte[PACKET_ARRAY_LENGTH];
			int readLength;
			try {
				readLength = audioStream.read(packet);
				output.add(packet);
				readAhead.incrementAndGet();
				if (readLength < AudioUtils.PACKET_ARRAY_LENGTH) {
					try {
						audioStream.close();
					} catch (IOException e) {
					}
					return false;
				}
			} catch (IOException e) {
				try {
					audioStream.close();
				} catch (IOException d) {
				}
				return false;
			}

    	}
    	return true;
    }
    
    public static MainAudioSendHandler getOrJoin(SlashCommandInteractionEvent event) {
    	Member member = event.getMember();
        AudioChannel voiceChannel = member.getVoiceState().getChannel();
        if (voiceChannel == null) {
            event.reply("You need to be in a voice channel for the bot to play any audio!").setEphemeral(true).queue();
            return null;
        }
        AudioManager audioManager = event.getGuild().getAudioManager();
        AudioSendHandler sendHandler = audioManager.getSendingHandler();
        if (sendHandler == null) {
            sendHandler = new MainAudioSendHandler(member.getGuild().getIdLong(), event.getJDA());
            audioManager.setSendingHandler(sendHandler);
            audioManager.openAudioConnection(voiceChannel);
        } else if (!(sendHandler instanceof MainAudioSendHandler)) {
            event.reply("Somehow there's a different AudioHandler from the one I made being used by the bot. I never implemented any other handlers, so no clue what's happening").setEphemeral(true).queue();
            return null;
        }
        
        return (MainAudioSendHandler) sendHandler;
    }
}
