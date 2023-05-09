package domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "Resume")
@Table(name = "RESUME", schema = "public")
@Access(AccessType.FIELD)
public class Resume implements Serializable {


    @Id
    @Column(name = "ID", unique = true)
    private Long id;

    @Lob
    @Column(name = "profile_Extracted_Data")
    private String profileExtractedData;

    @Lob
    @Column(name = "education_Extracted_Data")
    private String educationExtractedData;

    @Lob
    @Column(name = "experience_Extracted_Data")
    private String experienceExtractedData;

    @Lob
    @Column(name = "skills_Extracted_Data")
    private String skillsExtractedData;

    @OneToOne
    @JoinColumn(name = "OWNER")
    private User owner;

}
