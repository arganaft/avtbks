package avtobuks.db.botSettings;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Table(name = "bot_settings")
public class BotSettings {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "token")
    private String token; //Ключ доступа к API с сайта plusofon

    @Column(name = "max_phone_price")
    private Integer maxPhonePrice; //максимальная цена для покупки телефонного номера

    @Column(name = "url")
    private String url; //адрес сервера

    @Column(name = "work_start_time")
    private LocalTime workStartTime; //время начала работы

    @Column(name = "creepjs_rating")
    private String creepjsRating; //показатели хорошего фингерпринта creepjs

    @Column(name = "xevil_server")
    private String xevilServer; //сервер XEvil

    @Column(name = "sctg_xevil_key")
    private String sctgXevilKey; //ключ XEvil sctg

    @Column(name = "max_profile_on_server")
    private Integer maxProfileOnServer; //максимум профилей на сервер

    @Column(name = "cookie_set_days")
    private Integer cookieSetDays; //дней для нагула cookies

    @Column(name = "google_sites_count")
    private Integer googleSitesCount; //количество сайтов для куков гугла в день

    @Column(name = "yandex_sites_count")
    private Integer yandexSitesCount; //количество сайтов для куков яндекса в день

    @Column(name = "account_warmup_days")
    private Integer accountWarmupDays; //дней на разогрев аккаунта

    @Column(name = "min_rest_time_minutes")
    private Integer minRestTimeMinutes; //минимальное время для отдыха после работы в минутах

    @Column(name = "max_rest_time_minutes")
    private Integer maxRestTimeMinutes; //максимальное время для отдыха после работы в минутах

    @Column(name = "max_daily_work_mins")
    private Integer maxDailyWorkMins; //максимум минут работы в день

    @Column(name = "max_session_work_mins")
    private Integer maxSessionWorkMins; //максимальная длительность сессий в минутах

    @Column(name = "min_session_work_mins")
    private Integer minSessionWorkMins; //минимальная длительность сессий в минутах

    @Column(name = "min_sessions_per_day")
    private Integer minSessionsPerDay; //минимум сессий в день

    @Column(name = "max_sessions_per_day")
    private Integer maxSessionsPerDay; //максимум сессий в день

}
