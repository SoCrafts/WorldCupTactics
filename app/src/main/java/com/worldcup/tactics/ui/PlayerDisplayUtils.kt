package com.worldcup.tactics.ui

/**
 * Returns the display name for a player label:
 *  - Last token of [fullName] if it contains a space.
 *  - [fullName] itself otherwise.
 * Result is capped at 10 characters; if longer, truncated to 9 + "…".
 */
fun shortName(fullName: String): String {
    val base = if (' ' in fullName) fullName.substringAfterLast(' ') else fullName
    return if (base.length > 9) base.take(9) + "…" else base
}
