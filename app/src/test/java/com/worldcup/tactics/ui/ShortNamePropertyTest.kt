package com.worldcup.tactics.ui

// Feature: field-screen-enhancements, Property 8: shortName respects the 10-character cap

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property 8: shortName respects the 10-character cap
 * Validates: Requirements 4.2, 4.7
 */
class ShortNamePropertyTest : FreeSpec({

    "Property 8 - shortName respects the 10-character cap" {
        checkAll(100, Arb.string()) { fullName ->
            val result = shortName(fullName)

            // Result must never exceed 10 characters
            result.length shouldBeLessThanOrEqualTo 10

            // When the base name exceeds 9 chars, result is exactly 10 and ends with ellipsis
            val base = if (' ' in fullName) fullName.substringAfterLast(' ') else fullName
            if (base.length > 9) {
                result.length shouldBe 10
                result.last() shouldBe '…'
            }
        }
    }
})
