# Tummio Android design and reminder audit — 2026-11-06

## Scope
Source review of the native Kotlin / Jetpack Compose application on `main`, followed by corrections on `feature/smart-reminders-ui-audit`. This is a **source-level audit**, not an emulator-based screenshot inspection. Device measurements and notification delivery still need QA.

## Reminder findings and changes

| Finding | Resolution |
| --- | --- |
| Legacy reminder supported feeding only | Added feeding, sleep-check and diaper-check rules |
| One global alarm ID caused collisions across babies | Unique alarm and notification identity for baby + activity |
| Reboot receiver did not reschedule reminders | Restores enabled reminders after boot; also handles time/time-zone updates and exact alarm permission changes |
| No persistent personalized schedules | Saved per-baby rule settings using SharedPreferences; interval or fixed local HH:mm times |
| Exact alarm access assumed | Uses `canScheduleExactAlarms()` and inexact `setAndAllowWhileIdle()` fallback |
| Reminders did not respond to diaper and sleep entries | Relevant recent activities reset interval-based reminders |
| No reminder administration UI | Added Settings → Smart Reminders |
| Missing age-aware planning | Added clearly labeled editable presets; not clinical requirements |
| Notification interaction missing for new categories | Open relevant tracker and snooze actions |
| Repeating old reminders after disable or reschedule | Stale-alarm timestamp check prevents unwanted alerts |

### Scheduling semantics
- New baby: feeding reminders start with the selected onboarding interval.
- Sleep and diaper check reminders are **off until enabled**, because frequency is a caregiver preference.
- One-time interval reminders wait for a new activity after firing; fixed HH:mm reminders repeat at the next chosen clock time.
- Historical entries more than 15 minutes old do not rewrite the active reminder schedule.
- The app must NOT portray feeding, sleep, or diaper reminders as professional medical advice.

## Design findings and changes

| Screen/area | Source-level issue | Change |
| --- | --- | --- |
| Home quick actions | Five equally weighted controls crammed onto one narrow row | 3+2 responsive rows with larger targets |
| Home nutrition panel | Three columns of long labels and quantities at narrow widths | Full-width stacked metrics |
| Home next feed | Due state could appear even without an actual configured alarm | Read persisted reminder rule and show unscheduled state |
| Home countdown | Large display text could crowd long overdue wording | Reduced to adaptive title size |
| Settings unit/guideline rows | Leading text columns had no flexible width against switches/chips | Weighted text columns and spacing |
| Insights references | Heading and NHS/AAP chips shared cramped horizontal row | Separate title and reference selector |
| Feeding quantities | Several preset chips could extend outside screen | FlowRow wrapping |
| Diaper stool colors and consistency | Wide fixed horizontal chip rows | FlowRow wrapping |
| Sleep quality | Multiple chips in a fixed-width row | FlowRow wrapping |
| History filter bar | Five filter chips could overflow | FlowRow wrapping |
| Onboarding and profile | Several compact chip groups squeezed into one line | FlowRow where appropriate |
| App typography | Labels at 10–11sp with 14–15sp line height | Raised labels to >=12sp with taller line heights |
| Launcher icon | Old assets did not match requested Cozy Baby Bottle Cloud identity | Used existing repository Cozy Baby Cloud asset for launcher and updated adaptive foreground |

## Additional issues identified during review

These are **outside the limited code changes above**, and need separate validation before publishing:
- WHO data: only metadata JSON is present in the bundled `assets/who` directory. Verify that calculation classes never display invented or placeholder percentiles as real WHO reference data.
- Possible mixed-bottle ambiguity: feeding summary currently records every mixed-bottle mL as formula. This is not enough information to infer a precise formula-versus-EBM breakdown.
- Fixed sleep duration totals: the 24-hour summary filters by sleep start time and may miss sessions crossing the cutoff.
- Android notification bell icons use a legacy icon fallback and should be tested at all system status-bar contrast settings.
- Full responsive visual QA is pending for large accessibility font scaling, split screen, 320dp devices, landscape, and display cutouts.
- The source includes several unlocalized strings, which will need localization work for Urdu.

## QA matrix — perform on a device or emulator

- [ ] Build `:app:assembleDebug`, run JVM tests, and inspect new CI results.
- [ ] 320dp compact phone, 360dp phone, 412dp phone, tablet widths.
- [ ] Font scales 1.0, 1.3, 1.5 and 2.0, both light and dark themes.
- [ ] Screen rotation and large/small keyboard interaction.
- [ ] Two different baby profiles; verify no cross-profile alerts.
- [ ] Feeding, diaper and sleep reminders independently enabled/disabled.
- [ ] Test interval after each actual event and after a backdated event.
- [ ] Test fixed clock times, midnight, and time-zone changes.
- [ ] Test snooze and tapping notification when app is backgrounded/closed.
- [ ] Reboot with an active scheduled reminder.
- [ ] Android 13+ notification permission denied then granted.
- [ ] Android 12+ exact-alarm access denied and granted.
- [ ] Battery saver/Doze; document possible delayed delivery.
- [ ] Confirm uploaded artwork and cropping in adaptive/round/legacy launchers.
- [ ] Verify all existing feeding, growth, export, and other modules retain their functionality.

## Known limitations
- No emulator, Gradle wrapper binary, or Android SDK build was available in the editing environment, so no local APK or full UI screenshot audit is claimed.
- The high-resolution icon was copied from the existing `public/assets/aistudio/Cozy_Baby_Bottle_Cloud_Icon_ee271d22.png` repository asset, which matches the requested artwork by asset name. It was not byte-for-byte verified against the newly uploaded attachment.
- For reliable testing, inspect the GitHub Actions Android validation workflow added in this branch.
