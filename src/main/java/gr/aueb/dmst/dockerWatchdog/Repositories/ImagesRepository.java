package gr.aueb.dmst.dockerWatchdog.Repositories;

import gr.aueb.dmst.dockerWatchdog.Models.Image;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ImagesRepository is an interface that help us with CRUD operations on the Image entity.
 * ImagesRepository does not declare any methods, as it relies on the standard methods provided by JpaRepository
 * but if any custom database access methods are needed in the future, they can be added here.
 */
public interface ImagesRepository extends JpaRepository<Image, Long> {
}