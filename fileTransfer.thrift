namespace java FileTransfer
namespace cpp FileTransfer

const i32 CHUNK_MAX_SIZE = 700; // the maximum size allowed in 1 data chunk

// The metadata of a file 
struct Metadata {
   1: required string srcPath;
   2: required string desPath;
   3: required i64 checkSum;
   4: required i64 numOfChunks;
   5: required i64 size;
}

// The data of each file
struct DataChunk {
    1: required string srcPath;
    2: required binary buffer;
    3: required i64 offset;
}

service FileTransfer {
    list<list<i64>> checkMetaData(1: Metadata header),
    oneway void sendMetaData(1: Metadata header),
    oneway void sendDataChunk(1: DataChunk chunk),
    void updateChecksum(1: string srcPath, 2: i64 checkSum)
}
