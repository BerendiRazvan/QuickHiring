package repository.applicationForJobRepository;

import lombok.NonNull;
import domain.ApplicationForJob;
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

public class ApplicationForJobRepositoryImpl implements ApplicationForJobRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationForJobRepositoryImpl.class);
    private final EntityManagerFactory entityManagerFactory;

    public ApplicationForJobRepositoryImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void add(@NonNull ApplicationForJob application) throws RepositoryException {
        LOGGER.info("Add element - started");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = null;
        try {
            entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();
            entityManager.persist(application);
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
    public void update(@NonNull Long id, @NonNull ApplicationForJob application) throws RepositoryException {
        LOGGER.info("Update element with id = {} - started", id);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = null;
        ApplicationForJob applicationToUpdate;
        try {
            entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();
            applicationToUpdate = entityManager.find(ApplicationForJob.class, id);
            setUpApplication(applicationToUpdate, application);
            entityManager.merge(applicationToUpdate);
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
        ApplicationForJob applicationToDelete;
        try {
            entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();
            applicationToDelete = entityManager.find(ApplicationForJob.class, id);
            entityManager.remove(applicationToDelete);
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
    public List<ApplicationForJob> findAll() {
        LOGGER.info("FindAll elements - started");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        List<ApplicationForJob> applicationList = new ArrayList<>();
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<ApplicationForJob> criteriaQuery = criteriaBuilder.createQuery(ApplicationForJob.class);
            Root<ApplicationForJob> rootEntry = criteriaQuery.from(ApplicationForJob.class);
            CriteriaQuery<ApplicationForJob> all = criteriaQuery.select(rootEntry);
            TypedQuery<ApplicationForJob> allQuery = entityManager.createQuery(all);
            applicationList = allQuery.getResultList();
            LOGGER.info("FindAll elements - finished");
        } catch (NoResultException ex) {
            LOGGER.info("FindAll elements - no result");
        } finally {
            entityManager.close();
        }
        return applicationList;
    }

    @Override
    public Optional<ApplicationForJob> findById(@NonNull Long id) {
        LOGGER.info("FindById element with id = {} - started", id);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        ApplicationForJob applicationToFind = null;
        try {
            applicationToFind = entityManager.find(ApplicationForJob.class, id);
            LOGGER.info("FindById element with id = {} - finished", id);
        } catch (NoResultException ex) {
            LOGGER.info("FindById elements - no result");
        } finally {
            entityManager.close();
        }
        return Optional.ofNullable(applicationToFind);
    }

    private void setUpApplication(ApplicationForJob applicationToUpdate, ApplicationForJob application) {
        applicationToUpdate.setApplicationDate(application.getApplicationDate());
        applicationToUpdate.setCandidateResume(application.getCandidateResume());
        applicationToUpdate.setJobApplied(application.getJobApplied());
        applicationToUpdate.setApplicationStatus(application.getApplicationStatus());
        applicationToUpdate.setApplicationDetails(application.getApplicationDetails());
    }
}
