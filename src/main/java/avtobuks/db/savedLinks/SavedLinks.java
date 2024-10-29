package avtobuks.db.savedLinks;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Table(name = "saved_links")
public class SavedLinks {
    @Id
    @Column(name = "link", nullable = false, unique = true)
    private String link;

    @Column(name = "referer", nullable = false)
    private String referer;
}
