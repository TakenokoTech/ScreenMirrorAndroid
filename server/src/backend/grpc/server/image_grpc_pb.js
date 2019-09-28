// GENERATED CODE -- DO NOT EDIT!

'use strict';
var grpc = require('grpc');
var image_pb = require('./image_pb.js');

function serialize_proto_ImageReply(arg) {
  if (!(arg instanceof image_pb.ImageReply)) {
    throw new Error('Expected argument of type proto.ImageReply');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_proto_ImageReply(buffer_arg) {
  return image_pb.ImageReply.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_proto_ImageRequest(arg) {
  if (!(arg instanceof image_pb.ImageRequest)) {
    throw new Error('Expected argument of type proto.ImageRequest');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_proto_ImageRequest(buffer_arg) {
  return image_pb.ImageRequest.deserializeBinary(new Uint8Array(buffer_arg));
}


var ImageDomainService = exports.ImageDomainService = {
  getImage: {
    path: '/proto.ImageDomain/getImage',
    requestStream: false,
    responseStream: false,
    requestType: image_pb.ImageRequest,
    responseType: image_pb.ImageReply,
    requestSerialize: serialize_proto_ImageRequest,
    requestDeserialize: deserialize_proto_ImageRequest,
    responseSerialize: serialize_proto_ImageReply,
    responseDeserialize: deserialize_proto_ImageReply,
  },
};

exports.ImageDomainClient = grpc.makeGenericClientConstructor(ImageDomainService);
