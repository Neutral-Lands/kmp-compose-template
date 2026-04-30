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
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics.plugin) apply false
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
// Usage: ./gradlew :shared:jvmTest koverHtmlReport koverXmlReport
dependencies {
    kover(project(":shared"))
}

// Class-name patterns excluded from all reports (dot notation, * = any chars).
// These packages have no unit-test path and would skew the threshold:
//   presentation.shared  — Composable UI, manually tested on device/simulator
//   data.connectivity    — platform stubs (Android/iOS/Wasm), not in jvmTest classpath
//   generated.resources  — SQLDelight / Compose codegen
val koverExcludedClasses = listOf(
    "com.nouri.presentation.shared.*",
    "com.nouri.data.connectivity.*",
    "nouri.shared.generated.resources.*",
)

kover {
    reports {
        total {
            // HTML report: ./gradlew :shared:jvmTest koverHtmlReport → build/reports/kover/html/index.html
            html { onCheck = false }
            // XML report parsed by CI Python script — apply same exclusions so the
            // reported percentage reflects testable code only.
            filters {
                excludes {
                    classes(*koverExcludedClasses.toTypedArray())
                }
            }
        }
    }
}

tasks.register("detektAll") {
    group = "verification"
    description = "Run Detekt on all modules"
    dependsOn(":shared:detekt", ":androidApp:detekt")
}
