package avtobuks.db.gmail;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Table(name = "gmail")
public class Gmail {

    @Id
    @Column(name = "gmail_address", nullable = false, unique = true)
    private String gmailAddress;

    @Column(name = "password", nullable = false)
    private String password;


}
