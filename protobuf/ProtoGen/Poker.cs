//------------------------------------------------------------------------------
// <auto-generated>
//     This code was generated by a tool.
//
//     Changes to this file may cause incorrect behavior and will be lost if
//     the code is regenerated.
// </auto-generated>
//------------------------------------------------------------------------------

// Generated from: Poker.proto
namespace com.nope.fishing
{
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"Poker_Response_RoomIdFund")]
  public partial class Poker_Response_RoomIdFund : global::ProtoBuf.IExtensible
  {
    public Poker_Response_RoomIdFund() {}
    
    private int _roomid = default(int);
    [global::ProtoBuf.ProtoMember(1, IsRequired = false, Name=@"roomid", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int roomid
    {
      get { return _roomid; }
      set { _roomid = value; }
    }
    private long _fund = default(long);
    [global::ProtoBuf.ProtoMember(2, IsRequired = false, Name=@"fund", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(long))]
    public long fund
    {
      get { return _fund; }
      set { _fund = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"Poker_Response_GameInfo")]
  public partial class Poker_Response_GameInfo : global::ProtoBuf.IExtensible
  {
    public Poker_Response_GameInfo() {}
    
    private readonly global::System.Collections.Generic.List<com.nope.fishing.Poker_Response_RoomInfo> _room = new global::System.Collections.Generic.List<com.nope.fishing.Poker_Response_RoomInfo>();
    [global::ProtoBuf.ProtoMember(1, Name=@"room", DataFormat = global::ProtoBuf.DataFormat.Default)]
    public global::System.Collections.Generic.List<com.nope.fishing.Poker_Response_RoomInfo> room
    {
      get { return _room; }
    }
  
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"Poker_Response_AllCountDown_10Room")]
  public partial class Poker_Response_AllCountDown_10Room : global::ProtoBuf.IExtensible
  {
    public Poker_Response_AllCountDown_10Room() {}
    
    private readonly global::System.Collections.Generic.List<com.nope.fishing.Poker_Response_CountDown> _room = new global::System.Collections.Generic.List<com.nope.fishing.Poker_Response_CountDown>();
    [global::ProtoBuf.ProtoMember(1, Name=@"room", DataFormat = global::ProtoBuf.DataFormat.Default)]
    public global::System.Collections.Generic.List<com.nope.fishing.Poker_Response_CountDown> room
    {
      get { return _room; }
    }
  
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"Poker_Response_UpdateMoney")]
  public partial class Poker_Response_UpdateMoney : global::ProtoBuf.IExtensible
  {
    public Poker_Response_UpdateMoney() {}
    
    private long _money = default(long);
    [global::ProtoBuf.ProtoMember(1, IsRequired = false, Name=@"money", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(long))]
    public long money
    {
      get { return _money; }
      set { _money = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"Poker_Request_JoinFakePlayer")]
  public partial class Poker_Request_JoinFakePlayer : global::ProtoBuf.IExtensible
  {
    public Poker_Request_JoinFakePlayer() {}
    
    private int _RoomID = default(int);
    [global::ProtoBuf.ProtoMember(1, IsRequired = false, Name=@"RoomID", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int RoomID
    {
      get { return _RoomID; }
      set { _RoomID = value; }
    }
    private int _id = default(int);
    [global::ProtoBuf.ProtoMember(2, IsRequired = false, Name=@"id", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int id
    {
      get { return _id; }
      set { _id = value; }
    }
    private string _username = "";
    [global::ProtoBuf.ProtoMember(3, IsRequired = false, Name=@"username", DataFormat = global::ProtoBuf.DataFormat.Default)]
    [global::System.ComponentModel.DefaultValue("")]
    public string username
    {
      get { return _username; }
      set { _username = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"Poker_Request_JoinLobby")]
  public partial class Poker_Request_JoinLobby : global::ProtoBuf.IExtensible
  {
    public Poker_Request_JoinLobby() {}
    
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"Poker_Request_LeaveLobby")]
  public partial class Poker_Request_LeaveLobby : global::ProtoBuf.IExtensible
  {
    public Poker_Request_LeaveLobby() {}
    
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"Poker_Request_JoinRoom")]
  public partial class Poker_Request_JoinRoom : global::ProtoBuf.IExtensible
  {
    public Poker_Request_JoinRoom() {}
    
    private int _roomid = default(int);
    [global::ProtoBuf.ProtoMember(1, IsRequired = false, Name=@"roomid", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int roomid
    {
      get { return _roomid; }
      set { _roomid = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"Poker_Request_LeaveRoom")]
  public partial class Poker_Request_LeaveRoom : global::ProtoBuf.IExtensible
  {
    public Poker_Request_LeaveRoom() {}
    
    private int _roomid = default(int);
    [global::ProtoBuf.ProtoMember(1, IsRequired = false, Name=@"roomid", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int roomid
    {
      get { return _roomid; }
      set { _roomid = value; }
    }
    private int _errorCode = default(int);
    [global::ProtoBuf.ProtoMember(2, IsRequired = false, Name=@"errorCode", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
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
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"Poker_Response_RoomInfo")]
  public partial class Poker_Response_RoomInfo : global::ProtoBuf.IExtensible
  {
    public Poker_Response_RoomInfo() {}
    
    private int _roomId = default(int);
    [global::ProtoBuf.ProtoMember(1, IsRequired = false, Name=@"roomId", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int roomId
    {
      get { return _roomId; }
      set { _roomId = value; }
    }
    private int _roomCurrentPhase = default(int);
    [global::ProtoBuf.ProtoMember(8, IsRequired = false, Name=@"roomCurrentPhase", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int roomCurrentPhase
    {
      get { return _roomCurrentPhase; }
      set { _roomCurrentPhase = value; }
    }
    private long _roomPot = default(long);
    [global::ProtoBuf.ProtoMember(9, IsRequired = false, Name=@"roomPot", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(long))]
    public long roomPot
    {
      get { return _roomPot; }
      set { _roomPot = value; }
    }
    private long _roomNewMinBet = default(long);
    [global::ProtoBuf.ProtoMember(18, IsRequired = false, Name=@"roomNewMinBet", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(long))]
    public long roomNewMinBet
    {
      get { return _roomNewMinBet; }
      set { _roomNewMinBet = value; }
    }
    private int _roomIDOfNextDealer = default(int);
    [global::ProtoBuf.ProtoMember(22, IsRequired = false, Name=@"roomIDOfNextDealer", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int roomIDOfNextDealer
    {
      get { return _roomIDOfNextDealer; }
      set { _roomIDOfNextDealer = value; }
    }
    private readonly global::System.Collections.Generic.List<int> _roomLastAction = new global::System.Collections.Generic.List<int>();
    [global::ProtoBuf.ProtoMember(19, Name=@"roomLastAction", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public global::System.Collections.Generic.List<int> roomLastAction
    {
      get { return _roomLastAction; }
    }
  
    private readonly global::System.Collections.Generic.List<int> _roomJoinedGameID = new global::System.Collections.Generic.List<int>();
    [global::ProtoBuf.ProtoMember(10, Name=@"roomJoinedGameID", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public global::System.Collections.Generic.List<int> roomJoinedGameID
    {
      get { return _roomJoinedGameID; }
    }
  
    private readonly global::System.Collections.Generic.List<bool> _roomTurn = new global::System.Collections.Generic.List<bool>();
    [global::ProtoBuf.ProtoMember(11, Name=@"roomTurn", DataFormat = global::ProtoBuf.DataFormat.Default)]
    public global::System.Collections.Generic.List<bool> roomTurn
    {
      get { return _roomTurn; }
    }
  
    private readonly global::System.Collections.Generic.List<int> _roomDealerTurn = new global::System.Collections.Generic.List<int>();
    [global::ProtoBuf.ProtoMember(12, Name=@"roomDealerTurn", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public global::System.Collections.Generic.List<int> roomDealerTurn
    {
      get { return _roomDealerTurn; }
    }
  
    private readonly global::System.Collections.Generic.List<long> _roomBetAmount = new global::System.Collections.Generic.List<long>();
    [global::ProtoBuf.ProtoMember(17, Name=@"roomBetAmount", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public global::System.Collections.Generic.List<long> roomBetAmount
    {
      get { return _roomBetAmount; }
    }
  
    private readonly global::System.Collections.Generic.List<long> _roomBetAmountTotal = new global::System.Collections.Generic.List<long>();
    [global::ProtoBuf.ProtoMember(23, Name=@"roomBetAmountTotal", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public global::System.Collections.Generic.List<long> roomBetAmountTotal
    {
      get { return _roomBetAmountTotal; }
    }
  
    private readonly global::System.Collections.Generic.List<com.nope.fishing.Poker_Player> _roomPlayerCardsHolder = new global::System.Collections.Generic.List<com.nope.fishing.Poker_Player>();
    [global::ProtoBuf.ProtoMember(13, Name=@"roomPlayerCardsHolder", DataFormat = global::ProtoBuf.DataFormat.Default)]
    public global::System.Collections.Generic.List<com.nope.fishing.Poker_Player> roomPlayerCardsHolder
    {
      get { return _roomPlayerCardsHolder; }
    }
  
    private readonly global::System.Collections.Generic.List<com.nope.fishing.Poker_Player> _roomPlayerAvailableAction = new global::System.Collections.Generic.List<com.nope.fishing.Poker_Player>();
    [global::ProtoBuf.ProtoMember(14, Name=@"roomPlayerAvailableAction", DataFormat = global::ProtoBuf.DataFormat.Default)]
    public global::System.Collections.Generic.List<com.nope.fishing.Poker_Player> roomPlayerAvailableAction
    {
      get { return _roomPlayerAvailableAction; }
    }
  
    private readonly global::System.Collections.Generic.List<com.nope.fishing.Poker_Cards> _roomAll52Cards = new global::System.Collections.Generic.List<com.nope.fishing.Poker_Cards>();
    [global::ProtoBuf.ProtoMember(15, Name=@"roomAll52Cards", DataFormat = global::ProtoBuf.DataFormat.Default)]
    public global::System.Collections.Generic.List<com.nope.fishing.Poker_Cards> roomAll52Cards
    {
      get { return _roomAll52Cards; }
    }
  
    private readonly global::System.Collections.Generic.List<com.nope.fishing.Poker_Cards> _roomCommunityCards = new global::System.Collections.Generic.List<com.nope.fishing.Poker_Cards>();
    [global::ProtoBuf.ProtoMember(16, Name=@"roomCommunityCards", DataFormat = global::ProtoBuf.DataFormat.Default)]
    public global::System.Collections.Generic.List<com.nope.fishing.Poker_Cards> roomCommunityCards
    {
      get { return _roomCommunityCards; }
    }
  
    private readonly global::System.Collections.Generic.List<long> _roomCardStrenght = new global::System.Collections.Generic.List<long>();
    [global::ProtoBuf.ProtoMember(20, Name=@"roomCardStrenght", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public global::System.Collections.Generic.List<long> roomCardStrenght
    {
      get { return _roomCardStrenght; }
    }
  
    private readonly global::System.Collections.Generic.List<int> _roomJoinedGameIDFOLDER = new global::System.Collections.Generic.List<int>();
    [global::ProtoBuf.ProtoMember(21, Name=@"roomJoinedGameIDFOLDER", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public global::System.Collections.Generic.List<int> roomJoinedGameIDFOLDER
    {
      get { return _roomJoinedGameIDFOLDER; }
    }
  
    private readonly global::System.Collections.Generic.List<int> _roomJoinedGameIDOUTOFMONEY = new global::System.Collections.Generic.List<int>();
    [global::ProtoBuf.ProtoMember(24, Name=@"roomJoinedGameIDOUTOFMONEY", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public global::System.Collections.Generic.List<int> roomJoinedGameIDOUTOFMONEY
    {
      get { return _roomJoinedGameIDOUTOFMONEY; }
    }
  
    private readonly global::System.Collections.Generic.List<int> _userid = new global::System.Collections.Generic.List<int>();
    [global::ProtoBuf.ProtoMember(3, Name=@"userid", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public global::System.Collections.Generic.List<int> userid
    {
      get { return _userid; }
    }
  
    private readonly global::System.Collections.Generic.List<string> _username = new global::System.Collections.Generic.List<string>();
    [global::ProtoBuf.ProtoMember(4, Name=@"username", DataFormat = global::ProtoBuf.DataFormat.Default)]
    public global::System.Collections.Generic.List<string> username
    {
      get { return _username; }
    }
  
    private readonly global::System.Collections.Generic.List<long> _usermoney = new global::System.Collections.Generic.List<long>();
    [global::ProtoBuf.ProtoMember(5, Name=@"usermoney", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public global::System.Collections.Generic.List<long> usermoney
    {
      get { return _usermoney; }
    }
  
    private readonly global::System.Collections.Generic.List<bool> _userBeginTurn = new global::System.Collections.Generic.List<bool>();
    [global::ProtoBuf.ProtoMember(6, Name=@"userBeginTurn", DataFormat = global::ProtoBuf.DataFormat.Default)]
    public global::System.Collections.Generic.List<bool> userBeginTurn
    {
      get { return _userBeginTurn; }
    }
  
    private readonly global::System.Collections.Generic.List<int> _userAction = new global::System.Collections.Generic.List<int>();
    [global::ProtoBuf.ProtoMember(7, Name=@"userAction", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public global::System.Collections.Generic.List<int> userAction
    {
      get { return _userAction; }
    }
  
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"Poker_Response_CountDown")]
  public partial class Poker_Response_CountDown : global::ProtoBuf.IExtensible
  {
    public Poker_Response_CountDown() {}
    
    private int _roomId = default(int);
    [global::ProtoBuf.ProtoMember(1, IsRequired = false, Name=@"roomId", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int roomId
    {
      get { return _roomId; }
      set { _roomId = value; }
    }
    private long _roomCountdown = default(long);
    [global::ProtoBuf.ProtoMember(2, IsRequired = false, Name=@"roomCountdown", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(long))]
    public long roomCountdown
    {
      get { return _roomCountdown; }
      set { _roomCountdown = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"Poker_Player")]
  public partial class Poker_Player : global::ProtoBuf.IExtensible
  {
    public Poker_Player() {}
    
    private int _playerID = default(int);
    [global::ProtoBuf.ProtoMember(1, IsRequired = false, Name=@"playerID", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int playerID
    {
      get { return _playerID; }
      set { _playerID = value; }
    }
    private readonly global::System.Collections.Generic.List<com.nope.fishing.Poker_Cards> _playerCardHolder = new global::System.Collections.Generic.List<com.nope.fishing.Poker_Cards>();
    [global::ProtoBuf.ProtoMember(2, Name=@"playerCardHolder", DataFormat = global::ProtoBuf.DataFormat.Default)]
    public global::System.Collections.Generic.List<com.nope.fishing.Poker_Cards> playerCardHolder
    {
      get { return _playerCardHolder; }
    }
  
    private readonly global::System.Collections.Generic.List<int> _playerAvailableAction = new global::System.Collections.Generic.List<int>();
    [global::ProtoBuf.ProtoMember(3, Name=@"playerAvailableAction", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public global::System.Collections.Generic.List<int> playerAvailableAction
    {
      get { return _playerAvailableAction; }
    }
  
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"Poker_Cards")]
  public partial class Poker_Cards : global::ProtoBuf.IExtensible
  {
    public Poker_Cards() {}
    
    private string _cardnumber = "";
    [global::ProtoBuf.ProtoMember(1, IsRequired = false, Name=@"cardnumber", DataFormat = global::ProtoBuf.DataFormat.Default)]
    [global::System.ComponentModel.DefaultValue("")]
    public string cardnumber
    {
      get { return _cardnumber; }
      set { _cardnumber = value; }
    }
    private string _cardtype = "";
    [global::ProtoBuf.ProtoMember(2, IsRequired = false, Name=@"cardtype", DataFormat = global::ProtoBuf.DataFormat.Default)]
    [global::System.ComponentModel.DefaultValue("")]
    public string cardtype
    {
      get { return _cardtype; }
      set { _cardtype = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"Poker_TurnOver")]
  public partial class Poker_TurnOver : global::ProtoBuf.IExtensible
  {
    public Poker_TurnOver() {}
    
    private int _roomid = default(int);
    [global::ProtoBuf.ProtoMember(1, IsRequired = false, Name=@"roomid", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int roomid
    {
      get { return _roomid; }
      set { _roomid = value; }
    }
    private int _playerid = default(int);
    [global::ProtoBuf.ProtoMember(2, IsRequired = false, Name=@"playerid", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int playerid
    {
      get { return _playerid; }
      set { _playerid = value; }
    }
    private int _action = default(int);
    [global::ProtoBuf.ProtoMember(3, IsRequired = false, Name=@"action", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int action
    {
      get { return _action; }
      set { _action = value; }
    }
    private long _currentMoney_Phase = default(long);
    [global::ProtoBuf.ProtoMember(4, IsRequired = false, Name=@"currentMoney_Phase", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(long))]
    public long currentMoney_Phase
    {
      get { return _currentMoney_Phase; }
      set { _currentMoney_Phase = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"Poker_NEWGAME")]
  public partial class Poker_NEWGAME : global::ProtoBuf.IExtensible
  {
    public Poker_NEWGAME() {}
    
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"Poker_Request_Bet")]
  public partial class Poker_Request_Bet : global::ProtoBuf.IExtensible
  {
    public Poker_Request_Bet() {}
    
    private int _userid = default(int);
    [global::ProtoBuf.ProtoMember(1, IsRequired = false, Name=@"userid", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int userid
    {
      get { return _userid; }
      set { _userid = value; }
    }
    private long _betAmount = default(long);
    [global::ProtoBuf.ProtoMember(3, IsRequired = false, Name=@"betAmount", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(long))]
    public long betAmount
    {
      get { return _betAmount; }
      set { _betAmount = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
}