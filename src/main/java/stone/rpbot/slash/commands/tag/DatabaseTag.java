package stone.rpbot.slash.commands.tag;

import java.lang.IllegalArgumentException;
import java.util.concurrent.atomic.AtomicLong;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.DriverManager;

public class DatabaseTag implements Tag {
    
    private static final Connection DATABASE;

    private final String table;
    private final long id;

    static {
        DATABASE = DriverManager.getConnection("jdbc:derby:tags;create=true");
    }

    public DatabaseTag(long id, String table) {
        this.id = id;
    }

    public DatabaseTag(String name, String description, String table, Set<String> aliases, Tag superTag, Set<Tag> subTags) {
        try (Statement statement = DATABASE.createStatement()) {
            if (statement.execute("SELECT normalized FROM _tables WHERE name = "+table+';')) {
                try (ResultSet rs = state.getResultSet()) {
                    rs.first();
                    this.table = rs.getString(1);
                }
            } else {// Nothing found
                throw new TagDatabaseException(TagDatabaseException.Type.NO_TABLE, "The specified table: "+table+" doesn't exist!");
            }
        }
    }
    
    private void readData() {  
        PreparedStatement selectInfo = DATABASE.prepareStatement("SELECT name, description FROM "+table+"_info WHERE id = ?;");
        selectInfo.setLong(1, id);
    }

    public static class TagDatabaseException extends Exception {
        public static enum Type {
            NO_TABLE, TAG_EXISTS
        }

        Type type;

        public TagDatabaseException(Type type, String reason) {
            super(reason);
            this.type = type;
        }
        public TagDatabaseException(Type type) {
            super(type, "Error of Type: "+type.name());
        }
    }
}
                                                                  
