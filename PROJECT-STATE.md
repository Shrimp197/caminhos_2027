# PROJECT STATE — V1

## Branch

- Development branch: `v1-route-import`
- Do not write V1 development changes to `main`.
- State verified against GitHub on 2026-09-04.

## Current HEAD

- SHA: `6fd58669419aebfd0d1031714ee8753f1be55209`
- Commit: `docs(v1): restore authoritative project state`
- Parent: `e7ecbf3281d18be3b1973e4eb7f92fb20ba7d77e`

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

## Known consolidation point

`V1MainActivity` currently restores an active session by calling `runtime.resume()` and then attaching the returned walk manually. `AndroidV1AppContainer` already provides `resumePersistedWalk(...)`, which centralizes this boundary and should be preferred in the next consolidation block. This is a local architectural cleanup, not a product change.

## Data

- Published production APOI catalogue is intentionally empty until qualified 2027 evidence exists.
- 2026 data remains historical/reference data and must not be presented as a 2027 guarantee.
- No invented route, stage or APOI data is to be introduced.

## Validation

Workflows present on `v1-route-import` include:

- `v1-route-import-build.yml`
- `v1-route-import-validation.yml`
- `v1-route-source-provenance.yml`

The branch HEAD currently has no recorded combined status checks available through the GitHub connector. Do not report CI as green without an observed completed workflow run.

## Next logical block

1. Consolidate Android session restoration through `AndroidV1AppContainer.resumePersistedWalk(...)`.
2. Add/strengthen a regression test around restored-session attachment where feasible without introducing Android UI test infrastructure.
3. Validate the branch through the existing JVM/build workflows.
4. Then continue end-to-end UX consolidation rather than adding unrelated features.

## Integrity rule

The GitHub branch state is authoritative. Documentation, conversation summaries and transfer prompts never override code/tests actually present in the repository.
