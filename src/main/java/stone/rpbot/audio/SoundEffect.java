package stone.rpbot.audio;

import net.dv8tion.jda.api.audio.AudioSendHandler;

import stone.rpbot.audio.AudioSupplier;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 * A one-off sound effect
 *
 * Effectively a buffered wrapper around an AudioInputStream
 */
public class SoundEffect implements AudioSupplier, Runnable {
    
	private static final int BUFFER_LENGTH = 10 * 1000 / 20;
    private AudioInputStream stream;
    private boolean isClosed = false;
    
    private Queue<byte[]> buffer = new ConcurrentLinkedQueue<>();
    private AtomicBoolean isBuffering = new AtomicBoolean(false);
    private AtomicInteger readAhead = new AtomicInteger(0);
    
    public SoundEffect(AudioInputStream stream) {
    	if (stream.getFormat().matches(AudioSendHandler.INPUT_FORMAT)) {
    		this.stream = stream;
    	} else {
    		this.stream = AudioSystem.getAudioInputStream(AudioSendHandler.INPUT_FORMAT, stream);
    	}
        
    }

    @Override
    public byte[] getPacket() {
        byte[] packet = buffer.remove();
        MainAudioSendHandler.threadPool.submit(this);
        readAhead.decrementAndGet();
        return packet;
    }

    @Override
    public boolean isClosed() {
        return buffer.isEmpty();
    }

	@Override
	public void run() {
		if (!isBuffering.compareAndExchange(false, true)) {
			AudioUtils.fillAudioQueue(buffer, this.stream, readAhead, BUFFER_LENGTH - readAhead.get());
			isBuffering.set(false);
		}
	}
}
