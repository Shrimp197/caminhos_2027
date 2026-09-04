# PROJECT STATE — V1

## Branch

- Development branch: `v1-route-import`
- Do not write V1 development changes to `main`.
- State verified against GitHub on 2026-09-04.

## Current HEAD

- SHA: `3aa2a118ed94e9735cf74eedae8678362db3b563`
- Commit: `refactor(v1): centralize persisted walking restoration`
- Parent: `84b078223fe0e61338073c34d6b9ce212e4acb54`

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
- route projection through the validated route geometry;
- persistent walking session/checkpoint runtime;
- last reliable position retained during signal loss;
- GPS timestamp/movement protections;
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
- Persisted Android walking restoration now goes through `AndroidV1AppContainer.resumePersistedWalk(...)`; the Activity no longer calls `runtime.resume()` directly.

## Data

- Published production APOI catalogue is intentionally empty until qualified 2027 evidence exists.
- 2026 data remains historical/reference data and must not be presented as a 2027 guarantee.
- No invented route, stage or APOI data is to be introduced.

## Validation

Workflows present on `v1-route-import` include:

- `v1-route-import-build.yml`
- `v1-route-import-validation.yml`
- `v1-route-source-provenance.yml`

CI for the current restoration-consolidation commit must be reported only from completed workflow results.

## Next logical block

1. Validate the current Activity/container consolidation through completed JVM/build workflows.
2. Consolidate the preparation/start UX only where it improves clarity without changing the explicit `PLANNED → ACTIVE` contract.
3. Continue end-to-end UX consolidation and then physical Android GPS validation.

## Integrity rule

The GitHub branch state is authoritative. Documentation, conversation summaries and transfer prompts never override code/tests actually present in the repository.
