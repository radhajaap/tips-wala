package com.marketpulse.content;
import java.util.UUID; import org.springframework.data.jpa.repository.JpaRepository;
public interface InquiryRepository extends JpaRepository<Inquiry,UUID> { long countByStatus(String s); }
