#!/usr/bin/env python
"""Convert Blockbench JSON to VoxelShape. Usage: python voxel.py [--per-block] model.json"""

import sys
import os
import json
from itertools import zip_longest

def grouper(iterable, n, fillvalue=None):
    args = [iter(iterable)] * n
    return zip_longest(*args, fillvalue=fillvalue)

def to_local(lo_val, hi_val, base):
    lo = max(lo_val, base)
    hi = min(hi_val, base + 16)
    return (lo - base, hi - base) if lo < hi else None

def parse_blockbench_per_block(filename):
    """Parse JSON and output per-block shapes (0-16 local coords) for 3x3x2 multiblock."""
    with open(filename, encoding="utf-8") as f:
        data = json.load(f)
    elements = data.get("elements", [])
    if not elements:
        return "// No elements found"

    shapes_by_block = {}
    for dy in (0, 1):
        for dx in (-1, 0, 1):
            for dz in (-1, 0, 1):
                boxes = []
                for el in elements:
                    f, t = el["from"], el["to"]
                    lx = to_local(f[0], t[0], dx * 16)
                    ly = to_local(f[1], t[1], dy * 16)
                    lz = to_local(f[2], t[2], dz * 16)
                    if lx and ly and lz:
                        boxes.append(f"Block.box({lx[0]}, {ly[0]}, {lz[0]}, {lx[1]}, {ly[1]}, {lz[1]})")
                if boxes:
                    shapes_by_block[(dx, dy, dz)] = boxes

    lines = []
    for (dx, dy, dz) in sorted(shapes_by_block.keys(), key=lambda k: (k[1], k[0], k[2])):
        boxes = shapes_by_block[(dx, dy, dz)]
        expr = boxes[0] if len(boxes) == 1 else "Shapes.or(" + ", ".join(boxes) + ")"
        lines.append(f"    // dx={dx} dy={dy} dz={dz}")
        lines.append(f"    {expr},")
    return "\n".join(lines)

def parse_line_format(filename):
    with open(filename, encoding="utf-8") as o:
        data = o.readlines()

    lines = [l for l in data if '"from"' in l or '"to"' in l]
    new_lines = []
    for from_line, to_line in grouper(lines, 2):
        if from_line and to_line:
            from_vals = from_line.split(": [")[1].replace("]", "").strip().rstrip(",")
            to_vals = to_line.split(": [")[1].replace("]", "").strip().rstrip(",")
            new_lines.append("Block.box(" + from_vals + ", " + to_vals + ")")

    return "Shapes.or(" + ", ".join(new_lines) + ");"

def parse(filename, per_block=False):
    base, _ = os.path.splitext(filename)
    out_path = base + "_voxelshape.txt"

    try:
        with open(filename, encoding="utf-8") as f:
            raw = f.read()
        if per_block and '"elements"' in raw and '"from"' in raw:
            output = parse_blockbench_per_block(filename)
        else:
            output = parse_line_format(filename)
    except json.JSONDecodeError:
        output = parse_line_format(filename)

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
