# Header in every look (mockup)

Feedback, 2026-11-07: "Add so all have these icons and text": the Karoo header (green bolt top left,
field name such as "3S POWER" top right) in the BigWatts look too, not only the Karoo look.

Built on branch `karoo-native-type` (WattsRenderer: the header block runs for every look; the
one-row zone bar sits where the Karoo look had it). Field gallery, half page, light and dark:
`01-number-half.png`, `02-bar-half.png`, `03-arc-half.png`, `04-hr-half.png`.
`05-small-sizes-light.png`: one row, small and quarter (number, bar, arc): the number keeps a
6 px gap below the header, as in the Karoo look.

## Alignment check on our test Karoo (dark, half page, test build)

`device/01-test-build-half-page-dark.png`, `06-header-vs-karoo-fields-3x.png` (each tile from its
top border, 3× zoom; red = the Karoo's caps top and baseline, blue = 17 px from the left, 469 px).

| From the tile's top-left | Karoo's own (AVG SPEED / AVG POWER, 168 px tiles) | BigWatts now |
| --- | --- | --- |
| Capitals top / height / baseline | 26 / 23 / 49 px | 14 / 22 / 36 px (12 px higher, a bit smaller) |
| Icon | bolt 15×25 px, top 24, left 17 (speed icon left 12); white/black | bolt 15×21, top 14, left 18; green |
| Text's right edge | 469 px (9 px from the screen edge) | 467 px |

Horizontally it's within 1–2 px; vertically it's 10–12 px too high and the icon is 4 px short.
`UpdateGraphicConfig(showHeader = true)` draws no Karoo header on a graphical field (tried), so
the header stays ours. ClearWind's measured rules (`karoo-clearwind/docs/design/header-typography`)
are the reference for a fix.

## Fixed: the Karoo's own header rules

WattsRenderer draws the header the way the Karoo draws its own (ClearWind's measured rules, checked
again here): capitals 20 px in tiles under 150 px, else 23 px; capitals start 21 px from the top
at 142 px and 26 px from 168 px up (a full-width row: 12 px, measured on its 3S POWER field);
text ends 9 px from the right. The bolt is the Karoo's own shape, traced from its AVG POWER field
(15 × 26.5 px with 23 px capitals), centred in a 25 px box 12 px from the left, ending on the
baseline, in the text colour (black / white, as the Karoo's power icons in both modes). The watts
start 10 px below the header.

- `device/02-fixed-half-page-light.png`, `07-fixed-vs-karoo-fields-3x.png`: test build on our
  Karoo, light, next to AVG SPEED / AVG POWER: capitals top 26 and baseline 48 in both; bolt
  17–31 × 24–48 (Karoo) vs 18–31 × 25–48; text's right edge 469 vs 468 (moved 1 px after this shot).
- `01-number-half.png`, `02-bar-half.png`, `03-arc-half.png`, `04-karoo-bar-hr-half.png`: half page
  in the device, light and dark (field gallery). `05-small-sizes-light.png`: row, small, quarter.

Not checked on a device: a full-width row tile and dark mode with the fix (the gallery shows both).

## Watts a bit lower (mockup)

Feedback: "move down the watts a bit from the 3S POWER text". Space under the header, from its
baseline to the first ink below: `max(22, 0.9 × padding)` (was `max(10, 0.5 × padding)`). Half page
(gallery): zone bar 12 → 22 px, Karoo look bar 11 → 22 px, number 28 → 34 px (the number is a
little smaller to make room). The arc already keeps its watts well below the header (unchanged).
`08-…-karoo-bar-hr`, `09-…-bar`, `10-…-number`: now vs lower, light and dark.

- `device/03-watts-lower-before-after-dark.png`: on our test Karoo (Karoo look, bar, heart rate,
  demo data): before 0.1.5, gap 12 px; after, 23 px.
