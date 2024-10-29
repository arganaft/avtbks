package avtobuks.db.payeer;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payeer")
public class Payeer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // ID

    @Column(name = "login", nullable = false, unique = true)
    private String login;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "secret_code", nullable = false)
    private String secretCode;

    @Column(name = "master_key", nullable = false)
    private String masterKey;

    public Payeer(String login, String password, String secretCode, String masterKey) {
        this.login = login;
        this.password = password;
        this.secretCode = secretCode;
        this.masterKey = masterKey;
    }
}
