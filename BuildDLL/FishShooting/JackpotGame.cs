//------------------------------------------------------------------------------
// <auto-generated>
//     This code was generated by a tool.
//
//     Changes to this file may cause incorrect behavior and will be lost if
//     the code is regenerated.
// </auto-generated>
//------------------------------------------------------------------------------

// Generated from: Proto/JackpotGame.proto
namespace com.nope.fishing
{
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"JoinRoomRequest")]
  public partial class JoinRoomRequest : global::ProtoBuf.IExtensible
  {
    public JoinRoomRequest() {}
    
    private int _roomId = default(int);
    [global::ProtoBuf.ProtoMember(1, IsRequired = false, Name=@"roomId", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int roomId
    {
      get { return _roomId; }
      set { _roomId = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"RoomStakeInfo")]
  public partial class RoomStakeInfo : global::ProtoBuf.IExtensible
  {
    public RoomStakeInfo() {}
    
    private long _stepMoney = default(long);
    [global::ProtoBuf.ProtoMember(2, IsRequired = false, Name=@"stepMoney", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(long))]
    public long stepMoney
    {
      get { return _stepMoney; }
      set { _stepMoney = value; }
    }
    private long _defaultStep = default(long);
    [global::ProtoBuf.ProtoMember(3, IsRequired = false, Name=@"defaultStep", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(long))]
    public long defaultStep
    {
      get { return _defaultStep; }
      set { _defaultStep = value; }
    }
    private long _maxStep = default(long);
    [global::ProtoBuf.ProtoMember(4, IsRequired = false, Name=@"maxStep", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(long))]
    public long maxStep
    {
      get { return _maxStep; }
      set { _maxStep = value; }
    }
    private long _minStep = default(long);
    [global::ProtoBuf.ProtoMember(5, IsRequired = false, Name=@"minStep", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(long))]
    public long minStep
    {
      get { return _minStep; }
      set { _minStep = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"RoomPotResponse")]
  public partial class RoomPotResponse : global::ProtoBuf.IExtensible
  {
    public RoomPotResponse() {}
    
    private int _roomId = default(int);
    [global::ProtoBuf.ProtoMember(2, IsRequired = false, Name=@"roomId", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int roomId
    {
      get { return _roomId; }
      set { _roomId = value; }
    }
    private long _pot = default(long);
    [global::ProtoBuf.ProtoMember(3, IsRequired = false, Name=@"pot", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(long))]
    public long pot
    {
      get { return _pot; }
      set { _pot = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
}