# dexspliter
拆分主dex解决api19以下的65536问题。split main dex.

# 使用方法
只要四行代码加一个过滤文件就能解决方法数超限问题。
1.在根目录下添加以下依赖。
```
repositories {
    maven { url 'https://dl.bintray.com/black201/maven' }
}
dependencies {
    classpath 'com.shitylife.plugin:dexspliter:1.0.0'
}
```
2.在app模块目录下的build.gradle里添加如下代码
```
apply plugin: 'com.shitylife.dexspliter'
dexspliter{
    excludeClassFile "../main_dex_exclude_class.txt"//配置过滤文件的位置
}
```
3.创建一个名为main_dex_exclude_class.txt的文件（你想叫什么都行，只要在配置里配对了文件位置就可以），把需要从主Dex分离出来的class都写进去。具体需要放入哪些类取决于你的项目。可以查看这个文件
project.buildDir}/intermediates/legacy_multidex_main_dex_list/${variant.dirName}/transformClassesWithMultidexlistFor${variantName}/mainDexList.txt
这个文件包含了主dex的所有class，你可以选取其中的一些class，写入main_dex_exclude_class.txt,这些被写入的类会在打包的时候放入子dex中，这样就解决了65536的问题。


# How to use?
In Project root/build.gradle
1. Add the following code in build.gradle in the rootProject dir.
```
repositories {
    maven { url 'https://dl.bintray.com/black201/maven' }
}
dependencies {
    classpath 'com.shitylife.plugin:dexspliter:1.0.0'
}
```

2.Add the following code in build.gradle in the subProject dir.
```
apply plugin: 'com.shitylife.dexspliter'
dexspliter{
    excludeClassFile "../main_dex_exclude_class.txt"
}
```
3.Make a file named main_dex_exclude_class.txt,write the classes you want put into sub dex.
