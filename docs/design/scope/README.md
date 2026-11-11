# Big Watts: scope

**Agreed (round 2, 2026-11-07):** one Karoo data field with power big in the middle, taken from
Interval Screen's (now BetterIntervals') big-watts styles. Colours follow the **Karoo power zones**
from the rider profile, not a workout target. No workouts, no intervals.icu.

- **No tap on the field**: a tap action would block the Karoo's long-press (change field). All
  choices are on the Settings page (from the app list):
  - Style: Plain / Number / Tint / Both / Gauge (default Number).
  - Above the watts: Zone ("Z4 · Threshold") / Heart rate (heart + bpm, as in Interval Screen) /
    Avg watts (whole ride) (default Zone).
  - Power: Instant / 3 s / 5 s / 10 s (default 3 s).
- The number sizes itself to the field (full page, half, quarter, small).
- Gauge: arc 0–160 % FTP in the seven zone colours, current zone bright, needle at the watts.

Round 1 (`mockup-round1.html`, `round1-overview.png`) had the style switched by tapping the
watts; dropped for the long-press reason above.

Files: `mockup.html` (built from `mockup.src.html` by `build.py`; reference screens in
`reference/`), `round2-overview.png`.

Device screenshots (`device/`, debug build with demo data, drawn by the debug-only preview screen
at full-page size 478×721):

- `01-settings-light.png`: Settings page.
- `10-plain-z4-light.png` … `14-gauge-z4-light.png`: the five styles at 280 W (Z4).
- `20-gauge-z6-light.png`: gauge at 380 W (Z6).
- `21-tint-z2-hr-light.png`: tint, heart rate above the watts.
- `22-number-z5-avg-light.png`: coloured number, average watts above.
- `23-both-half-light.png`: both, half-page height.

Not yet checked on the device: dark mode, and the field on a real ride page.
