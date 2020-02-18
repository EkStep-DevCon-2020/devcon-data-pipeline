package org.ekstep.ep.samza.util;

public class UserKey {

    private String profileId;
    private String stallId;
    private String ideaId;

    public UserKey(String profileId, String stallId, String ideaId) {
        this.profileId = profileId;
        this.stallId = stallId;
        this.ideaId = ideaId;
    }

    @Override
    public int hashCode() {
        return this.profileId.hashCode() + this.stallId.hashCode() + this.ideaId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        UserKey thatObj = (UserKey) obj;
        return this.profileId.equalsIgnoreCase(thatObj.profileId) && this.stallId.equalsIgnoreCase(thatObj.stallId)
                && this.ideaId.equalsIgnoreCase(thatObj.ideaId);
    }

    public String getProfileId() {
        return profileId;
    }

    public String getStallId() {
        return stallId;
    }

    public String getIdeaId() {
        return ideaId;
    }
}
