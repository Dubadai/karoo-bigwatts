# Bigger pulse, taller zone bars

Feedback, 2026-11-07: "Pulse is too small and should not be grey. The bars are too small in the one
under the watts."

Agreed (`01-before-after.png`, rendered on the emulator with Karoo look, gauge bar, heart rate,
at a full, half and third of a page):

- The line under the watts (heart rate, zone or ride average) is in the text colour (black, or
  white in dark mode), no longer grey.
- It is up to 60 px (was 32), sized by the field: `min(h × 0.11, w × 0.13, 60)`, at least 22.
- The zone bar is up to 44 px tall (was 30): `min(h × 0.07, 44)`, at least 22. The zones you're
  not in are 70 % of that (was 60 %).
- On a half page the watts get a little smaller to make room; a full page has space to spare.
