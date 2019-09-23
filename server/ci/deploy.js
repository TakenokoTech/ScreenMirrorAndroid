const fs = require('fs');
const archiver = require('archiver');

let TARGET_PATH = '';
switch (process.env.TARGET_OS || '') {
    case 'windows-latest':
        TARGET_PATH = 'ScreenMirrorApp-win32-x64';
        break;
    case 'macOS-latest':
        TARGET_PATH = 'ScreenMirrorApp-darwin-x64';
        break;
    case 'ubuntu-latest':
        TARGET_PATH = 'ScreenMirrorApp-linux-x64';
        break;
    default:
        TARGET_PATH = 'ScreenMirrorApp-win32-x64';
        break;
}
console.log('target path: ' + TARGET_PATH);

const archive = archiver('zip', { zlib: { level: 9 } });
archive.on('error', err => {
    throw err;
});

const input = __dirname + '/../build/' + targetPath;
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
