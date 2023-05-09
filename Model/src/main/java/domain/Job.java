package domain;

import lombok.*;
import enums.EmploymentType;
import enums.ExperienceLevel;
import enums.JobAvailability;
import enums.JobType;
import utils.LocalDateTimeConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "Job")
@Table(name = "JOB", schema = "public")
@Access(AccessType.FIELD)
public class Job implements Serializable {

    public Job(@NonNull Long id, @NonNull String title, @NonNull String description, @NonNull JobType jobType, @NonNull ExperienceLevel experienceLevel, @NonNull EmploymentType employmentType, @NonNull Company company, @NonNull Location location, @NonNull JobAvailability jobAvailability) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.postingDate = LocalDateTime.now();
        this.jobType = jobType;
        this.experienceLevel = experienceLevel;
        this.employmentType = employmentType;
        this.company = company;
        this.location = location;
        this.jobAvailability = jobAvailability;
    }

    @Id
    @Column(name = "ID", unique = true)
    private Long id;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "POSTING_DATE", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime postingDate;

    @Column(name = "JOB_TYPE")
    @Enumerated(EnumType.STRING)
    private JobType jobType;

    @Column(name = "EXPERIENCE_LEVEL")
    @Enumerated(EnumType.STRING)
    private ExperienceLevel experienceLevel;

    @Column(name = "EMPLOYMENT_TYPE")
    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;

    @OneToOne
    @JoinColumn(name = "COMPANY")
    private Company company;

    @OneToOne
    @JoinColumn(name = "LOCATION")
    private Location location;

    @Column(name = "JOB_AVAILABILITY")
    @Enumerated(EnumType.STRING)
    private JobAvailability jobAvailability;

    @OneToOne
    @JoinColumn(name = "RECRUITER")
    private User recruiter;

    @Override
    public String toString() {
        return title + " - " + company + " - " + location;
    }
}
