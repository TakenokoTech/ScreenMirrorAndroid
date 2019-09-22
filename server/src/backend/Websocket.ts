import WebSocket from 'ws';

const server = new WebSocket.Server({ port: 8080 });
let tempMessage = '';

export function startWebsocket() {
    server.on('connection', ws => {
        ws.on('message', message => {
            switch (true) {
                case message == 'polling':
                    // console.log('Received: polling ', tempMessage.length);
                    if (tempMessage != '') ws.send(tempMessage);
                    break;
                default:
                    // console.log('Received: data    ', tempMessage.length);
                    tempMessage = Array.from(message);
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
