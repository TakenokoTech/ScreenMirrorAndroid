import 'jquery.qrcode';

class WebSocketClient {
    private socket = null;
    private timer = null;

    onOpenHandler = () => {};
    onMessageHandler = () => {};

    open(url) {
        this.close();
        this.socket = new WebSocket(url);
        this.socket.addEventListener('open', e => {
            console.log('open');
            this.timer = setInterval(() => this.socket.send('polling'), 30);
            $('#qrcode').qrcode({ text: `ws://${websocketInput.value}:8080`, width: 200, height: 200, background: '#ffffff', foreground: '#333333' });
        });
        this.socket.addEventListener('message', e => {
            // console.log(e);
            const data = e.data;
            image.src = URL.createObjectURL(data);
        });
        this.socket.addEventListener('close', e => {
            console.log('close', e);
        });
        this.socket.addEventListener('error', e => {
            console.log('error', e);
        });
    }

    close() {
        clearInterval(this.timer);
        if (this.socket) this.socket.close();
    }
}

export default new WebSocketClient();
