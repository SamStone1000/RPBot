package stone.rpbot.slash.commands.tag;

import stone.rpbot.slash.commands.tag.Tag.Rating;

import java.util.Set;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseTagManager implements TagManager {
    static Connection DATABASE;

    public static TagManager INSTANCE;

    private final TagFactory factory;
    
    private final PreparedStatement insertInfo;
    private final PreparedStatement insertRelation;
    private final PreparedStatement insertAlias;
    private final PreparedStatement removeAlias;
    private final PreparedStatement insertRating;
    private final PreparedStatement updateRating;

    public DatabaseTagManager() throws SQLException {
        this.factory = new DatabaseTagFactory();
        
        this.insertInfo = DATABASE.prepareStatement("INSERT INTO info(name, short_description, description VALUES (?, ?, ?) OUTPUT id;");
        this.insertRelation = DATABASE.prepareStatement("INSERT INTO relations(super, sub) VALUES (?, ?);");
        this.insertAlias = DATABASE.prepareStatement("INSERT INTO aliases(id, alias);");
        this.removeAlias = DATABASE.prepareStatement("DELETE FROM aliases WHERE id = ? AND alias = ?;");
        this.insertRating = DATABASE.prepareStatement("INSERT INTO rating" +
                                                      " (id, user, value, type) " +
                                                      "VALUES " +
                                                      "(?, ?, ?, ?);"
                                                      );
        this.updateRating = DATABASE.prepareStatement("UPDATE ratings SET " +
                                                      "value = ?, type = ? " +
                                                      "WHERE id = ? AND user = ?;"
                                                      );
    }

    @Override
    public Tag createTag(String name, String shortDescription, String description, Tag superTag) {
        long id;
        synchronized (insertInfo) {
            try {
                insertInfo.setString(1, name);
                insertInfo.setString(2, shortDescription);
                insertInfo.setString(3, description);
                try (ResultSet rs = insertInfo.executeQuery();) {
                    rs.next();
                    id = rs.getLong("id");
                }
            } catch (SQLException e) {
                return null;
            }
        }

        long superID = superTag == null ? -1 : superTag.getID();

        synchronized (insertRelation) {
            try {
                insertRelation.setLong(1, superID);
                insertRelation.setLong(2, id);
                insertRelation.executeUpdate();
            } catch (SQLException e) {
                return null;
            }
        }

        LazyTag.Builder builder = new LazyTag.Builder();
        builder.setID(id);
        builder.setName(name);
        builder.setShortDescription(shortDescription);
        builder.setDescription(description);
        builder.setSuperTag(superID);
        return builder.build();
    }

    @Override
    public boolean addAliases(Tag tag, String... aliases) {
        synchronized (insertAlias) {
            try {
                insertAlias.setLong(1, tag.getID());
                for (String alias : aliases) {
                    insertAlias.setString(2, alias);
                    insertAlias.executeUpdate();
                }
            } catch (SQLException e) {
                return false;
            }
        }

        return true;
    }
    
    @Override
    public boolean removeAliases(Tag tag, String... aliases) {
        synchronized (removeAlias) {
            try {
                removeAlias.setLong(1, tag.getID());

                for (String alias : aliases) {
                    removeAlias.setString(2, alias);
                    removeAlias.executeUpdate();
                }
            } catch (SQLException e) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean addRating(Tag tag, long user, Tag.Rating rating) {
        synchronized (insertRating) {
            try {
                insertRating.setLong(1, tag.getID());
                insertRating.setLong(2, user);
                insertRating.setInt(3, rating.value());
                insertRating.setShort(4, (short) rating.type().ordinal());
                insertRating.executeUpdate();
            } catch (SQLException e) {
                return false;
            }
        }
        return onRatingChanged(tag, user, rating);
    }

    @Override
    public boolean updateRating(Tag tag, long user, Rating rating) {
        Rating oldRating = tag.getRatings().get(user);
        if (!oldRating.equals(rating)) {
            synchronized (updateRating) {
                try {
                    updateRating.setLong(3, tag.getID());
                    updateRating.setLong(4, user);
                    updateRating.setInt(1, rating.value());
                    updateRating.setShort(2, (short) rating.type().ordinal());
                    updateRating.executeUpdate();
                } catch (SQLException e) {
                    return false;
                }
            }


            return onRatingChanged(tag, user, rating);
        } else {
            return true;
        }
    }

    @Override
    public boolean removeRating(Tag tag, long user) {
        return false;
    }

    public boolean onRatingChanged(Tag tag, long user, Rating newRating) {
        boolean hasFailed = false;
        if (newRating.shouldPropagateUp()) {
            Tag superTag = tag.getSuperTag();
            if (superTag != null) {
                Rating superRating = superTag.getRatings().get(user);
                if (superRating.canBeOverriden(newRating)) {
                    Set<Tag> siblingTags = superTag.getSubTags();
                    int avgValue = (int) (siblingTags.stream()
                                           .map(t -> t.getRatings().get(user))
                                           .filter(r -> r.shouldPropagateUp())
                                           .mapToInt(r -> r.value())
                                           .average().getAsDouble());

                    hasFailed |= !this.setRating(tag, user, avgValue, Rating.Type.IMPLICIT_SUB);
                }
            }
        }

        if (newRating.shouldPropagateDown()) {
            Rating subRating = newRating.with(Rating.Type.IMPLICIT_SUPER);
            Set<Tag> subTags = tag.getSubTags();
            for (Tag subTag : subTags) {
                if (subTag.getRating(user).canBeOverriden(newRating))
                    hasFailed |= !this.setRating(tag, user, subRating);
            }
        }

        return !hasFailed;
    }

    @Override
    public Tag getTag(String id) {
        return null;
    }
                
}
