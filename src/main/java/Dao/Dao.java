package Dao;

import javafx.beans.property.LongProperty;

import java.util.Collection;
import java.util.List;

public interface Dao<T, ID> {
    T findById(ID id) throws Exception;
    Collection<T> findByRoomId(ID id) throws Exception;
    Collection<T> findAll() throws Exception;
    T save(T entity) throws Exception;
    T update(T entity) throws Exception;

    //void delete(T entity);

    void deleteById(long id) throws Exception;

    List<T> search(String query) throws Exception;
    //Collection<T> searchByPhone(String query) throws Exception;


}