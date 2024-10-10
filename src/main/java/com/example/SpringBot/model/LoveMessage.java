package com.example.SpringBot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class LoveMessage {

    @Column(length = 237)
    private String body;

    private String category;

    @Id
    private Integer id;
}
