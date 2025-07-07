package mate.academy.carsharing.app.repository;

import java.util.Optional;
import mate.academy.carsharing.app.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @EntityGraph(attributePaths = {"rental", "rental.user"})
    Page<Payment> findAllByRental_User_Id(Long userId, Pageable pageable);

    Optional<Payment> findBySessionId(String sessionId);
}
