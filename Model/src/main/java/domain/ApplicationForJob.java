package domain;

import lombok.*;
import enums.ApplicationStatus;
import utils.LocalDateTimeConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "ApplicationForJob")
@Table(name = "APPLICATION_FOR_JOB", schema = "public")
@Access(AccessType.FIELD)
public class ApplicationForJob implements Serializable {
    public ApplicationForJob(@NonNull Long id, @NonNull Resume candidateResume, @NonNull Job jobApplied) {
        this.id = id;
        this.candidateResume = candidateResume;
        this.jobApplied = jobApplied;
        this.applicationDate = LocalDateTime.now();
        this.applicationStatus = ApplicationStatus.APPLIED;
        this.applicationDetails = "";
    }

    @Id
    @Column(name = "ID", unique = true)
    private Long id;


    @OneToOne
    @JoinColumn(name = "CANDIDATE_RESUME")
    private Resume candidateResume;


    @OneToOne
    @JoinColumn(name = "JOB_APPLIED")
    private Job jobApplied;


    @Column(name = "APPLICATION_DATE", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime applicationDate;


    @Column(name = "APPLICATION_STATUS")
    @Enumerated(EnumType.STRING)
    private ApplicationStatus applicationStatus;

    @Lob
    @Column(name = "APPLICATION_DETAILS")
    private String applicationDetails;

    @Override
    public String toString() {
        return jobApplied.toString();
    }
}
