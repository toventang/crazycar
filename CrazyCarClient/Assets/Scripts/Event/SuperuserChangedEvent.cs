/// <summary>
/// 超级用户状态变更事件 —— 由 UserModel 在 IsSuperuser 值变化时发送<br/>
/// System 层订阅此事件来执行 UI 操作，避免 Model 绕过架构直接调用 System
/// </summary>
public class SuperuserChangedEvent {
    /// <summary>当前的超级用户状态</summary>
    public bool IsSuperuser { get; }

    public SuperuserChangedEvent(bool isSuperuser) {
        IsSuperuser = isSuperuser;
    }
}
