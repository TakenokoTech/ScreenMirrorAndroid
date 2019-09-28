import { ImageDomainClient } from '../backend/grpc/front/image_grpc_web_pb';
import { ImageRequest } from '../backend/grpc/front/image_pb';

class GrpcClient {
    private timer = null;
    private client: ImageDomainClient;

    open() {
        this.client = new ImageDomainClient(`http://localhost:50051`, {}, {});
        this.timer = setInterval(() => this.send(), 60);
    }

    send() {
        const request = new ImageRequest();
        this.client.getImage(request, {}, (err: any, response: any) => {
            console.log(err);
            console.log(response);
        });
    }

    close() {
        clearInterval(this.timer);
        if (this.socket) this.client.close();
    }
}

export default new GrpcClient();
