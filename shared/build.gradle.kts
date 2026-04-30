import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kover)
    kotlin("native.cocoapods")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    android {
        namespace = "com.neutrallands.nouri.shared"
        compileSdk = 36
        minSdk = 26
    }

    jvm() // JVM target so commonTest runs on JVM and Kover can measure coverage

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("shared")
        browser {
            commonWebpackConfig {
                outputFileName = "shared.js"
            }
        }
        binaries.executable()
    }

    cocoapods {
        summary = "Nouri shared module"
        homepage = "https://nouri.app"
        version = "1.0"
        ios.deploymentTarget = "14.0"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.koin.core)
            implementation(libs.supabase.auth)
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.storage)
            implementation(libs.supabase.realtime)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.sqldelight.driver.android)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.driver.native)
        }
        jvmMain.dependencies {
            implementation(libs.sqldelight.driver.jdbc)
            implementation(libs.ktor.client.cio)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}

detekt {
    config.setFrom(rootProject.file("detekt/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
    source.setFrom(
        "src/commonMain/kotlin",
        "src/androidMain/kotlin",
        "src/iosMain/kotlin",
        "src/wasmJsMain/kotlin",
    )
}

// Kover: exclude packages with no unit-test path and enforce 70% line coverage.
// Coverage verify runs via :shared:koverVerify (also called by CI).
// Excluded:
//   presentation.shared  — Composable UI, verified manually on device/simulator
//   data.connectivity    — platform stubs (Android/iOS/Wasm), not in jvmTest classpath
//   generated.resources  — SQLDelight / Compose codegen
kover {
    reports {
        filters {
            excludes {
                classes(
                    "com.neutrallands.nouri.presentation.shared.*",
                    "com.neutrallands.nouri.data.connectivity.*",
                    "nouri.shared.generated.resources.*",
                )
            }
        }
        verify {
            rule {
                bound {
                    minValue = 70
                    coverageUnits = CoverageUnit.LINE
                    aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                }
            }
        }
    }
}

sqldelight {
    databases {
        create("NouriDatabase") {
            packageName.set("com.neutrallands.nouri.data.local")
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/schema"))
            verifyMigrations.set(true)
        }
    }
}

// Fix: ktlint per-source-set KMP tasks include generated files (Compose resources,
// SQLDelight) because KtlintExtension.filter{} does not propagate to those tasks.
//
// The Kotlin DSL resolves `source` as FileTree (via a SourceTask DSL extension),
// hiding the backing ConfigurableFileCollection and its setFrom() method. We access
// the ConfigurableFileCollection via Java reflection and replace it with an explicit
// fileTree("src") so only hand-written code is linted. All generated files live under
// the project build directory; src/ contains only hand-written source sets.
afterEvaluate {
    val handWrittenSources = fileTree("src") { include("**/*.kt", "**/*.kts") }
    fun patchKtlintSource(task: Task) {
        val src = runCatching {
            task.javaClass.getMethod("getSource").invoke(task)
                as? org.gradle.api.file.ConfigurableFileCollection
        }.getOrNull() ?: return
        src.setFrom(handWrittenSources)
    }
    tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask>().configureEach {
        patchKtlintSource(this)
    }
    tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.KtLintFormatTask>().configureEach {
        patchKtlintSource(this)
    }
}
