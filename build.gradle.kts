import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    kotlin("jvm") version("1.8.21")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.runelite.net")
    }
    gradlePluginPortal()
    mavenCentral()
}

val runeLiteVersion = "1.10.16.1"

dependencies {

    compileOnly(group = "com.example", name = "example", version = "5.4")
    compileOnly("org.projectlombok:lombok:1.18.20")
    implementation("com.google.inject.extensions:guice-multibindings:4.1.0")
    compileOnly("net.runelite:client:$runeLiteVersion+")
    compileOnly("org.pf4j:pf4j:3.6.0")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
    testImplementation("junit:junit:4.13.1")
}

group = "com.lucidplugins"
version = "5.5.6"

val javaMajorVersion = JavaVersion.VERSION_11.majorVersion

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = javaMajorVersion
        targetCompatibility = javaMajorVersion
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = javaMajorVersion
    }
    withType<Jar> {
        manifest {

        }
    }
    withType<ShadowJar> {
        baseName = "LucidPlugins"

        //exclude("com/lucidplugins/disablerendering/**")
        //exclude("com/lucidplugins/lucidcannonreloader/**")
        //exclude("com/lucidplugins/lucidcombat/**")
        //exclude("com/lucidplugins/lucidcustomprayers/**")
        //exclude("com/lucidplugins/luciddiscordlogger/**")
        //exclude("com/lucidplugins/lucidgauntlet/**")
        //exclude("com/lucidplugins/lucidgearswapper/**")
        //exclude("com/lucidplugins/lucidhotkeys/**")
        //exclude("com/lucidplugins/lucidmuspah/**")
        //exclude("com/lucidplugins/api/**")
        exclude("com/lucidplugins/toa/**")
    }
}
