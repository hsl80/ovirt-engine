package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetRootTagQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    @Inject
    private TagsDirector tagsDirector;

    public GetRootTagQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(tagsDirector.getRootTag());
    }
}
