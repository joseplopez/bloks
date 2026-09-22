import com.android.build.api.dsl.ApplicationExtension

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

configure<ApplicationExtension> {
    namespace = "com.centelles.bloks"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.centelles.bloks"
        minSdk = 26
        targetSdk = 37
        versionCode = 2
        versionName = "0.1"

        testInstrumentationRunner = "com.centelles.bloks.CustomTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("Untitled.jks")
            storePassword = "123456789"
            keyAlias = "key0"
            keyPassword = "123456789"
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            buildConfigField("String", "BANNER_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField("String", "INTERSTITIAL_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "REWARDED_ID", "\"ca-app-pub-3940256099942544/5224354917\"")
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("String", "BANNER_ID", "\"ca-app-pub-9749336798654274/8844552863\"")
            buildConfigField("String", "INTERSTITIAL_ID", "\"ca-app-pub-9749336798654274/3561717854\"")
            buildConfigField("String", "REWARDED_ID", "\"ca-app-pub-9749336798654274/7315848776\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.datastore.preferences)
    
    // Services
    implementation(libs.google.ads)
    implementation(libs.google.ump)
    implementation(libs.google.billing)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.compose.uitest.junit4)
    debugImplementation(libs.compose.uitest.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    
    // Hilt Testing
    androidTestImplementation(libs.dagger.hilt.testing)
    kaptAndroidTest(libs.hilt.compiler)
}
