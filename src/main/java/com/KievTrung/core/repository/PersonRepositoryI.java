package com.KievTrung.core.repository;

import com.KievTrung.core.domain.Person;

import java.util.List;

// Customer, Item, Supplier
public interface PersonRepositoryI extends RepositoryI<Person> {
  List<Person> getAllWithDebt(Person.Type type);
}
