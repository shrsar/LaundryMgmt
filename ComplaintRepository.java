package com.laundrymgmt.modern.repository;

import com.laundrymgmt.modern.model.Complaint;
import com.laundrymgmt.modern.model.ComplaintStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findAllByOrderByCreatedAtDescIdDesc();

    List<Complaint> findAllByCustomer_IdOrderByCreatedAtDescIdDesc(Long customerId);

    Optional<Complaint> findByTicketCodeIgnoreCase(String ticketCode);

    long countByStatus(ComplaintStatus status);

    List<Complaint> findTop5ByOrderByCreatedAtDescIdDesc();
}
