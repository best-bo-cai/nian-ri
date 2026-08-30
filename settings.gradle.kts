// CI 环境（如 GitHub Actions，海外服务器）直接使用官方源；
// 本地构建（国内网络）优先使用阿里云镜像加速。
// 注意：pluginManagement 块由 Gradle 提前单独编译，不能引用脚本中定义的顶层变量，
// 因此环境变量判断需直接内联在每个块里。
pluginManagement {
    repositories {
        if (System.getenv("CI") != "true") {
            maven { url = uri("https://maven.aliyun.com/repository/public") }
            maven { url = uri("https://maven.aliyun.com/repository/google") }
            maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        if (System.getenv("CI") != "true") {
            maven { url = uri("https://maven.aliyun.com/repository/public") }
            maven { url = uri("https://maven.aliyun.com/repository/google") }
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "NianRi"
include(":app")
