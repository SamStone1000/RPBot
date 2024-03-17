package stone.rpbot.slash.commands.tag;

import java.lang.Exception;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
