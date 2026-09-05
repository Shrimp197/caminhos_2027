# PROJECT STATE — V1

## Branch

- Development branch: `v1-route-import`
- Do not write V1 development changes to `main`.
- State verified against GitHub on 2026-09-05.

## Current functional baseline

- Functional baseline SHA: `943c3b70ba8ab3149ae8ffc5ec94d63c78a75275`
- Commit: `test(v1): correct QA GPS simulation timestamp assertion`
- The baseline includes the validated QA route-selection and GPX simulation changes described below.

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

## QA route execution

- Walking preparation now exposes three explicit route choices: production `Caminho do Centenário`, `SR` and `HF`.
- `SR` and `HF` are explicitly marked as test environments in the preparation UI.
- QA route geometry is loaded from the committed GPX assets `percurso-teste-casa-trabalho.gpx` and `percurso-teste-hf.gpx`.
- GPX parsing is isolated in `AssetGpxRouteDataSource` / `GpxRouteParser`; invalid or insufficient GPX geometry is rejected.
- QA simulation is implemented as `GpxSimulationLocationSource`, a raw-position source only.
- Real Android GPS and QA simulation enter the same downstream walking callback/pipeline; route projection, validation and walking-state rules are not duplicated for QA.
- QA stepping is controlled separately from the raw location-source abstraction and does not replace the real Android location source.
- Test route identifiers are explicit and are not used as production APOI data or as the production route default.
- Test fixtures are labelled `TEST/FICTITIOUS` and remain isolated from production datasets.

## Important current architecture

- `V1AppContainer` owns route, published APOI catalogue and persistent walking runtime.
- `AndroidV1AppContainer` is the Android composition boundary and can select the route explicitly for QA execution.
- `WalkingSessionRuntime` owns the persistent walking lifecycle/checkpoint coordination.
- `WalkingPreparationAppStateController` owns preparation publication and the explicit saved-plan start transition.
- `WalkingAppStateController` bridges runtime/coordinator state into `AppStateStore`.
- `V1MainActivity` owns Android lifecycle and presentation/navigation only; QA route stepping is delegated to `GpxSimulationLocationSource`.
- `AndroidLocationSource` reports raw device positions; route projection remains in the domain/GPS pipeline.
- `GpxSimulationLocationSource` reports raw simulated positions only; it does not calculate route progress or deviation.
- `WalkingLocationPipeline` bridges raw positions to route-aware GPS state without Compose/Android policy leakage.
- Persisted Android walking restoration goes through `AndroidV1AppContainer.resumePersistedWalk(...)`; the Activity does not call `runtime.resume()` directly.

## Active walking UX

- `WalkingExperienceScreenV1` consumes `AppState.walking` as the single read model.
- GPS state is presented as information rather than navigation authority.
- Signal acquisition, `ON_ROUTE`, possible/probable deviation and loss-of-signal semantics are represented by the walking read model; the UI does not reproduce GPS thresholds or hysteresis.
- The active surface keeps the projected position and walking progress visible and leaves the walking decision with the pilgrim.

## Data

- Published production APOI catalogue is intentionally empty until qualified 2027 evidence exists.
- 2026 data remains historical/reference data and must not be presented as a 2027 guarantee.
- No invented route, stage or APOI data is to be introduced.
- SR/HF GPX assets are QA inputs and must not enter production datasets.

## Validation

- `Build Android APK` #810 — success for commit `943c3b70ba8ab3149ae8ffc5ec94d63c78a75275`.
- Build job completed all validation steps, including JVM tests, debug APK build, APK verification and artifact upload.
- `V1 Route Source Provenance` #898 — success for commit `943c3b70ba8ab3149ae8ffc5ec94d63c78a75275`.
- The generated debug artifact is `Caminhos-do-Peregrino-v1-route-import-debug`; artifact SHA-256: `3555830d229d7b64ac2451fd2f0ca1eb9e4f62967015f6a969b553633416d61c`.
- JVM test suite passed on the validated build; the earlier QA simulation timestamp assertion failure was corrected before this baseline.
- Tests cover route parsing/catalog exposure, QA raw-position emission, walking GPS pipeline behaviour, walking/APOI/decision integration, persistence/restoration and production-data isolation constraints.
- Physical Android GPS behaviour has not yet been validated in this baseline.

## Next logical block

1. Install the validated debug APK on the physical Android device and validate the preparation menu for production/SR/HF.
2. Exercise SR and HF from `PLANNED` through first simulated GPS fix, route projection, progress updates and controlled stepping.
3. Validate persisted active-walk restoration and route selection after Activity/process recreation.
4. Validate the real production route start with physical GPS while off-route first, then on-route; confirm no crash and clear state transition.
5. Validate signal loss, recovery and possible/probable deviation against real device behaviour.
6. Record only observed defects and apply targeted corrections on `v1-route-import`.
7. Re-run CI after any functional correction.

## Integrity rule

The GitHub branch state is authoritative. Documentation, conversation summaries and transfer prompts never override code/tests actually present in the repository.
