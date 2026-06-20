"""wikigen — build a browsable knowledge wiki from the curated link libraries in todo/.

Pipeline stages (run via cli.py):
    parse    -> extract link records from todo/*.md
    manifest -> dedupe + canonicalize -> manifest.json
    fetch    -> polite async download into the local-only archive/
    build    -> extract + render public docs/ pages + build report
    all      -> parse -> manifest -> fetch -> build

Design note: the published docs/ tree contains only annotations + a short excerpt +
the canonical link (copyright-safe). Full text lives only in the git-ignored archive/.
"""

__version__ = "1.0.0"
