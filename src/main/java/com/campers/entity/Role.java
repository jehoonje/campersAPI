// src/main/java/com/campers/entity/Role.java

package com.campers.entity;

import javax.persistence.*;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // 예: "ROLE_USER", "ROLE_ADMIN"

    // Constructors
    public Role() {}

    public Role(String name) {
        this.name = name;
    }

    // Getter와 Setter
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
