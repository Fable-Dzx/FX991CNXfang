const { app, BrowserWindow, Menu } = require('electron');
const path = require('path');

function createWindow() {
  const win = new BrowserWindow({
    width: 480,
    height: 720,
    minWidth: 400,
    minHeight: 620,
    autoHideMenuBar: true,
    backgroundColor: '#eaecf3',
    icon: path.join(__dirname, 'src', 'favicon.ico'),
    webPreferences: {
      // 只加载本地静态资源，不开 node 集成，保持安全
      nodeIntegration: false,
      contextIsolation: true,
      sandbox: true,
    },
  });

  // 菜单栏隐藏（计算器 App 不需要）
  Menu.setApplicationMenu(null);

  win.loadFile(path.join(__dirname, 'src', 'index.html'));

  // 阻止所有外部导航（保持本地应用）
  win.webContents.on('will-navigate', (e, url) => {
    if (!url.startsWith('file://')) e.preventDefault();
  });
  win.webContents.setWindowOpenHandler(() => ({ action: 'deny' }));
}

app.whenReady().then(() => {
  createWindow();
  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit();
});
