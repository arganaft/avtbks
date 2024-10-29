package avtobuks.db.phone.referralLink;

import avtobuks.db.buks.Buks;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "referral_links")
public class ReferralLink {

    @Id
    @Column(name = "link", nullable = false)
    private String link;

    @Column(name = "referer", nullable = false)
    private String referer;

    @ManyToOne
    @JoinColumn(name = "buks_id", referencedColumnName = "buks_name", nullable = false)
    private Buks buks;

    @Column(name = "account_count")
    private Integer accountCount;

}
