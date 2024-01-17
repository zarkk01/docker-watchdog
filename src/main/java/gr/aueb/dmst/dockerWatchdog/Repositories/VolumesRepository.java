package gr.aueb.dmst.dockerWatchdog.Repositories;

import gr.aueb.dmst.dockerWatchdog.Models.Volume;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * VolumesRepository is an interface that help us
 * with executing MYSQL queries on the Volume entity.
 * VolumesRepository does not declare any methods
 * , as it relies on the standard methods provided by JpaRepository
 * but if any custom database access methods are needed in the future, they can be added here.
 */
public interface VolumesRepository extends JpaRepository<Volume, String> {
}
