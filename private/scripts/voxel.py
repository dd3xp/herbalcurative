#!/usr/bin/env python
"""Convert Blockbench JSON to VoxelShape.

Usage:
    python voxel.py model.json              # Normal mode
    python voxel.py --per-block model.json  # Per-block mode for multiblock
"""

import sys
import os
import json

from src.voxel import parse_normal, parse_perblock


def parse(filename, per_block=False):
    """Parse Blockbench JSON and write VoxelShape output."""
    base, _ = os.path.splitext(filename)
    out_path = base + "_voxelshape.txt"

    try:
        with open(filename, encoding="utf-8") as f:
            raw = f.read()

        if per_block and '"elements"' in raw and '"from"' in raw:
            output = parse_perblock(filename)
        else:
            output = parse_normal(filename)
    except json.JSONDecodeError:
        output = parse_normal(filename)

    with open(out_path, "w", encoding="utf-8") as f:
        f.write(output)
    print(f"Written to {out_path}")


def main(args):
    per_block = "--per-block" in args
    if per_block:
        args = [a for a in args if a != "--per-block"]

    for fn in args[1:]:
        if fn.endswith(".json"):
            parse(fn, per_block)


if __name__ == "__main__":
    main(sys.argv)
