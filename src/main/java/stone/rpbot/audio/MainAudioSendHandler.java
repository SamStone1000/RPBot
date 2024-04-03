package stone.rpbot.audio;

import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.lang.Byte;
import java.lang.Exception;
import java.lang.Math;
import java.lang.Runnable;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class MainAudioSendHandler implements AudioSendHandler {

    // small buffer to help reduce concurrency problems
    // should only have ~100 ms since it's not possible to edit the sound once it gets in here
    // This is the FINAL audio packets to send out to clients, it already has all the audio mixed into one stream
    private Queue<ByteBuffer> packets = new ConcurrentLinkedQueue<>();
    private static int mixedLength = 100 / 40; // 100 ms / 20 ms for number of mixed packets to buffer

    private Map<Integer, Queue<byte[]>> packetQueues = new ConcurrentHashMap<>();

    /**
     * The number of bytes per buffer for 20 ms
     */
    private static int packetLength;

    private static AtomicInteger lastId = new AtomicInteger();
    
    /**
     * The number of ByteBuffers per Queue to have an acceptable buffer of data. 10s of audio
     */
    private static int queueLength = 10 * 1000 / 20; // 10 s / 20 ms
    
    static {
        int byteSize = Byte.SIZE;
        float sampleRate = AudioSendHandler.INPUT_FORMAT.getSampleRate() / 1000f;
        int sampleSize = AudioSendHandler.INPUT_FORMAT.getSampleSizeInBits();
        int bufferTime = 40; // packet length in ms
        packetLength = (int) (bufferTime * (sampleRate * sampleSize) / byteSize);
    }

    private MainAudioSendHandler.AudioStreamManager audioStreamManager = this.new AudioStreamManager();
    private MainAudioSendHandler.AudioStreamMixer audioStreamMixer = this.new AudioStreamMixer();

    private static ExecutorService threadPool = Executors.newCachedThreadPool();
    
    @Override
    public boolean canProvide() {
        return !packets.isEmpty();
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        threadPool.submit(audioStreamManager);
        threadPool.submit(audioStreamMixer);
        //System.out.println(packetQueues.size());
        return packets.remove();
    }

    @Override
    public boolean isOpus() {
        return false;
    }

    public int addAudioStream(AudioInputStream audioStream) {
        return this.audioStreamManager.addAudioStream(audioStream);
    }

    public int addTempAudioFile(Path file) throws IOException, UnsupportedAudioFileException {
        //InputStream fileStream = Files.newInputStream(file, StandardOpenOption.DELETE_ON_CLOSE);
        InputStream fileStream = new BufferedInputStream(Files.newInputStream(file, StandardOpenOption.DELETE_ON_CLOSE));
        return this.addAudioStream(AudioSystem.getAudioInputStream(fileStream));
        //return this.addAudioStream(AudioSystem.getAudioInputStream(file.toFile()));
    }

    public boolean removeAudioStream(int id) {
        return this.audioStreamManager.removeAudioStream(id);
    }

    private class AudioStreamManager implements Runnable {
        private AtomicBoolean isBuffering = new AtomicBoolean(false);
        private Map<Integer, AudioInputStream> audioStreams = new ConcurrentHashMap<>();
        
        @Override
        public void run() {
            try {
                //System.out.println("trying to buffer");
                if (isBuffering.compareAndExchange(false, true)) {
                    return;
                }


                //System.out.println("buffering");
                Spliterator<Map.Entry<Integer, AudioInputStream>> entries = audioStreams.entrySet().spliterator();
                //System.out.println(entries.hasNext());
                entries.forEachRemaining((entry) -> {
                        Queue<byte[]> packetQueue = packetQueues.get(entry.getKey());
                        int size = packetQueue.size();
                        //System.out.println(size);
                        if (size < queueLength) {
                            AudioInputStream audioStream = entry.getValue();
                            boolean hasMore = fillAudioQueue(packetQueue, audioStream, (queueLength * 2) - size);
                            if (!hasMore) {
                                System.out.println("Manager: removed!");
                                audioStreams.remove(entry.getKey());
                            }
                        }
                    });
                isBuffering.set(false);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }

        public int addAudioStream(AudioInputStream audioStream) {
            if (!audioStream.getFormat().matches(AudioSendHandler.INPUT_FORMAT)) {
                audioStream = AudioSystem.getAudioInputStream(AudioSendHandler.INPUT_FORMAT, audioStream);
            }

            int nextId = lastId.incrementAndGet();
            Queue<byte[]> packetQueue = new ConcurrentLinkedQueue<>();
            boolean hasMore = fillAudioQueue(packetQueue, audioStream, queueLength * 2);
            // Queue has to go in first to prevent the mixing from trying to
            // grap a queue that didn't exist because a stream that exists in
            // the stream map should have a counterpart in the queue map
            packetQueues.put(nextId, packetQueue); 
            if (hasMore) {
                audioStreams.put(nextId, audioStream);
            }

            if (packets.isEmpty()) {
                threadPool.submit(audioStreamMixer);
                //packets.add(ByteBuffer.wrap(new byte[packetLength]));
            }
            return nextId;
        }

        public boolean removeAudioStream(int id) {
            return audioStreams.remove(id) != null;
        }

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
        private boolean fillAudioQueue(Queue<byte[]> output, AudioInputStream audioStream, int length) {
            try {
                for (int i = 0; i < length; i++) {
                    byte[] packet = new byte[packetLength];
                    // this weird order is to ensure that the queue doesn't have a unprocessed packet in it (which could be seen on another thread potentially)
                    boolean empty = audioStream.read(packet) < packetLength;
                    output.add(packet);
                    if (empty) {
                        audioStream.close();
                        return false;
                    }
                }
            } catch (IOException e) {
                try {
                    audioStream.close();
                } catch (IOException d) {
                    return false;
                }
                return false;
            }
            return true;
        }
    }

    private class AudioStreamMixer implements Runnable {

        private AtomicBoolean isMixing = new AtomicBoolean(false);
        
        @Override
        public void run() {
            if (isMixing.compareAndExchange(false, true)) {
                return;
            }

            byte[][] mixedPackets = new byte[mixedLength - packets.size()][packetLength];
            
            Spliterator<Map.Entry<Integer, Queue<byte[]>>> queues = packetQueues.entrySet().spliterator();
            queues.forEachRemaining((entry) -> {
                    //System.out.println("mixing");
                    Queue<byte[]> packetQueue = entry.getValue();
                    for (byte[] mixedPacket : mixedPackets) {
                        byte[] newPacket = packetQueue.remove();
                        for (int i = 0; i < mixedPacket.length; i += 2) {
                            int mixedValue = (mixedPacket[i] << 8) + mixedPacket[i + 1];
                            int newValue = (newPacket[i] << 8) + newPacket[i + 1];
                            int sum = Math.min(Math.max(mixedValue + newValue, -32768), 32767);
                            mixedPacket[i] = (byte) ((sum & 0x0000ff00) >> 8);
                            mixedPacket[i + 1] = (byte) (sum & 0x000000ff);
                        }
                        if (packetQueue.isEmpty()) {
                            System.out.println("Mixer: removing");
                            packetQueues.remove(entry.getKey());
                            break;
                        }
                    }
                });

            for (byte[] mixedPacket : mixedPackets) {
                packets.add(ByteBuffer.wrap(mixedPacket));
            }

            isMixing.set(false);
        }
    }
}
