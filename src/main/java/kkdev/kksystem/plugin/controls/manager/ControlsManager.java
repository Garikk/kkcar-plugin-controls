package kkdev.kksystem.plugin.controls.manager;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import kkdev.kksystem.base.classes.base.PinData;
import kkdev.kksystem.base.classes.base.PinDataFtrCtx;
import kkdev.kksystem.base.classes.base.PinDataTaggedObj;
import kkdev.kksystem.base.classes.base.PinDataTaggedString;
import kkdev.kksystem.base.classes.controls.PinDataControl;
import kkdev.kksystem.base.classes.plugins.simple.managers.PluginManagerControls;
import kkdev.kksystem.base.constants.PluginConsts;
import static kkdev.kksystem.base.constants.SystemConsts.KK_BASE_FEATURES_SYSTEM_MULTIFEATURE_UID;
import kkdev.kksystem.plugin.controls.KKPlugin;
import kkdev.kksystem.plugin.controls.adapters.IHWAdapter;
import kkdev.kksystem.plugin.controls.adapters.IHWAdapterCallback;
import kkdev.kksystem.plugin.controls.adapters.debug.DebugAdapterConsole;
import kkdev.kksystem.plugin.controls.adapters.rpi.RPIControlAdapter;
import kkdev.kksystem.plugin.controls.adapters.rpi.RPII2CAdapter;
import kkdev.kksystem.plugin.controls.adapters.smarthead.Smarthead;
import kkdev.kksystem.plugin.controls.adapters.unilinux.UNIL_RS232Adapter;
import kkdev.kksystem.plugin.controls.configuration.Adapter;
import kkdev.kksystem.plugin.controls.configuration.Control;
import kkdev.kksystem.plugin.controls.configuration.PluginSettings;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author blinov_is
 */
public class ControlsManager extends PluginManagerControls {

    private final IHWAdapterCallback AdapterCallback;
    HashMap<String, IHWAdapter> HWAdapters;
    IHWAdapter SmartheadAdapter;

    private String ___CurrentUIContext;

    public ControlsManager() {
        currentFeature = new HashMap<>();
        this.AdapterCallback = new IHWAdapterCallback() {
            @Override
            public void Control_Triggered(Control Ctrl) {
                Set<String> Target = GetTargetFeature(Ctrl);

                CONTROL_SendPluginMessageData(Target, GetTargetUIContext(Ctrl), Target, Ctrl.buttonID, PinDataControl.KK_CONTROL_DATA.CONTROL_TRIGGERED, 1);
            }

            @Override
            public void Control_SwitchOn(Control Ctrl) {
                Set<String> Target = GetTargetFeature(Ctrl);

                CONTROL_SendPluginMessageData(Target, GetTargetUIContext(Ctrl), Target, Ctrl.buttonID, PinDataControl.KK_CONTROL_DATA.CONTROL_ACTIVATE, 1);
            }

            @Override
            public void Control_SwitchOff(Control Ctrl) {
                Set<String> Target = GetTargetFeature(Ctrl);

                CONTROL_SendPluginMessageData(Target, GetTargetUIContext(Ctrl), Target, Ctrl.buttonID, PinDataControl.KK_CONTROL_DATA.CONTROL_DEACTIVATE, 0);

            }

            @Override
            public void Control_ChangeState(Control Ctrl, int State) {
                Set<String> Target = GetTargetFeature(Ctrl);

                CONTROL_SendPluginMessageData(Target, GetTargetUIContext(Ctrl), Target, Ctrl.buttonID, PinDataControl.KK_CONTROL_DATA.CONTROL_CHANGEVALUE, State);

            }

            @Override
            public void Control_LongPress(Control Ctrl, int State) {
                Set<String> Target = GetTargetFeature(Ctrl);

       
                    CONTROL_SendPluginMessageData(Target, GetTargetUIContext(Ctrl),Target, Ctrl.buttonID, PinDataControl.KK_CONTROL_DATA.CONTROL_LONGPRESS, State);

            }

            private Set<String> GetTargetFeature(Control Ctrl) {
                Set<String> Ret;

                if (Ctrl.FixedFeature) {
                    Ret = new LinkedHashSet<>();
                    Ret.add(Ctrl.FixedFeatureTarget);
                } else if (Ctrl.Global) {
                    Ret = new LinkedHashSet<>();
                    Ret.add(KK_BASE_FEATURES_SYSTEM_MULTIFEATURE_UID);
                    Ret.add(currentFeature.get(___CurrentUIContext));
                } else {
                    Ret = new LinkedHashSet<>();
                    Ret.add(currentFeature.get(___CurrentUIContext));
                }

                return Ret;
            }

            private String GetTargetUIContext(Control Ctrl) {
                if (Ctrl.FixedContext) {
                    return Ctrl.FixedContextTarget;
               // } else if (Ctrl.Global) {
                //    return KK_BASE_UICONTEXT_DEFAULT_MULTI;
                } else {
                    return ___CurrentUIContext;//Ctrl.CurrentUIContext;
                }
            }
        };
    }

