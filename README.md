# FX991CNXfang - Windows 版

FX-991CN X 在线科学计算器（MIT License）的 Windows 桌面封装。

- 技术栈：Electron + electron-builder，静态网页资源全部内置
- 构建：GitHub Actions（windows-latest）自动产出便携版 exe

## 本地构建
```
npm install
npx electron-builder --win portable
```
产物：`dist/FX991CNX-Windows.exe`
