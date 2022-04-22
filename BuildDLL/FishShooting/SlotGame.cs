//------------------------------------------------------------------------------
// <auto-generated>
//     This code was generated by a tool.
//
//     Changes to this file may cause incorrect behavior and will be lost if
//     the code is regenerated.
// </auto-generated>
//------------------------------------------------------------------------------

// Generated from: Proto/SlotGame.proto
// Note: requires additional types generated from: JackpotGame.proto
namespace com.nope.fishing
{
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"SlotJoinRequest")]
  public partial class SlotJoinRequest : global::ProtoBuf.IExtensible
  {
    public SlotJoinRequest() {}
    
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
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"PlayerUpdateLinesRequest")]
  public partial class PlayerUpdateLinesRequest : global::ProtoBuf.IExtensible
  {
    public PlayerUpdateLinesRequest() {}
    
    private readonly global::System.Collections.Generic.List<int> _line = new global::System.Collections.Generic.List<int>();
    [global::ProtoBuf.ProtoMember(1, Name=@"line", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public global::System.Collections.Generic.List<int> line
    {
      get { return _line; }
    }
  
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"PlayerUpdateLinesResponse")]
  public partial class PlayerUpdateLinesResponse : global::ProtoBuf.IExtensible
  {
    public PlayerUpdateLinesResponse() {}
    
    private readonly global::System.Collections.Generic.List<int> _line = new global::System.Collections.Generic.List<int>();
    [global::ProtoBuf.ProtoMember(1, Name=@"line", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public global::System.Collections.Generic.List<int> line
    {
      get { return _line; }
    }
  
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"ResultItemIndex")]
  public partial class ResultItemIndex : global::ProtoBuf.IExtensible
  {
    public ResultItemIndex() {}
    
    private int _col = default(int);
    [global::ProtoBuf.ProtoMember(1, IsRequired = false, Name=@"col", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int col
    {
      get { return _col; }
      set { _col = value; }
    }
    private int _row = default(int);
    [global::ProtoBuf.ProtoMember(2, IsRequired = false, Name=@"row", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int row
    {
      get { return _row; }
      set { _row = value; }
    }
    private int _itemId = default(int);
    [global::ProtoBuf.ProtoMember(3, IsRequired = false, Name=@"itemId", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int itemId
    {
      get { return _itemId; }
      set { _itemId = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"SpinSlotRequest")]
  public partial class SpinSlotRequest : global::ProtoBuf.IExtensible
  {
    public SpinSlotRequest() {}
    
    private readonly global::System.Collections.Generic.List<int> _line = new global::System.Collections.Generic.List<int>();
    [global::ProtoBuf.ProtoMember(1, Name=@"line", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public global::System.Collections.Generic.List<int> line
    {
      get { return _line; }
    }
  
    private int _stakeStep = default(int);
    [global::ProtoBuf.ProtoMember(2, IsRequired = false, Name=@"stakeStep", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int stakeStep
    {
      get { return _stakeStep; }
      set { _stakeStep = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"WinResultProto")]
  public partial class WinResultProto : global::ProtoBuf.IExtensible
  {
    public WinResultProto() {}
    
    private long _money = default(long);
    [global::ProtoBuf.ProtoMember(1, IsRequired = false, Name=@"money", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(long))]
    public long money
    {
      get { return _money; }
      set { _money = value; }
    }
    private readonly global::System.Collections.Generic.List<com.nope.fishing.ResultItemIndex> _itemIndex = new global::System.Collections.Generic.List<com.nope.fishing.ResultItemIndex>();
    [global::ProtoBuf.ProtoMember(2, Name=@"itemIndex", DataFormat = global::ProtoBuf.DataFormat.Default)]
    public global::System.Collections.Generic.List<com.nope.fishing.ResultItemIndex> itemIndex
    {
      get { return _itemIndex; }
    }
  
    private int _line = default(int);
    [global::ProtoBuf.ProtoMember(3, IsRequired = false, Name=@"line", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int line
    {
      get { return _line; }
      set { _line = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"BonusWinResultProto")]
  public partial class BonusWinResultProto : global::ProtoBuf.IExtensible
  {
    public BonusWinResultProto() {}
    
    private readonly global::System.Collections.Generic.List<long> _moneyList = new global::System.Collections.Generic.List<long>();
    [global::ProtoBuf.ProtoMember(1, Name=@"moneyList", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public global::System.Collections.Generic.List<long> moneyList
    {
      get { return _moneyList; }
    }
  
    private com.nope.fishing.WinResultProto _winResult = null;
    [global::ProtoBuf.ProtoMember(2, IsRequired = false, Name=@"winResult", DataFormat = global::ProtoBuf.DataFormat.Default)]
    [global::System.ComponentModel.DefaultValue(null)]
    public com.nope.fishing.WinResultProto winResult
    {
      get { return _winResult; }
      set { _winResult = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"SpinSlotResponse")]
  public partial class SpinSlotResponse : global::ProtoBuf.IExtensible
  {
    public SpinSlotResponse() {}
    
    private readonly global::System.Collections.Generic.List<int> _line = new global::System.Collections.Generic.List<int>();
    [global::ProtoBuf.ProtoMember(1, Name=@"line", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public global::System.Collections.Generic.List<int> line
    {
      get { return _line; }
    }
  
    private readonly global::System.Collections.Generic.List<com.nope.fishing.ResultItemIndex> _resultItemIndex = new global::System.Collections.Generic.List<com.nope.fishing.ResultItemIndex>();
    [global::ProtoBuf.ProtoMember(2, Name=@"resultItemIndex", DataFormat = global::ProtoBuf.DataFormat.Default)]
    public global::System.Collections.Generic.List<com.nope.fishing.ResultItemIndex> resultItemIndex
    {
      get { return _resultItemIndex; }
    }
  
    private readonly global::System.Collections.Generic.List<com.nope.fishing.WinResultProto> _normal = new global::System.Collections.Generic.List<com.nope.fishing.WinResultProto>();
    [global::ProtoBuf.ProtoMember(10, Name=@"normal", DataFormat = global::ProtoBuf.DataFormat.Default)]
    public global::System.Collections.Generic.List<com.nope.fishing.WinResultProto> normal
    {
      get { return _normal; }
    }
  
    private readonly global::System.Collections.Generic.List<com.nope.fishing.WinResultProto> _jackpot = new global::System.Collections.Generic.List<com.nope.fishing.WinResultProto>();
    [global::ProtoBuf.ProtoMember(11, Name=@"jackpot", DataFormat = global::ProtoBuf.DataFormat.Default)]
    public global::System.Collections.Generic.List<com.nope.fishing.WinResultProto> jackpot
    {
      get { return _jackpot; }
    }
  
    private com.nope.fishing.WinResultProto _freeSpin = null;
    [global::ProtoBuf.ProtoMember(12, IsRequired = false, Name=@"freeSpin", DataFormat = global::ProtoBuf.DataFormat.Default)]
    [global::System.ComponentModel.DefaultValue(null)]
    public com.nope.fishing.WinResultProto freeSpin
    {
      get { return _freeSpin; }
      set { _freeSpin = value; }
    }
    private readonly global::System.Collections.Generic.List<com.nope.fishing.BonusWinResultProto> _bonus = new global::System.Collections.Generic.List<com.nope.fishing.BonusWinResultProto>();
    [global::ProtoBuf.ProtoMember(13, Name=@"bonus", DataFormat = global::ProtoBuf.DataFormat.Default)]
    public global::System.Collections.Generic.List<com.nope.fishing.BonusWinResultProto> bonus
    {
      get { return _bonus; }
    }
  
    private long _money = default(long);
    [global::ProtoBuf.ProtoMember(4, IsRequired = false, Name=@"money", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(long))]
    public long money
    {
      get { return _money; }
      set { _money = value; }
    }
    private int _errorCode = default(int);
    [global::ProtoBuf.ProtoMember(5, IsRequired = false, Name=@"errorCode", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int errorCode
    {
      get { return _errorCode; }
      set { _errorCode = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"MainSlotReward")]
  public partial class MainSlotReward : global::ProtoBuf.IExtensible
  {
    public MainSlotReward() {}
    
    private int _itemId = default(int);
    [global::ProtoBuf.ProtoMember(1, IsRequired = false, Name=@"itemId", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int itemId
    {
      get { return _itemId; }
      set { _itemId = value; }
    }
    private int _x2 = default(int);
    [global::ProtoBuf.ProtoMember(2, IsRequired = false, Name=@"x2", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int x2
    {
      get { return _x2; }
      set { _x2 = value; }
    }
    private int _x3 = default(int);
    [global::ProtoBuf.ProtoMember(3, IsRequired = false, Name=@"x3", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int x3
    {
      get { return _x3; }
      set { _x3 = value; }
    }
    private int _x4 = default(int);
    [global::ProtoBuf.ProtoMember(4, IsRequired = false, Name=@"x4", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int x4
    {
      get { return _x4; }
      set { _x4 = value; }
    }
    private int _x5 = default(int);
    [global::ProtoBuf.ProtoMember(5, IsRequired = false, Name=@"x5", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int x5
    {
      get { return _x5; }
      set { _x5 = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"MiniSlotReward")]
  public partial class MiniSlotReward : global::ProtoBuf.IExtensible
  {
    public MiniSlotReward() {}
    
    private int _itemId = default(int);
    [global::ProtoBuf.ProtoMember(1, IsRequired = false, Name=@"itemId", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int itemId
    {
      get { return _itemId; }
      set { _itemId = value; }
    }
    private int _x2 = default(int);
    [global::ProtoBuf.ProtoMember(2, IsRequired = false, Name=@"x2", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int x2
    {
      get { return _x2; }
      set { _x2 = value; }
    }
    private int _x3 = default(int);
    [global::ProtoBuf.ProtoMember(3, IsRequired = false, Name=@"x3", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int x3
    {
      get { return _x3; }
      set { _x3 = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"SlotRoomInfo")]
  public partial class SlotRoomInfo : global::ProtoBuf.IExtensible
  {
    public SlotRoomInfo() {}
    
    private int _roomId = default(int);
    [global::ProtoBuf.ProtoMember(1, IsRequired = false, Name=@"roomId", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int roomId
    {
      get { return _roomId; }
      set { _roomId = value; }
    }
    private com.nope.fishing.RoomStakeInfo _roomStakeInfo = null;
    [global::ProtoBuf.ProtoMember(2, IsRequired = false, Name=@"roomStakeInfo", DataFormat = global::ProtoBuf.DataFormat.Default)]
    [global::System.ComponentModel.DefaultValue(null)]
    public com.nope.fishing.RoomStakeInfo roomStakeInfo
    {
      get { return _roomStakeInfo; }
      set { _roomStakeInfo = value; }
    }
    private long _potAmount = default(long);
    [global::ProtoBuf.ProtoMember(3, IsRequired = false, Name=@"potAmount", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(long))]
    public long potAmount
    {
      get { return _potAmount; }
      set { _potAmount = value; }
    }
    private int _errorCode = default(int);
    [global::ProtoBuf.ProtoMember(4, IsRequired = false, Name=@"errorCode", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int errorCode
    {
      get { return _errorCode; }
      set { _errorCode = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"SlotGameInfo")]
  public partial class SlotGameInfo : global::ProtoBuf.IExtensible
  {
    public SlotGameInfo() {}
    
    private readonly global::System.Collections.Generic.List<com.nope.fishing.SlotRoomInfo> _room = new global::System.Collections.Generic.List<com.nope.fishing.SlotRoomInfo>();
    [global::ProtoBuf.ProtoMember(1, Name=@"room", DataFormat = global::ProtoBuf.DataFormat.Default)]
    public global::System.Collections.Generic.List<com.nope.fishing.SlotRoomInfo> room
    {
      get { return _room; }
    }
  
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"MainSlotInfoResponse")]
  public partial class MainSlotInfoResponse : global::ProtoBuf.IExtensible
  {
    public MainSlotInfoResponse() {}
    
    private com.nope.fishing.SlotGameInfo _slotGameInfo = null;
    [global::ProtoBuf.ProtoMember(1, IsRequired = false, Name=@"slotGameInfo", DataFormat = global::ProtoBuf.DataFormat.Default)]
    [global::System.ComponentModel.DefaultValue(null)]
    public com.nope.fishing.SlotGameInfo slotGameInfo
    {
      get { return _slotGameInfo; }
      set { _slotGameInfo = value; }
    }
    private readonly global::System.Collections.Generic.List<com.nope.fishing.MainSlotReward> _reward = new global::System.Collections.Generic.List<com.nope.fishing.MainSlotReward>();
    [global::ProtoBuf.ProtoMember(2, Name=@"reward", DataFormat = global::ProtoBuf.DataFormat.Default)]
    public global::System.Collections.Generic.List<com.nope.fishing.MainSlotReward> reward
    {
      get { return _reward; }
    }
  
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"MiniSlotInfoResponse")]
  public partial class MiniSlotInfoResponse : global::ProtoBuf.IExtensible
  {
    public MiniSlotInfoResponse() {}
    
    private com.nope.fishing.SlotGameInfo _slotGameInfo = null;
    [global::ProtoBuf.ProtoMember(1, IsRequired = false, Name=@"slotGameInfo", DataFormat = global::ProtoBuf.DataFormat.Default)]
    [global::System.ComponentModel.DefaultValue(null)]
    public com.nope.fishing.SlotGameInfo slotGameInfo
    {
      get { return _slotGameInfo; }
      set { _slotGameInfo = value; }
    }
    private readonly global::System.Collections.Generic.List<com.nope.fishing.MiniSlotReward> _reward = new global::System.Collections.Generic.List<com.nope.fishing.MiniSlotReward>();
    [global::ProtoBuf.ProtoMember(2, Name=@"reward", DataFormat = global::ProtoBuf.DataFormat.Default)]
    public global::System.Collections.Generic.List<com.nope.fishing.MiniSlotReward> reward
    {
      get { return _reward; }
    }
  
    private int _roomIdDefault = default(int);
    [global::ProtoBuf.ProtoMember(3, IsRequired = false, Name=@"roomIdDefault", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int roomIdDefault
    {
      get { return _roomIdDefault; }
      set { _roomIdDefault = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"MiniSlotChangeRoomResponse")]
  public partial class MiniSlotChangeRoomResponse : global::ProtoBuf.IExtensible
  {
    public MiniSlotChangeRoomResponse() {}
    
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
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"SlotHistoryRecord")]
  public partial class SlotHistoryRecord : global::ProtoBuf.IExtensible
  {
    public SlotHistoryRecord() {}
    
    private long _id = default(long);
    [global::ProtoBuf.ProtoMember(1, IsRequired = false, Name=@"id", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(long))]
    public long id
    {
      get { return _id; }
      set { _id = value; }
    }
    private long _moneyBet = default(long);
    [global::ProtoBuf.ProtoMember(2, IsRequired = false, Name=@"moneyBet", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(long))]
    public long moneyBet
    {
      get { return _moneyBet; }
      set { _moneyBet = value; }
    }
    private long _moneyWin = default(long);
    [global::ProtoBuf.ProtoMember(3, IsRequired = false, Name=@"moneyWin", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(long))]
    public long moneyWin
    {
      get { return _moneyWin; }
      set { _moneyWin = value; }
    }
    private long _timestamp = default(long);
    [global::ProtoBuf.ProtoMember(4, IsRequired = false, Name=@"timestamp", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(long))]
    public long timestamp
    {
      get { return _timestamp; }
      set { _timestamp = value; }
    }
    private string _description = "";
    [global::ProtoBuf.ProtoMember(5, IsRequired = false, Name=@"description", DataFormat = global::ProtoBuf.DataFormat.Default)]
    [global::System.ComponentModel.DefaultValue("")]
    public string description
    {
      get { return _description; }
      set { _description = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"SlotHistoryInfoResponse")]
  public partial class SlotHistoryInfoResponse : global::ProtoBuf.IExtensible
  {
    public SlotHistoryInfoResponse() {}
    
    private readonly global::System.Collections.Generic.List<com.nope.fishing.SlotHistoryRecord> _record = new global::System.Collections.Generic.List<com.nope.fishing.SlotHistoryRecord>();
    [global::ProtoBuf.ProtoMember(1, Name=@"record", DataFormat = global::ProtoBuf.DataFormat.Default)]
    public global::System.Collections.Generic.List<com.nope.fishing.SlotHistoryRecord> record
    {
      get { return _record; }
    }
  
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"LeaveGameResponse")]
  public partial class LeaveGameResponse : global::ProtoBuf.IExtensible
  {
    public LeaveGameResponse() {}
    
    private int _errorCode = default(int);
    [global::ProtoBuf.ProtoMember(1, IsRequired = false, Name=@"errorCode", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int errorCode
    {
      get { return _errorCode; }
      set { _errorCode = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"OpenBonusItemRequest")]
  public partial class OpenBonusItemRequest : global::ProtoBuf.IExtensible
  {
    public OpenBonusItemRequest() {}
    
    private int _row = default(int);
    [global::ProtoBuf.ProtoMember(1, IsRequired = false, Name=@"row", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int row
    {
      get { return _row; }
      set { _row = value; }
    }
    private int _col = default(int);
    [global::ProtoBuf.ProtoMember(2, IsRequired = false, Name=@"col", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int col
    {
      get { return _col; }
      set { _col = value; }
    }
    private int _openAll = default(int);
    [global::ProtoBuf.ProtoMember(3, IsRequired = false, Name=@"openAll", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int openAll
    {
      get { return _openAll; }
      set { _openAll = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"BonusResultItemResponse")]
  public partial class BonusResultItemResponse : global::ProtoBuf.IExtensible
  {
    public BonusResultItemResponse() {}
    
    private int _row;
    [global::ProtoBuf.ProtoMember(1, IsRequired = true, Name=@"row", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public int row
    {
      get { return _row; }
      set { _row = value; }
    }
    private int _col;
    [global::ProtoBuf.ProtoMember(2, IsRequired = true, Name=@"col", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public int col
    {
      get { return _col; }
      set { _col = value; }
    }
    private int _money;
    [global::ProtoBuf.ProtoMember(3, IsRequired = true, Name=@"money", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public int money
    {
      get { return _money; }
      set { _money = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"BonusResultAllResponse")]
  public partial class BonusResultAllResponse : global::ProtoBuf.IExtensible
  {
    public BonusResultAllResponse() {}
    
    private readonly global::System.Collections.Generic.List<com.nope.fishing.BonusResultItemResponse> _bonusItems = new global::System.Collections.Generic.List<com.nope.fishing.BonusResultItemResponse>();
    [global::ProtoBuf.ProtoMember(1, Name=@"bonusItems", DataFormat = global::ProtoBuf.DataFormat.Default)]
    public global::System.Collections.Generic.List<com.nope.fishing.BonusResultItemResponse> bonusItems
    {
      get { return _bonusItems; }
    }
  
    private long _totalMoney = default(long);
    [global::ProtoBuf.ProtoMember(2, IsRequired = false, Name=@"totalMoney", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(long))]
    public long totalMoney
    {
      get { return _totalMoney; }
      set { _totalMoney = value; }
    }
    private int _errorCode = default(int);
    [global::ProtoBuf.ProtoMember(3, IsRequired = false, Name=@"errorCode", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int errorCode
    {
      get { return _errorCode; }
      set { _errorCode = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
}