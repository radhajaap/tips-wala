package com.marketpulse.content;
import java.util.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import org.springframework.transaction.annotation.Transactional;
public interface PostRepository extends JpaRepository<Post,UUID> {
  List<Post> findByPublishedTrueOrderByPublishedAtDesc(); Optional<Post> findBySlugAndPublishedTrue(String slug);
  @Modifying @Transactional @Query("update Post p set p.views=p.views+1 where p.id=:id") void bumpViews(@Param("id") UUID id);
}
