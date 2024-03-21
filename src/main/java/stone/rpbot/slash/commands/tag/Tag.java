package stone.rpbot.slash.commands.tag;

import java.util.Map;
import java.util.Set;

public interface Tag {

    /**
     * The name of this tag
     * Mainly used for when the tag needs to be displayed and can't have all the
     * aliases on screen at once
     */ 
    public String getName();

    /**
     * All the aliases of this tag
     * Each alias is a string that will map back to this tag when entered. The primary
     * alias is removed from this Set for convenience.
     */
    public Set<String> getAliases();

    /**
     * A short description of the tag
     */

    public String getShortDescription();
    /**
     * The description what this tag is
     */
    public String getDescription();

    /**
     * The supertag that this tag is under
     * Each supertag has a unique name space for subtags (like classes/packages) and each
     * Tag can only have 1 supertag
     */
    public Tag getSuperTag();

    /**
     * The Set of subtags for this tag
     * Tags can have an arbitrary amount of subtags
     */
    
    public Set<Tag> getSubTags();

    /**
     * A map of ratings for this tag
     * Each user is identified with a unique ID (Discord IDs as longs). Ratings are
     * standardized to numbers where higher means better rated, along with a few special
     * ratings for unrated and the like
     */
    public Map<Long, Rating> getRatings();

    /**
     * A unique id to identify each tag
     */
    public long getID();

    public record Rating(int value, Type type) {
        /**
         * An enum representing why this rating exists
         */

        // DO NOT REORDER THESE
        // The order in which these are declared determines what number they are in the database
        public enum Type {
            // the tag, supertag, and subtags were never rated, ie value is meaningless
            UNRATED(false, false),
            // the tag and subtags weren't rated but the supertag was, ie value is the supertag's
            IMPLICIT_SUPER(false, true),
            // the tag  wasn't rated, but the subtags were, ie value is the average of subtags
            IMPLICIT_SUB(true, false),
            // the tag was explcitly rated, ie the value is what the user gave
            EXPLICIT(true, true),

            public static final Type[] VALUES = Type.values();
            
            private final boolean shouldPropagateUp;
            private final boolean shouldPropagateDown;
            
            private Type(boolean shouldPropagateUp, boolean shouldPropagateDown) {
                this.shouldPropagateUp = shouldPropagateUp;
                this.shouldPropagateDown = shouldPropagateDown;
            }

            public boolean shouldPropagateUp() {return this.shouldPropagateUp;}
            public boolean shouldPropagateDown() {return this.shouldPropagateDown;}
            /**
             * Can this type override the other type
             */
            public boolean canOverride(Type other) {
                if (other == UNRATED) // never rated so fill it out
                    return true;
                if (other == EXPLICIT) // is rated so never overwrite
                    return false;
                if (this == EXPLICIT) // this tag is rated and it's the best
                                      // source of true rating, so override
                    return true;
                /*
                 * This tag's subtags were rated but the other tag's supertag
                 * was rated. Override it.  it's like rating a band and a
                 * album's songs, but not the album, so the rating of the songs
                 * should determine the rating of the album
                 */
                if (this == IMPLICIT_SUB && other == IMPLICIT_SUPER)
                    return true;
                
                return false;
            }

            public boolean canBeOverriden(Type other) {
                return other.canOverride(this);
            }

        public static Rating of(int value, short type) {
            return new Rating(value, Type.VALUES[type]);
        }

        public static Rating of(int value, Type type) {
            return new Rating(value, type);
        }

        public Rating with(Type type) {
            return new Rating(this.value, type);
        }

        public boolean shouldPropagateUp() {return this.type.shouldPropagateUp();}
        public boolean shouldPropagateDown() {return this.type.shouldPropagateDown();}

        public boolean canOverride(Rating other) {
            return this.type.canOverride(other.type);
        }

        public boolean canBeOverriden(Rating other) {
            return this.type.canBeOverriden(other.type);
        }
    }
}
