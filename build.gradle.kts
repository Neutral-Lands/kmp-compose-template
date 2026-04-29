import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.kover)
}

// Apply ktlint to all subprojects — generated source exclusion handled per-module
subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.5.0")
        android.set(true)
        outputToConsole.set(true)
    }
}

// Aggregate coverage from all modules into a single root report.
// shared:jvmTest must run first to populate coverage data (JVM target added in shared).
// Usage: ./gradlew :shared:jvmTest koverHtmlReport
dependencies {
    kover(project(":shared"))
}

kover {
    reports {
        verify {
            // Thresholds enforced in CI (NEU-18) — not called on local build
            rule {
                bound {
                    minValue = 70
                    coverageUnits = CoverageUnit.LINE
                    aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                }
            }
        }
        total {
            // Single merged HTML report: ./gradlew :shared:jvmTest koverHtmlReport
            // Output: build/reports/kover/html/index.html
            html { onCheck = false }
        }
    }
}

tasks.register("detektAll") {
    group = "verification"
    description = "Run Detekt on all modules"
    dependsOn(":shared:detekt", ":androidApp:detekt")
}
