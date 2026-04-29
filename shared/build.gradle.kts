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
        namespace = "com.nouri.shared"
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

sqldelight {
    databases {
        create("NouriDatabase") {
            packageName.set("com.nouri.data.local")
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/schema"))
            verifyMigrations.set(true)
        }
    }
}

// Disable ktlint source-set tasks that scan Gradle-generated code (Compose, SQLDelight).
// They cannot be filtered via KtlintExtension because the plugin bypasses the filter
// for per-source-set tasks. Hand-written sources are covered by ktlintKotlinScriptCheck
// and the androidApp module's ktlint tasks.
afterEvaluate {
    tasks.matching { task ->
        task.name.startsWith("runKtlintCheck") ||
            task.name.startsWith("runKtlintFormat") ||
            (task.name.startsWith("ktlint") && task.name.contains("SourceSet"))
    }.configureEach { enabled = false }
}
