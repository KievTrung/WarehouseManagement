package com.KievTrung.core.repository;

import com.KievTrung.core.domain.Person;

import java.util.List;

// các domain repository sẽ implement generic interface này
// nếu repository nào cần có thêm method thì sẽ extend interface này
public interface RepositoryI<T> {
  void create(Person.Type type, T obj);

  void create(Person.Type type, List<T> objs);

  void update(Person.Type type, T obj);

  void delete(Person.Type type, Integer id);

  List<T> findAll(Person.Type type);

  List<T> findByName(Person.Type type, String name);

  T findById(Person.Type type, Integer id);

  List<T> findAllById(Person.Type type, Integer id);

  List<T> findAllActiveById(Person.Type type, Integer id);
}
