package avtobuks.db.profile;

import avtobuks.db.account.Account;
import avtobuks.db.gmail.Gmail;
import avtobuks.db.payeer.Payeer;
import avtobuks.db.phone.Phone;
import avtobuks.db.proxy.Proxy;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "profile")
public class Profile {

    @Id
    @Column(name = "nickname", nullable = false, unique = true)
    private String nickname;

    @Column(name = "server")
    private Long server;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "patronymic", nullable = false)
    private String patronymic;

    @Column(name = "gender", nullable = false)
    private String gender;

    @Column(name = "vk_user_url", nullable = false)
    private String vkUserUrl;

    @Column(name = "account_photo", nullable = false)
    private String accountPhoto;

    @ManyToOne
    @JoinColumn(name = "gmail", referencedColumnName = "gmail_address")
    private Gmail gmail;

    @ManyToOne
    @JoinColumn(name = "proxy", referencedColumnName = "proxy_address")
    private Proxy proxy;

    @ManyToOne
    @JoinColumn(name = "payeer", referencedColumnName = "id")
    private Payeer payeer;

    @OneToOne
    @JoinColumn(name = "phone", referencedColumnName = "phone_number")
    private Phone phone;

    @Column(name = "is_blocked")
    private Boolean isBlocked;

    @Column(name = "load")
    private Integer load;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Profile profile)) return false;
        return nickname.equals(profile.nickname) && birthDate.equals(profile.birthDate) && name.equals(profile.name) && surname.equals(profile.surname) && patronymic.equals(profile.patronymic) && gender.equals(profile.gender) && vkUserUrl.equals(profile.vkUserUrl) && accountPhoto.equals(profile.accountPhoto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nickname, birthDate, name, surname, patronymic, gender, vkUserUrl, accountPhoto);
    }
}
