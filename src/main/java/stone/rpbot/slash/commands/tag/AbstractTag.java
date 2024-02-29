package stone.rpbot.slash.commands.tag;

import java.util.concurrent.atomic.AtomicLong;
import java.sql.Connection;
import java.sql.DriverManager;

public abstract class AbstractTag implements Tag {
    
    private static final AtomicLong nextID;
    private static final Connection DATABASE;

    private static final Path IDFile = Path.of(SharedConstants.BIN + "tag", "id.txt");
    
    private final long ID;

    static {
        DATABASE = DriverManager.getConnection("jdbc:derby:tags;create=true");
        
    
    private void readData(
