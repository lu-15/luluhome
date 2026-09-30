# 评论翻译 APK（安卓原生版）· 构建与使用说明

无障碍服务读取白名单 App 的**评论区**文字 → 粤语词库/翻译API → 悬浮窗实时显示普通话。

## 怎么拿到 APK（二选一）

### 路线 A：GitHub 云端编译（推荐，本机啥都不用装）
1. 把 `apk-project` 整个文件夹推到一个 GitHub 仓库（工程里已带 `.github/workflows/build-apk.yml`）
2. 仓库页 → Actions → 「Build APK」→ 跑完后在 Artifacts 下载 `comment-translator-debug-apk`
3. 传到手机安装即可（调试版 APK，无需签名）

### 路线 B：本机 Android Studio
1. 装 Android Studio → 打开 `apk-project` 目录 → 等 Gradle Sync 完成
2. 菜单 Build → Build App Bundle(s)/APK(s) → Build APK(s)
3. 产物在 `app/build/outputs/apk/debug/app-debug.apk`

## 装好后 3 步启用（App 首页有两个按钮引导）

1. **无障碍权限**：设置 → 无障碍 → 「评论翻译」→ 开启（部分 ROM 在「已下载的服务」里）
2. **悬浮窗权限**：点 App 里第二个按钮 → 允许「显示在其他应用上层」
3. **关省电限制**（国产 ROM 必做，否则服务被杀）：设置 → 电池 → 评论翻译 → 无限制/允许后台

然后打开 B站/抖音/快手/YouTube 的评论区就能看到效果：
- 粤语评论 → 悬浮窗紫色条目（本地词库，离线秒出）
- 英语评论 → 悬浮窗蓝色条目（MyMemory API，失败自动切 Google）

## 监听的 App（`Config.kt` 里可增删包名）

B站（含国际版）、抖音、快手（含极速版）、YouTube。想加别的 App，把包名加进 `WATCH_PACKAGES` 重新编译即可。

## 诚实的预期管理

| 能/不能 | 说明 |
|---|---|
| ✅ 评论区文字 | 无障碍接口可读，实时翻译 |
| ❌ 视频弹幕 | SurfaceView 像素渲染，**系统层面读不到**，任何正规手段都不行 |
| ❌ iOS | 系统封闭，此方案无 iOS 对应版 |

- 粤语转换与油猴脚本同词库，冷门口语可能转不顺，反馈给我加词
- 英语翻译需要网络；国内网络下 Google 通道大概率超时，主力走 MyMemory
- 这是 MVP v1.0：还没有开关白名单设置界面、翻译历史持久化、点击悬浮窗跳转原评论等，按需迭代

## 后续迭代候选

- App 内设置页：白名单开关、粤语词库自定义、悬浮窗大小/透明度
- 通知栏常驻通知防杀
- release 签名打包（现在发的是 debug 包，自己用够了）
