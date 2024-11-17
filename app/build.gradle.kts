plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.mapalocalizacaoapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mapalocalizacaoapp"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}") // Corrigido aqui
        }
    }
}

dependencies {
    // Dependências principais
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)

    // Dependências do Google Play Services
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-location:18.0.0")

    // Testes
    implementation(libs.ui.test.junit4)
    implementation(libs.androidx.ui.test.android.v175)
    implementation(libs.androidx.espresso.core)

    testImplementation(libs.junit)

    // Forçar a versão do JUnit para resolver conflitos
    implementation("androidx.test.ext:junit:1.2.1")

    // Testes de Instrumentação
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Forçar a versão do JUnit para testes Android
    androidTestImplementation("androidx.test.ext:junit:1.2.1")

    // Debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// Forçar a resolução da versão 1.2.1 do JUnit em todas as configurações de dependência
configurations.all {
    resolutionStrategy {
        force("androidx.test.ext:junit:1.2.1")
    }
}