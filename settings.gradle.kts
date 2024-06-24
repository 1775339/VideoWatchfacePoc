pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven { url=uri("https://jitpack.io") }
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven{ url=uri("https://maven.aliyun.com/repository/public") }
        maven{url=uri("https://maven.aliyun.com/repository/google")}
        maven { url =uri("https://jitpack.io") }
       /* maven {
            url = uri("http://119.91.231.211:8081/repository/maven-releases/")
            isAllowInsecureProtocol = true
        }*/
        maven {
            url = uri("https://nexus.huawo-wear.com/repository/maven-releases/")
            credentials {
                username = "huaworead"
                password = "huawo202301"
            }
        }
    }

}

rootProject.name = "TitanVideoTrimmingPoc"
include(":app")
 