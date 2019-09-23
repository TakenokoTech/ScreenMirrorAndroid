const fs = require('fs');
const path = require('path');
const readline = require('readline');
const { google } = require('googleapis');

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

let BUILD_VERSION = Math.floor(new Date().getTime() / 1000);
let REVISION_VERSION = 1;
const SCOPES = ['https://www.googleapis.com/auth/drive.file', 'https://www.googleapis.com/auth/drive.appdata'];
const TOKEN_PATH = 'service_account_token';
const SERVICE_TOKEN = process.env.SERVICE_TOKEN || '';
const MAIL_ADDRESS = process.env.MAIL_ADDRESS || '';
console.log('target path: ' + TARGET_PATH);

const tokenPath = __dirname + '/../build/' + TOKEN_PATH + '.json';
const targetZip = __dirname + '/../build/' + TARGET_PATH + '.zip';
const regexp = new RegExp(TARGET_PATH + '-([0-9]*)-([0-9]*).zip');

(async () => {
    await saveToken();
    const auth = await authentication();
    const oldFiles = await listFiles(auth);
    // Version
    console.log('\n[maxRevision]');
    oldFiles.forEach(file => {
        const revision = regexp.test(file.name) ? regexp.exec(file.name)[2] : null;
        console.log(file.name, ' --> revision: ', revision);
        REVISION_VERSION = Math.max(REVISION_VERSION, +revision + 1);
    });
    // Delete File
    console.log('\n[deleteFile]');
    const promiseAll = oldFiles.map(file => {
        const revision = regexp.test(file.name) ? regexp.exec(file.name)[2] : null;
        if (revision != null && +revision <= REVISION_VERSION - 3) return deleteFile(auth, file.id);
    });
    await Promise.all(promiseAll);
    // Create File
    const fileId = await createFile(auth, BUILD_VERSION, REVISION_VERSION);
    await createPermissions(auth, fileId);
    const newFiles = await listFiles(auth);
})();

async function saveToken() {
    return new Promise((resoleve, reject) => {
        fs.writeFile(tokenPath, SERVICE_TOKEN, err => {
            if (err) {
                console.log(err);
                reject(err);
                return;
            }
            console.log('Token stored to', tokenPath);
            resoleve(tokenPath);
        });
    });
}

async function authentication() {
    console.log('\n[authentication]');
    return google.auth.getClient({ keyFile: tokenPath, scopes: SCOPES }).then(result => {
        console.log('authentication done.');
        return result;
    });
}

async function createFile(auth) {
    console.log('\n[createFile]');
    console.log('Build Version: ' + BUILD_VERSION);
    console.log('Revision Version: ' + REVISION_VERSION);
    return new Promise((resoleve, reject) => {
        const fileMetadata = { name: `${TARGET_PATH}-${BUILD_VERSION}-${REVISION_VERSION}.zip` };
        const media = { mimeType: 'application/zip', body: fs.createReadStream(targetZip) };
        google.drive({ version: 'v3', auth }).files.create({ resource: fileMetadata, media: media, fields: 'id' }, (err, res) => {
            if (err) {
                console.log('The API returned an error: ' + err);
                reject(err);
                return;
            }
            console.log(res.data);
            resoleve(res.data.id);
        });
    });
}

async function deleteFile(auth, fileId) {
    // console.log('\n[deleteFile]');
    return new Promise((resoleve, reject) => {
        google.drive({ version: 'v3', auth }).files.delete({ fileId: fileId }, (err, res) => {
            if (err) {
                console.log('The API returned an error: ' + err);
                reject(err);
                return;
            }
            console.log('delete done. ' + fileId);
            resoleve(res.data);
        });
    });
}

async function createPermissions(auth, fileId) {
    console.log('\n[createPermissions]');
    return new Promise((resoleve, reject) => {
        const userPermission = { type: 'user', role: 'reader', emailAddress: MAIL_ADDRESS };
        google.drive({ version: 'v3' }).permissions.create({ auth: auth, resource: userPermission, fileId: fileId }, (err, res) => {
            if (err) {
                console.log('The API returned an error: ' + err);
                reject(err);
                return;
            }
            console.log(res.data);
            resoleve(res);
        });
    });
}

async function listFiles(auth) {
    console.log('\n[listFiles]');
    return new Promise((resoleve, reject) => {
        google.drive({ version: 'v3', auth }).files.list({ pageSize: 10, fields: 'nextPageToken, files(id, name, mimeType)' }, (err, res) => {
            if (err) {
                console.log('The API returned an error: ' + err);
                reject(err);
                return;
            }
            const files = res.data.files;
            if (files.length) files.map(file => console.log(`${file.name} (${file.id})`));
            else console.log('No files found.');
            resoleve(res.data.files);
        });
    });
}
