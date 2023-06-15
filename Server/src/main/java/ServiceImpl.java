
import domain.*;
import enums.ApplicationStatus;
import enums.JobAvailability;
import repository.applicationForJobRepository.ApplicationForJobRepository;
import repository.companyRepository.CompanyRepository;
import repository.exception.RepositoryException;
import repository.imageDataRepository.ImageDataRepository;
import repository.jobRepository.JobRepository;
import repository.locationRepository.LocationRepository;
import repository.resumeRepository.ResumeRepository;
import repository.userRepository.UserRepository;
import resumeAnalyzer.ResumeParser;
import similarityAnalyzer.DocumentSimilarity;
import validators.JobValidator;
import validators.ResumeValidator;
import validators.UserValidator;
import validators.ValidatorException;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ServiceImpl implements IService {

    private UserRepository userRepository;
    private ResumeRepository resumeRepository;
    private LocationRepository locationRepository;
    private JobRepository jobRepository;
    private CompanyRepository companyRepository;
    private ApplicationForJobRepository applicationForJobRepository;
    private ImageDataRepository imageDataRepository;

    private Map<String, IObserver> loggedClients;


    public ServiceImpl(UserRepository userRepository, ResumeRepository resumeRepository, LocationRepository locationRepository, JobRepository jobRepository, CompanyRepository companyRepository, ApplicationForJobRepository applicationForJobRepository, ImageDataRepository imageDataRepository) {
        this.userRepository = userRepository;
        this.resumeRepository = resumeRepository;
        this.locationRepository = locationRepository;
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
        this.applicationForJobRepository = applicationForJobRepository;
        this.imageDataRepository = imageDataRepository;

        loggedClients = new ConcurrentHashMap<>();

        System.out.println("Repository initialized!");
    }

    @Override
    public User login(User user, IObserver client) throws ServiceException {
        Optional<User> userToLogin = userRepository.findByMail(user.getMail());
        if (userToLogin.isPresent()) {
            if (userToLogin.get().getMail().equals(user.getMail())) {
                if (userToLogin.get().getPassword().equals(user.getPassword())) {
                    if (loggedClients.get(user.getMail()) != null) {
                        System.out.println("User already logged in!");
                        throw new ServiceException("User already logged in!");
                    }
                    loggedClients.put(user.getMail(), client);
                    System.out.println(user.getMail() + " - login successful");
                    return userToLogin.get();
                } else {
                    System.out.println("Login failed!");
                    throw new ServiceException("The password is wrong.\nLogin failed!");
                }
            } else {
                System.out.println("Login failed!");
                throw new ServiceException("The email does not exist.\nLogin failed!");
            }
        } else {
            System.out.println("Login failed!");
            throw new ServiceException("The account does not exist.\nLogin failed!");
        }
    }

    @Override
    public void logout(User user) throws ServiceException {
        IObserver localClient = loggedClients.remove(user.getMail());
        if (localClient == null) {
            System.out.println("Logout failed!");
            throw new ServiceException("User " + user.getMail() + " was not logged in!");
        }
        System.out.println(user.getMail() + " - logout successful");
    }

    @Override
    public User createAccount(User user) throws ServiceException {
        UserValidator userValidator = new UserValidator();
        try {
            userValidator.validate(user);
            user.setId(generateUserId());

            setProfileImage(user);

            userRepository.add(user);
            return user;
        } catch (ValidatorException e) {
            throw new ServiceException(e.getMessage());
        } catch (RepositoryException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    private Long generateUserId() {
        Long id = 1L;
        while (userRepository.findById(id).isPresent()) id++;
        return id;
    }

    @Override
    public User modifyAccount(Long id, User user) throws ServiceException {
        UserValidator userValidator = new UserValidator();
        try {
            userValidator.validate(user);
            user.setId(id);

            setProfileImage(user);

            userRepository.update(id, user);
            Optional<User> userUpdated = userRepository.findById(id);

            if (userUpdated.isPresent())
                return userUpdated.get();
            else
                throw new ServiceException("Update error");
        } catch (ValidatorException e) {
            throw new ServiceException(e.getMessage());
        } catch (RepositoryException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    private void setProfileImage(User user) throws RepositoryException {
        System.out.println(user);
        ImageData imageData = user.getProfileImage();
        imageData.setId(user.getId() + 2);
        imageData.setName((user.getFirstName() + "_" + user.getLastName() + "_ProfileImage").trim());
        user.setProfileImage(imageData);
        System.out.println(user.getProfileImage());

        Optional<ImageData> img = imageDataRepository.findById(imageData.getId());
        if (img.isPresent()) {
            if (img.get().getId() != 1L)
                imageDataRepository.update(imageData.getId(), imageData);
        } else {
            imageDataRepository.add(imageData);
        }
    }

    @Override
    public void addJob(Job job) throws ServiceException {
        JobValidator jobValidator = new JobValidator();
        try {
            jobValidator.validate(job);
            job.setId(generateJobId());
            jobRepository.add(job);
        } catch (ValidatorException | RepositoryException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    private Long generateJobId() {
        Long id = 1L;
        while (jobRepository.findById(id).isPresent()) id++;
        return id;
    }

    @Override
    public void modifyJob(Long id, Job job) throws ServiceException {
        JobValidator jobValidator = new JobValidator();
        try {
            jobValidator.validate(job);
            jobRepository.update(id, job);
        } catch (ValidatorException | RepositoryException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public void applyForJob(Job job, User user) throws ServiceException {
        Optional<Resume> resume = resumeRepository.findAll()
                .stream()
                .filter(resumeF -> resumeF.getOwner().getId().equals(user.getId()))
                .findFirst();

        if (resume.isPresent()) {
            ApplicationForJob applicationForJob = new ApplicationForJob();
            applicationForJob.setId(generateApplicationId());
            applicationForJob.setCandidateResume(resume.get());
            applicationForJob.setJobApplied(job);
            applicationForJob.setApplicationDate(LocalDateTime.now());
            applicationForJob.setApplicationStatus(ApplicationStatus.APPLIED);
            applicationForJob.setApplicationDetails("");

            LocalDateTime dateTime = applicationForJob.getApplicationDate();
            String newDetailsInfo = dateTime.getDayOfMonth() + "-" + dateTime.getMonth().getValue() + "-" + dateTime.getYear() +
                    " - Status: " + applicationForJob.getApplicationStatus() + "\n\n" +
                    applicationForJob.getApplicationDetails() + "\n\n";

            applicationForJob.setApplicationDetails(newDetailsInfo);

            try {
                applicationForJobRepository.add(applicationForJob);
            } catch (RepositoryException e) {
                throw new ServiceException(e.getMessage());
            }
        } else throw new ServiceException("You do not have a resume");
    }

    private Long generateApplicationId() {
        Long id = 1L;
        while (applicationForJobRepository.findById(id).isPresent()) id++;
        return id;
    }

    @Override
    public void modifyApplicationStatus(ApplicationForJob application, ApplicationStatus status, String details) throws ServiceException {
        application.setApplicationStatus(status);
        application.setApplicationDetails(addApplicationDetails(application, details));

        try {
            applicationForJobRepository.update(application.getId(), application);
        } catch (RepositoryException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public List<ApplicationForJob> findAllApplicationsForUser(User user, ApplicationStatus applicationStatus) {
        return applicationForJobRepository.findAll()
                .stream()
                .sorted((a1, a2) -> a2.getApplicationDate().compareTo(a1.getApplicationDate()))
                .filter(application -> application.getCandidateResume().getOwner().getId().equals(user.getId()) &&
                        application.getApplicationStatus() == applicationStatus)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationForJob> findAllApplicationsForJob(Job job, ApplicationStatus status) {
        return applicationForJobRepository.findAll()
                .stream()
                .sorted((a, b) -> Double.compare(
                        getResumeJobMatchingScore(b.getCandidateResume(), b.getJobApplied()),
                        getResumeJobMatchingScore(a.getCandidateResume(), a.getJobApplied()))
                )
                .filter(application -> application.getJobApplied().getId().equals(job.getId()) &&
                        application.getApplicationStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public Resume analyzeResume(File resumeFile) {
        ResumeParser resumeParser = new ResumeParser();
        return resumeParser.extractData(resumeFile);
    }

    @Override
    public void addResume(Resume resume) throws ServiceException {
        ResumeValidator resumeValidator = new ResumeValidator();
        try {
            resumeValidator.validate(resume);
            resume.setId(resume.getOwner().getId());
            if (resumeRepository.findById(resume.getId()).isPresent())
                resumeRepository.delete(resume.getId());
            resumeRepository.add(resume);
        } catch (ValidatorException e) {
            throw new ServiceException(e.getMessage());
        } catch (RepositoryException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public List<Job> findAllJobs() {
        return jobRepository.findAll();
    }

    @Override
    public List<Job> findAllJobsAvailable() {
        return jobRepository.findAll()
                .stream()
                .filter(job -> job.getJobAvailability() == JobAvailability.OPEN_FOR_APPLICATIONS)
                .collect(Collectors.toList());
    }

    @Override
    public double getResumeJobMatchingScore(Resume userResume, Job job) {
        String resumeData;
        String jobData;

        resumeData = userResume.getProfileExtractedData() + "\n" +
                userResume.getEducationExtractedData() + "\n" +
                userResume.getExperienceExtractedData() + "\n" +
                userResume.getEducationExtractedData();

        jobData = job.getTitle() + "\n" +
                job.getDescription() + "\n" +
                job.getJobType() + " " + job.getExperienceLevel() + " " + job.getEmploymentType() + " " +
                job.getLocation().toString();

        DocumentSimilarity similarityCalculator = new DocumentSimilarity();

        return similarityCalculator.calculateMatchingScore(resumeData.toLowerCase(), jobData.toLowerCase());
    }

    @Override
    public List<Job> findBestJobs(int numberOfJobs, Resume resume) {
        return jobRepository.findAll()
                .stream()
                .sorted((a, b) -> Double.compare(
                        getResumeJobMatchingScore(resume, b),
                        getResumeJobMatchingScore(resume, a))
                )
                .filter(job -> job.getJobAvailability() == JobAvailability.OPEN_FOR_APPLICATIONS)
                .limit(numberOfJobs)
                .collect(Collectors.toList());
    }

    @Override
    public List<Job> searchJob(String info) {
        String[] strings = info.split(" ");
        return jobRepository.findAll()
                .stream()
                .filter(job -> {
                            boolean ok = true;
                            if (job.getJobAvailability() == JobAvailability.OPEN_FOR_APPLICATIONS) {
                                for (String s : strings) {
                                    if (!job.toString().toLowerCase().contains(s.toLowerCase()))
                                        ok = false;
                                }
                            } else ok = false;
                            return ok;
                        }
                )
                .collect(Collectors.toList());
    }

    @Override
    public boolean appliedForThis(Long userId, Long jobId) {
        Optional<ApplicationForJob> userApplication = applicationForJobRepository.findAll().stream()
                .filter(application -> application.getJobApplied().getId().equals(jobId)
                        && application.getCandidateResume().getOwner().getId().equals(userId))
                .findFirst();
        return userApplication.isPresent();
    }

    @Override
    public Long getApplicantsNumber(Long jobId) {
        return applicationForJobRepository.findAll()
                .stream()
                .filter(application -> application.getJobApplied().getId().equals(jobId))
                .count();
    }

    @Override
    public List<Job> findAllJobsPosted(User recruiter) {
        return jobRepository.findAll()
                .stream()
                .filter(job -> (job.getJobAvailability() == JobAvailability.OPEN_FOR_APPLICATIONS ||
                        job.getJobAvailability() == JobAvailability.CLOSED_FOR_APPLICATIONS) &&
                        job.getRecruiter().getId().equals(recruiter.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Job> searchJobPosted(String info, User recruiter) {
        String[] strings = info.split(" ");
        return jobRepository.findAll()
                .stream()
                .filter(job -> {
                            boolean ok = true;
                            if ((job.getJobAvailability() == JobAvailability.OPEN_FOR_APPLICATIONS ||
                                    job.getJobAvailability() == JobAvailability.CLOSED_FOR_APPLICATIONS) &&
                                    job.getRecruiter().getId().equals(recruiter.getId())) {
                                for (String s : strings) {
                                    if (!job.toString().toLowerCase().contains(s.toLowerCase()))
                                        ok = false;
                                }
                            } else ok = false;
                            return ok;
                        }
                )
                .collect(Collectors.toList());
    }

    @Override
    public void removeJob(Job job) throws ServiceException {
        job.setJobAvailability(JobAvailability.CLOSED);
        try {
            jobRepository.update(job.getId(), job);
            updateApplicationsToRejected(job);
        } catch (RepositoryException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    private void updateApplicationsToRejected(Job job) {
        List<ApplicationForJob> allApplicationsForThisJob = applicationForJobRepository.findAll()
                .stream()
                .filter(application -> application.getJobApplied().getId().equals(job.getId()))
                .peek(application -> {
                    if (application.getApplicationStatus() != ApplicationStatus.ACCEPTED)
                        application.setApplicationStatus(ApplicationStatus.REJECTED);
                })
                .collect(Collectors.toList());

        allApplicationsForThisJob.forEach(application -> {
            try {
                applicationForJobRepository.update(application.getId(), application);
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        });

    }

    @Override
    public void changeJobStatus(Job job) throws ServiceException {
        if (job.getJobAvailability() == JobAvailability.OPEN_FOR_APPLICATIONS)
            job.setJobAvailability(JobAvailability.CLOSED_FOR_APPLICATIONS);
        else if (job.getJobAvailability() == JobAvailability.CLOSED_FOR_APPLICATIONS)
            job.setJobAvailability(JobAvailability.OPEN_FOR_APPLICATIONS);
        try {
            jobRepository.update(job.getId(), job);
        } catch (RepositoryException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    @Override
    public void moveApplicationBackward(ApplicationForJob application, String details) throws ServiceException {
        ApplicationStatus status;

        switch (application.getApplicationStatus()) {
            case IN_REVIEW:
                status = ApplicationStatus.APPLIED;
                break;
            case INTERVIEW:
                status = ApplicationStatus.IN_REVIEW;
                break;
            default:
                throw new ServiceException("You can not move application backward");
        }

        application.setApplicationStatus(status);
        application.setApplicationDetails(addApplicationDetails(application, details));

        try {
            applicationForJobRepository.update(application.getId(), application);
        } catch (RepositoryException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public void moveApplicationForward(ApplicationForJob application, String details) throws ServiceException {
        ApplicationStatus status;

        switch (application.getApplicationStatus()) {
            case APPLIED:
                status = ApplicationStatus.IN_REVIEW;
                break;
            case IN_REVIEW:
                status = ApplicationStatus.INTERVIEW;
                break;
            case INTERVIEW:
                status = ApplicationStatus.ACCEPTED;
                break;
            default:
                throw new ServiceException("You can not move application backward");
        }

        application.setApplicationStatus(status);
        application.setApplicationDetails(addApplicationDetails(application, details));

        try {
            applicationForJobRepository.update(application.getId(), application);
        } catch (RepositoryException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public ApplicationForJob getApplication(Long id) throws ServiceException {
        Optional<ApplicationForJob> application = applicationForJobRepository.findById(id);
        if (application.isPresent())
            return application.get();
        else
            throw new ServiceException("This application do not exist");
    }

    @Override
    public Resume getUserResume(Long id) throws ServiceException {
        Optional<Resume> resume = resumeRepository.findAll()
                .stream()
                .filter(r -> r.getOwner().getId().equals(id))
                .findFirst();
        if (resume.isPresent())
            return resume.get();
        else
            throw new ServiceException("This user does not have a resume");
    }

    private String addApplicationDetails(ApplicationForJob application, String details) {
        LocalDateTime dateTime = LocalDateTime.now();
        return dateTime.getDayOfMonth() + "-" + dateTime.getMonth().toString() + "-" + dateTime.getYear() +
                " - Status: " + application.getApplicationStatus() + "\n" +
                details + "\n\n" +
                application.getApplicationDetails() + "\n\n";
    }

    @Override
    public ImageData getProfileImage(Long imageId) {
        Optional<ImageData> imageData = imageDataRepository.findById(imageId);
        return imageData.orElseGet(() -> imageDataRepository.findAll().get(0));
    }

    @Override
    public void saveImage(ImageData imageDataForUser) {
        try {
            imageDataRepository.add(imageDataForUser);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }
}
