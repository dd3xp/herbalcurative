"""Normal mode: convert Blockbench JSON to single VoxelShape."""

from itertools import zip_longest


def grouper(iterable, n, fillvalue=None):
    """Collect data into fixed-length chunks."""
    args = [iter(iterable)] * n
    return zip_longest(*args, fillvalue=fillvalue)


def parse(filename):
    """Parse Blockbench JSON and return Shapes.or(...) expression."""
    with open(filename, encoding="utf-8") as f:
        data = f.readlines()

    lines = [line for line in data if '"from"' in line or '"to"' in line]
    boxes = []

    for from_line, to_line in grouper(lines, 2):
        if from_line and to_line:
            from_vals = from_line.split(": [")[1].replace("]", "").strip().rstrip(",")
            to_vals = to_line.split(": [")[1].replace("]", "").strip().rstrip(",")
            boxes.append(f"Block.box({from_vals}, {to_vals})")

    if not boxes:
        return "// No elements found"
    if len(boxes) == 1:
        return boxes[0] + ";"
    return "Shapes.or(" + ", ".join(boxes) + ");"
