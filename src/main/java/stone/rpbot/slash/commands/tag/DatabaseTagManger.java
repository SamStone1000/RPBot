package stone.rpbot.slash.commands.tag;

import stone.rpbot.slash.commands.tag.Tag.Rating;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseTagManager implements TagManager<LazyTag> {
    static Connection DATABASE;

    private final TagFactory factory = new DatabaseTagFactory();
    
    private final PreparedStatement insertInfo;
    private final PreparedStatement insertRelation;
    private final PreparedStatement insertAlias;
    private final PreparedStatement removeAlias;
    private final PreparedStatement insertRating;
    private final PreparedStatement updateRating;

    public DatabaseTagManager() {
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
    public LazyTag createTag(String name, String shortDescription, String description, LazyTag superTag) {
        long id;
        synchronized (insertTag) {
            try {
                insertInfo.setString(1, name);
                insertInfo.setString(2, shortDescription);
                insertInfo.setString(3, description);
                ResultSet id = insertInfo.executeQuery();
                id = id.getLong("id");
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
    public boolean addAliases(LazyTag tag, String... aliases) {
        synchronized (insertAlias) {
            insertAlias.setLong(1, tag.getID());
            try {
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
    public boolean removeAliases(LazyTag tag, String... aliases) {
        synchronized (removeAlias) {
            removeAlias.setLong(1, tag.getID());
            try {
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
    public boolean addRating(LazyTag tag, long user, Tag.Rating rating) {
        synchronized (insertRating) {
            try {
                insertRating.setLong(1, tag.getID());
                insertRating.setLong(2, user);
                insertRating.setInt(3, rating.getValue());
                insertRating.setShort(4, (short) rating.getType().ordinal());
                insertRating.executeUpdate();
            } catch (SQLException e) {
                return false;
            }
        }
        return onRatingChanged(tag, user, rating);
    }

    @Override
    public boolean updateRating(LazyTag tag, long user, Rating rating) {
        Rating oldRating = tag.getRatings().get(user);
        if (!oldRating.equals(rating)) {
            synchronized (updateRating) {
                try {
                    updateRating.setLong(3, tag.getID());
                    updateRating.setLong(4, user);
                    updateRating.setInt(1, rating.getValue());
                    updateRating.setShort(2, (short) rating.getType().ordinal());
                    updateRating.executeUpdate();
                } catch (SQLException e) {
                    return false;
                }
            }


            return onRatingChanged(tag, user, oldRating, rating);
        } else {
            return true;
        }
    }

    @Override
    public boolean removeRating(LazyTag tag, long user) {
        
    }

    public boolean onRatingChanged(LazyTag tag, long user, Rating newRating) {
        if (newRating.getType() == UNRATED) {
            // deleting a rating, have to clear out implicit ratings
            
        } else {
            boolean hasFailed = false;
            if (newRating.shouldPropagateUp()) {
                Tag superTag = tag.getSuperTag();
                if (superTag != null) {
                    Rating superRating = superTag.getRatings().get(user);
                    if (superRating.canBeOverriden(newRating)) {
                        Set<Tag> siblingTags = superTag.getSubTags();
                        int avgRating = (int) (siblingTags.stream()
                                               .map(t -> t.getRatings().get(user))
                                               .filter(r -> r.shouldPropagateUp())
                                               .mapToInt(r -> r.getValue())
                                               .average().getAsDouble());

                        hasFailed |= !this.setRating(tag, user, avgRating, Rating.Type.IMPLICIT_SUB);
                    }
                }
            }

            if (newRating.shouldPropagateDown()) {
                Rating subRating = newRating.with(Rating.Type.IMPLICIT_SUPER);
                Set<Tag> subTags = tag.getSubTags();
                for (Tag subTag : subTags) {
                    if (subTag.getRating().get(user).canBeOverriden(newRating))
                        hasFailed |= !this.setRating(tag, user, subRating);
                }
            }
        }

        return !hasFailed;
    }
                
}
