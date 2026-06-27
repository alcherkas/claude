#!/usr/bin/env python3
"""Return ONE product from the catalog — the "needle", not the "haystack".

Usage:  python3 scripts/lookup.py "<id-or-name>"
  e.g.  python3 scripts/lookup.py SKU-1042
        python3 scripts/lookup.py "Cobalt Sprocket 1042"

Loads data/catalog.json, finds the single matching product (by id or
case-insensitive name), applies its pricing rule, and prints just that product as
JSON. This is what the *lean* skill calls so that only ~50 tokens — not the whole
catalog — ever reach the model's context. Stdlib only; no dependencies.
"""
import json
import os
import sys


def apply_rule(base, rule, stock):
    """Same rules described in data/pricing-rules.md, in code form."""
    if rule == "bulk-discount":
        return round(base * 0.90, 2) if stock > 100 else base
    if rule == "member-price":
        return round(base * 0.85, 2)
    if rule == "clearance":
        return round(base * 0.65, 2)
    if rule == "bundle":
        return round(base * 0.95, 2)
    return base  # "standard" or anything unknown


def load_catalog():
    here = os.path.dirname(os.path.abspath(__file__))
    path = os.path.normpath(os.path.join(here, "..", "data", "catalog.json"))
    if not os.path.exists(path):
        sys.exit("catalog.json not found — run: python3 scripts/gen_catalog.py")
    with open(path) as f:
        return json.load(f)


def find(catalog, query):
    q = query.strip().lower()
    for p in catalog:
        if p["id"].lower() == q or p["name"].lower() == q:
            return p
    return None


def main():
    if len(sys.argv) != 2:
        sys.exit('usage: python3 scripts/lookup.py "<id-or-name>"')
    product = find(load_catalog(), sys.argv[1])
    if product is None:
        sys.exit(f'no product matched "{sys.argv[1]}"')
    result = {
        "id": product["id"],
        "name": product["name"],
        "category": product["category"],
        "base_price": product["base_price"],
        "price": apply_rule(product["base_price"], product["pricing_rule"], product["stock"]),
        "stock": product["stock"],
        "applied_rule": product["pricing_rule"],
    }
    print(json.dumps(result, indent=2))


if __name__ == "__main__":
    main()
