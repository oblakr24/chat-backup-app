plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

android {
    namespace = "com.rokoblak.chatbackup"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.rokoblak.chatbackup"
        minSdk = 29
        targetSdk = 34
        versionCode = 7
        versionName = "1.0.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeHiltNavigationVersion = "1.2.0"
    val navVersion = "2.7.7"
    val composeUiVersion = "1.6.3"
    val timberVersion = "5.0.1"

    // Core/activity/lifecycle
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.49")
    kapt("com.google.dagger:hilt-compiler:2.49")

    // Compose
    implementation("androidx.compose.ui:ui:$composeUiVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeUiVersion")
    implementation("androidx.compose.material:material:1.6.3")
    // Compose constraint layout
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")
    // Compose tooling
    debugImplementation("androidx.compose.ui:ui-tooling:$composeUiVersion")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$composeUiVersion")
    // Compose Navigation
    implementation("androidx.navigation:navigation-compose:$navVersion")
    implementation("androidx.hilt:hilt-navigation-compose:$composeHiltNavigationVersion")
    // Compose permissions
    implementation("com.google.accompanist:accompanist-permissions:0.35.0-alpha")
    // Compose extended material icons
    implementation("androidx.compose.material:material-icons-extended:1.6.3")
    // Compose Material 3
    implementation(platform("androidx.compose:compose-bom:2024.02.02"))
    implementation("androidx.compose.material3:material3")
    // Coil
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Timber
    implementation("com.jakewharton.timber:timber:$timberVersion")

    // Datastore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // KotlinX immutable collections
    api("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")

    // KotlinX Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    // SMS-MMS parsing lib, used for MMS
    implementation("com.klinkerapps:android-smsmms:5.2.6")

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeUiVersion")
}

kapt {
    correctErrorTypes = true
}

apply {
    plugin("app.cash.molecule")
}