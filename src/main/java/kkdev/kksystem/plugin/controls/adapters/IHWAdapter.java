/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kkdev.kksystem.plugin.controls.adapters;

import kkdev.kksystem.base.classes.base.PinDataTaggedString;
import kkdev.kksystem.plugin.controls.configuration.Control;

/**
 *
 * @author blinov_is
 */
public interface IHWAdapter {
    public void registerControl(Control Ctrl,IHWAdapterCallback Callback);
    public void setActive();
    public void setInactive();
    public void receiveStringPin(PinDataTaggedString PM);
}
