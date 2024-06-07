package stone.rpbot.audio;

import java.util.NavigableMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AudioQueue implements AudioSupplier, Runnable {

    private NavigableMap<TrackPriority, Track> trackList = new ConcurrentSkipListMap<>();
    private AtomicInteger lastId = new AtomicInteger();

    private volatile Track currentTrack;
    // these could be the same queue
    private volatile Queue<byte[]> currentlyPlaying;
    private volatile Queue<byte[]> currentlyBuffering;

    private volatile Track bufferingTrack;

    private boolean isPlaying = false;
    private AtomicBoolean isBuffering = new AtomicBoolean(false);

    private boolean finishedBuffering = false;
    private int volume = 100;
    private double percentVolume = 1;

    public void addTrack(Track stream, int priority) {
        TrackPriority trackPriority = new TrackPriority(priority, lastId.incrementAndGet());
        trackList.put(trackPriority, stream);
        if (currentlyPlaying == null) {
            this.skip();
        }
    }

    public void addTrack(Track stream) {
        this.addTrack(stream, 0);
    }

    @Override
    public byte[] getPacket() {
        byte[] packet = currentlyPlaying.poll();
        if (currentlyPlaying.isEmpty()) {
            if (currentlyPlaying != currentlyBuffering) {
                currentlyPlaying = currentlyBuffering;
            }
            if (currentTrack != bufferingTrack) {
                currentTrack = bufferingTrack;
            }
        }
        if (packet == null) {
            packet = AudioUtils.EMPTY_PACKET;
        }
        for (int i = 0; i < packet.length; i++) {
            int oldValue = (packet[i] << 8) + packet[i + 1];
            int newValue = Math.min(Math.max((int) (oldValue * this.percentVolume), -32768), 32767);
            packet[i] = (byte) ((newValue & 0x0000ff00) >> 8);
            packet[i + 1] = (byte) (newValue & 0x000000ff);
        }
        MainAudioSendHandler.threadPool.submit(this);
        return packet;
    }

    @Override
    public void run() {
        if (!isBuffering.compareAndExchange(false, true)) {
            System.out.println(finishedBuffering);
            if (currentlyPlaying == currentlyBuffering && finishedBuffering) {
                currentlyBuffering = new ConcurrentLinkedQueue<byte[]>();
                bufferingTrack = trackList.pollFirstEntry().getValue();
                finishedBuffering = false;
            }
            if (!finishedBuffering) {
                finishedBuffering = !AudioUtils.fillAudioQueue(currentlyBuffering, bufferingTrack,
                        10000 / 20 - currentlyBuffering.size());
            }

            isBuffering.set(false);
        }
    }

    @Override
    public boolean isClosed() {
        return currentlyPlaying.isEmpty() && currentlyBuffering.isEmpty();
    }

    @Override
    public boolean isPlaying() {
        return this.isPlaying;
    }

    public void setIsPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public boolean togglePlaying() {
        return this.isPlaying = !this.isPlaying;
    }

    public void skip() {
        this.currentTrack = trackList.pollFirstEntry().getValue();
        this.bufferingTrack = currentTrack;

        this.currentlyPlaying = new ConcurrentLinkedQueue<byte[]>();
        this.currentlyBuffering = currentlyPlaying;

        AudioUtils.fillAudioQueue(currentlyBuffering, bufferingTrack, 10000 / 20);
    }

    public void addVolume(int delta) {
        this.volume += delta;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public record TrackPriority(int priority, int FIFO) implements Comparable<TrackPriority> {

        @Override
        public int compareTo(TrackPriority other) {
            if (this.priority == other.priority) {
                // fallback to FIFO ordering
                return this.FIFO - other.FIFO;
            } else {
                return this.priority - other.priority;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof TrackPriority))
                return false;
            TrackPriority other = (TrackPriority) o;
            return this.priority == other.priority && this.FIFO == other.FIFO;
        }
    }
}
