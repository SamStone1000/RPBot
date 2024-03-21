package stone.rpbot.slash.commands.tag;

import stone.rpbot.slash.commands.tag.Tag.Rating;
/**
 * The entity that controls all the tags
 * Can make new tags, delete tags, create new root tags, etc.
 */
public interface TagManager<T extends Tag> {
    public T createTag(String name, String shortDescription, String description, T superTag);
    public boolean addAliases(T tag, String... aliases);
    public boolean removeAliases(T tag, String... aliases);
    public boolean addRating(T tag, long user, Rating rating);
    public boolean updateRating(T tag, long user, Rating rating);
    public boolean removeRating(T tag, long user);

    public default boolean setRating(T tag, long user, Rating rating) {
        if (tag.getRatings().get(user).getType() == UNRATED) {
            return addRating(tag, user, rating);
        } else {
            return updateRating(tag, user, rating);
        }
    }

    public default boolean setRating(T tag, long user, int value, Rating.Type type) {
        return setRating(tag, user, Rating.of(value, type));
    }
    
    public T getTag(String tag);
}
