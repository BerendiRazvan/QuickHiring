package domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "Location")
@Table(name = "LOCATION", schema = "public")
@Access(AccessType.FIELD)
public class Location implements Serializable {

    @Id
    @Column(name = "ID", unique = true)
    private Long id;

    @Column(name = "COUNTRY")
    private String country;

    @Column(name = "CITY")
    private String city;

    @Override
    public String toString() {
        return city + ", " + country;
    }


}
