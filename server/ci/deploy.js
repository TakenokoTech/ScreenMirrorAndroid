const fs = require('fs');
const archiver = require('archiver');

const targetPath = process.env.TARGET_PATH || '';
console.log('target path: ' + targetPath);

if (!targetPath || targetPath == '') {
    process.exit();
}

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
