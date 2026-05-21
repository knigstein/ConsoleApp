package model;

import java.io.Serializable;
import java.sql.Timestamp;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String login;
    private String passwordHash;
    private Timestamp createdAt;

    public User(Integer id, String login, String passwordHash, Timestamp createdAt) {
        this.id = id;
        this.login = login;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    
}