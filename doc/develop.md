# 前言

MiraiAndroid提供了加载dex作为插件的功能，该功能最初的版本(d8手动转换)存在着一些问题，包括不便于开发，不便于安装

为解决此问题，我们开发了apk插件系统，类似于xposed模块

# 创建项目

首先创建一个空项目

![](https://cdn.jsdelivr.net/gh/mzdluo123/blog_imgs/img/20210127201037.png)

后一步的minsdk可以调整为Android8.0，其他参数请自行调整

# 配置项目

## 导入依赖

请参考[mirai-console项目配置](https://github.com/mamoe/mirai-console/blob/master/docs/ConfiguringProjects.md)的方式完成项目配置，必须使用手动方式配置，比如下面这样

```
dependencies {
    compileOnly("net.mamoe:mirai-core:2.1.0") // mirai-core 的 API
    compileOnly("net.mamoe:mirai-console:2.0.0") // 后端
    testImplementation("net.mamoe:mirai-console-terminal:2.0.0") // 前端, 用于启动测试

    // 下面是Android项目默认生成的，可以不管
    implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
    implementation 'androidx.core:core-ktx:1.3.2'
    implementation 'androidx.appcompat:appcompat:1.2.0'
    implementation 'com.google.android.material:material:1.2.1'
    testImplementation 'junit:junit:4.+'
    androidTestImplementation 'androidx.test.ext:junit:1.1.2'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.3.0'
}
```

**请注意，要在app模块内的build.gradle内添加，而不是项目的build.gradle**

## 设置meta-data

打开`AndroidManifest.xml`，在`application`节点内添加以下内容

```
  <meta-data
            android:name="miraiandroid_plugin"
            android:value="true"/>
```

## 设置主类

像这样的方式添加java resources文件夹

![](https://cdn.jsdelivr.net/gh/mzdluo123/blog_imgs/img/20210127201829.png)

在文件夹内创建`META-INF/services/net.mamoe.mirai.console.plugin.jvm.JvmPlugin`文件

![](https://cdn.jsdelivr.net/gh/mzdluo123/blog_imgs/img/20210127201930.png)

在文件内填写主类名称

![](https://cdn.jsdelivr.net/gh/mzdluo123/blog_imgs/img/20210127201959.png)


![](https://cdn.jsdelivr.net/gh/mzdluo123/blog_imgs/img/20210127202021.png)


# 安装，调试和分发

请直接按下右上角的绿色箭头将应用安装到手机，安装后打开MiraiAndroid即可

下一个版本将允许release版本进行调试；请点击`Run > Attach Debugger to Android Process`并选择你的设备并勾选`Show all processes`并选择`io.github.mzdluo123.mirai.android:BotProcess`来开始调试

分发方式同一般Android应用，请创建一个签名后的apk来分发你的插件

# 其他问题

## 关于context

插件的加载形式为MiraiAndroid读取apk文件加载到自己的进程内，因此获得的context为MiraiAndroid的context，同时，在插件应用内申请的权限也没有作用，后期会尝试优化

## 数据存储

数据的存储路径在MiraiAndroid显示的工作目录下的插件数据目录内