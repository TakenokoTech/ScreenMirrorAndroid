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
                    // console.log('Received: polling ', tempMessage.length);
                    // Log.info('polling', performance.now() - pollingStartTime);
                    pollingStartTime = performance.now();
                    if (tempMessage != '') ws.send(tempMessage);
                    break;
                default:
                    // console.log('Received: data    ', tempMessage.length);
                    Log.info('image', performance.now() - imageStartTime);
                    imageStartTime = performance.now();
                    tempMessage = message;
                    // tempMessage = Array.from(message);
                    break;
            }
        });
    });
}

/*
    if (message == 'polling') {
        // console.log('Received: polling ', tempMessage.length);
        if (tempMessage != '') ws.send(tempMessage);
    } else {
        // console.log('Received: data    ', tempMessage.length);
        tempMessage = Array.from(message);
    }
*/
