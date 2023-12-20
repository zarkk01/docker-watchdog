package gr.aueb.dmst.dockerWatchdog;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InstanceRepository extends JpaRepository<Instance, Long> {
    // You can define custom query methods here if needed
}