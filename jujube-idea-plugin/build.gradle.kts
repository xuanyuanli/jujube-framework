plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "cn.xuanyuanli.ideaplugin"
version = "2025.1.0"

repositories {
    maven {
        url = uri("https://maven.aliyun.com/repository/public")
    }
    mavenCentral()
}

dependencies {
    implementation("cn.xuanyuanli:jujube-jdbc:3.1.1") {
        exclude(group = "cn.xuanyuanli", module = "jujube-core-json")
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2024.1.7")
    type.set("IU") // Target IDE Platform

    // "com.intellij.jsp", "com.intellij.javaee.el", "com.intellij.spring.mvc",
    plugins.set(listOf("com.intellij.java", "com.intellij.database", "com.intellij.freemarker", "org.intellij.plugins.markdown"))
    updateSinceUntilBuild.set(false)
}

configurations.all {
    resolutionStrategy {
        cacheChangingModulesFor(0, "seconds")
    }
}


tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        options.encoding = "UTF-8"
    }

    patchPluginXml {
        sinceBuild.set("241")
        untilBuild.set("399.*")
    }

    printBundledPlugins
}
