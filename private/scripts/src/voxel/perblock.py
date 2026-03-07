"""Per-block mode: convert Blockbench JSON to per-block VoxelShapes for multiblock structures."""

import json
import math


def to_local(lo_val, hi_val, base):
    """Convert global coords to local 0-16 coords for a block at given base."""
    lo = max(lo_val, base)
    hi = min(hi_val, base + 16)
    return (lo - base, hi - base) if lo < hi else None


def calc_bounds(elements):
    """Calculate bounding box of all elements, return block ranges for each axis."""
    min_x = min_y = min_z = float('inf')
    max_x = max_y = max_z = float('-inf')

    for el in elements:
        f, t = el["from"], el["to"]
        min_x, max_x = min(min_x, f[0], t[0]), max(max_x, f[0], t[0])
        min_y, max_y = min(min_y, f[1], t[1]), max(max_y, f[1], t[1])
        min_z, max_z = min(min_z, f[2], t[2]), max(max_z, f[2], t[2])

    def block_range(lo, hi):
        start = math.floor(lo / 16)
        end = math.ceil(hi / 16)
        return range(start, end)

    return block_range(min_x, max_x), block_range(min_y, max_y), block_range(min_z, max_z)


def parse(filename):
    """Parse Blockbench JSON and return per-block shapes (auto-detect size)."""
    with open(filename, encoding="utf-8") as f:
        data = json.load(f)

    elements = data.get("elements", [])
    if not elements:
        return "// No elements found"

    x_range, y_range, z_range = calc_bounds(elements)

    shapes_by_block = {}

    for dy in y_range:
        for dx in x_range:
            for dz in z_range:
                boxes = []
                for el in elements:
                    f_coords, t_coords = el["from"], el["to"]
                    lx = to_local(f_coords[0], t_coords[0], dx * 16)
                    ly = to_local(f_coords[1], t_coords[1], dy * 16)
                    lz = to_local(f_coords[2], t_coords[2], dz * 16)
                    if lx and ly and lz:
                        boxes.append(f"Block.box({lx[0]}, {ly[0]}, {lz[0]}, {lx[1]}, {ly[1]}, {lz[1]})")
                if boxes:
                    shapes_by_block[(dx, dy, dz)] = boxes

    lines = []
    size_x, size_y, size_z = len(x_range), len(y_range), len(z_range)
    lines.append(f"// Auto-detected size: {size_x}x{size_y}x{size_z} (X×Y×Z)")
    lines.append(f"// Block range: X[{x_range.start}~{x_range.stop-1}] Y[{y_range.start}~{y_range.stop-1}] Z[{z_range.start}~{z_range.stop-1}]")
    lines.append("")

    for (dx, dy, dz) in sorted(shapes_by_block.keys(), key=lambda k: (k[1], k[0], k[2])):
        boxes = shapes_by_block[(dx, dy, dz)]
        if len(boxes) == 1:
            expr = boxes[0]
        else:
            expr = "Shapes.or(" + ", ".join(boxes) + ")"
        lines.append(f"    // dx={dx} dy={dy} dz={dz}")
        lines.append(f"    {expr},")

    return "\n".join(lines)
