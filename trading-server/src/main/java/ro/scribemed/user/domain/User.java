package ro.scribemed.user.domain;

import jakarta.persistence.*;
import ro.scribemed.shared.persistence.BaseEntity;

@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    protected User() {}

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }


}
