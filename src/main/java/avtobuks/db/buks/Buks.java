package avtobuks.db.buks;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Table(name = "buks")
public class Buks {

    @Id
    @Column(name = "buks_name")
    private String buksName;

    @Column(name = "channel_load_mbps")
    private Integer channelLoadMbps;

    @Column(name = "max_account")
    private Integer maxAccount; // максимально количество аккаунтов которое можно регистрировать за день

    @Column(name = "today")
    private LocalDate today; // сегодняшнее число

    @Column(name = "account_today")
    private Integer accountToday; // аккаунтов за сегодня

    @Column(name = "cashout_min")
    private Integer cashoutMin; // минимальная сумма для вывода

    @Column(name = "cashout_max")
    private Integer cashoutMax; // максимальная сумма для вывода

    @Column(name = "perfect_canvas", columnDefinition = "TEXT")
    private String perfect_canvas; // перфект канвас для этого букса (нужен при подборе фингерпринта)

    @Column(name = "extensions")
    private String extensions; // браузерные расширения для Google Chrome
}
