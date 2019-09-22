import { app, BrowserWindow } from 'electron';
import { startWebsocket } from './backend/Websocket';
import { startExpress } from './backend/Express';

function createWindow() {
    startWebsocket();
    startExpress();

    // ブラウザウインドウを作成
    let win = new BrowserWindow({
        width: 1500,
        height: 800,
        webPreferences: {
            nodeIntegration: true,
        },
    });

    // そしてこのアプリの index.html をロード
    win.loadFile('./static/index.html');

    // 開発者ツールを開く
    win.webContents.openDevTools();

    // ウィンドウが閉じられた時に発火
    win.on('closed', () => {
        // ウインドウオブジェクトの参照を外す。
        // 通常、マルチウインドウをサポートするときは、
        // 配列にウインドウを格納する。
        // ここは該当する要素を削除するタイミング。
        win = null;
    });
}

// このイベントは、Electronが初期化処理と
// browser windowの作成を完了した時に呼び出されます。
// 一部のAPIはこのイベントが発生した後にのみ利用できます。
app.on('ready', createWindow);

// 全てのウィンドウが閉じられた時に終了する
app.on('window-all-closed', () => {
    // macOSでは、ユーザが Cmd + Q で明示的に終了するまで、
    // アプリケーションとそのメニューバーは有効なままにするのが一般的。
    if (process.platform !== 'darwin') {
        app.quit();
    }
});

app.on('activate', () => {
    // macOSでは、ユーザがドックアイコンをクリックしたとき、
    // そのアプリのウインドウが無かったら再作成するのが一般的。
    if (win === null) {
        createWindow();
    }
});
