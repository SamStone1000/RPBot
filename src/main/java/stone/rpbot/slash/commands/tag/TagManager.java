package stone.rpbot.slash.commands.tag;

import stone.rpbot.slash.commands.tag.Tag.Rating;
/**
 * The entity that controls all the tags
 * Can make new tags, delete tags, create new root tags, etc.
 */
public interface TagManager {
    public Tag createTag(String name, String shortDescription, String description, Tag superTag);
    public boolean addAliases(Tag tag, String... aliases);
    public boolean removeAliases(Tag tag, String... aliases);
    public boolean addRating(Tag tag, long user, Rating rating);
    public boolean updateRating(Tag tag, long user, Rating rating);
    public boolean removeRating(Tag tag, long user);

    public default boolean setRating(Tag tag, long user, Rating rating) {
        if (tag.getRatings().get(user).type() == Rating.Type.UNRATED) {
            return addRating(tag, user, rating);
        } else {
            return updateRating(tag, user, rating);
        }
    }

    public default boolean setRating(Tag tag, long user, int value, Rating.Type type) {
        return setRating(tag, user, Rating.of(value, type));
    }
    
    public Tag getTag(String tag);
}
