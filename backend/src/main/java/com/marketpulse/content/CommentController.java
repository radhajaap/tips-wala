package com.marketpulse.content;
import com.marketpulse.user.UserRepository; import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.util.*;
import org.springframework.beans.factory.annotation.Value; import org.springframework.http.ResponseEntity; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*;
/** Off by default (COMMENTS_ENABLED=false). Every comment is held for admin approval. */
@RestController @RequestMapping("/api")
public class CommentController {
  record CommentReq(@NotBlank @Size(max=1000) String body) {}
  private final CommentRepository comments; private final PostRepository posts; private final UserRepository users; private final boolean enabled;
  CommentController(CommentRepository c, PostRepository p, UserRepository u, @Value("${app.comments-enabled}") boolean e){ comments=c; posts=p; users=u; enabled=e; }
  @GetMapping("/posts/{slug}/comments") ResponseEntity<?> list(@PathVariable String slug){
    var p = posts.findBySlugAndPublishedTrue(slug).orElse(null); if (p == null) return ResponseEntity.notFound().build();
    if (!enabled) return ResponseEntity.ok(Map.of("enabled", false, "items", List.of()));
    return ResponseEntity.ok(Map.of("enabled", true, "items", comments.findByPostIdAndStatusOrderByCreatedAtAsc(p.id, "APPROVED").stream().map(c -> Map.of("name", c.authorName, "body", c.body, "createdAt", c.createdAt.toString())).toList())); }
  @PostMapping("/posts/{slug}/comments") ResponseEntity<?> add(@PathVariable String slug, @Valid @RequestBody CommentReq r, Authentication a){
    if (!enabled) return ResponseEntity.badRequest().body(Map.of("error","Comments are disabled"));
    var p = posts.findBySlugAndPublishedTrue(slug).orElse(null); if (p == null) return ResponseEntity.notFound().build();
    var u = users.findById(UUID.fromString(a.getName())).orElseThrow(); var c = new Comment(); c.postId = p.id; c.userId = u.id; c.authorName = u.fullName.trim().split(" ")[0]; c.body = r.body().trim();
    comments.save(c); return ResponseEntity.ok(Map.of("status","PENDING")); }
  @GetMapping("/admin/comments") @PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_ADMIN')") List<Comment> pending(@RequestParam(defaultValue="PENDING") String status){ return comments.findByStatusOrderByCreatedAtDesc(status); }
  @PutMapping("/admin/comments/{id}/status") @PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_ADMIN')") ResponseEntity<?> moderate(@PathVariable UUID id, @RequestParam String value){
    if (!Set.of("APPROVED","HIDDEN").contains(value)) return ResponseEntity.badRequest().body(Map.of("error","value must be APPROVED or HIDDEN"));
    return comments.findById(id).<ResponseEntity<?>>map(c -> { c.status = value; return ResponseEntity.ok(comments.save(c)); }).orElse(ResponseEntity.notFound().build()); }
}
