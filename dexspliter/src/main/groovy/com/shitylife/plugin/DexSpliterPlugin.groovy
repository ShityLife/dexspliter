package com.shitylife.plugin

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project;

class DexSpliterPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        DexSpliterExtension extension = project.extensions.create('dexspliter', DexSpliterExtension)
        project.afterEvaluate {
            def android = project.extensions.findByType(BaseExtension)
            println "handle main-dex by user，start..."
            if (android.defaultConfig.minSdkVersion.getApiLevel() >= 21) {
                return
            }
            println "main-dex，minSdkVersion is ${android.defaultConfig.minSdkVersion.getApiLevel()}"
            android.applicationVariants.all { variant ->

                def variantName = variant.name.capitalize()
                def multiDexTask = project.tasks.findByName("transformClassesWithMultidexlistFor${variantName}")
                def exist = multiDexTask != null
                println "main-dex multidexTask(transformClassesWithMultidexlistFor${variantName}) exist: ${exist}"

                if (exist) {
                    def replaceTask = createReplaceMainDexListTask(variant, project, extension);
                    multiDexTask.finalizedBy replaceTask
                }
            }
        }
    }

    def createReplaceMainDexListTask(variant, Project project, DexSpliterExtension extension) {
        def variantName = variant.name.capitalize()

        return project.task("replace${variantName}MainDexClassList").doLast {
            //从主dex移除的列表
            def excludeClassList = []
//            File excludeClassFile = new File("${project.projectDir}/main_dex_exclude_class.txt")
            File excludeClassFile = new File("${extension.excludeClassFile}")
            println "${extension.excludeClassFile} exist: ${excludeClassFile.exists()}"
            if (excludeClassFile.exists()) {
                excludeClassFile.eachLine { line ->
                    if (!line.trim().isEmpty() && !line.startsWith("#")) {
                        excludeClassList.add(line.trim())
                    }
                }
                excludeClassList.unique()
            }
            def mainDexList = []
            //默认值 default value
            String mainDexPath = "${project.buildDir}/intermediates/legacy_multidex_main_dex_list/${variant.dirName}/transformClassesWithMultidexlistFor${variantName}/maindexlist.txt"
            if (!android.text.TextUtils.isEmpty(extension.mainDexFile)) {
                //如果用户设置了值，就用用户设置的
                //or use extension value
                mainDexPath = extension.mainDexFile
            }
            File mainDexFile = new File(mainDexPath)
            println "${mainDexPath} exist : ${mainDexFile.exists()}"
            //再次判断兼容 linux/mac 环境获取
            if (!mainDexFile.exists()) {
                String linuxMacPath = "${project.buildDir}/intermediates/legacy_multidex_main_dex_list/${variant.dirName}/transformClassesWithMultidexlistFor${variantName}/mainDexList.txt"
                mainDexFile = new File(linuxMacPath)
                println "${linuxMacPath} exist : ${mainDexFile.exists()}"
            }
            if (mainDexFile.exists()) {
                mainDexFile.eachLine { line ->
                    if (!line.isEmpty()) {
                        mainDexList.add(line.trim())
                    }
                }
                mainDexList.unique()
                if (!excludeClassList.isEmpty()) {
                    def newMainDexList = mainDexList.findResults { mainDexItem ->
                        def isKeepMainDexItem = true
                        for (excludeClassItem in excludeClassList) {
                            if (mainDexItem.contains(excludeClassItem)) {
                                isKeepMainDexItem = false
                                break
                            }
                        }
                        if (isKeepMainDexItem) mainDexItem else null
                    }
                    if (newMainDexList.size() < mainDexList.size()) {
                        mainDexFile.delete()
                        mainDexFile.createNewFile()
                        mainDexFile.withWriterAppend { writer ->
                            newMainDexList.each {
                                writer << it << '\n'
                                writer.flush()
                            }
                        }
                    }
                }
            }
        }
    }
}
