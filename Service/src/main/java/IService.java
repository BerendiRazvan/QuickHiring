import domain.*;
import enums.ApplicationStatus;

import java.io.File;
import java.util.List;

public interface IService {

    User login(User user, IObserver client) throws ServiceException;

    void logout(User user) throws ServiceException;

    User createAccount(User user) throws ServiceException;

    User modifyAccount(Long id, User user) throws ServiceException;

    void addJob(Job job) throws ServiceException;

    void modifyJob(Long id, Job job) throws ServiceException;

    void applyForJob(Job job, User user) throws ServiceException;

    void modifyApplicationStatus(ApplicationForJob application, ApplicationStatus status, String details) throws ServiceException;

    List<ApplicationForJob> findAllApplicationsForUser(User user, ApplicationStatus applicationStatus); // order by app date

    List<ApplicationForJob> findAllApplicationsForJob(Job job, ApplicationStatus status); // order by best matching

    Resume analyzeResume(File resumeFile) throws ServiceException;

    void addResume(Resume resume) throws ServiceException;

    List<Job> findAllJobs();

    List<Job> findAllJobsAvailable();

    double getResumeJobMatchingScore(Resume userResume, Job job);

    List<Job> findBestJobs(int numberOfJobs, Resume resume);

    List<Job> searchJob(String info);

    boolean appliedForThis(Long userId, Long jobId);

    Long getApplicantsNumber(Long jobId);

    List<Job> findAllJobsPosted(User recruiter);

    List<Job> searchJobPosted(String info, User recruiter);

    void removeJob(Job job) throws ServiceException;

    void changeJobStatus(Job job) throws ServiceException;

    List<Company> getAllCompanies();

    void moveApplicationBackward(ApplicationForJob application, String details) throws ServiceException;

    void moveApplicationForward(ApplicationForJob application, String details) throws ServiceException;

    ApplicationForJob getApplication(Long id) throws ServiceException;

    Resume getUserResume(Long id) throws ServiceException;

    ImageData getProfileImage(Long imageId);

    void saveImage(ImageData imageDataForUser);

}
