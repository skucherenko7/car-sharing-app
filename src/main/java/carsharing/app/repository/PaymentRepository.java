package carsharing.app.repository;

import carsharing.app.model.Payment;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @EntityGraph(attributePaths = {"rental", "rental.user"})
    Page<Payment> findAllByRental_User_Id(Long userId, Pageable pageable);

    Optional<Payment> findBySessionId(String sessionId);
}
