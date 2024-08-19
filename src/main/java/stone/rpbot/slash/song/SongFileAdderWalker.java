package stone.rpbot.slash.song;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import stone.rpbot.audio.AudioQueue;
import stone.rpbot.audio.Track;

public class SongFileAdderWalker extends SimpleFileVisitor<Path> {

    private final List<Track.File> tracks = new ArrayList<>();
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        this.tracks.add(new Track.File(file, file.getFileName().toString(), "lol"));
        return FileVisitResult.CONTINUE;
    }

    public void add(AudioQueue queue) {
        this.tracks.sort((trackA, trackB)  -> {
                return trackA.getPath().compareTo(trackB.getPath());
        });
        for (Track track : tracks) {
            queue.addTrack(track);
        }
    }
}
