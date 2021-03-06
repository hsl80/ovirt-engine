package org.ovirt.engine.ui.uicommonweb.models.datacenters;


import java.util.ArrayList;

import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class RemoveNetworkQoSModel extends ConfirmationModel {

    private final ListModel sourceListModel;

    public RemoveNetworkQoSModel(ListModel sourceListModel) {
        this.sourceListModel = sourceListModel;

        setTitle(ConstantsManager.getInstance().getConstants().removeNetworkQoSTitle());
        setMessage();
        UICommand tempVar = UICommand.createDefaultOkUiCommand("onRemove", this); //$NON-NLS-1$
        getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("cancel", this); //$NON-NLS-1$
        getCommands().add(tempVar2);
    }

    private void setMessage() {

        ArrayList<VdcQueryParametersBase> parameters = new ArrayList<>();
        ArrayList<VdcQueryType> queryTypes = new ArrayList<>();
        for (Object networkQoS : sourceListModel.getSelectedItems()) {
            VdcQueryParametersBase parameter = new IdQueryParameters(((NetworkQoS) networkQoS).getId());
            parameters.add(parameter);
            queryTypes.add(VdcQueryType.GetVnicProfilesByNetworkQosId);
        }
        Frontend.getInstance().runMultipleQueries(queryTypes, parameters, result -> {
            ArrayList<VnicProfileView> vnicProfiles = new ArrayList<>();

            setHelpTag(HelpTag.remove_network_qos);
            setHashName("remove_network_qos"); //$NON-NLS-1$

            for (VdcQueryReturnValue returnValue : result.getReturnValues()) {
                vnicProfiles.addAll((ArrayList<VnicProfileView>) returnValue.getReturnValue());
            }
            if (vnicProfiles.isEmpty()) {
                ArrayList<String> list = new ArrayList<>();
                for (Object item : sourceListModel.getSelectedItems()) {
                    NetworkQoS i = (NetworkQoS) item;
                    list.add(i.getName());
                }
                setItems(list);
            } else {
                setMessage(ConstantsManager.getInstance().getMessages().removeNetworkQoSMessage(vnicProfiles.size()));

                ArrayList<String> list = new ArrayList<>();
                for (VnicProfileView item : vnicProfiles) {
                    list.add(item.getName());
                }
                setItems(list);
            }
        });
    }

    public void onRemove() {
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<>();

        for (Object networkQoS : sourceListModel.getSelectedItems()) {
            QosParametersBase<NetworkQoS> parameter = new QosParametersBase<>();
            NetworkQoS tempQos = (NetworkQoS) networkQoS;
            parameter.setQos(tempQos);
            parameters.add(parameter);
        }
        Frontend.getInstance().runMultipleAction(VdcActionType.RemoveNetworkQoS, parameters);

        cancel();
    }

    private void cancel() {
        sourceListModel.setConfirmWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if ("onRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        } else if ("cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }
}
