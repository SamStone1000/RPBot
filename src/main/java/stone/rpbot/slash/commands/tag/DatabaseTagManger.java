package stone.rpbot.slash.commands.tag;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class DatabaseTagManager implements TagManager<LazyTag> {
    static Connection DATABASE;

    private final PreparedStatement insertInfo;
    private final PreparedStatement insertRelation;

    public DatabaseTagManager() {
        this.insertInfo = DATABASE.prepareStatement("INSERT INTO info(name, short_description, description VALUES (?, ?, ?);");
        this.insertRelation = DATABASE.prepareStatement("INSERT INTO relations(super, sub) VALUES (?, ?);");
    }
    
    public LazyTag createTag(String name, String shortDescription, String description, LazyTag superTag) {
        synchronized (insertTagStatement) {
            insertInfo.setString(1, name);
            insertInfo.setString(2, shortDescription);
            insertInfo.setString(3, description);
            insertInfo.executeQuery();
        } //somehow get the id from this execution?? idk
    }
}
