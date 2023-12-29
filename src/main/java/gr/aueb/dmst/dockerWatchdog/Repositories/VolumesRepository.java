package gr.aueb.dmst.dockerWatchdog.Repositories;

import gr.aueb.dmst.dockerWatchdog.Models.Volume;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VolumesRepository extends JpaRepository<Volume, String> {
}
