//------------------------------------------------------------------------------
// <auto-generated>
//     This code was generated by a tool.
//
//     Changes to this file may cause incorrect behavior and will be lost if
//     the code is regenerated.
// </auto-generated>
//------------------------------------------------------------------------------

// Generated from: Proto/xmessage.proto
namespace com.nope.fishing
{
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"XMessage")]
  public partial class XMessage : global::ProtoBuf.IExtensible
  {
    public XMessage() {}
    
    private int _command;
    [global::ProtoBuf.ProtoMember(1, IsRequired = true, Name=@"command", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public int command
    {
      get { return _command; }
      set { _command = value; }
    }
    private int _beanType;
    [global::ProtoBuf.ProtoMember(2, IsRequired = true, Name=@"beanType", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public int beanType
    {
      get { return _beanType; }
      set { _beanType = value; }
    }
    private byte[] _data = null;
    [global::ProtoBuf.ProtoMember(3, IsRequired = false, Name=@"data", DataFormat = global::ProtoBuf.DataFormat.Default)]
    [global::System.ComponentModel.DefaultValue(null)]
    public byte[] data
    {
      get { return _data; }
      set { _data = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"PaymentItemResponseBean")]
  public partial class PaymentItemResponseBean : global::ProtoBuf.IExtensible
  {
    public PaymentItemResponseBean() {}
    
    private int _id;
    [global::ProtoBuf.ProtoMember(1, IsRequired = true, Name=@"id", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public int id
    {
      get { return _id; }
      set { _id = value; }
    }
    private int _type;
    [global::ProtoBuf.ProtoMember(2, IsRequired = true, Name=@"type", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public int type
    {
      get { return _type; }
      set { _type = value; }
    }
    private int _value;
    [global::ProtoBuf.ProtoMember(3, IsRequired = true, Name=@"value", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public int value
    {
      get { return _value; }
      set { _value = value; }
    }
    private int _realValue;
    [global::ProtoBuf.ProtoMember(4, IsRequired = true, Name=@"realValue", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public int realValue
    {
      get { return _realValue; }
      set { _realValue = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"RequestInitClientBean")]
  public partial class RequestInitClientBean : global::ProtoBuf.IExtensible
  {
    public RequestInitClientBean() {}
    
    private string _product_name;
    [global::ProtoBuf.ProtoMember(1, IsRequired = true, Name=@"product_name", DataFormat = global::ProtoBuf.DataFormat.Default)]
    public string product_name
    {
      get { return _product_name; }
      set { _product_name = value; }
    }
    private string _os_version;
    [global::ProtoBuf.ProtoMember(2, IsRequired = true, Name=@"os_version", DataFormat = global::ProtoBuf.DataFormat.Default)]
    public string os_version
    {
      get { return _os_version; }
      set { _os_version = value; }
    }
    private int _client_version;
    [global::ProtoBuf.ProtoMember(3, IsRequired = true, Name=@"client_version", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public int client_version
    {
      get { return _client_version; }
      set { _client_version = value; }
    }
    private string _mac_address = "";
    [global::ProtoBuf.ProtoMember(4, IsRequired = false, Name=@"mac_address", DataFormat = global::ProtoBuf.DataFormat.Default)]
    [global::System.ComponentModel.DefaultValue("")]
    public string mac_address
    {
      get { return _mac_address; }
      set { _mac_address = value; }
    }
    private string _mobile_country_code = "";
    [global::ProtoBuf.ProtoMember(5, IsRequired = false, Name=@"mobile_country_code", DataFormat = global::ProtoBuf.DataFormat.Default)]
    [global::System.ComponentModel.DefaultValue("")]
    public string mobile_country_code
    {
      get { return _mobile_country_code; }
      set { _mobile_country_code = value; }
    }
    private string _mobile_network_code = "";
    [global::ProtoBuf.ProtoMember(6, IsRequired = false, Name=@"mobile_network_code", DataFormat = global::ProtoBuf.DataFormat.Default)]
    [global::System.ComponentModel.DefaultValue("")]
    public string mobile_network_code
    {
      get { return _mobile_network_code; }
      set { _mobile_network_code = value; }
    }
    private int _width = default(int);
    [global::ProtoBuf.ProtoMember(7, IsRequired = false, Name=@"width", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int width
    {
      get { return _width; }
      set { _width = value; }
    }
    private int _height = default(int);
    [global::ProtoBuf.ProtoMember(8, IsRequired = false, Name=@"height", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int height
    {
      get { return _height; }
      set { _height = value; }
    }
    private int _platform;
    [global::ProtoBuf.ProtoMember(9, IsRequired = true, Name=@"platform", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public int platform
    {
      get { return _platform; }
      set { _platform = value; }
    }
    private string _device_id = "";
    [global::ProtoBuf.ProtoMember(10, IsRequired = false, Name=@"device_id", DataFormat = global::ProtoBuf.DataFormat.Default)]
    [global::System.ComponentModel.DefaultValue("")]
    public string device_id
    {
      get { return _device_id; }
      set { _device_id = value; }
    }
    private string _imie = "";
    [global::ProtoBuf.ProtoMember(11, IsRequired = false, Name=@"imie", DataFormat = global::ProtoBuf.DataFormat.Default)]
    [global::System.ComponentModel.DefaultValue("")]
    public string imie
    {
      get { return _imie; }
      set { _imie = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"InitClientResponseBean")]
  public partial class InitClientResponseBean : global::ProtoBuf.IExtensible
  {
    public InitClientResponseBean() {}
    
    private int _error_code;
    [global::ProtoBuf.ProtoMember(1, IsRequired = true, Name=@"error_code", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public int error_code
    {
      get { return _error_code; }
      set { _error_code = value; }
    }
    private string _description = "";
    [global::ProtoBuf.ProtoMember(2, IsRequired = false, Name=@"description", DataFormat = global::ProtoBuf.DataFormat.Default)]
    [global::System.ComponentModel.DefaultValue("")]
    public string description
    {
      get { return _description; }
      set { _description = value; }
    }
    private string _url_download = "";
    [global::ProtoBuf.ProtoMember(3, IsRequired = false, Name=@"url_download", DataFormat = global::ProtoBuf.DataFormat.Default)]
    [global::System.ComponentModel.DefaultValue("")]
    public string url_download
    {
      get { return _url_download; }
      set { _url_download = value; }
    }
    private string _link_fanpage = "";
    [global::ProtoBuf.ProtoMember(4, IsRequired = false, Name=@"link_fanpage", DataFormat = global::ProtoBuf.DataFormat.Default)]
    [global::System.ComponentModel.DefaultValue("")]
    public string link_fanpage
    {
      get { return _link_fanpage; }
      set { _link_fanpage = value; }
    }
    private long _serverTime = default(long);
    [global::ProtoBuf.ProtoMember(5, IsRequired = false, Name=@"serverTime", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(long))]
    public long serverTime
    {
      get { return _serverTime; }
      set { _serverTime = value; }
    }
    private bool _is_enable_iap = default(bool);
    [global::ProtoBuf.ProtoMember(6, IsRequired = false, Name=@"is_enable_iap", DataFormat = global::ProtoBuf.DataFormat.Default)]
    [global::System.ComponentModel.DefaultValue(default(bool))]
    public bool is_enable_iap
    {
      get { return _is_enable_iap; }
      set { _is_enable_iap = value; }
    }
    private int _new_version = default(int);
    [global::ProtoBuf.ProtoMember(7, IsRequired = false, Name=@"new_version", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    [global::System.ComponentModel.DefaultValue(default(int))]
    public int new_version
    {
      get { return _new_version; }
      set { _new_version = value; }
    }
    private string _link_store = "";
    [global::ProtoBuf.ProtoMember(8, IsRequired = false, Name=@"link_store", DataFormat = global::ProtoBuf.DataFormat.Default)]
    [global::System.ComponentModel.DefaultValue("")]
    public string link_store
    {
      get { return _link_store; }
      set { _link_store = value; }
    }
    private readonly global::System.Collections.Generic.List<com.nope.fishing.PaymentItemResponseBean> _paymentItem = new global::System.Collections.Generic.List<com.nope.fishing.PaymentItemResponseBean>();
    [global::ProtoBuf.ProtoMember(9, Name=@"paymentItem", DataFormat = global::ProtoBuf.DataFormat.Default)]
    public global::System.Collections.Generic.List<com.nope.fishing.PaymentItemResponseBean> paymentItem
    {
      get { return _paymentItem; }
    }
  
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
  [global::System.Serializable, global::ProtoBuf.ProtoContract(Name=@"DisconectBean")]
  public partial class DisconectBean : global::ProtoBuf.IExtensible
  {
    public DisconectBean() {}
    
    private int _error_code;
    [global::ProtoBuf.ProtoMember(1, IsRequired = true, Name=@"error_code", DataFormat = global::ProtoBuf.DataFormat.TwosComplement)]
    public int error_code
    {
      get { return _error_code; }
      set { _error_code = value; }
    }
    private global::ProtoBuf.IExtension extensionObject;
    global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
      { return global::ProtoBuf.Extensible.GetExtensionObject(ref extensionObject, createIfMissing); }
  }
  
}