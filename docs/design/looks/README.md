# BigWatts: looks (round 3, built 2026-11-07)

Asked 2026-11-07: keep the big centred value, gauge and zone colours, but let the rider pick a
**look**, and split colour into **number** and **background**. Readability first, as in
BetterIntervals.

Settings page (scrolls; the Karoo's own segmented style):

- **Look:** BigWatts (IBM Plex Sans SemiBold, no header) / Karoo (Karoo header: green icon top
  left, field name top right such as `3S POWER` in IBM Plex Sans Condensed Medium 31.875 px, and
  the number in the Karoo's condensed font).
- **Number:** Neutral / Zone colour / Gauge.
- **Gauge** (round 3i, only when Number is Gauge): Arc / Bar. Arc is the round gauge; Bar is
  the flat zone bar from the one-row field at every size: number, bar, then the zone line, centred
  as one group (bar 18–30 px tall, the full width inside the margins). One row always uses the bar.
- **Background:** None / Zone (the whole field takes the zone colour). Greyed out with the
  gauge (round 3d): "Not with the gauge: it already shows your zone."
- **Under the watts:** Zone / Heart rate / Avg watts. Moved under the number (round 3b) so it
  isn't crowded against the Karoo field name at the top. Left out at one row.
- **Zone name** (only when Under the watts is Zone, round 3f): Short `Z4` / Compact `Zone 4` /
  Long `Zone 4 · Lactate Threshold` (a small grey dot between).
- Everything under the watts is secondary data, so it's in a secondary grey (round 3f), taken
  from PartnerETA's `text_secondary`: light `#5F5E5A` (6.5:1 on white), dark `#B4B2A9`. The dot
  is BetterIntervals' `icon_muted` `#888780`. On a zone background it falls back to black / white
  if it would drop under 4.5:1.
- **Power:** Instant / 3 s / 5 s / 10 s.
- Footer: "Best two rows tall or more…". The Karoo SDK can't force a minimum size (the rider
  sets it in the page editor), so this is advice only.

Round 2's five styles map onto Number × Background: Plain = Neutral + None, Number = Zone +
None, Tint = Neutral + Zone, Both = Zone + Zone, Gauge = Gauge + None.

Sizes:

- **One row** (480×144): the line under the watts is dropped and the number fills the field. The
  gauge lies flat under the number (an arc would make the number too small).
- **Two rows** and up: line under the watts, number as big as fits.

Gauge (round 3c): our BetterIntervals arc (ends 20.5° above horizontal, needle in the text
colour with a background-coloured outline, number on the arc's centre line), with the zones shown
the way the Coros does it (inspiration only): the arc is split into the seven Karoo zones, equal
lengths, each in its zone colour. The current zone is thick and bright, the others thinner and a
little quieter, and the needle sits at the watts inside the current zone. At one row the same
zones lie flat under the number. The layout reserves room for the needle above the arc, so the
gauge is never cut off at the top.

Round 3d made it softer, like Apple's zone display: every zone segment has rounded ends
(rounded bars at one row), the current one thick and bright, the others a little thinner and
quieter.

Zones follow the Karoo: boundaries from the rider profile (`UserProfile.powerZones`), names from
the Karoo ride app (Active Recovery, Endurance, Tempo, Lactate Threshold, VO2 Max, Anaerobic
Capacity, Neuromuscular). The Karoo's own zone colours are set in its code, not its resources, so
they couldn't be read from the APK.

Zone colours (recommended): the usual training order, grey, blue, green, yellow, orange, red,
purple, which most riders know from Zwift, TrainingPeaks and intervals.icu. Neighbours also differ
in lightness, so they stay apart for red-green colour blindness. Gauge light `#8F8D86 #2F7FD8
#3FA34D #E8B400 #F07A1A #E0383E #8B4FD8`, gauge dark (brighter) `#A6A49C #4C9BF0 #5CC46A #FFD02E
#FF9440 #FF5A5F #A97AF2`. Numbers and zone backgrounds keep the round 2 deep / light shades.

Round 3e: the one-row zone bar is taller (18 px for the current zone), and from two rows up the
content has more room around it (8 % of the field's height and width as margins, at least 18 px),
with the number and the zone line centred as one group.

Round 3g: one row has more room above and below (16 px margins, 14 px at the sides; the zone
bar sits inside the bottom margin). The gauge is as big as the margins allow: its exact height is
counted (needle tip and outline above the arc, the same number-to-line gap as a plain number), and
the number inside the arc is up to 0.7 × r tall and 1.22 × r wide, so the needle never touches it,
even at the ends of the arc.

Round 3h (Karoo look + gauge): the header only uses the corners (icon left, field name right),
so the arc may rise into the space between them. It starts as big as the whole field allows and
shrinks until it keeps 8 px clear of both header items. At one row the number starts right under
the header (10 px bottom margin) so it isn't squeezed between the header and the zone bar.

Readability rules in the mockup:

- The number is always the largest that fits inside the margins.
- A zone-coloured number must keep 4.5:1 contrast against what's behind it, or it falls back to
  black / white. Icons need 3:1 (the green Karoo icon falls back the same way).
- The line under the watts is 22–34 px (Karoo look: 31.875 px, like Karoo field names).

Files: `mockup.html` (built from `mockup.src.html` by `build.py`; inlines the Karoo 3 frame,
fonts from `design-assets/fonts` and the screens in `reference/`). `reference/ride-*.png` are
ride pages from the ClearWind design (native fields, top two rows replaced by BigWatts);
`reference/interval-settings-*.png` give the Settings status bar.

## Built

Ported from `mockup.src.html` into `WattsRenderer.kt` (same sizes, in Karoo 3 pixels), settings in
`BigWattsState.kt` / `Settings.kt` / `SettingsActivity.kt`. v0.1's single Style setting is migrated
on first read (Plain → Neutral, Number → Zone colour, Tint → Neutral + Zone background, Both → Zone
colour + Zone background, Gauge → Gauge arc). The Karoo font is `res/font/plex_condensed_medium.otf`
(IBM Plex Sans Condensed Medium, OFL, from `design-assets/fonts`).

`device/` (debug build on our test Karoo, dark mode, drawn by the debug PreviewActivity):

- `01-settings-dark.png`: Settings, top.
- `10-bw-zone-2rows-dark.png`: BigWatts look, zone colour, two rows, Z4.
- `11-bw-arc-2rows-dark.png`: gauge arc, two rows, Compact `Zone 4`.
- `12-karoo-arc-2rows-dark.png`: Karoo look, gauge arc rising between the header items, Long zone name at 380 W.
- `13-bw-bar-fullpage-dark.png`: gauge bar on a full page, `Zone 5 · VO2 Max`.
- `14-karoo-bar-1row-dark.png`: Karoo look, one row, bar.
- `15-bw-tint-hr-2rows-dark.png`: neutral number on the zone background, heart rate under the watts.

Not yet checked: light mode on the device, and the field on a real ride page.

## Later: Background removed (v0.1.1)

The Background setting (None / Zone) is gone. The Karoo SDK can't colour an extension's tile, and a
zone fill of our own covered the tile's rounded corners and border (see `../transparent-field/`).
We chose to remove it rather than draw our own rounded shape. Old settings: Tint → Neutral,
Both → Zone colour.
