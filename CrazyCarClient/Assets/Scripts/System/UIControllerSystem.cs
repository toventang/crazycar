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
/// UIController 作为场景中的 MonoBehaviour 必须存在，IUISystem 提供符合框架架构的访问方式
/// </summary>
public class UIControllerSystem : AbstractSystem, IUISystem {
    public void ShowPage(ShowPageInfo info) => UIController.Instance.ShowPage(info);
    public UniTask<bool> ShowPageAsync(ShowPageInfo info) => UIController.Instance.ShowPageAsync(info);
    public void HidePage(UIPageType pageType) => UIController.Instance.HidePage(pageType);
    public void HidePageByLevel(UILevelType levelType) => UIController.Instance.HidePageByLevel(levelType);
    public void DestoryPageByLevel(UILevelType levelType) => UIController.Instance.DestoryPageByLevel(levelType);
    public UniTask InitUI() => UIController.Instance.InitUI();

    protected override void OnInit() { }
}
