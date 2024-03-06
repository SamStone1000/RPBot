package stone.rpbot.slash.commands.tag;

import java.lang.Exception;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseTagFactory implements TagFactory {

    private static final Connection DATABASE = DriverManager.getConnection("jdbc:derby:tags;create=true");
    
    private final PreparedStatement info2id;
    private final PreparedStatement alias2id;
    private final PreparedStatement id2alias;
    private final PreparedStatement super2subs;
    private final PreparedStatement subs2super;
    private final PreparedStatement id2ratings;
        
    public DatabaseTagFactory(long table) {
        id2info = DATABASE.prepareStatement("SELECT primary_alias, short_description, description FROM "+table+"_info WHERE id = ?;");
        
        alias2id = DATABASE.prepareStatement("SELECT id FROM "+table+"_alias WHERE alias = ?;");
        id2alias = DATABASE.prepareStatement("SELECT alias FROM "+table+"_alias WHERE id = ?;");

        super2subs = DATABASE.prepareStatement("SELECT sub FROM "+table+"_relation WHERE super = ?;");
        sub2super = DATABASE.prepareStatement("SELECT super FROM "+table+"_relation WHERE sub = ?;");

        id2ratings = DATABASE.prepareStatement("SELECT user, value, type FROM "+table+"_rating WHERE id = ?;");
    }
    
    public Tag makeTag(long id) {
        LazyTag.Builder builder = new LazyTag.Builder();
        synchronized (id2info) {
            id2info.setLong(1, id);
            try (ResultSet info = id2info.executeQuery()) {
                if (info.next()) {
                    builder.setPrimaryAlias(info.getString("primary_alias"));
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
                    builder.addRating(ratings.getLong("user"), Rating.of(ratings.getByte("value"), ratings.getByte("type")));
                }
            } catch (SQLException e) {
                return null;
            }
        }

        return builder.build();
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
            super(type, "Error of Type: "+type.name());
        }

        public Type getType() {
            return type;
        }
    }
}
