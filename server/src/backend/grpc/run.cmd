set PLUGIN_TS=.\..\..\..\node_modules\.bin\protoc-gen-ts.cmd
set PLUGIN_GRPC=.\..\..\..\node_modules\.bin\grpc_tools_node_protoc_plugin.cmd
set SERVER_DIST_DIR=./server/

protoc ^
--js_out=import_style=commonjs,binary:"%SERVER_DIST_DIR%" ^
--ts_out=import_style=commonjs,binary:"%SERVER_DIST_DIR%" ^
--grpc_out="%SERVER_DIST_DIR%" ^
--plugin=protoc-gen-grpc="%PLUGIN_GRPC%" ^
--plugin=protoc-gen-ts="%PLUGIN_TS%" ^
--proto_path=./ ^
-I "%SERVER_DIST_DIR%" ^
./image.proto


set FRONT_DIST_DIR=./front/

protoc ^
--js_out=import_style=commonjs:"%FRONT_DIST_DIR%" ^
--grpc-web_out=import_style=commonjs+dts,mode=grpcwebtext:"%FRONT_DIST_DIR%" ^
--proto_path=./ ^
-I "%FRONT_DIST_DIR%" ^
image.proto