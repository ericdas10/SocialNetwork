package socialnetwork.socialnetwork.repository;

import socialnetwork.socialnetwork.domain.Entity;

import java.io.IOException;
import java.util.Optional;

public interface AbstractRepo<ID, E extends Entity<ID>> {
    Optional<E> save(E user) throws IOException;
    Optional<E> remove(ID id) throws IOException;
    Iterable<E> findAll() throws IOException;

    Optional<E> findOne(ID id) throws IOException;
    Optional<E> update(E entity) throws IOException;
}
