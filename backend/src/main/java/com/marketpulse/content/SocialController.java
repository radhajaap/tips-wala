package com.marketpulse.content;
import com.marketpulse.market.*; import jakarta.validation.Valid; import jakarta.validation.constraints.Pattern; import java.util.*;
import org.springframework.http.ResponseEntity; import org.springframework.jdbc.core.JdbcTemplate; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api")
public class SocialController {
  record WatchReq(@Pattern(regexp="^[A-Z0-9&.\\-]{1,30}$", message="invalid symbol") String symbol) {}
  private final JdbcTemplate jdbc; private final PostRepository posts; private final MarketService market;
  SocialController(JdbcTemplate j, PostRepository p, MarketService m){ jdbc=j; posts=p; market=m; }
  private static UUID id(Authentication a){ return UUID.fromString(a.getName()); }
  @GetMapping("/watchlist") List<Quote> watchlist(Authentication a){
    return jdbc.queryForList("select symbol from watchlist where user_id=? order by created_at", String.class, id(a)).stream().map(market::get).toList(); }
  @PostMapping("/watchlist") ResponseEntity<?> add(@Valid @RequestBody WatchReq r, Authentication a){
    Integer n = jdbc.queryForObject("select count(*) from watchlist where user_id=?", Integer.class, id(a));
    if (n != null && n >= 100) return ResponseEntity.badRequest().body(Map.of("error","Watchlist limit is 100"));
    jdbc.update("insert into watchlist(user_id,symbol) values (?,?) on conflict do nothing", id(a), r.symbol()); return ResponseEntity.ok(Map.of("ok", true)); }
  @DeleteMapping("/watchlist/{symbol}") ResponseEntity<?> remove(@PathVariable String symbol, Authentication a){
    jdbc.update("delete from watchlist where user_id=? and symbol=?", id(a), symbol.toUpperCase()); return ResponseEntity.noContent().build(); }
  @PostMapping("/posts/{slug}/like") ResponseEntity<?> like(@PathVariable String slug, Authentication a){
    var p = posts.findBySlugAndPublishedTrue(slug).orElse(null); if (p == null) return ResponseEntity.notFound().build();
    boolean liked = jdbc.update("insert into post_likes(user_id,post_id) values (?,?) on conflict do nothing", id(a), p.id) == 1;
    if (liked) jdbc.update("update posts set likes=likes+1 where id=?", p.id);
    else { jdbc.update("delete from post_likes where user_id=? and post_id=?", id(a), p.id); jdbc.update("update posts set likes=greatest(likes-1,0) where id=?", p.id); }
    return ResponseEntity.ok(Map.of("liked", liked, "likes", jdbc.queryForObject("select likes from posts where id=?", Long.class, p.id))); }
  @PostMapping("/posts/{slug}/bookmark") ResponseEntity<?> bookmark(@PathVariable String slug, Authentication a){
    var p = posts.findBySlugAndPublishedTrue(slug).orElse(null); if (p == null) return ResponseEntity.notFound().build();
    boolean saved = jdbc.update("insert into bookmarks(user_id,post_id) values (?,?) on conflict do nothing", id(a), p.id) == 1;
    if (!saved) jdbc.update("delete from bookmarks where user_id=? and post_id=?", id(a), p.id);
    return ResponseEntity.ok(Map.of("saved", saved)); }
  @GetMapping("/me/bookmarks") List<Map<String,Object>> bookmarks(Authentication a){
    return jdbc.queryForList("select p.slug, p.title from bookmarks b join posts p on p.id=b.post_id where b.user_id=? and p.published order by b.created_at desc", id(a)); }
}
