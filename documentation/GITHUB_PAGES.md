# Free public site on GitHub Pages (no install)
Needs: public repo (free Pages works only on public repos unless you pay for GitHub Pro/Team).
1. Copy from the zip into your repo folder: `frontend\lite.html`, `frontend\analysis.html`, `analysis\` (folder), `scripts\build_site.py`, `.github\workflows\pages.yml`.
2. In cmd: `cd Documents\GitHub\tips-wala` then `git add .` then `git commit -m "Pages + analysis"` then `git push`.
3. GitHub repo > Settings > Pages > Build and deployment > Source = **GitHub Actions**.
4. Repo > Actions tab > "Deploy site (GitHub Pages)" > wait for green tick. Link: `https://YOUR-USERNAME.github.io/tips-wala/`.
5. New analysis: repo > `analysis` folder > Add file > Create new file > name `2026-11-12-nifty-overview.md` > paste `_template.md` text > Commit. Live in 1-2 minutes.
The build refuses posts with buy/sell calls, targets, stop-loss or guarantee words, and restricted categories.
