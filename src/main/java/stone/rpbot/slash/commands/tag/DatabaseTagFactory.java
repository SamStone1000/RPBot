package stone.rpbot.slash.commands.tag;

import java.util.HashSet;
import java.util.Set;
import java.lang.Exception;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseTagFactory implements TagFactory {
    private static Connection DATABASE;
    
    private final PreparedStatement id2info;
    private final PreparedStatement alias2id;
    private final PreparedStatement id2alias;
    private final PreparedStatement super2subs;
    private final PreparedStatement sub2super;
    private final PreparedStatement id2ratings;
        
    public DatabaseTagFactory() throws SQLException{
        this.id2info = DATABASE.prepareStatement("SELECT name, short_description, description FROM info WHERE id = ?;");
        
        this.alias2id = DATABASE.prepareStatement("SELECT id FROM aliases WHERE alias = ?;");
        this.id2alias = DATABASE.prepareStatement("SELECT alias FROM aliases WHERE id = ?;");

        this.super2subs = DATABASE.prepareStatement("SELECT sub FROM relations WHERE super = ?;");
        this.sub2super = DATABASE.prepareStatement("SELECT super FROM relations WHERE sub = ?;");

        this.id2ratings = DATABASE.prepareStatement("SELECT user, value, type FROM ratings WHERE id = ?;");
    }
    
    public Tag makeTag(long id) {
        try {
            LazyTag.Builder builder = new LazyTag.Builder();
            builder.setID(id);
            synchronized (id2info) {
                id2info.setLong(1, id);
                try (ResultSet info = id2info.executeQuery()) {
                    if (info.next()) {
                        builder.setName(info.getString("name"));
                        builder.setShortDescription(info.getString("short_description"));
                        builder.setDescription(info.getString("description"));
                    } else {
                        //accessing non existant tag??
                        return null;
                    }
                } catch (SQLException e) {
                    return null;
                }
            }

            synchronized (id2alias) {
                id2alias.setLong(1, id);
                try (ResultSet aliases = id2info.executeQuery()) {
                    while (aliases.next()) {
                        builder.addAlias(aliases.getString("alias"));
                    }
                } catch (SQLException e) {
                    return null;
                }
            }

            synchronized (sub2super) {
                sub2super.setLong(1, id);
                try (ResultSet superTag = sub2super.executeQuery()) {
                    if (superTag.next()) {
                        builder.setSuperTag(superTag.getLong("super"));
                    } else {
                        // no super tag
                    }
                } catch (SQLException e) {
                    return null;
                }
            }

            synchronized (super2subs) {
                super2subs.setLong(1, id);
                try (ResultSet subTags = super2subs.executeQuery()) {
                    while (subTags.next()) {
                        builder.addSubTag(subTags.getLong("sub"));
                    }
                } catch (SQLException e) {
                    return null;
                }
            }

            synchronized (id2ratings) {
                id2ratings.setLong(1, id);
                try (ResultSet ratings = id2ratings.executeQuery()) {
                    while (ratings.next()) {
                        builder.addRating(ratings.getLong("user"), Tag.Rating.of(ratings.getInt("value"), ratings.getShort("type")));
                    }
                } catch (SQLException e) {
                    return null;
                }
            }
            return builder.build();
        } catch (SQLException e) {
            return null;
        }
    }

    public static void connect() throws SQLException {
        DatabaseTagManager.DATABASE = DriverManager.getConnection("jdbc:derby:tags;create=true");
        DatabaseTagFactory.DATABASE = DatabaseTagManager.DATABASE;

        ResultSet tables = DATABASE.getMetaData().getTables(null, null, null, new String[] { "TABLE" });
        Set<String> existingTables = new HashSet<String>();
        while (tables.next()) {
            existingTables.add(tables.getString("TABLE_NAME"));
        }

        Statement statement = DATABASE.createStatement();

        if (!existingTables.contains("info")) {
            statement.execute(
                              "CREATE TABLE info(" +
                              "id BIGINT " +
                                "GENERATED ALWAYS AS IDENTITY " +
                                "(START WITH -1, INCREMENT BY 1, NO CYCLE)," +
                              "name VARCHAR(512) NOT NULL, " +
                              "short_description VARCHAR(512), " +
                              "description VARCHAR(32672), " +
                              "CONSTRAINT PK_info_id" +
                                "PRIMARY KEY(id));"
                              );
            statement.execute(
                              "INSERT INTO info" +
                              "(name, short_description, description) " +
                              "VALUES " +
                              "(\"_root\", \"The virtual tag that's the supertag of all root tags\", \"If you see this something really bad has happened. Call me an idiot and report this please!\");");
        }

        if (!existingTables.contains("aliases")) {
            statement.execute(
                              "CREATE TABLE aliases(" +
                              "id BIGINT NOT NULL," +
                              "alias VARCHAR(512) NOT NULL" +
                              "CONSTRAINT PK_aliases_id" +
                                "PRIMARY KEY(id, alias), " +
                              "CONSTRAINT FK_aliases_id" +
                                "FOREIGN KEY(id) " +
                                "REFERENCES info(id) " +
                                "ON DELETE CASCADE);"
                              );
                        statement.execute(
                              "INSERT INTO aliases" +
                              "(id, alias) " +
                              "VALUES " +
                              "(-1, \"_root\");");
        }

        if (!existingTables.contains("ratings")) {
            statement.execute(
                              "CREATE TABLE ratings(" +
                              "id BIGINT NOT NULL, " +
                              "user BIGINT NOT NULL, " +
                              "value INT NOT NULL, " +
                              "type SMALLINT NOT NULL, " +
                              "CONSTRAINT PK_ratings_id_user " +
                                "PRIMARY KEY(id, user), " +
                              "CONSTRAINT FK_ratings_id" +
                                "FOREIGN KEY(id) " +
                                "REFERENCES info(id) " +
                                "ON DELETE CASCADE);"
                              );
        }

        LazyTag.FACTORY = new DatabaseTagFactory();
        DatabaseTagManager.INSTANCE = new DatabaseTagManager();
    }

    public static void disconnect() throws SQLException {
        DATABASE.close();
    }

    public static class TagDatabaseException extends Exception {
        public static enum Type {
            NO_TABLE, TAG_EXISTS, INVALID_NAME_ID
        }

        private final Type type;

        public TagDatabaseException(Type type, String reason) {
            super(reason);
            this.type = type;
        }
        public TagDatabaseException(Type type) {
            this(type, "Error of Type: "+type.toString());
        }

        public Type getType() {
            return type;
        }
    }
}
