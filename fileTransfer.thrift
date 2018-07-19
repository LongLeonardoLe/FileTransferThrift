namespace java FileTransfer
namespace cpp FileTransfer

const i32 CHUNK_MAX_SIZE = 2048; // the maximum size allowed in 1 data chunk

// The metadata of a file includes:
// [REQUIRED] srcPath: path of source file
// [REQUIRED] desPath: path destination
// [REQUIRED] checkSum: hased check sum of the file 
// [OPTIONAL] size: size of the file
struct Metadata {
   1: required string srcPath;
   2: required string desPath;
   3: required i64 checkSum;
   4: required i32 numOfChunks;
   5: optional i32 size;
}

// The data of each file:
// [REQUIRED] srcPath: since source path is unique for each file
// [REQUIRED] buffer: the byte array of the chunk
// [REQUIRED] index: index of the chunk in the array of chunks
struct DataChunk {
    1: required string srcPath;
    2: required binary buffer;
    3: required i32 offset;
}

service FileTransfer {
    oneway void sendMetaData(1: Metadata header),
    oneway void sendDataChunk(1: DataChunk chunk),
    void updateChecksum(1: string srcPath, 2: i64 checkSum)
}
