import domain.*;
import enums.ApplicationStatus;
import enums.JobAvailability;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import repository.applicationForJobRepository.ApplicationForJobRepository;
import repository.companyRepository.CompanyRepository;
import repository.exception.RepositoryException;
import repository.imageDataRepository.ImageDataRepository;
import repository.jobRepository.JobRepository;
import repository.locationRepository.LocationRepository;
import repository.resumeRepository.ResumeRepository;
import repository.userRepository.UserRepository;
import resumeAnalyzer.ResumeParser;
import validators.UserValidator;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServiceImplTest {

    private IService service;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private ApplicationForJobRepository applicationForJobRepository;

    @Mock
    private ImageDataRepository imageDataRepository;

    @Mock
    private UserValidator userValidator;

    @Mock
    private IObserver client;

    @Mock
    private ResumeParser resumeParser;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ServiceImpl(userRepository, resumeRepository, locationRepository, jobRepository,
                companyRepository, applicationForJobRepository, imageDataRepository);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @DisplayName("Login test - Successful login")
    public void testLogin_SuccessfulLogin_ReturnsUser() throws ServiceException {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setMail("test@example.com");
        user.setPassword("password");
        when(userRepository.findByMail(user.getMail())).thenReturn(Optional.of(user));

        // Act
        User result = service.login(user, client);

        // Assert
        verify(userRepository).findByMail(user.getMail());

        assertNotNull(result);
        assertEquals(user, result);

        try {
            Field loggedClients = ServiceImpl.class.getDeclaredField("loggedClients");
            loggedClients.setAccessible(true);

            // Assert
            assertEquals(((Map<String, IObserver>) loggedClients.get(service)).size(), 1);
            assertEquals(((Map<String, IObserver>) loggedClients.get(service)).get(user.getMail()), client);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Login test - Incorrect password")
    public void testLogin_IncorrectPassword_ThrowsServiceException() {
        // Arrange
        User userToLogin = new User();
        userToLogin.setId(1L);
        userToLogin.setMail("test@example.com");
        userToLogin.setPassword("incorrect_password");

        User user = new User();
        user.setId(1L);
        user.setMail("test@example.com");
        user.setPassword("password");

        when(userRepository.findByMail(user.getMail())).thenReturn(Optional.of(user));

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> service.login(userToLogin, client));
        assertEquals("The password is wrong.\nLogin failed!", exception.getMessage());
    }

    @Test
    @DisplayName("Login test - Nonexistent mail")
    public void testLogin_NonexistentEmail_ThrowsServiceException() {
        // Arrange
        User userToLogin = new User();
        userToLogin.setId(1L);
        userToLogin.setMail("nonexistent@example.com");
        userToLogin.setPassword("incorrect_password");

        User user = new User();
        user.setId(1L);
        user.setMail("test@example.com");
        user.setPassword("password");

        when(userRepository.findByMail(userToLogin.getMail())).thenReturn(Optional.of(user));

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> service.login(userToLogin, client));
        assertEquals("The email does not exist.\nLogin failed!", exception.getMessage());
    }

    @Test
    @DisplayName("Login test - Nonexistent account")
    public void testLogin_NonexistentAccount_ThrowsServiceException() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setMail("nonexistent@example.com");
        user.setPassword("incorrect_password");
        when(userRepository.findByMail(user.getMail())).thenReturn(Optional.empty());

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> service.login(user, client));
        assertEquals("The account does not exist.\nLogin failed!", exception.getMessage());
    }

    @Test
    @DisplayName("Login test - User already logged in")
    public void testLogin_UserAlreadyLoggedIn_ThrowsServiceException() throws ServiceException {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setMail("test@example.com");
        user.setPassword("incorrect_password");
        when(userRepository.findByMail(user.getMail())).thenReturn(Optional.of(user));
        service.login(user, client); // Login the user once

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> service.login(user, client));
        assertEquals("User already logged in!", exception.getMessage());
    }

    @Test
    @DisplayName("Logout test - Successful logout")
    public void testLogout_UserLoggedIn_SuccessfulLogout() throws ServiceException {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setMail("test@example.com");
        user.setPassword("password");

        when(userRepository.findByMail(user.getMail())).thenReturn(Optional.of(user));
        service.login(user, client); // Log in the user

        verify(userRepository).findByMail(user.getMail());

        // Act
        service.logout(user);

        // Assert
        try {
            Field loggedClients = ServiceImpl.class.getDeclaredField("loggedClients");
            loggedClients.setAccessible(true);

            assertTrue(((Map<String, IObserver>) loggedClients.get(service)).isEmpty());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Logout test - User not logged in")
    public void testLogout_UserNotLoggedIn_ThrowsServiceException() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setMail("test@example.com");
        user.setPassword("password");

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> service.logout(user));
        assertEquals("User " + user.getMail() + " was not logged in!", exception.getMessage());
    }

    @Test
    @DisplayName("Create account test - Valid user")
    public void testCreateAccount_ValidUser_ReturnsCreatedUser() throws ServiceException {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setMail("test@example.com");
        user.setPassword("Password1234");
        user.setFirstName("testFN");
        user.setLastName("testLN");
        user.setPhoneNumber("0751578787");
        user.setBirthDate(LocalDateTime.now().minusYears(20));

        ImageData imageData = new ImageData();
        imageData.setId(user.getId() + 2);
        user.setProfileImage(imageData);

        when(imageDataRepository.findById(imageData.getId())).thenReturn(Optional.of(imageData));

        // Act
        User result = service.createAccount(user);

        // Assert
        assertNotNull(result);
        assertEquals(user, result);

        when(imageDataRepository.findById(imageData.getId())).thenReturn(Optional.of(imageData));
    }

    @Test
    @DisplayName("Create account test - Invalid user")
    public void testCreateAccount_InvalidUser_ThrowsServiceException() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setMail("test@example.com");
        user.setPassword("invalidPassword");
        user.setFirstName("testFN");
        user.setLastName("testLN");
        user.setPhoneNumber("invalidPN");
        user.setBirthDate(LocalDateTime.now().minusYears(20));

        ImageData imageData = new ImageData();
        user.setProfileImage(imageData);

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> service.createAccount(user));
        assertNotNull(exception.getMessage());
        try {
            verify(userRepository, never()).add(user);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Create account test - User already exist")
    public void testCreateAccount_RepositoryException_ThrowsServiceException() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setMail("test@example.com");
        user.setPassword("Password1234");
        user.setFirstName("testFN");
        user.setLastName("testLN");
        user.setPhoneNumber("0751578787");
        user.setBirthDate(LocalDateTime.now().minusYears(20));

        ImageData imageData = new ImageData();
        user.setProfileImage(imageData);

        try {
            doThrow(new RepositoryException("Repository exception")).when(userRepository).add(user);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> service.createAccount(user));
        assertNotNull(exception.getMessage());

        try {
            verify(userRepository, times(1)).add(user);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Modify account test - Valid user")
    public void testModifyAccount_ValidUser_ReturnsModifiedUser() throws ServiceException {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setMail("test@example.com");
        user.setPassword("Password1234");
        user.setFirstName("testFN");
        user.setLastName("testLN");
        user.setPhoneNumber("0751578787");
        user.setBirthDate(LocalDateTime.now().minusYears(20));

        ImageData imageData = new ImageData();
        user.setProfileImage(imageData);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // Act
        User result = service.modifyAccount(user.getId(), user);

        // Assert
        assertNotNull(result);
        assertEquals(user, result);

        try {
            verify(userRepository, times(1)).update(user.getId(), user);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Modify account test - Invalid user")
    public void testModifyAccount_InvalidUser_ThrowsServiceException() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setMail("test@example.com");
        user.setPassword("invalidPassword");
        user.setFirstName("testFN");
        user.setLastName("testLN");
        user.setPhoneNumber("invalidPN");
        user.setBirthDate(LocalDateTime.now().minusYears(20));

        ImageData imageData = new ImageData();
        user.setProfileImage(imageData);

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> service.modifyAccount(user.getId(), user));
        assertNotNull(exception.getMessage());

        try {
            verify(userRepository, never()).update(user.getId(), user);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Modify account test - User not found")
    public void testModifyAccount_UserNotFound_ThrowsServiceException() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setMail("test@example.com");
        user.setPassword("Password1234");
        user.setFirstName("testFN");
        user.setLastName("testLN");
        user.setPhoneNumber("0751578787");
        user.setBirthDate(LocalDateTime.now().minusYears(20));

        ImageData imageData = new ImageData();
        user.setProfileImage(imageData);

        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> service.modifyAccount(user.getId(), user));
        assertNotNull(exception.getMessage());

        try {
            verify(userRepository, times(1)).update(user.getId(), user);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Modify account test - Repository exception")
    public void testModifyAccount_RepositoryException_ThrowsServiceException() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setMail("test@example.com");
        user.setPassword("Password1234");
        user.setFirstName("testFN");
        user.setLastName("testLN");
        user.setPhoneNumber("0751578787");
        user.setBirthDate(LocalDateTime.now().minusYears(20));

        ImageData imageData = new ImageData();
        user.setProfileImage(imageData);

        try {
            doThrow(new RepositoryException("Repository exception")).when(userRepository).update(user.getId(), user);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> service.modifyAccount(user.getId(), user));
        assertNotNull(exception.getMessage());

        try {
            verify(userRepository, times(1)).update(user.getId(), user);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }


    @Test
    @DisplayName("Add job test - Valid job")
    public void testAddJob_ValidJob_SuccessfulAddition() throws ServiceException, RepositoryException {
        // Arrange
        Job job = new Job();
        job.setTitle("test");
        job.setDescription("test");
        job.setCompany(new Company());
        job.setRecruiter(new User());
        job.setLocation(new Location());

        // Act
        service.addJob(job);

        // Assert
        verify(jobRepository, times(1)).add(job);
    }

    @Test
    @DisplayName("Add job test - Invalid job")
    public void testAddJob_InvalidJob_ThrowsServiceException() {
        // Arrange
        Job job = new Job();
        job.setTitle("test");
        job.setDescription("test");

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> service.addJob(job));
        assertNotNull(exception.getMessage());

        try {
            verify(jobRepository, never()).add(job);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Add job test - Repository exception")
    public void testAddJob_RepositoryException_ThrowsServiceException() throws RepositoryException {
        // Arrange
        Job job = new Job();
        job.setTitle("test");
        job.setDescription("test");
        job.setCompany(new Company());
        job.setRecruiter(new User());
        job.setLocation(new Location());

        doThrow(new RepositoryException("Repository exception")).when(jobRepository).add(job);

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> service.addJob(job));
        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("Modify job test - Valid job")
    public void testModifyJob_ValidJob_SuccessfulModification() throws ServiceException, RepositoryException {
        // Arrange
        Job job = new Job();
        job.setTitle("test");
        job.setDescription("test");
        job.setCompany(new Company());
        job.setRecruiter(new User());
        job.setLocation(new Location());

        // Act
        service.modifyJob(job.getId(), job);

        // Assert
        verify(jobRepository, times(1)).update(job.getId(), job);
    }

    @Test
    @DisplayName("Modify job test - Invalid job")
    public void testModifyJob_InvalidJob_ThrowsServiceException() {
        // Arrange
        Job job = new Job();
        job.setTitle("test");
        job.setDescription("test");


        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> service.modifyJob(job.getId(), job));
        assertNotNull(exception.getMessage());
        try {
            verify(jobRepository, never()).update(job.getId(), job);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Modify job test - Repository exception")
    public void testModifyJob_RepositoryException_ThrowsServiceException() throws RepositoryException {
        // Arrange
        Job job = new Job();
        job.setTitle("test");
        job.setDescription("test");
        job.setCompany(new Company());
        job.setRecruiter(new User());
        job.setLocation(new Location());

        doThrow(new RepositoryException("Repository exception")).when(jobRepository).update(job.getId(), job);


        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> service.modifyJob(job.getId(), job));
        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("Apply for job test - Successful application")
    public void testApplyForJob_ResumeExists_SuccessfulApplication() throws ServiceException, RepositoryException {
        // Arrange
        User user = new User();
        user.setId(1L);

        Job job = new Job();
        job.setId(1L);

        Resume resume = new Resume();
        resume.setId(1L);
        resume.setOwner(user);

        List<Resume> resumeList = new ArrayList<>();
        resumeList.add(resume);

        when(resumeRepository.findAll()).thenReturn(resumeList);

        // Act
        service.applyForJob(job, user);

        // Assert
        verify(applicationForJobRepository, times(1)).add(any(ApplicationForJob.class));
    }

    @Test
    @DisplayName("Apply for job test - Repository exception")
    public void testApplyForJob_RepositoryException_ThrowsServiceException() throws ServiceException, RepositoryException {
        // Arrange
        User user = new User();
        user.setId(1L);

        Job job = new Job();
        job.setId(1L);

        Resume resume = new Resume();
        resume.setId(1L);
        resume.setOwner(user);

        List<Resume> resumeList = new ArrayList<>();
        resumeList.add(resume);

        when(resumeRepository.findAll()).thenReturn(resumeList);

        // Act
        service.applyForJob(job, user);

        // Assert
        verify(applicationForJobRepository, times(1)).add(any(ApplicationForJob.class));
    }

    @Test
    @DisplayName("Apply for job test - Resume not exists")
    public void testApplyForJob_ResumeNotExists_ThrowsServiceException() throws RepositoryException {
        // Arrange
        User user = new User();
        user.setId(1L);

        Job job = new Job();
        job.setId(1L);

        Resume resume = new Resume();
        resume.setId(1L);
        resume.setOwner(user);

        List<Resume> resumeList = new ArrayList<>();
        resumeList.add(resume);

        when(resumeRepository.findAll()).thenReturn(resumeList);
        doThrow(new RepositoryException("Repository exception")).when(applicationForJobRepository).add(any(ApplicationForJob.class));

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> service.applyForJob(job, user));
        assertNotNull(exception.getMessage());

        // Assert
        verify(applicationForJobRepository, times(1)).add(any(ApplicationForJob.class));
    }

    @Test
    @DisplayName("Modify application status test - Successful modification")
    public void testModifyApplicationStatus_ValidApplication_SuccessfulModification() throws ServiceException, RepositoryException {
        // Arrange
        ApplicationForJob application = new ApplicationForJob();

        ApplicationStatus status = ApplicationStatus.ACCEPTED;
        String details = "Application accepted.";

        // Act
        service.modifyApplicationStatus(application, status, details);

        // Assert
        assertEquals(status, application.getApplicationStatus());
        assertTrue(application.getApplicationDetails().contains(details));
        verify(applicationForJobRepository, times(1)).update(application.getId(), application);
    }

    @Test
    @DisplayName("Modify application status test - Repository exception")
    public void testModifyApplicationStatus_RepositoryException_ThrowsServiceException() throws RepositoryException {
        // Arrange
        ApplicationForJob application = new ApplicationForJob();
        ApplicationStatus status = ApplicationStatus.REJECTED;
        String details = "Application rejected.";

        doThrow(new RepositoryException("Repository exception")).when(applicationForJobRepository).update(application.getId(), application);

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> service.modifyApplicationStatus(application, status, details));
        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("Find all applications for user test - Returns filtered applications")
    public void testFindAllApplicationsForUser_ReturnsFilteredApplications() {
        // Arrange
        User user = new User();
        user.setId(1L);

        ApplicationStatus applicationStatus = ApplicationStatus.ACCEPTED;

        List<ApplicationForJob> applications = new ArrayList<>();

        Resume resume = new Resume();
        resume.setId(1L);
        resume.setOwner(user);

        // Create a few test applications
        for (int i = 1; i <= 5; i++) {
            ApplicationForJob application = new ApplicationForJob();
            application.setCandidateResume(resume);
            application.setApplicationStatus(applicationStatus);
            application.setApplicationDate(LocalDateTime.now().minusDays(i));
            applications.add(application);
        }

        when(applicationForJobRepository.findAll()).thenReturn(applications);

        // Act
        List<ApplicationForJob> result = service.findAllApplicationsForUser(user, applicationStatus);

        // Assert
        List<ApplicationForJob> expected = applications.stream()
                .filter(application -> application.getCandidateResume().getOwner().getId().equals(user.getId()) &&
                        application.getApplicationStatus() == applicationStatus)
                .sorted((a1, a2) -> a2.getApplicationDate().compareTo(a1.getApplicationDate()))
                .collect(Collectors.toList());

        assertEquals(expected, result);
        verify(applicationForJobRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Find all applications for job test - Returns filtered applications")
    public void testFindAllApplicationsForJob_ReturnsFilteredApplications() {
        // Arrange
        Job job = new Job();
        job.setId(1L);
        job.setDescription("test description");
        job.setLocation(new Location());

        User user = new User();
        user.setId(1L);

        Resume resume = new Resume();
        resume.setId(1L);
        resume.setOwner(user);
        resume.setEducationExtractedData("test ed");
        resume.setExperienceExtractedData("test ex");
        resume.setProfileExtractedData("test pd");
        resume.setSkillsExtractedData("test sk");

        ApplicationStatus status = ApplicationStatus.ACCEPTED;

        List<ApplicationForJob> applications = new ArrayList<>();

        // Create a few test applications
        for (int i = 1; i <= 5; i++) {
            ApplicationForJob application = new ApplicationForJob();
            application.setJobApplied(job);
            application.setApplicationStatus(status);
            application.setCandidateResume(resume);
            applications.add(application);
        }

        when(applicationForJobRepository.findAll()).thenReturn(applications);

        // Act
        List<ApplicationForJob> result = service.findAllApplicationsForJob(job, status);

        // Assert
        List<ApplicationForJob> expected = applications.stream()
                .sorted((a, b) -> Double.compare(
                        service.getResumeJobMatchingScore(b.getCandidateResume(), b.getJobApplied()),
                        service.getResumeJobMatchingScore(a.getCandidateResume(), a.getJobApplied()))
                )
                .filter(application -> application.getJobApplied().getId().equals(job.getId()) &&
                        application.getApplicationStatus() == status)
                .collect(Collectors.toList());

        assertEquals(expected, result);
        verify(applicationForJobRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Analyze resume test - Returns parsed resume")
    public void testAnalyzeResume_ReturnsParsedResume() throws ServiceException, URISyntaxException {
        // Arrange

        Path path = FileSystems.getDefault().getPath("src/test/testData/myResume.pdf").toAbsolutePath();
        System.out.println(path);
        File resumeFile = path.toFile();

        // Act
        Resume result = service.analyzeResume(resumeFile);

        // Assert
        assertFalse(result.getEducationExtractedData().isEmpty());
        assertFalse(result.getExperienceExtractedData().isEmpty());
        assertFalse(result.getProfileExtractedData().isEmpty());
        assertFalse(result.getSkillsExtractedData().isEmpty());
    }

    @Test
    @DisplayName("Add resume test - Valid resume")
    public void testAddResume_ValidResume_SuccessfulAddition() throws ServiceException, RepositoryException {
        // Arrange
        User user = new User();
        user.setId(1L);

        Resume resume = new Resume();
        resume.setId(1L);
        resume.setOwner(user);
        resume.setSkillsExtractedData("test s");
        resume.setProfileExtractedData("test p");
        resume.setExperienceExtractedData("test e");
        resume.setEducationExtractedData("test e");

        // Act
        service.addResume(resume);

        // Assert
        verify(resumeRepository, times(1)).add(resume);
    }

    @Test
    @DisplayName("Add resume test - Invalid resume")
    public void testAddResume_InvalidResume_ThrowsServiceException() {
        // Arrange
        User user = new User();
        user.setId(1L);

        Resume resume = new Resume();
        resume.setId(1L);
        resume.setOwner(user);
        resume.setSkillsExtractedData("");
        resume.setProfileExtractedData("test p");
        resume.setExperienceExtractedData("test e");
        resume.setEducationExtractedData("test e");

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> service.addResume(resume));
        assertNotNull(exception.getMessage());
        try {
            verify(resumeRepository, never()).add(resume);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Add resume test - Existing resume")
    public void testAddResume_ExistingResume_DeletesAndAddsResume() throws ServiceException, RepositoryException {
        // Arrange
        User user = new User();
        user.setId(1L);

        Resume resume = new Resume();
        resume.setId(1L);
        resume.setOwner(user);
        resume.setSkillsExtractedData("test s");
        resume.setProfileExtractedData("test p");
        resume.setExperienceExtractedData("test e");
        resume.setEducationExtractedData("test e");

        when(resumeRepository.findById(1L)).thenReturn(Optional.of(resume));

        // Act
        service.addResume(resume);

        // Assert
        verify(resumeRepository, times(1)).delete(1L);
        verify(resumeRepository, times(1)).add(resume);
    }

    @Test
    @DisplayName("Add resume test - Repository exception")
    public void testAddResume_RepositoryException_ThrowsServiceException() throws RepositoryException {
        // Arrange
        User user = new User();
        user.setId(1L);

        Resume resume = new Resume();
        resume.setId(1L);
        resume.setOwner(user);
        resume.setSkillsExtractedData("test s");
        resume.setProfileExtractedData("test p");
        resume.setExperienceExtractedData("test e");
        resume.setEducationExtractedData("test e");

        doThrow(new RepositoryException("Repository exception")).when(resumeRepository).add(any(Resume.class));

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> service.addResume(resume));
        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("Find all jobs test - Returns all jobs")
    public void testFindAllJobs_ReturnsAllJobs() {
        // Arrange
        List<Job> expectedJobs = new ArrayList<>();
        expectedJobs.add(new Job());
        expectedJobs.add(new Job());
        expectedJobs.add(new Job());

        when(jobRepository.findAll()).thenReturn(expectedJobs);

        // Act
        List<Job> result = service.findAllJobs();

        // Assert
        assertEquals(expectedJobs, result);
        verify(jobRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Find all jobs available test - Returns available jobs")
    public void testFindAllJobsAvailable_ReturnsAvailableJobs() {
        // Arrange
        List<Job> allJobs = new ArrayList<>();
        allJobs.add(new Job());
        allJobs.add(new Job());
        allJobs.add(new Job());

        List<Job> expectedJobs = allJobs.stream()
                .filter(job -> job.getJobAvailability() == JobAvailability.OPEN_FOR_APPLICATIONS)
                .collect(Collectors.toList());

        when(jobRepository.findAll()).thenReturn(allJobs);

        // Act
        List<Job> result = service.findAllJobsAvailable();

        // Assert
        assertEquals(expectedJobs, result);
        verify(jobRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Find best jobs test - Returns best matching jobs")
    public void testFindBestJobs_ReturnsBestMatchingJobs() {
        // Arrange
        int numberOfJobs = 3;
        Resume resume = new Resume();

        Job job = new Job();
        job.setTitle("test");
        job.setDescription("test descr");
        job.setLocation(new Location());
        job.setCompany(new Company());
        job.setRecruiter(new User());

        List<Job> allJobs = new ArrayList<>();
        allJobs.add(job);
        allJobs.add(job);
        allJobs.add(job);
        allJobs.add(job);
        allJobs.add(job);

        List<Job> expectedJobs = allJobs.stream()
                .filter(j -> j.getJobAvailability() == JobAvailability.OPEN_FOR_APPLICATIONS)
                .sorted((a, b) -> Double.compare(
                        service.getResumeJobMatchingScore(resume, b),
                        service.getResumeJobMatchingScore(resume, a))
                )
                .limit(numberOfJobs)
                .collect(Collectors.toList());

        when(jobRepository.findAll()).thenReturn(allJobs);

        // Act
        List<Job> result = service.findBestJobs(numberOfJobs, resume);

        // Assert
        assertEquals(expectedJobs, result);
        verify(jobRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Search job test - Returns matching jobs")
    public void testSearchJob_ReturnsMatchingJobs() {
        // Arrange
        String info = "software developer";

        Job j1 = new Job();
        j1.setTitle("Software Developer");
        j1.setJobAvailability(JobAvailability.OPEN_FOR_APPLICATIONS);
        Job j2 = new Job();
        j2.setTitle("Data Analyst");
        j2.setJobAvailability(JobAvailability.OPEN_FOR_APPLICATIONS);
        Job j3 = new Job();
        j3.setTitle("Project Manager");
        Job j4 = new Job();
        j4.setTitle("Software Engineer");

        List<Job> allJobs = new ArrayList<>();
        allJobs.add(j1);
        allJobs.add(j2);
        allJobs.add(j3);
        allJobs.add(j4);

        List<Job> expectedJobs = allJobs.stream()
                .filter(job -> {
                    boolean ok = true;
                    if (job.getJobAvailability() == JobAvailability.OPEN_FOR_APPLICATIONS) {
                        for (String s : info.split(" ")) {
                            if (!job.toString().toLowerCase().contains(s.toLowerCase())) {
                                ok = false;
                                break;
                            }
                        }
                    } else {
                        ok = false;
                    }
                    return ok;
                })
                .collect(Collectors.toList());

        when(jobRepository.findAll()).thenReturn(allJobs);


        // Act
        List<Job> result = service.searchJob(info);

        // Assert
        assertEquals(expectedJobs, result);
        verify(jobRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Applied for this test - User has applied")
    public void testAppliedForThis_UserHasApplied_ReturnsTrue() {
        // Arrange
        User user = new User();
        user.setId(1L);

        Resume resume = new Resume();
        resume.setId(1L);
        resume.setOwner(user);

        Job job = new Job();
        job.setId(1L);

        ApplicationForJob application = new ApplicationForJob();
        application.setId(1L);
        application.setJobApplied(job);
        application.setCandidateResume(resume);

        List<ApplicationForJob> applications = new ArrayList<>();
        applications.add(application);

        when(applicationForJobRepository.findAll()).thenReturn(applications);

        // Act
        boolean result = service.appliedForThis(user.getId(), job.getId());

        // Assert
        assertTrue(result);
        verify(applicationForJobRepository, times(1)).findAll();
    }


    @Test
    @DisplayName("Get applicants number test - Returns correct number")
    public void testGetApplicantsNumber_ReturnsCorrectNumber() {
        // Arrange
        User user = new User();
        user.setId(1L);

        Resume resume = new Resume();
        resume.setId(1L);
        resume.setOwner(user);

        Job job = new Job();
        job.setId(1L);

        ApplicationForJob application = new ApplicationForJob();
        application.setId(1L);
        application.setJobApplied(job);
        application.setCandidateResume(resume);

        List<ApplicationForJob> applications = new ArrayList<>();
        applications.add(application);

        when(applicationForJobRepository.findAll()).thenReturn(applications);

        // Act
        Long result = service.getApplicantsNumber(1L);

        // Assert
        assertEquals(1L, result);
        verify(applicationForJobRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Find all jobs posted test - Returns posted jobs")
    public void testFindAllJobsPosted_ReturnsPostedJobs() {
        // Arrange
        User recruiter = new User();
        recruiter.setId(1L);

        Job job1 = new Job();
        job1.setRecruiter(recruiter);
        job1.setJobAvailability(JobAvailability.OPEN_FOR_APPLICATIONS);

        Job job2 = new Job();
        job2.setRecruiter(recruiter);
        job2.setJobAvailability(JobAvailability.CLOSED_FOR_APPLICATIONS);

        List<Job> allJobs = new ArrayList<>();
        allJobs.add(job1);
        allJobs.add(job2);

        List<Job> expectedJobs = allJobs.stream()
                .filter(job -> (job.getJobAvailability() == JobAvailability.OPEN_FOR_APPLICATIONS ||
                        job.getJobAvailability() == JobAvailability.CLOSED_FOR_APPLICATIONS) &&
                        job.getRecruiter().getId().equals(recruiter.getId()))
                .collect(Collectors.toList());

        when(jobRepository.findAll()).thenReturn(allJobs);

        // Act
        List<Job> result = service.findAllJobsPosted(recruiter);

        // Assert
        assertEquals(expectedJobs, result);
        verify(jobRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Search job posted test - Returns matching jobs posted by recruiter")
    public void testSearchJobPosted_ReturnsMatchingJobsPostedByRecruiter() {
        // Arrange
        String info = "software developer";
        User recruiter = new User();
        recruiter.setId(1L);

        Job j1 = new Job();
        j1.setTitle("Software Developer");
        j1.setJobAvailability(JobAvailability.OPEN_FOR_APPLICATIONS);
        j1.setRecruiter(recruiter);
        Job j2 = new Job();
        j2.setTitle("Data Analyst");
        j2.setJobAvailability(JobAvailability.CLOSED_FOR_APPLICATIONS);
        j2.setRecruiter(recruiter);
        Job j3 = new Job();
        j3.setTitle("Project Manager");
        j3.setRecruiter(recruiter);
        Job j4 = new Job();
        j4.setTitle("Software Engineer");
        j4.setRecruiter(recruiter);

        List<Job> allJobs = new ArrayList<>();
        allJobs.add(j1);
        allJobs.add(j2);
        allJobs.add(j3);
        allJobs.add(j4);

        List<Job> expectedJobs = allJobs.stream()
                .filter(job -> (job.getJobAvailability() == JobAvailability.OPEN_FOR_APPLICATIONS ||
                        job.getJobAvailability() == JobAvailability.CLOSED_FOR_APPLICATIONS) &&
                        job.getRecruiter().getId().equals(recruiter.getId()))
                .filter(job -> {
                    boolean ok = true;
                    for (String s : info.split(" ")) {
                        if (!job.toString().toLowerCase().contains(s.toLowerCase())) {
                            ok = false;
                            break;
                        }
                    }
                    return ok;
                })
                .collect(Collectors.toList());

        when(jobRepository.findAll()).thenReturn(allJobs);

        // Act
        List<Job> result = service.searchJobPosted(info, recruiter);

        // Assert
        assertEquals(expectedJobs, result);
        verify(jobRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Remove job test - Closes job and update applications")
    public void testRemoveJob_ClosesJobAndUpdateApplications() throws ServiceException {
        // Arrange
        Job job = new Job();
        job.setId(1L);

        User user = new User();
        user.setId(1L);

        Resume resume = new Resume();
        resume.setId(1L);
        resume.setOwner(user);

        ApplicationForJob application = new ApplicationForJob();
        application.setId(1L);
        application.setJobApplied(job);
        application.setCandidateResume(resume);
        application.setApplicationStatus(ApplicationStatus.APPLIED);

        List<ApplicationForJob> applications = new ArrayList<>();
        applications.add(application);

        when(applicationForJobRepository.findAll()).thenReturn(applications);

        // Act
        service.removeJob(job);

        // Assert
        try {
            verify(jobRepository, times(1)).update(job.getId(), job);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Remove job test - Throws exception")
    public void testRemoveJob_ThrowsServiceException_WhenRepositoryExceptionOccurs() throws RepositoryException {
        // Arrange
        Job job = new Job(/* job details */);
        job.setId(1L);

        String errorMessage = "Repository error";
        doThrow(new RepositoryException(errorMessage)).when(jobRepository).update(job.getId(), job);

        // Act + Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> service.removeJob(job));
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Change job status test - Changes job status to closed")
    public void testChangeJobStatus_ChangesJobStatusToClosed_WhenStatusIsOpen() throws ServiceException {
        // Arrange
        Job job = new Job();
        job.setId(1L);
        job.setJobAvailability(JobAvailability.OPEN_FOR_APPLICATIONS);

        // Act
        service.changeJobStatus(job);

        // Assert
        assertEquals(JobAvailability.CLOSED_FOR_APPLICATIONS, job.getJobAvailability());
        try {
            verify(jobRepository, times(1)).update(job.getId(), job);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Change job status test - Changes job status to open")
    public void testChangeJobStatus_ChangesJobStatusToOpen_WhenStatusIsClosed() throws ServiceException {
        // Arrange
        Job job = new Job();
        job.setId(1L);
        job.setJobAvailability(JobAvailability.CLOSED_FOR_APPLICATIONS);

        // Act
        service.changeJobStatus(job);

        // Assert
        assertEquals(JobAvailability.OPEN_FOR_APPLICATIONS, job.getJobAvailability());
        try {
            verify(jobRepository, times(1)).update(job.getId(), job);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Change job status test - Throws exception")
    public void testChangeJobStatus_ThrowsServiceException_WhenRepositoryExceptionOccurs() throws RepositoryException {
        // Arrange
        Job job = new Job();
        job.setId(1L);
        job.setJobAvailability(JobAvailability.OPEN_FOR_APPLICATIONS);

        String errorMessage = "Repository error";
        doThrow(new RepositoryException(errorMessage)).when(jobRepository).update(job.getId(), job);

        // Act + Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> service.changeJobStatus(job));
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Get all companies test - Returns all companies")
    public void testGetAllCompanies_ReturnsAllCompanies() {
        // Arrange
        List<Company> allCompanies = new ArrayList<>();
        allCompanies.add(new Company());
        allCompanies.add(new Company());
        allCompanies.add(new Company());

        when(companyRepository.findAll()).thenReturn(allCompanies);

        // Act
        List<Company> result = service.getAllCompanies();

        // Assert
        assertEquals(allCompanies, result);
        verify(companyRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Move application backward test - Changes status to applied")
    public void testMoveApplicationBackward_ChangesStatusToApplied_WhenStatusIsInReview() throws ServiceException {
        // Arrange
        ApplicationForJob application = new ApplicationForJob();
        application.setId(1L);
        application.setApplicationStatus(ApplicationStatus.IN_REVIEW);

        String details = "Additional details";

        // Act
        service.moveApplicationBackward(application, details);

        // Assert
        assertEquals(ApplicationStatus.APPLIED, application.getApplicationStatus());
        try {
            verify(applicationForJobRepository, times(1)).update(application.getId(), application);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Move application backward test - Changes status to in review")
    public void testMoveApplicationBackward_ChangesStatusToInReview_WhenStatusIsInterview() throws ServiceException {
        // Arrange
        ApplicationForJob application = new ApplicationForJob();
        application.setId(1L);
        application.setApplicationStatus(ApplicationStatus.INTERVIEW);

        String details = "Additional details";

        // Act
        service.moveApplicationBackward(application, details);

        // Assert
        assertEquals(ApplicationStatus.IN_REVIEW, application.getApplicationStatus());
        try {
            verify(applicationForJobRepository, times(1)).update(application.getId(), application);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Move application backward test - Throws exception")
    public void testMoveApplicationBackward_ThrowsServiceException_WhenStatusIsNotInReviewOrInterview() throws ServiceException {
        // Arrange
        ApplicationForJob application = new ApplicationForJob();
        application.setId(1L);
        application.setApplicationStatus(ApplicationStatus.APPLIED);

        String details = "Additional details";

        // Act + Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> service.moveApplicationBackward(application, details));
        assertEquals("You can not move application backward", exception.getMessage());
        try {
            verify(applicationForJobRepository, never()).update(application.getId(), application);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Move application backward test - Throws exception")
    public void testMoveApplicationBackward_ThrowsServiceException_WhenRepositoryExceptionOccurs() throws RepositoryException {
        // Arrange
        ApplicationForJob application = new ApplicationForJob();
        application.setId(1L);
        application.setApplicationStatus(ApplicationStatus.IN_REVIEW);

        String details = "Additional details";

        String errorMessage = "Repository error";
        doThrow(new RepositoryException(errorMessage)).when(applicationForJobRepository).update(application.getId(), application);

        // Act + Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> service.moveApplicationBackward(application, details));
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Move application forward test - Changes status to in review")
    public void testMoveApplicationForward_ChangesStatusToInReview_WhenStatusIsApplied() throws ServiceException {
        // Arrange
        ApplicationForJob application = new ApplicationForJob();
        application.setId(1L);
        application.setApplicationStatus(ApplicationStatus.APPLIED);

        String details = "Additional details";

        // Act
        service.moveApplicationForward(application, details);

        // Assert
        assertEquals(ApplicationStatus.IN_REVIEW, application.getApplicationStatus());
        try {
            verify(applicationForJobRepository, times(1)).update(application.getId(), application);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Move application forward test - Changes status to interview")
    public void testMoveApplicationForward_ChangesStatusToInterview_WhenStatusIsInReview() throws ServiceException {
        // Arrange
        ApplicationForJob application = new ApplicationForJob();
        application.setId(1L);
        application.setApplicationStatus(ApplicationStatus.IN_REVIEW);

        String details = "Additional details";

        // Act
        service.moveApplicationForward(application, details);

        // Assert
        assertEquals(ApplicationStatus.INTERVIEW, application.getApplicationStatus());
        try {
            verify(applicationForJobRepository, times(1)).update(application.getId(), application);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Move application forward test - Changes status to accepted")
    public void testMoveApplicationForward_ChangesStatusToAccepted_WhenStatusIsInterview() throws ServiceException {
        // Arrange
        ApplicationForJob application = new ApplicationForJob();
        application.setId(1L);
        application.setApplicationStatus(ApplicationStatus.INTERVIEW);

        String details = "Additional details";

        // Act
        service.moveApplicationForward(application, details);

        // Assert
        assertEquals(ApplicationStatus.ACCEPTED, application.getApplicationStatus());
        try {
            verify(applicationForJobRepository, times(1)).update(application.getId(), application);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Move application forward test - Throws exception")
    public void testMoveApplicationForward_ThrowsServiceException_WhenStatusIsNotAppliedInReviewOrInterview() throws ServiceException {
        // Arrange
        ApplicationForJob application = new ApplicationForJob();
        application.setId(1L);
        application.setApplicationStatus(ApplicationStatus.ACCEPTED);

        String details = "Additional details";

        // Act + Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> service.moveApplicationForward(application, details));
        assertEquals("You can not move application backward", exception.getMessage());
        try {
            verify(applicationForJobRepository, never()).update(application.getId(), application);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Move application forward test - Throws exception")
    public void testMoveApplicationForward_ThrowsServiceException_WhenRepositoryExceptionOccurs() throws RepositoryException {
        // Arrange
        ApplicationForJob application = new ApplicationForJob();
        application.setId(1L);
        application.setApplicationStatus(ApplicationStatus.APPLIED);

        String details = "Additional details";

        String errorMessage = "Repository error";
        doThrow(new RepositoryException(errorMessage)).when(applicationForJobRepository).update(application.getId(), application);

        // Act + Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> service.moveApplicationForward(application, details));
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Get application test - Returns application")
    public void testGetApplication_ReturnsApplication_WhenIdExists() throws ServiceException {
        // Arrange
        ApplicationForJob expectedApplication = new ApplicationForJob();
        expectedApplication.setId(1L);

        when(applicationForJobRepository.findById(1L)).thenReturn(Optional.of(expectedApplication));

        // Act
        ApplicationForJob result = service.getApplication(1L);

        // Assert
        assertEquals(expectedApplication, result);
        verify(applicationForJobRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Get application test - Throws exception")
    public void testGetApplication_ThrowsServiceException_WhenIdDoesNotExist() {
        // Arrange
        long nonExistentId = 9999L;

        when(applicationForJobRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act + Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> service.getApplication(nonExistentId));
        assertEquals("This application do not exist", exception.getMessage());
        verify(applicationForJobRepository, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("Get user resume test - Returns resume")
    public void testGetUserResume_ReturnsResume_WhenUserHasResume() throws ServiceException {
        // Arrange
        User user = new User();
        user.setId(1L);

        Resume expectedResume = new Resume();
        expectedResume.setId(user.getId());
        expectedResume.setOwner(user);

        List<Resume> resumes = new ArrayList<>();
        resumes.add(expectedResume);

        when(resumeRepository.findAll()).thenReturn(resumes);

        // Act
        Resume result = service.getUserResume(user.getId());

        // Assert
        assertEquals(expectedResume, result);
        verify(resumeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Get user resume test - Throws exception")
    public void testGetUserResume_ThrowsServiceException_WhenUserDoesNotHaveResume() {
        // Arrange
        long nonExistentUserId = 9999L;
        List<Resume> resumes = new ArrayList<>();

        when(resumeRepository.findAll()).thenReturn(resumes);

        // Act + Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> service.getUserResume(nonExistentUserId));
        assertEquals("This user does not have a resume", exception.getMessage());
        verify(resumeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Get profile image test - Returns image data")
    public void testGetProfileImage_ReturnsImageData_WhenImageIdExists() {
        // Arrange
        long imageId = 1L;
        ImageData expectedImageData = new ImageData();
        expectedImageData.setId(imageId);

        when(imageDataRepository.findById(imageId)).thenReturn(Optional.of(expectedImageData));

        // Act
        ImageData result = service.getProfileImage(imageId);

        // Assert
        assertEquals(expectedImageData, result);
        verify(imageDataRepository, times(1)).findById(imageId);
        verify(imageDataRepository, never()).findAll();
    }

    @Test
    @DisplayName("Get profile image test - Returns first image data")
    public void testGetProfileImage_ReturnsFirstImageData_WhenImageIdDoesNotExist() {
        // Arrange
        List<ImageData> imageDatas = new ArrayList<>();
        ImageData firstImageData = new ImageData();
        firstImageData.setId(1L);
        imageDatas.add(firstImageData);

        when(imageDataRepository.findAll()).thenReturn(imageDatas);

        // Act
        ImageData result = service.getProfileImage(9999L);

        // Assert
        assertEquals(firstImageData, result);
        verify(imageDataRepository, times(1)).findById(9999L);
        verify(imageDataRepository, times(1)).findAll();
    }


    @Test
    @DisplayName("Save image test")
    public void testSaveImage_CallsAddMethodOnImageDataRepository() throws RepositoryException {
        // Arrange
        ImageData imageData = new ImageData(/* image details */);

        // Act
        service.saveImage(imageData);

        // Assert
        verify(imageDataRepository, times(1)).add(imageData);
    }
}