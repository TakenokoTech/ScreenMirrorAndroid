import * as grpcWeb from 'grpc-web';

import {
  ImageReply,
  ImageRequest} from './image_pb';

export class ImageDomainClient {
  constructor (hostname: string,
               credentials: null | { [index: string]: string; },
               options: null | { [index: string]: string; });

  getImage(
    request: ImageRequest,
    metadata: grpcWeb.Metadata | undefined,
    callback: (err: grpcWeb.Error,
               response: ImageReply) => void
  ): grpcWeb.ClientReadableStream<ImageReply>;

}

export class ImageDomainPromiseClient {
  constructor (hostname: string,
               credentials: null | { [index: string]: string; },
               options: null | { [index: string]: string; });

  getImage(
    request: ImageRequest,
    metadata?: grpcWeb.Metadata
  ): Promise<ImageReply>;

}

