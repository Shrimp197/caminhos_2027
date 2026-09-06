# PROJECT STATE — V1

## Branch

- Development branch: `v1-route-import`
- Do not write V1 development changes to `main`.
- State verified against GitHub on 2026-09-06.

## Current functional baseline

- Current functional HEAD: `828590fab93af3aaf66d3f612f9f348c16bd7864`
- Recent commits:
  - `639a7600b164f8c69ac1980eca9ba34b37dbade1` — `test(v1): add deterministic QA GPS loss and deviation controls`
  - `86dc3d6156386cfb6a11de103de27cfca8bd7ccd` — `test(v1): cover controlled QA GPS loss recovery and deviation`
  - `828590fab93af3aaf66d3f612f9f348c16bd7864` — `feat(v1): expose QA GPS loss and deviation controls`
- The branch enforces debug-only QA route/catalog access, deterministic raw GPS simulation controls, and visible debug walking controls for signal loss/recovery and deliberate deviation testing.

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

## Preparation and QA route execution

- Walking preparation exposes three explicit choices in debug builds: production `Caminho do Centenário`, `SR` and `HF`.
- `SR` and `HF` are explicitly marked as test environments in the preparation UI.
- Preparation allows an explicit planned start and destination route km inside the selected route geometry.
- Saving the plan remains distinct from starting the walk; saved plans remain `PLANNED` until a valid GPS position starts the walk.
- QA route geometry is loaded from the committed GPX assets `percurso-teste-casa-trabalho.gpx` and `percurso-teste-hf.gpx`.
- GPX parsing is isolated in `AssetGpxRouteDataSource` / `GpxRouteParser`; invalid or insufficient GPX geometry is rejected.
- QA simulation is implemented as `GpxSimulationLocationSource`, a raw-position source only.
- QA simulation starts at the route point nearest the planned start km so SR/HF can exercise non-zero planned starts.
- QA simulation supports deterministic temporary signal loss/recovery and one deliberate off-route raw-position emission for GPS deviation QA.
- The debug walking surface exposes `Avançar no percurso`, `Perder GPS`, `Recuperar GPS` and `Simular desvio` controls.
- Real Android GPS and QA simulation enter the same downstream walking callback/pipeline; route projection, validation and walking-state rules are not duplicated for QA.
- Test route identifiers are explicit and are not used as production APOI data or as the production route default.
- Test fixtures are labelled `TEST/FICTITIOUS` and remain isolated from production datasets.

## Important current architecture

- `V1AppContainer` owns route, published APOI catalogue and persistent walking runtime.
- `AndroidV1AppContainer` is the Android composition boundary and can select the route explicitly for QA execution.
- `WalkingSessionRuntime` owns the persistent walking lifecycle/checkpoint coordination.
- `WalkingPreparationAppStateController` owns preparation publication and the explicit saved-plan start transition.
- `WalkingAppStateController` bridges runtime/coordinator state into `AppStateStore`.
- `V1MainActivity` owns Android lifecycle and presentation/navigation only; QA controls delegate actions to `GpxSimulationLocationSource` and the shared walking controller.
- `AndroidLocationSource` reports raw device positions; route projection remains in the domain/GPS pipeline.
- `GpxSimulationLocationSource` reports raw simulated positions only; it does not calculate route progress, deviation or navigation instructions.
- `WalkingLocationPipeline` bridges raw positions to route-aware GPS state without Compose/Android policy leakage.
- Persisted Android walking restoration goes through `AndroidV1AppContainer.resumePersistedWalk(...)`; the Activity does not call `runtime.resume()` directly.

## Active walking UX

- `WalkingExperienceScreenV1` consumes `AppState.walking` as the single read model.
- GPS state is presented as information rather than navigation authority.
- Signal acquisition, `ON_ROUTE`, possible/probable deviation and loss-of-signal semantics are represented by the walking read model; the UI does not reproduce GPS thresholds or hysteresis.
- The active surface keeps the projected position and walking progress visible and leaves the walking decision with the pilgrim.
- Progress is calculated against the actual walking start and planned destination route km; it is never recalculated from official stage execution.
- In debug QA routes, the active surface can deliberately exercise signal loss/recovery and deviation without bypassing the shared GPS/state path.

## Data

- Published production APOI catalogue is intentionally empty until qualified 2027 evidence exists.
- 2026 data remains historical/reference data and must not be presented as a 2027 guarantee.
- No invented route, stage or APOI data is to be introduced.
- SR/HF GPX assets are QA inputs and must not enter production datasets.
- Debug-only QA APOI selection uses `data/qa/apoi-qa.json`; production composition uses `data/published/apoi-production.json`.

## Validation

- `V1 Route Source Provenance` #964 — success for `828590fab93af3aaf66d3f612f9f348c16bd7864`.
- `Build Android APK` #843 — was still running after JVM tests entered execution; final completion must be observed before claiming a green build or publishing a new APK.
- The prior debug isolation change has a successful provenance/build baseline.
- Deterministic QA simulation tests cover availability loss/recovery, frozen emission during loss, resume from the last route point, deliberate deviation raw-position emission, and invalid deviation parameters.
- The QA UI change itself is a small wiring/presentation change; its correctness still requires the fresh Android build to complete and the QA flow to be exercised.
- Physical Android GPS behaviour has not yet been revalidated after the latest QA controls; it remains human/device validation.

## Next logical block

1. Finish and verify `Build Android APK` #843 for the QA-control commit.
2. Execute the complete SR/HF QA scenario: planned non-zero start → first GPS → progress → APOI ahead → APOI detail → back → decision → signal loss/recovery → deviation/recovery → persistence.
3. Produce and verify the new debug APK only after the build succeeds; record its artifact/hash in the project state.
4. Re-run the physical Android test of Centenário outside-route guidance and on-route start with the new APK.
5. Keep navigation turn instructions and external map handoff deferred until their geometry/data prerequisites are validated.

## Integrity rule

The GitHub branch state is authoritative. Documentation, conversation summaries and transfer prompts never override code/tests actually present in the repository.
