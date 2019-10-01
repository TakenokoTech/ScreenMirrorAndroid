let TARGET_PATH = (() => {
    switch (process.env.TARGET_OS || '') {
        case 'macOS-latest':
            return 'ScreenMirrorApp-darwin-x64';
        case 'ubuntu-latest':
            return 'ScreenMirrorApp-linux-x64';
        case 'windows-latest':
        default:
            return 'ScreenMirrorApp-win32-x64';
    }
})();
console.log(TARGET_PATH)