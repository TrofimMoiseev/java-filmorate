//package ru.yandex.practicum.filmorate.storage.dbStorage;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import ru.yandex.practicum.filmorate.dal.film.FilmRepository;
//import ru.yandex.practicum.filmorate.dal.user.UserRepository;
//import ru.yandex.practicum.filmorate.exception.NotFoundException;
//import ru.yandex.practicum.filmorate.model.Film;
//import ru.yandex.practicum.filmorate.model.Mpa;
//import ru.yandex.practicum.filmorate.model.User;
//
//import java.time.LocalDate;
//import java.util.Collection;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//public class FilmDbStorageTest {
//
//    @Autowired
//    private FilmRepository filmDbStorage;
//
//    @Autowired
//    private UserRepository userDbStorage;
//
//    @Test
//    @DisplayName("Создание и получение фильма по ID")
//    void shouldCreateAndFindFilmById() {
//        // Создаем пользователя через UserDbStorage
//        User newUser = new User();
//        newUser.setEmail("user1@example.com");
//        newUser.setLogin("userLogin1");
//        newUser.setName("User Name");
//        newUser.setBirthday(LocalDate.of(1990, 1, 1));
//        User savedUser = userDbStorage.create(newUser);
//
//        Mpa mpa = new Mpa();
//        mpa.setId(1L);
//        // Создаем новый фильм
//        Film newFilm = new Film();
//        newFilm.setName("Inception");
//        newFilm.setDescription("Sci-Fi thriller");
//        newFilm.setReleaseDate(LocalDate.of(2010, 7, 16));
//        newFilm.setDuration(148);
//        newFilm.setMpa(mpa);
//
//        // Сохраняем фильм и находим его по ID
//        Film savedFilm = filmDbStorage.create(newFilm);
//        Film foundFilm = filmDbStorage.findFilmById(savedFilm.getId()).orElseThrow(() -> {
//            throw new NotFoundException("Фильм с id = " + savedFilm.getId() + " не найден");
//        });
//
//        // Проверяем, что данные фильма сохранены корректно
//        assertThat(foundFilm).isNotNull();
//        assertThat(foundFilm.getName()).isEqualTo("Inception");
//    }
//
//    @Test
//    @DisplayName("Обновление фильма")
//    void shouldUpdateFilm() {
//        // Создаем пользователя через UserDbStorage
//        User newUser = new User();
//        newUser.setEmail("user2@example.com");
//        newUser.setLogin("userLogin2");
//        newUser.setName("User Name");
//        newUser.setBirthday(LocalDate.of(1990, 1, 1));
//        User savedUser = userDbStorage.create(newUser);
//
//        // Создаем новый фильм
//        Mpa mpa = new Mpa();
//        mpa.setId(1L);
//        Film film = new Film();
//        film.setName("Old Film");
//        film.setDescription("Old Description");
//        film.setReleaseDate(LocalDate.of(2000, 1, 1));
//        film.setDuration(120);
//        film.setMpa(mpa);
//
//        // Сохраняем фильм
//        Film savedFilm = filmDbStorage.create(film);
//
//        // Обновляем данные фильма
//        savedFilm.setName("Updated Film");
//        savedFilm.setDescription("Updated Description");
//
//        // Обновляем фильм
//        Film updatedFilm = filmDbStorage.update(savedFilm);
//
//        // Проверяем, что данные обновлены
//        assertThat(updatedFilm.getName()).isEqualTo("Updated Film");
//        assertThat(updatedFilm.getDescription()).isEqualTo("Updated Description");
//    }
//
//    @Test
//    @DisplayName("Получение всех фильмов")
//    void shouldFindAllFilms() {
//        // Создаем пользователей через UserDbStorage
//        User user1 = new User();
//        user1.setEmail("user3@example.com");
//        user1.setLogin("user13");
//        user1.setName("User One");
//        user1.setBirthday(LocalDate.of(1991, 1, 1));
//        userDbStorage.create(user1);
//
//        User user2 = new User();
//        user2.setEmail("user4@example.com");
//        user2.setLogin("user24");
//        user2.setName("User Two");
//        user2.setBirthday(LocalDate.of(1992, 2, 2));
//        userDbStorage.create(user2);
//
//        Mpa mpa = new Mpa();
//        mpa.setId(1L);
//        // Создаем и сохраняем фильмы
//        Film film1 = new Film();
//        film1.setName("Film One");
//        film1.setDescription("Description One");
//        film1.setReleaseDate(LocalDate.of(2005, 1, 1));
//        film1.setDuration(100);
//        film1.setMpa(mpa);
//        filmDbStorage.create(film1);
//
//        Film film2 = new Film();
//        film2.setName("Film Two");
//        film2.setDescription("Description Two");
//        film2.setReleaseDate(LocalDate.of(2010, 1, 1));
//        film2.setDuration(120);
//        film2.setMpa(mpa);
//        filmDbStorage.create(film2);
//
//        // Получаем все фильмы и проверяем их количество
//        Collection<Film> films = filmDbStorage.findAll();
//
//        // Проверяем, что количество фильмов не меньше двух
//        assertThat(films).hasSizeGreaterThanOrEqualTo(2);
//    }
//
//    @Test
//    @DisplayName("Проверка существования фильма по ID")
//    void shouldCheckIfFilmExistsById() {
//        // Создаем пользователя через UserDbStorage
//        User newUser = new User();
//        newUser.setEmail("user5@example.com");
//        newUser.setLogin("userLogin5");
//        newUser.setName("User Name");
//        newUser.setBirthday(LocalDate.of(1990, 1, 1));
//        User savedUser = userDbStorage.create(newUser);
//
//        // Создаем новый фильм
//        Mpa mpa = new Mpa();
//        mpa.setId(1L);
//        Film film = new Film();
//        film.setName("Film Exists");
//        film.setDescription("Description");
//        film.setReleaseDate(LocalDate.of(2020, 1, 1));
//        film.setDuration(140);
//        film.setMpa(mpa);
//        Film savedFilm = filmDbStorage.create(film);
//
//        // Проверяем существование фильма по ID
//        assertThat(filmDbStorage.checkId(savedFilm.getId())).isTrue();
//        assertThat(filmDbStorage.checkId(99999L)).isFalse();
//    }
//}
