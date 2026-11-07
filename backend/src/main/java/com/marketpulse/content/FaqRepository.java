package com.marketpulse.content;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface FaqRepository extends JpaRepository<Faq,UUID> { List<Faq> findAllByOrderBySortOrderAsc(); }
