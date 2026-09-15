package com.caminhos2027.v1.core.model

/** User choices made before a walk starts. Kept with the walk so the same plan drives every surface. */
data class WalkingPreparationConfig(
    val audioMode: AudioMode = AudioMode.NORMAL,
    val mapOrientation: MapOrientation = MapOrientation.NORTH,
    val intelligentBreaksEnabled: Boolean = true,
    val customBreakTimeMinutes: Int? = null,
    val customBreakDistanceKm: Double? = null,
    val visibleApoiCategories: Set<ApoiCategory> = emptySet(),
    val notes: List<String> = emptyList()
)

enum class AudioMode { NORMAL, IMMERSIVE, SILENT }

enum class MapOrientation { NORTH, WALK_DIRECTION }
