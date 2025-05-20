package com.group06.music_app;

import com.group06.music_app.song.Song;
import com.group06.music_app.song.SongRepository;
import com.group06.music_app.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableAsync
public class MusicAppServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MusicAppServerApplication.class, args);
	}

	@Bean
	CommandLineRunner initSong(SongRepository songRepository, UserRepository userRepository) {
		return args -> {
			if(songRepository.count() == 0) {
				Song song = Song.builder()
						.name("Em của ngày hôm qua")
						.authorName("Sơn Tùng MTP")
						.singerName("Sơn Tùng MTP")
						.audioUrl("https://res.cloudinary.com/dee2s8sgk/video/upload/v1745932928/audios/Em_C%E1%BB%A7a_Ng%C3%A0y_H%C3%B4m_Qua_Lyrics_Video_t3xjgi.mp3")
						.coverImageUrl("https://img.tripi.vn/cdn-cgi/image/width=700,height=700/https://gcs.tripi.vn/public-tripi/tripi-feed/img/482786dLt/anh-mo-ta.png")
						.lyrics("/dee2s8sgk/raw/upload/v1745935865/audios/lyric_em_cua_ngay_home_qua_m0vjvx.json")
						.isPublic(true)
						.isDeleted(false)
						.fileName("em_cua_ngay_hom_qua")
						.fileExtension("mp3")
						.user(userRepository.findById(1L).get())
						.build();
				songRepository.save(song);
			}
		};
	}

}
