package avtobuks.db.phone;

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
@Table(name = "phone")
public class Phone {

    @Id
    @Column(name = "phone_number", nullable = false, unique = true)
    private Long phoneNumber;

    @Column(name = "phone_id", unique = true)
    private Long id;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    public Phone(Long phoneNumber, LocalDate purchaseDate) {
        this.phoneNumber = phoneNumber;
        this.purchaseDate = purchaseDate;
    }
}
