package com.ankur.springcassandra.repository;

import com.ankur.springcassandra.domain.Car;
import java.util.UUID;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CarRepository extends CassandraRepository<Car, UUID> {
}
