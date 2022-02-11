<!--
 * @Descripttion: 
 * @version: 
 * @Author: sueRimn
 * @Date: 2020-05-08 16:45:00
 * @LastEditors: sueRimn
 * @LastEditTime: 2020-05-09 12:22:15
 -->
 
 <div align="center">
   <img width="160" src="https://cdn.jsdelivr.net/gh/mzdluo123/blog_imgs/img/20200531205703.png" alt="logo"></br>

   <img width="95" src="https://cdn.jsdelivr.net/gh/mzdluo123/blog_imgs/img/20200531205726.png" alt="title">

----
[Mirai](https://github.com/mamoe/mirai) 是一个在全平台下运行，提供 QQ Android 协议支持的高效率机器人库

图标以及形象由画师<a href = "https://github.com/DazeCake">DazeCake</a>绘制

</div>


# MiraiAndroid

[![GitHub Workflow Status](https://img.shields.io/github/workflow/status/mzdluo123/MiraiAndroid/Android%20Build?style=flat-square&logo=github)](https://github.com/mzdluo123/MiraiAndroid/actions)
[![Release](https://img.shields.io/github/v/release/mzdluo123/MiraiAndroid?style=flat-square&color=orange&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAGQAAABkCAMAAABHPGVmAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAAxQTFRFOqTdPpDDDjdWAAAA01db/wAAAAR0Uk5T////AEAqqfQAAAGRSURBVHja7NrRTsMwEETRO+v//2cKQtDSNNm1PSuE8Ht0FOcpM8NoOPwpJM6P5g/yI/gRsCNgR8COgB0BOwJ2BOwI2BGwI2BHwI6AHQE7AnYE7AjYEbAjYEfAjoAdATsCdgTsCNgRsCNgR8COgB2BdiTkRxQlZQq5GbdnzMi7McKLfBhjyIkoPv805UMU8fU/a0O+jfRnKSN3RvrCqojiIQCQA1HEj5jBgPw0cp+lhjwZuQsrIQdGSqkgiuP0ZyeiOEZiJ/LCSFxYHnlpXF9YFpHiLJTb9CZnxlAHcqGkkasUcwdynZV2IFpHErHvciKRC5c7kLGGJHPyWEHSabzmkUrmP4vUmoUORHNIsSSZCjrrVUwHMurIRKtUDp+nuivVkNmGrILM93AdiPLIQqWY7k/WissOZOSQxQ421WktN726Rnb0yR3IuEK2VOM6RzYV8Ke17L6avwMZr5GNi4WXVfnWXYSOkd3riw4kjpDtQxI9I4a5ytPawzOKeURc05t7xLXvkTq2RC3I+Ed+KfImwADzdEfKPNJbbAAAAABJRU5ErkJggg==&logoWidth=12)](https://github.com/mzdluo123/MiraiAndroid/releases)
[![MiraiForum](https://img.shields.io/badge/官方论坛-mirai--forum-blueviolet?style=flat-square&logo=appveyor)](https://mirai.mamoe.net)

MiraiAndroid 是 基于 [Mirai](https://github.com/mamoe/mirai) 的 QQ 机器人 Android 前端程序，支持多种脚本接口，具有 轻量、简洁、易用、高效 的特点，依赖于 [mirai-console](https://github.com/mamoe/mirai-console) 。

相比使用 `Termux` 或者是 `Linux Deploy` 等部署并运行 [Mirai](https://github.com/mamoe/mirai) 的方案， MiraiAndroid 提供了更好的性能、更少的资源占用，以及更方便的操作界面。

最新的构建版本你可以到 [appcenter](https://install.appcenter.ms/users/mzdluo123/apps/miraiandroid/distribution_groups/release) 内找到。
## 声明

### 一切开发旨在学习，请勿用于非法用途
- MiraiAndroid 是完全免费且开放源代码的软件，仅供学习和娱乐用途使用
- MiraiAndroid 不会通过任何方式强制收取费用，或对使用者提出物质条件
- MiraiAndroid 由整个开源社区维护，并不是属于某个个体的作品，所有贡献者都享有其作品的著作权。

### 许可证

    Copyright (C) 2019-2020 Mamoe Technologies and contributors.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

`MiraiAndroid` 采用 `AGPLv3` 协议开源。为了整个社区的良性发展，我们**强烈建议**您做到以下几点：

- **间接接触（包括但不限于使用 `httpapi` 或 跨进程技术）到 `mirai` 的软件使用 `AGPLv3` 开源**
- **不鼓励，不支持一切商业使用**

## 已实现的功能

* 兼容 mirai-console 插件(实验性)
* 带验证码的登录处理
* 内置 Google d8 dex 编译器，可直接编译 JVM 的 console 插件在 Android 运行(实验性)
* lua 脚本接口（测试版）
* 网络掉线提醒

## 安装脚本

目前脚本系统仍在开发中，对于2.x版本的lua或js脚本系统正在迁移中，请等待后续更新

## 安装插件

对于 apk(Android 软件包) 格式的插件，请直接安装到系统内即可，关于这类插件的开发说明请看[这里](docs/develop.md)

对于jar格式的插件，你有两个办法安装插件

### 使用 app 直接打开 jar 文件安装

这是最简单的方式。 app 切换到插件管理点击右上角选择即可，你也可以使用系统文件选择器直接打开 jar 文件

对于已经经过 d8 转换过的插件，请直接导入

**如果你无法选择文件**，请使用第三方文件选择器选择（例如 Mix）

### 使用 pc 转换后导入

请按照以下方法操作

* 找到 `d8` 编译器的运行脚本

d8工具已在新版 `Android sdk` 中自带，它就在 `build-tools` 中对应版本的文件夹下。在Windows平台他是一个bat文件


* 编译

打开终端，使用以下命令编译

```
d8.bat --output 输出文件.jar 源文件
```
输出文件扩展名必须是jar或者是zip

* 复制资源

使用压缩软件打开源jar文件，将里面的 `plugin.yml` ， `META-INF` 和其他资源文件(除存放class文件夹的其他文件)复制到新的jar文件内

* 安装插件

将上一步的新的jar文件复制到手机的 `/sdcard/Android/data/io.github.mzdluo123.mirai.android/files/plugins/`

重启即可使用插件，当然部分插件可能也会存在兼容性问题

## FAQ

Q: 后台运行被系统杀死<br>
A：请手动将应用添加到系统后台白名单

Q：应用崩溃或后台报错<br>
A：如果是后台报错一般是插件或者是mirai-core的问题，是mirai-core的问题请在菜单内找到分享日志并到群内或开启issue反馈，插件的问题请联系对应开发者；如果是应用崩溃，请重启并按照上面的方法提交日志给我们

## 兼容的 Console 插件列表

以下插件由群友测试未发现问题，你可以到群内下载，或是到[插件中心](https://github.com/mamoe/mirai-plugins)手动下载jvm版并导入

* mirai-api-http
* chatcommand

对于其他插件请自行尝试；此外，如果你的插件使用了一些 Android 不支持的 api(例如BufferedImage) 那么使用了这个api的功能将绝对不能正常工作

## 关于支持的 Android 版本

MiraiAndroid 只能在 Android8.0 及以上版本的系统中工作，因为上游 MiaiCore 使用了大量 Java8 特性

目前测试过的版本中 Android10 无问题， Android8.1 无法在移动端编译插件

## 消息推送(2.9新增)

必须使用自动登录并在设置中开启才能使用该功能

你可以发送广播来快速向指定群或联系人推送信息，这里是data的URI格式

```
ma://sendGroupMsg?msg=消息&id=群号
ma://sendFriendMsg?msg=消息&id=账号
ma://sendFriendMsg?msg=消息&id=账号&at=要at的人
```

```kotlin
sendBroadcast(Intent("io.github.mzdluo123.mirai.android.PushMsg").apply {
        data = Uri.parse("ma://sendGroupMsg?msg=HelloWorld&id=655057127")
    })
```

以下是 auto.js 的示例

```js
app.sendBroadcast({
    action: "io.github.mzdluo123.mirai.android.PushMsg",
    data: "ma://sendGroupMsg?msg=来自autojs的消息&id=655057127"
})
```

以下是 tasker 的示例

```yaml
    ma (2)
    	A1: 发送意图 [ 操作:io.github.mzdluo123.mirai.android.PushMsg 类别:None Mime类型: 数据:ma://sendGroupMsg?msg=来自tasker的消息&id=655057127 额外: 额外: 额外: 包: 类: 目标:Broadcast Receiver ] 
```
