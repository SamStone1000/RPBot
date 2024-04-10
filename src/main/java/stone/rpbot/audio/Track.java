package stone.rpbot.audio;

import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public interface Track extends AudioSupplier {
    public String getTitle();
    public String getArtist();
    
    public static class File implements Track {
    	
    	private String title;
    	private String artist;

        private AudioInputStream stream;
        private boolean isClosed = false;

        @Override
        public byte[] getPacket() {
            byte[] packet = new byte[AudioUtils.PACKET_ARRAY_LENGTH];
            try {
                int readLength = this.stream.read(packet);
                this.isClosed = readLength < AudioUtils.PACKET_ARRAY_LENGTH;
                if (this.isClosed) {
                    this.stream.close();
                }
                return packet;
            } catch (IOException e) {
                // something went wrong reading data, or closing the stream
                this.isClosed = true;
                try {
                    this.stream.close();
                } catch (IOException d) {
                    // something went wrong on the safety close
                } finally {
                    //return empty array in case of any errors
                    return new byte[AudioUtils.PACKET_ARRAY_LENGTH];
                }
            } 
        }

        @Override
        public boolean isClosed() {
            return this.isClosed;
        }
        
    	public File(Path path, String title, String artist) throws UnsupportedAudioFileException, IOException {
            this.stream = AudioSystem.getAudioInputStream(AudioSendHandler.INPUT_FORMAT, AudioSystem.getAudioInputStream(new BufferedInputStream(Files.newInputStream(path))));
            this.title = title;
            this.artist = artist;
    	}

        @Override
        public String getTitle() {
            return this.title;
        }

        @Override
        public String getArtist() {
            return this.artist;
        }
    	
    }
}
