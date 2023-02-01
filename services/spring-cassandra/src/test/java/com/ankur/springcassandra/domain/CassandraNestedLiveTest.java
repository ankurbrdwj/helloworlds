package com.ankur.springcassandra.domain;

import com.ankur.springcassandra.repository.CarRepository;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.utils.UUIDs;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class CassandraNestedLiveTest {
  /*
  /docker run -p 9042:9042 --rm --name cassandra -d cassandra:4.1.0
  docker exec -it cassandra cqlsh
  CREATE KEYSPACE spring_cassandra WITH replication = {'class' : 'SimpleStrategy', 'replication_factor' : 1};
   */
  private static final String KEYSPACE_NAME = "spring_cassandra";

  @Container
  private static final CassandraContainer cassandra = (CassandraContainer) new CassandraContainer("cassandra:4.1.0")
    .withExposedPorts(9042);

  @BeforeAll
  static void setupCassandraConnectionProperties() {
    System.setProperty("spring.data.cassandra.keyspace-name", KEYSPACE_NAME);
    System.setProperty("spring.data.cassandra.contact-points", cassandra.getContainerIpAddress());
    System.setProperty("spring.data.cassandra.port", String.valueOf(cassandra.getMappedPort(9042)));

    createKeyspace(cassandra.getCluster());
  }

  static void createKeyspace(Cluster cluster) {
    try(Session session = cluster.connect()) {
      session.execute("CREATE KEYSPACE IF NOT EXISTS " + KEYSPACE_NAME + " WITH replication = \n" +
        "{'class':'SimpleStrategy','replication_factor':'1'};");
    }
  }

  @Nested
  class ApplicationContextLiveTest {

    @Test
    void givenCassandraContainer_whenSpringContextIsBootstrapped_thenContainerIsRunningWithNoExceptions() {
      assertThat(cassandra.isRunning()).isTrue();
    }

  }

  @Nested
  class CarRepositoryLiveTest {

    @Autowired
    private CarRepository carRepository;

    @Test
    void givenValidCarRecord_whenSavingIt_thenRecordIsSaved() {
      UUID carId = UUIDs.timeBased();
      Car newCar = new Car(carId, "Nissan", "Qashqai", 2018);

      carRepository.save(newCar);

      List<Car> savedCars = carRepository.findAllById(List.of(carId));
      assertThat(savedCars.get(0)).isEqualTo(newCar);
    }

    @Test
    void givenExistingCarRecord_whenUpdatingIt_thenRecordIsUpdated() {
      UUID carId = UUIDs.timeBased();
      Car existingCar = carRepository.save(new Car(carId, "Nissan", "Qashqai", 2018));

      existingCar.setModel("X-Trail");
      carRepository.save(existingCar);

      List<Car> savedCars = carRepository.findAllById(List.of(carId));
      assertThat(savedCars.get(0).getModel()).isEqualTo("X-Trail");
    }

    @Test
    void givenExistingCarRecord_whenDeletingIt_thenRecordIsDeleted() {
      UUID carId = UUIDs.timeBased();
      Car existingCar = carRepository.save(new Car(carId, "Nissan", "Qashqai", 2018));

      carRepository.delete(existingCar);

      List<Car> savedCars = carRepository.findAllById(List.of(carId));
      assertThat(savedCars.isEmpty()).isTrue();
    }

  }

}
