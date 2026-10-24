# /// script
# requires-python = ">=3.11"
# dependencies = ["fonttools>=4.50", "uharfbuzz>=0.39"]
# ///
"""Generate the "Tabgram" wordmark vectors that replace telegram_logo and telegram_logo_2.

Glyphs come from Roboto Medium (the font AndroidUtilities.bold() uses), shaped with
HarfBuzz so kerning matches what a TextView would draw. The fill is white because the
callers tint the drawable with a MULTIPLY color filter.

    uv run scripts/tabgram-wordmark.py [--preview DIR]
"""
import argparse
from pathlib import Path

import uharfbuzz as hb
from fontTools.pens.boundsPen import BoundsPen
from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.pens.transformPen import TransformPen
from fontTools.ttLib import TTFont

ROOT = Path(__file__).resolve().parent.parent
FONT = ROOT / "TMessagesProj/src/main/assets/fonts/rmedium.ttf"
OUT_DIR = ROOT / "TMessagesProj_App/src/tabgram/res/drawable"
TEXT = "Tabgram"
# Viewport units per dp, as in the upstream logo vectors.
UNITS_PER_DP = 3
# Height of each upstream drawable they replace and, where a caller forces the upstream
# bounds (IntroActivity sets telegram_logo to 115x26.33dp, upstream's 83x19dp shape), its
# viewport width so the glyphs are not stretched; the ink is centred in that width, as the
# upstream glyphs span their whole viewport. Without one, the width follows the text advance.
WORDMARKS = {
    "telegram_logo": (19.0, 249),
    "telegram_logo_2": (20.408922, None),
}


def num(value: float) -> str:
    text = f"{value:.2f}".rstrip("0").rstrip(".")
    return "0" if text == "-0" else text


def dp(value: float) -> str:
    return f"{value:.6f}".rstrip("0").rstrip(".") + "dp"


def shape(font_path: Path):
    blob = hb.Blob.from_file_path(str(font_path))
    hb_font = hb.Font(hb.Face(blob))
    buf = hb.Buffer()
    buf.add_str(TEXT)
    buf.guess_segment_properties()
    hb.shape(hb_font, buf, {"kern": True, "liga": True})
    return [(info.codepoint, pos.x_advance, pos.x_offset, pos.y_offset)
            for info, pos in zip(buf.glyph_infos, buf.glyph_positions)]


def build(font: TTFont, glyphs, height_dp: float, fixed_viewport_w):
    glyph_set = font.getGlyphSet()
    order = font.getGlyphOrder()
    placed = []
    pen_x = 0.0
    for gid, advance, dx, dy in glyphs:
        placed.append((order[gid], pen_x + dx, dy))
        pen_x += advance
    advance_width = pen_x

    bounds = BoundsPen(glyph_set)
    for name, x, y in placed:
        glyph_set[name].draw(TransformPen(bounds, (1, 0, 0, 1, x, y)))
    x_min, y_min, x_max, y_max = bounds.bounds

    viewport_h = round(height_dp * UNITS_PER_DP)
    scale = viewport_h / (y_max - y_min)
    if fixed_viewport_w is None:
        viewport_w = round(advance_width * scale)
        shift = 0.0
    else:
        viewport_w = fixed_viewport_w
        shift = (viewport_w - (x_max - x_min) * scale) / 2 - x_min * scale
    width_dp = viewport_w / UNITS_PER_DP

    svg = SVGPathPen(glyph_set, ntos=num)
    for name, x, y in placed:
        transform = (scale, 0, 0, -scale, x * scale + shift, (y_max - y) * scale)
        glyph_set[name].draw(TransformPen(svg, transform))
    return svg.getCommands(), width_dp, viewport_w, viewport_h


# fillType needs API 24, as in the upstream logos, so the build rasterizes these for older
# devices into the same anydpi-v24 + density variants as the library's and replaces each one.
def vector_xml(path, width_dp, height_dp, viewport_w, viewport_h) -> str:
    return (
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"'
        f' android:width="{num(width_dp)}dp" android:height="{dp(height_dp)}"'
        f' android:viewportWidth="{viewport_w}" android:viewportHeight="{viewport_h}">\n'
        f'    <path android:fillColor="#FFFFFFFF" android:fillType="nonZero" android:pathData="{path}" />\n'
        "</vector>\n"
    )


def preview_svg(path, viewport_w, viewport_h, fill: str, background: str) -> str:
    pad = 8
    return (
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{(viewport_w + 2 * pad) * 2}"'
        f' height="{(viewport_h + 2 * pad) * 2}"'
        f' viewBox="{-pad} {-pad} {viewport_w + 2 * pad} {viewport_h + 2 * pad}">'
        f'<rect x="{-pad}" y="{-pad}" width="100%" height="100%" fill="{background}"/>'
        f'<path fill="{fill}" d="{path}"/></svg>\n'
    )


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--preview", type=Path, help="also write preview SVGs to this directory")
    args = parser.parse_args()

    font = TTFont(FONT)
    glyphs = shape(FONT)
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    for name, (height_dp, viewport_w) in WORDMARKS.items():
        path, width_dp, vw, vh = build(font, glyphs, height_dp, viewport_w)
        (OUT_DIR / f"{name}.xml").write_text(vector_xml(path, width_dp, height_dp, vw, vh))
        print(f"{name}: {num(width_dp)}dp x {dp(height_dp)} (viewport {vw}x{vh})")
        if args.preview:
            args.preview.mkdir(parents=True, exist_ok=True)
            for theme, fill, bg in (("light", "#222222", "#FFFFFF"), ("dark", "#FFFFFF", "#1F2732")):
                (args.preview / f"{name}-{theme}.svg").write_text(preview_svg(path, vw, vh, fill, bg))


if __name__ == "__main__":
    main()
