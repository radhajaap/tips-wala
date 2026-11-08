package com.marketpulse.content;
import java.net.URLEncoder; import java.nio.charset.StandardCharsets; import java.util.List;
import org.springframework.beans.factory.annotation.Value; import org.springframework.web.bind.annotation.*;
@RestController
public class SitemapController {
  private final PostRepository posts; private final String base;
  SitemapController(PostRepository p, @Value("${app.public-url}") String b){ posts = p; base = b.replaceAll("/+$", ""); }
  @GetMapping(value = "/sitemap.xml", produces = "application/xml") String sitemap(){
    var sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
    for (String s : List.of("/","/updates.html","/calendar.html","/pages/about.html","/pages/privacy.html","/pages/terms.html","/pages/disclaimer.html","/pages/risk.html","/pages/refund.html"))
      sb.append("<url><loc>").append(base).append(s).append("</loc></url>");
    for (var p : posts.findByPublishedTrueOrderByPublishedAtDesc()) if (!p.premium)
      sb.append("<url><loc>").append(base).append("/post.html?slug=").append(URLEncoder.encode(p.slug, StandardCharsets.UTF_8)).append("</loc><lastmod>").append(p.publishedAt).append("</lastmod></url>");
    return sb.append("</urlset>").toString(); }
}
