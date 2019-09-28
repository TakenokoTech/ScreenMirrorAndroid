import * as jspb from "google-protobuf"

export class ImageRequest extends jspb.Message {
  getImage(): Uint8Array | string;
  getImage_asU8(): Uint8Array;
  getImage_asB64(): string;
  setImage(value: Uint8Array | string): void;

  serializeBinary(): Uint8Array;
  toObject(includeInstance?: boolean): ImageRequest.AsObject;
  static toObject(includeInstance: boolean, msg: ImageRequest): ImageRequest.AsObject;
  static serializeBinaryToWriter(message: ImageRequest, writer: jspb.BinaryWriter): void;
  static deserializeBinary(bytes: Uint8Array): ImageRequest;
  static deserializeBinaryFromReader(message: ImageRequest, reader: jspb.BinaryReader): ImageRequest;
}

export namespace ImageRequest {
  export type AsObject = {
    image: Uint8Array | string,
  }
}

export class ImageReply extends jspb.Message {
  getImage(): Uint8Array | string;
  getImage_asU8(): Uint8Array;
  getImage_asB64(): string;
  setImage(value: Uint8Array | string): void;

  serializeBinary(): Uint8Array;
  toObject(includeInstance?: boolean): ImageReply.AsObject;
  static toObject(includeInstance: boolean, msg: ImageReply): ImageReply.AsObject;
  static serializeBinaryToWriter(message: ImageReply, writer: jspb.BinaryWriter): void;
  static deserializeBinary(bytes: Uint8Array): ImageReply;
  static deserializeBinaryFromReader(message: ImageReply, reader: jspb.BinaryReader): ImageReply;
}

export namespace ImageReply {
  export type AsObject = {
    image: Uint8Array | string,
  }
}

