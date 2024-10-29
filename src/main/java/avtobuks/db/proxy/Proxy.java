package avtobuks.db.proxy;

import avtobuks.db.profile.Profile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Table(name = "proxy")
public class Proxy {

    @Id
    @Column(name = "proxy_address", nullable = false, unique = true)
    private String proxyAddress;

    @Column(name = "IpId", nullable = false)
    private String IpId;

    @Column(name = "socks5_port", nullable = false)
    private String socks5Port;

    @Column(name = "login", nullable = false)
    private String login;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "date_start", nullable = false)
    private LocalDate dateStart	;

    @Column(name = "date_end", nullable = false)
    private LocalDate dateEnd;

}
