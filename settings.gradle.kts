pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("http://4thline.org/m2")
            isAllowInsecureProtocol = true
        }
    }
}

rootProject.name = "PeerPunch"
include(":app")
 