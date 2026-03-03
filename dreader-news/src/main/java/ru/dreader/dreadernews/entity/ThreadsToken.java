package ru.dreader.dreadernews.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "threads_token")
@Getter
@Setter
public class ThreadsToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String accessToken;

    @OneToOne(mappedBy = "threadsToken", cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @Column(nullable = false)
    private Instant expiresAt;

}