    public void InitControls(KKPlugin PConnector) {
        setPluginConnector(PConnector);
        //connector = PConnector;
        //
        //Only one feature supported by now
        //
        PluginSettings.InitConfig(PConnector.globalConfID, PConnector.pluginInfo.getPluginInfo().PluginUUID);
        InitAdapters();
    }

    private void InitAdapters() {
        HWAdapters = new HashMap<>();

        for (Control CTR : PluginSettings.MainConfiguration.Controls) {
            //System.out.println("[CTL] " +CTR.Name + " " + CTR.AdapterID);
            if (!HWAdapters.containsKey(CTR.AdapterID)) {
                HWAdapters.put(CTR.AdapterID, CreateAdapter(CTR.AdapterID));
            }
            //
            HWAdapters.get(CTR.AdapterID).registerControl(CTR, AdapterCallback);
        }
    }

    private IHWAdapter CreateAdapter(String AdapterID) {
        for (Adapter ADP : PluginSettings.MainConfiguration.Adapters) {
            if (ADP.ID.equals(AdapterID)) {
                if (null != ADP.Type) {
                    switch (ADP.Type) {
                        case RaspberryPI_B: //Base RPI GPIO
                            return new RPIControlAdapter();
                        case Debug:         //Debug
                            return new DebugAdapterConsole();
                        case RaspberryPI_B_PI4J_I2C: // RPI i2c Bus
                            return new RPII2CAdapter(ADP);
                        case UniversalLinux_RS232:   // Universal rs232 bus
                            return new UNIL_RS232Adapter(ADP);
                        case KKSmarthead:   // Smarthead source
                            SmartheadAdapter = new Smarthead(ADP);
                            return SmartheadAdapter;
                        default:
                            break;
                    }
                }
            }
        }
        return new DebugAdapterConsole();
    }

    public void PluginStart() {
        for (String K : HWAdapters.keySet()) {
            HWAdapters.get(K).setActive();
        }
    }

    public void PluginStop() {
        for (String K : HWAdapters.keySet()) {
            HWAdapters.get(K).setInactive();
        }
    }

    public void ReceivePin(String PinName, PinData pinData) {
        switch (PinName) {
            case PluginConsts.KK_PLUGIN_BASE_PIN_COMMAND:
                ProcessBaseCommand((PinDataFtrCtx)pinData);
                break;
            case PluginConsts.KK_PLUGIN_BASE_BASIC_TAGGEDOBJ_DATA:
                ProcessStrPinData((PinDataTaggedString)pinData);
                break;
        }

    }

    private void ProcessStrPinData(PinDataTaggedString Obj) {
        if (Obj.tag.equals("SMARTHEAD")) {
            if (SmartheadAdapter != null) {
                SmartheadAdapter.receiveStringPin(Obj);
            }
        }
    }

    private void ProcessBaseCommand(PinDataFtrCtx Command) {
        switch (Command.managementCommand) {
            case ChangeFeature:
                ___CurrentUIContext = Command.manageUIContextID;
                if (!currentFeature.containsKey(Command.manageUIContextID)) {
                    currentFeature.put(Command.manageUIContextID, Command.manageFeatureID);
                }

                if (currentFeature.get(Command.manageUIContextID).equals(Command.manageFeatureID)) {
                    return;
                } else {
                    currentFeature.put(Command.manageUIContextID, Command.manageFeatureID);
                }
                break;
        }
    }

}
