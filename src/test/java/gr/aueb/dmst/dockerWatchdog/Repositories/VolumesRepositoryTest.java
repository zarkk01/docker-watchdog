//package gr.aueb.dmst.dockerWatchdog.Repositories;
//
//import gr.aueb.dmst.dockerWatchdog.Models.Volume;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//public class VolumesRepositoryTest {
//
//    @Autowired
//    private TestEntityManager entityManager;
//
//    @Autowired
//    private VolumesRepository volumesRepository;
//
//    @Test
//    public void testSaveAndFindById() {
//        Volume volume = new Volume();
//        volume.setName("volume1");
//
//        volumesRepository.save(volume);
//        entityManager.flush();
//
//        Optional<Volume> foundVolume = volumesRepository.findById("volume1");
//
//        assertTrue(foundVolume.isPresent());
//        assertEquals("volume1", foundVolume.get().getName());
//    }
//}
