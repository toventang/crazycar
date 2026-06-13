using QFramework;
using UnityEngine;

public class EnterGameState : AbstractState<LaunchStates, Launch>, IController {
    public EnterGameState(FSM<LaunchStates> fsm, Launch target) : base(fsm, target) {
    }

    public override void OnEnter() {
        this.GetSystem<IUISystem>().ShowPage(new ShowPageInfo(UIPageType.LoginUI));
        this.GetSystem<IUISystem>().HidePage(UIPageType.DownloadResUI);
    }

    public IArchitecture GetArchitecture() {
        return CrazyCar.Interface;
    }
}