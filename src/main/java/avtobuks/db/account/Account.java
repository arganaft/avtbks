package avtobuks.db.account;

import avtobuks.db.buks.Buks;
import avtobuks.db.fingerprint.Fingerprint;
import avtobuks.db.profile.Profile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Table(name = "account", indexes = {
        @Index(name = "idx_account_profile", columnList = "profile_nickname")
})
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // ID

    @ManyToOne
    @JoinColumn(name = "profile_nickname", referencedColumnName = "nickname", nullable = false)
    private Profile profile; // ссылка на профиль

    @ManyToOne
    @JoinColumn(name = "buks_name", referencedColumnName = "buks_name", nullable = false)
    private Buks buks; // имя букса

    @Column(name = "credentials", columnDefinition = "TEXT")
    private String credentials; // Логин, пароль, секретный код и тд.

    @OneToOne
    @JoinColumn(name = "fingerprint_id", referencedColumnName = "id")
    private Fingerprint fingerprint;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt; // дата создания

    @Column(name = "blocked_at")
    private LocalDate blockedAt; // дата блокировки

    @Column(name = "browser_profile")
    private String browserProfile; // ссылка на профиль браузера
    // название для папки с профилем создается по конструктору: номер сервера + букс + ник профиля
    // пример: 1_aviso_arganaft

    @Column(name = "browser_farm_days")
    private Integer browserFarmDays; // сколько дней нагуливали cokies

    @Column(name = "account_warmup_days")
    private Integer accountWarmupDays; // сколько дней разогревали аккаунт

    @Column(name = "minutes_worked_today")
    private Integer minutesWorkedToday; // сколько минут проработал аккаунт за сегодня

    @Column(name = "randome_session_today")
    private Integer randomeSessionToday; // случайное количество сессий на сегодня

    @Column(name = "fact_session_today")
    private Integer factSessionToday; // фактическое количество сессий на сегодня

    @Column(name = "active")
    private Boolean active; // задействован ли сейчас аккаунт

    @Column(name = "unlock_time")
    private LocalDateTime unlockTime; // время когда можно взять в работу аккаунт.
    // Это число когда завершили работу с аккаунтом + время для отдыха из таблицы параметры

    @Column(name = "today")
    private LocalDate today; // когда LocalDate.now() не соответствует today
    // обнуляем переменные minutesWorkedToday, randomeSessionToday, factSessionToday



    public Account(Profile profile, Buks buks, LocalDate createdAt) {
        this.profile = profile;
        this.buks = buks;
        this.createdAt = createdAt;
        this.browserFarmDays = 0;
        this.accountWarmupDays = 0;
        this.minutesWorkedToday = 0;
        this.randomeSessionToday = 0;
        this.factSessionToday = 0;
        this.active = false;
        this.unlockTime = LocalDateTime.now();
        this.today = LocalDate.now();
    }
}
