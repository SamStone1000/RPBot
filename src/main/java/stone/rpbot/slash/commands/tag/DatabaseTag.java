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

    public DatabaseTag(String name, String description, String table, String nameID, Set<String> aliases, Tag superTag, Set<Tag> subTags) {
        try (Statement statement = DATABASE.createStatement()) {
            if (statement.execute("SELECT normalized FROM _tables WHERE name = "+table+';')) {
                try (ResultSet rs = state.getResultSet()) {
                    rs.first();
                    this.table = rs.getString(1);
                }
            } else {// Nothing found
                throw new TagDatabaseException(TagDatabaseException.Type.NO_TABLE, "The specified table: "+table+" doesn't exist!");
            }

            if (!isValidNameID(nameID)) {
                throw new TagDatabaseException(TagDatabaseException.Type.INVALID_NAME_TAG, "The name ID was invalid!");
            }

            this.nameID = normalizeNameID(nameID);

            if (statement.execute("SELECT * FROM "+this.table+" WHERE name = "+this.nameID+';')) {
                throw new TagDatabaseException(TagDatabaseException.Type.TAG_EXISTS, "The tag already exists");
            }

            
        }
    }
    
    private void readData() {  
        PreparedStatement selectInfo = DATABASE.prepareStatement("SELECT name, description FROM "+table+"_info WHERE id = ?;");
        selectInfo.setLong(1, id);
    }

    private String normalizeNameID(String nameID) {
        try (Statement statement = DATABASE.createStatement()) {
            

    private boolean isValidNameID(String nameID) {
        for (int i=0; i<str.length(); i++) {
            char c = str.charAt(i);
            if (c < 0x30 || (c >= 0x3a && c <= 0x40) || (c > 0x5a && c <= 0x60) || c > 0x7a)
                return false;
        }
        return true;
    }

    public static class TagDatabaseException extends Exception {
        public static enum Type {
            NO_TABLE, TAG_EXISTS, INVALID_NAME_ID
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
                                                                  
