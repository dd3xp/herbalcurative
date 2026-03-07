"""Blockbench JSON to VoxelShape converter package."""

from .normal import parse as parse_normal
from .perblock import parse as parse_perblock

__all__ = ["parse_normal", "parse_perblock"]
