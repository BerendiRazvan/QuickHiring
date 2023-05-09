package repository.companyRepository;

import lombok.NonNull;
import domain.Company;
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

public class CompanyRepositoryImpl implements CompanyRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyRepositoryImpl.class);
    private final EntityManagerFactory entityManagerFactory;

    public CompanyRepositoryImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }


    @Override
    public void add(@NonNull Company company) throws RepositoryException {
        LOGGER.info("Add element - started");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = null;
        try {
            entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();
            entityManager.persist(company);
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
    public void update(@NonNull Long id, @NonNull Company company) throws RepositoryException {
        LOGGER.info("Update element with id = {} - started", id);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = null;
        Company companyToUpdate;
        try {
            entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();
            companyToUpdate = entityManager.find(Company.class, id);
            setUpCompany(companyToUpdate, company);
            entityManager.merge(companyToUpdate);
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
        Company companyToDelete;
        try {
            entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();
            companyToDelete = entityManager.find(Company.class, id);
            entityManager.remove(companyToDelete);
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
    public List<Company> findAll() {
        LOGGER.info("FindAll elements - started");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        List<Company> companyList = new ArrayList<>();
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Company> criteriaQuery = criteriaBuilder.createQuery(Company.class);
            Root<Company> rootEntry = criteriaQuery.from(Company.class);
            CriteriaQuery<Company> all = criteriaQuery.select(rootEntry);
            TypedQuery<Company> allQuery = entityManager.createQuery(all);
            companyList = allQuery.getResultList();
            LOGGER.info("FindAll elements - finished");
        } catch (NoResultException ex) {
            LOGGER.info("FindAll elements - no result");
        } finally {
            entityManager.close();
        }
        return companyList;
    }

    @Override
    public Optional<Company> findById(@NonNull Long id) {
        LOGGER.info("FindById element with id = {} - started", id);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Company companyToFind = null;
        try {
            companyToFind = entityManager.find(Company.class, id);
            LOGGER.info("FindById element with id = {} - finished", id);
        } catch (NoResultException ex) {
            LOGGER.info("FindById elements - no result");
        } finally {
            entityManager.close();
        }
        return Optional.ofNullable(companyToFind);
    }

    private void setUpCompany(Company companyToUpdate, Company company) {
        companyToUpdate.setName(company.getName());
        companyToUpdate.setDescription(company.getDescription());
        companyToUpdate.setLocations(company.getLocations());
    }
}
