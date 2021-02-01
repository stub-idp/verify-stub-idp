package uk.gov.ida.rp.testrp.authentication;

import java.security.Principal;

public abstract class User implements Principal {
    private String id;

    public User(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id");
        }
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        User user = (User) o;

        return id != null ? id.equals(user.id) : user.id == null;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
