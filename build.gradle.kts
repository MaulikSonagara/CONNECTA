// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript{
    dependencies{
        classpath ("com.google.gms:google-services:4.4.2")
    }
}

plugins {
    id("com.android.application") version "8.7.3" apply false

    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    alias(libs.plugins.google.gms.google.services) apply false
}