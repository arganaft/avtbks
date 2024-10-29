package avtobuks.db.fingerprint;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Table(name = "fingerprint")
public class Fingerprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // ID

    @Column(name = "fingerprint", columnDefinition = "TEXT")
    private String fingerprint;
}



