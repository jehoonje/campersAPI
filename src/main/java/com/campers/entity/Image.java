// src/main/java/com/yourapp/entity/Image.java
package com.campers.entity;

import javax.persistence.*;

@Entity
@Table(name = "images")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;

    @ManyToOne
    @JoinColumn(name = "campground_id")
    private Campground campground;

    // Getters and setters
}
