# MiraiAndroid
（实验性）在Android上运行Mirai-console

关于mirai项目的一切请点击[这里](https://github.com/mamoe/mirai)

**仅Android 8及以上可用**

相比使用`Termux`或者是`Linux Deploy`等应用运行mirai的方案，该项目提供的方案具有更好的性能以及更少的资源占用，但可能存在兼容性问题

更多信息请加QQ群`655057127`了解


# 已实现的功能

* 兼容mirai-console插件(实验性)
* 带验证码的登录处理
* 内置Google d8 dex编译器，可直接编译JVM的console插件在Android运行(实验性)

# 计划功能

* 带设备锁需要验证的登录处理
* lua脚本接口

# 安装插件

你有两个办法安装插件

## 使用app直接打开jar文件安装

这是最简单的方式。app切换到插件管理点击右上角选择即可，你也可以使用系统文件选择器直接打开jar文件

## 使用pc转换后导入

请按照以下方法操作

* 找到`d8`编译器的运行脚本

d8工具已在新版`Android sdk`中自带，它就在`build-tools`中对应版本的文件夹下。在Windows平台他是一个bat文件

* 编译

打开终端，使用以下命令编译

```
d8.bat --output 输出文件.jar 源文件
```
输出文件扩展名必须是jar或者是zip

* 复制资源

使用压缩软件打开源jar文件，将里面的`plugin.yml`，`META-INF`和其他资源文件(除存放class文件夹的其他文件)复制到新的jar文件内

* 安装插件

将上一步的新的jar文件复制到手机的`/sdcard/Android/data/io.github.mzdluo123.mirai.android/files/plugins/`

重启即可使用插件，当然部分插件可能也会存在兼容性问题