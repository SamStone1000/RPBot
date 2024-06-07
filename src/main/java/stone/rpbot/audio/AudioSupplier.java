package stone.rpbot.audio;

/**
 * An abstracted supplier of audio data
 *
 * Generally designed to be like an AudioStream with more information to allow
 * things like pausing the stream or fading two concatenated streams
 */
public interface AudioSupplier {

	public byte[] getPacket();

	public default boolean isPlaying() {
		return true;
	}

	public boolean isClosed();
}
