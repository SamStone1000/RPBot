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

    public record Rating(int value, Type type) {
        /**
         * An enum representing why this rating exists
         */

        // DO NOT REORDER THESE
        // The order in which these are declared determines what number they are in the database
        public enum Type {
            // the tag, supertag, and subtags were never rated, ie value is meaningless
            UNRATED(false, false),
            // the tag wasn't rated but the supertag was (ignores subtags), ie value is the supertag's
            IMPLICIT_SUPER(false, true),
            // the tag and supertag wasn't rated, but the subtag was, ie value is the average of subtags
            IMPLICIT_SUB(true, false),
            // the tag was explcitly rated, ie the value is what the user gave
            EXPLICIT(true, true);

            public static final Type[] VALUES = Type.values();
            
            private final boolean shouldPropagateUp;
            private final boolean shouldPropagateDown;
            
            private Type(boolean shouldPropagateUp, boolean shouldPropagateDown) {
                this.shouldPropagateUp = shouldPropagateUp;
                this.shouldPropagateDown = shouldPropagateDown;
            }

            public boolean shouldPropagateUp() {return this.shouldPropagateUp;}
            public boolean shouldPropagateDown() {return this.shouldPropagateDown;}
        }

        public static Rating of(int value, short type) {
            return new Rating(value, Type.VALUES[type]);
        }
    }
}
