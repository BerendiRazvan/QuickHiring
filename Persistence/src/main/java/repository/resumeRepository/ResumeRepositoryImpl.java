package repository.resumeRepository;

import lombok.NonNull;
import domain.Resume;
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

public class ResumeRepositoryImpl implements ResumeRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResumeRepositoryImpl.class);
    private final EntityManagerFactory entityManagerFactory;

    public ResumeRepositoryImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void add(@NonNull Resume resume) throws RepositoryException {
        LOGGER.info("Add element - started");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = null;
        try {
            entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();
            entityManager.persist(resume);
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
    public void update(@NonNull Long id, @NonNull Resume resume) throws RepositoryException {
        LOGGER.info("Update element with id = {} - started", id);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = null;
        Resume resumeToUpdate;
        try {
            entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();
            resumeToUpdate = entityManager.find(Resume.class, id);
            setUpResume(resumeToUpdate, resume);
            entityManager.merge(resumeToUpdate);
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
        Resume resumeToDelete;
        try {
            entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();
            resumeToDelete = entityManager.find(Resume.class, id);
            entityManager.remove(resumeToDelete);
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
    public List<Resume> findAll() {
        LOGGER.info("FindAll elements - started");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        List<Resume> resumeList = new ArrayList<>();
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Resume> criteriaQuery = criteriaBuilder.createQuery(Resume.class);
            Root<Resume> rootEntry = criteriaQuery.from(Resume.class);
            CriteriaQuery<Resume> all = criteriaQuery.select(rootEntry);
            TypedQuery<Resume> allQuery = entityManager.createQuery(all);
            resumeList = allQuery.getResultList();
            LOGGER.info("FindAll elements - finished");
        } catch (NoResultException ex) {
            LOGGER.info("FindAll elements - no result");
        } finally {
            entityManager.close();
        }
        return resumeList;
    }

    @Override
    public Optional<Resume> findById(@NonNull Long id) {
        LOGGER.info("FindById element with id = {} - started", id);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Resume resumeToFind = null;
        try {
            resumeToFind = entityManager.find(Resume.class, id);
            LOGGER.info("FindById element with id = {} - finished", id);
        } catch (NoResultException ex) {
            LOGGER.info("FindById elements - no result");
        } finally {
            entityManager.close();
        }
        return Optional.ofNullable(resumeToFind);
    }

    private void setUpResume(Resume resumeToUpdate, Resume resume) {
        resumeToUpdate.setSkillsExtractedData(resume.getSkillsExtractedData());
        resumeToUpdate.setExperienceExtractedData(resume.getExperienceExtractedData());
        resumeToUpdate.setProfileExtractedData(resume.getProfileExtractedData());
        resumeToUpdate.setEducationExtractedData(resume.getEducationExtractedData());
        resumeToUpdate.setOwner(resume.getOwner());
    }
}
