package repository.imageDataRepository;

import lombok.NonNull;
import domain.ImageData;
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

public class ImageDataRepositoryImpl implements ImageDataRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageDataRepositoryImpl.class);
    private final EntityManagerFactory entityManagerFactory;

    public ImageDataRepositoryImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void add(@NonNull ImageData image) throws RepositoryException {
        LOGGER.info("Add element - started");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = null;
        try {
            entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();
            entityManager.persist(image);
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
    public void update(@NonNull Long id, @NonNull ImageData image) throws RepositoryException {
        LOGGER.info("Update element with id = {} - started", id);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = null;
        ImageData imageToUpdate;
        try {
            entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();
            imageToUpdate = entityManager.find(ImageData.class, id);
            setUpImage(imageToUpdate, image);
            entityManager.merge(imageToUpdate);
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
        ImageData imageToDelete;
        try {
            entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();
            imageToDelete = entityManager.find(ImageData.class, id);
            entityManager.remove(imageToDelete);
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
    public List<ImageData> findAll() {
        LOGGER.info("FindAll elements - started");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        List<ImageData> imageList = new ArrayList<>();
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<ImageData> criteriaQuery = criteriaBuilder.createQuery(ImageData.class);
            Root<ImageData> rootEntry = criteriaQuery.from(ImageData.class);
            CriteriaQuery<ImageData> all = criteriaQuery.select(rootEntry);
            TypedQuery<ImageData> allQuery = entityManager.createQuery(all);
            imageList = allQuery.getResultList();
            LOGGER.info("FindAll elements - finished");
        } catch (NoResultException ex) {
            LOGGER.info("FindAll elements - no result");
        } finally {
            entityManager.close();
        }
        return imageList;
    }

    @Override
    public Optional<ImageData> findById(@NonNull Long id) {
        LOGGER.info("FindById element with id = {} - started", id);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        ImageData userToFind = null;
        try {
            userToFind = entityManager.find(ImageData.class, id);
            LOGGER.info("FindById element with id = {} - finished", id);
        } catch (NoResultException ex) {
            LOGGER.info("FindById elements - no result");
        } finally {
            entityManager.close();
        }
        return Optional.ofNullable(userToFind);
    }

    private void setUpImage(ImageData imageToUpdate, ImageData image) {
        imageToUpdate.setName(image.getName());
        imageToUpdate.setType(image.getType());
        imageToUpdate.setImageData(image.getImageData());
        imageToUpdate.setWidth(image.getWidth());
        imageToUpdate.setHeight(image.getHeight());
    }

}
