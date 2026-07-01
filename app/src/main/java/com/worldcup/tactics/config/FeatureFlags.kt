package com.worldcup.tactics.config

/**
 * Central feature flags.
 *
 * Flip [USE_REMOTE_TEAM_IMAGES] to true when the API rate limits allow it
 * (requires a paid SportsDB key or a key with higher rate limits).
 * When false, team cards show a flag emoji instead of a network image,
 * and the parallel SportsDB enrichment calls in TeamRepositoryImpl are skipped.
 */
object FeatureFlags {

    /**
     * true  → fetch strBadge / strBanner from SportsDB for every team (48 parallel calls)
     * false → show flag emoji, no extra network calls
     */
    const val USE_REMOTE_TEAM_IMAGES: Boolean = false
}
