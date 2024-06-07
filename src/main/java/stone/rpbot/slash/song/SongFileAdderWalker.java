package stone.rpbot.slash.song;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import stone.rpbot.audio.AudioQueue;
import stone.rpbot.audio.Track;

public class SongFileAdderWalker extends SimpleFileVisitor<Path> {
    AudioQueue queue;

    public SongFileAdderWalker(AudioQueue queue) {
        this.queue = queue;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        this.queue.addTrack(new Track.File(file, file.getFileName().toString(), "lol"));
        return FileVisitResult.CONTINUE;
    }
}
