#!/usr/bin/env python3
"""Generate a deterministic product catalog for the skill-context-efficiency demo.

Run from the example root:  python3 scripts/gen_catalog.py
Writes data/catalog.json (~2,000 products). Output is fully deterministic — no
randomness — so re-running it leaves `git diff` clean. The file is intentionally
large so that *reading it whole* (what the bloated skill does) is visibly expensive,
while a single matched row (what the lean skill pulls) is tiny.
"""
import json
import os

N = 2000

ADJECTIVES = [
    "Aurora", "Boreal", "Cobalt", "Dusk", "Ember", "Fjord", "Glacier", "Halcyon",
    "Iris", "Juniper", "Krypton", "Lumen", "Mistral", "Nimbus", "Onyx", "Pyrite",
    "Quartz", "Riven", "Solstice", "Tundra",
]
NOUNS = [
    "Widget", "Gadget", "Sprocket", "Lantern", "Kettle", "Satchel", "Compass",
    "Trowel", "Beacon", "Flask", "Harness", "Ladle", "Mallet", "Notebook",
    "Pulley", "Quiver", "Rasp", "Spindle", "Thimble", "Vise",
]
CATEGORIES = ["Home", "Outdoor", "Kitchen", "Office", "Garden", "Hardware"]
RULES = ["standard", "bulk-discount", "member-price", "clearance", "bundle"]


def build():
    products = []
    for i in range(N):
        adj = ADJECTIVES[i % len(ADJECTIVES)]
        noun = NOUNS[(i // len(ADJECTIVES)) % len(NOUNS)]
        products.append({
            "id": f"SKU-{1000 + i}",
            # The trailing number is unique, so every name is unambiguous.
            "name": f"{adj} {noun} {1000 + i}",
            "category": CATEGORIES[i % len(CATEGORIES)],
            "base_price": round(4.99 + (i % 240) * 0.25, 2),
            "stock": (i * 37 + 11) % 600,
            "pricing_rule": RULES[i % len(RULES)],
        })
    return products


def main():
    here = os.path.dirname(os.path.abspath(__file__))
    out = os.path.join(here, "..", "data", "catalog.json")
    out = os.path.normpath(out)
    os.makedirs(os.path.dirname(out), exist_ok=True)
    with open(out, "w") as f:
        json.dump(build(), f, indent=2)
        f.write("\n")
    print(f"wrote {N} products -> {out}")


if __name__ == "__main__":
    main()
