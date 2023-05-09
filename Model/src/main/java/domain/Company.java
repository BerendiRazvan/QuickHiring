package domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "Company")
@Table(name = "COMPANY", schema = "public")
@Access(AccessType.FIELD)
public class Company implements Serializable {

    @Id
    @Column(name = "ID", unique = true)
    private Long id;

    @Column(name = "NAME")
    private String name;


    @Column(name = "DESCRIPTION")
    private String description;


    @ManyToMany(targetEntity = Location.class, fetch = FetchType.EAGER)
    @JoinTable(name = "COMPANY_LOCATIONS")
    private List<Location> locations;

    @Override
    public String toString() {
        return name;
    }
}
