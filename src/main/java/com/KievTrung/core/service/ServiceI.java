package com.KievTrung.core.service;

import com.KievTrung.core.domain.Person;

import java.util.List;

public interface ServiceI<T> {
  void create(Person.Type type, T obj);

  void update(Person.Type type, T obj);

  void delete(Person.Type type, Integer id);

  T get(Person.Type type, Integer id);

  List<T> getAll(Person.Type type);
}
