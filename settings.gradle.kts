pluginManagement {
    repositories {
        // 改为阿里云的镜像地址
//        maven { setUrl("https://maven.aliyun.com/repository/central") }
//        maven { setUrl("https://maven.aliyun.com/repository/jcenter") }
//        maven { setUrl("https://maven.aliyun.com/repository/google") }
//        maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
//        maven { setUrl("https://maven.aliyun.com/repository/public") }
//        maven { setUrl("https://jitpack.io") }
//        maven { setUrl("https://maven.aliyun.com/nexus/content/groups/public/") }
//        maven { setUrl("https://maven.aliyun.com/nexus/content/repositories/jcenter") }
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    resolutionStrategy {
        // 统一声明 KSP 版本（避免多模块版本不一致）
        eachPlugin {
            if (requested.id.id == "com.google.devtools.ksp") {
                useVersion("1.9.0-1.0.13") // 与你的 Kotlin 版本匹配
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 改为阿里云的镜像地址
//        maven { setUrl("https://maven.aliyun.com/repository/central") }
//        maven { setUrl("https://maven.aliyun.com/repository/jcenter") }
//        maven { setUrl("https://maven.aliyun.com/repository/google") }
//        maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
//        maven { setUrl("https://maven.aliyun.com/repository/public") }
//        maven { setUrl("https://jitpack.io") }

        google()
        mavenCentral()
    }
}


rootProject.name = "mBili"
include(":app")
 