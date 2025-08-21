plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.7.2"
}

group = "cn.xuanyuanli.ideaplugin"
version = "2025.1.0"

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.aliyun.com/repository/public")
    }
    
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("cn.xuanyuanli:jujube-jdbc:3.1.1") {
        exclude(group = "org.slf4j")
    }
    
    intellijPlatform {
        intellijIdeaUltimate("2024.2")
        
        bundledPlugin("com.intellij.java")
        bundledPlugin("com.intellij.database")
        bundledPlugin("com.intellij.freemarker")
        bundledPlugin("org.intellij.plugins.markdown")
        
        pluginVerifier()
        zipSigner()
    }
    
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
}

// Configure IntelliJ Platform Gradle Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "242"
            untilBuild = "399.*"
        }
    }
}

configurations.all {
    resolutionStrategy {
        cacheChangingModulesFor(0, "seconds")
    }
}


tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
        options.encoding = "UTF-8"
    }
    
    test {
        useJUnitPlatform()
    }
}
