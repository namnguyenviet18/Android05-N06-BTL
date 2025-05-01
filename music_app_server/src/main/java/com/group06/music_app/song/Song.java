package com.group06.music_app.song;

import com.group06.music_app.commom.BaseEntity;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Song extends BaseEntity {

    ///  Các entity khác extend BaseEntity tương tự như song
}
