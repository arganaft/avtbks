package avtobuks.db.cookieSites;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Table(name = "cookie_sites")
public class CookieSites {

    @Id
    @Column(name = "url", nullable = false, unique = true)
    private String url;

    @Column(name = "li_rank", nullable = false)
    private Long li_rank;

    @Column(name = "google", nullable = false)
    private Boolean google;

    @Column(name = "yandex", nullable = false)
    private Boolean yandex;

    @Column(name = "use_count")
    private Long useCount;
}
