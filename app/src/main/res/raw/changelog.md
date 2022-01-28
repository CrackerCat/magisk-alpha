# alpha更新日志

## Magisk (f880b575-alpha)
- [App] 支持SharedUserId
- [App] 还原boot镜像后删除备份文件
- [App] 内置当前版本更新日志
- [General] 不再自动解锁设备块
- [General] Zygisk关闭时使用MagiskHide实现隐藏
- [App] 适配 Android 12L
- [MagiskInit] 重构两步启动的处理方法
- [General] 更新到 NDK 23
- [App] 添加崩溃统计

### 如何安装？
通过Magisk应用来安装和卸载Magisk，一般情况应直接在应用内完成，第一次安装等特殊情况应修补镜像后使用fastboot/odin工具刷入。
通过自定义Recovery不是受支持的方式。

# 上游更新日志

## 2022.1.28 Magisk v24.1

> For those coming from v24.0, v24.1 only has some minor app improvements. The following are copied from v24.0 release notes.

It has been a while since the last public release, long time no see! A personal update for those unaware: I am now working at Google on the Android Platform Security team. Without further ado, let's jump right into it!

### MagiskHide Removal

I have lost interest in fighting this battle for quite a while; plus, the existing MagiskHide implementation is flawed in so many ways. Decoupling Magisk from root hiding is, in my opinion, beneficial to the community. Ever since my announcement on Twitter months ago, highly effective "root hiding" modules (much **MUCH** better than MagiskHide) has been flourishing, which again shows that people are way more capable than I am on this subject. So why not give those determined their time to shine, and let me focus on improving Magisk instead of drowning in the everlasting cat-and-mouse game 😉.

### Sunsetting Magisk-Modules-Repo

Due to lack of time and maintenance, the centralized Magisk-Modules-Repo was frozen, and the functionality to download modules from the repo is removed in v24.0. As a supplement, module developers can now specify an `updateJson` URL in their modules. The Magisk app will use that to check, download, and install module updates.

### Introducing Zygisk

Zygisk is **Magisk in Zygote**, the next big thing for Magisk! When this feature is enabled, a part of Magisk will run in the `Zygote` daemon process, allowing module developers to run code directly in every Android apps' processes. If you've heard of [Riru](https://github.com/RikkaApps/Riru), then Zygisk is inspired by that project and is functionally similar, though the implementation is quite different internally. I cannot wait to see what module developers can achieve using Zygisk!

### Documentation

For developers, details about `updateJson` and building Zygisk modules can all be found in the updated [documentation](https://topjohnwu.github.io/Magisk/guides.html#magisk-modules).

### v24.1 Full Changelog

- [App] Stability improvements
