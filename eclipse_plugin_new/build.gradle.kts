plugins {
    kotlin("jvm") version "1.9.22"
    id("com.diffplug.eclipse.mavencentral") version "3.37.0"
}

repositories {
    mavenCentral()
}

eclipseMavenCentral {
    release("4.24.0") {
        implementation("org.eclipse.swt")
        useNativesForRunningPlatform()
        constrainTransitivesToThisRelease()
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.eclipse.platform:org.eclipse.core.runtime:3.29.0")
    implementation("org.eclipse.platform:org.eclipse.ui:3.200.0")
    implementation("org.eclipse.platform:org.eclipse.core.resources:3.20.0")
    implementation("org.eclipse.platform:org.eclipse.core.net:1.5.800")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(kotlin("reflect"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("org.mockito:mockito-core:5.10.0")
}

tasks.jar {
    manifest {
        attributes(
            "Bundle-ManifestVersion" to "2",
            "Bundle-Name" to "Weibo Agent for Eclipse",
            "Bundle-SymbolicName" to "com.sina.weibo.agent;singleton:=true",
            "Bundle-Version" to "1.0.0.qualifier",
            "Bundle-Activator" to "com.sina.weibo.agent.Activator",
            "Bundle-RequiredExecutionEnvironment" to "JavaSE-17",
            "Require-Bundle" to "org.eclipse.ui,org.eclipse.core.runtime",
            "Bundle-ActivationPolicy" to "lazy"
        )
    }
}

tasks.register<Zip>("packagePlugin") {
    from(tasks.jar)
    from(file("plugin.xml"))
}
