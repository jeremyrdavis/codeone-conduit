package io.vertx.codeone.conduit.models;

import io.vertx.core.json.JsonObject;

/**
    Add to maven to enable Json.decodeValue(jsonString, POJO.class);
    <!-- https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-annotations -->
    <dependency>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-core</artifactId>
      <version>2.11.0.rc1</version>
    </dependency>

    <!-- https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind -->
    <dependency>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-databind</artifactId>
      <version>2.11.0.rc1</version>
    </dependency>
*/

public class User {

    String username;

    String email;

    String bio;

    String password;

    String image;

    String token;

    public JsonObject toConduitJson() {

        JsonObject user = new JsonObject().put("email", this.email).put("token", this.token)
                .put("username", this.username).put("bio", this.bio).put("image", this.image);

        return new JsonObject().put("user", user);
    }

    public User() {
        super();
    }

    public User(final String username, final String email, final String bio, final String password, final String image, final String token) {
        super();
        
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.password = password;
        this.image = image;
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
