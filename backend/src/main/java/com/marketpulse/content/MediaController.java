package com.marketpulse.content;
import java.io.*; import java.nio.file.*; import java.util.*;
import org.springframework.beans.factory.annotation.Value; import org.springframework.core.io.*; import org.springframework.http.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*; import org.springframework.web.multipart.MultipartFile;
/** Type is decided from file CONTENT (magic bytes), never from file name or client header. SVG/HTML are not accepted. */
@RestController
public class MediaController {
  private static final Map<String,String> EXT = Map.of("image/jpeg","jpg","image/png","png","image/webp","webp","video/mp4","mp4");
  private static final Map<String,String> MIME = Map.of("jpg","image/jpeg","png","image/png","webp","image/webp","mp4","video/mp4");
  private final Path dir; MediaController(@Value("${app.media.dir}") String d){ dir = Path.of(d); }
  private ResponseEntity<?> store(MultipartFile f, boolean imagesOnly, long maxImage) throws IOException {
    if (f.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error","Empty file"));
    String type; try (InputStream in = f.getInputStream()) { type = MediaTypes.of(in.readNBytes(16)); }
    if (type == null || (imagesOnly && !type.startsWith("image"))) return ResponseEntity.badRequest().body(Map.of("error", imagesOnly ? "Only JPEG, PNG or WebP images are allowed" : "Only JPEG, PNG, WebP images or MP4 video are allowed"));
    long max = type.startsWith("video") ? 50L << 20 : maxImage;
    if (f.getSize() > max) return ResponseEntity.badRequest().body(Map.of("error","File too large (max " + (max >> 20) + " MB)"));
    String name = UUID.randomUUID() + "." + EXT.get(type); Files.createDirectories(dir);
    try (InputStream in = f.getInputStream()) { Files.copy(in, dir.resolve(name)); }
    return ResponseEntity.ok(Map.of("url", "/media/" + name, "type", type)); }
  @PostMapping("/api/admin/media") @PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_ADMIN')")
  ResponseEntity<?> upload(@RequestParam("file") MultipartFile f) throws IOException { return store(f, false, 5L << 20); }
  /** Logged-in users: images only, 2 MB (used for review photos). */
  @PostMapping("/api/media/user") ResponseEntity<?> userUpload(@RequestParam("file") MultipartFile f) throws IOException { return store(f, true, 2L << 20); }
  @GetMapping("/media/{name}") ResponseEntity<Resource> serve(@PathVariable String name){
    if (!name.matches("^[a-f0-9\\-]{36}\\.(jpg|png|webp|mp4)$")) return ResponseEntity.notFound().build();
    Path p = dir.resolve(name); if (!Files.isRegularFile(p)) return ResponseEntity.notFound().build();
    return ResponseEntity.ok().header("X-Content-Type-Options","nosniff").header("Cache-Control","public, max-age=604800")
      .contentType(MediaType.parseMediaType(MIME.get(name.substring(name.lastIndexOf('.') + 1)))).body(new FileSystemResource(p)); }
}
