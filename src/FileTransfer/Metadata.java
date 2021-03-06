/**
 * Autogenerated by Thrift Compiler (1.0.0-dev)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package FileTransfer;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (1.0.0-dev)", date = "2018-07-20")
public class Metadata implements org.apache.thrift.TBase<Metadata, Metadata._Fields>, java.io.Serializable, Cloneable, Comparable<Metadata> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("Metadata");

  private static final org.apache.thrift.protocol.TField SRC_PATH_FIELD_DESC = new org.apache.thrift.protocol.TField("srcPath", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField DES_PATH_FIELD_DESC = new org.apache.thrift.protocol.TField("desPath", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField CHECK_SUM_FIELD_DESC = new org.apache.thrift.protocol.TField("checkSum", org.apache.thrift.protocol.TType.I64, (short)3);
  private static final org.apache.thrift.protocol.TField NUM_OF_CHUNKS_FIELD_DESC = new org.apache.thrift.protocol.TField("numOfChunks", org.apache.thrift.protocol.TType.I64, (short)4);
  private static final org.apache.thrift.protocol.TField SIZE_FIELD_DESC = new org.apache.thrift.protocol.TField("size", org.apache.thrift.protocol.TType.I64, (short)5);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new MetadataStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new MetadataTupleSchemeFactory();

  public @org.apache.thrift.annotation.Nullable java.lang.String srcPath; // required
  public @org.apache.thrift.annotation.Nullable java.lang.String desPath; // required
  public long checkSum; // required
  public long numOfChunks; // required
  public long size; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    SRC_PATH((short)1, "srcPath"),
    DES_PATH((short)2, "desPath"),
    CHECK_SUM((short)3, "checkSum"),
    NUM_OF_CHUNKS((short)4, "numOfChunks"),
    SIZE((short)5, "size");

    private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

    static {
      for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    @org.apache.thrift.annotation.Nullable
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // SRC_PATH
          return SRC_PATH;
        case 2: // DES_PATH
          return DES_PATH;
        case 3: // CHECK_SUM
          return CHECK_SUM;
        case 4: // NUM_OF_CHUNKS
          return NUM_OF_CHUNKS;
        case 5: // SIZE
          return SIZE;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    @org.apache.thrift.annotation.Nullable
    public static _Fields findByName(java.lang.String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final java.lang.String _fieldName;

    _Fields(short thriftId, java.lang.String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public java.lang.String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __CHECKSUM_ISSET_ID = 0;
  private static final int __NUMOFCHUNKS_ISSET_ID = 1;
  private static final int __SIZE_ISSET_ID = 2;
  private byte __isset_bitfield = 0;
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.SRC_PATH, new org.apache.thrift.meta_data.FieldMetaData("srcPath", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.DES_PATH, new org.apache.thrift.meta_data.FieldMetaData("desPath", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.CHECK_SUM, new org.apache.thrift.meta_data.FieldMetaData("checkSum", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.NUM_OF_CHUNKS, new org.apache.thrift.meta_data.FieldMetaData("numOfChunks", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.SIZE, new org.apache.thrift.meta_data.FieldMetaData("size", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(Metadata.class, metaDataMap);
  }

  public Metadata() {
  }

  public Metadata(
    java.lang.String srcPath,
    java.lang.String desPath,
    long checkSum,
    long numOfChunks,
    long size)
  {
    this();
    this.srcPath = srcPath;
    this.desPath = desPath;
    this.checkSum = checkSum;
    setCheckSumIsSet(true);
    this.numOfChunks = numOfChunks;
    setNumOfChunksIsSet(true);
    this.size = size;
    setSizeIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public Metadata(Metadata other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.isSetSrcPath()) {
      this.srcPath = other.srcPath;
    }
    if (other.isSetDesPath()) {
      this.desPath = other.desPath;
    }
    this.checkSum = other.checkSum;
    this.numOfChunks = other.numOfChunks;
    this.size = other.size;
  }

  public Metadata deepCopy() {
    return new Metadata(this);
  }

  @Override
  public void clear() {
    this.srcPath = null;
    this.desPath = null;
    setCheckSumIsSet(false);
    this.checkSum = 0;
    setNumOfChunksIsSet(false);
    this.numOfChunks = 0;
    setSizeIsSet(false);
    this.size = 0;
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.String getSrcPath() {
    return this.srcPath;
  }

  public Metadata setSrcPath(@org.apache.thrift.annotation.Nullable java.lang.String srcPath) {
    this.srcPath = srcPath;
    return this;
  }

  public void unsetSrcPath() {
    this.srcPath = null;
  }

  /** Returns true if field srcPath is set (has been assigned a value) and false otherwise */
  public boolean isSetSrcPath() {
    return this.srcPath != null;
  }

  public void setSrcPathIsSet(boolean value) {
    if (!value) {
      this.srcPath = null;
    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.String getDesPath() {
    return this.desPath;
  }

  public Metadata setDesPath(@org.apache.thrift.annotation.Nullable java.lang.String desPath) {
    this.desPath = desPath;
    return this;
  }

  public void unsetDesPath() {
    this.desPath = null;
  }

  /** Returns true if field desPath is set (has been assigned a value) and false otherwise */
  public boolean isSetDesPath() {
    return this.desPath != null;
  }

  public void setDesPathIsSet(boolean value) {
    if (!value) {
      this.desPath = null;
    }
  }

  public long getCheckSum() {
    return this.checkSum;
  }

  public Metadata setCheckSum(long checkSum) {
    this.checkSum = checkSum;
    setCheckSumIsSet(true);
    return this;
  }

  public void unsetCheckSum() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __CHECKSUM_ISSET_ID);
  }

  /** Returns true if field checkSum is set (has been assigned a value) and false otherwise */
  public boolean isSetCheckSum() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __CHECKSUM_ISSET_ID);
  }

  public void setCheckSumIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __CHECKSUM_ISSET_ID, value);
  }

  public long getNumOfChunks() {
    return this.numOfChunks;
  }

  public Metadata setNumOfChunks(long numOfChunks) {
    this.numOfChunks = numOfChunks;
    setNumOfChunksIsSet(true);
    return this;
  }

  public void unsetNumOfChunks() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __NUMOFCHUNKS_ISSET_ID);
  }

  /** Returns true if field numOfChunks is set (has been assigned a value) and false otherwise */
  public boolean isSetNumOfChunks() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __NUMOFCHUNKS_ISSET_ID);
  }

  public void setNumOfChunksIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __NUMOFCHUNKS_ISSET_ID, value);
  }

  public long getSize() {
    return this.size;
  }

  public Metadata setSize(long size) {
    this.size = size;
    setSizeIsSet(true);
    return this;
  }

  public void unsetSize() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __SIZE_ISSET_ID);
  }

  /** Returns true if field size is set (has been assigned a value) and false otherwise */
  public boolean isSetSize() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __SIZE_ISSET_ID);
  }

  public void setSizeIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __SIZE_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
    switch (field) {
    case SRC_PATH:
      if (value == null) {
        unsetSrcPath();
      } else {
        setSrcPath((java.lang.String)value);
      }
      break;

    case DES_PATH:
      if (value == null) {
        unsetDesPath();
      } else {
        setDesPath((java.lang.String)value);
      }
      break;

    case CHECK_SUM:
      if (value == null) {
        unsetCheckSum();
      } else {
        setCheckSum((java.lang.Long)value);
      }
      break;

    case NUM_OF_CHUNKS:
      if (value == null) {
        unsetNumOfChunks();
      } else {
        setNumOfChunks((java.lang.Long)value);
      }
      break;

    case SIZE:
      if (value == null) {
        unsetSize();
      } else {
        setSize((java.lang.Long)value);
      }
      break;

    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case SRC_PATH:
      return getSrcPath();

    case DES_PATH:
      return getDesPath();

    case CHECK_SUM:
      return getCheckSum();

    case NUM_OF_CHUNKS:
      return getNumOfChunks();

    case SIZE:
      return getSize();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case SRC_PATH:
      return isSetSrcPath();
    case DES_PATH:
      return isSetDesPath();
    case CHECK_SUM:
      return isSetCheckSum();
    case NUM_OF_CHUNKS:
      return isSetNumOfChunks();
    case SIZE:
      return isSetSize();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof Metadata)
      return this.equals((Metadata)that);
    return false;
  }

  public boolean equals(Metadata that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_srcPath = true && this.isSetSrcPath();
    boolean that_present_srcPath = true && that.isSetSrcPath();
    if (this_present_srcPath || that_present_srcPath) {
      if (!(this_present_srcPath && that_present_srcPath))
        return false;
      if (!this.srcPath.equals(that.srcPath))
        return false;
    }

    boolean this_present_desPath = true && this.isSetDesPath();
    boolean that_present_desPath = true && that.isSetDesPath();
    if (this_present_desPath || that_present_desPath) {
      if (!(this_present_desPath && that_present_desPath))
        return false;
      if (!this.desPath.equals(that.desPath))
        return false;
    }

    boolean this_present_checkSum = true;
    boolean that_present_checkSum = true;
    if (this_present_checkSum || that_present_checkSum) {
      if (!(this_present_checkSum && that_present_checkSum))
        return false;
      if (this.checkSum != that.checkSum)
        return false;
    }

    boolean this_present_numOfChunks = true;
    boolean that_present_numOfChunks = true;
    if (this_present_numOfChunks || that_present_numOfChunks) {
      if (!(this_present_numOfChunks && that_present_numOfChunks))
        return false;
      if (this.numOfChunks != that.numOfChunks)
        return false;
    }

    boolean this_present_size = true;
    boolean that_present_size = true;
    if (this_present_size || that_present_size) {
      if (!(this_present_size && that_present_size))
        return false;
      if (this.size != that.size)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetSrcPath()) ? 131071 : 524287);
    if (isSetSrcPath())
      hashCode = hashCode * 8191 + srcPath.hashCode();

    hashCode = hashCode * 8191 + ((isSetDesPath()) ? 131071 : 524287);
    if (isSetDesPath())
      hashCode = hashCode * 8191 + desPath.hashCode();

    hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(checkSum);

    hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(numOfChunks);

    hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(size);

    return hashCode;
  }

  @Override
  public int compareTo(Metadata other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetSrcPath()).compareTo(other.isSetSrcPath());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSrcPath()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.srcPath, other.srcPath);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetDesPath()).compareTo(other.isSetDesPath());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetDesPath()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.desPath, other.desPath);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetCheckSum()).compareTo(other.isSetCheckSum());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetCheckSum()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.checkSum, other.checkSum);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetNumOfChunks()).compareTo(other.isSetNumOfChunks());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetNumOfChunks()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.numOfChunks, other.numOfChunks);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetSize()).compareTo(other.isSetSize());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSize()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.size, other.size);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  @org.apache.thrift.annotation.Nullable
  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    scheme(iprot).read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    scheme(oprot).write(oprot, this);
  }

  @Override
  public java.lang.String toString() {
    java.lang.StringBuilder sb = new java.lang.StringBuilder("Metadata(");
    boolean first = true;

    sb.append("srcPath:");
    if (this.srcPath == null) {
      sb.append("null");
    } else {
      sb.append(this.srcPath);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("desPath:");
    if (this.desPath == null) {
      sb.append("null");
    } else {
      sb.append(this.desPath);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("checkSum:");
    sb.append(this.checkSum);
    first = false;
    if (!first) sb.append(", ");
    sb.append("numOfChunks:");
    sb.append(this.numOfChunks);
    first = false;
    if (!first) sb.append(", ");
    sb.append("size:");
    sb.append(this.size);
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (srcPath == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'srcPath' was not present! Struct: " + toString());
    }
    if (desPath == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'desPath' was not present! Struct: " + toString());
    }
    // alas, we cannot check 'checkSum' because it's a primitive and you chose the non-beans generator.
    // alas, we cannot check 'numOfChunks' because it's a primitive and you chose the non-beans generator.
    // alas, we cannot check 'size' because it's a primitive and you chose the non-beans generator.
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class MetadataStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MetadataStandardScheme getScheme() {
      return new MetadataStandardScheme();
    }
  }

  private static class MetadataStandardScheme extends org.apache.thrift.scheme.StandardScheme<Metadata> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, Metadata struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // SRC_PATH
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.srcPath = iprot.readString();
              struct.setSrcPathIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // DES_PATH
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.desPath = iprot.readString();
              struct.setDesPathIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // CHECK_SUM
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.checkSum = iprot.readI64();
              struct.setCheckSumIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // NUM_OF_CHUNKS
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.numOfChunks = iprot.readI64();
              struct.setNumOfChunksIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // SIZE
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.size = iprot.readI64();
              struct.setSizeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      if (!struct.isSetCheckSum()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'checkSum' was not found in serialized data! Struct: " + toString());
      }
      if (!struct.isSetNumOfChunks()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'numOfChunks' was not found in serialized data! Struct: " + toString());
      }
      if (!struct.isSetSize()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'size' was not found in serialized data! Struct: " + toString());
      }
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, Metadata struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.srcPath != null) {
        oprot.writeFieldBegin(SRC_PATH_FIELD_DESC);
        oprot.writeString(struct.srcPath);
        oprot.writeFieldEnd();
      }
      if (struct.desPath != null) {
        oprot.writeFieldBegin(DES_PATH_FIELD_DESC);
        oprot.writeString(struct.desPath);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(CHECK_SUM_FIELD_DESC);
      oprot.writeI64(struct.checkSum);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(NUM_OF_CHUNKS_FIELD_DESC);
      oprot.writeI64(struct.numOfChunks);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(SIZE_FIELD_DESC);
      oprot.writeI64(struct.size);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class MetadataTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MetadataTupleScheme getScheme() {
      return new MetadataTupleScheme();
    }
  }

  private static class MetadataTupleScheme extends org.apache.thrift.scheme.TupleScheme<Metadata> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, Metadata struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      oprot.writeString(struct.srcPath);
      oprot.writeString(struct.desPath);
      oprot.writeI64(struct.checkSum);
      oprot.writeI64(struct.numOfChunks);
      oprot.writeI64(struct.size);
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, Metadata struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      struct.srcPath = iprot.readString();
      struct.setSrcPathIsSet(true);
      struct.desPath = iprot.readString();
      struct.setDesPathIsSet(true);
      struct.checkSum = iprot.readI64();
      struct.setCheckSumIsSet(true);
      struct.numOfChunks = iprot.readI64();
      struct.setNumOfChunksIsSet(true);
      struct.size = iprot.readI64();
      struct.setSizeIsSet(true);
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

