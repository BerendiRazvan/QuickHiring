package domain;

import lombok.*;
import enums.AccountType;
import utils.LocalDateTimeConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "USER", schema = "public")
@Access(AccessType.FIELD)
public class User implements Serializable {

    public User(@NonNull Long id, @NonNull String mail, @NonNull String password, @NonNull String firstName, @NonNull String lastName, @NonNull LocalDateTime birthDate, @NonNull String phoneNumber, @NonNull AccountType accountType) {
        this.id = id;
        this.mail = mail;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.accountType = accountType;
        this.profileImage = new ImageData(); // init with default photo later
    }

    @Id
    @Column(name = "ID", unique = true)
    private Long id;


    @Column(name = "MAIL", unique = true)
    private String mail;

    @NonNull
    @Column(name = "PASSWORD")
    private String password;


    @Column(name = "FIRST_NAME")
    private String firstName;


    @Column(name = "LAST_NAME")
    private String lastName;


    @Column(name = "BIRTH_DATE", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime birthDate;


    @Column(name = "PHONE_NUMBER", unique = true)
    private String phoneNumber;


    @Column(name = "ACCOUNT_TYPE")
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @OneToOne
    @JoinColumn(name = "PROFILE_IMAGE")
    private ImageData profileImage;

}
