package com.group06.music_app_mobile.models;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class BaseEntity {
    private Long id;
    private String createdDate;
    private String lastModifiedDate;
    private Long createdBy;
    private Long lastModifiedBy;


    public LocalDateTime createdDateFromString() {
        return toLocalDateTime(this.createdDate);
    }

    public LocalDateTime lastModifiedDateFromString() {
        return toLocalDateTime(this.lastModifiedDate);
    }

    private LocalDateTime toLocalDateTime(String dateTimeString) {
        return LocalDateTime.parse(dateTimeString);
    }
}

