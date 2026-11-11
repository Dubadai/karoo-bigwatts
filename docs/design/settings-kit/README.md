# Settings kit (shared settings design)

BigWatts' Settings built from the shared settings kit (karoo-starter `SettingsKit.kt`, design
`karoo-starter/docs/design/settings-kit/`, BigWatts was the mock's first example). The Example card
scrolls with the page.

- Every heading has its own icon: **Aa** Look, target Number, speed gauge Gauge, clock Zone time,
  the top-row icon upside down for Under the watts, a **Z4** tag for Zone name, the bolt for Power.
  All are the kit's icons (`ic_settings_look`, `_target`, `_speed`, `_clock`, `_under`, `_zone_name`,
  `_power`; kit update 4 took BigWatts' Aa, Z4 and under icons in).
- The size tip is a `settingsInfoRow` (tiles icon); the page ends with `settingsAppRows` (update
  notices, version, github.com/Dubadai/karoo-bigwatts).

| Picture | What |
| --- | --- |
| `01-top-light.png` | Top: title, Example, Look |
| `02-gauge-zone-time-light.png` | Gauge, Zone time, Under the watts |
| `03-zone-name-power-light.png` | Zone name, Power, the size tip |
| `04-end-light.png` | Update notices and the version row (name, version, GitHub on three lines) |

Checked on the Android 12L emulator in light and dark (dark: every heading icon tints white).
