import { Server, ServerCredentials, credentials } from 'grpc';
import { ImageRequest, ImageReply } from './backend/grpc/server/image_pb';
import { ImageDomainService, ImageDomainClient, IImageDomainClient } from './backend/grpc/server/image_grpc_pb';
import Response from 'express';
import { Log } from './utils/Log';

function getImage(call: any, callback: any) {
    Log.info('getImage', call);
    const request: ImageRequest = call.request;
    const reply: ImageReply = new ImageReply();
    callback(null, reply);
}

const grpcServer: Server = new Server();
grpcServer.addService(ImageDomainService, { getImage });
grpcServer.bind('localhost:50051', ServerCredentials.createInsecure());
grpcServer.start();
Log.info('start');

// const client: IImageDomainClient = new ImageDomainClient(`localhost:50051`, credentials.createInsecure());
// client.getImage(new ImageRequest(), (err: any, response: any) => {});
