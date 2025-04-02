package com.uhsadong.ddtube.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = @Index(name = "idx_code", columnList = "code"))
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private Playlist playlist;

    @Column(nullable = false, length = 5, unique = true)
    @Length(min = 5, max = 5)
    private String code; // 5자리 AlphaNumeric

    @Length(min=2, max=20)
    @Column(nullable = false, length = 20)
    private String name;
    @Length(min=2, max=100)
    @Column(nullable = false, length = 100)
    private String password;
    @Column(nullable = false)
    private boolean isAdmin;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;


    public static User toEntity(Playlist playlist, String code, String name, String password, boolean isAdmin) {
        return User.builder()
            .playlist(playlist)
            .code(code)
            .name(name)
            .password(password)
            .isAdmin(isAdmin)
            .build();
    }
}
