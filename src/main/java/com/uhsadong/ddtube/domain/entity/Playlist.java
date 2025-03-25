package com.uhsadong.ddtube.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
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
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 5, unique = true)
    @Length(min = 5, max = 5)
    private String code; // 5자리 AlphaNumeric

    @Column(nullable = false, length = 100)
    @Length(min = 1, max = 100)
    private String title;
    @Length(min = 4, max = 4)
    @Column(nullable = false)
    @Pattern(regexp = "\\d{4}", message = "PIN must be a 4-digit number")
    private String pin; // 4자리 숫자

    @Column(nullable = false)
    private LocalDateTime willDeleteAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;


    public static Playlist toEntity(String code, String title, String pin, LocalDateTime willDeleteAt) {
        return Playlist.builder()
            .code(code)
            .title(title)
            .pin(pin)
            .willDeleteAt(willDeleteAt)
            .build();
    }

}
