package stone.rpbot.audio;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Queue;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.managers.AudioManager;

public class MainAudioSendHandler implements AudioSendHandler {

    // small buffer to help reduce concurrency problems
    // should only have ~100 ms since it's not possible to edit the sound once it
    // gets in here
    // This is the FINAL audio packets to send out to clients, it already has all
    // the audio mixed into one stream
    private Queue<ByteBuffer> packets = new ConcurrentLinkedQueue<>();
    private static int mixedLength = 100 / 40; // 100 ms / 20 ms for number of mixed packets to buffer

    private Map<Integer, AudioSupplier> audioSuppliers = new ConcurrentHashMap<>();
    private AudioQueue songQueue;

    private static AtomicInteger lastId = new AtomicInteger();

    /**
     * The number of ByteBuffers per Queue to have an acceptable buffer of data. 10s
     * of audio
     */
    private static int queueLength = 10 * 1000 / 20; // 10 s / 20 ms

    private MainAudioSendHandler.AudioStreamMixer audioStreamMixer = this.new AudioStreamMixer();

    static ExecutorService threadPool = Executors.newCachedThreadPool();
    // if this needs more than 1 thread I'll eat my hat
    private static ScheduledExecutorService activityPool = Executors.newSingleThreadScheduledExecutor();
    private AtomicBoolean hasProvided = new AtomicBoolean(true);

    public MainAudioSendHandler(long guildId, JDA jda) {
        activityPool.schedule(this.new ActivityDetector(guildId, jda), 30, TimeUnit.SECONDS);
    }

    @Override
    public boolean canProvide() {
        return !packets.isEmpty();
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        threadPool.submit(audioStreamMixer);
        System.out.println("Handler: packets.size() = "+packets.size());
        hasProvided.set(true);
        return packets.remove();
    }

    @Override
    public boolean isOpus() {
        return false;
    }

    public int add(AudioInputStream audioStream) {
        return this.add(new SoundEffect(audioStream));
    }

    public int addTempAudioFile(Path file) throws IOException, UnsupportedAudioFileException {
        // InputStream fileStream = Files.newInputStream(file,
        // StandardOpenOption.DELETE_ON_CLOSE);
        InputStream fileStream = new BufferedInputStream(
                                                         Files.newInputStream(file, StandardOpenOption.DELETE_ON_CLOSE));
        return this.add(AudioSystem.getAudioInputStream(fileStream));
        // return this.addAudioStream(AudioSystem.getAudioInputStream(file.toFile()));
    }

    public int add(AudioSupplier supplier) {
        int id = lastId.incrementAndGet();
        this.audioSuppliers.put(id, supplier);
        MainAudioSendHandler.threadPool.submit(audioStreamMixer);
        System.out.println("Adding supplier: "+supplier);
        return id;
    }

    public AudioQueue getSongQueue() {
        if (this.songQueue == null) {
            this.songQueue = new AudioQueue();
            add(this.songQueue);
        }
        return this.songQueue;
    }

    public boolean removeAudio(int id) {
        return this.audioSuppliers.remove(id) != null;
    }

    private class AudioStreamMixer implements Runnable {

        private AtomicBoolean isMixing = new AtomicBoolean(false);

        @Override
        public void run() {
            if (isMixing.compareAndExchange(false, true)) {
                return;
            }

            if (!audioSuppliers.isEmpty()) {
                AtomicBoolean didWork = new AtomicBoolean(false);
                byte[][] mixedPackets = new byte[mixedLength - packets.size()][AudioUtils.PACKET_ARRAY_LENGTH];

                Spliterator<Map.Entry<Integer, AudioSupplier>> queues = audioSuppliers.entrySet().spliterator();
                queues.forEachRemaining((entry) -> {

                        AudioSupplier packetQueue = entry.getValue();
                    
                        if (packetQueue.isPlaying()) {
                            System.out.println("mixing");
                            didWork.set(true);
                            for (byte[] mixedPacket : mixedPackets) {
                                byte[] newPacket = packetQueue.getPacket();
                                for (int i = 0; i < mixedPacket.length; i += 2) {
                                    int mixedValue = (mixedPacket[i] << 8) + mixedPacket[i + 1];
                                    int newValue = (newPacket[i] << 8) + newPacket[i + 1];
                                    int sum = Math.min(Math.max(mixedValue + newValue, -32768), 32767);
                                    mixedPacket[i] = (byte) ((sum & 0x0000ff00) >> 8);
                                    mixedPacket[i + 1] = (byte) (sum & 0x000000ff);
                                }
                                if (packetQueue.isClosed()) {
                                    System.out.println("Mixer: removing");
                                    audioSuppliers.remove(entry.getKey());
                                    break;
                                }
                            }
                        }
                    });

                for (byte[] mixedPacket : mixedPackets) {
                    packets.add(ByteBuffer.wrap(mixedPacket));
                }
            }

            isMixing.set(false);
        }
    }

    public class ActivityDetector implements Runnable {

        private long guild;
        private JDA jda;

        public ActivityDetector(long guildId, JDA jda) {
            this.guild = guildId;
            this.jda = jda;
        }

        @Override
        public void run() {
            if (!hasProvided.compareAndExchange(true, false)) {
                AudioManager manager = jda.getGuildById(guild).getAudioManager();
                synchronized (manager) {
                    if (manager.isConnected()) {
                        manager.closeAudioConnection();
                        manager.setSendingHandler(null);
                    }
                }
            } else {
                activityPool.schedule(this, 30, TimeUnit.SECONDS);
            }
            // TODO: Say "bye bye!" when leaving
        }
    }
}
