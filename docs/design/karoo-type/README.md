# Karoo look in the Karoo's own type (round 1, mockup, not decided)

Feedback, 2026-11-07: what can BigWatts take from BetterIntervals' new free-ride screen
(`karoo-betterintervals` branch `free-ride-native`)? Shown at half page, BigWatts' main size.

`00-betterintervals-free-ride-vs-bigwatts.png`: the free-ride screen on our test Karoo next to
BigWatts 0.1.4 (Karoo look).

`01-half-page-now-vs-proposal.png`: the field gallery (real field code on the emulator, Karoo
page with the shared showcase numbers), light and dark. Karoo look, zone bar, heart rate.

Proposal (Karoo look only):
1. Watts in the Karoo's number font: IBM Plex Sans Light, 90 % wide, letter spacing −0.05 (as
   BetterIntervals and the Karoo's own fields). Was Plex Sans Condensed Medium.
2. The line under the watts in the same family, Plex Sans Regular 90 % wide, so its strokes match
   the Karoo's field values (Light looked thin at that size).
3. The Karoo's HR icon: outline heart in the text colour (was a filled red heart).
4. Zone bar / arc at full colour for every zone (was faded, muddy in dark mode); the current zone
   still shows by its height and the needle.

**Decided (2026-11-07): keep 0.1.4.** "Current one is actually better. More clear." The
heavier numbers read better than the Karoo's light number font, so nothing above was built.

0.1.4 at half page, every variant, light and dark (field gallery, Karoo page with the showcase
numbers): `02-0.1.4-number-half.png`, `03-…-bar`, `04-…-arc`, `05-…-hr`, `06-…-karoo`,
`07-…-karoo-bar-hr` (our setup).
