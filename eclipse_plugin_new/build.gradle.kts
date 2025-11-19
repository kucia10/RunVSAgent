plugins {
    kotlin("jvm") version "1.9.22"
    id("com.diffplug.eclipse.apt") version "3.36.0"
    id("com.diffplug.eclipse.mavencentral") version "3.36.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

sourceSets {
    main {
        kotlin {
            srcDirs("src")
        }
    }
}

eclipseMavenCentral {
    release("4.22.0") {
        implementation("org.eclipse.ui")
        implementation("org.eclipse.core.runtime")
    }
}
