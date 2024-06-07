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
    private volatile Queue<byte[]> currentlyPlaying = null;
    private volatile Queue<byte[]> currentlyBuffering = null;

    private volatile Track bufferingTrack;

    private boolean isPlaying = false;
    private AtomicBoolean isBuffering = new AtomicBoolean(false);

    private boolean finishedBuffering = false;
    private int volume = 100;
    private double percentVolume = .5;

    public void addTrack(Track stream, int priority) {
        System.out.println("adding track!");
        TrackPriority trackPriority = new TrackPriority(priority, lastId.incrementAndGet());
        trackList.put(trackPriority, stream);
        System.out.println("currentlyPlaying == null -> "+(currentlyPlaying == null));
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
        byte[] newPacket = new byte[AudioUtils.PACKET_ARRAY_LENGTH];
        for (int i = 0; i < packet.length; i += 2) {
            int oldValue = (packet[i] << 8) + packet[i + 1];
            int newValue = (int) Math.min(Math.max(oldValue * this.percentVolume, -32768), 32767);
            newPacket[i] = (byte) ((newValue & 0x0000ff00) >> 8);
            newPacket[i + 1] = (byte) (newValue & 0x000000ff);
        }
    
        MainAudioSendHandler.threadPool.submit(this);
        return newPacket;
    }

    @Override
    public void run() {
        if (!isBuffering.compareAndExchange(false, true)) {
            System.out.println("AudioQueue: finishedBuffering = "+finishedBuffering);
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
        System.out.println("currentlyPlaying.isEmpty() + "+ currentlyPlaying.isEmpty());
        System.out.println("currentlyBuffering.isEmpty() + " + currentlyBuffering.isEmpty());
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
        this.setVolume(this.volume + delta);
    }

    public void setVolume(int volume) {
        this.volume = volume;
        this.percentVolume = (this.volume / 100d);
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
