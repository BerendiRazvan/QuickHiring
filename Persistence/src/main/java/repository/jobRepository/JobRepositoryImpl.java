package repository.jobRepository;

import lombok.NonNull;
import domain.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.exception.RepositoryException;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JobRepositoryImpl implements JobRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobRepositoryImpl.class);
    private final EntityManagerFactory entityManagerFactory;

    public JobRepositoryImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void add(@NonNull Job job) throws RepositoryException {
        LOGGER.info("Add element - started");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = null;
        try {
            entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();
            entityManager.persist(job);
            entityTransaction.commit();
            LOGGER.info("Add element - transaction committed");
        } catch (Exception ex) {
            if (entityTransaction != null) {
                entityTransaction.rollback();
                LOGGER.warn("Add element - rolled back");
            }
            LOGGER.warn("Add element - exception occurred -> {}", ex.getMessage());
            throw new RepositoryException("This element already exists!");
        } finally {
            entityManager.close();
        }
        LOGGER.info("Add element - finished");
    }

    @Override
    public void update(@NonNull Long id, @NonNull Job job) throws RepositoryException {
        LOGGER.info("Update element with id = {} - started", id);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = null;
        Job jobToUpdate;
        try {
            entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();
            jobToUpdate = entityManager.find(Job.class, id);
            setUpJob(jobToUpdate, job);
            entityManager.merge(jobToUpdate);
            entityTransaction.commit();
            LOGGER.info("Update element with id = {} - transaction committed", id);
        } catch (Exception ex) {
            if (entityTransaction != null) {
                entityTransaction.rollback();
                LOGGER.warn("Update element with id = {} - rolled back", id);
            }
            LOGGER.warn("Update element with id = {} to - exception occurred -> {}", id,
                    ex.getMessage());
            throw new RepositoryException("This element does not exist!");
        } finally {
            entityManager.close();
        }
        LOGGER.info("Update element with id = {} - finished", id);
    }

    @Override
    public void delete(@NonNull Long id) throws RepositoryException {
        LOGGER.info("Delete element with id = {} - started", id);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = null;
        Job jobToDelete;
        try {
            entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();
            jobToDelete = entityManager.find(Job.class, id);
            entityManager.remove(jobToDelete);
            entityTransaction.commit();
            LOGGER.info("Delete element with id = {} - transaction committed", id);
        } catch (Exception ex) {
            if (entityTransaction != null) {
                entityTransaction.rollback();
                LOGGER.warn("Delete element with id = {} - rolled back", id);
            }
            LOGGER.warn("Delete element with id = {} - exception occurred -> {}", id, ex.getMessage());
            throw new RepositoryException("This element does not exist!");
        } finally {
            entityManager.close();
        }
        LOGGER.info("Delete element with id = {} - finished", id);
    }

    @Override
    public List<Job> findAll() {
        LOGGER.info("FindAll elements - started");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        List<Job> jobList = new ArrayList<>();
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Job> criteriaQuery = criteriaBuilder.createQuery(Job.class);
            Root<Job> rootEntry = criteriaQuery.from(Job.class);
            CriteriaQuery<Job> all = criteriaQuery.select(rootEntry);
            TypedQuery<Job> allQuery = entityManager.createQuery(all);
            jobList = allQuery.getResultList();
            LOGGER.info("FindAll elements - finished");
        } catch (NoResultException ex) {
            LOGGER.info("FindAll elements - no result");
        } finally {
            entityManager.close();
        }
        return jobList;
    }

    @Override
    public Optional<Job> findById(@NonNull Long id) {
        LOGGER.info("FindById element with id = {} - started", id);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Job jobToFind = null;
        try {
            jobToFind = entityManager.find(Job.class, id);
            LOGGER.info("FindById element with id = {} - finished", id);
        } catch (NoResultException ex) {
            LOGGER.info("FindById elements - no result");
        } finally {
            entityManager.close();
        }
        return Optional.ofNullable(jobToFind);
    }

    private void setUpJob(Job jobToUpdate, Job job) {
        jobToUpdate.setTitle(job.getTitle());
        jobToUpdate.setDescription(job.getDescription());
        jobToUpdate.setPostingDate(job.getPostingDate());
        jobToUpdate.setJobType(job.getJobType());
        jobToUpdate.setExperienceLevel(job.getExperienceLevel());
        jobToUpdate.setEmploymentType(job.getEmploymentType());
        jobToUpdate.setCompany(job.getCompany());
        jobToUpdate.setLocation(job.getLocation());
        jobToUpdate.setJobAvailability(job.getJobAvailability());
    }
}
