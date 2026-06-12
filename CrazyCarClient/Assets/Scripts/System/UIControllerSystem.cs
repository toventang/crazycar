using QFramework;
using Cysharp.Threading.Tasks;

/// <summary>
/// UI 控制系统接口 —— 通过框架 IoC 访问 UIController，避免直接在 System/Model/Command 中引用静态单例
/// </summary>
public interface IUISystem : ISystem {
    void ShowPage(ShowPageInfo info);
    UniTask<bool> ShowPageAsync(ShowPageInfo info);
    void HidePage(UIPageType pageType);
    void HidePageByLevel(UILevelType levelType);
    void DestoryPageByLevel(UILevelType levelType);
    UniTask InitUI();
}

/// <summary>
/// UI 控制系统实现 —— 薄包装层，委托给 UIController.Instance<br/>
/// UIController 作为场景中的 MonoBehaviour 必须存在，IUISystem 提供符合框架架构的访问方式<br/>
/// 同时负责处理需要驱动 UI 的系统级事件（如 SuperuserChangedEvent）
/// </summary>
public class UIControllerSystem : AbstractSystem, IUISystem {
    public void ShowPage(ShowPageInfo info) => UIController.Instance.ShowPage(info);
    public UniTask<bool> ShowPageAsync(ShowPageInfo info) => UIController.Instance.ShowPageAsync(info);
    public void HidePage(UIPageType pageType) => UIController.Instance.HidePage(pageType);
    public void HidePageByLevel(UILevelType levelType) => UIController.Instance.HidePageByLevel(levelType);
    public void DestoryPageByLevel(UILevelType levelType) => UIController.Instance.DestoryPageByLevel(levelType);
    public UniTask InitUI() => UIController.Instance.InitUI();

    /// <summary>
    /// 订阅超级用户状态变更事件，处理 GameHelper 调试页面的显示/隐藏<br/>
    /// 将原本在 UserModel 中绕过架构直接调用 System 的逻辑移至此处的 System 层统一处理
    /// </summary>
    protected override void OnInit() {
        this.RegisterEvent<SuperuserChangedEvent>(e => {
            if (e.IsSuperuser) {
                ShowPage(new ShowPageInfo(UIPageType.GameHelper, UILevelType.Debug));
            } else {
                HidePage(UIPageType.GameHelper);
            }
        });
    }
}
