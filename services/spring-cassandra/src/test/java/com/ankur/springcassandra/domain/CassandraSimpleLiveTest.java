package com.ankur.springcassandra.domain;
import static org.assertj.core.api.Assertions.assertThat;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
@Testcontainers
@SpringBootTest
class CassandraSimpleLiveTest {
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

  @Test
  void givenCassandraContainer_whenSpringContextIsBootstrapped_thenContainerIsRunningWithNoExceptions() {
    assertThat(cassandra.isRunning()).isTrue();
  }

}
