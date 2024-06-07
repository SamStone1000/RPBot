package stone.rpbot.audio;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import net.dv8tion.jda.api.audio.AudioSendHandler;

public interface Track extends AudioSupplier {
	public String getTitle();

	public String getArtist();

	public static class File implements Track {

		private String title;
		private String artist;

		private AudioInputStream stream;
		private boolean isClosed = false;

		private final Path file;

		@Override
		public byte[] getPacket() {
			if (this.stream != null) {
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
					}
					// return empty array in case of any errors
					return AudioUtils.EMPTY_PACKET;
				}
			} else {
				Path tempFile;
				try {
					tempFile = Files.createTempFile("rpbot-track", ".wav").toAbsolutePath();
					ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-y", "-i", file.toAbsolutePath().toString(),
							tempFile.toString());
					pb.redirectOutput(Path.of("/tmp/ffmpeg.log").toFile());
					pb.redirectError(Path.of("/tmp/ffmpeg.error").toFile());
					pb.start().waitFor();
				} catch (InterruptedException | IOException e) {
					return AudioUtils.EMPTY_PACKET;
				}
				try {
					this.stream = AudioSystem.getAudioInputStream(AudioSendHandler.INPUT_FORMAT,
							AudioSystem.getAudioInputStream(new BufferedInputStream(Files.newInputStream(tempFile))));
				} catch (UnsupportedAudioFileException | IOException e) {
					return AudioUtils.EMPTY_PACKET;
				}
				return getPacket();
			}
		}

		@Override
		public boolean isClosed() {
			return this.isClosed;
		}

		public File(Path path, String title, String artist) throws UnsupportedAudioFileException, IOException {
			this.file = path;
			try {
				this.stream = AudioSystem.getAudioInputStream(AudioSendHandler.INPUT_FORMAT,
						AudioSystem.getAudioInputStream(new BufferedInputStream(Files.newInputStream(path))));
			} catch (UnsupportedAudioFileException e) {
				this.stream = null;
			}
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
