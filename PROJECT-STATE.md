# PROJECT STATE — V1

## Branch

- Development branch: `v1-route-import`
- Do not write V1 development changes to `main`.
- State verified against GitHub on 2026-09-05.

## Current HEAD

- SHA: `1e70276273c9f396df73d984950d53ab1ee53685`
- Commit: `test(v1): reject off-route GPS at walking start`
- Parent: `0034820fc8983d09a4843d84f458297d3ef9c49c`

## Route

- Production route asset: `app/src/main/assets/data/route.geojson`
- Source declared by the asset: ACF official GPX.
- Source SHA-256 recorded in the GeoJSON: `1159c88bc316f0b73257e2c4d89cf3911ddf2191106609de43763a0bf2999266`.
- Published route distance remains distinct from technical geometry length.
- Historical `ACF_2020` KML remains reference-only.

## Walking V1

Implemented in the current branch:

- preparation produces `PLANNED` state;
- explicit `startSaved(...)` transition to `ACTIVE`;
- first GPS observation establishes the actual start position;
- start is rejected when the projected GPS position is outside the shared possible-deviation threshold;
- route projection through the validated route geometry;
- persistent walking session/checkpoint runtime;
- last reliable position retained during signal loss;
- GPS timestamp/movement protections;
- GPS deviation hysteresis with recovery to `ON_ROUTE`;
- shared `AppState` / `AppStateStore` read model;
- APOI catalogue, filtering, search and next-APOI context;
- APOI detail and decision-support surfaces;
- walking map read model with protection against fabricated geometry.

## Important current architecture

- `V1AppContainer` owns route, published APOI catalogue and persistent walking runtime.
- `AndroidV1AppContainer` is the Android composition boundary.
- `WalkingSessionRuntime` owns the persistent walking lifecycle/checkpoint coordination.
- `WalkingPreparationAppStateController` owns preparation publication and the explicit saved-plan start transition.
- `WalkingAppStateController` bridges runtime/coordinator state into `AppStateStore`.
- `V1MainActivity` owns Android lifecycle and presentation/navigation only.
- `AndroidLocationSource` reports raw device positions; route projection remains in the domain/GPS pipeline.
- `WalkingLocationPipeline` bridges raw positions to route-aware GPS state without Compose/Android policy leakage.
- Persisted Android walking restoration goes through `AndroidV1AppContainer.resumePersistedWalk(...)`; the Activity does not call `runtime.resume()` directly.

## Data

- Published production APOI catalogue is intentionally empty until qualified 2027 evidence exists.
- 2026 data remains historical/reference data and must not be presented as a 2027 guarantee.
- No invented route, stage or APOI data is to be introduced.

## Validation

The current HEAD has completed successfully in CI:

- `Build Android APK` #784 — success.
- `V1 Route Source Provenance` #846 — success after controlled retry of the external official-source capture.
- GPS evaluator and walking-location pipeline tests cover stable route tracking, deviation hysteresis/recovery, weak accuracy, signal loss, implausible jumps, timestamp ordering, malformed observations, future timestamps and prepared-walk GPS start validation.

## Next logical block

1. Consolidate the active walking UX around the GPS read model without adding navigation authority or duplicating domain rules.
2. Ensure GPS state, last reliable position and signal-loss semantics are visible where the pilgrim needs them.
3. Validate the resulting Android build through completed CI.
4. Then move to physical Android GPS validation before offline-map or 2027 APOI expansion.

## Integrity rule

The GitHub branch state is authoritative. Documentation, conversation summaries and transfer prompts never override code/tests actually present in the repository.
