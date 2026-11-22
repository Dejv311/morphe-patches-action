import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test

extension {
    name = "extensions/extension.mpe"
}

android {
    namespace = "app.action.extension"
}

// The pixel-only regression tests do not need an Android variant or emulator.
val qrCodeTestDependencies by configurations.creating {
    isCanBeConsumed = false
}

dependencies {
    add(qrCodeTestDependencies.name, "junit:junit:4.13.2")
    add(qrCodeTestDependencies.name, "com.google.zxing:core:3.5.3")
}

val compileQrCodeQuietZoneTests by tasks.registering(JavaCompile::class) {
    source("src/main/java/app/action/extension/QrCodeQuietZone.java", "src/test/java")
    classpath = qrCodeTestDependencies
    destinationDirectory.set(layout.buildDirectory.dir("qrQuietZoneTest/classes"))
    options.release.set(11)
}

tasks.register<Test>("testQrCodeQuietZone") {
    description = "Verify QR quiet zones and decode the resulting codes without Android."
    group = "verification"
    dependsOn(compileQrCodeQuietZoneTests)
    testClassesDirs = files(compileQrCodeQuietZoneTests.flatMap { it.destinationDirectory })
    classpath = testClassesDirs + qrCodeTestDependencies
    useJUnit()
    filter.includeTestsMatching("app.action.extension.QrCodeQuietZoneTest")
}
