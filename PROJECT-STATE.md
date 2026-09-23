# PROJECT STATE — V1

## Branch

- Development branch: `v1-route-import`.
- Do not write V1 development changes to `main`.
- GitHub branch state is authoritative.

## Current functional baseline

- Current branch HEAD: `728966576c77e56a3efb3408c79119d1180ec094`.
- The six primary walking surfaces remain the acceptance baseline: preparation, active walking map, contextual bottom sheet, next-APOI horizon, APOI list/cards and walking progress.
- The preparation surface is integrated into the V1 Activity flow with route selection, stage/start/end, audio, map orientation, breaks, APOIs, notes and explicit save/start semantics.

## Walking V1

- `PLANNED` and `ACTIVE` are distinct states.
- Saving a plan does not start the walk.
- Explicit start requires a valid GPS position projected onto the selected route within the shared deviation policy.
- Real Android GPS and deterministic QA simulation use the same downstream walking state pipeline.
- Position, progress, route geometry and next APOI are read from the walking runtime/state rather than fixed UI placeholders.
- Signal loss retains the last reliable position and does not invent movement.
- APOI browsing/detail/decision navigation is attached to the same walking context.
- Stop clears the active session and returns to preparation.
- Persisted walking state is restored by the runtime when the app is recreated.

## QA data policy

- SR/HF are synthetic test environments and are explicitly labelled as such.
- QA APOIs are debug-only and must not become production data.
- Production 2027 APOIs remain empty until qualified evidence exists.
- Historical 2026 information is not presented as a 2027 guarantee.

## Validation gate

A green JVM/build pipeline is necessary but not sufficient for human acceptance. The completion gate is:

1. JVM tests pass.
2. Debug APK builds and verifies.
3. Android instrumentation/E2E passes on the current branch.
4. The E2E covers preparation → save → explicit start → GPS → loss/recovery → APOI → detail/back → decision → stop.
5. Visual captures are produced from the E2E run for comparison with the approved reference.
6. Physical Android validation confirms real location permission timing, GPS acquisition, on-route position and continuity.
7. Human acceptance confirms the reference visual hierarchy and that every visible control performs its stated action.

## Current blocker

The branch has a real Android E2E pipeline, but the latest HEAD still requires the current E2E run to complete successfully before the project can be declared human-test ready. No completion claim is made from compilation alone.

## Completion rule

When a CI or human test fails, fix the first reproducible cause on `v1-route-import`, rerun the affected validation, and only then proceed to the next block. Never hide an unresolved failure by weakening the test or changing production data.
