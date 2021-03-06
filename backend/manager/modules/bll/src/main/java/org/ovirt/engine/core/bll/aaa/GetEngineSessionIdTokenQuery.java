package org.ovirt.engine.core.bll.aaa;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetEngineSessionIdTokenQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    @Inject
    private SessionDataContainer sessionDataContainer;

    public GetEngineSessionIdTokenQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(sessionDataContainer.getSsoAccessToken(getParameters().getSessionId()));
        getQueryReturnValue().setSucceeded(true);
    }
}
