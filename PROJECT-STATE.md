# PROJECT STATE — V1

## Branch

- Development branch: `v1-route-import`
- Do not write V1 development changes to `main`.
- State verified against GitHub on 2026-09-06.

## Current functional baseline

- Current functional HEAD: `86dc3d6156386cfb6a11de103de27cfca8bd7ccd`
- Recent commits:
  - `7fcd18ce925d35a73bf05aec0027871a13dc6070` — `build(v1): enable BuildConfig for debug QA isolation`
  - `639a7600b164f8c69ac1980eca9ba34b37dbade1` — `test(v1): add deterministic QA GPS loss and deviation controls`
  - `86dc3d6156386cfb6a11de103de27cfca8bd7ccd` — `test(v1): cover controlled QA GPS loss recovery and deviation`
- The branch now enforces debug-only QA route/catalog access and has deterministic raw GPS simulation controls for signal loss/recovery and deliberate deviation testing.

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
- QA simulation now supports deterministic temporary signal loss/recovery and one deliberate off-route raw-position emission for GPS deviation QA.
- Real Android GPS and QA simulation enter the same downstream walking callback/pipeline; route projection, validation and walking-state rules are not duplicated for QA.
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
- `GpxSimulationLocationSource` reports raw simulated positions only; it does not calculate route progress, deviation or navigation instructions.
- `WalkingLocationPipeline` bridges raw positions to route-aware GPS state without Compose/Android policy leakage.
- Persisted Android walking restoration goes through `AndroidV1AppContainer.resumePersistedWalk(...)`; the Activity does not call `runtime.resume()` directly.

## Active walking UX

- `WalkingExperienceScreenV1` consumes `AppState.walking` as the single read model.
- GPS state is presented as information rather than navigation authority.
- Signal acquisition, `ON_ROUTE`, possible/probable deviation and loss-of-signal semantics are represented by the walking read model; the UI does not reproduce GPS thresholds or hysteresis.
- The active surface keeps the projected position and walking progress visible and leaves the walking decision with the pilgrim.
- Progress is calculated against the actual walking start and planned destination route km; it is never recalculated from official stage execution.

## Data

- Published production APOI catalogue is intentionally empty until qualified 2027 evidence exists.
- 2026 data remains historical/reference data and must not be presented as a 2027 guarantee.
- No invented route, stage or APOI data is to be introduced.
- SR/HF GPX assets are QA inputs and must not enter production datasets.
- Debug-only QA APOI selection uses `data/qa/apoi-qa.json`; production composition uses `data/published/apoi-production.json`.

## Validation

- `V1 Route Source Provenance` #956 — success for `7fcd18ce925d35a73bf05aec0027871a13dc6070`.
- `Build Android APK` #839 — success for `7fcd18ce925d35a73bf05aec0027871a13dc6070`.
- JVM test suite passed on the validated build for the debug isolation change.
- New deterministic QA simulation tests cover availability loss/recovery, frozen emission during loss, resume from the last route point, deliberate deviation raw-position emission, and invalid deviation parameters.
- The current HEAD `86dc3d6156386cfb6a11de103de27cfca8bd7ccd` triggered fresh CI; `V1 Route Source Provenance` completed successfully and `Build Android APK` was still running at the time of this state synchronization.
- Physical Android GPS behaviour has not yet been revalidated after the latest QA controls; it remains human/device validation.

## Next logical block

1. Finish and verify the fresh `Build Android APK` run for the current HEAD.
2. Expose the deterministic QA signal-loss/recovery and deviation controls in the debug walking surface so SR/HF can exercise the complete GPS-state flow without editing code.
3. Add or execute the end-to-end QA scenario: planned non-zero start → first GPS → progress → APOI ahead → APOI detail → back → decision → signal loss/recovery → deviation/recovery → persistence.
4. Re-run the physical Android test of Centenário outside-route guidance and on-route start after the new APK is produced.
5. Keep navigation turn instructions and external map handoff deferred until their geometry/data prerequisites are validated.

## Integrity rule

The GitHub branch state is authoritative. Documentation, conversation summaries and transfer prompts never override code/tests actually present in the repository.
