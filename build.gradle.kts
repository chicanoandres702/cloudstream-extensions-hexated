import com.lagradost.cloudstream3.gradle.CloudstreamExtension
import com.android.build.gradle.BaseExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.kotlin.dsl.*
import org.gradle.api.tasks.Delete

buildscript {
    repositories {
        google()
        mavenCentral()
        // Shitpack repo which contains our tools and dependencies
        maven("https://jitpack.io")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.3.0")
        // Cloudstream gradle plugin which makes everything work and builds plugins
        classpath("com.github.recloudstream:gradle:-SNAPSHOT")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

fun Project.cloudstream(configuration: CloudstreamExtension.() -> Unit) =
    extensions.getByName<CloudstreamExtension>("cloudstream").configuration()

fun Project.android(configuration: BaseExtension.() -> Unit) =
    extensions.getByName<BaseExtension>("android").configuration()

subprojects {
    apply(plugin = "com.android.library")
    apply(plugin = "kotlin-android")
    apply(plugin = "com.lagradost.cloudstream3.gradle")

    cloudstream {
        // when running through github workflow, GITHUB_REPOSITORY should contain current repository name
        setRepo(
            System.getenv("GITHUB_REPOSITORY")
                ?: "https://github.com/hexated/cloudstream-extensions-hexated"
        )

        authors = listOf("Hexated")
    }

    android {
        defaultConfig {
            minSdk = 21
            compileSdkVersion(33)
            targetSdk = 33
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        tasks.withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = "17" // Required
                // Disables some unnecessary features
                freeCompilerArgs = freeCompilerArgs +
                        "-Xno-call-assertions" +
                        "-Xno-param-assertions" +
                        "-Xno-receiver-assertions"
            }
        }
    }

    dependencies {
        val apk: Configuration by configurations
        val implementation: Configuration by configurations

        // Stubs for all Cloudstream classes
        apk("com.lagradost:cloudstream3:pre-release")

        // these dependencies can include any of those which are added by the app,
        // but you dont need to include any of them if you dont need them
        // https://github.com/recloudstream/cloudstream/blob/master/app/build.gradle
        implementation("com.example:conflictingLibrary:1.0") {
            exclude(group = "com.fasterxml.jackson.core", module = "jackson-core")
        }
        implementation(kotlin("stdlib")) // adds standard kotlin features, like listOf, mapOf etc
        implementation("com.github.Blatzar:NiceHttp:0.4.11") // http library
        implementation("org.jsoup:jsoup:1.17.2") // html parser

        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")

        implementation("io.karn:khttp-android:0.1.2")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
        implementation("org.mozilla:rhino:1.7.14") //run JS

        implementation("com.fasterxml.jackson.core:jackson-core:2.13.0")
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
