import WebSocket from 'ws';
import { PerformanceObserver, performance } from 'perf_hooks';

import { Log } from '../utils/Log';

const server = new WebSocket.Server({ port: 8080 });
let tempMessage = '';
let imageStartTime = null;
let pollingStartTime = null;

export function startWebsocket() {
    server.on('connection', ws => {
        ws.on('message', message => {
            switch (true) {
                case message == 'polling':
                    pollingStartTime = performance.now();
                    if (tempMessage != '') ws.send(tempMessage);
                    break;
                default:
                    Log.info('image', performance.now() - imageStartTime);
                    imageStartTime = performance.now();
                    tempMessage = message;
                    break;
            }
        });
    });
}
