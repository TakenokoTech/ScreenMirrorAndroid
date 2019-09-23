const fs = require('fs');
const archiver = require('archiver');

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
console.log('target path: ' + TARGET_PATH);

const archive = archiver('zip', { zlib: { level: 9 } });
archive.on('error', err => {
    throw err;
});

const input = __dirname + '/../build/' + TARGET_PATH;
const output = fs.createWriteStream(input + '.zip');
output.on('close', () => {
    console.log(archive.pointer() + ' total bytes');
    console.log('archiver has been finalized and the output file descriptor has closed.');
});
output.on('end', () => {
    console.log('Data has been drained');
});
archive.pipe(output);
archive.directory(input, false);
archive.finalize();
