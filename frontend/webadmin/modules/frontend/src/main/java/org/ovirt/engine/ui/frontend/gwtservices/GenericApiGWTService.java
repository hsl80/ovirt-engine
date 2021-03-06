package org.ovirt.engine.ui.frontend.gwtservices;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.client.rpc.XsrfProtectedService;
import com.google.gwt.user.server.rpc.NoXsrfProtect;

@RemoteServiceRelativePath("GenericApiGWTService")
public interface GenericApiGWTService extends XsrfProtectedService {

    VdcQueryReturnValue runQuery(VdcQueryType search,
            VdcQueryParametersBase searchParameters);

    VdcReturnValueBase runAction(VdcActionType actionType,
            VdcActionParametersBase params);

    @NoXsrfProtect
    VdcQueryReturnValue runPublicQuery(VdcQueryType queryType,
            VdcQueryParametersBase params);

    ArrayList<VdcQueryReturnValue> runMultipleQueries(
            ArrayList<VdcQueryType> vdcQueryTypeList,
            ArrayList<VdcQueryParametersBase> paramsList);

    List<VdcReturnValueBase> runMultipleActions(
            VdcActionType actionType,
            ArrayList<VdcActionParametersBase> multipleParams,
            boolean isRunOnlyIfAllValidationPass);

    List<VdcReturnValueBase> runMultipleActions(
            VdcActionType actionType,
            ArrayList<VdcActionParametersBase> multipleParams,
            boolean isRunOnlyIfAllValidationPass, boolean isWaitForResult);

    void storeInHttpSession(String key, String value);

    String retrieveFromHttpSession(String key);

}
