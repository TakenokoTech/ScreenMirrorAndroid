// package: proto
// file: image.proto

/* tslint:disable */

import * as grpc from "grpc";
import * as image_pb from "./image_pb";

interface IImageDomainService extends grpc.ServiceDefinition<grpc.UntypedServiceImplementation> {
    getImage: IImageDomainService_IgetImage;
}

interface IImageDomainService_IgetImage extends grpc.MethodDefinition<image_pb.ImageRequest, image_pb.ImageReply> {
    path: string; // "/proto.ImageDomain/getImage"
    requestStream: boolean; // false
    responseStream: boolean; // false
    requestSerialize: grpc.serialize<image_pb.ImageRequest>;
    requestDeserialize: grpc.deserialize<image_pb.ImageRequest>;
    responseSerialize: grpc.serialize<image_pb.ImageReply>;
    responseDeserialize: grpc.deserialize<image_pb.ImageReply>;
}

export const ImageDomainService: IImageDomainService;

export interface IImageDomainServer {
    getImage: grpc.handleUnaryCall<image_pb.ImageRequest, image_pb.ImageReply>;
}

export interface IImageDomainClient {
    getImage(request: image_pb.ImageRequest, callback: (error: grpc.ServiceError | null, response: image_pb.ImageReply) => void): grpc.ClientUnaryCall;
    getImage(request: image_pb.ImageRequest, metadata: grpc.Metadata, callback: (error: grpc.ServiceError | null, response: image_pb.ImageReply) => void): grpc.ClientUnaryCall;
    getImage(request: image_pb.ImageRequest, metadata: grpc.Metadata, options: Partial<grpc.CallOptions>, callback: (error: grpc.ServiceError | null, response: image_pb.ImageReply) => void): grpc.ClientUnaryCall;
}

export class ImageDomainClient extends grpc.Client implements IImageDomainClient {
    constructor(address: string, credentials: grpc.ChannelCredentials, options?: object);
    public getImage(request: image_pb.ImageRequest, callback: (error: grpc.ServiceError | null, response: image_pb.ImageReply) => void): grpc.ClientUnaryCall;
    public getImage(request: image_pb.ImageRequest, metadata: grpc.Metadata, callback: (error: grpc.ServiceError | null, response: image_pb.ImageReply) => void): grpc.ClientUnaryCall;
    public getImage(request: image_pb.ImageRequest, metadata: grpc.Metadata, options: Partial<grpc.CallOptions>, callback: (error: grpc.ServiceError | null, response: image_pb.ImageReply) => void): grpc.ClientUnaryCall;
}
