package repository.locationRepository;

import lombok.NonNull;
import domain.Location;
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

public class LocationRepositoryImpl implements LocationRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationRepositoryImpl.class);
    private final EntityManagerFactory entityManagerFactory;

    public LocationRepositoryImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void add(@NonNull Location location) throws RepositoryException {
        LOGGER.info("Add element - started");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = null;
        try {
            entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();
            entityManager.persist(location);
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
    public void update(@NonNull Long id, @NonNull Location location) throws RepositoryException {
        LOGGER.info("Update element with id = {} - started", id);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = null;
        Location locationToUpdate;
        try {
            entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();
            locationToUpdate = entityManager.find(Location.class, id);
            setUpLocation(locationToUpdate, location);
            entityManager.merge(locationToUpdate);
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
        Location locationToDelete;
        try {
            entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();
            locationToDelete = entityManager.find(Location.class, id);
            entityManager.remove(locationToDelete);
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
    public List<Location> findAll() {
        LOGGER.info("FindAll elements - started");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        List<Location> locationList = new ArrayList<>();
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Location> criteriaQuery = criteriaBuilder.createQuery(Location.class);
            Root<Location> rootEntry = criteriaQuery.from(Location.class);
            CriteriaQuery<Location> all = criteriaQuery.select(rootEntry);
            TypedQuery<Location> allQuery = entityManager.createQuery(all);
            locationList = allQuery.getResultList();
            LOGGER.info("FindAll elements - finished");
        } catch (NoResultException ex) {
            LOGGER.info("FindAll elements - no result");
        } finally {
            entityManager.close();
        }
        return locationList;
    }

    @Override
    public Optional<Location> findById(@NonNull Long id) {
        LOGGER.info("FindById element with id = {} - started", id);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Location locationToFind = null;
        try {
            locationToFind = entityManager.find(Location.class, id);
            LOGGER.info("FindById element with id = {} - finished", id);
        } catch (NoResultException ex) {
            LOGGER.info("FindById elements - no result");
        } finally {
            entityManager.close();
        }
        return Optional.ofNullable(locationToFind);
    }

    private void setUpLocation(Location locationToUpdate, Location location) {
        locationToUpdate.setCity(location.getCity());
        locationToUpdate.setCountry(location.getCountry());
    }
}
