# FX991CNXfang

将 [FX-991-CNX](https://github.com/Fable-Dzx/FX-991-CNX)（在线科学计算器，MIT License）打包为 Android WebView 应用。

- Web 部分：Next.js 静态导出，打包进 `app/src/main/assets/`
- 壳：原生 Android WebView，`shouldInterceptRequest` 将 `/_next/...` 等绝对路径映射到 assets
- 构建：GitHub Actions 自动产出 debug APK

## 本地构建（可选）
```
gradle :app:assembleDebug
```
产物：`app/build/outputs/apk/debug/app-debug.apk`
