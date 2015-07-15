package org.ovirt.engine.api.restapi.resource;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.GraphicsConsole;
import org.ovirt.engine.api.model.GraphicsType;
import org.ovirt.engine.api.model.ProxyTicket;
import org.ovirt.engine.api.resource.ApiMediaType;
import org.ovirt.engine.api.resource.VmGraphicsConsoleResource;
import org.ovirt.engine.api.resource.VmGraphicsConsolesResource;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.api.restapi.util.DisplayHelper;
import org.ovirt.engine.api.restapi.utils.HexUtils;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.console.ConsoleOptions;
import org.ovirt.engine.core.common.queries.ConfigureConsoleOptionsParams;
import org.ovirt.engine.core.common.queries.ConsoleOptionsParams;
import org.ovirt.engine.core.common.queries.GetSignedWebsocketProxyTicketParams;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmGraphicsConsoleResource
    extends BackendResource
    implements VmGraphicsConsoleResource {

    private VmGraphicsConsolesResource parent;
    private Guid vmGuid;
    private String consoleId;

    public BackendVmGraphicsConsoleResource(BackendVmGraphicsConsolesResource parent, Guid vmGuid, String consoleId) {
        this.parent = parent;
        this.vmGuid = vmGuid;
        this.consoleId = consoleId;
    }

    @Override
    public GraphicsConsole get() {
        List<String> supportedIds = new ArrayList<>();

        for (GraphicsConsole graphicsConsole : parent.list().getGraphicsConsoles()) {
            if (consoleId.equals(graphicsConsole.getId())) {
                return graphicsConsole;
            }

            supportedIds.add(graphicsConsole.getId());
        }

        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
    }

    @Override
    public Response generateDescriptor() {
        org.ovirt.engine.core.common.businessentities.GraphicsType graphicsType = asGraphicsType();

        ConsoleOptions consoleOptions = new ConsoleOptions(graphicsType);
        consoleOptions.setVmId(vmGuid);
        ConsoleOptions configuredOptions = runQuery(VdcQueryType.ConfigureConsoleOptions,
                new ConfigureConsoleOptionsParams(consoleOptions, true)).getReturnValue();

        VdcQueryReturnValue returnValue = runQuery(VdcQueryType.GetConsoleDescriptorFile, new ConsoleOptionsParams(configuredOptions));

        Response.ResponseBuilder builder;
        if (returnValue.getSucceeded() && returnValue.getReturnValue() != null) {
            builder = Response.ok(((String) returnValue.getReturnValue()).getBytes(Charset.forName("UTF-8")), ApiMediaType.APPLICATION_X_VIRT_VIEWER);
        } else {
            builder = Response.noContent();
        }

        return builder.build();
    }

    public org.ovirt.engine.core.common.businessentities.GraphicsType asGraphicsType() {
        String consoleString = HexUtils.hex2string(consoleId);
        validateGraphicsEnum(consoleString);

        GraphicsType type = GraphicsType.valueOf(consoleString);
        return getMappingLocator().getMapper(GraphicsType.class, org.ovirt.engine.core.common.businessentities.GraphicsType.class).map(type, null);
    }

    public void validateGraphicsEnum(String consoleString) {
        GraphicsConsole console = new GraphicsConsole();
        console.setProtocol(consoleString);
        validateEnums(GraphicsConsole.class, console);
    }

    @Override
    public Response remove() {
        org.ovirt.engine.core.common.businessentities.GraphicsType graphicsType = asGraphicsType();

        List<GraphicsDevice> devices = DisplayHelper.getGraphicsDevicesForEntity(this, vmGuid);
        if (devices == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
        }

        for (GraphicsDevice device : devices) {
            if (device.getGraphicsType().equals(graphicsType)) {
                return performAction(VdcActionType.RemoveGraphicsDevice, new GraphicsParameters(device));
            }
        }

        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
    }

    @Override
    public Response proxyTicket(Action action) {
        final String plainConsoleId = HexUtils.hex2string(consoleId);
        final GraphicsType graphicsTypeModel = GraphicsType.fromValue(plainConsoleId);
        final org.ovirt.engine.core.common.businessentities.GraphicsType graphicsTypeEntity =
                VmMapper.map(graphicsTypeModel, null);

        final String ticketValue = getTicket(graphicsTypeEntity);
        if (!action.isSetProxyTicket()) {
            action.setProxyTicket(new ProxyTicket());
        }
        action.getProxyTicket().setValue(ticketValue);
        return Response.ok().entity(action).build();
    }

    private String getTicket(org.ovirt.engine.core.common.businessentities.GraphicsType graphicsTypeEntity) {
        final GetSignedWebsocketProxyTicketParams params =
                new GetSignedWebsocketProxyTicketParams(vmGuid, graphicsTypeEntity);
        final VdcQueryReturnValue ticketQueryReturnValue =
                runQuery(VdcQueryType.GetSignedWebsocketProxyTicket, params);
        if (!ticketQueryReturnValue.getSucceeded()) {
            try {
                backendFailure(ticketQueryReturnValue.getExceptionString());
            } catch (BackendFailureException ex) {
                handleError(ex, false);
            }
        }
        return ticketQueryReturnValue.getReturnValue();
    }

}
