package stone.rpbot.audio;

import java.util.Collections;
import java.util.ConcurrentLinkedQueue;
import java.util.HashSet;
import java.util.Set;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MainAudioSendHandler implements AudioSendHandler {

    // small buffer to help reduce concurrency problems
    // should only have ~100 ms since it's not possible to edit the sound once it gets in here
    // This is the FINAL audio packets to send out to clients, it already has all the audio mixed into one stream
    private Queue<ByteBuffer> packets = new ConcurrentLinkedQueue<>();
    
    @Override
    public boolean canProvide() {
        return !packets.isEmpty();
    }

    @OVerride
    public ByteBuffer provide20MsAudio() {
        return packets.remove();
    }

    @Override
    public boolean isOpus() {
        return false;
    }

    private class AudioStreamManager implements Runnable {

        private static AtomicInteger lastId = new AtomicInteger();
        
        /**
         * The number of bytes per buffer for 20 ms
         */
        private static int packetLength;
        /**
         * The number of ByteBuffers per Queue to have acceptable buffer of data. 10s of audio
         */
        private static int queueLength = 10 * 1000 / 20; // 10 s / 20 ms

        static {
            int byteSize = Byte.BITS;
            float sampleRate = AudioSendHandler.INPUT_FORMAT.getSampleRate() / 1000f;
            int sampleSize = AudioSendHandler.INPUT_FORMAT.getSampleSizeInBits();
            int bufferTime = 20; // packet length in ms
            bufferLength = (int) (20 * (sampleRate * sampleSize) / byteSize);
        }
        
        private AtomicBoolean isMixing = new AtomicBoolean(false);
        private Map<Integer, AudioInputStream> audioStreams = new ConcurrentHashMap<>();
        private Map<Integer, Queue<ByteBuffer>> packetQueues = new ConcurrentHashMap<>();
        
        @Override
        public void run() {
            if (isMixing.get()) {
                return;
            }
            
            isMixing.set(true);
            Iterator<Map.Entry<Integer, AudioInputStream>> entries = audioStream.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<Integer, AudioInputStream> entry = entries.next();
                Queue<ByteBuffer> packets = packetQueues.get(entry.getKey());
                int size = packets.size();
                if (size < queueLength) {
                    AudioInputStream audioStream = entry.getValue();
                    if (fillAudioQueue(packets, audioStream, (queueLength * 2) - size))
                        entries.remove();
                }
            }
        }

        public int addAudioStream(AudioInputStream audioStream) {
            if (!audioStream.getFormat().matches(AudioSendHandler.INPUT_FORMAT)) {
                audioStream = AudioSystem.getAudioInputStream(AudioSendHandler.INPUT_FORMAT, audioStream);
            }

            int nextId = lastId.incrementAndGet();
            Queue<ByteBuffer> oacketQueue = new ConcurrentLinkedQueue<>();
            boolean emptied = fillAudioQueue(packetQueue, audioStream, queueLength);
            // Queue has to go in first to prevent the mixing from trying to
            // grap a queue that didn't exist because a stream that exists in
            // the stream map should have a counterpart in the queue map
            packetQueues.put(nextId, packetQueue); 
            if (!emptied) {
                audioStreams.put(nextId, audioStream);
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
        private boolean fillAudioQueue(Queue<ByteBuffer> packets, AudioInputStream audioStream, int length) {
            for (int i = 0; i < length; i++) {
                byte[] packet = new byte[packetLength];
                // this weird order is to ensure that the queue doesn't have a unprocessed packet in it (which could be seen on another thread potentially)
                boolean empty = audioStream.read(packet) < packetLength;
                packets.add(ByteBuffer.wrap(packet));
                if (empty) {
                    audioStream.close();
                    return false;
                }
                }
            }

            return true;
        }
            
