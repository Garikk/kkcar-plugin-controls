package kkdev.kksystem.plugin;



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import kkdev.kksystem.base.classes.plugins.PluginMessage;
import kkdev.kksystem.base.classes.plugins.simple.KKPluginBase;
import kkdev.kksystem.base.interfaces.IPluginBaseInterface;
import kkdev.kksystem.plugin.controls.manager.ControlsManager;

/**
 *
 * @author blinov_is
 */
public final class KKPlugin extends KKPluginBase {
    public KKPlugin() {
        super(new ControlsPluginInfo());
        Global.PM=new ControlsManager();
    }

    @Override
    public void PluginInit(IPluginBaseInterface BaseConnector) {
        super.PluginInit(BaseConnector);
        Global.PM.InitHID(this);
    }

   
    @Override
    public PluginMessage ExecutePin(PluginMessage Pin) {
        super.ExecutePin(Pin);
       Global.PM.ReceivePin(Pin.PinName, Pin.PinData);
        return null;
    }
}
