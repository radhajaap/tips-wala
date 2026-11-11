#!/usr/bin/env python3
"""Builds _site/ for GitHub Pages: lite.html -> index.html, pages, assets, analysis posts + analysis/index.json (with a compliance gate)."""
import json, re, shutil, sys, pathlib, datetime
ROOT = pathlib.Path(__file__).resolve().parent.parent
SITE, FE, AN = ROOT / "_site", ROOT / "frontend", ROOT / "analysis"
ALLOWED = {"DAILY_OVERVIEW","NIFTY_ANALYSIS","BANKNIFTY_ANALYSIS","GOLD","SILVER","CRYPTO","EDUCATION","ECONOMIC_NEWS","TECHNICAL_ANALYSIS"}
RESTRICTED = {"STOCK_RECOMMENDATION","TARGET_PRICE","STOP_LOSS","PORTFOLIO_ADVICE"}
BAD = [(re.compile(r"\b(buy|sell)\s+(at|above|below|near|on)\b", re.I), "buy/sell call"),
       (re.compile(r"\btarget(s)?\b|\btgt\b", re.I), "target"),
       (re.compile(r"stop[\s-]?loss|\bsl\s*[:=]", re.I), "stop-loss"),
       (re.compile(r"\b(guaranteed|sure[\s-]?shot|assured\s+returns?)\b", re.I), "guarantee claim")]
errors = []
def err(f, m): errors.append(f"::error file=analysis/{f}::{m}"); print(f"ERROR {f}: {m}")
def parse(p):
    m = re.match(r"^---\s*\n(.*?)\n---\s*\n?(.*)$", p.read_text(encoding="utf-8"), re.S)
    if not m: return None, None
    meta = {}
    for line in m.group(1).splitlines():
        if ":" in line: k, v = line.split(":", 1); meta[k.strip().lower()] = v.strip()
    return meta, m.group(2)
if SITE.exists(): shutil.rmtree(SITE)
SITE.mkdir()
shutil.copy(FE / "lite.html", SITE / "index.html"); shutil.copy(FE / "analysis.html", SITE / "analysis.html")
for d in ("pages", "assets"):
    if (FE / d).exists(): shutil.copytree(FE / d, SITE / d)
(SITE / ".nojekyll").write_text("")
(SITE / "analysis").mkdir()
if (AN / "images").exists(): shutil.copytree(AN / "images", SITE / "analysis" / "images")
today = datetime.date.today().isoformat(); posts = []
for p in sorted(AN.glob("*.md")):
    if p.name.startswith("_") or p.name.lower() == "readme.md": continue
    if not re.match(r"^[A-Za-z0-9._-]+\.md$", p.name): err(p.name, "file name: sirf letters, numbers, - _ . allowed"); continue
    meta, body = parse(p)
    if meta is None: err(p.name, "top par --- title: ... --- wala front matter nahi mila (_template.md dekhein)"); continue
    if meta.get("draft", "").lower() == "true": continue
    cat = meta.get("category", "EDUCATION").upper()
    if cat in RESTRICTED: err(p.name, f"category {cat} SEBI registration ke bina publish nahi ho sakti"); continue
    if cat not in ALLOWED: err(p.name, f"unknown category {cat}"); continue
    date = meta.get("date", "")
    if not re.match(r"^\d{4}-\d{2}-\d{2}$", date): err(p.name, "date YYYY-MM-DD format mein likhein"); continue
    if not meta.get("title"): err(p.name, "title missing"); continue
    if date > today: continue                                  # scheduled for later
    if meta.get("compliance_ok", "").lower() != "reviewed":
        for rx, what in BAD:
            hit = rx.search(body + " " + meta.get("title", "") + " " + meta.get("summary", ""))
            if hit: err(p.name, f"'{hit.group(0)}' ({what}) mila. Ye SEBI-regulated advice jaisa lag sakta hai; text badlein."); break
    shutil.copy(p, SITE / "analysis" / p.name)
    posts.append({"file": p.name, "title": meta["title"], "category": cat, "date": date, "summary": meta.get("summary", ""), "image": meta.get("image", "")})
posts.sort(key=lambda x: x["date"], reverse=True)
(SITE / "analysis" / "index.json").write_text(json.dumps(posts, ensure_ascii=False, indent=1), encoding="utf-8")
print(f"built {len(posts)} analysis post(s)")
if errors: print("\n".join(errors)); sys.exit(1)
