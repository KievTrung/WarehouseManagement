package com.KievTrung.core.dao;

import com.KievTrung.core.domain.Person;
import com.KievTrung.core.repository.PersonRepositoryI;
import com.KievTrung.util.helper.DaoHelper;
import com.KievTrung.util.helper.Validation;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PersonDao extends DaoHelper<Person> implements PersonRepositoryI {

    @Override
    public void create(Person.Type type, Person person) {
        try {
            Validation.validate(person);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
        String sql = "insert into " + (!person.isCustomer() ? "suppliers" : "customers") +
                " (name, phone, address, note, createDate, updateDate)" +
                " values (?, ?, ?, ?, ?, ?)";
        wrapListPrepare(new String[]{sql}, new SqlAction<>() {
            @Override
            public void execListPrepare(List<PreparedStatement> pss) throws SQLException {
                @Cleanup PreparedStatement ps = pss.getFirst();
                LocalDateTime created = person.getCreateDate(),
                        updated = person.getUpdateDate();
                ps.setString(1, person.getName());
                ps.setString(2, person.getPhone());
                ps.setString(3, person.getAddress());
                ps.setString(4, person.getNote());
                ps.setTimestamp(5, Timestamp.valueOf(created));
                ps.setTimestamp(6, Timestamp.valueOf(updated));
                ps.executeUpdate();
            }
        });
    }

    @Override
    public void create(Person.Type type, List<Person> persons) {
        wrapTransaction(new SqlAction<>() {
            @Override
            public void execTransaction(Connection con) throws SQLException {
                String sql = String.format("insert into %s ", type == Person.Type.CUSTOMER ? "customers" : "suppliers") +
                        "(id, name, phone, address, note, createDate, updateDate) values (?, ?, ?, ?, ?, ?, ?)";
                @Cleanup PreparedStatement ps = con.prepareStatement(sql);
                for (Person person : persons) {
                    ps.setInt(1, person.getId());
                    ps.setString(2, person.getName());
                    ps.setString(3, person.getPhone());
                    ps.setString(4, person.getAddress());
                    ps.setString(5, person.getNote());
                    ps.setTimestamp(6, Timestamp.valueOf(person.getCreateDate()));
                    ps.setTimestamp(7, Timestamp.valueOf(person.getUpdateDate()));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        });
    }

    @Override
    public void update(Person.Type type, Person person) {
        String sql = "update " + (!person.isCustomer() ? "suppliers" : "customers") + " set name = ?, phone = ?, address = ?, note = ?, updateDate = ? where id = ?";
        wrapListPrepare(new String[]{sql}, new SqlAction<>() {
            @Override
            public void execListPrepare(List<PreparedStatement> pss) throws SQLException {
                @Cleanup PreparedStatement ps = pss.getFirst();
                ps.setString(1, person.getName());
                ps.setString(2, person.getPhone());
                ps.setString(3, person.getAddress());
                ps.setString(4, person.getNote());
                ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(6, person.getId());
                ps.executeUpdate();
            }
        });
    }

    @Override
    public void delete(Person.Type type, Integer id) {
        String sql = "delete from " + (type == Person.Type.SUPPLIER ? "suppliers" : "customers") + " where id = ?";
        wrapListPrepare(new String[]{sql}, new SqlAction<>() {
            @Override
            public void execListPrepare(List<PreparedStatement> pss) throws SQLException, IOException {
                @Cleanup PreparedStatement ps = pss.getFirst();
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        });
    }

    @Override
    public List<Person> findAll(Person.Type type) {
        String sql = "select * from " + (type == Person.Type.SUPPLIER ? "suppliers" : "customers");
        return wrapListResultReturnListT(new String[]{sql}, new SqlAction<>() {
            @Override
            public List<Person> execListResultReturnListT(List<ResultSet> rss) throws SQLException {
                ResultSet rs = rss.getFirst();
                List<Person> customers = new ArrayList<>();
                while (rs.next()) {
                    Person person = new Person();
                    person.setId(rs.getInt("id"));
                    person.setName(rs.getString("name"));
                    person.setPhone(rs.getString("phone"));
                    person.setNote(rs.getString("note"));
                    person.setAddress(rs.getString("address"));
                    person.setCreateDate(rs.getTimestamp("createDate").toLocalDateTime());
                    person.setUpdateDate(rs.getTimestamp("updateDate").toLocalDateTime());
                    person.setType(type);
                    customers.add(person);
                }
                return customers;
            }
        });
    }

    @Override
    public List<Person> getAllWithDebt(Person.Type type) {
        String sql = "select id, name " +
                String.format("from %s p inner join %s_debt d on p.id = d.personId ",
                        type == Person.Type.CUSTOMER ? "customers" : "suppliers",
                        type == Person.Type.CUSTOMER ? "sale" : "purchase") +
                "where isSettled = 0";
        return wrapListResultReturnListT(new String[]{sql}, new SqlAction<>() {
            @Override
            public List<Person> execListResultReturnListT(List<ResultSet> rss) throws SQLException {
                @Cleanup ResultSet rs = rss.getFirst();
                List<Person> persons = new ArrayList<>();
                while(rs.next()){
                    Person person = new Person();
                    person.setId(rs.getInt("id"));
                    person.setName(rs.getString("name"));
                    persons.add(person);
                }
                return persons;
            }
        });
    }

    @Override
    public List<Person> findByName(Person.Type type, String name) {
        String sql = String.format("select * from %s where name like ?", type == Person.Type.SUPPLIER ? "suppliers" : "customers");
        return wrapListPrepareReturnListT(new String[]{sql}, new SqlAction<>() {
            @Override
            public List<Person> execListPrepareReturnListT(List<PreparedStatement> pss) throws SQLException {
                @Cleanup PreparedStatement ps = pss.getFirst();
                ps.setString(1, "%" + name + "%");
                @Cleanup ResultSet rs = ps.executeQuery();
                List<Person> customers = new ArrayList<>();
                while (rs.next()) {
                    Person person = new Person();
                    person.setId(rs.getInt("id"));
                    person.setName(rs.getString("name"));
                    person.setPhone(rs.getString("phone"));
                    person.setNote(rs.getString("note"));
                    person.setAddress(rs.getString("address"));
                    person.setCreateDate(rs.getTimestamp("createDate").toLocalDateTime());
                    person.setUpdateDate(rs.getTimestamp("updateDate").toLocalDateTime());
                    person.setType(type);
                    customers.add(person);
                }
                return customers;
            }
        });
    }

    @Override
    public Person findById(Person.Type type, Integer id) {
        return null;
    }

    @Override
    public List<Person> findAllById(Person.Type type, Integer id) {
        return List.of();
    }

    @Override
    public List<Person> findAllActiveById(Person.Type type, Integer id) {
        return List.of();
    }

}

