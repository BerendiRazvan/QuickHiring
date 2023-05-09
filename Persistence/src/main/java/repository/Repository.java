package repository;

import lombok.NonNull;
import repository.exception.RepositoryException;

import java.util.List;
import java.util.Optional;

public interface Repository<ID, T> {

    void add(@NonNull T elem) throws RepositoryException;

    void update(@NonNull ID id, @NonNull T elem) throws RepositoryException;

    void delete(@NonNull ID id) throws RepositoryException;

    List<T> findAll();

    Optional<T> findById(@NonNull ID id);
}