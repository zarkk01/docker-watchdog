//package gr.aueb.dmst.dockerWatchdog.Repositories;
//
//import gr.aueb.dmst.dockerWatchdog.Models.Instance;
//import gr.aueb.dmst.dockerWatchdog.Models.Metric;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//public class InstancesRepositoryTest {
//
//    @Autowired
//    private TestEntityManager entityManager;
//
//    @Autowired
//    private InstancesRepository instancesRepository;
//
//    private Instance instance1;
//    private Instance instance2;
//
//    @BeforeEach
//    public void setup() {
//        Metric metric = new Metric();
//        metric.setId(2);
//
//        instance1 = new Instance();
//        instance1.setId("id1");
//        instance1.setMetricId(2);
//        instance1.setName("name1");
//        instance1.setImage("image1");
//        instance1.setStatus("exited");
//        instance1.setMemoryUsage(1000L);
//        instance1.setPids(10L);
//        instance1.setCpuUsage(0.5);
//        instance1.setBlockI(100.0);
//        instance1.setBlockO(200.0);
//        instance1.setVolumes("volume1");
//        instance1.setSubnet("subnet1");
//        instance1.setGateway("gateway1");
//        instance1.setPrefixLen(24);
//        entityManager.persistAndFlush(instance1);
//
//        instance2 = new Instance();
//        instance2.setId("id2");
//        instance2.setMetricId(2);
//        instance2.setName("name2");
//        instance2.setImage("image2");
//        instance2.setStatus("exited");
//        instance2.setMemoryUsage(2000L);
//        instance2.setPids(20L);
//        instance2.setCpuUsage(1.0);
//        instance2.setBlockI(200.0);
//        instance2.setBlockO(400.0);
//        instance2.setVolumes("volume2");
//        instance2.setSubnet("subnet2");
//        instance2.setGateway("gateway2");
//        instance2.setPrefixLen(24);
//        entityManager.persistAndFlush(instance2);
//    }
//
//    @Test
//    public void testFindAllByMaxMetricId() {
//        List<Instance> instances = instancesRepository.findAllByMaxMetricId();
//
//        int maxMetricId = 0;
//        for(Instance instance : instances) {
//            int curMetricId = instance.getMetricid();
//            if(curMetricId > maxMetricId) {
//                maxMetricId = curMetricId;
//            }
//        }
//
//        assertEquals(maxMetricId, instances.get(0).getMetricid());
//    }
//
//    @Test
//    public void testCountByMetricIdAndStatusRunning() {
//        instance1.setStatus("running");
//        entityManager.persist(instance1);
//
//        long count = instancesRepository.countByMetricIdAndStatusRunning(2);
//
//        assertEquals(2, count);
//    }
//
//    @Test
//    public void testFindByContainerId() {
//        Instance instance = instancesRepository.findByContainerId("id2");
//
//        assertNotNull(instance);
//        assertEquals("id2", instance.getId());
//    }
//
//    @Test
//    public void testFindAllByImageName() {
//        instance1.setImage("image1");
//        entityManager.persist(instance1);
//
//        List<Instance> instances = instancesRepository.findAllByImageName("image1");
//
//        assertNotNull(instances);
//        assertEquals("image1", instances.get(0).getImage());
//    }
//}