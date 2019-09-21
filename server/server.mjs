import WebSocket from 'ws';

const server = new WebSocket.Server({port : 8080});
let tempMessage = "";

server.on('connection', function(ws) {
  ws.on('message', function(message) {
    if (message == 'polling') {
      console.log('Received: polling ', tempMessage.length);
      if (tempMessage != "") ws.send(tempMessage);
    } else {
      console.log('Received: data    ', tempMessage.length);
      tempMessage = Array.from(message);
      // console.log(tempMessage);
    }
  });
});
